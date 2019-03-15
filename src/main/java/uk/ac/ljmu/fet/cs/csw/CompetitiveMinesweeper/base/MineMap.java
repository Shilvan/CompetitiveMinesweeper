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

import java.util.Random;

/**
 * The representation of a mine map. Allows random generation of mines as well
 * as copying from other mine maps. The main operations to be used by AIs are
 * pickASpot and flagASpot. These operations are expected to be used in a
 * sequential fashion (no multithreaded access is allowed to them). Apart from
 * these two important methods, a minemap also contains some functionality for
 * limiting AI's to speeds perceivable in UIs.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class MineMap {

	public class MapCopyException extends Exception {
		private static final long serialVersionUID = -8312441820699627778L;

		public MapCopyException(final String message) {
			super(message);
		}

	}

	// the random generator for all minemaps
	public static final Random r = new Random();

	// Map representations. All have the following dimensions:
	// - first dimension is rows,
	// - second is columns.

	// The actual map
	private final Spot[][] completeMap;
	// The explored version of the map
	private final ExploredSpot[][] exploredMap;
	// Near-mine counts for each individual spot
	private final int[][] howManyAround;

	/**
	 * The number of rows and columns in this minefield.
	 */
	public final int rows, cols;
	/**
	 * The total area of the minefield (in square spots).
	 */
	public final int fieldSize;
	/**
	 * Very helpful to tell how many mines are contained in a given map. This
	 * knowledge is often times essential for mine sweeper AIs.
	 */
	public final int mineCount;

	// The delay applied for each MineMap
	public final int uidelay;

	// true after a mine spot was picked or after the game was won
	private boolean gameEnded;
	// true if the constructor has done deploying the mines. If true, the
	// sweepAround method is used for tracing unexplored non-mined areas
	private boolean preparedMap;

	private boolean allowCopy = true;

	private int lastCol = -1, lastRow = -1;

	/**
	 * Enables the creation of completely randomly generated MineMaps
	 * 
	 * @param rows      How many rows should the map have.
	 * @param cols      How many columns should the map have.
	 * @param mineRatio What's the percentage of the mines to the total number of
	 *                  spots in the whole map.
	 * @param uidelay   How long should we wait before each AI operation takes
	 *                  action.
	 * 
	 * @throws IllegalArgumentException   if the number of mines would be more than
	 *                                    the total number of spots, or when the
	 *                                    uidelay would be negative (should finish
	 *                                    the delay before we even start?!)
	 * @throws NegativeArraySizeException if the nr. of columns or rows were set to
	 *                                    a negative value
	 */
	public MineMap(final int rows, final int cols, final double mineRatio, int uidelay) {
		if (mineRatio > 1) {
			throw new IllegalArgumentException("Impossible to create a map with more mines than spots");
		}
		checkForCorrectUIDelay(uidelay);
		// Basic init
		this.uidelay = uidelay;
		this.rows = rows;
		this.cols = cols;
		fieldSize = rows * cols;
		completeMap = new Spot[rows][cols];
		howManyAround = new int[rows][cols];
		exploredMap = new ExploredSpot[rows][cols];
		gameEnded = false;
		preparedMap = false;

		// Declaring the map completely unexplored
		for (int rc = 0; rc < rows; rc++) {
			for (int cc = 0; cc < cols; cc++) {
				exploredMap[rc][cc] = new ExploredSpot(Spot.UNEXPLORED, -1);
				completeMap[rc][cc] = Spot.UNEXPLORED;
			}
		}

		// Deploying the mines in the playing field
		mineCount = (int) (Math.round(fieldSize * mineRatio));
		for (int howManyMinesSoFar = 0; howManyMinesSoFar < mineCount; howManyMinesSoFar++) {
			int cc = r.nextInt(cols);
			int rc = r.nextInt(rows);
			// Ensuring that we don't deploy a mine two times at the same spot
			if (Spot.MINE.equals(completeMap[rc][cc])) {
				howManyMinesSoFar--;
				continue;
			}
			completeMap[rc][cc] = Spot.MINE;
			// Updating the how many around matrix
			sweepAround(rc, cc);
		}
		preparedMap = true;
	}

	/**
	 * The copy constructor of the MineMap. It allows to make an exact copy of
	 * another MineMap. This is helpful if one wants to test multiple algorithms
	 * with the exact same maps. It is important that this map copies the exact
	 * state. Thus it is possible to make a copy of a half solved mine map and allow
	 * an alternative solver to tackle the rest of the map independently from the
	 * original.
	 * 
	 * @param otherToCopy The original map to copy from.
	 * @param allowCopy   Specify if we allow further copies of this map. True if we
	 *                    allow them, false otherwise.
	 * @throws MapCopyException If the map in the first parameter is not supposed to
	 *                          be copied.
	 */
	public MineMap(final MineMap otherToCopy, final boolean allowCopy) throws MapCopyException {
		checkRightToCopy(otherToCopy, allowCopy);
		// Copies all the data members
		uidelay = otherToCopy.uidelay;
		rows = otherToCopy.rows;
		cols = otherToCopy.cols;
		completeMap = new Spot[rows][cols];
		howManyAround = new int[rows][cols];
		exploredMap = new ExploredSpot[rows][cols];
		fieldSize = otherToCopy.fieldSize;
		mineCount = otherToCopy.mineCount;
		copyHelper(otherToCopy);
	}

	/**
	 * This copy constructor acts like {@link #MineMap(MineMap, boolean)} but always
	 * assumes that the copy is not allowed to be copied anymore.
	 */
	public MineMap(final MineMap otherToCopy) throws MapCopyException {
		this(otherToCopy, false);
	}

	/**
	 * This method is a helper for the copy constructors and ensures that only those
	 * copies are done which are allowed to be copied. It also maintains the class's
	 * {@link #allowCopy} data member.
	 * 
	 * @param otherToCopy The source of the copying
	 * @param allowCopy   if the copy is allowed then this value will be the new
	 *                    copy's {@link #allowCopy} data member. True if further
	 *                    copies are allowed, false otherwise.
	 * @throws MapCopyException If the source is not allowed to be copied.
	 */
	private void checkRightToCopy(final MineMap otherToCopy, final boolean allowCopy) throws MapCopyException {
		if (!otherToCopy.allowCopy) {
			throw new MapCopyException("The source map cannot be copied further");
		}
		this.allowCopy = allowCopy;
	}

	/**
	 * Checks if the specified UI delay is acceptable as Thread.sleep's input. If
	 * not it throws an IllegalArgumentException.
	 * 
	 * <i>Warning: this method does not actually set the delay, just checks its
	 * value. It is still the constructor that is responsible for setting the value
	 * itself in the corresponding data field.</i>
	 * 
	 * @param uidelay How long should we wait before each AI operation takes action.
	 * @throws IllegalArgumentException when the uidelay would be negative (should
	 *                                  finish the delay before we even start?!)
	 */
	private void checkForCorrectUIDelay(final int uidelay) {
		if (uidelay < 0) {
			throw new IllegalArgumentException("UI cannot have a negative delay");
		}
	}

	/**
	 * A slight variant of the copy constructor ({@link #MineMap(MineMap)}). This
	 * variant allows uiDelays to be changed while the rest of the map is copied
	 * just like in the other case. For relevant use cases check the description of
	 * the other copy constructor as well.
	 * 
	 * @param otherToCopy The original map to copy from.
	 * @param allowCopy   Specify if we allow further copies of this map. True if we
	 *                    allow them, false otherwise.
	 * @param newUIdelay  The alternative UI delay to be used with this map instance
	 * @throws MapCopyException If the map in the first parameter is not supposed to
	 *                          be copied.
	 * @throws IllegalArgumentException when the uidelay would be negative (should
	 *                                  finish the delay before we even start?!)
	 */
	public MineMap(final MineMap otherToCopy, final int newUIdelay, final boolean allowCopy) throws MapCopyException {
		checkForCorrectUIDelay(newUIdelay);
		checkRightToCopy(otherToCopy, allowCopy);
		uidelay = newUIdelay;
		rows = otherToCopy.rows;
		cols = otherToCopy.cols;
		completeMap = new Spot[rows][cols];
		howManyAround = new int[rows][cols];
		exploredMap = new ExploredSpot[rows][cols];
		fieldSize = otherToCopy.fieldSize;
		mineCount = otherToCopy.mineCount;
		copyHelper(otherToCopy);
	}

	/**
	 * This copy constructor acts like {@link #MineMap(MineMap, int, boolean)} but
	 * always assumes that the copy is not allowed to be copied anymore.
	 */
	public MineMap(final MineMap otherToCopy, final int newUIdelay) throws MapCopyException {
		this(otherToCopy, newUIdelay, false);
	}

	/**
	 * Makes sure the deep copy phase of the copy constructors is done well
	 * 
	 * @param otherToCopy The original map to copy from.
	 */
	private void copyHelper(final MineMap otherToCopy) {
		gameEnded = otherToCopy.gameEnded;
		preparedMap = true;

		// Deep copy of the arrays.
		for (int rc = 0; rc < rows; rc++) {
			for (int cc = 0; cc < cols; cc++) {
				exploredMap[rc][cc] = otherToCopy.exploredMap[rc][cc];
				completeMap[rc][cc] = otherToCopy.completeMap[rc][cc];
				howManyAround[rc][cc] = otherToCopy.howManyAround[rc][cc];

			}
		}
	}

	/**
	 * Allows to determine if a given spot lies outside the perimeter of this map or
	 * not.
	 * 
	 * @param rowCoord The row coordinate to test
	 * @param colCoord The column coordinate to test
	 * @return true if the spot asked about is not within the range of this map.
	 *         false otherwise.
	 */
	public boolean checkOutOfRange(final int rowCoord, final int colCoord) {
		return colCoord < 0 || rowCoord < 0 || rowCoord >= completeMap.length || colCoord >= completeMap[0].length;
	}

	/**
	 * Helper for the tracing and mine counting operations. Allows the tracing and
	 * mine counting to take affect in the immediate vicinity of a given spot.
	 * 
	 * @param rowCoord The row coordinate of the spot to be looked around.
	 * @param colCoord The column coordinate of the spot to be looked around.
	 */
	private void sweepAround(final int rowCoord, final int colCoord) {
		for (int dc = colCoord - 1; dc < colCoord + 2; dc++) {
			for (int dr = rowCoord - 1; dr < rowCoord + 2; dr++) {
				if (checkOutOfRange(dr, dc))
					continue;
				// The internals of this method act differently dependin on whether the mehtod
				// is called from the constructor or afterwards.
				if (preparedMap) {
					traceFrom(dr, dc);
				} else {
					howManyAround[dr][dc]++;
				}
			}
		}
	}

	/**
	 * Helper method to change the last touched coordinate (this is usually used by
	 * flagASpot/pickASpot.
	 * 
	 * @param rowCoord The row coordinate of the spot that changed last time
	 * @param colCoord The column coordinate of the spot that changed last time
	 */
	private void markPicked(final int rowCoord, final int colCoord) {
		lastCol = colCoord;
		lastRow = rowCoord;
	}

	/**
	 * This is one of the main interaction points for AIs. Allows telling the map
	 * that the AI thinks this spot is unsafe (i.e., there is a suspected mine
	 * underneath it). It also allows the AI to change its mind: a flagged spot can
	 * always be turned back to unexplored.
	 * 
	 * @param rowCoord the row coordinate of a suspected spot
	 * @param colCoord the column coordinate of a suspected spot
	 */
	public synchronized void flagASpot(final int rowCoord, final int colCoord) {
		// initial checks
		if (gameEnded || checkOutOfRange(rowCoord, colCoord))
			return;

		// UI specific actions
		delayForUI();
		boolean mark = true;

		// Our main business here, flagging/unflagging the given spot depending on its
		// previous state
		if (Spot.UNEXPLORED.equals(exploredMap[rowCoord][colCoord].type)) {
			exploredMap[rowCoord][colCoord] = new ExploredSpot(Spot.FLAG, -1);
		} else if (Spot.FLAG.equals(exploredMap[rowCoord][colCoord].type)) {
			exploredMap[rowCoord][colCoord] = new ExploredSpot(Spot.UNEXPLORED, -1);
		} else {
			// No UI related change has happened
			mark = false;
		}

		if (mark) {
			// There was a UI specific change, this should be recorded about the suspected
			// spot.
			markPicked(rowCoord, colCoord);
		}
	}

	/**
	 * Allows an introduction of some delay in the minemap automated operations.
	 * This makes sure the UI can catch up with all the updates the AI does.
	 */
	private void delayForUI() {
		// Rate of requests to flag a spot are limited by uidelay.
		try {
			Thread.sleep(uidelay);
		} catch (InterruptedException iex) {

		}
	}

	/**
	 * This is one of the main interaction points for AIs. Allows telling the map
	 * that the AI thinks this part is safe to step on and expects to reveal what is
	 * underneath. If the spot chosen has 0 adjacent mines, then the surrounding
	 * area is automatically explored. This method only functions if the game has
	 * not ended yet or if the specified x and y coordinates are within the map.
	 * 
	 * @param rowCoord the rows address coordinate of the spot to be uncovered
	 * @param colCoord the coloumn address coordinate of the spot to be uncovered
	 * @return true if the game has ended/if there is no reason to call pick a spot
	 *         again. false otherwise.
	 */
	public synchronized boolean pickASpot(final int rowCoord, final int colCoord) {
		// initial checks
		if (gameEnded) {
			return true;
		}
		if (checkOutOfRange(rowCoord, colCoord) || Spot.FLAG.equals(exploredMap[rowCoord][colCoord].type)) {
			return false;
		}

		// UI related operations
		delayForUI();
		// Remembering what location was picked (this is really important for the UI)
		markPicked(rowCoord, colCoord);

		// The actual reveal of the spot
		boolean ret = false;
		if (Spot.MINE.equals(completeMap[rowCoord][colCoord])) {
			// The game ended..
			exploredMap[rowCoord][colCoord] = new ExploredSpot(Spot.EXPLODED, Integer.MAX_VALUE);
			gameEnded = true;
			ret = true;
		} else {
			// The spot was not a mine, we can explore further
			traceFrom(rowCoord, colCoord);
		}
		return ret;
	}

	/**
	 * Allows discovering the largest extent of unexplored, but completely safe
	 * (i.e., spots with no mines on them) area
	 * 
	 * @param rowCoord the row coordinate where the tracing should happen from
	 * @param colCoord the column coordinate where the tracing should happen from
	 */
	private void traceFrom(final int rowCoord, final int colCoord) {
		if (Spot.UNEXPLORED.equals(exploredMap[rowCoord][colCoord].type)) {
			exploredMap[rowCoord][colCoord] = new ExploredSpot(Spot.SAFE, howManyAround[rowCoord][colCoord]);
			if (howManyAround[rowCoord][colCoord] == 0) {
				sweepAround(rowCoord, colCoord);
			}
		}
	}

	/**
	 * Determines if the game has been played to its final steps or not. If this
	 * returns true, the pickASpot and flagASpot methods don't function anymore!
	 * 
	 * @return true if the game has no more moves. false otherwise.
	 */
	public boolean isEnded() {
		if (gameEnded)
			return true;
		if (isWon()) {
			gameEnded = true;
			return true;
		}
		return false;
	}

	/**
	 * Determines if the game has been won.
	 * 
	 * @return true if the game is won, false if the game is still ongoing or the
	 *         game is lost
	 */
	public boolean isWon() {
		return getExploredAreaSize() == fieldSize - mineCount && mineCount - getFlaggedMineCount() == 0;
	}

	/**
	 * Tells how big of an area have been picked or flagged in this map
	 * 
	 * @return the number of spots that have been interacted with
	 */
	public int getExploredAreaSize() {
		int size = 0;
		for (int rc = 0; rc < rows; rc++) {
			for (int cc = 0; cc < cols; cc++) {
				size += Spot.unknown.contains(exploredMap[rc][cc].type) ? 0 : 1;
			}
		}
		return size;
	}

	/**
	 * Calculates the number of mines that have a flag on them
	 * 
	 * @return the number of correctly flagged mines
	 */
	private int getFlaggedMineCount() {
		int idd = 0;
		for (int rc = 0; rc < rows; rc++) {
			for (int cc = 0; cc < cols; cc++) {
				idd += Spot.FLAG.equals(exploredMap[rc][cc].type) && Spot.MINE.equals(completeMap[rc][cc]) ? 1 : 0;
			}
		}
		return idd;
	}

	/**
	 * Determines how many flags were placed on mines
	 * 
	 * @return the number of flags correctly placed or -1 if the game has not ended
	 *         yet
	 */
	public int getCorrectlyIdentifiedMineCount() {
		if (gameEnded) {
			return getFlaggedMineCount();
		} else {
			return -1;
		}
	}

	/**
	 * Determines how many flags were placed on spots without mines
	 * 
	 * @return Either the number of flags incorrectly placed or -1 (if the game is
	 *         still ongoing)
	 */
	public int getInCorrectlyIdentifiedMineCount() {
		if (gameEnded) {
			int idd = 0;
			for (int rc = 0; rc < rows; rc++) {
				for (int cc = 0; cc < cols; cc++) {
					idd += Spot.FLAG.equals(exploredMap[rc][cc].type) && !Spot.MINE.equals(completeMap[rc][cc]) ? 1 : 0;
				}
			}
			return idd;
		} else {
			return -1;
		}
	}

	/**
	 * This is one of the main interaction points for AIs. Allows to query the
	 * explored map internally maintained by this MineMap.
	 * 
	 * @param rowCoord the row coordinate of the requested spot
	 * @param colCoord the column coordinate of the requested spot
	 * @return The details about the given spot in the explored map
	 * @throws ArrayIndexOutOfBoundsException if the requested position is not
	 *                                        within the the map, if you want to
	 *                                        avoid this exception you can check if
	 *                                        your coordinates are within range with
	 *                                        {@link #checkOutOfRange(int, int)
	 *                                        checkOutOfRange} method.
	 */
	public ExploredSpot getPos(final int rowCoord, final int colCoord) throws ArrayIndexOutOfBoundsException {
		return exploredMap[rowCoord][colCoord];
	}

	/**
	 * Queries the X position of the last picked/flagged spot on the map
	 * 
	 * @return
	 */
	public int getLastCol() {
		return lastCol;
	}

	/**
	 * Queries the Y position of the last picked/flagged spot on the map
	 * 
	 * @return
	 */
	public int getLastRow() {
		return lastRow;
	}
}
