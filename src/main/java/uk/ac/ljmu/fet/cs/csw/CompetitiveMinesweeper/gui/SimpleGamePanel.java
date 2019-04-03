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
package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.ExploredSpot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;

/**
 * Offers the a UI for a particular mine sweeping game (the game is actually
 * represented by a {@link MineMap} instance). The UI is atomatically sizing
 * itself based on the map size (smaller maps have bigger visualisations).
 * Implements some simple visual effects with the help of the
 * {@link FlashableJLabel}. At the end of a game, the UI also offers some basic
 * statistics about the game.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class SimpleGamePanel extends JFrame implements Runnable {
	private static final long serialVersionUID = -7488536857391039958L;
	/**
	 * Colour of unknown territory
	 */
	public static final Color inFogColour = new Color(12, 36, 97);
	/**
	 * Colour of explored territory
	 */
	public static final Color clearColour = new Color(74, 105, 189);
	/**
	 * Colour of regular cell contents
	 */
	public static final Color textColour = new Color(246, 185, 59);
	/**
	 * Colour of flagged spot
	 */
	public static final Color flagColour = new Color(229, 80, 57);
	/**
	 * Colour of a spot recently picked/flagged
	 */
	public static final Color pickedColour = new Color(130, 204, 221);
	/**
	 * Colour to depict the place of death of our sweeper
	 */
	public static final Color deadColour = new Color(183, 21, 64);
	/**
	 * Colour to show the winning spiral in
	 */
	public static final Color winSpiral = new Color(255, 177, 66);

	/**
	 * Defines the minimum height/width of the squares that represent the spots on
	 * the gui
	 */
	public static final int minSpotDimension = 15;

	// Main areas of the game window
	private final JPanel playArea;

	// The map which is being played at the moment
	protected final MineMap toMonitor;
	/**
	 * This is the visual representation of a MineMap. This is a completely
	 * unalterable array so it is always in alignment with MineMap's coordinates.
	 * Allows access to the individual visualisation labels on the play area. Thus
	 * allows their customisation without changing the main visualisation logic.
	 */
	public final List<List<FlashableJLabel>> field;
	// What was the last picked/flagged spot's coordinate. Allows us to not to flash
	// the same coordinate again.
	private int flashedRow = -1, flashedCol = -1;

	private boolean disposeAfterRun = true;

	/**
	 * Instantiating a window will immediately show it and starts the monitoring and
	 * visualisation process of a given minemap.
	 * 
	 * @param toMonitor The map to visualise
	 * @param title     The title of this window
	 * @param scale     <i>true</i> enable automatically scaling the spots,
	 *                  <i>false</i> if this feature should be disabled
	 */
	public SimpleGamePanel(final MineMap toMonitor, final String title, boolean scale) {
		super(title);
		this.toMonitor = toMonitor;
		setIconImage(MineSweeper.mineswicon);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Prepare the playing grid
		ArrayList<List<FlashableJLabel>> alterableField = new ArrayList<>();
		playArea = new JPanel();
		playArea.setLayout(new GridLayout(toMonitor.rows, toMonitor.cols, 0, 0));
		// Spot size dynamically set so the window is reasonably big when the grid is
		// small, but when the grid is big we will limit the spot size to 15 pixels
		int spotSize = scale ? Math.max(minSpotDimension, 100 - Math.max(toMonitor.rows, toMonitor.cols) * 4 + 10)
				: minSpotDimension;
		for (int rc = 0; rc < toMonitor.rows; rc++) {
			ArrayList<FlashableJLabel> alterableRow = new ArrayList<>();
			for (int cc = 0; cc < toMonitor.cols; cc++) {
				// Each and every item in the grid is a flashable label
				FlashableJLabel currSpot = new FlashableJLabel();
				currSpot.setText(" ");
				// Must be opaque so the background colour changes are visible
				currSpot.setOpaque(true);
				ensureColour(currSpot, textColour, inFogColour);
				// Sizing the cell and its font
				currSpot.setFont(new Font(Font.MONOSPACED, Font.PLAIN, spotSize - 1));
				currSpot.setPreferredSize(new Dimension(spotSize, spotSize));
				// centering the text
				currSpot.setHorizontalAlignment(JLabel.CENTER);
				currSpot.setVerticalAlignment(JLabel.CENTER);
				// Adding a 3d effect to the spot
				currSpot.setBorder(BorderFactory.createEtchedBorder());
				alterableRow.add(currSpot);
				playArea.add(currSpot);
			}
			// Making sure the grid cannot be modified by any users of this class
			alterableField.add(Collections.unmodifiableList(alterableRow));
		}
		// Adding an external border around the full playing grid and ensuring the grid
		// is not stretched/compressed.
		playArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		playArea.setMaximumSize(new Dimension(2 + spotSize * toMonitor.cols, 2 + spotSize * toMonitor.rows));
		field = Collections.unmodifiableList(alterableField);

		// registering the two main components of the panel
		Container cp = getContentPane();
		cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
		cp.add(playArea);

		// While the game is ongoing the window close controls will result in exiting
		// the game
		pack();
		// fixing the size of the window so it matches the grid
		setResizable(false);
		setVisible(true);
		// The panel is ready to show the game
		// We will launch the refresh mechanism now (see the run() method)
		new Thread(this).start();
	}

	/**
	 * Colour changes for a label will be propagated if the current colour is not
	 * the same as the one that was requested
	 * 
	 * @param forWhat The label for which the change is needed
	 * @param fg      The desired foreground (i.e., text) colour
	 * @param bg      The desired background colour
	 */
	private void ensureColour(final FlashableJLabel forWhat, final Color fg, final Color bg) {
		if (!forWhat.getForeground().equals(fg)) {
			forWhat.setForeground(fg);
		}
		if (!forWhat.getBackground().equals(bg)) {
			forWhat.setExpectedBackground(bg);
		}
	}

	/**
	 * This method updates all labels in the play area to show the state of the map
	 * at the time when the method is called.
	 */
	private void refreshArea() {
		for (int rc = 0; rc < toMonitor.rows; rc++) {
			List<FlashableJLabel> currRow = field.get(rc);
			for (int cc = 0; cc < toMonitor.cols; cc++) {
				FlashableJLabel currLabel = currRow.get(cc);
				ExploredSpot currSpot = toMonitor.getPos(rc, cc);
				// this char will contain an unicode char code
				// this char depends on what is the current spot
				char toShowinSpot = 1;
				switch (currSpot.type) {
				case EXPLODED:
					// A star sign
					toShowinSpot = 0x2600;
					ensureColour(currLabel, textColour, clearColour);
					break;
				case FLAG:
					// A flag sign
					toShowinSpot = 0x2691;
					ensureColour(currLabel, flagColour, clearColour);
					break;
				case MINE:
					System.err.println("A mine was in an explored spot, MineMap is broken!");
					System.exit(1);
				case SAFE:
					// A number (if there are mines nearby) or a space (if there are no mines
					// around)
					toShowinSpot = currSpot.nearMineCount == 0 ? ' ' : (char) ('0' + currSpot.nearMineCount);
					ensureColour(currLabel, textColour, clearColour);
					break;
				case UNEXPLORED:
					// Space ...
					toShowinSpot = ' ';
					ensureColour(currLabel, textColour, inFogColour);
					break;
				}
				String newText = "" + toShowinSpot;
				if (!currLabel.getText().equals(newText)) {
					currLabel.setText(newText);
				}
			}
		}
	}

	/**
	 * Ensures the window contains the most up to date look of the MineMap.
	 */
	@Override
	public void run() {
		do {
			// Refresh the play area
			refreshArea();
			// We can now flash the most recently picked/flagged item so the GUI can show
			// the AI's operations. Note this does not show all the AI ops. It just shows
			// the latest one when we reach this place in the code.
			int currLastCol = toMonitor.getLastCol();
			int currLastRow = toMonitor.getLastRow();
			if (flashedRow != currLastRow || flashedCol != currLastCol) {
				flashedRow = currLastRow;
				flashedCol = currLastCol;
				// We ask the label to flash itself
				field.get(flashedRow).get(flashedCol).flash(pickedColour, 20);
			}

			// We have done all the refresh procedures needed for this cycle. We can now
			// wait till the next grid refresh is due
			try {
				Thread.sleep(50);
			} catch (InterruptedException iex) {
				// ignore
			}

			// If the game has not ended yet we will repeat this loop.
		} while (!toMonitor.isEnded());

		// Refresh the area the last time (as in there could have been updates to the
		// map in
		// the last 50 ms long sleep)
		refreshArea();

		if (toMonitor.isWon()) {
			// If the game has been successful we will show a fancy spiral originating at
			// the last picked spot
			int centercol = toMonitor.getLastCol();
			int centerrow = toMonitor.getLastRow();
			int prevrow = -1, prevcol = -1;
			int minDim = Math.min(toMonitor.cols, toMonitor.rows);
			double gap = minDim / 80.0;
			// we want to have variable increment around the spiral so we have enough
			// resolution even in the larger spiral parts
			double inc = Math.PI / 180;
			double end = Math.PI * (10 + Math.random() * 2);
			double pastPiMult = 0;
			for (double angle = 0; angle < end; angle += inc) {
				// Calculating a single point on the spiral
				double r = angle * gap;
				double nrow = r * Math.sin(angle) + centerrow;
				double ncol = r * Math.cos(angle) + centercol;
				int currrow = (int) Math.round(nrow);
				int currcol = (int) Math.round(ncol);
				// Determine if we need to draw the point (if it is on screen and we have not
				// drawn it yet)
				if (toMonitor.checkOutOfRange(currrow, currcol))
					continue;
				if (currrow != prevrow || currcol != prevcol) {
					prevrow = currrow;
					prevcol = currcol;
					// Let's draw the spiral now
					field.get(currrow).get(currcol).flash(winSpiral, 50);
					try {
						// Wait a bit so we have a longer arc from the spiral
						Thread.sleep(Math.max(1, (int) (70 + 1.3 * (10 - minDim))));
					} catch (InterruptedException iex) {
						// ignore
					}
				}
				// now we need to determine if we need to alter our angle increment
				// this is needed so the biggest spiral arms are still continuous
				double currPiMult = Math.round(angle / Math.PI);
				if (pastPiMult != currPiMult) {
					pastPiMult = currPiMult;
					inc /= 1.1;
				}
			}
		} else {
			// If the game has not been won, we will flash the mine that killed us a few
			// times
			for (int i = 0; i < 20; i++) {
				field.get(toMonitor.getLastRow()).get(toMonitor.getLastCol()).flash(deadColour, 40);
			}
			// At the end of all the dead Colour flashes we leave the background with the
			// dead colour
			field.get(toMonitor.getLastRow()).get(toMonitor.getLastCol()).setExpectedBackground(deadColour);
		}
		if (disposeAfterRun) {
			dispose();
		}

		// Disabling flashing to allow an orderly termination of all threads associated
		// with the rendering of this panel
		for (int rc = 0; rc < toMonitor.rows; rc++) {
			List<FlashableJLabel> row = field.get(rc);
			for (int cc = 0; cc < toMonitor.cols; cc++) {
				row.get(cc).terminateFlashing();
			}
		}
	}

	protected void setDisposeAfterRun(boolean disposeAfterRun) {
		this.disposeAfterRun = disposeAfterRun;
	}

}
