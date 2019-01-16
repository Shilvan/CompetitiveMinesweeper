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
package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui.SingleGamePanel;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * This is the class that is to be extended by most AIs.
 * 
 * The base functionality of getting details about the solvable map and GUI is
 * implemented here so such bolierplate code does not have to be implemented by
 * any other solvers. This class also defends the data that it receives by
 * blocking write access to them.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public abstract class AbstractSolver implements GameSolverThread {
	/**
	 * The map to be solved. Can't be changed by subclasses of the AbstractSolver.
	 * Instead it can be queried if they wish to do so.
	 */
	private MineMap myMap = null;
	/**
	 * The GUI to interact with (i.e,. where the flashing labels can acquired from
	 * that represent the spots on the grid). Can't be changed by subclasses of the
	 * AbstractSolver. Instead it can be queried if they wish to do so.
	 */
	private SingleGamePanel myGUI = null;

	/**
	 * Subclasses can get the mine map to be solved via this method. This ensures
	 * that subclasses cannot mistakenly forget about the map that they need to
	 * solve.
	 * 
	 * @return the map to solve.
	 */
	protected MineMap getMyMap() {
		return myMap;
	}

	/**
	 * Subclasses can get the gui of the minemap to be solved via this method. This
	 * ensures that subclasses cannot mistakenly forget about the gui that they need
	 * to interact with.
	 * 
	 * @return the gui for the map's visualisation.
	 */
	protected SingleGamePanel getMyGUI() {
		return myGUI;
	}

	/**
	 * Ensures we don't run without getting a map and a gui.
	 */
	@Override
	public void run() {
		if (myMap == null || myGUI == null) {
			throw new IllegalStateException("Cannot start the solver before sending over the map&GUI");
		}
	}

	/**
	 * The Minemap is saved to the corresponding field: {@link #myMap}. If the map
	 * was already saved before this method complains to the caller with an
	 * IllegalStateException.
	 */
	@Override
	public void sendMap(final MineMap toSolve) {
		if (myMap != null) {
			throw new IllegalStateException("We already received a map before...");
		}
		myMap = toSolve;
	}

	/**
	 * Receives the GUI that could be used for customising the AI's window. This is
	 * most useful for human based/assisted AIs. If the GUI was already saved before
	 * this method complains to the caller with an IllegalStateException.
	 */
	@Override
	public void sendGUI(final SingleGamePanel myVisualiser) {
		if (myGUI != null) {
			throw new IllegalStateException("We already received a GUI before...");
		}
		myGUI = myVisualiser;
	}

}
