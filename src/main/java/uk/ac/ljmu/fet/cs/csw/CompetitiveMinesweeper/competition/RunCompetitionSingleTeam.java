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

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * Runs an unlimited size team competition with the solvers that are specified
 * as command line parameters.
 * 
 * This is one of the main executables. If you don't have more than 8 solver
 * implementations, it is recommended to run your competition with this
 * executable.
 * 
 * <i>Note:</i> The solvers in the command line parameters must be specified as
 * fully qualified class names (including package name). For example, the simple
 * line by line solver should be referred as
 * <i>uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.SimpleLineByLineSolver</i>
 * on the command line. The referred class must be loadable by the same class
 * loader that loaded the executable itself (i.e., it must be in the class
 * path).
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class RunCompetitionSingleTeam {
	public static void main(String[] args) throws Exception {
		TeamCompetition competition = new TeamCompetition(false);
		ArrayList<Class<? extends GameSolverThread>> preList = RunCompetitionMultiPhase.parseCompetingClassNames(args,
				3);
		// All competitors into a single team competition
		for (Class<? extends GameSolverThread> cp : preList) {
			competition.addToCompetitors(cp);
		}
		ArrayList<SolverRanking> rankings;
		System.out.println("Starting competition!");
		competition.arrangeSets();
		competition.runSets();
		rankings = competition.getRankedList();
		System.out.println("Competition finished. League table:");
		for (int i = 0; i < rankings.size(); i++) {
			System.out.println((i + 1) + ". " + rankings.get(i));
		}

	}
}
