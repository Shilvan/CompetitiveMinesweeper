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
import java.util.Random;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * Offers an implementation of a single elimination tournament. For details see:
 * <a href=
 * "https://en.wikipedia.org/wiki/Single-elimination_tournament">wikipedia</a>.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class SingleEliminationTournament {

	/**
	 * This function runs a single elimination tournament to order the current
	 * competitors list according to their strength. By the end of the run, all
	 * competitors in the currentCompetitors list will be ordered.
	 * 
	 * <i>Note:</i> as the function uses recursion, you should restrict the size of
	 * the knockout phase to numbers that would fit in the stack.
	 * 
	 * @param currentCompetitors the list of competitors to be ordered
	 * @return the ordered list of competitors by strength (the first item in the
	 *         list is the strongest, the last is the weakest)
	 * @throws Exception
	 */
	public static ArrayList<Class<? extends GameSolverThread>> runCompetition(
			ArrayList<Class<? extends GameSolverThread>> currentCompetitors) throws Exception {
		if (currentCompetitors.size() % 2 != 0) {
			throw new RuntimeException("Cannot process non-even membered tournaments");
		}
		Collections.shuffle(currentCompetitors);
		Random rndGen = new Random();
		ArrayList<Class<? extends GameSolverThread>> winners = new ArrayList<Class<? extends GameSolverThread>>();
		ArrayList<Class<? extends GameSolverThread>> losers = new ArrayList<Class<? extends GameSolverThread>>();
		for (int i = 0; i < currentCompetitors.size() - 1; i += 2) {
			int miniRounds = 0;
			SingleSet ss;
			// Repeat each match until someone is a clear winner
			do {
				ss = new SingleSet(currentCompetitors.get(i), currentCompetitors.get(i + 1));
				ss.runSet();
			} while (ss.getPointsForTeamOne() == 1 && miniRounds++ < 10);
			// If after 10 sets we still don't have a clear winner we randomly
			// pick one
			winners.add(ss.getPointsForTeamOne() == 3 ? ss.solverOne
					: (ss.getPointsForTeamOne() == 1 ? (rndGen.nextBoolean() ? ss.solverOne : ss.solverTwo)
							: ss.solverTwo));
			losers.add(ss.solverOne == winners.get(winners.size() - 1) ? ss.solverTwo : ss.solverOne);
		}
		if (winners.size() > 1) {
			winners = runCompetition(winners);
			winners.addAll(runCompetition(losers));
		} else {
			winners.add(losers.get(0));
		}
		return winners;
	}

}
