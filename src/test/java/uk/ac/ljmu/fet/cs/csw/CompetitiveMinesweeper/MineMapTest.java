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

import java.util.Random;

import org.junit.Test;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap.MapCopyException;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.Spot;

public class MineMapTest {
	public static final Random r = new Random();
	private int cols, rows;
	private double mineRatio = .05;
	private int cc, rc;

	private void genMineDetails() {
		cols = r.nextInt(15) + 5;
		rows = r.nextInt(15) + 5;
		mineRatio = .05;
	}

	private MineMap genDefaultMap() {
		genMineDetails();
		return new MineMap(rows, cols, mineRatio, 0);
	}

	private int genRow() {
		return r.nextInt(rows);
	}

	private int genCol() {
		return r.nextInt(cols);
	}

	private void genNewCoords() {
		rc = genRow();
		cc = genCol();
	}

	@Test(timeout = 50)
	public void shouldReportTheCorrectDimensions() {
		final MineMap mm = genDefaultMap();
		assertEquals("Number of rows are not retained correctly", rows, mm.rows);
		assertEquals("Number of columns are not retained correctly", cols, mm.cols);
		assertEquals("Field size is not reported correctly", cols * rows, mm.fieldSize);
		assertEquals("Mine count is not reported correctly", ((double) cols * rows) * mineRatio, mm.mineCount, 1);
	}

	@Test(timeout = 500)
	public void shouldObserveUIDelay() {
		final int uiLongDel = 120, delRatio = 2, uiShortDel = uiLongDel / delRatio, acceptablePickTime = 10;
		genMineDetails();
		final MineMap mmdelayed = new MineMap(rows, cols, mineRatio, uiLongDel);
		long before = System.currentTimeMillis();
		mmdelayed.flagASpot(1, 1);
		long flagDuration = System.currentTimeMillis() - before;
		assertTrue("Placing a flag should incur a uiDelay", flagDuration >= uiLongDel);

		before = System.currentTimeMillis();
		mmdelayed.pickASpot(2, 2);
		long pickDuration = System.currentTimeMillis() - before;
		assertTrue("Picking a spot should incur a uiDelay", pickDuration >= uiLongDel);

		final MineMap mmnodelay = new MineMap(rows, cols, mineRatio, 0);
		before = System.currentTimeMillis();
		mmnodelay.flagASpot(1, 1);
		long flagNoDDuration = System.currentTimeMillis() - before;
		long flagDiff = flagDuration - flagNoDDuration;
		assertTrue("Placing a flag should not incur any uiDelay: " + flagDiff,
				Math.abs(flagDiff - uiLongDel) <= acceptablePickTime);

		mmnodelay.pickASpot(2, 2);
		long pickNoDDuration = System.currentTimeMillis() - before;
		long pickDiff = pickDuration - pickNoDDuration;
		assertTrue("Picking a spot should not incur any uiDelay: " + pickDiff,
				Math.abs(pickDiff - uiLongDel) <= acceptablePickTime);

		final MineMap mmsdelayed = new MineMap(rows, cols, mineRatio, uiShortDel);
		before = System.currentTimeMillis();
		mmsdelayed.flagASpot(1, 1);
		long flagSDuration = System.currentTimeMillis() - before;

		assertTrue("Placing a flag should incur a uiDelay", flagSDuration >= uiShortDel);
		before = System.currentTimeMillis();
		mmsdelayed.pickASpot(2, 2);
		long pickSDuration = System.currentTimeMillis() - before;
		assertTrue("Picking a spot should incur a uiDelay", pickSDuration >= uiShortDel);

		assertEquals("Short delay's duration should be half of the long one", delRatio,
				(int) Math.round((0.0 + pickDuration + flagDuration) / (pickSDuration + flagSDuration)));
	}

	@Test(timeout = 50)
	public void shouldEndGameIfMineIsHit() {
		final MineMap mm = genDefaultMap();
		do {
			genNewCoords();
			mm.pickASpot(rc, cc);
		} while (!Spot.EXPLODED.equals(mm.getPos(rc, cc).type));
		assertTrue("Game should end with an exploded mine", mm.isEnded());
		assertFalse("Game should be lost with an exploded mine", mm.isWon());
	}

	@Test(timeout = 50)
	public void shouldTellTheLastCoords() {
		final MineMap mm = genDefaultMap();
		do {
			genNewCoords();
			boolean pick, wasunknown = Spot.unknown.contains(mm.getPos(rc, cc).type);
			if (pick = r.nextBoolean()) {
				mm.pickASpot(rc, cc);
			} else {
				mm.flagASpot(rc, cc);
			}
			if (pick || wasunknown) {
				assertEquals("Reported column should be the same as the " + (pick ? "picked" : "flagged") + " one ", cc,
						mm.getLastCol());
				assertEquals("Reported row should be the same as the " + (pick ? "picked" : "flagged") + " one ", rc,
						mm.getLastRow());
			}
		} while (!mm.isEnded());
		int orc, occ;
		do {
			orc = genRow();
			occ = genCol();
		} while (orc == rc || occ == cc);
		mm.pickASpot(orc, occ);
		assertEquals("Reported column coordinate should not change via picking after the game ended", cc,
				mm.getLastCol());
		assertEquals("Reported row coordinate should not change via picking after the game ended", rc, mm.getLastRow());
		mm.flagASpot(orc, occ);
		assertEquals("Reported column coordinate should not change via flagging after the game ended", cc,
				mm.getLastCol());
		assertEquals("Reported row coordinate should not change via flagging after the game ended", rc,
				mm.getLastRow());
	}

	@Test(timeout = 50)
	public void shouldNotChangeLastCoordsWhenFlaggingKnown() {
		MineMap mm;
		do {
			mm = genDefaultMap();
			genNewCoords();
			mm.pickASpot(rc, cc);
			int orc, occ;
			do {
				orc = genRow();
				occ = genCol();
			} while (orc == rc || occ == cc);
			mm.pickASpot(orc, occ);
			if (!mm.isEnded()) {
				// Trying to flag a previously explored spot
				mm.flagASpot(rc, cc);
				assertEquals("Flag should not make changes to already explored column coords", occ, mm.getLastCol());
				assertEquals("Flag should not make changes to already explored row coords", orc, mm.getLastRow());
			}
		} while (mm.isEnded());
	}

	@Test(timeout = 50)
	public void changeFlagStatus() {
		final MineMap mm = genDefaultMap();
		for (int flagCount = 0; flagCount < cols; flagCount++) {
			genNewCoords();
			if (!Spot.UNEXPLORED.equals(mm.getPos(rc, cc).type)) {
				flagCount--;
				continue;
			}
			mm.flagASpot(rc, cc);
			assertEquals("Spot should be flagged", Spot.FLAG, mm.getPos(rc, cc).type);
		}
		for (int drc = 0; drc < rows; drc++) {
			for (int dcc = 0; dcc < cols; dcc++) {
				if (Spot.FLAG.equals(mm.getPos(drc, dcc).type)) {
					mm.flagASpot(drc, dcc);
					assertEquals("Spot should be unexplored if it was unflagged with the flagaspot method",
							Spot.UNEXPLORED, mm.getPos(drc, dcc).type);
				}
			}
		}
	}

	@Test(timeout = 50)
	public void noExceptionRangeCheckFlag() {
		// r.nextInt() typically generates some large numbers (not within 5-20 which is
		// our usual range of map dimensions.
		genDefaultMap().flagASpot(r.nextInt(), r.nextInt());
	}

	@Test(timeout = 50)
	public void noExceptionRangeCheckPick() {
		// r.nextInt() typically generates some large numbers (not within 5-20 which is
		// our usual range of map dimensions.
		genDefaultMap().pickASpot(r.nextInt(), r.nextInt());
	}

	@Test(timeout = 50)
	public void checkRange() {
		final MineMap mm = genDefaultMap();
		for (int dcc = -1; dcc <= cols; dcc++) {
			for (int drc = -4; drc <= rows + 2; drc++) {
				if (dcc < 0 || drc < 0 || dcc >= cols || drc >= rows) {
					assertTrue("Should tell the index is out of range of the map", mm.checkOutOfRange(drc, dcc));
				} else {
					assertFalse("Should tell the index is within the range of the map", mm.checkOutOfRange(drc, dcc));
				}
			}
		}
	}

	@Test(timeout = 50, expected = ArrayIndexOutOfBoundsException.class)
	public void shouldThrowErrorIfGetPosGetsIncorrectCoords() {
		final MineMap mm = genDefaultMap();
		while (true) {
			mm.getPos(r.nextInt(), r.nextInt());
		}
	}

	@Test(timeout = 50)
	public void shouldNotTraceWhenMinesAreClose() {
		boolean zeroNearMines = true, moreNearMines = true;
		MineMap mm;
		do {
			mm = genDefaultMap();
			genNewCoords();
			mm.pickASpot(rc, cc);
			if (!mm.isEnded()) {
				int tracedCount = mm.getExploredAreaSize();
				if (mm.getPos(rc, cc).nearMineCount == 0) {
					zeroNearMines = false;
					assertTrue("Should trace more than the picked spot if there are no close mines", tracedCount > 1);
				} else {
					moreNearMines = false;
					assertEquals("Should not trace more than the picked spot if there are close mines", 1, tracedCount);
				}
			}
		} while (mm.isEnded() || zeroNearMines || moreNearMines);
	}

	@Test(timeout = 50)
	public void shouldRejectPrematureInfoLeak() {
		final MineMap mm = genDefaultMap();
		assertEquals("Should not report the correct flagcount before the game ends", -1,
				mm.getCorrectlyIdentifiedMineCount());
		assertEquals("Should not report the incorrect flagcount before the game ends", -1,
				mm.getInCorrectlyIdentifiedMineCount());
	}

	@Test(timeout = 50)
	public void simpleCopyTest() throws MapCopyException {
		final MineMap orig = genDefaultMap();
		final MineMap newer = new MineMap(orig);
		assertEquals("Coloumns should be copied", cols, newer.cols);
		assertEquals("Rows should be copied", rows, newer.rows);
		assertEquals("Field size is not reported correctly in the copy", cols * rows, newer.fieldSize);
		assertEquals("Mine count is not reported correctly", ((double) cols * rows) * mineRatio, newer.mineCount, 1);
	}

	@Test(timeout = 50)
	public void midGameCopyTest() throws MapCopyException {
		MineMap mm;
		do {
			mm = genDefaultMap();
			for (int picks = 0; picks < 10; picks++) {
				genNewCoords();
				mm.pickASpot(rc, cc);
			}
			for (int flags = 0; flags < 10; flags++) {
				genNewCoords();
				mm.flagASpot(rc, cc);
			}
			if (!mm.isEnded()) {
				final MineMap newer = new MineMap(mm);
				for (int dcc = 0; dcc < cols; dcc++) {
					for (int drc = 0; drc < rows; drc++) {
						assertEquals("All the explored area should have the same amount of mines around them",
								mm.getPos(drc, dcc).nearMineCount, newer.getPos(drc, dcc).nearMineCount);
						assertEquals("All the explored area should have the same type of spot ",
								mm.getPos(drc, dcc).type, newer.getPos(drc, dcc).type);
					}
				}
			}
		} while (mm.isEnded());
	}

	@Test(timeout = 50, expected = MapCopyException.class)
	public void blockRepeatedCopy() throws MapCopyException {
		MineMap mm = genDefaultMap();
		MineMap firstCopy = new MineMap(mm);
		MineMap secondCopy = new MineMap(firstCopy, true);
	}

	@Test(timeout = 50)
	public void allowRepeatedCopy() throws MapCopyException {
		MineMap mm = genDefaultMap();
		MineMap firstCopy = new MineMap(mm, true);
		MineMap secondCopy = new MineMap(firstCopy, true);
	}

	@Test(timeout = 50)
	public void shouldNotAllowPickingAFlaggedSpot() {
		MineMap mm = genDefaultMap();
		mm.flagASpot(0, 0);
		mm.pickASpot(0, 0);
		assertEquals("Flagged spot should not be possible to change with pick a spot", Spot.FLAG, mm.getPos(0, 0).type);
	}

	@Test(timeout = 50)
	public void shouldNotAllowPickingACorrectlyFlaggedSpot() {
		genMineDetails();
		MineMap allMines = new MineMap(rows, cols, 1, 0);
		allMines.flagASpot(0, 0);
		allMines.pickASpot(0, 0);
		assertEquals("A correctly flagged spot should not be possible to change with pick a spot", Spot.FLAG,
				allMines.getPos(0, 0).type);
	}

}
