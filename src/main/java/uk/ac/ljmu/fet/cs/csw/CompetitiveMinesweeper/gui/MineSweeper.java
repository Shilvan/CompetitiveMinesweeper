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
import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.HumanSolver;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.interfaces.GameSolverThread;

/**
 * This is the main window of the Mine sweeper GUI. It allows configuring at
 * most 2 AIs with the same map so they can compete against each other. The
 * configuration window allows changing the size of the mine sweeping grid, the
 * difficulty level and the AI classes as well as the speed of the AIs. It also
 * allows to start a human facing GUI.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class MineSweeper extends JFrame implements ActionListener {
	private static final long serialVersionUID = -879006114019674131L;

	// The list of difficulties (in terms of mined area to total area ratio)
	public static final double[] mineRatios = new double[] { 0.04, 0.08, 0.12, 0.16, 0.20, 0.24 };
	// List of difficulties to be shown to the users
	public static final String[] levels = new String[] { "Piece of cake", "Easy", "Medium", "Hard", "Pro", "Insane" };

	/**
	 * The application icon to be used for the windows of this minesweeper
	 */
	public static final Image mineswicon;

	static {
		// This static block just loads up the icon from the resources
		try {
			mineswicon = ImageIO.read(MineSweeper.class.getResource("/MineSweeperIcon.png"));
		} catch (IOException ioex) {
			throw new RuntimeException(ioex);
		}
	}

	// Main configuration elements
	private final JTextField width = new JTextField(3);
	private final JTextField height = new JTextField(3);
	private final JCheckBox aicheck = new JCheckBox();
	// AI related configuration is handled in a different class
	private final AIConfigPanel aiconfig = new AIConfigPanel(this);
	private final JComboBox<String> level = new JComboBox<>(levels);
	// This is the button which allows the starting of the minesweeping action.
	private final JButton lb = new JButton("GO!");

	private final ArrayList<RestartableGamePanel> currentGuis = new ArrayList<>();

	/**
	 * Constructs and shows the main window with a very simple minesweeping
	 * configuration UI.
	 */
	public MineSweeper() {
		super("Configurator for the competitive minesweeper GUI");
		lb.addActionListener(this);
		setIconImage(mineswicon);

		// Adds the necessary UI components and lays them out
		Container cp = getContentPane();
		JPanel leftPanel = new JPanel();
		cp.add(leftPanel, BorderLayout.LINE_START);
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		leftPanel.add(new JLabel("Basic config:"));
		leftPanel.add(new JLabel("Width:"));
		leftPanel.add(width);
		leftPanel.add(new JLabel("Height:"));
		leftPanel.add(height);
		leftPanel.add(level);
		leftPanel.add(new JLabel("AI?"));
		leftPanel.add(aicheck);
		aicheck.addActionListener(this);
		cp.add(lb, BorderLayout.LINE_END);

		// Makes sure that closing the configurator window terminates the while
		// minesweeper application
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Allows unified parsing of the minefield's dimensions (width and height). If
	 * there is some parsing error, the textfield's background is set to red to
	 * highlight it to the user.
	 * 
	 * @param source the JTextField which contains the dimension value as text
	 * @return the dimension value parsed
	 * @throws NumberFormatException if there was a parsing error (e.g., the
	 *                               textfield contained something that is not a
	 *                               number) or if the dimension value was out of
	 *                               acceptable range.
	 */
	private int parseDimensionValue(final JTextField source) throws NumberFormatException {
		try {
			// Parsing the textfield's contents
			int dimension = Integer.parseInt(source.getText().trim());
			// Parse success, let's see if we are within acceptable dimensions
			if (dimension < 5 || dimension > 150)
				throw new NumberFormatException();
			// Everything is fine let's show it and return the parsed value to the
			// configurator
			source.setBackground(Color.WHITE);
			return dimension;
		} catch (NumberFormatException nex) {
			// There were some issues with the input, lets highlight it to the user
			source.setBackground(Color.RED);
			throw nex;
		}
	}

	/**
	 * Simple wrapper around the configured AI class's loader. Allows to hide the
	 * exception handling mechanism as well as the error propagation to the UI.
	 * 
	 * @param source The TextField which is expected to contain a class's fully
	 *               qualified name (package name included)
	 * @return A ready to be instantiated class.
	 * @throws Exception If there were any issues with finding the given class.
	 */
	private Class<? extends GameSolverThread> loadReferredClass(final JTextField source) throws Exception {
		try {
			source.setBackground(Color.WHITE);
			// Lookup of the classname typed in the textfield
			return (Class<? extends GameSolverThread>) Class.forName(source.getText().trim());
		} catch (Exception e) {
			// There was a class loading error, let's show it to the user.
			source.setBackground(Color.RED);
			throw e;
		}
	}

	/**
	 * This method checks for the correctness of the configuration and if everything
	 * is fine, it creates a to be solved map then launches the necessary GUIs and
	 * AIs. If the method is not called for the first time, it also disposes all
	 * previous GUIs.
	 * 
	 * Note that this method is used from {@link RestartableGamePanel} when a new
	 * game needs to be launched.
	 */
	void launchSolvers() {
		// Handling the reminders of the previous UIs.
		if (!currentGuis.isEmpty()) {
			for (RestartableGamePanel gui : currentGuis) {
				gui.dispose();
			}
			currentGuis.clear();
		}

		int widthValue = 0, heightValue = 0;
		double ratio = -1;
		try {
			// Note: all operations here can throw an exception and disable the launching of
			// the solvers

			// Checking the inputs, first the map related ones so we can instantiate a map
			widthValue = parseDimensionValue(width);
			heightValue = parseDimensionValue(height);
			for (int i = 0; i < levels.length; i++) {
				if (levels[i].equals(level.getSelectedItem())) {
					ratio = mineRatios[i];
					break;
				}
			}

			// Detecting the desired AI solver configuration

			// First, we assume the config is about launching a single gui for humans...
			Class<? extends GameSolverThread> competitor1 = HumanSolver.class;
			Class<? extends GameSolverThread> competitor2 = null;
			int delay = 0;
			if (aicheck.isSelected()) {
				// AI was needed
				delay = aiconfig.speedSlider.getValue();
				competitor1 = loadReferredClass(aiconfig.cl1);
				if (aiconfig.singleaicheck.isSelected()) {
					// Second AI is needed
					competitor2 = loadReferredClass(aiconfig.cl2);
				}
			}
			// If we reach this point, the configuration is correct
			try {
				// We create our map to be used by the competitors
				MineMap newMap = new MineMap(heightValue, widthValue, ratio, delay);
				// Launch the competitors (the second is optional)
				launchCompetitor(competitor1, newMap, false);
				if (competitor2 != null) {
					launchCompetitor(competitor2, newMap, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Solver could not be initiated");
				System.exit(1);
			}
		} catch (Exception e) {
			// ignore.. already handled, we just need to finish with the action
		}

	}

	/**
	 * Handling the GO button and the AI checkbox
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == lb) {
			// Go was pressed, lets launch the corresponding GUIs
			launchSolvers();
		} else if (arg0.getSource() == aicheck) {
			// Change in AI configuration, we either need to add or remove the ai
			// configuration panel
			if (aicheck.isSelected()) {
				getContentPane().add(aiconfig);
				MineSweeper.this.pack();
			} else {
				getContentPane().remove(aiconfig);
				MineSweeper.this.pack();
			}
		}
	}

	/**
	 * Clones a map, instantiates an AI and configures its GUI, launches the AI's
	 * solver thread, then finally it makes the configuration window disappear. If
	 * there are issues with instantiating the solver class, there could be all
	 * kinds of exceptions thrown.
	 * 
	 * @param comp  The class of the AI solver.
	 * @param base  The base map to solve
	 * @param shift Is this the second window?
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private void launchCompetitor(final Class<? extends GameSolverThread> comp, final MineMap base, final boolean shift)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		GUIHelper.launchCompetitor(comp, new GUIHelper.LaunchGUI() {
			@Override
			public SimpleGamePanel launch(final MineMap map, final String title, final boolean scale) {
				RestartableGamePanel guiToReturn = new RestartableGamePanel(MineSweeper.this, !shift, map, title,
						scale);
				currentGuis.add(guiToReturn);
				return guiToReturn;
			}
		}, base, shift ? 1 : 0, 0, true);
		setVisible(false);
	}

	/**
	 * The main method to start the application with
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new MineSweeper();
	}
}
