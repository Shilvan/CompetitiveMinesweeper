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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.ExploredSpot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.Spot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.AbstractSolver;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.SimpleLineByLineSolver;

/**
 * Sample solution to the AI. Although it is more complex than
 * {@link SimpleLineByLineSolver}, it is still very simple. It shows the basic
 * mechanics of exploring the map and deciding what to pick or flag. The logic
 * is very simple: it randomly explores the map but prefers exploring those
 * areas first which are in the vicinity of an already explored spot with one
 * mine in its neighbourhood. This "AI" can often times win the easiest games,
 * but loses if there are mines which don't have a spot around them with only
 * one mine around.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class ExploreOnes extends AbstractSolver {

	/**
	 * Implements the following technique: the algorithm randomly explores the map
	 * but prefers exploring those areas first which are in the vicinity of an
	 * already explored spot with one mine in its neighbourhood.
	 */
	@Override
	public void run() {
		// two boilerplate lines that is a strong requirement for almost all solvers
		super.run();
		MineMap theMapToSolve = getMyMap();

		// The actual algorithm starts here
		mainloop: do {
			List<CoordinatesForSpot> allOnes = searchForOnes(theMapToSolve);
			if (allOnes.isEmpty()) {
				// We have to go for a random choice, nothing is explored so far
				doFullAreaRandomPick(theMapToSolve);
			} else {
				// Randomise the spots with ones so we don't explore the map in a predefined
				// order
				Collections.shuffle(allOnes);
				List<List<CoordinatesForSpot>> unexploredLocationsAroundOnes = searchUnexploredAround(theMapToSolve,
						allOnes);
				int biggestSize = -1;
				List<CoordinatesForSpot> neighbourhoodToPickFrom = null;
				for (int i = 0; i < unexploredLocationsAroundOnes.size(); i++) {
					List<CoordinatesForSpot> aNeighbourhood = unexploredLocationsAroundOnes.get(i);
					CoordinatesForSpot neighbourhoodCentre = allOnes.get(i);
					if (areThereAnyFlagsAround(theMapToSolve, neighbourhoodCentre)) {
						// We can pick all the spots in the neighbourhood of a 1 if we already know
						// where is its mine (i.e., we have a flag on it)
						pickAllOnList(theMapToSolve, aNeighbourhood);
					} else {
						// We are not so lucky, we have no flags around, we have to look deeper
						if (aNeighbourhood.size() == 1) {
							// There is just one unexplored neighbouring spot, that must be a mine then,
							// let's flag it
							CoordinatesForSpot theMine = aNeighbourhood.get(0);
							theMapToSolve.flagASpot(theMine.rowCoord, theMine.colCoord);
							// As we now have an extra flag on the map, we are better of restarting the
							// loop.
							continue mainloop;
						} else {
							// We have multiple unexplored neighbours, we need to look for the biggest
							// unexplored area so we can pick a bit safer from them.
							if (biggestSize < aNeighbourhood.size()) {
								biggestSize = aNeighbourhood.size();
								neighbourhoodToPickFrom = aNeighbourhood;
							}
						}
					}
				}
				if (biggestSize > 1) {
					// We had found a spot with one on it where there are multiple unexplored
					// neighbours, we need to pick one of them.
					pickARandomSpotFromList(theMapToSolve, neighbourhoodToPickFrom);
				} else {
					// There are no areas to explore around the ones, we have to guess the next spot
					doFullAreaRandomPick(theMapToSolve);
				}
			}
		} while (!theMapToSolve.isEnded());
	}

	/**
	 * Searches the map for unexplored spots and picks one randomly.
	 * 
	 * @param theMapToSolve the map to explore and pick on
	 */
	public static void doFullAreaRandomPick(MineMap theMapToSolve) {
		List<CoordinatesForSpot> allUnexplored = searchForUnexplored(theMapToSolve);
		pickARandomSpotFromList(theMapToSolve, allUnexplored);
	}

	/**
	 * Explores the complete map for unexplored spots.
	 * 
	 * @param theMapToSolve The map to explore
	 * @return The list of the coordinates of the unexplored spots on the complete
	 *         map.
	 */
	public static List<CoordinatesForSpot> searchForUnexplored(MineMap theMapToSolve) {
		List<CoordinatesForSpot> fullListOfUnexplored = new ArrayList<CoordinatesForSpot>();
		for (int cc = 0; cc < theMapToSolve.cols; cc++) {
			for (int rc = 0; rc < theMapToSolve.rows; rc++) {
				ExploredSpot aSpot = theMapToSolve.getPos(rc, cc);
				if (Spot.UNEXPLORED.equals(aSpot.type)) {
					fullListOfUnexplored.add(new CoordinatesForSpot(rc, cc));
				}
			}
		}
		return fullListOfUnexplored;
	}

	/**
	 * Explores the area around a set of coordinates on the map and gives the list
	 * of unexplored items around.
	 * 
	 * @param theMapToSolve                       The map where the exploration
	 *                                            should happen.
	 * @param theLocationsWhereWeNeedToLookAround The list of spots (specified as
	 *                                            coordinates on the map) for which
	 *                                            the neighbourhood unexplored spots
	 *                                            should be discovered.
	 * @return The method generates a two dimensional dynamic array. In which each
	 *         row will contain a list of spots (depicted with its coordinates)
	 *         which can be explored around a given original coordinate. For
	 *         example, the first row will hold the coordinates of unexplored spots
	 *         around the spot given in the first item of the
	 *         <i>theLocationsWhereWeNeedToLookAround</i> list.
	 */
	public static List<List<CoordinatesForSpot>> searchUnexploredAround(MineMap theMapToSolve,
			List<CoordinatesForSpot> theLocationsWhereWeNeedToLookAround) {
		ArrayList<List<CoordinatesForSpot>> theFullList = new ArrayList<>();
		for (CoordinatesForSpot spotToLookAround : theLocationsWhereWeNeedToLookAround) {
			ArrayList<CoordinatesForSpot> aParticularSpotsList = new ArrayList<>();
			for (int cc = spotToLookAround.colCoord - 1; cc < spotToLookAround.colCoord + 2; cc++) {
				for (int rc = spotToLookAround.rowCoord - 1; rc < spotToLookAround.rowCoord + 2; rc++) {
					if (!theMapToSolve.checkOutOfRange(rc, cc)) {
						ExploredSpot aSpot = theMapToSolve.getPos(rc, cc);
						if (Spot.UNEXPLORED.equals(aSpot.type)) {
							aParticularSpotsList.add(new CoordinatesForSpot(rc, cc));
						}
					}
				}
			}
			theFullList.add(aParticularSpotsList);
		}
		return theFullList;
	}

	/**
	 * Randomly chooses a spot from the list of areas and picks it on the map that
	 * it receives.
	 * 
	 * @param theMapToSolve            The map on which we have to pick a spot.
	 * @param theListofAreasToPickFrom The list of areas from which the method can
	 *                                 choose from when it decides which to pick.
	 */
	public static void pickARandomSpotFromList(MineMap theMapToSolve,
			List<CoordinatesForSpot> theListofAreasToPickFrom) {
		if (theListofAreasToPickFrom.size() == 0)
			return;
		Collections.shuffle(theListofAreasToPickFrom);
		CoordinatesForSpot whatToPick = theListofAreasToPickFrom.get(0);
		theMapToSolve.pickASpot(whatToPick.rowCoord, whatToPick.colCoord);
	}

	/**
	 * Picks all spots handed over as a list.
	 * 
	 * @param theMapToSolve            The map where we have to pick the spots.
	 * @param theListofAreasToPickFrom The list of spots to pick. WARNING: all spots
	 *                                 will be picked. Incorrect use of this method
	 *                                 could lead to mines exploding.
	 */
	public static void pickAllOnList(MineMap theMapToSolve, List<CoordinatesForSpot> theListofAreasToPickFrom) {
		for (CoordinatesForSpot whatToPick : theListofAreasToPickFrom) {
			theMapToSolve.pickASpot(whatToPick.rowCoord, whatToPick.colCoord);
		}
	}

	/**
	 * Determines if the explored map around a particular spot contains any flags
	 * (i.e., suspected mines).
	 * 
	 * @param theMapToSolve       The map where to look around.
	 * @param neighbourhoodCentre The spot for which we need to check its
	 *                            neighbourhood.
	 * @return true if we found at least a flag in the vicinity. false otherwise.
	 */
	public static boolean areThereAnyFlagsAround(MineMap theMapToSolve, CoordinatesForSpot neighbourhoodCentre) {
		for (int cc = neighbourhoodCentre.colCoord - 1; cc < neighbourhoodCentre.colCoord + 2; cc++) {
			for (int rc = neighbourhoodCentre.rowCoord - 1; rc < neighbourhoodCentre.rowCoord + 2; rc++) {
				if (!theMapToSolve.checkOutOfRange(rc, cc)) {
					ExploredSpot aSpot = theMapToSolve.getPos(rc, cc);
					if (Spot.FLAG.equals(aSpot.type)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Lists all the coordinates on the map which has one mine in the vicinity. This
	 * method returns all ones independently from the surrounding area's state
	 * (i.e., even if it is already explored, it still returns the location).
	 * 
	 * @param aMap The map where we look for the spots with one mine in their
	 *             vicinity.
	 * @return The list of spots (depicted with their coordinates) that have exactly
	 *         one mine in their vicinity.
	 */
	public static List<CoordinatesForSpot> searchForOnes(MineMap aMap) {
		List<CoordinatesForSpot> fullListOfOnes = new ArrayList<CoordinatesForSpot>();
		for (int cc = 0; cc < aMap.cols; cc++) {
			for (int rc = 0; rc < aMap.rows; rc++) {
				ExploredSpot aSpot = aMap.getPos(rc, cc);
				if (Spot.SAFE.equals(aSpot.type)) {
					if (aSpot.nearMineCount == 1) {
						fullListOfOnes.add(new CoordinatesForSpot(rc, cc));
					}
				}
			}
		}
		return fullListOfOnes;
	}
}
