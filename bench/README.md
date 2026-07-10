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
| old `GridPanel.paintComponent` | 557 | 439,740 | 166 us |
| new `GridPanel.paintComponent` | 41 | 6,272 + a 180,300 px blit | 65 us |
| new movement cycle, clipped | 41 | 13,500 clip | 44 us |
| `bakeStaticZone`, whole board | 512 | 255,264 | 227 us |
| `bakeStaticZone`, a four-row band | 176 | 61,088 | -- |
| `bakeStaticZone`, one landing (2x3 cells) | **33** | 9,320 | -- |
| old `InfoPanel.paintComponent` | -- | -- | 81 us |
| new `InfoPanel.paintComponent` | -- | -- | 85 us |

The 96 landed blocks cost 480 primitives, because `fill3DRect` is five of them.
The rewrite moved that cost out of every paint and into every landing, and the
clip-bounded loops then shrank a landing from 512 primitives to 33.

### How often each runs

This is where the whole thing is won, and it is not visible in the table above.

| | old | new |
|---|---|---|
| `GridPanel` full paint | 60 / sec | never, during play |
| `GridPanel` clipped paint | -- | one per piece movement |
| `InfoPanel` paint | 60 / sec | on a score or drop-speed change |
| board redraw (480 primitives) | 60 / sec | once per landing, footprint-sized |

Estimating a movement rate of ~30/sec under DAS and ~20 score updates/sec under
soft drop, EDT time spent painting falls from about **14.8 ms per second of play**
to about **3.1 ms**, and `GridPanel` primitives issued fall from **33,420/sec** to
roughly **1,300/sec**. The second figure is the one CheerpJ bills for.

### Input blackout

`TetrisKeyListener.handleKeyInput` ignores every key while
`game.blockTimer.isRunning()` is false, and `holdForAnimation()` stops that timer.
The 100 ms placement pulse was therefore swallowing all input after *every piece*.
`AbstractAnimatedRenderPhase.haltsGameplay()` now lets the pulse play over a
running game; only the line clear, which dissolves rows already deleted from the
board, still halts. That removes roughly a tenth of a second of dropped input per
landing.

### What the profiling turned up, and what was done about it

**`tick()` used to dirty the whole tree.** It called `repaint()` on the
JLayeredPane at every 16 ms input poll. A `RepaintManager` probe showed it
requesting `684x601` while the phased renderer's own `repaintZone` requested
`60x90`; Swing unions those, so every panel repainted fully at 60 Hz and the
per-zone clipping never took effect during play. `tick()` now repaints the
`InfoPanel` alone, and only when the drop interval has actually moved. The probe
now reports nothing dirtied.

**The bake's clip masked work rather than avoiding it.** `bakeStaticZone` set a
clip and then drew the whole board; Java2D discarded the pixels outside it, but
only after all 480 primitives had been issued. `drawStaticBoardBlocks` and
`BoardRegionRenderPhase` now derive their cell range from `getClipBounds()`, so a
four-row band bakes 176 primitives instead of 512, and a landing's bake is
proportional to the piece's footprint.

**The keycap legend was redrawn 60 times a second.** Six caps are twelve `Path2D`
fills and twelve strings. It is now baked once into an opaque `BufferedImage`,
keyed by `(width, height, showControls)`. An opaque cache matters: a translucent
one measured barely faster than drawing the caps outright, because the blit then
alpha-composites every pixel.

## Caveats

- Offscreen software pipeline, not the real onscreen D3D/OpenGL one. Ratios
  travel; absolute microseconds do not.
- Both paths pay identical reflection overhead per paint.
- `-Xint` is a bracket, not a proxy for CheerpJ: it slows our bytecode while
  leaving OpenJDK's *native* Java2D fill loops untouched, and CheerpJ has no
  native loops at all.
- The old `InfoPanel` is timed against a stub `GameController`, because the
  pre-refactor music tree no longer compiles against today's ChiptuneSynth jar.
