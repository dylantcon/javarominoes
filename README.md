# javarominoes

*A Tetris-inspired block stacking puzzle game, implemented using Java's Swing libraries. Dedicated to my dear Granddad.*

## Overview

Javarominoes is a Java-based tetris-inspired game that combines classic puzzle gameplay with a carefully calibrated difficulty progression. This project features unique random piece generation mechanics, a challenging score-to-speed relationship, and a clean, modular structure that makes it easy to extend and maintain. The game was built entirely in Java, relying only on core JDK features, and packaged into a runnable JAR file. As such, if you have the JRE installed (https://www.oracle.com/java/technologies/javase/jdk22-archive-downloads.html), you should be able to simply double-click the '`Javarominoes.jar`' file and it will fire up. There isn't much to speak of in terms of supplemental GUI at the moment, although I am considering adding in a menu and high-scores.

## Features
**Piece Generation**: I've implemented the "bag" method for random piece generation, ensuring a balanced distribution of all tetrominoes throughout gameplay. An array holds all 7 of the pieces, which I shuffle using the **Fisher-Yates algorithm**. The program will iterate through the list after every new piece is generated, and after we reach the end, the array is repopulated and reshuffled. Thus, the longest possible 'drought' between identical pieces is *13*.

**Difficulty Scaling**: I used a mix of logarithmic and linear functions to determine how the time-to-descend (TTD) changes based on the player's score. This balance provides a dynamic yet fair difficulty curve, challenging players to continuously improve without causing overwhelming spikes in speed.

**Graphical Interface**: I built the graphical interface using Java Swing, featuring detailed, smooth game graphics that are updated in real-time, creating a responsive and engaging experience for the player. The game should automatically resize to any aspect ratio the user chooses, and this is also cross-platform.

**Score Tracking and Game Mechanics**: I implemented real-time score updates with different incentives for actions such as line clears and full piece drops.

## Architecture
Javarominoes is designed with modularity and maintainability in mind. Here is a breakdown of the core classes and how they contribute to the game:

**`Pieces`**: Manages all the data for the seven tetrominoes, including their different rotations. It stores piece configurations in a 4D array (`matrix`) and includes methods for retrieving specific block data and determining initial piece offsets.

**`Board`**: Represents the main game grid where pieces are placed. This class manages the core gameplay mechanics like placing pieces, clearing lines, and detecting game-over conditions. It directly communicates with `GamePanel` to reflect game state changes.

**`BoardPanel`**: Responsible for rendering the board visually. It updates based on the current state managed by `GamePanel`, ensuring that each move or line clear is instantly represented on-screen.

**`InfoPanel`**: Displays key information like the player's current score. It plays a crucial role in score management, interacting with `GamePanel` to ensure that the score updates in response to in-game events like line clears and successful piece drops.

**`GamePanel`**: The central hub that manages game state, user input, and interactions between other components. It handles piece generation (`generateNewPiece()`), movements (`movePiece()`, `movePieceDown()`, `rotateCW()`), and collisions (`checkCollision()`). It also manages the game's timing and difficulty progression (`shortenTTDInterval()`). _`GamePanel` is in charge of delegating tasks to `BoardPanel` and `InfoPanel` for proper rendering and score updating_.

**`Javarominoes`** **(Main Class)**: Serves as the entry point of the game. It initializes all components (`GamePanel`, `BoardPanel`, `InfoPanel`) and sets up the game's main window. Javarominoes ensures that all aspects of the game are properly initialized and interconnected, ready for gameplay.

## How To Play
Movement: Use '`A`' to move pieces left, '`D`' to move pieces right, and '`S`' to move pieces down. Rotate pieces counterclockwise using '`Q`' and clockwise using '`E`'. Press '`Space`' to instantly drop the piece to the lowest possible position, which is depicted using the silhouette.

Goal: The goal is to clear as many lines as possible by strategically placing pieces, earning points and increasing the game's difficulty.

Scoring: Points are awarded based on piece movement, full drops, and line clears. The more lines you clear at once, the higher the score multiplier.

## Motivation
I built this project as a tribute to the original Tetris game, which I grew up playing a lot on an original release-model GameBoy at my Granddad's house. I could never beat his high-scores, he honestly was really good! After he passed, I wanted to honor all of the love he brought into my life by making something I know he would've really appreciated. _This one's for you, Roy <3_

## Acknowledgements
This project was built entirely independently, with considerable time invested in understanding the Java Swing library and implementing efficient game logic. Special thanks to everyone who has supported the development of Javarominoes, including my lovely girlfriend, and all the family and friends who inspired the creation of this game.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Connect
Feel free to reach out or follow my journey on LinkedIn, where I often share my weekly projects or any other insights pertaining to my journey towards becoming a skilled developer. Your suggestions and feedback are always welcome!!
