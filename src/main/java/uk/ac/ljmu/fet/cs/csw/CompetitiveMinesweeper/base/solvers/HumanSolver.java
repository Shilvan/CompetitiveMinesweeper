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

import java.util.List;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui.FlashableJLabel;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui.SingleGamePanel;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui.SpotEventHandler;

/**
 * A sample "solver" which does not solve anything instead it allows a human to
 * interact with the GUI. Utilises {@link SpotEventHandler} to handle
 * interactions with individual cells in the grid.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class HumanSolver extends AbstractSolver {

	/**
	 * Receives the GUI that needs new event handlers to support more interaction
	 * with the map.
	 */
	@Override
	public void sendGUI(final SingleGamePanel myVisualiser) {
		super.sendGUI(myVisualiser);
		// Adds a mouse listener for each grid item to handle mouse clicks and hover
		// overs
		for (int rc = 0; rc < myVisualiser.field.size(); rc++) {
			List<FlashableJLabel> currRow = myVisualiser.field.get(rc);
			for (int cc = 0; cc < currRow.size(); cc++) {
				FlashableJLabel xyInGUIGrid = currRow.get(cc);
				SpotEventHandler spotLevelInteractions = new SpotEventHandler(getMyMap(), xyInGUIGrid, rc, cc);
				xyInGUIGrid.addMouseListener(spotLevelInteractions);
			}
		}
	}

}
