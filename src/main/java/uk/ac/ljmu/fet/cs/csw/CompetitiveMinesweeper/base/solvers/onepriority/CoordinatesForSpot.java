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
package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.onepriority;

/**
 * Allows to store the two coordinates of a spot on a minemap. Objects of this
 * class keep their data constant to allow rapid access to them.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */

public class CoordinatesForSpot {
	/**
	 * The coordinates to remember by the instances of this class are stored here
	 */
	public final int rowCoord, colCoord;

	/**
	 * Allows pairing up two coordinates together in an easyly accessible fashion.
	 * 
	 * @param rowC The row coordinate to remember.
	 * @param colC The column coordiante to remember.
	 */
	public CoordinatesForSpot(final int rowC, final int colC) {
		rowCoord = rowC;
		colCoord = colC;
	}
}
