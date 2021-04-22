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
	
	public boolean actionTaken = false;
	CoordinatesForSpot spotToPickWithUncertainty = null;
	float originalProbability = 1;
	
	@Override
	public void run() {
		
		// two boilerplate lines that is a strong requirement for almost all solvers
		super.run();
		
		//Get map
		MineMap myMap = getMyMap(); 
		
		//Iterate while the game is not ended
		mainLoop: do {
			
			//Pick a spot at random to start
			if (originalProbability <= 0.5) {
				myMap.pickASpot(spotToPickWithUncertainty.rowCoord, spotToPickWithUncertainty.colCoord);
				System.out.println("\n>>>>>>SPOT PICKED USING PROBABILITY " + originalProbability + " spot: (" + spotToPickWithUncertainty.rowCoord + "," + spotToPickWithUncertainty.colCoord +")");
				spotToPickWithUncertainty = null;
			} else {
				doFullAreaRandomPick(myMap);
			}
			
			
			if(myMap.isEnded())
				break mainLoop;

			//Iterate through the spots that have the same nearMine count
			do {
				actionTaken = false;
				
				for(int i = 1; i < 9; i++) {
					
					searchNearMineCount(myMap, i);
					
					if(myMap.isEnded())
						break mainLoop;
						
				}
				
				
			} while (actionTaken);
			
			
			
			
			
			
		} while(!myMap.isEnded());
		
		if(myMap.isWon()) {
			System.out.println("\nYOU WON!!!!");
		} else {
			System.out.println("\nYOU LOST, EXITING!!!!");
		}
		
		
	}
	
	
	public void searchAroundCentre(MineMap myMap, CoordinatesForSpot centre) {
		
		ArrayList<CoordinatesForSpot> unexploredSpots = new ArrayList<>();
		ArrayList<CoordinatesForSpot> flaggedSpots = new ArrayList<>();
		
		
		for (int cc = centre.colCoord - 1; cc < centre.colCoord + 2; cc++) {
			for (int rc = centre.rowCoord - 1; rc < centre.rowCoord + 2; rc++) {
				if (!myMap.checkOutOfRange(rc, cc)) {
					ExploredSpot aSpot = myMap.getPos(rc, cc);
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
		
		
		
		if (unexploredSpots.size() > 0) {
			if (flaggedSpots.size() + unexploredSpots.size() == myMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount) {
				System.out.println("Complete flagging of the area around a spot (flagged spots: " + flaggedSpots.size() + ", spots to flag: " + unexploredSpots.size() + ")" );
				flagSpots(myMap, unexploredSpots); //flag the unexplored spots
				
				if(myMap.isWon()) {
					System.out.println("\nYOU WON!!!!");
				} else { 
					if (myMap.isEnded()) {
						System.out.println("\nYOU LOST!!!!");
					}
				}
				actionTaken = true;
				
			} else if (flaggedSpots.size() == myMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount) {
				System.out.println("Already Flagged, pick all the spots around it (flagged spots: " + flaggedSpots.size() + ")");
				pickAllOnList(myMap, unexploredSpots);//pick all unexplored spots if not equal 0
				
				actionTaken = true;
				
			} else {
				System.out.println("No action taken");
				
				
				//If there's no action taken after looping through 1-8 nearMineCount pick 'spotToPickWithUncertainty' instead of a random spot with 
				if(spotToPickWithUncertainty == null) {
					Collections.shuffle(unexploredSpots);
					spotToPickWithUncertainty = unexploredSpots.get(0);
					originalProbability = ((float) myMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount) / unexploredSpots.size();	
					System.out.println("*Probability: "+ originalProbability +", nearMine: " + myMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount + ", size: " + unexploredSpots.size());
					
				} else {
					
					float newProbability = ((float) myMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount) / unexploredSpots.size();
				
					System.out.println("*Probability: "+ newProbability +", nearMine: " + myMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount + ", size: " + unexploredSpots.size());
					
					if (newProbability < originalProbability) {
						Collections.shuffle(unexploredSpots);
						spotToPickWithUncertainty  = unexploredSpots.get(0);
						originalProbability = newProbability;
					}
					
					
				}
			}
			
		} else {
			System.out.println("No action taken");
		}
		
		
		return;
		
		
	}
	
	
	
	public void pickAllOnList(MineMap theMapToSolve, List<CoordinatesForSpot> theListofAreasToPickFrom) {
		for (CoordinatesForSpot whatToPick : theListofAreasToPickFrom) {
			theMapToSolve.pickASpot(whatToPick.rowCoord, whatToPick.colCoord);
		}
	}
	
	
	public void flagSpots(MineMap myMap, List<CoordinatesForSpot> spotsToFlag) {
		for(CoordinatesForSpot spot : spotsToFlag) {		
			myMap.flagASpot(spot.rowCoord, spot.colCoord);
		}
	}
	
	
	public void doFullAreaRandomPick(MineMap theMapToSolve) {
		List<CoordinatesForSpot> fullListOfUnexplored = new ArrayList<CoordinatesForSpot>();
		for (int cc = 0; cc < theMapToSolve.cols; cc++) {
			for (int rc = 0; rc < theMapToSolve.rows; rc++) {
				ExploredSpot aSpot = theMapToSolve.getPos(rc, cc);
				if (Spot.UNEXPLORED.equals(aSpot.type)) {
					fullListOfUnexplored.add(new CoordinatesForSpot(rc, cc));
				}
			}
		}
		
		if (fullListOfUnexplored.size() == 0)
			return;
		Collections.shuffle(fullListOfUnexplored);
		CoordinatesForSpot whatToPick = fullListOfUnexplored.get(0);
		theMapToSolve.pickASpot(whatToPick.rowCoord, whatToPick.colCoord);
		System.out.println("\n>>>>>>RANDOM SPOT PICKED");
	}
	
	
	
	public void searchNearMineCount(MineMap aMap, int nearMineCount) {
		
		
		System.out.println("\n-NEAR MINE COUNT " + nearMineCount + ": " );
		List<CoordinatesForSpot> fullList = new ArrayList<CoordinatesForSpot>();
		for (int cc = 0; cc < aMap.cols; cc++) {
			for (int rc = 0; rc < aMap.rows; rc++) {
				ExploredSpot aSpot = aMap.getPos(rc, cc);
				if (Spot.SAFE.equals(aSpot.type)) {
					if (aSpot.nearMineCount == nearMineCount) {
						fullList.add(new CoordinatesForSpot(rc, cc));
						System.out.print("(" + rc + ", " + cc +"): ");
						
						//SEARCH AROUND SPOT AND TAKE ACTIONS
						searchAroundCentre(aMap, new CoordinatesForSpot(rc, cc));
					}
				}
			}
		}
		
		return;
	}
}
