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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;

/**
 * On top of the {@link SimpleGamePanel} functionalty, this class also offers a
 * few controls at the end of a game. These controls allow to start a new game
 * or reconfigure the game setup.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class RestartableGamePanel extends SimpleGamePanel implements ActionListener, WindowListener {
	private static final long serialVersionUID = -4222596642204133499L;

	// Basic game status messages:
	public static final String won = "Game Won!";
	public static final String lost = "Game Lost!";
	public static final String ongoing = "Game still going!";

	// The bottom area of the game window
	private final JPanel stats, configReturn;

	// Label to show the status messages
	private final JLabel overallStatus = new JLabel(ongoing);

	// Buttons to steer a new game
	private final JButton reconfig = new JButton("Reconfigure");
	private final JButton playAgain = new JButton("Play a new game");

	/**
	 * A reference to the configurator window which created us. If this is null then
	 * the current window is not in control of the configurator.
	 * 
	 */
	private final MineSweeper parent;

	/**
	 * Instantiating a window will immediately show it and starts the monitoring and
	 * visualisation process of a given minemap.
	 * 
	 * @param p                  The configurator window
	 * @param allowParentControl Are we allowed to control the configurator?
	 * @param toMonitor          The map to visualise
	 * @param title              The title of this window
	 * @param scale              <i>true</i> enable automatically scaling the spots,
	 *                           <i>false</i> if this feature should be disabled
	 */
	public RestartableGamePanel(final MineSweeper p, final boolean allowParentControl, final MineMap toMonitor,
			final String title, boolean scale) {
		super(toMonitor, title, scale);
		setDisposeAfterRun(false);
		if (allowParentControl) {
			parent = p;
		} else {
			parent = null;
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Creating the top row for the game status/stats
		stats = new JPanel();
		stats.setLayout(new BoxLayout(stats, BoxLayout.LINE_AXIS));
		stats.add(overallStatus);
		Container cp = getContentPane();
		cp.add(stats);
		pack();

		// Creating the bottom row for new game options
		configReturn = new JPanel();
		configReturn.setLayout(new BorderLayout());
		configReturn.add(reconfig, BorderLayout.LINE_START);
		configReturn.add(playAgain, BorderLayout.LINE_END);
		reconfig.addActionListener(this);
		playAgain.addActionListener(this);

		// Ensuring that the configurator window will be visible if we close the window
		addWindowListener(this);
	}

	/**
	 * Ensures the window contains the most up to date look of the MineMap (this is
	 * done with the help of the original {@link SimpleGamePanel#run()} method. Ath
	 * the end of the game, this method also allows starting a new game.
	 */
	@Override
	public void run() {
		final long msbefore = System.currentTimeMillis();

		super.run();

		// The game has ended, we need to refresh the status line on the
		// top of the window

		String finalStats = "<html>";
		if (toMonitor.isWon()) {
			finalStats += won;
		} else {
			finalStats += lost;
		}
		long duration = System.currentTimeMillis() - msbefore;
		finalStats += "<br> Statistics: " + toMonitor.getExploredAreaSize() * 100 / toMonitor.fieldSize + "% explored, "
				+ toMonitor.getCorrectlyIdentifiedMineCount() + "/" + toMonitor.getInCorrectlyIdentifiedMineCount()
				+ " flagged correctly/incorrectly<br> Completed in: " + duration + " ms</html>";
		overallStatus.setText(finalStats);
		stats.remove(overallStatus);
		pack();
		stats.add(overallStatus);
		pack();

		// This window has nothing else to do. We can now try to persuade the user to
		// try again
		// then our visualiser thread can die.
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		if (parent != null) {
			// We are the main game window, we can control the parent
			getContentPane().add(configReturn);
			pack();
		}
	}

	/**
	 * Handles the bottom two buttons on the window that are only visible if the
	 * current game has ended. Allows reconfiguration or a new game with the same
	 * config.
	 */
	@Override
	public void actionPerformed(final ActionEvent arg0) {
		Object source = arg0.getSource();
		if (source == reconfig) {
			dispose();
		} else if (source == playAgain) {
			removeWindowListener(this);
			dispose();
			if (parent != null) {
				parent.launchSolvers();
			}
		}
	}

	@Override
	public void windowActivated(final WindowEvent arg0) {
		// ignore
	}

	/**
	 * Makes sure the configurator window is visible if this window gets closed
	 */
	@Override
	public void windowClosed(final WindowEvent arg0) {
		if (parent != null) {
			parent.setVisible(true);
		}
	}

	@Override
	public void windowClosing(final WindowEvent arg0) {
		// ignore
	}

	@Override
	public void windowDeactivated(final WindowEvent arg0) {
		// ignore
	}

	@Override
	public void windowDeiconified(final WindowEvent arg0) {
		// ignore
	}

	@Override
	public void windowIconified(final WindowEvent arg0) {
		// ignore
	}

	@Override
	public void windowOpened(final WindowEvent arg0) {
		// ignore
	}
}
