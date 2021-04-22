package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers;
//uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.CSW6Solver

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.ExploredSpot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.Spot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.onepriority.CoordinatesForSpot;

public class CSW6Solver extends AbstractSolver {
	/**
	 * Variable used to determine if in looping through all the spots there was an action taken
	 *to know if to keep looping or pick a new spot to continue the game
	 */
	public boolean actionTaken = false;
	
	/**
	 * A spot that can have the potential of having a low probability of being a mine, to use
	 *instead of picking a spot at random
	 */
	CoordinatesForSpot spotToPickWithUncertainty = null;
	float originalProbability = 1; //The probability of that spot being a mine
	
	/**
	 * The algorithm starts with a point at random and then iterate through all the spots 
	 * that have i mines around, starting at 1 and ending at 8
	 */
	@Override
	public void run() {
		// Run and get map to solve
		super.run();
		MineMap myMap = getMyMap(); 
		
		//Iterate while the game is not ended
		mainLoop: do {
			//Pick spot that has a lower probability of being a mine
			if (originalProbability <= 0.5) {
				myMap.pickASpot(spotToPickWithUncertainty.rowCoord, spotToPickWithUncertainty.colCoord);
				System.out.println("\n>>>>>>SPOT PICKED USING PROBABILITY " + originalProbability + " spot: (" + spotToPickWithUncertainty.rowCoord + "," + spotToPickWithUncertainty.colCoord +")");
				spotToPickWithUncertainty = null;
			} else {
				pickAtRandom(myMap);
			}
			
			//exit loop if the spot picked was a mine
			if(myMap.isEnded())
				break mainLoop;

			//Iterate through the spots that have the same nearMine count while actions are being taken
			do {
				//Set action taken to false before it enters the loop which will only be able to set it to true or leave unmodified
				actionTaken = false;
				
				//A spot can have 1 through 8 mines around
				for(int i = 1; i < 9; i++) {
					System.out.println("\n-NEAR MINE COUNT " + i + ": " );
					//Find all the spots that have i mines around
					for (int cc = 0; cc < myMap.cols; cc++) {
						for (int rc = 0; rc < myMap.rows; rc++) {
							ExploredSpot aSpot = myMap.getPos(rc, cc);
							if (Spot.SAFE.equals(aSpot.type)) {
								if (aSpot.nearMineCount == i) {
									System.out.print("(" + rc + ", " + cc +"): ");
									//Search around the spot that has i mines around
									searchAroundCentre(myMap, new CoordinatesForSpot(rc, cc));
									//Break out the loop if the last action taken has ended the game
									if (myMap.isEnded())
										break mainLoop;
								}
							}
						}
					}	
				}
			} while (actionTaken);
			
		} while(!myMap.isEnded());
		
		//Print if the game ended with a win or loss
		if(myMap.isWon()) {
			System.out.println("\nYOU WON!!!!");
		} else {
			System.out.println("\nYOU LOST, EXITING!!!!");
		}
		
		return;
	}
	
	/**
	 * Explores the area around a centre spot and gives the list of unexplored and flagged spots around it to take actions on.
	 * @param aMap The map where the exploration should happen.
	 * @param centre The spot on the map that we have to find the spots that surrounds it.
	 */
	public void searchAroundCentre(MineMap aMap, CoordinatesForSpot centre) {
		ArrayList<CoordinatesForSpot> unexploredSpots = new ArrayList<>();
		ArrayList<CoordinatesForSpot> flaggedSpots = new ArrayList<>();
		//Loop through all the spots around a centre spot
		for (int cc = centre.colCoord - 1; cc < centre.colCoord + 2; cc++) {
			for (int rc = centre.rowCoord - 1; rc < centre.rowCoord + 2; rc++) {
				if (!aMap.checkOutOfRange(rc, cc)) {
					ExploredSpot aSpot = aMap.getPos(rc, cc);
					//Add the spot to the respective list depending on its type
					switch(aSpot.type) {
						case UNEXPLORED:
							unexploredSpots.add(new CoordinatesForSpot(rc, cc));
							break;
						case FLAG:
							flaggedSpots.add(new CoordinatesForSpot(rc, cc));
							break;
					}
				}
			}
		}
		
		//If there are no unexplored spots, there is no action to be taken
		if (unexploredSpots.size() > 0) {
			//If there's i spot(s) to complete the nearMineCount flag the unexplored spots
			if (flaggedSpots.size() + unexploredSpots.size() == aMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount) {
				System.out.println("Complete flagging of the area around a spot (flagged spots: " + flaggedSpots.size() + ", spots to flag: " + unexploredSpots.size() + ")" );
				flagSpots(aMap, unexploredSpots);
				actionTaken = true;//An action has been taken, update the attribute
			} 
			//If flagging has been complete, pick all the rest of unexplored spots around the centre
			else if (flaggedSpots.size() == aMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount) {
				System.out.println("Already Flagged, pick all the spots around it (flagged spots: " + flaggedSpots.size() + ")");
				pickSpots(aMap, unexploredSpots);
				actionTaken = true;//An action has been taken, update the attribute
			} 
			//There's not enough data to take an action
			else {
				System.out.println("No action taken");
				//Set 'spotToPickWithUncertainty' if one has not been set yet, or set a new one with lower probability of being a mine
				if(spotToPickWithUncertainty == null) {
					Collections.shuffle(unexploredSpots);
					spotToPickWithUncertainty = unexploredSpots.get(0);
					
					//Calculate the probability of it being a mine
					originalProbability = ((float) aMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount) / unexploredSpots.size();	
					System.out.println("*Probability: "+ originalProbability +", nearMine: " + aMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount + ", size: " + unexploredSpots.size());
					
				} else {
					//Set new spot if probability is lower
					float newProbability = ((float) aMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount) / unexploredSpots.size();
					System.out.println("*Probability: "+ newProbability +", nearMine: " + aMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount + ", size: " + unexploredSpots.size());
				
					if (newProbability < originalProbability) {
						Collections.shuffle(unexploredSpots);
						spotToPickWithUncertainty  = unexploredSpots.get(0);
						originalProbability = newProbability;
					}
					
				}
			}
			
		} 
		//If there's no unexplored spots, there's no action to be taken
		else {
			System.out.println("No action taken");
		}
		
		
	}
	
	/**
	 * Flags all spots handed over as a list.
	 * @param aMap The map where we have to pick the spots.
	 * @param spotsToPick The list of spots to pick.
	 */
	public void pickSpots(MineMap aMap, List<CoordinatesForSpot> spotsToPick) {
		for (CoordinatesForSpot whatToPick : spotsToPick) {
			aMap.pickASpot(whatToPick.rowCoord, whatToPick.colCoord);
		}
	}
	
	/**
	 * Flags all spots handed over as a list.
	 * @param aMap The map where we have to flag the spots.
	 * @param spotsToFlag The list of spots to flag.
	 */
	public void flagSpots(MineMap aMap, List<CoordinatesForSpot> spotsToFlag) {
		for(CoordinatesForSpot spot : spotsToFlag) {		
			aMap.flagASpot(spot.rowCoord, spot.colCoord);
		}
	}
	
	/**
	 * Searches the map for unexplored spots and picks one randomly.
	 * @param aMap The map to explore and pick on
	 */
	public void pickAtRandom(MineMap aMap) {
		List<CoordinatesForSpot> fullListOfUnexplored = new ArrayList<CoordinatesForSpot>();
		for (int cc = 0; cc < aMap.cols; cc++) {
			for (int rc = 0; rc < aMap.rows; rc++) {
				ExploredSpot aSpot = aMap.getPos(rc, cc);
				if (Spot.UNEXPLORED.equals(aSpot.type)) {
					fullListOfUnexplored.add(new CoordinatesForSpot(rc, cc));
				}
			}
		}
		if (fullListOfUnexplored.size() == 0)
			return;
		Collections.shuffle(fullListOfUnexplored);
		CoordinatesForSpot whatToPick = fullListOfUnexplored.get(0);
		aMap.pickASpot(whatToPick.rowCoord, whatToPick.colCoord);
		System.out.println("\n>>>>>>RANDOM SPOT PICKED");
	}
	
	
	
	
}
