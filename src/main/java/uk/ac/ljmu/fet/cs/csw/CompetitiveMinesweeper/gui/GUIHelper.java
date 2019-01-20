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
package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.lang.reflect.InvocationTargetException;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.HumanSolver;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * This class offers automation on launching a GUI and an AI in parallel to each
 * other. The class also offers linking the GUI and the AI together.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class GUIHelper {
	/**
	 * The interface allows customising the creation of a SimpleGamePanel
	 * 
	 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
	 *         Moores University, (c) 2019"
	 */
	public static interface LaunchGUI {
		/**
		 * This is a factory method for GUIs. Allows us to provide alternative means of
		 * instantiating SimpleGamePanels.
		 * 
		 * @param map   The map to be shown on the panel
		 * @param title The title of the panel
		 * @param scale Should the size of the spots scale or stay at their original
		 *              size?
		 * @return A game panel object that meets the implemetor's custom needs
		 */
		SimpleGamePanel launch(MineMap map, String title, boolean scale);
	}

	/**
	 * This class offers an implementation of the {@link LaunchGUI} interface on the
	 * simplest way possible: directly calling the constructor of
	 * {@link SimpleGamePanel}. Note that this class is handled as a singleton (so
	 * it can't be instantiated). To access its singleton use the
	 * {@link GUIHelper#simpleLauncher} member of its parent class.
	 * 
	 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
	 *         Moores University, (c) 2019"
	 *
	 */
	private static class SimpleLaunch implements LaunchGUI {
		@Override
		public SimpleGamePanel launch(final MineMap map, final String title, final boolean scale) {
			return new SimpleGamePanel(map, title, scale);
		}
	}

	/**
	 * To achieve the simplest competitor launch, we can just instruct the
	 * {@link GUIHelper#launchCompetitor(Class, LaunchGUI, MineMap, int, int, boolean)
	 * method to instantiate {@link SimpleGamePanel} objects. To do so we can refer
	 * to this data member of {@link GUIHelper}.
	 */
	public static LaunchGUI simpleLauncher = new SimpleLaunch();

	/**
	 * Clones a map, instantiates an AI and configures its GUI, launches the AI's
	 * solver thread. If there are issues with instantiating the solver class, there
	 * could be all kinds of exceptions thrown.
	 * 
	 * @param comp      The class of the AI solver.
	 * @param mygui     The launcher for the GUI to be created.
	 * @param base      The base map to solve
	 * @param pushright how many windows should we go to the right
	 * @param pushdown  how many windows should we go towards the bottom
	 * @param scale     Do we want to allow the GUI to scale its grid size based on
	 *                  the minemap's column and row numbers?
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	static MineMap launchCompetitor(Class<? extends GameSolverThread> comp, LaunchGUI mygui, MineMap base,
			int pushright, int pushdown, boolean scale) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		GameSolverThread gst = comp.getConstructor().newInstance();
		MineMap myMap = gst instanceof HumanSolver ? new MineMap(base, 0) : new MineMap(base);
		gst.sendMap(myMap);
		SimpleGamePanel gui = mygui.launch(myMap, comp.getSimpleName() + " solving this window", scale);
		Point oldLoc = gui.getLocation();
		Dimension size = gui.getSize();
		gui.setLocation(oldLoc.x + size.width * pushright, oldLoc.y + size.height * pushdown);
		gst.sendGUI(gui);
		new Thread(gst).start();
		return myMap;
	}
}
