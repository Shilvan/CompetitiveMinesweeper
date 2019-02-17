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

/**
 * One should implement this interface to offer scores about two teamed
 * sets/matches.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public interface Scorer {
	/**
	 * Queries the results of the first team
	 * 
	 * @return an integer score (the bigger the score is better). Note the
	 *         particular range of values are to be determined by the particular
	 *         implementation.
	 */
	public int getPointsForTeamOne();

	/**
	 * Queries the results of the second team
	 * 
	 * @return an integer score (the bigger the score is better). Note the
	 *         particular range of values are to be determined by the particular
	 *         implementation.
	 */
	public int getPointsForTeamTwo();

}
