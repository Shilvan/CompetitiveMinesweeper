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
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;

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
	 * Handles all flashes uniformly from a single thread. This thread exits if
	 * there are no further flashes needed. When adding a new flash, this class
	 * checks if we have a thread for the flashes. If there is no such thread, then
	 * it creates one.
	 * 
	 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
	 *         Moores University, (c) 2019"
	 */
	private static class GlobalFlasherThread {
		private HashSet<FlashableJLabel> toFlash = new HashSet<>();

		public void addFlash(final FlashableJLabel forLabel) {
			int size;
			synchronized (toFlash) {
				toFlash.add(forLabel);
				size = toFlash.size();
			}
			if (size == 1) {
				// Creates the thread that will do the flashes
				new Thread() {
					@Override
					public void run() {
						while (!toFlash.isEmpty()) {
							int mindelay = Integer.MAX_VALUE;
							synchronized (toFlash) {
								// All to be flashed labels are told to do their flashes
								final Iterator<FlashableJLabel> itFlash = toFlash.iterator();
								while (itFlash.hasNext()) {
									final FlashableJLabel curr = itFlash.next();
									final int askedDelay = curr.singleFlashIteration();
									if (askedDelay < 0) {
										// No further flashing needed on the current label
										itFlash.remove();
									} else {
										mindelay = Math.min(askedDelay, mindelay);
									}
								}
							}
							if (mindelay != Integer.MAX_VALUE) {
								// There was a change to mindelay, we have to wait for the repaints a bit
								try {
									// Instruct a few repaints so the transition between the various colours is
									// smooth.
									for (int j = 0; j < 20; j++) {
										Thread.sleep(mindelay);
										synchronized (toFlash) {
											for (FlashableJLabel curr : toFlash) {
												curr.repaint();
											}
										}
									}
								} catch (final InterruptedException e) {
									// Ignore
								}
							}
						}
					}
				}.start(); // Starts the just created thread so its run method works independently from the
							// rest of the minesweeper.
			}
		}
	}

	/**
	 * We will do the flashing of all labels via this single thread, note this
	 * thread will die out if there is nothing to flash and it will be automatically
	 * resurrected if needed.
	 */
	private static GlobalFlasherThread globalFlasher = new GlobalFlasherThread();

	/**
	 * Represents a flashing request that can be queued.
	 * 
	 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
	 *         Moores University, (c) 2019"
	 */
	private static class FlashRequest {
		public final Color finish;
		public final int delay;

		public FlashRequest(final Color finish, final int delay) {
			this.finish = finish;
			this.delay = delay;
		}
	}

	/**
	 * The queue of necessary flashes of this single label. This is used only if
	 * there are more than one flashes to be done for the label.
	 */
	private ArrayDeque<FlashRequest> furtherFlashes = new ArrayDeque<>();

	/**
	 * The number of steps it takes to reach the desired colour in a flashing
	 * sequence. Then it takes the same amount of steps to get back to the original
	 * colour.
	 */
	public static final int totSteps = 10;

	/**
	 * Where we are in the transition between mid colour and start colour
	 */
	private int currStep = 0;
	/**
	 * Colour components of the starting colour
	 */
	private int startR, startG, startB;
	/**
	 * Colour components of the mid colour (the one the flash transitions to)
	 */
	private int midR, midG, midB;
	/**
	 * This is 1 if we are transitioning towards the mid colour, -1 if we are going
	 * towards the start colour
	 */
	private int direction;
	/**
	 * The delay we should apply for each colour transition step
	 */
	private int delay;
	/**
	 * The time the last transition took place (unix time in ms)
	 */
	private long prevChange;

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
	 * Should we accept more flashing requests?
	 */
	private boolean terminated = false;

	/**
	 * This method simplifies the transition between two colour components
	 * 
	 * @param st   Starting component value
	 * @param fn   Final component value
	 * @param step Which step are we at in the transition? This must be between 0
	 *             and {@link #totSteps}
	 * @return the current value of the component
	 */
	private int scaleComp(final int st, final int fn, final int step) {
		return (int) (st + (double) step * (fn - st) / totSteps);
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
	 * Iterates the background colour of the label by a single step. If it is not
	 * yet time for an iteration, this method will just return immediately without
	 * changing the background.
	 * 
	 * @return The time gap to be applied for the background repaints. If <0 then
	 *         there is no further repaint needed.
	 */
	private int singleFlashIteration() {
		long currTime = System.currentTimeMillis();
		if (prevChange + delay > currTime) {
			// Its not time yet to do our thing again
			return (int) (currTime - prevChange);
		}
		setBackground(new Color(scaleComp(startR, midR, currStep), scaleComp(startG, midG, currStep),
				scaleComp(startB, midB, currStep)));
		currStep += direction;
		// Changes direction if we have visualised the mid colour.
		if (currStep == totSteps) {
			// We are heading towards the start colour
			direction = -1;
		} else if (currStep == -1) {
			if (furtherFlashes.isEmpty()) {
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
				// Finish the flash once we have returned the flash to the original colour
				return -1;
			} else {
				FlashRequest nextFlash = furtherFlashes.pollFirst();
				initNextFlash(nextFlash.finish, nextFlash.delay);
			}
		}
		return Math.max(1, delay / 20);
	}

	/**
	 * Prepares a flash by resetting all parameters used by
	 * {@link #singleFlashIteration()}.
	 * 
	 * @param finish The colour to be shown at the middle of the flash
	 * @param delay  The delay between each colour change
	 */
	private void initNextFlash(final Color finish, final int delay) {
		// Initialisation, breaking down the input colours to their components
		Color start = getBackground();
		startR = start.getRed();
		startG = start.getGreen();
		startB = start.getBlue();
		midR = finish.getRed();
		midG = finish.getGreen();
		midB = finish.getBlue();
		// As we just start, we want the transition to be towards the colour specified
		// in the parameter. Thus we set the direction to 1 (i.e., towards)
		direction = 1;
		this.delay = delay;
		currStep = 0;
		prevChange = System.currentTimeMillis() - delay;
	}

	/**
	 * Request to flash the background with a specific colour.
	 * 
	 * @param finish The colour to alter the background of this label temporarily.
	 * @param delay  Each step in the transition will take this many miliseconds. In
	 *               total the flash will last 2*{@link #totSteps}*delay.
	 */
	public void flash(final Color finish, final int delay) {
		if (terminated) {
			return;
		}
		// we wait until setExpectedBackground finishes its inner workings then set
		// inThread to true.
		synchronized (blocker) {
			if (inThread) {
				furtherFlashes.add(new FlashRequest(finish, delay));
				return;
			}
			inThread = true;
		}
		initNextFlash(finish, delay);
		// We now start the flashing
		globalFlasher.addFlash(this);
	}

	/**
	 * Makes this object non-flashable. Note that this method will not eliminate
	 * flashes that were requested before the call. They will finish in an orderly
	 * fashion but no further requests are processed.
	 */
	void terminateFlashing() {
		terminated = true;
	}
}
