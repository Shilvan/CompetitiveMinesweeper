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
package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base;

import java.util.EnumSet;

/**
 * Provides the list of possible spots in the map.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public enum Spot {
	/**
	 * The spot is not yet explored
	 */
	UNEXPLORED,
	/**
	 * The spot is explored and it is safe to step on. {@link ExploredSpot}s offer
	 * how many mines are around if they are in this state.
	 */
	SAFE,
	/**
	 * The spot has a mine deployed underneath it. This is not used in explored
	 * maps. If we were to explore a mine the explored map will show an exploded
	 * state instead.
	 */
	MINE,
	/**
	 * The spot has a potential mine underneath it. The AI has flagged it as such.
	 * Note that this is only appearing in explored maps.
	 */
	FLAG,
	/**
	 * The spot had a mine on it and the AI asked to explore it, thus the AI has
	 * exploded.
	 */
	EXPLODED;
	/**
	 * The list of areas that we don't necessary know what is underneath them.
	 */
	public static final EnumSet<Spot> unknown = EnumSet.of(UNEXPLORED, FLAG);
}
