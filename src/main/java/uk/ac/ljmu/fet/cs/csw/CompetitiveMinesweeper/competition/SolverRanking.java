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

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

public class SolverRanking implements Comparable<SolverRanking> {
	public final Class<? extends GameSolverThread> solver;
	public final int score;

	public SolverRanking(Class<? extends GameSolverThread> solver, int score) {
		this.solver = solver;
		this.score = score;
	}

	@Override
	public int compareTo(SolverRanking o) {
		return -((Integer) score).compareTo(o.score);
	}

	@Override
	public String toString() {
		return solver.getName() + " score: " + score;
	}
}
