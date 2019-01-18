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

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */

public class FullScreenSolveShow {
	public static void main(String[] args) throws Exception {
		Class<? extends GameSolverThread> toBeTiledSolver = (Class<? extends GameSolverThread>) Class.forName(args[0]);
		for (int i = 0; i < Integer.parseInt(args[1]); i++) {
			for (int j = 0; j < Integer.parseInt(args[2]); j++) {

			}
		}
	}
}
