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

/**
 * Runs two phase competition. First it organises solver groups of 3-4 solvers.
 * The solvers of the groups must be specified on the command line. It organises
 * individual {@link TeamCompetition}s for each group arranged. Then it selects
 * the best eight teams from the groups according to the results of the
 * {@link TeamCompetition}. In the second phase, these top teams then take place
 * in a {@link SingleEliminationTournament}.
 * 
 * This is one of the main executables. If you have more than 8 solver
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
public class RunCompetitionMultiPhase {

	/**
	 * Validates the argument list, loads them as class objects, and randomizes
	 * their order for fairness
	 * 
	 * @param args the unparsed command line argument list
	 * @return the class objects representing all solvers to take part in the
	 *         competition
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<Class<? extends GameSolverThread>> parseCompetingClassNames(String[] args, int minSolvers) {
		if (args.length < minSolvers) {
			System.err.println("There are not enough solvers listed in the cli argument list.");
			System.err.println("You should specify at least " + minSolvers + ".");
			System.err.println("Please use the following format: RunCompetition [FullyQualifiedClassName]*");
			System.exit(1);
		}
		ArrayList<Class<? extends GameSolverThread>> preList = new ArrayList<Class<? extends GameSolverThread>>();
		for (int i = 0; i < args.length; i++) {
			try {
				preList.add((Class<? extends GameSolverThread>) Class.forName(args[i]));
			} catch (Exception e) {
				System.err.println("Argument " + i + " does not seem to be a fully qualified classname: " + args[i]);
				System.err.println("Make sure all arguments are referring to classes in your classpath!");
				System.exit(1);
			}
		}
		Collections.shuffle(preList);
		return preList;
	}

	public static void main(String[] args) throws Exception {
		// Preparing the teams
		ArrayList<Class<? extends GameSolverThread>> preList = parseCompetingClassNames(args, 8);
		TeamCompetition[] competitions = new TeamCompetition[(args.length + 3) / 4];
		for (int i = 0; i < competitions.length; i++) {
			competitions[i] = new TeamCompetition(true);
		}
		for (int i = 0; i < preList.size(); i++) {
			// Round robin assignment to teams
			competitions[i % competitions.length].addToCompetitors(preList.get(i));
		}
		// By now teams are set, we are good to go

		ArrayList<SolverRanking> rankings = new ArrayList<SolverRanking>();
		System.out.println("Starting team competition phase!");
		int groupIndex = 1;
		for (TeamCompetition tc : competitions) {
			System.out.println("Starting team " + groupIndex);
			tc.arrangeSets();
			tc.runSets();
			// merge the rankings across all groups
			rankings.addAll(tc.getRankedList());
			System.out.println("Completed team " + groupIndex);
			groupIndex++;
		}
		System.out.println("Team competitions finished. Merged rankings:");

		// We sort all solvers to see who performed the best in each group
		Collections.sort(rankings);
		ArrayList<Class<? extends GameSolverThread>> topSolvers = new ArrayList<Class<? extends GameSolverThread>>();
		for (SolverRanking pr : rankings) {
			// Only the first 8 teams go to the knockout phase
			if (topSolvers.size() < 8) {
				topSolvers.add(pr.solver);
			}
			System.out.println(pr);
		}

		// Knockout phase
		System.out.println("Sinlge elimination tournament starts.......");
		topSolvers = SingleEliminationTournament.runCompetition(topSolvers);

		// Results
		System.out.println("Sinlge elimination tournament completed final league table:");
		for (int i = 0; i < topSolvers.size(); i++) {
			System.out.println((i + 1) + ". " + topSolvers.get(i).getName());
		}
	}

}
