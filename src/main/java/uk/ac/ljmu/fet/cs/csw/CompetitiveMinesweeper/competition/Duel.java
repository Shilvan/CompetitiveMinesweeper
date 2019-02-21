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

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * Runs a simple duel between two solvers. The solvers of the groups must be
 * specified on the command line. The winner of the duel is decided based on
 * {@link SingleSet}'s behaviour.
 * 
 * This is one of the main executables. If you want to check your solver's
 * performance in relation to a single other solver implementation, then this is
 * the executable to use.
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
public class Duel {

	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, InterruptedException {
		// Loads up the two participant's names and runs a single set between the two
		// solvers
		ArrayList<Class<? extends GameSolverThread>> theParticipants = RunCompetitionMultiPhase
				.parseCompetingClassNames(args, 2);
		SingleSet setForDuel = new SingleSet(theParticipants.get(0), theParticipants.get(1));
		setForDuel.runSet();
	}
}
