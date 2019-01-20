/*
 *  ========================================================================
 *  Competitive Minesweeper
 *  ========================================================================
 *  
 *  This file is part of Competitive Minesweeper.
 *  
 *  Competitive Minesweeper Interpreter is free software: you can redistribute
 *  it and/or modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the License,
 *  or (at your option) any later version.
 *  
 *  Competitive Minesweeper Interpreter is distributed in the hope that it will
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with Competitive Minesweeper.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2019, Gabor Kecskemeti (g.kecskemeti@ljmu.ac.uk)
 */
package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * A GUI that produces a tiled layout with several independent games solved by
 * the same solver. In order to start this GUI, the main method receives all its
 * configuration via the CLI. It can handle the following CLI parameters:
 * <ol>
 * <li>The fully qualified name of the solver class to be used in all
 * GamePanels</li>
 * <li>The dimensions of the MineMap (e.g., 20x20)
 * <li>The mine ratio (e.g., .12, for details consult {@link MineSweeper} which
 * have several suggested mine ratios and their corresponding names in the
 * GUI)</li>
 * <li>The amount of time introduced by the Minemap in each AI instructed
 * operation in milliseconds (e.g., 1000) - useful to slow down the AI to
 * visually pleasing speeds. Setting this below 100 is really dangerous and
 * could result in the rapid spawning and disappearance of windows.</li>
 * <li>The number of games to be played in a row</li>
 * <li>The number of games to be played in a column</li>
 * </ol>
 * Apart from the several games shown in parallel. If a game ends (whatever is
 * the outcome) its corresponding game panel is also terminated. To see how the
 * AI performed so far the GUI also shows some simple statistics (in terms of
 * won and lost games). <br>
 * Once the games start, the GUI can be terminated by clicking on any of the
 * close (x) buttons.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */

public class FullScreenSolveShow {
	static int won = 0, lost = 0;

	public static void main(final String[] args) throws Exception {
		final Class<? extends GameSolverThread> toBeTiledSolver = (Class<? extends GameSolverThread>) Class
				.forName(args[0]);
		// Setting up the stats window with the relevant label
		JFrame statsWindow = new JFrame();
		statsWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel statsLabel = new JLabel("WON:            LOST:           ");
		statsLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
		statsWindow.add(statsLabel);
		statsWindow.setAlwaysOnTop(true);
		statsWindow.pack();
		statsWindow.setVisible(true);
		final String[] dimensions = args[1].split("x");
		for (int i = 0; i < Integer.parseInt(args[4]); i++) {
			for (int j = 0; j < Integer.parseInt(args[5]); j++) {
				final int myI = i, myJ = j;
				new Thread() {
					@Override
					public void run() {
						try {
							while (true) {
								MineMap theActualMapThatIsSolved = GUIHelper.launchCompetitor(toBeTiledSolver,
										GUIHelper.simpleLauncher,
										new MineMap(Integer.parseInt(dimensions[0]), Integer.parseInt(dimensions[1]),
												Double.parseDouble(args[2]), Integer.parseInt(args[3])),
										myI, myJ, false);
								while (!theActualMapThatIsSolved.isEnded()) {
									sleep(100);
								}
								// Serialise the updates on won/lost
								synchronized (statsWindow) {
									if (theActualMapThatIsSolved.isWon()) {
										won++;
									} else {
										lost++;
									}
									statsLabel.setText("WON: " + won + " LOST: " + lost);
								}
							}
						} catch (Exception e) {
							System.err.println("The gui could not start...");
							System.exit(1);
						}
					}
				}.start();
			}
		}
	}
}
