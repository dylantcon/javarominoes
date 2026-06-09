package javarominoes.view;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javarominoes.model.TetrominoGraphics;
import javarominoes.model.Board;
import java.awt.Color;
import javax.swing.JPanel;
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
  private final static int FG_BLOCK_LIST_RESIZE_BUFFER_PX = 10; // +10 px buffer
  private final static int REBOUND_EVERY = 70; //ms
  
  private final static float DEFAULT_COMMON_RATIO = 0.80f;

  // Pinned foreground block size in pixels. Previously derived at runtime from
  // Toolkit.getScreenResolution() * 0.5in, which on a standard 96-DPI display
  // yields 48px. That DPI is environment-dependent: under CheerpJ the browser
  // reports a different screen resolution, so the parallax geometry would drift
  // from the desktop build and break pixel-identical fidelity. Pin it instead.
  // NOTE: if your dev machine does NOT report 96 DPI, set this to whatever
  // (int)(Toolkit.getDefaultToolkit().getScreenResolution() * 0.5f) prints there.
  private final static int BASE_BLOCK_SIZE_PX = 48;
  
  private final static float SCROLL_SPD_BLOCK_PER_SEC = 3.8f;
  
  /**
   * fifteen percent of the panel's total height must be used to render a blank
   * area at the panel's top. The y coordinate yielded from the product of the
   * height of the panel and this float represents the absolute height limit of
   * any backing layer regardless of count.
   */
  private final static float TOP_BLANK_AREA = 0.15f;
  
  /* MEMBER DATA */
  
  private boolean initialized = false;
  
  private int lastPanelWidth = -1;
  
  private Timer redrawTimer;
  
  private Timer boundRefresher;
  
  private final int fgBlockSzPx;
  
  private ArrayList<Integer> fgBlockPositions;
  
  private ArrayList<ArrayList<Board>> jaggedBackingsArray;

  private ArrayList<ArrayList<Integer>> jaggedBackingsPositionsArray;

  // accumulates sub-pixel movement to prevent rounding losses
  private float[] layerSubPixelAccum;
  private float fgSubPixelAccum;

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
    
    initializeRedrawTimer(refreshHz);
    initializeBoundRefreshTimer();
    fgBlockSzPx = getBaseBlockSize();
    
    initializeForegroundPositions(640);
    setVisible(true);
    redrawTimer.start();
    boundRefresher.start();
  }
  
  /* INITIALIZATION HELPERS */
  
  private void initializeRedrawTimer(int refreshHz) {
    if (refreshHz <= 0)
      refreshHz = DEFAULT_HZ;
    
    int rerenderPeriod = SEC_TO_MSECS / refreshHz;
    
    if (redrawTimer != null && redrawTimer.isRunning()) {
      redrawTimer.stop();
      redrawTimer.setDelay(rerenderPeriod);
    }
    else
      redrawTimer = new Timer(rerenderPeriod, ParallaxScrollPanel.this);
  }
  
  private void initializeBoundRefreshTimer() {
    boundRefresher = new Timer(REBOUND_EVERY, ParallaxScrollPanel.this);    
  }
  
  private int getBaseBlockSize() {
    return BASE_BLOCK_SIZE_PX;
  }
  
  private void initializeForegroundPositions(int windowWidth) {
    int listSize = windowWidth / fgBlockSzPx + 1; // one more for scrolling
    int offset = fgBlockSzPx / 2;
    fgBlockPositions = new ArrayList<>(listSize);
    
    if (listSize <= 0)
      return;
    
    // set up all blocks. scrolls R to L
    for (int blockNo = 0; blockNo < listSize; blockNo++) {
      fgBlockPositions.add(blockNo, (blockNo * fgBlockSzPx) - offset);
    }
  }
  
  private void initializeBackgroundPositions(int windowWidth, int windowHeight) {
    jaggedBackingsArray = new ArrayList<>(DEFAULT_BACKING_LAYER_NUM);
    jaggedBackingsPositionsArray = new ArrayList<>(DEFAULT_BACKING_LAYER_NUM);
    layerSubPixelAccum = new float[DEFAULT_BACKING_LAYER_NUM];

    Random rand = new Random();

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
    
    if (fgBlockPositions == null)
      return;
    
    Color previousColor = g.getColor();
    g.setColor(Color.WHITE.darker());
    
    int yFgTop = getHeight() - fgBlockSzPx;
    int panelWidth = getWidth();
    
    for (int i = 0; i < fgBlockPositions.size(); i++) {
      int x = fgBlockPositions.get(i);
      if (x + fgBlockSzPx < 0 || x > panelWidth)
        continue; // skip foreground blocks scrolled off either edge
      g.fill3DRect(x, yFgTop, fgBlockSzPx, fgBlockSzPx, true);
    }
    
    g.setColor(previousColor);
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
  
  // this should only be called in actionPerformed or a delegate of actionPerformed
  private void updateForegroundPositions() {
    if (fgBlockPositions == null)
        return;

      float blocksMoved = getPeriod() * (SCROLL_SPD_BLOCK_PER_SEC / SEC_TO_MSECS);

      // accumulate sub-pixel movement
      fgSubPixelAccum += blocksMoved * fgBlockSzPx;
      int pixelsMoved = (int) fgSubPixelAccum;
      fgSubPixelAccum -= pixelsMoved;

      if (pixelsMoved == 0)
        return;

      for (int i = 0; i < fgBlockPositions.size(); i++) {
        fgBlockPositions.set(i, fgBlockPositions.get(i) - pixelsMoved);
      }

      /* if first foreground block scrolled fully out of view, remove it, append
          new position exactly one block size higher than the old last index val */
      if (fgBlockPositions.get(0) <= -fgBlockSzPx) {
        fgBlockPositions.remove(0);
        fgBlockPositions.add(fgBlockPositions.get(fgBlockPositions.size() - 1) + fgBlockSzPx);
      }
  }

  private void updateBackgroundPositions() {
    if (jaggedBackingsArray == null || jaggedBackingsPositionsArray == null)
      return;

    float blocksMoved = getPeriod() * (SCROLL_SPD_BLOCK_PER_SEC / SEC_TO_MSECS);

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

          Random rand = new Random();

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
    }
  }
  
  public void resizeForegroundLayer() {
    
    if (fgBlockPositions == null) {
      initializeForegroundPositions(getWidth());
      return;
    }
    
    int fgProjectionX = fgBlockPositions.get(fgBlockPositions.size() - 1) + fgBlockSzPx;
    int fgNeededWidth = getWidth() + fgBlockSzPx + FG_BLOCK_LIST_RESIZE_BUFFER_PX;
    int fgDelta = fgNeededWidth - fgProjectionX;
    int blockDelta = fgDelta / fgBlockSzPx;
    
    if (blockDelta == 0)
      return;
    
    if (blockDelta > 0) { // fg delta will be positive
      for (int i = 0; i < blockDelta; i++) {
        fgBlockPositions.add(fgBlockPositions.get(fgBlockPositions.size() - 1) + (fgBlockSzPx));
      }
    }
    else {
      for (int i = 0; i < -blockDelta; i++) {
        fgBlockPositions.remove(fgBlockPositions.size() - 1);
      }
    }
  }
  
  public void resizeBackgroundLayers() {
    if (jaggedBackingsArray == null) {
      initializeBackgroundPositions(getWidth(), getHeight());
      return;
    }

    // add more towers if window grew wider
    Random rand = new Random();
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
  
  /* GETTERS */
  
  public Timer getRedrawTimer() {
    return redrawTimer;
  }
  
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
    if (evt.getSource() == redrawTimer) {
      updateForegroundPositions();
      updateBackgroundPositions();
    }
    if (evt.getSource() == boundRefresher) {
      refreshPanelBounds();
    }
    repaint();
  }
  
  @Override
  public void paintComponent(Graphics g) {

    super.paintComponent(g);

    // Build the size-dependent geometry on the first paint that has a real size,
    // so the very first visible frame already shows the towers (no blank-then-
    // pop). paint only runs once the component is realized and laid out, so the
    // size here is trustworthy — unlike a launch-time timer tick.
    ensureLayersInitialized();

    if (!initialized && getWidth() > 0) {
      lastPanelWidth = getWidth();
      updateForegroundPositions();
      initialized = true;
    }

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

    resizeForegroundLayer();                   // refit the 640-seeded fg list to width
    initializeBackgroundPositions(w, h);
    lastPanelWidth = w;
  }

  private void refreshPanelBounds() {
    ensureLayersInitialized();                 // covers the first-valid-size build

    int currentPanelWidth = getWidth();

    // once built, react only to genuine width changes (user resized the window)
    if (jaggedBackingsArray != null
        && currentPanelWidth > 0
        && currentPanelWidth != lastPanelWidth) {
      resizeForegroundLayer();
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
