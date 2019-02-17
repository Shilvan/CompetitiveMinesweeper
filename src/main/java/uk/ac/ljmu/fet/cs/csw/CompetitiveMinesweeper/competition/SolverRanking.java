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

/**
 * Allows to pair a solver class with its score and offers a comparator on the
 * solver to allow easy sorting of solvers (i.e., to figure out who is the
 * best).
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class SolverRanking implements Comparable<SolverRanking> {
	/**
	 * The solver class that was ranked
	 */
	public final Class<? extends GameSolverThread> solver;
	/**
	 * The achieved score of the solver class in the competition it was taking part
	 */
	public final int score;

	/**
	 * Enables to fill in the two data members
	 * 
	 * @param solver the solver for which we record the score
	 * @param score  the score the solver achieved in the particular competition
	 */
	public SolverRanking(Class<? extends GameSolverThread> solver, int score) {
		this.solver = solver;
		this.score = score;
	}

	/**
	 * Enables sorting the solvers based on their achieved score. Note it reverses
	 * typical comparator results (i.e., if something has a bigger score it tells it
	 * has a smaller one). This makes sure sorting algorithms will list the best
	 * scoring solvers first.
	 */
	@Override
	public int compareTo(SolverRanking o) {
		return -((Integer) score).compareTo(o.score);
	}

	/**
	 * Simple text representation of the score and the solver. It is useful to print
	 * out the ranking without a hassle.
	 */
	@Override
	public String toString() {
		return solver.getName() + " score: " + score;
	}
}
