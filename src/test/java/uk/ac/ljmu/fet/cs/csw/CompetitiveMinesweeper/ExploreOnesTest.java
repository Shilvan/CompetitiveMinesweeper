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
package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.ExploredSpot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap.MapCopyException;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.Spot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.onepriority.CoordinatesForSpot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.onepriority.ExploreOnes;

/**
 * Example test cases for the helper functions of the {@link ExploreOnes} class.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class ExploreOnesTest {
	private static Random rnd = new Random();

	@Test(timeout = 150)
	public void ifOneIsExploredItMustbeOnList() {
		boolean needRedo = false;
		int magicCols = -1, magicRows = -1;
		do {
			MineMap theMap = new MineMap(5, 5, .03, 0);
			outerLoop: for (int cc = 0; cc < theMap.cols; cc++) {
				for (int rc = 0; rc < theMap.rows; rc++) {
					theMap.pickASpot(rc, cc);
					ExploredSpot justExplored = theMap.getPos(rc, cc);
					if (Spot.EXPLODED.equals(justExplored.type)) {
						needRedo = true;
						break outerLoop;
					}
					if (Spot.SAFE.equals(justExplored.type)) {
						if (justExplored.nearMineCount == 1) {
							magicCols = cc;
							magicRows = rc;
							break outerLoop;
						}
					}
				}
			}
			if (!needRedo) {
				List<CoordinatesForSpot> allOnes = ExploreOnes.searchForOnes(theMap);
				for (CoordinatesForSpot oneCoord : allOnes) {
					if (oneCoord.colCoord == magicCols && oneCoord.rowCoord == magicRows) {
						return;
					}
				}
			}
		} while (needRedo);
		fail("We managed to explore a spot with one on it which the searchForOnes method have not found");
	}

	@Test(timeout = 150)
	public void shouldNotListAnythingIfThereisNoOnesExplored() {
		MineMap theMap = new MineMap(5, 5, .03, 0);
		List<CoordinatesForSpot> allOnes = ExploreOnes.searchForOnes(theMap);
		assertEquals("A fresh map should never have explored spots with ones on it", 0, allOnes.size());
		theMap.flagASpot(rnd.nextInt(theMap.rows), rnd.nextInt(theMap.cols));
		allOnes = ExploreOnes.searchForOnes(theMap);
		assertEquals("A map with only flags on it should never have explored spots with ones on it", 0, allOnes.size());
		boolean needRedo;
		do {
			needRedo = false;
			theMap = new MineMap(5, 5, .12, 0);
			int luckyHits = 0;
			outerLoop: for (int cc = 0; cc < theMap.cols; cc++) {
				for (int rc = 0; rc < theMap.rows; rc++) {
					theMap.pickASpot(rc, cc);
					ExploredSpot justExplored = theMap.getPos(rc, cc);
					if (Spot.EXPLODED.equals(justExplored.type)) {
						needRedo = true;
						break outerLoop;
					} else {
						if (justExplored.nearMineCount < 2) {
							needRedo = true;
							break outerLoop;
						} else {
							luckyHits++;
							if (luckyHits > 1) {
								break outerLoop;
							}
						}
					}
				}
			}
			if (!needRedo) {
				allOnes = ExploreOnes.searchForOnes(theMap);
				assertEquals("A map with some explored but non-one related areas should not produce any spotlist", 0,
						allOnes.size());
			}
		} while (needRedo);
	}

	@Test(timeout = 150)
	public void shouldBeAbleToTellFlagInNeighbourhood() {
		for (int i = 0; i < 50; i++) {
			MineMap theMap = new MineMap(5, 5, .03, 0);
			int centreRow = rnd.nextInt(theMap.rows), centreCol = rnd.nextInt(theMap.cols);
			CoordinatesForSpot centre = new CoordinatesForSpot(centreRow, centreCol);
			ArrayList<CoordinatesForSpot> coordsToFlag = new ArrayList<>();
			for (int dr = centreRow - 1; dr < centreRow + 2; dr++) {
				for (int dc = centreCol - 1; dc < centreCol + 2; dc++) {
					if (theMap.checkOutOfRange(dr, dc)) {
						continue;
					}
					coordsToFlag.add(new CoordinatesForSpot(dr, dc));
				}
			}
			Collections.shuffle(coordsToFlag);
			int maxFlagCount = rnd.nextInt(coordsToFlag.size());
			for (int j = 0; j < maxFlagCount; j++) {
				CoordinatesForSpot coord = coordsToFlag.get(j);
				theMap.flagASpot(coord.rowCoord, coord.colCoord);
			}
			if (maxFlagCount == 0) {
				assertFalse("Should not have found flags around the spot",
						ExploreOnes.areThereAnyFlagsAround(theMap, centre));
			} else {
				assertTrue("Should have found flags around the spot",
						ExploreOnes.areThereAnyFlagsAround(theMap, centre));
			}
		}
	}

	@Test(timeout = 150)
	public void shouldNotIndicateAFlagIfNoneInNeighbourhood() {
		MineMap theMap = new MineMap(5, 5, .03, 0);
		theMap.flagASpot(4, 4);
		for (int dr = 0; dr < theMap.rows; dr++) {
			for (int dc = 0; dc < theMap.cols; dc++) {
				if (dr < 3 || dc < 3) {
					assertFalse("Should not have found flags when there is none around",
							ExploreOnes.areThereAnyFlagsAround(theMap, new CoordinatesForSpot(dr, dc)));
				}
			}
		}
	}

	@Test(timeout = 150)
	public void ensureFullListPick() {
		for (int reps = 0; reps < 10; reps++) {
			MineMap theMap;
			do {
				theMap = new MineMap(5, 5, .03, 0);
				int maxPickCount = rnd.nextInt(theMap.fieldSize);
				ArrayList<CoordinatesForSpot> coordsToPick = new ArrayList<>(maxPickCount);
				for (int i = 0; i < maxPickCount; i++) {
					coordsToPick.add(new CoordinatesForSpot(rnd.nextInt(theMap.rows), rnd.nextInt(theMap.cols)));
				}
				ExploreOnes.pickAllOnList(theMap, coordsToPick);
				if (!theMap.isEnded()) {
					for (CoordinatesForSpot coord : coordsToPick) {
						assertEquals(
								"This spot (" + coord.rowCoord + "," + coord.colCoord + ") should have been picked",
								Spot.SAFE, theMap.getPos(coord.rowCoord, coord.colCoord).type);
					}
				}
			} while (theMap.isEnded());
		}
	}

	@Test(timeout = 150)
	public void ensureOneIsPicked() throws MapCopyException {
		for (int reps = 0; reps < 10; reps++) {
			MineMap theMap;
			theMap = new MineMap(5, 5, .2, 0);
			MineMap theAltMap = new MineMap(theMap);
			int maxPickCount = rnd.nextInt(theMap.fieldSize);
			if (maxPickCount == 0) {
				reps--;
				continue;
			}
			ArrayList<CoordinatesForSpot> coordsToPick = new ArrayList<>(maxPickCount);
			for (int i = 0; i < maxPickCount; i++) {
				coordsToPick.add(new CoordinatesForSpot(rnd.nextInt(theMap.rows), rnd.nextInt(theMap.cols)));
			}
			ExploreOnes.pickARandomSpotFromList(theMap, coordsToPick);
			// Here we guaranteeing that we only pick a single spot, but this should have
			// the same effect on a clone map as it has on the original. If it is not the
			// same we know that the original was explored further than this.
			theAltMap.pickASpot(theMap.getLastRow(), theMap.getLastCol());
			for (int cc = 0; cc < theMap.cols; cc++) {
				for (int rc = 0; rc < theMap.rows; rc++) {
					assertEquals(
							"Should only pick one spot, i.e., should explore the same area as the one we would get when picking the last picked spot",
							theAltMap.getPos(rc, cc).type, theMap.getPos(rc, cc).type);
				}
			}
		}
	}

	@Test(timeout = 150)
	public void ensureNoUnexploredIsUndiscovered() throws MapCopyException {
		for (int trials = 0; trials < 10; trials++) {
			MineMap theMap;
			do {
				theMap = new MineMap(10, 10, .1, 0);
				for (int i = 0; i < 10; i++) {
					theMap.pickASpot(rnd.nextInt(theMap.rows), rnd.nextInt(theMap.cols));
				}
				if (!theMap.isEnded()) {
					List<CoordinatesForSpot> coords = ExploreOnes.searchForUnexplored(theMap);
					nextRandomSpot: for (int i = 0; i < 50; i++) {
						int rc = rnd.nextInt(theMap.rows);
						int cc = rnd.nextInt(theMap.cols);
						ExploredSpot es = theMap.getPos(rc, cc);
						if (Spot.UNEXPLORED.equals(es.type)) {
							for (CoordinatesForSpot coord : coords) {
								if (coord.rowCoord == rc && coord.colCoord == cc) {
									continue nextRandomSpot;
								}
							}
							fail("Should have found unexplored spot (" + rc + "," + cc + ") but it did not");
						} else {
							for (CoordinatesForSpot coord : coords) {
								if (coord.rowCoord == rc && coord.colCoord == cc) {
									fail("Listed an already explored spot as unexplored (" + rc + "," + cc + ")");
								}
							}
						}
					}
				}
			} while (theMap.isEnded());
		}
	}

	@Test(timeout = 150)
	public void testFocusedUnexploredSearch() throws MapCopyException {
		for (int trials = 0; trials < 10; trials++) {
			MineMap theMap;
			do {
				theMap = new MineMap(10, 10, .1, 0);
				for (int i = 0; i < 10; i++) {
					theMap.pickASpot(rnd.nextInt(theMap.rows), rnd.nextInt(theMap.cols));
				}
				if (!theMap.isEnded()) {
					nextRandomSpot: for (int i = 0; i < 50; i++) {
						int rc = rnd.nextInt(theMap.rows);
						int cc = rnd.nextInt(theMap.cols);
						ExploredSpot es = theMap.getPos(rc, cc);
						if (!Spot.UNEXPLORED.equals(es.type)) {
							i--;
							continue;
						}
						rc--;
						cc--;
						int adjr, adjc;
						do {
							adjr = rnd.nextInt(3);
							adjc = rnd.nextInt(3);
						} while (theMap.checkOutOfRange(rc + adjr, cc + adjc));
						List<CoordinatesForSpot> theList = new ArrayList<CoordinatesForSpot>();
						theList.add(new CoordinatesForSpot(rc + adjr, cc + adjc));
						List<List<CoordinatesForSpot>> retList = ExploreOnes.searchUnexploredAround(theMap, theList);
						rc++;
						cc++;
						assertEquals(
								"We only asked to look around one spot, we only need to receive a single list back", 1,
								retList.size());
						for (CoordinatesForSpot coord : retList.get(0)) {
							if (coord.rowCoord == rc && coord.colCoord == cc) {
								continue nextRandomSpot;
							}
						}
						fail("Should have listed the unexplored spot we have started from");
					}
				}
			} while (theMap.isEnded());
		}
	}
}
