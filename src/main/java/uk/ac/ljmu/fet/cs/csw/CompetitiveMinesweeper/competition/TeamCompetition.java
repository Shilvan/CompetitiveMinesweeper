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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * This class allows to organise a competition between various game solvers
 * where each solver plays a set of games with everyone else in the same group.
 * After all sets are played one can collect the results from the class in a
 * ranked order. <br/>
 * Typical calling order:
 * <ol>
 * <li>constructor</li>
 * <li>{@link #addToCompetitors(Class)} several times (at least 3x)</li>
 * <li>{@link #arrangeSets()} to randomly generate the sets to be played against
 * other competitors</li>
 * <li>{@link #runSets()} to run the actual competition between all participants
 * according to the previous arrangements</li>
 * <li>{@link #getRankedList()} to figure out the ranking of the
 * competition</li>
 * </ol>
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class TeamCompetition {
	/**
	 * False if competitions could be arbitrary sized.
	 */
	private final boolean applyCompetitorLimit;
	/**
	 * The competitors that were assigned to this team
	 */
	private final ArrayList<Class<? extends GameSolverThread>> competitors = new ArrayList<Class<? extends GameSolverThread>>();
	/**
	 * The sets that were arranged by the {@link #arrangeSets()} method.
	 */
	private final ArrayList<SingleSet> sets = new ArrayList<SingleSet>();
	/**
	 * The results that were collected based on the sets.
	 */
	private final HashMap<Class<? extends GameSolverThread>, Integer> resultsTable = new HashMap<Class<? extends GameSolverThread>, Integer>();

	/**
	 * Prepares the competition
	 * 
	 * @param applyCompetitorLimit true if there should be no more than 4
	 *                             competitors in a team competition
	 */
	public TeamCompetition(boolean applyCompetitorLimit) {
		this.applyCompetitorLimit = applyCompetitorLimit;
	}

	/**
	 * Allows new competitors to be added to a not yet arranged team competition
	 * 
	 * @param toAdd the class of the competitor
	 * @throws RuntimeException if competitor limit is reached or if the team
	 *                          competition is already arranged
	 */
	public void addToCompetitors(Class<? extends GameSolverThread> toAdd) {
		if (!sets.isEmpty()) {
			throw new RuntimeException("Tried to add a competitor to a team with already arranged sets");
		}
		competitors.add(toAdd);
		if (applyCompetitorLimit && competitors.size() > 4) {
			throw new RuntimeException("Could not allow more than 4 members in a group!");
		}
	}

	/**
	 * Allows us to query the number of competitors taking part in this team
	 * competition.
	 * 
	 * @return
	 */
	public int getSize() {
		return competitors.size();
	}

	/**
	 * Prepares the competition so each competitor is playing a set with the other
	 * participants in the team.
	 */
	public void arrangeSets() {
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

	/**
	 * Runs all arranged sets in a sequential order then accumulates the score for
	 * each team.
	 * 
	 * @throws InterruptedException      see {@link SingleMatch#runMatch()}
	 * @throws SecurityException         see {@link SingleMatch#runMatch()}
	 * @throws NoSuchMethodException     see {@link SingleMatch#runMatch()}
	 * @throws InvocationTargetException see {@link SingleMatch#runMatch()}
	 * @throws IllegalArgumentException  see {@link SingleMatch#runMatch()}
	 * @throws IllegalAccessException    see {@link SingleMatch#runMatch()}
	 * @throws InstantiationException    see {@link SingleMatch#runMatch()}
	 * @throws RuntimeException          If sets were not arranged yet.
	 */
	public void runSets() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException {
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

	/**
	 * Once all sets were executed we can query the ranking of each participant with
	 * this method.
	 * 
	 * @return Returns an ordered list of the participants (the best will be the
	 *         first in the list)
	 */
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
