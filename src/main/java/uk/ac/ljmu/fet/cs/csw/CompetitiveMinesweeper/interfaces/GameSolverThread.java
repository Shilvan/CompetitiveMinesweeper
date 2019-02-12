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
package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.AbstractSolver;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.SimpleLineByLineSolver;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui.MineSweeper;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui.SimpleGamePanel;

/**
 * The interface each AI has to implement in order to allow it to take part in a
 * competition. Implementors of this interface can be launched by a configurator
 * in their dedicated threads. For details see
 * {@link MineSweeper#launchCompetitor(Class, MineMap, boolean)} for details.
 * Notice that this interface extends the {@link Runnable} interface. Which
 * means an extra run method also has to be implemented in it. The
 * {@link Runnable#run()} method should provide the AI's main functionality
 * (i.e., exploration). To understand how simple exploration of the map would
 * work, have a look at the {@link SimpleLineByLineSolver}. To simpify the
 * implementation of this interface it is a good idea to start it by extending
 * the class {@link AbstractSolver}.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public interface GameSolverThread extends Runnable {
	/**
	 * Make sure the to be solved map is propagated to the solver.
	 * 
	 * @param toSolve The to be solved map.
	 */
	void sendMap(MineMap toSolve);

	/**
	 * Although for most solvers the GUI is not necessary, it is also passed along
	 * to allow solvers to provide extra feedback on the user interface.
	 * 
	 * @param myVisualiser The GUI of our solver's map.
	 */
	void sendGUI(SimpleGamePanel myVisualiser);

	/**
	 * Tells if the solver can function without a GUI passed.
	 * 
	 * @return true if there is a GUI needed for the operation of this solver. false
	 *         otherwise.
	 */
	boolean requiresGUI();
}
