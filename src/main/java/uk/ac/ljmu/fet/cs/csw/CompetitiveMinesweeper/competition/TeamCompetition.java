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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

public class TeamCompetition {
	private final boolean applyCompetitorLimit;
	private final ArrayList<Class<? extends GameSolverThread>> competitors = new ArrayList<Class<? extends GameSolverThread>>();
	private final ArrayList<SingleSet> sets = new ArrayList<SingleSet>();
	private final HashMap<Class<? extends GameSolverThread>, Integer> resultsTable = new HashMap<Class<? extends GameSolverThread>, Integer>();

	public TeamCompetition(boolean applyCompetitorLimit) {
		this.applyCompetitorLimit = applyCompetitorLimit;
	}

	public void addToCompetitors(Class<? extends GameSolverThread> toAdd) {
		competitors.add(toAdd);
		if (applyCompetitorLimit && competitors.size() > 4) {
			throw new RuntimeException("Could not allow more than 4 members in a group!");
		}
	}

	public int getSize() {
		return competitors.size();
	}

	public void arrangeSets() throws Exception {
		if (competitors.size() < 3) {
			throw new RuntimeException("No group is allowed to have less than 3 members!");
		}
		Collections.shuffle(competitors);
		for (int i = 0; i < competitors.size() - 1; i++) {
			for (int j = i + 1; j < competitors.size(); j++) {
				sets.add(new SingleSet(competitors.get(i), competitors.get(j)));
			}
		}
	}

	public void runSets() throws Exception {
		if (sets.size() > 0) {
			for (SingleSet currSet : sets) {
				// Run the current matches
				currSet.runSet();

				// Accumulate the scores:
				Integer currValue = resultsTable.get(currSet.solverOne);
				if (currValue == null) {
					currValue = Integer.valueOf(0);
				}
				currValue += currSet.getPointsForTeamOne();
				resultsTable.put(currSet.solverOne, currValue);

				currValue = resultsTable.get(currSet.solverTwo);
				if (currValue == null) {
					currValue = Integer.valueOf(0);
				}
				currValue += currSet.getPointsForTeamTwo();
				resultsTable.put(currSet.solverTwo, currValue);
			}
		} else {
			throw new RuntimeException("Should arrange the sets first!");
		}
	}

	public ArrayList<SolverRanking> getRankedList() {
		if (resultsTable.isEmpty()) {
			throw new RuntimeException("Should run the sets first!");
		} else {
			ArrayList<SolverRanking> ranking = new ArrayList<SolverRanking>();
			for (Class<? extends GameSolverThread> solver : competitors) {
				ranking.add(new SolverRanking(solver, resultsTable.get(solver)));
			}
			Collections.sort(ranking);
			return ranking;
		}
	}

}
