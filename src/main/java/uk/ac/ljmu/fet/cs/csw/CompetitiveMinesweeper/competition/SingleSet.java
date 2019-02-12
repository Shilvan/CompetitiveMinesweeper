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

import java.util.ArrayList;
import java.util.Collections;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

public class SingleSet implements Scorer {
	private final SingleMatch[] matches = new SingleMatch[10];
	public final Class<? extends GameSolverThread> solverOne, solverTwo;
	private int sumSubScoreOne = -1, sumSubScoreTwo = -1;

	public SingleSet(Class<? extends GameSolverThread> solverOne, Class<? extends GameSolverThread> solverTwo)
			throws Exception {
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

	public void runSet() throws Exception {
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

	@Override
	public int getPointsForTeamOne() {
		return sumSubScoreOne > sumSubScoreTwo ? 3 : (sumSubScoreOne == sumSubScoreTwo ? 1 : 0);
	}

	@Override
	public int getPointsForTeamTwo() {
		final int t1pts = getPointsForTeamOne();
		return t1pts == 3 ? 0 : (t1pts == 0 ? 3 : 1);
	}

	@Override
	public String toString() {
		return "Set between " + solverOne.getName() + " and " + solverOne.getName()
				+ (sumSubScoreOne < 0 ? ""
						: (" scores: " + sumSubScoreOne + ":" + sumSubScoreTwo + " points: " + getPointsForTeamOne()
								+ "/" + getPointsForTeamTwo()));
	}
}
