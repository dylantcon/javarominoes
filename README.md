# javarominoes

*A Tetris-inspired block stacking puzzle game, implemented using Java's Swing libraries. Dedicated to my dear Granddad.*

## Overview

Javarominoes is a Java-based tetris-inspired game that I wrote for fun. I have faithfully replicated all major features from prior installments of the series, **excluding only**:

- T-spin point awards
- Type-B mode 
- Locally cached high scores

For a list of all implemented features, see the section below. The game was built entirely in Java, relying only on core JRE features, and packaged into a runnable JAR file. As such, if you have the JRE installed (https://www.oracle.com/java/technologies/javase/jdk22-archive-downloads.html), you should be able to simply double-click the `Javarominoes.jar` file and it will fire up.

## Features

**Fair Piece Generation**: I've implemented the so-called "bag" method for random piece generation, ensuring a balanced distribution of all tetrominoes throughout gameplay. An array holds all 7 of the pieces, which I shuffle using the **Fisher-Yates algorithm**. The program will iterate through the list after every new piece is generated, and after we reach the end, the array is repopulated and reshuffled. Thus, the longest possible 'drought' between identical pieces is *13*. This fixes an issue present in the original Tetris title for the NES, which uses a [linear feedback shift register](https://arxiv.org/html/2404.12011v1) to generate random pieces without a piece buffer array. The naive LFSR approach might cause a scenario in which the user does not receive an 'I' piece for 20 or more piece generations, effectively ending their game and causing understandable frustration.

**Difficulty Scaling**: I used a mix of logarithmic and linear functions to determine how the time-to-descend (TTD) changes based on the player's score. Initially, to facilitate an engaging early-game, the time-to-descend in milliseconds is determined by the average of a logarithmic and linear function of score. Once the linear function intersects with the logarithmic function at around 13,000 score, it relies solely on the logarithmic function for the remainder of the game. This keeps things paced properly and gives the user a sense of urgency.

**Graphical Interface**: I built the graphical interface using Java Swing, with `java.awt.Graphics` primarily. I do use `java.awt.Graphics2D` in the game panel for the nice top-to-bottom gradient coloring on the horizontal board area padding. The game should flexibly rescale to almost any window size, with some necessary exceptions made (for disproportionately tall or wide windows) to maintain the proper dimensions for the game board.

**Pause and Unpause**: Not finished with your game, and need to take care of something else? No worries, just hit `Esc` to pause, and either `Esc` again to unpause, or click the Resume menu button. There's also an option to restart your current game if desired. Finally, if you decide that you're actually all finished playing, you can also use the menu buttons to quit to the main menu. Something to note, my pausing system accounts for elapsed time since last tick, so **no, you cannot repeatedly pause and unpause to suspend a piece for longer than ordinarily possible**. 

**Main Menu with Parallax Effect**: For polish, I added a main menu. You can play a game of Javarominoes from here by clicking the respective button, or exit to desktop if all finished. Behind the title, subtitle, and buttons, a vibrant and dynamic landscape of blocks can be seen. I used my knowledge of the geometric series to create a hierarchy of layers, with each layer having a smaller block size than the one in front of it. Let's suppose that we specify a base block size in inches, and multiply by the PPI of the monitor to obtain a value in pixels. If the base block size at the foreground is $B_b$, the panel has $n$ backing layers, and specifies a common ratio of $r$, then all we need to do to visualize the parallax effect is specify a scroll speed $S$ in units of $\frac{\text{blocks}}{\text{s}}$. Then, the layers have block sizes $S_l = \{r^0 \times B_b, r^1 \times B_b, \cdots , r^{n-1} \times B_b, r^n \times B_b\}$, causing layers in the back to support a proportionately higher block-per-inch density. Since all layers scroll at the same rate in blocks per second, layers with higher bblock densities will appear to move slower than those with lower block densities. This approach models the problem so well that adjusting a field such as layer count, scroll speed, base block size, or panel refresh rate can be done by changing just one static constant. I did have to implement sub-pixel accumulators in the form of a parallel array of floats, as initially layers with very small calculated block sizes would move such a small distance every tick that the calculated coordinates for re-render would truncate to their original value pre-tick.

**Score Tracking and Game Mechanics**: As I already mentioned, there's dynamic time-to-descend based on score, and unlike the original NES Tetris, speed increases are not arbitrarily discretized by "Level". Speed of piece descent is directly mapped to the player's current score. I also made sure to reward players for higher simultaneous line clears, in a manner consistent with the approach found in mainline Tetris titles. Since my game speed calculation system is slightly different, I adjusted the point awards to compensate. The rewards are still proportionally consistent with the original design, though their exact numeric values may differ. Also, the game has piece previews, which I know was something a few Tetris titles omitted.

**In-Game Music**: I used Java's `Sequencer` object from `javax.swing.Sequencer` to play a MIDI file containing the familiar Russian folk song, [Korobeiniki](https://en.wikipedia.org/wiki/Korobeiniki). It spruces up the game and gives it a little bit of extra charm. For the record, **I did not create this composition**, I found it online available for free. **I relinquish all rights to this particular MIDI file, and will remove it from my game upon request** should the [original creators](https://onlinesequencer.net/3204422) issue such a notice. A huge thanks to the original creators Jacob Morgan and George Burdell for making this composition publicly available.

## Architecture
I built Javarominoes a while back, intending to expand it later. As a result, there's some decent modularity. Here is a breakdown of the core classes and how they contribute to the game:

**`Pieces`**: Manages all the data for the seven tetrominoes, including their different rotations. It stores piece configurations in a 4D array (`matrix`) and includes methods for retrieving specific block data and determining initial piece offsets. Not as rigorous as doing manual matrix transformations, but significantly more efficient in terms of runtime overhead.

**`Board`**: Represents the main game grid where pieces are placed. This class manages the core gameplay mechanics like placing pieces, clearing lines, and detecting game-over conditions. It directly communicates with `GamePanel` to reflect game state changes. It is also used cosmetically by the `ParallaxScrollPanel` to model the lists of randomized towers seen scrolling from right-to-left in each of the backing layers.

**`TetrominoGraphics`**: Utility class with public static inner class `Render`, which has public methods `getBlockColor(int num)`, `drawStaticBoardBlocks(Graphics g, Board b, int bPx)`, `drawTower(Graphics g, Board b, int bPx, float depthFactor)`, and `drawPiece(Graphics g, int bPx, int pc, int rot, int gX, int gY, boolean showRotator, Color override)`. Renders on components whose origin does not represent the top left corner of a game grid must have an offset applied prior to method invocation. This is done using a fluent builder, `RenderOffsetBuilder`. To offset the next render by $x$ pixels horizontally and $y$ pixels vertically, one must call `TetrominoGraphics.offsetNextRender().xBy(x).yBy(y)`. This sets static `int` members to the requested values, which are then used for the respective position calculations in the next render. They are cleared after each render and must be specified before using methods `drawTower()` or `drawPiece()`, if an offset is needed.

**`BoardPanel`**: Responsible for rendering the board visually. It updates based on the current state managed by `GamePanel`, ensuring that each move or line clear is instantly represented on-screen. The visual grid representing the board will expand vertically as much as possible, after which point the effective height of the grid in pixels is divided by 20 to obtain the appropriate atomic block size. This size is then used to expand the grid area horizontally by 10 blocks. Any remaining space after horizontal expansion is reserved for horizontal padding on the left and right, which centers the play grid within the parent component's available area. Only after this is in place may the grid-lines, ground padding, stationary pieces, and active piece be rendered.

**`InfoPanel`**: Displays key information for the player's consumption, including the piece preview, current score, and current time-to-descend in milliseconds. It plays a crucial role in score management, as it has the public method `deltaTTD(int score)` which powers the entire dynamic game speed system. `GamePanel` owns an `InfoPanel` as a member, the `InfoPanel` is primarily a delegate and statefully represents only the score and incoming piece information. The `GamePanel` must manage this object to ensure that the score updates in response to in-game events like line clears and piece drops.

**`MenuPanel`**: Abstract class extending `JPanel`, the base component that is used in `JLayeredPane` hierarchies to represent menu elements arranged by a `GridBagLayout` within the user interface. Provides methodsfor creating buttons, managing the visibility of buttons, and adding elements to the `GridBagLayout`. Children must define abstract method `void initGbl()`, which specifies how the child of `MenuPanel` is to arrange its components within the `GridBagLayout`.

**`PauseMenuPanel`**: Child of `MenuPanel`, the component that is used in `JLayeredPane` hierarchies to represent pause menu elements arranged by a `GridBagLayout`. This particular subclass has setters and getters for its buttons, and methods for optionally displaying a game over menu or a pause menu, upon the caller's request. Transparent, with `setOpaque(false)`.

**`MainMenuPanel`** Child of `MenuPanel`, the component that is used in `JLayeredPane` hierarchies to represent main menu elements arranged by a `GridBagLayout`. This particular subclass has setters and getters for its buttons. Transparent, with `setOpaque(false)`.

**`ParallaxScrollPanel`**: Subclass of `JPanel`, implements custom [parallax scrolling](https://en.wikipedia.org/wiki/Parallax_scrolling) graphics using a silver foreground of blocks, and any number of backing layers composed of lists of randomly generated block towers. It is powered by a system that leverages a geometric series with common ratio $r$ to scale down the block size in each consecutive backing layer, creating a series of planes with naturally increasing block-density-per-inch. This, combined with a constant scroll speed specified in units of $\frac{\text{block}}{\text{s}}$, allows for a natural mathematical parallax effect to appear on screen when each of the layers are rendered in descending order, based on depth.

**`GamePanel`**: The central hub that manages game state, user input, and interactions between other components. It handles piece generation (`generateNewPiece()`), movements (`movePiece()`, `movePieceDown()`, `rotateCW()`), and collisions (`checkCollision()`). It also manages the game's timing and difficulty progression (`shortenTTDInterval()`). _`GamePanel` is in charge of delegating tasks to `BoardPanel` and `InfoPanel` for proper rendering and score updating_. 

**`Javarominoes`** **(Main Class)**: Serves as the entry point of the game. It initializes all components (`GamePanel`, `BoardPanel`, `InfoPanel`) and sets up the game's main window. Javarominoes ensures that all aspects of the game are properly initialized and interconnected, ready for gameplay. It also handles playing the music since it is the top-level component in the `JComponent` hierarchy.

## How To Play
**Movement**: Use `A` to move pieces left, `D` to move pieces right, and `S` to move pieces down. Rotate pieces counterclockwise using `Q` and clockwise using `E`. Press `Space` to instantly drop the piece to the lowest possible position, which is depicted using the silhouette. To or unpause the pause the game, press `Esc`. If you would like to quit, pause the game and then click on the menu button labelled `Return to menu`.

**Goal**: The goal is to clear as many lines as possible by strategically placing pieces, earning points and increasing the game's difficulty. Try to earn the highest score you can! I got 17,703 one time, can you beat it?

**Scoring**: Points are awarded based on piece movement, full drops, and line clears. The more lines you clear at once, the higher the score multiplier. To earn points faster, use the quick-drop with `Space`, as it awards you two points per block traversed, as opposed to the ordinary one point per block traversed for normal descent.

## Motivation
I built this project as a tribute to the original Tetris game, which I grew up playing a lot on an original release-model GameBoy at my Granddad's house. I could never beat his high-scores, he honestly was really good! After he passed, I wanted to honor all of the love he brought into my life by making something I know he would've really appreciated. _This one's for you, Roy <3_

## Acknowledgements
This project was built entirely independently, with considerable time invested in understanding the Java Swing library and implementing efficient game logic. Special thanks to everyone who has supported the development of Javarominoes, including my lovely girlfriend, and all the family and friends who inspired the creation of this game. I sincerely hope you enjoy.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Connect
Feel free to reach out or follow my journey on LinkedIn, where I often share my weekly projects or any other insights pertaining to my journey towards becoming a skilled developer. Your suggestions and feedback are always welcome!!
