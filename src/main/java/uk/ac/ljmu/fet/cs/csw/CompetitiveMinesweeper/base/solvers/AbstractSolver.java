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
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui.SimpleGamePanel;
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
	private SimpleGamePanel myGUI = null;

	/**
	 * Tells whether to allow calling the {@link #run()} method without setting the
	 * GUI first. If true, the {@link #run()} method will not run without a sendGUI
	 * method call first.
	 */
	private boolean requiresGUI = false;

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
	protected SimpleGamePanel getMyGUI() {
		return myGUI;
	}

	/**
	 * Ensures we don't run without getting a map and a gui.
	 */
	@Override
	public void run() {
		if (myMap == null || (requiresGUI && myGUI == null)) {
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
	public void sendGUI(final SimpleGamePanel myVisualiser) {
		if (myGUI != null) {
			throw new IllegalStateException("We already received a GUI before...");
		}
		myGUI = myVisualiser;
	}

	/**
	 * Solvers that does not depend on a GUI directly can disable for its checking
	 * in the {@link #run()} method. This is intended for subclasses to call if they
	 * wish to allow GUI less operations (useful for most AIs).
	 * 
	 * @param requiresGUI False if it is ok to start the run method without a gui
	 *                    passed to the solver. True otherwise. The default value is
	 *                    false, it does not have to be set.
	 */
	protected void setRequiresGUI(final boolean requiresGUI) {
		this.requiresGUI = requiresGUI;
	}

	@Override
	public boolean requiresGUI() {
		return requiresGUI;
	}

}
