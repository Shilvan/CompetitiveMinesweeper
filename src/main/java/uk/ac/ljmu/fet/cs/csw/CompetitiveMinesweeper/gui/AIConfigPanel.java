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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * This class offers the middle (AI specific) section of the configuration
 * panel. It allows to configure two AIs and their speed.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class AIConfigPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 4402385903158103537L;
	// Helper objects to visualise the gui
	private final JLabel singleQuestion = new JLabel("Two AI?");
	private final JLabel aiSpeed = new JLabel("AI speed config:");
	private final JLabel firstAIQuestion = new JLabel("1st AI class:");
	private final JLabel secondAIQuestion = new JLabel("2nd AI class:");
	private final JPanel classNamePanel = new JPanel();
	private final JPanel baseConfig = new JPanel();
	private final JPanel sliderPanel = new JPanel();

	// Allows control of the parent window
	final MineSweeper parent;

	// GUI objects containing the configuration.
	/**
	 * Checkbox to show if we want one or two AIs. If this is selected then two AIs
	 * are needed.
	 */
	final JCheckBox singleaicheck = new JCheckBox();
	/**
	 * The textfield to contain the class name of the first competing AI
	 */
	final JTextField cl1 = new JTextField(100);
	/**
	 * The textfield to contain the class name of the second competing AI. Note that
	 * this does not necessarily have to be filled and its correctness is only
	 * checked if the tickbox {@link #singleaicheck} is picked.
	 */
	final JTextField cl2 = new JTextField(100);
	/**
	 * The slider that allows to set the speed of the AIs. The value of this gets
	 * propagated to
	 * {@link uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap#uidelay
	 * uidelay} in MineMap.
	 */
	final JSlider speedSlider = new JSlider(JSlider.VERTICAL, 0, 1200, 100);

	/**
	 * Composes the AI config panel and initiates all the GUI objects needed to
	 * interact with it.
	 * 
	 * @param swp the minesweeper window. We need to pass it so this config panel
	 *            can readjust the window size and layout if it changes the visible
	 *            components of the window.
	 */
	public AIConfigPanel(MineSweeper swp) {
		parent = swp;
		// The base layout of all the panels
		setLayout(new BorderLayout());
		add(baseConfig, BorderLayout.NORTH);
		add(sliderPanel, BorderLayout.LINE_END);
		add(classNamePanel, BorderLayout.CENTER);

		// Our only interaction option with this panel which requires special handling
		// is the disabling/enabling operation of a second AI. The possible handling of
		// this action is registered here.
		singleaicheck.addActionListener(this);

		// Top of the window question on single AI or competition.
		baseConfig.setLayout(new BoxLayout(baseConfig, BoxLayout.LINE_AXIS));
		baseConfig.add(singleQuestion);
		baseConfig.add(singleaicheck);

		// This panel will contain the AI speed slider on the right, next to the GO
		// button (visualised by the MineSweeper.java).
		sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));
		sliderPanel.add(aiSpeed);
		// The slider's configuration is done here
		speedSlider.setMajorTickSpacing(50);
		speedSlider.setPaintTicks(true);
		// This hashtable enables an easier understanding of the speed slider's settings
		// and its implications. If the speed is set to 0, then the AI's are not
		// bounded. If the speed is set to be around the screen refresh time of the
		// SingleGamePanel, then the GUI will be able to keep up with the AI.
		Hashtable<Integer, JLabel> tickSpec = new Hashtable<>();
		tickSpec.put(0, new JLabel("Unlimited"));
		tickSpec.put(100, new JLabel("Nice visual"));
		tickSpec.put(1200, new JLabel("Personlike"));
		speedSlider.setLabelTable(tickSpec);
		speedSlider.setPaintLabels(true);
		sliderPanel.add(speedSlider);

		// The classname panel. Note this is only configure with one classname
		// initially. The user has to pick the singleaicheck checkbox to allow the
		// second AI to be configured.
		classNamePanel.setLayout(new BoxLayout(classNamePanel, BoxLayout.PAGE_AXIS));
		classNamePanel.add(firstAIQuestion);
		classNamePanel.add(cl1);
	}

	/**
	 * Handling of the change in status for the {@link #singleaicheck} checkbox.
	 * This method mainly deals with making sure the second AI's configuration
	 * option is shown or hidden.
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (singleaicheck.isSelected()) {
			classNamePanel.add(secondAIQuestion);
			classNamePanel.add(cl2);
			parent.pack();
		} else {
			classNamePanel.remove(secondAIQuestion);
			classNamePanel.remove(cl2);
			parent.pack();
		}

	}

}
