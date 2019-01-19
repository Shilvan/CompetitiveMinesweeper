# CompetitiveMinesweeper

## Overview
This is a simple, java based minesweeper game created to help some basic "AI" development on a competitive way. The game also offers a chance to play the randomly generated minefields as a human (similarly to the classic game).

Website: 
https://github.com/kecskemeti/CompetitiveMinesweeper

Documentation website:
https://kecskemeti.github.io/CompetitiveMinesweeper

Licensing:
[GNU General Public License 3 and later](https://www.gnu.org/licenses/gpl-3.0.en.html)

## Compilation & Installation

Prerequisites: [Apache Maven 3](http://maven.apache.org/), Java 8

After cloning, run the following in the main dir of the checkout:

`mvn clean install javadoc:javadoc`

This command will download all other prerequisites for compilation and testing. Then it will compile the complete sources of CompetitiveMinesweeper as well as its test classes. If the compilation is successful, then the tests are executed. In case no test fails, maven proceeds with the packaging and istallation.

The installed simulator will be located in the default maven repository's (e.g., `~/.m2/repository`) following directory: 
`uk/ac/ljmu/fet/cs/csw/CompetitiveMinesweeper/[VERSION]/CompetitiveMinesweeper-[VERSION].jar`

Where `[VERSION]` stands for the currently installed version of the simulator.

The documentation for the simulator's java API will be generated in the following subfolder of the main dir of the checkout:

`docs`

## Getting started

### Minimum runtime dependencies
CompetitiveMinesweeper depends on the following libraries during its runtime: 
* Java 8

### How to run
The main GUI to be run is [MineSweeper](https://github.com/kecskemeti/CompetitiveMinesweeper/blob/master/src/main/java/uk/ac/ljmu/fet/cs/csw/CompetitiveMinesweeper/gui/MineSweeper.java). This will produce a simple configurator window which has the following settings:
* The dimensions of the playing field
* The selection of the difficulty level (i.e. what's the percentage of mines in relation to the playing field's area)
* If we want to play as a human player then we can opt for it here.
* If we wish to challange two AI solutions with the same puzzle, then we can specify the two AI's fully qualified classnames here as well. The only AI offered by default is called uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.SimpleLineByLineSolver .
* Finally we can configure the speed of the AI. Each operation of the AI can be delayed between 0-1000ms. The slider which allows speed adjustments offers a few hints. 0 delay means we want to see the AI's raw speed. 

If the game is configured one can click the "GO" button and one/two minesweeping windows will start to solve a puzzle. If we choose to play ourselves, then the window of ours can be controlled as follows:
* Left click a spot in the grid to reveal what's behind it. This risks you getting exploded if you are not careful (this will cause you to lose the game).
* Right click a spot in the grid to flag it as a potential mine. If you changed your mind, right click on the same spot again.
* How to win? Explore the whole playing field. All spots on the grid should be revealed except the ones that have flags on them.

If the game completes (because of the player exploding or completely exploring the field) the main game window will offer to start a new game or return to the configuration window. Note, closing any game's window will result in the complete termination of the main application.

### How to add a new AI solver
* Extend the class AbstractSolver
* Make sure to create a run() method that attempts to solve the given [MineMap](https://kecskemeti.github.io/CompetitiveMinesweeper/uk/ac/ljmu/fet/cs/csw/CompetitiveMinesweeper/base/MineMap.html). 
* When the game is running, this method will be run in its own thread and the GUI will visualise the game while your AI tries to solve the MineMap. The GUI assumes your AI will eventually make the MineMap pronouncing the end of the game. If this is not the case please rework your AI. 
* You are recommended to check out MineMap's api documentation for further details.
