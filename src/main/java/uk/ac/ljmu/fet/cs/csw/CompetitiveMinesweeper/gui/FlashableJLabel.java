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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JLabel;

/**
 * This class makes sure we always have the correct background colour for a
 * particular label. One can set an expected colour or ask for the label to
 * flash its background.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class FlashableJLabel extends JLabel {

	private static final long serialVersionUID = 1728909396799612303L;

	/**
	 * The number of steps it takes to reach the desired colour in a flashing
	 * sequence. Then it takes the same amount of steps to get back to the original
	 * colour.
	 */
	public static final int steps = 10;

	/**
	 * This executor allows queueing flashing threads so only one is doing its
	 * flashing operations at a given time
	 */
	private final ExecutorService ex = Executors.newSingleThreadExecutor();
	/**
	 * Used as to block access to inThread and expectedBackground while dealing with
	 * flashing initialisation and background changes.
	 */
	private final String blocker = new String("Flashblocker");
	/**
	 * If this is not null then there was a background change request during an
	 * ongoing flash.
	 */
	private Color expectedBackground = null;
	/**
	 * Do we have a running thread to flash the background?
	 */
	private boolean inThread = false;

	/**
	 * This method simplifies the transition between two colour components
	 * 
	 * @param st   Starting component value
	 * @param fn   Final component value
	 * @param step Which step are we at in the transition? This must be between 0
	 *             and {@link #steps}
	 * @return the current value of the component
	 */
	private int scaleComp(final int st, final int fn, final int step) {
		return (int) (st + (double) step * (fn - st) / steps);
	}

	/**
	 * Sets the background to the desired colour. If the background is flashing at
	 * the moment, then the request is delayed till the end of the flash.
	 * 
	 * @param bg The desired background colour
	 */
	public void setExpectedBackground(final Color bg) {
		synchronized (blocker) {
			if (inThread) {
				expectedBackground = bg;
			} else {
				setBackground(bg);
			}
		}
	}

	/**
	 * Request to flash the background with a specific colour.
	 * 
	 * @param finish The colour to alter the background of this label temporarily.
	 * @param delay  Each step in the transition will take this many miliseconds. In
	 *               total the flash will last 2*{@link #steps}*delay.
	 */
	public void flash(final Color finish, final int delay) {
		// Queue the flashing request
		ex.execute(new Runnable() {
			/**
			 * This method is called by the executor when there are no queued flashing
			 * operations in front of the current one.
			 */
			@Override
			public void run() {
				// we wait until setExpectedBackground finishes its inner workings then set
				// inThread to true.
				synchronized (blocker) {
					inThread = true;
				}
				// Initialisation, breaking down the input colours to their components
				final Color start = FlashableJLabel.this.getBackground();
				final int stR = start.getRed();
				final int stG = start.getGreen();
				final int stB = start.getBlue();
				final int fnR = finish.getRed();
				final int fnG = finish.getGreen();
				final int fnB = finish.getBlue();
				// 1 if we are heading towards the colour finish, -1 if we return back to the
				// original colour
				int direction = 1;
				// The step we are taking at the moment (used with scaleComp).
				int i = 0;
				do {
					try {
						setBackground(
								new Color(scaleComp(stR, fnR, i), scaleComp(stG, fnG, i), scaleComp(stB, fnB, i)));
						i += direction;
						// Changes direction if we have visualised the final colour.
						if (i == steps) {
							direction = -1;
						}
						// Instruct a few repaints so the transition between the various colours is
						// smooth.
						for (int j = 0; j < 20; j++) {
							Thread.sleep(Math.max(1, delay / 20));
							repaint();
						}
					} catch (InterruptedException iex) {

					}
					// Finish the loop once we have returned the flash to the original colour
				} while (i != -1);
				// we wait until setExpectedBackground finishes its inner workings (if it is
				// called around this time of the flashing operation) then change the background
				// colour to the expected one.
				synchronized (blocker) {
					inThread = false;
					if (expectedBackground != null) {
						setBackground(expectedBackground);
						expectedBackground = null;
					}
				}
			}
		});
	}

	void terminateFlashing() {
		ex.shutdown();
	}
}
