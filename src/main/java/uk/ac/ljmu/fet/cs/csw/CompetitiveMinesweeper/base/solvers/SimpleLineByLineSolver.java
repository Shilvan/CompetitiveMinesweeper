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

/**
 * Sample solution to the AI. This is intentionally kept very simple and dumb.
 * It shows how to pick every spot in the grid. This "AI" will always lose the
 * game even with a single mine on the map as it will guaranteed to pick that
 * mine once it reaches its corresponding grid position.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class SimpleLineByLineSolver extends AbstractSolver {
	/**
	 * The solving algorithm. This one just picks the spots next to each other until
	 * it reaches the end of the line. If so, it will go to the next line. It will
	 * do so until it hits a mine. Then it terminates the solving immediately.
	 */
	@Override
	public void run() {
		// Makes sure the generic run is done from Abstract solver so we don't have to
		// handle the basic errors.
		super.run();
		MineMap myMap = getMyMap();
		// Our main logic, this never really works unless there are no mines on the
		// map... But this demonstrates how to scan through the entire grid. Note that
		// this
		for (int cc = 0; cc < myMap.cols; cc++) {
			for (int rc = 0; rc < myMap.rows; rc++) {
				// There is no need to try to pick further spots if the game has already ended.
				// Note that it would be just as fine to pick the spots further as the pickASpot
				// method would not really do any further operations once the game ended anyway.
				if (myMap.isEnded())
					return;
				myMap.pickASpot(rc, cc);
			}
		}
	}

}
