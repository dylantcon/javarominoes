package javarominoes.view;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javarominoes.model.TetrominoGraphics;
import javarominoes.model.Board;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

/**
 *
 * @author dylan
 */
public class ParallaxScrollPanel extends JPanel implements ActionListener, ComponentListener {

  /* CONSTANTS */

  private final static int DEFAULT_BACKING_LAYER_NUM = 6;
  private final static int DEFAULT_HZ = 60;
  private final static int SEC_TO_MSECS = 1000;

  private final static float DEFAULT_COMMON_RATIO = 0.80f;

  // Pinned foreground block size in pixels. Previously derived at runtime from
  // Toolkit.getScreenResolution() * 0.5in, which on a standard 96-DPI display
  // yields 48px. That DPI is environment-dependent: under CheerpJ the browser
  // reports a different screen resolution, so the parallax geometry would drift
  // from the desktop build and break pixel-identical fidelity. Pin it instead.
  // NOTE: if your dev machine does NOT report 96 DPI, set this to whatever
  // (int)(Toolkit.getDefaultToolkit().getScreenResolution() * 0.5f) prints there.
  private final static int BASE_BLOCK_SIZE_PX = 48;

  private final static float SCROLL_SPD_BLOCK_PER_SEC = 2.0f;

  /**
   * fifteen percent of the panel's total height must be used to render a blank
   * area at the panel's top. The y coordinate yielded from the product of the
   * height of the panel and this float represents the absolute height limit of
   * any backing layer regardless of count.
   */
  private final static float TOP_BLANK_AREA = 0.15f;

  /* ADAPTIVE FRAME GOVERNOR CONSTANTS */

  // The timer delay is steered toward measuredFrameCost * COST_HEADROOM, so a
  // fixed fraction of each period is guaranteed idle EDT time for input. 1.5
  // targets a ~67% duty cycle.
  private final static float COST_HEADROOM = 1.5f;

  // delay is clamped to [base, base * MAX_THROTTLE_FACTOR] — at a 60 Hz base
  // the animation never drops below 15 fps no matter how slow the host is
  private final static int MAX_THROTTLE_FACTOR = 4;

  // smoothing factor for the frame-cost EMA (~ last 30 frames dominate)
  private final static float COST_EMA_ALPHA = 0.1f;

  // re-evaluate the delay only this often, and only act on changes of at
  // least the deadband, so the timer isn't reprogrammed over noise
  private final static int GOVERN_EVERY_TICKS = 30;
  private final static int GOVERN_DEADBAND_MS = 4;

  // discard absurd single-frame cost samples (GC pause, tab switch, debugger)
  private final static float MAX_COST_SAMPLE_MS = 250f;

  // ceiling on per-tick simulated time. Browsers throttle timers in background
  // tabs; without a clamp the first tick after returning would teleport the
  // whole scene by the entire time the tab spent hidden.
  private final static float MAX_TICK_DT_SEC = 0.1f;

  /* MEMBER DATA */

  private int lastPanelWidth = -1;

  private Timer redrawTimer;

  private final int fgBlockSzPx;

  // Wall-clock pacing: the animation advances by measured elapsed time rather
  // than the timer's nominal delay, so scroll speed stays correct even when
  // the EDT cannot keep up with the configured rate (typical under CheerpJ,
  // where every Java thread is multiplexed onto the single browser thread).
  private long lastTickNanos = -1;

  // adaptive frame governor state (see recordFrameCost / governFrameRate)
  private final int baseDelayMs;
  private float frameCostEmaMs = -1f;
  private int ticksSinceGovern = 0;

  // Pre-rendered repeating strip for the foreground block row. All foreground
  // blocks are identical and evenly spaced, so the whole row is one periodic
  // image blitted at a phase offset: a single drawImage per frame instead of
  // one fill3DRect per block. fgScrollPx is the phase, kept in [0, blockSize).
  private BufferedImage fgStrip;
  private float fgScrollPx = 0f;

  // single shared RNG; tower recycling previously allocated one per recycle
  private final Random rand = new Random();

  private ArrayList<ArrayList<Board>> jaggedBackingsArray;

  private ArrayList<ArrayList<Integer>> jaggedBackingsPositionsArray;

  // accumulates sub-pixel movement to prevent rounding losses
  private float[] layerSubPixelAccum;

  // Per-tower pre-rendered bitmap cache. A tower's geometry (block size, depth
  // shading, and contents) is fixed for its entire lifetime, so it is rasterized
  // once on first sighting and then blitted each frame, instead of issuing a
  // fill3DRect per filled cell every frame. Keyed by Board identity; the
  // WeakHashMap lets recycled/off-screen towers be reclaimed automatically once
  // they fall out of the active lists, so the cache never needs manual eviction.
  private final Map<Board, BufferedImage> towerCache = new WeakHashMap<>();

  /* CONSTRUCTORS */

 /**
  * Default constructor for ParallaxScrollPanel
  * @param refreshHz The refresh rate of the parallax scroll in Hertz.
  * @param commonRatio A float indicating the intended ratio of block size for
  * each adjacent parallax layer. For 0.8f and a base = 50 PX, backing1 = 40 PX
  */
  public ParallaxScrollPanel(int refreshHz, float commonRatio) {

    setBackground(Color.BLACK);
    addComponentListener(ParallaxScrollPanel.this);

    if (refreshHz <= 0)
      refreshHz = DEFAULT_HZ;
    baseDelayMs = SEC_TO_MSECS / refreshHz;
    redrawTimer = new Timer(baseDelayMs, ParallaxScrollPanel.this);

    fgBlockSzPx = getBaseBlockSize();

    setVisible(true);
    redrawTimer.start();
  }

  /* INITIALIZATION HELPERS */

  private int getBaseBlockSize() {
    return BASE_BLOCK_SIZE_PX;
  }

  private void initializeBackgroundPositions(int windowWidth, int windowHeight) {
    jaggedBackingsArray = new ArrayList<>(DEFAULT_BACKING_LAYER_NUM);
    jaggedBackingsPositionsArray = new ArrayList<>(DEFAULT_BACKING_LAYER_NUM);
    layerSubPixelAccum = new float[DEFAULT_BACKING_LAYER_NUM];

    for (int layer = 0; layer < DEFAULT_BACKING_LAYER_NUM; layer++) {
      int blockSizePx = getBackingLayerBlockSize(layer);

      // per-layer height limits: furthest back (highest index) gets full height
      // closer layers are progressively shorter
      float layerHeightFactor = (float)(layer + 1) / DEFAULT_BACKING_LAYER_NUM;
      int availableHeight = (int)(windowHeight * (1 - TOP_BLANK_AREA) * layerHeightFactor);

      int maxHeightBlocks = roundDownTo4(availableHeight / blockSizePx);
      int minHeightBlocks = Math.max(4, roundDownTo4(maxHeightBlocks / 2));

      ArrayList<Board> layerTowers = new ArrayList<>();
      ArrayList<Integer> layerPositions = new ArrayList<>();

      boolean[] usedTypes = new boolean[7];

      int xPosition = 0;
      while (xPosition < windowWidth + blockSizePx * 12) { // buffer for scrolling
        int towerWidth = (rand.nextInt(2) + 1) * 4;

        int heightOptions = Math.max(1, (maxHeightBlocks - minHeightBlocks) / 4 + 1);
        int towerHeight = minHeightBlocks + (rand.nextInt(heightOptions) * 4);

        Board tower = new Board(towerHeight, towerWidth);
        fillTowerWithTetrominoes(tower, usedTypes, rand);

        layerTowers.add(tower);
        layerPositions.add(xPosition);

        // add 1 block spacing between towers
        xPosition += (towerWidth + 1) * blockSizePx;
      }

      jaggedBackingsArray.add(layerTowers);
      jaggedBackingsPositionsArray.add(layerPositions);
    }
  }

  private int roundDownTo4(int value) {
    return (value / 4) * 4;
  }

  private void fillTowerWithTetrominoes(Board board, boolean[] usedTypes, Random rand) {
    int width = board.getWidth();
    int height = board.getHeight();

    // simple greedy fill from bottom-up, left-to-right
    // allow gaps - looks more natural like a game in progress
    for (int row = height - 1; row >= 0; row--) {
      for (int col = 0; col < width; col++) {
        if (!board.isFreeBlock(col, row)) {
          continue; // already filled
        }

        // pick a random piece, prioritizing unused types
        int piece = pickPiece(usedTypes, rand);
        int rot = rand.nextInt(4);

        // try to place at this position (try a few offsets)
        boolean placed = false;
        for (int offX = -4; offX <= 0 && !placed; offX++) {
          for (int offY = -4; offY <= 0 && !placed; offY++) {
            int pX = col + offX;
            int pY = row + offY;

            if (board.containsTetromino(pX, pY, piece, rot)) {
              board.storePiece(pX, pY, piece, rot);
              usedTypes[piece] = true;
              placed = true;
            }
          }
        }

        // if couldn't place preferred piece, try others
        if (!placed) {
          for (int altPiece = 0; altPiece < 7 && !placed; altPiece++) {
            for (int altRot = 0; altRot < 4 && !placed; altRot++) {
              for (int offX = -4; offX <= 0 && !placed; offX++) {
                for (int offY = -4; offY <= 0 && !placed; offY++) {
                  int pX = col + offX;
                  int pY = row + offY;

                  if (board.containsTetromino(pX, pY, altPiece, altRot)) {
                    board.storePiece(pX, pY, altPiece, altRot);
                    usedTypes[altPiece] = true;
                    placed = true;
                  }
                }
              }
            }
          }
        }
        // if still not placed, leave the gap - that's okay
      }
    }
  }

  private int pickPiece(boolean[] usedTypes, Random rand) {
    // prioritize unused piece types for variety
    ArrayList<Integer> unused = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      if (!usedTypes[i]) {
        unused.add(i);
      }
    }
    if (!unused.isEmpty()) {
      return unused.get(rand.nextInt(unused.size()));
    }
    return rand.nextInt(7);
  }

  /* RENDERING HELPERS */

  public void paintForeground(Graphics g) {
    ensureForegroundStrip();

    // the row is periodic with period fgBlockSzPx, so blitting the strip at
    // the negated phase offset reproduces every block in one call
    g.drawImage(fgStrip, -((int) fgScrollPx), getHeight() - fgBlockSzPx, null);
  }

  // (Re)build the foreground strip if it has never been built or the panel
  // has grown past its width. A strip left over from a wider panel is kept:
  // drawing it is clipped to the panel anyway, and rebuilding on every
  // resize event would churn images during an interactive window drag.
  private void ensureForegroundStrip() {
    int neededWidth = getWidth() + fgBlockSzPx; // one spare block for the phase shift
    if (fgStrip != null && fgStrip.getWidth() >= neededWidth)
      return;

    int blocks = neededWidth / fgBlockSzPx + 1;
    BufferedImage strip =
        new BufferedImage(blocks * fgBlockSzPx, fgBlockSzPx, BufferedImage.TYPE_INT_RGB);
    Graphics2D sg = strip.createGraphics();
    sg.setColor(Color.WHITE.darker());
    for (int b = 0; b < blocks; b++) {
      sg.fill3DRect(b * fgBlockSzPx, 0, fgBlockSzPx, fgBlockSzPx, true);
    }
    sg.dispose();
    fgStrip = strip;
  }

  public void paintBackground(Graphics g) {
    if (jaggedBackingsArray == null || jaggedBackingsPositionsArray == null)
      return;

    int panelHeight = getHeight();
    int panelWidth = getWidth();

    // paint from back to front (highest layer index = furthest back)
    for (int layer = jaggedBackingsArray.size() - 1; layer >= 0; layer--) {
      int blockSizePx = getBackingLayerBlockSize(layer);
      float depthFactor = (float)(layer + 1) / (DEFAULT_BACKING_LAYER_NUM + 1);

      ArrayList<Board> towers = jaggedBackingsArray.get(layer);
      ArrayList<Integer> positions = jaggedBackingsPositionsArray.get(layer);

      for (int t = 0; t < towers.size(); t++) {
        Board tower = towers.get(t);
        int xPos = positions.get(t);
        int towerWidthPx = tower.getWidth() * blockSizePx;

        // viewport cull: skip towers entirely off either horizontal edge. The
        // recycler intentionally keeps off-screen towers queued in the buffer,
        // so this skips real per-frame work, not just rare edge cases.
        if (xPos + towerWidthPx < 0 || xPos > panelWidth)
          continue;

        int towerHeightPx = tower.getHeight() * blockSizePx;

        // align tower bottom to half-block above panel bottom (below foreground)
        int yPos = panelHeight - (fgBlockSzPx / 2) - towerHeightPx;

        // blit the tower's cached sprite (rasterized once on first sighting)
        // rather than re-filling every cell every frame.
        BufferedImage sprite = towerCache.get(tower);
        if (sprite == null) {
          sprite = rasterizeTower(tower, blockSizePx, depthFactor);
          towerCache.put(tower, sprite);
        }
        g.drawImage(sprite, xPos, yPos, null);
      }
    }
  }

  // Rasterize a tower to a transparent ARGB sprite exactly once. Depth shading is
  // baked in, and empty (gap) cells stay transparent so farther layers show
  // through. Reuses the existing drawTower path at a zero offset so tile
  // rendering stays a single source of truth.
  private BufferedImage rasterizeTower(Board tower, int blockSizePx, float depthFactor) {
    int w = Math.max(1, tower.getWidth() * blockSizePx);
    int h = Math.max(1, tower.getHeight() * blockSizePx);
    BufferedImage sprite = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D ig = sprite.createGraphics();
    TetrominoGraphics.offsetNextRender().xBy(0).yBy(0);
    TetrominoGraphics.Render.drawTower(ig, tower, blockSizePx, depthFactor);
    ig.dispose();
    return sprite;
  }

  /* BLOCK GEOMETRY TRANSFORMATION HELPERS */

  // Advance the foreground phase by elapsed wall time. Returns the top y of
  // the screen band this changed, or Integer.MAX_VALUE if the phase did not
  // cross a whole pixel (nothing visible moved).
  private int updateForegroundScroll(float dtSec) {
    int prevOffsetPx = (int) fgScrollPx;
    fgScrollPx = (fgScrollPx + dtSec * SCROLL_SPD_BLOCK_PER_SEC * fgBlockSzPx) % fgBlockSzPx;

    if ((int) fgScrollPx == prevOffsetPx)
      return Integer.MAX_VALUE;
    return getHeight() - fgBlockSzPx;
  }

  // Advance every backing layer by elapsed wall time. Returns the top y of the
  // screen band that visibly changed (bounded by the tallest tower of the
  // highest layer that moved a whole pixel), or Integer.MAX_VALUE if none did.
  private int updateBackgroundPositions(float dtSec) {
    if (jaggedBackingsArray == null || jaggedBackingsPositionsArray == null)
      return Integer.MAX_VALUE;

    int dirtyTop = Integer.MAX_VALUE;
    float blocksMoved = dtSec * SCROLL_SPD_BLOCK_PER_SEC;

    for (int layer = 0; layer < jaggedBackingsArray.size(); layer++) {
      int blockSizePx = getBackingLayerBlockSize(layer);

      // accumulate sub-pixel movement to prevent rounding to zero
      layerSubPixelAccum[layer] += blocksMoved * blockSizePx;
      int pixelsMoved = (int) layerSubPixelAccum[layer];
      layerSubPixelAccum[layer] -= pixelsMoved;

      if (pixelsMoved == 0) {
        continue; // nothing to move yet
      }

      ArrayList<Integer> positions = jaggedBackingsPositionsArray.get(layer);
      ArrayList<Board> towers = jaggedBackingsArray.get(layer);

      // update all tower positions
      for (int i = 0; i < positions.size(); i++) {
        positions.set(i, positions.get(i) - pixelsMoved);
      }

      // if first tower scrolled fully out of view, recycle it to the end
      if (!towers.isEmpty() && !positions.isEmpty()) {
        Board firstTower = towers.get(0);
        int firstTowerWidth = firstTower.getWidth() * blockSizePx;

        if (positions.get(0) <= -firstTowerWidth) {
          // remove from front
          towers.remove(0);
          positions.remove(0);

          // generate new tower and add to end
          int lastPos = positions.isEmpty() ? 0 : positions.get(positions.size() - 1);
          int lastTowerWidth = towers.isEmpty() ? 0 : towers.get(towers.size() - 1).getWidth() * blockSizePx;

          // use same per-layer height calculation as init
          float layerHeightFactor = (float)(layer + 1) / DEFAULT_BACKING_LAYER_NUM;
          int availableHeight = (int)(getHeight() * (1 - TOP_BLANK_AREA) * layerHeightFactor);
          int maxHeightBlocks = roundDownTo4(availableHeight / blockSizePx);
          int minHeightBlocks = Math.max(4, roundDownTo4(maxHeightBlocks / 2));

          int newWidth = (rand.nextInt(2) + 1) * 4;
          int heightOptions = Math.max(1, (maxHeightBlocks - minHeightBlocks) / 4 + 1);
          int newHeight = minHeightBlocks + (rand.nextInt(heightOptions) * 4);

          Board newTower = new Board(newHeight, newWidth);
          boolean[] usedTypes = new boolean[7];
          fillTowerWithTetrominoes(newTower, usedTypes, rand);

          towers.add(newTower);
          // add 1 block spacing
          positions.add(lastPos + lastTowerWidth + blockSizePx);
        }
      }

      // the tallest live tower bounds how far up this layer's movement reaches
      int tallestBlocks = 0;
      for (int i = 0; i < towers.size(); i++) {
        tallestBlocks = Math.max(tallestBlocks, towers.get(i).getHeight());
      }
      int layerTopY = getHeight() - (fgBlockSzPx / 2) - tallestBlocks * blockSizePx;
      dirtyTop = Math.min(dirtyTop, layerTopY);
    }

    return dirtyTop;
  }

  public void resizeBackgroundLayers() {
    if (jaggedBackingsArray == null) {
      initializeBackgroundPositions(getWidth(), getHeight());
      return;
    }

    // add more towers if window grew wider
    int windowWidth = getWidth();
    int windowHeight = getHeight();

    for (int layer = 0; layer < jaggedBackingsArray.size(); layer++) {
      int blockSizePx = getBackingLayerBlockSize(layer);

      ArrayList<Board> towers = jaggedBackingsArray.get(layer);
      ArrayList<Integer> positions = jaggedBackingsPositionsArray.get(layer);

      // calculate rightmost edge
      int rightEdge = 0;
      if (!towers.isEmpty() && !positions.isEmpty()) {
        int lastPos = positions.get(positions.size() - 1);
        int lastWidth = towers.get(towers.size() - 1).getWidth() * blockSizePx;
        rightEdge = lastPos + lastWidth;
      }

      // add towers until we have enough buffer
      int neededWidth = windowWidth + blockSizePx * 12;
      while (rightEdge < neededWidth) {
        float layerHeightFactor = (float)(layer + 1) / DEFAULT_BACKING_LAYER_NUM;
        int availableHeight = (int)(windowHeight * (1 - TOP_BLANK_AREA) * layerHeightFactor);
        int maxHeightBlocks = roundDownTo4(availableHeight / blockSizePx);
        int minHeightBlocks = Math.max(4, roundDownTo4(maxHeightBlocks / 2));

        int newWidth = (rand.nextInt(2) + 1) * 4;
        int heightOptions = Math.max(1, (maxHeightBlocks - minHeightBlocks) / 4 + 1);
        int newHeight = minHeightBlocks + (rand.nextInt(heightOptions) * 4);

        Board newTower = new Board(newHeight, newWidth);
        boolean[] usedTypes = new boolean[7];
        fillTowerWithTetrominoes(newTower, usedTypes, rand);

        towers.add(newTower);
        positions.add(rightEdge + blockSizePx); // 1 block spacing

        rightEdge += (newWidth + 1) * blockSizePx;
      }
    }
  }

  /* FRAME PACING */

  // Measured wall time since the previous tick, clamped so a tab-throttled or
  // suspended interval cannot fast-forward the scene. First tick yields 0.
  private float computeDeltaSeconds(long nowNanos) {
    if (lastTickNanos < 0) {
      lastTickNanos = nowNanos;
      return 0f;
    }
    float dtSec = (nowNanos - lastTickNanos) / 1_000_000_000f;
    lastTickNanos = nowNanos;
    return Math.min(dtSec, MAX_TICK_DT_SEC);
  }

  /**
   * Record how long the frame that began at tickStartNanos actually cost the
   * EDT. Called from a runnable posted with invokeLater AFTER repaint(): the
   * event queue is FIFO, so by the time the runnable runs, both the update
   * and the paint this tick queued have completed — the elapsed time is the
   * true end-to-end frame cost, including time spent servicing any other
   * events that were queued in between (which is exactly the load the
   * governor should react to).
   */
  private void recordFrameCost(long tickStartNanos) {
    float costMs = (System.nanoTime() - tickStartNanos) / 1_000_000f;
    if (costMs > MAX_COST_SAMPLE_MS)
      return; // one-off stall (GC, tab switch): not steady-state load

    frameCostEmaMs = (frameCostEmaMs < 0f)
        ? costMs
        : frameCostEmaMs + COST_EMA_ALPHA * (costMs - frameCostEmaMs);

    if (++ticksSinceGovern >= GOVERN_EVERY_TICKS) {
      ticksSinceGovern = 0;
      governFrameRate();
    }
  }

  /**
   * Adaptive frame governor: steer the timer delay toward
   * frameCost * COST_HEADROOM, clamped to [base, base * MAX_THROTTLE_FACTOR].
   *
   * On a desktop where a frame costs a few ms, the target sits below the base
   * delay and the animation runs at the full configured rate. Under CheerpJ —
   * where every Java thread is multiplexed onto the single browser thread —
   * an expensive frame raises the delay until a fixed share of each period is
   * idle, which is precisely the time the browser needs to service input.
   * Because the cost measurement does not depend on the current delay,
   * recovery is automatic when frames get cheap again. Movement is paced by
   * wall time (see computeDeltaSeconds), so delay changes alter smoothness
   * only — never scroll speed.
   */
  private void governFrameRate() {
    int targetDelayMs = Math.round(frameCostEmaMs * COST_HEADROOM);
    targetDelayMs = Math.max(baseDelayMs,
                    Math.min(targetDelayMs, baseDelayMs * MAX_THROTTLE_FACTOR));

    if (Math.abs(targetDelayMs - redrawTimer.getDelay()) < GOVERN_DEADBAND_MS)
      return;

    redrawTimer.setDelay(targetDelayMs);
    // visible in the browser console under CheerpJ; changes are infrequent
    System.out.println(String.format(
        "[parallax] governor: avg frame cost %.1fms -> timer delay %dms",
        frameCostEmaMs, targetDelayMs));
  }

  /* GETTERS */

  public Timer getRedrawTimer() {
    return redrawTimer;
  }

  /**
   * @return the redraw timer's current delay in ms. Note this is the governed
   * delay, which may be above the configured base when the frame governor has
   * throttled the animation; it is no longer used for movement (movement is
   * paced by measured wall time).
   */
  public int getPeriod() {
    if (redrawTimer == null)
      return 0;
    return redrawTimer.getDelay();
  }

  public int getBackingLayerBlockSize(int idx) {

    return (int)Math.floor(fgBlockSzPx * Math.pow(DEFAULT_COMMON_RATIO, idx + 1));
  }
  /* OVERRIDES */

  @Override
  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() != redrawTimer)
      return;

    final long tickStartNanos = System.nanoTime();
    float dtSec = computeDeltaSeconds(tickStartNanos);

    int dirtyTop = Math.min(updateForegroundScroll(dtSec),
                            updateBackgroundPositions(dtSec));

    // Repaint only the horizontal band that visibly changed this tick. The
    // panel sits under a non-opaque menu in a JLayeredPane, where dirty
    // regions are promoted to the whole layer stack: a full-panel repaint()
    // would force the menu's labels and buttons to repaint every tick, while
    // the band spares everything above the layers that moved. On ticks where
    // only the near (short) layers crossed a whole pixel, that skips most of
    // the window.
    if (dirtyTop < getHeight()) {
      int bandTop = Math.max(0, dirtyTop - 2); // small safety margin
      repaint(0, bandTop, getWidth(), getHeight() - bandTop);
    }

    // posted after repaint(), so it runs once this frame's paint is done and
    // measures the frame's true EDT cost (see recordFrameCost)
    SwingUtilities.invokeLater(() -> recordFrameCost(tickStartNanos));
  }

  @Override
  public void paintComponent(Graphics g) {

    super.paintComponent(g);

    // Build the size-dependent geometry on the first paint that has a real size,
    // so the very first visible frame already shows the towers (no blank-then-
    // pop). paint only runs once the component is realized and laid out, so the
    // size here is trustworthy — unlike a launch-time timer tick.
    ensureLayersInitialized();

    // background first (behind), then foreground (in front)
    paintBackground(g);
    paintForeground(g);
  }

  /**
   * Build the parallax layers the first time a real (laid-out) size is known.
   * Idempotent and cheap to re-call: it no-ops once the towers exist, and bails
   * while the panel is still reported at a negligible 0x0 size (which happens
   * briefly at launch before the layout manager sizes the panel).
   *
   * Initialization is keyed on "do the layers exist yet?" — NOT on catching a
   * width-change event. Keying it on a width delta is what made launch a race:
   * a first paint could pre-set lastPanelWidth to the real width, so the delta
   * never tripped and the towers stayed unbuilt until a manual window resize.
   */
  private void ensureLayersInitialized() {
    if (jaggedBackingsArray != null)
      return;                                  // already built

    int w = getWidth(), h = getHeight();
    if (w <= 0 || h <= 0)
      return;                                  // no valid size yet; retry next tick

    initializeBackgroundPositions(w, h);
    lastPanelWidth = w;

    // this paint may be running under a partial (band) clip; queue one full
    // repaint so the freshly built towers above the band become visible too
    repaint();
  }

  private void refreshPanelBounds() {
    ensureLayersInitialized();                 // covers the first-valid-size build

    int currentPanelWidth = getWidth();

    // once built, react only to genuine width changes (user resized the window)
    if (jaggedBackingsArray != null
        && currentPanelWidth > 0
        && currentPanelWidth != lastPanelWidth) {
      resizeBackgroundLayers();
      lastPanelWidth = currentPanelWidth;
    }
  }

  @Override
  public void componentResized(ComponentEvent e) { refreshPanelBounds(); }

  @Override
  public void componentMoved(ComponentEvent e) { refreshPanelBounds(); }

  @Override
  public void componentShown(ComponentEvent e) { refreshPanelBounds(); }

  @Override
  public void componentHidden(ComponentEvent e) { refreshPanelBounds(); }
}
