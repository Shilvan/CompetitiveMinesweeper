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
package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.competition;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap.MapCopyException;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui.MineSweeper;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * Allows the arrangement of a single match between two solvers. To minimise the
 * effect of luck, a single match is played on the same initially randomly
 * generated map 5 times. The score of the solvers is determined based on their
 * performance in all 5 attempts at the map.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class SingleMatch implements Scorer {
	public final static boolean quiet = System
			.getProperty("uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.competition.SingleMatch.quietmode") != null;

	public final static PrintStream realStdOut = System.out;
	public final static PrintStream realStdErr = System.err;
	/**
	 * The minimum number of rows in a particular match. When a match is set up the
	 * base map will be generated with this in mind.
	 */
	public static final int minRows = 5;
	/**
	 * The maximum number of rows in a particular match. When a match is set up the
	 * base map will be generated with this in mind.
	 */
	public static final int maxRows = 100;
	/**
	 * The minimum number of columns in a particular match. When a match is set up
	 * the base map will be generated with this in mind.
	 */
	public static final int minCols = 5;
	/**
	 * The maximum number of columns in a particular match. When a match is set up
	 * the base map will be generated with this in mind.
	 */
	public static final int maxCols = 100;

	/**
	 * The two solvers which take part in the match.
	 */
	public final Class<? extends GameSolverThread> solverOne, solverTwo;
	/**
	 * true if the {@link #runMatch()} method has already completed the match
	 * between the two solvers.
	 */
	private boolean matchRan = false;
	/**
	 * Ensures a fresh random generator across all matches.
	 */
	private static final Random rng = new Random();

	/**
	 * The cumulative score of the match. These are the fields where
	 * {@link #runMatch()} keeps track of the scores awarded to the solvers so far.
	 * Once the match is done this never changes again.
	 */
	private int totalScoreOne = 0, totalScoreTwo = 0;

	/**
	 * Keeps hold of the two solvers that needs to compete in the current match.
	 * 
	 * @param solverOne The first solver to participate in the match.
	 * @param solverTwo The second solver to participate in the match.
	 */
	public SingleMatch(Class<? extends GameSolverThread> solverOne, Class<? extends GameSolverThread> solverTwo) {
		this.solverOne = solverOne;
		this.solverTwo = solverTwo;
	}

	/**
	 * Runs two solvers in parallel (the solvers are started in a random order to
	 * make sure the timing of their performance is consistent) and tests them for
	 * their performance on a randomly generated map. The map's complexity is also
	 * set randomly within the limits of this class' constants. The mine ratio of
	 * the map is set between Easy to Insane (see {@link MineSweeper#levels}).
	 * 
	 * A single map is generated, but each solver has a chance to solve it 5 times.
	 * The final score of the match is determined based on the total points awarded
	 * by {@link #getCurrentScore(MineMap, long, MineMap, long)} after each chance.
	 * 
	 * @throws InstantiationException    if there is an issue of instantiation with
	 *                                   one of the solvers
	 * @throws IllegalAccessException    if there is an issue of instantiation with
	 *                                   one of the solvers
	 * @throws IllegalArgumentException  if there is an issue of instantiation with
	 *                                   one of the solvers
	 * @throws InvocationTargetException if there is an issue of instantiation with
	 *                                   one of the solvers
	 * @throws NoSuchMethodException     if there is an issue of instantiation with
	 *                                   one of the solvers
	 * @throws SecurityException         if there is an issue of instantiation with
	 *                                   one of the solvers
	 * @throws InterruptedException      if the sleep of the runMatch method was
	 *                                   interrupted externally. If an interruption
	 *                                   occurs, the match never reaches its
	 *                                   completion
	 * @throws RuntimeException          if one tries to re-run an already done
	 *                                   match.
	 */
	public void runMatch() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException {
		try {
			if (matchRan) {
				throw new RuntimeException("Attemted to run a match two times");
			} else {
				MineMap singleMatchMap = new MineMap(rng.nextInt(maxRows - minRows) + minRows,
						rng.nextInt(maxCols - minCols) + minCols,
						// Ignores the easiest mine ratio, but allows any others
						MineSweeper.mineRatios[rng.nextInt(MineSweeper.mineRatios.length - 1) + 1], 0);

				// We need to ask to solve the same map a few times to make sure there is little
				// effect of initial random probing on the map
				for (int i = 0; i < 5; i++) {
					MineMap solverOneMap = new MineMap(singleMatchMap);
					MineMap solverTwoMap = new MineMap(singleMatchMap);
					ArrayList<GameSolverThread> theTwoSolvers = new ArrayList<>();
					ArrayList<Thread> runnerThreads = new ArrayList<>();
					introduceRedirections();
					GameSolverThread firstSolverInstance = solverOne.getConstructor().newInstance();
					firstSolverInstance.sendMap(solverOneMap);
					theTwoSolvers.add(firstSolverInstance);
					GameSolverThread secondSolverInstance = solverTwo.getConstructor().newInstance();
					secondSolverInstance.sendMap(solverTwoMap);
					theTwoSolvers.add(secondSolverInstance);
					if (firstSolverInstance.requiresGUI() || secondSolverInstance.requiresGUI()) {
						revertRedirects();
						throw new RuntimeException("GUI based solvers cannot compete with SingleMatch");
					}
					// Randomizing the order with which the solvers are instantiated
					Collections.shuffle(theTwoSolvers);
					long startTime = System.currentTimeMillis();
					// We should finish off both threads in the next minute
					long maxAllowedTime = startTime + 60000;
					for (GameSolverThread currSolver : theTwoSolvers) {
						Thread runner = new Thread(currSolver);
						runnerThreads.add(runner);
						runner.start();
					}
					long oneCompleteAt = -1, twoCompleteAt = -1;
					// We now have our two competing threads running, we can test for their
					// completion
					while (maxAllowedTime > System.currentTimeMillis() && (oneCompleteAt < 0 || twoCompleteAt < 0)) {
						// Time is saved here so we can be sure one does not have an edge over two
						// because it is tested for completion later
						long currentTime = System.currentTimeMillis();
						if (oneCompleteAt == -1 && solverOneMap.isEnded()) {
							oneCompleteAt = currentTime;
						}
						if (twoCompleteAt == -1 && solverTwoMap.isEnded()) {
							twoCompleteAt = currentTime;
						}
						Thread.sleep(1);
					}
					revertRedirects();
					// We wait a bit to allow both solvers to clean up and exit their solver
					// threads.
					Thread.sleep(10);
					for (int j = 0; j < 2; j++) {
						if (runnerThreads.get(j).isAlive()) {
							System.err.println(theTwoSolvers.get(j).getClass().getName()
									+ " did not terminate at the end of the game. It should be excluded from competitions.");
							System.exit(1);
						}
					}
					final long oneDur = oneCompleteAt - startTime;
					final long twoDur = twoCompleteAt - startTime;
					totalScoreOne += getCurrentScore(solverOneMap, oneDur, solverTwoMap, twoDur);
					totalScoreTwo += getCurrentScore(solverTwoMap, twoDur, solverOneMap, oneDur);

					// If interested in the performance of your solver you can check it out by
					// uncommenting the below line:
					// System.out.println("Duration of match was: " + (System.currentTimeMillis() -
					// startTime) + "ms");
				}
				matchRan = true;
			}
		} catch (MapCopyException ex) {
			System.err.println("One of the solvers tried to copy the map. This is a malicious activity.");
			System.err.println("We are stopping now to allow the exclusion of the problematic solver.");
			System.err.println("Solvers in question:");
			System.err.println(solverOne.getName());
			System.err.println(solverTwo.getName());
			System.err.println();
			System.err.println();
			ex.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Calculates the score of the team which was listed first in its parameters.
	 * The calculation is following the below scheme:
	 * <ol>
	 * <li>Awards 100 points if the first team won but the other lost the game</li>
	 * <li>Awards 90 points if both teams won but the first one was faster</li>
	 * <li>Awards 80 points if both teams won but the first one was slower</li>
	 * <li>Awards 0 points if the first team lost but the second won</li>
	 * <li>If both teams lost the map, they both receive points based on two
	 * components:
	 * <ul>
	 * <li>the ratio of correctly/incorrectly flagged mines (incorrect ones are
	 * strongly penalised), max 20 points</li>
	 * <li>the ratio of the explored areas of the two teams (i.e., how much more the
	 * first team explored), max 40 points - 20 is the baseline if both teams
	 * explore around the same area.</li>
	 * </ul>
	 * </li>
	 * </ol>
	 * 
	 * @param teamOne The final map of the first team
	 * @param durOne  The time it took team one to solve the map
	 * @param teamTwo The final map of the second team
	 * @param durTwo  The time it took for the second team to solve the map
	 * @return The score of the team which was listed first in the parameters
	 */
	private static int getCurrentScore(final MineMap teamOne, final long durOne, final MineMap teamTwo,
			final long durTwo) {
		if (teamOne.isWon()) {
			if (teamTwo.isWon()) {
				return durOne > durTwo ? 90 : 80;
			} else {
				return 100;
			}
		} else {
			if (teamTwo.isWon()) {
				return 0;
			} else {
				// Both teams lost
				// Teams with more explored areas will get a higher score
				final double exOne = teamOne.getExploredAreaSize();
				final double exTwo = teamTwo.getExploredAreaSize();
				final int exploreComponent = exTwo == 0 ? 40 : (int) Math.min(40, 20 * exOne / exTwo);
				final double correct = teamOne.getCorrectlyIdentifiedMineCount();
				final double incorrect = teamOne.getInCorrectlyIdentifiedMineCount();

				// Incorrectly identified mines are highly penalised
				final int flagComponent = incorrect == 0 ? 20 : (int) Math.min(20, 10 * (correct / (incorrect * 10)));
				return exploreComponent + flagComponent;
			}
		}

	}

	/**
	 * Tells how the first team performed in the match. If the match has not been
	 * run just yet, this returns -1.
	 * 
	 * See the {@link #getCurrentScore(MineMap, long, MineMap, long)} method for
	 * details on how a solver's score is determined.
	 */
	@Override
	public int getPointsForTeamOne() {
		return matchRan ? totalScoreOne : -1;
	}

	/**
	 * Tells how the second team performed in the match. If the match has not been
	 * run just yet, this returns -1.
	 * 
	 * See the {@link #getCurrentScore(MineMap, long, MineMap, long)} method for
	 * details on how a solver's score is determined.
	 */
	@Override
	public int getPointsForTeamTwo() {
		return matchRan ? totalScoreTwo : -1;
	}

	/**
	 * Allows to query whether the match has already been done or not.
	 * 
	 * @return true if the match was done and the points for the scorer are the
	 *         correct ones
	 */
	public boolean isMatchRan() {
		return matchRan;
	}

	/**
	 * Offers an easy way to present the results of a match if it is to be show in a
	 * textual form.
	 */
	@Override
	public String toString() {
		return "Match between " + solverOne.getName() + " and " + solverTwo.getName() + " score: "
				+ (matchRan ? ("" + getPointsForTeamOne() + "/" + getPointsForTeamTwo()) : "-");
	}

	public void introduceRedirections() {
		if (quiet) {
			try {
				System.setOut(new PrintStream(new OutputStream() {
					@Override
					public void write(int arg0) throws IOException {

					}
				}));
				System.setErr(new PrintStream(new OutputStream() {

					@Override
					public void write(int arg0) throws IOException {

					}
				}));

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void revertRedirects() {
		if (quiet) {
			System.setOut(realStdOut);
			System.setErr(realStdErr);
		}
	}
}
