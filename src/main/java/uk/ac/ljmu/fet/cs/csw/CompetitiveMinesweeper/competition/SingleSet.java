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

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * Allows the arrangement of a single multiple match set between two solvers.
 * The arranged set will have 10 matches each. The matches are ran in a
 * sequential fashion.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class SingleSet implements Scorer {
	/**
	 * The arranged set with all the matches. This is automatically generated upon
	 * construction of an instance of this class.
	 */
	private final SingleMatch[] matches = new SingleMatch[20];
	/**
	 * The two solvers that play against each other
	 */
	public final Class<? extends GameSolverThread> solverOne, solverTwo;
	/**
	 * The cumulative score of each solver
	 */
	private int sumSubScoreOne = -1, sumSubScoreTwo = -1;

	/**
	 * Prepares the set so it is ready to run with the {@link #runSet()} method. It
	 * generates ten matches to be played. Each match is has a randomly generated
	 * participant order to ensure there is no chance for preferential handling of
	 * any participant.
	 * 
	 * @param solverOne The first solver to take part in the set.
	 * @param solverTwo The second solver to take part in the set.
	 */
	public SingleSet(Class<? extends GameSolverThread> solverOne, Class<? extends GameSolverThread> solverTwo) {
		this.solverOne = solverOne;
		this.solverTwo = solverTwo;
		ArrayList<Class<? extends GameSolverThread>> solvers = new ArrayList<Class<? extends GameSolverThread>>();
		solvers.add(solverOne);
		solvers.add(solverTwo);
		for (int i = 0; i < matches.length; i++) {
			// Randomise the match participant order so our outcome depends less
			// on the order we received the solvers
			Collections.shuffle(solvers);
			matches[i] = new SingleMatch(solvers.get(0), solvers.get(1));
		}
	}

	/**
	 * Runs the matches previously arranged by the constructor. Prints the status of
	 * the matches and prints a summary at the end of the set.
	 * 
	 * @throws InterruptedException      see {@link SingleMatch#runMatch()}
	 * @throws SecurityException         see {@link SingleMatch#runMatch()}
	 * @throws NoSuchMethodException     see {@link SingleMatch#runMatch()}
	 * @throws InvocationTargetException see {@link SingleMatch#runMatch()}
	 * @throws IllegalArgumentException  see {@link SingleMatch#runMatch()}
	 * @throws IllegalAccessException    see {@link SingleMatch#runMatch()}
	 * @throws InstantiationException    see {@link SingleMatch#runMatch()}
	 */
	public void runSet() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException {
		System.out.println("~~~~~~~ Starting set ~~~~~~~");
		System.out.println(this);
		if (sumSubScoreOne < 0) {
			// On the first run we initialize the scores
			sumSubScoreOne = 0;
			sumSubScoreTwo = 0;
		}
		for (SingleMatch m : matches) {
			if (!m.isMatchRan()) {
				// Matches ran only once
				m.runMatch();
				System.out.println(m);

				// Accumulate scores
				if (m.solverOne == solverOne) {
					sumSubScoreOne += m.getPointsForTeamOne();
					sumSubScoreTwo += m.getPointsForTeamTwo();
				} else {
					sumSubScoreOne += m.getPointsForTeamTwo();
					sumSubScoreTwo += m.getPointsForTeamOne();
				}
			}
		}
		System.out.println(this);
		System.out.println("~~~~~~~ End of set ~~~~~~~");
	}

	/**
	 * Awards 3 points for the first solver if it achieved more points than the
	 * second. If both solvers achieve the same amount of points it awards 1 for the
	 * first solver. Otherwise it offers 0 points for the first solver.
	 */
	@Override
	public int getPointsForTeamOne() {
		return sumSubScoreOne > sumSubScoreTwo ? 3 : (sumSubScoreOne == sumSubScoreTwo ? 1 : 0);
	}

	/**
	 * Inverts the number of points that the first solver receives (see
	 * {@link #getPointsForTeamOne()}).
	 */
	@Override
	public int getPointsForTeamTwo() {
		final int t1pts = getPointsForTeamOne();
		return t1pts == 3 ? 0 : (t1pts == 0 ? 3 : 1);
	}

	/**
	 * Offers an easy way to present the results of a set if it is to be show in a
	 * textual form.
	 */
	@Override
	public String toString() {
		return "Set between " + solverOne.getName() + " and " + solverTwo.getName()
				+ (sumSubScoreOne < 0 ? ""
						: (" scores: " + sumSubScoreOne + ":" + sumSubScoreTwo + " points: " + getPointsForTeamOne()
								+ "/" + getPointsForTeamTwo()));
	}
}
