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

public class ExploreOnes extends AbstractSolver {
	@Override
	public void run() {
		super.run();
		MineMap theMapToSolve = getMyMap();

		mainloop: do {
			// Here should come our algorithm
			List<CoordinatesForSpot> allOnes = searchForOnes(theMapToSolve);
			if (allOnes.isEmpty()) {
				// We have to go for random
				doFullAreaRandomPick(theMapToSolve);
			} else {
				// We have some choice
				Collections.shuffle(allOnes);
				List<List<CoordinatesForSpot>> unexploredLocationsAroundOnes = searchUnexploredAround(theMapToSolve,
						allOnes);
				int biggestSize = -1;
				List<CoordinatesForSpot> neighbourhoodToPickFrom = null;
				for (int i = 0; i < unexploredLocationsAroundOnes.size(); i++) {
					List<CoordinatesForSpot> aNeighbourhood = unexploredLocationsAroundOnes.get(i);
					CoordinatesForSpot neighbourhoodCentre = allOnes.get(i);
					if (areThereAnyFlagsAround(theMapToSolve, neighbourhoodCentre)) {
						pickAllOnList(theMapToSolve, aNeighbourhood);
					} else {
						if (aNeighbourhood.size() == 1) {
							CoordinatesForSpot theMine = aNeighbourhood.get(0);
							theMapToSolve.flagASpot(theMine.rowCoord, theMine.colCoord);
							continue mainloop;
						} else {
							if (biggestSize < aNeighbourhood.size()) {
								biggestSize = aNeighbourhood.size();
								neighbourhoodToPickFrom = aNeighbourhood;
							}
						}
					}
				}
				if (biggestSize > 1) {
					pickARandomSpotFromList(theMapToSolve, neighbourhoodToPickFrom);
				} else {
					doFullAreaRandomPick(theMapToSolve);
				}
			}
		} while (!theMapToSolve.isEnded());
	}

	public static void doFullAreaRandomPick(MineMap theMapToSolve) {
		List<CoordinatesForSpot> allUnexplored = searchForUnexplored(theMapToSolve);
		pickARandomSpotFromList(theMapToSolve, allUnexplored);
	}

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

	public static void pickARandomSpotFromList(MineMap theMapToSolve,
			List<CoordinatesForSpot> theListofAreasToPickFrom) {
		if (theListofAreasToPickFrom.size() == 0)
			return;
		Collections.shuffle(theListofAreasToPickFrom);
		CoordinatesForSpot whatToPick = theListofAreasToPickFrom.get(0);
		theMapToSolve.pickASpot(whatToPick.rowCoord, whatToPick.colCoord);
	}

	public static void pickAllOnList(MineMap theMapToSolve, List<CoordinatesForSpot> theListofAreasToPickFrom) {
		for (CoordinatesForSpot whatToPick : theListofAreasToPickFrom) {
			theMapToSolve.pickASpot(whatToPick.rowCoord, whatToPick.colCoord);
		}
	}

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
