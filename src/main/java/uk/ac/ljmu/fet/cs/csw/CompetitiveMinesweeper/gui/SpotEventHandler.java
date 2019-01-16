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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.Spot;

/**
 * Offers visual feedback if the user is hovering over a particular spot. Also
 * allows mouse click events to be interpreted as user's wish to pick
 * (left-click) or flag (right-click) a spot.
 * 
 * @author "Gabor Kecskemeti, Department of Computer Science, Liverpool John
 *         Moores University, (c) 2019"
 */
public class SpotEventHandler implements MouseListener {
	/**
	 * When the mouse is over a particular spot, this colour is used to flash the
	 * given spot to give a sense of more interactivity to the user.
	 */
	public static final Color mouseover = new Color(247, 241, 227);

	/**
	 * The map that is to be solved
	 */
	private final MineMap myMap;
	/**
	 * The label that is in the visual grid representation of the map. The grid is
	 * created and maintained by {@link SingleGamePanel}.
	 */
	private FlashableJLabel inGrid;
	/**
	 * The coordinates of the spot that this event handler is responsible for.
	 */
	private final int myRowCoord, myColCoord;

	/**
	 * Allows to customize the event handler and make sure it correctly identifies
	 * with a particular spot in the map.
	 * 
	 * @param myMap    The map to be solved
	 * @param inGrid   The label that represents the spot in the UI created grid.
	 * @param rowCoord The row coordinate of the spot that this handler is
	 *                 responsible for.
	 * @param colCoord The column coordinate of the spot that this handler is
	 *                 responsible for.
	 */
	public SpotEventHandler(final MineMap myMap, final FlashableJLabel inGrid, final int rowCoord, final int colCoord) {
		this.myMap = myMap;
		myRowCoord = rowCoord;
		myColCoord = colCoord;
		this.inGrid = inGrid;
	}

	/**
	 * Sending out the messages to the map to explore it further
	 */
	@Override
	public void mouseClicked(final MouseEvent arg0) {
		arg0.consume();
		final int whichButton = arg0.getButton();
		switch (whichButton) {
		case MouseEvent.BUTTON1:
			// main button clicked, we will pick the spot if it was not flagged before
			if (!Spot.FLAG.equals(myMap.getPos(myRowCoord, myColCoord).type)) {
				myMap.pickASpot(myRowCoord, myColCoord);
			}
			break;
		case MouseEvent.BUTTON3:
			// right button clicked, we will flag the spot
			myMap.flagASpot(myRowCoord, myColCoord);
			break;
		default:
			// ignore
		}
	}

	/**
	 * Highlights the GUI item if we hover over them with a special, short white
	 * flash.
	 */
	@Override
	public void mouseEntered(MouseEvent arg0) {
		inGrid.flash(mouseover, 10);
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// ignore
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// ignore
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// ignore
	}

}
