# Render benchmarks

Comparing the phased renderer against the panel it replaced (`e2c2fc7`, the last
commit before the rewrite).

## Running

The new bench compiles against the working tree. The old one needs the
pre-refactor sources, which only requires four files:

```sh
OLD=/tmp/old && mkdir -p $OLD/javarominoes/{model,view}
for f in model/Board.java model/Pieces.java model/TetrominoGraphics.java view/BoardPanel.java; do
  git show e2c2fc7:src/javarominoes/$f > $OLD/javarominoes/$f
done
javac -d /tmp/co $(find $OLD -name '*.java')
javac -d /tmp/bo -cp /tmp/co bench/BenchSupport.java bench/CountingGraphics.java bench/RenderBenchOld.java

javac -d /tmp/cn -cp ../ChiptuneSynth/dist/ChiptuneSynth.jar $(find src -name '*.java')
javac -d /tmp/bn -cp "/tmp/cn:../ChiptuneSynth/dist/ChiptuneSynth.jar" bench/*.java
```

Then:

| what | how |
|---|---|
| throughput + EDT contention | `java -cp /tmp/bo:/tmp/co RenderBenchOld` |
| primitive counts | `java -Dbench.count=true -cp ... RenderBenchNew` |
| EDT only, inflated cost | `java -Xint -XX:ActiveProcessorCount=1 -Dbench.edtOnly=true ...` |

Knobs: `bench.bpx`, `bench.iters`, `bench.warmup`, `bench.frameMs`,
`bench.pollMs`, `bench.seconds`, `bench.count`, `bench.edtOnly`.

## What each number means

`RenderBench*` drives `GridPanel.paintComponent` directly, by reflection, into an
offscreen `BufferedImage`. It never shows a window, so it measures the panel and
not the compositor.

`CountingGraphics` counts the AWT primitives a paint issues. This matters because
**the JVM and the browser bill in different currencies.** Java2D rasterizes in
software, so its cost tracks pixel area. CheerpJ maps AWT windows onto HTML5
canvases, so its cost tracks the number of primitives issued, each crossing out
of WebAssembly, very nearly regardless of area. A change can be a small win by
one measure and a large one by the other.

Note that `java.awt.Graphics.fill3DRect` is concrete: it decomposes into one
`fillRect` and four `drawLine`s, and allocates two `Color`s via `brighter()` and
`darker()`. Every block drawn is five primitives, not one.

`BenchSupport.edtContention` drives the paint from a Swing Timer on the EDT while
an off-thread scheduler posts a synthetic keystroke every `pollMs` and times how
long it waits to run. Paint and keystroke share the one thread, which is
CheerpJ's constraint reproduced on the desktop. The latency is posted from off
the EDT on purpose: a Swing Timer would coalesce, and its period is floored by
the platform's ~15.6 ms timer granularity, which swamps the figure sought.

## Results

30 px blocks, 300x600 grid, 96 landed blocks, warm C2, 20k iterations.

### Per operation

| operation | primitives | filled px | JVM time |
|---|---|---|---|
| old `GridPanel.paintComponent` | 557 | 439,740 | 165 us |
| new `GridPanel.paintComponent` | 41 | 6,272 + a 180,300 px blit | 66 us |
| new `bakeStaticZone` (per landing) | 517 | 433,468 | 225 us |
| old `InfoPanel.paintComponent` | -- | -- | 81 us |
| new `InfoPanel.paintComponent` | -- | -- | 114 us |

The 96 landed blocks cost 480 primitives. The rewrite moved them out of every
paint and into every landing.

### Per frame, in steady state

`GameController.tick()` calls `repaint()` on the JLayeredPane every 16 ms from
`TetrisKeyListener`, which dirties the whole 684x601 tree. A `RepaintManager`
probe confirms it: `tick()` requests `684x601`, while the phased renderer's own
`repaintZone` requests `60x90`. Swing unions them, so every panel repaints fully
at 60 Hz and the per-zone clipping never takes effect during play.

That is a bug, not a property of the design. What survives it is the part that
matters: the phase queues still gate the *bake*, so a full-bounds clip blits an
already-correct `BufferedImage` rather than redrawing 96 blocks.

| | old | new |
|---|---|---|
| primitives / frame | ~585 | ~117 |
| EDT us / frame (GridPanel + InfoPanel) | 246 us | 180 us |

So the browser, which bills per primitive, sees ~5x less work; the desktop, which
bills per pixel, sees ~1.4x. The new `InfoPanel`'s six `Path2D` keycaps ate a
third of the desktop win.

## Caveats

- Offscreen software pipeline, not the real onscreen D3D/OpenGL one. Ratios
  travel; absolute microseconds do not.
- Both paths pay identical reflection overhead per paint.
- `-Xint` is a bracket, not a proxy for CheerpJ: it slows our bytecode while
  leaving OpenJDK's *native* Java2D fill loops untouched, and CheerpJ has no
  native loops at all.
- The old `InfoPanel` is timed against a stub `GameController`, because the
  pre-refactor music tree no longer compiles against today's ChiptuneSynth jar.
