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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui.MineSweeper;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

public class SingleMatch implements Scorer {
	public static final int minRows = 5;
	public static final int maxRows = 100;
	public static final int minCols = 5;
	public static final int maxCols = 100;

	public final Class<? extends GameSolverThread> solverOne, solverTwo;
	private boolean matchRan = false;
	private static final Random rng = new Random();

	private int totalScoreOne = 0, totalScoreTwo = 0;

	public SingleMatch(Class<? extends GameSolverThread> solverOne, Class<? extends GameSolverThread> solverTwo)
			throws Exception {
		this.solverOne = solverOne;
		this.solverTwo = solverTwo;
	}

	/**
	 * Runs two solvers in parallel and tests them for their performance on a
	 * randomly generated map. The map's complexity is also set randomly within the
	 * limits of this class' constants.
	 * 
	 * 
	 * @throws InstantiationException    if there is an issue of instantiation one
	 *                                   of the solvers
	 * @throws IllegalAccessException    if there is an issue of instantiation one
	 *                                   of the solvers
	 * @throws IllegalArgumentException  if there is an issue of instantiation one
	 *                                   of the solvers
	 * @throws InvocationTargetException if there is an issue of instantiation one
	 *                                   of the solvers
	 * @throws NoSuchMethodException     if there is an issue of instantiation one
	 *                                   of the solvers
	 * @throws SecurityException         if there is an issue of instantiation one
	 *                                   of the solvers
	 * @throws InterruptedException      if the sleep of the runMatch method was
	 *                                   interrupted externally. If an interruption
	 *                                   occurs, the match never reaches its
	 *                                   completion
	 */
	public void runMatch() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException {
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
				GameSolverThread firstSolverInstance = solverOne.getConstructor().newInstance();
				firstSolverInstance.sendMap(solverOneMap);
				theTwoSolvers.add(firstSolverInstance);
				GameSolverThread secondSolverInstance = solverOne.getConstructor().newInstance();
				secondSolverInstance.sendMap(solverTwoMap);
				theTwoSolvers.add(secondSolverInstance);
				if (firstSolverInstance.requiresGUI() || secondSolverInstance.requiresGUI()) {
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
				Thread.sleep(5);
				for (int j = 0; j < 2; j++) {
					if (runnerThreads.get(j).isAlive()) {
						System.err.println(theTwoSolvers.get(j).getClass().getName()
								+ " did not terminate at the end of the game. It should be excluded from competitions.");
						System.exit(1);
					}
				}

				totalScoreOne += getCurrentScore(solverOneMap, oneCompleteAt - startTime, solverTwoMap,
						twoCompleteAt - startTime);
				totalScoreTwo += getCurrentScore(solverTwoMap, twoCompleteAt - startTime, solverOneMap,
						oneCompleteAt - startTime);

				// System.out.println("Duration of match was: " + (System.currentTimeMillis() -
				// startTime) + "ms");
			}
			matchRan = true;
		}
	}

	private static int getCurrentScore(MineMap teamOne, long durOne, MineMap teamTwo, long durTwo) {
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
				int exploreComponent = (int) Math.max(40, 20 * ((double) teamOne.getExploredAreaSize())
						/ (teamTwo.getExploredAreaSize() == 0 ? 1 : teamTwo.getExploredAreaSize()));
				// Incorrectly identified mines are highly penalised
				int flagComponent = (int) Math.max(20,
						10 * ((double) teamOne.getCorrectlyIdentifiedMineCount())
								/ (teamOne.getInCorrectlyIdentifiedMineCount() == 0 ? 1
										: teamOne.getInCorrectlyIdentifiedMineCount() * 10));
				return exploreComponent + flagComponent;
			}
		}

	}

	@Override
	public int getPointsForTeamOne() {
		return matchRan ? totalScoreOne : -1;
	}

	@Override
	public int getPointsForTeamTwo() {
		return matchRan ? totalScoreTwo : -1;
	}

	public boolean isMatchRan() {
		return matchRan;
	}

	@Override
	public String toString() {
		return "Match between " + solverOne.getName() + " and " + solverTwo.getName() + " score: "
				+ (matchRan ? ("" + getPointsForTeamOne() + "/" + getPointsForTeamTwo()) : "-");
	}
}
