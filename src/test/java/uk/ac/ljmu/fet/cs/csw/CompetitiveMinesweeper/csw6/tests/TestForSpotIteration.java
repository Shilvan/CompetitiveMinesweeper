package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.csw6.tests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.ExploredSpot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.Spot;

import java.util.List;
import java.util.Random;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.FinalAreaFlagger;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.onepriority.CoordinatesForSpot;

public class TestForSpotIteration {
	@Test(timeout = 50)
	public void testIteratedCompleteList() {
		
		//Pick a spot at random
		//doFullAreaRandomPick(theMapToSolve);
		
		MineMap myMap = new MineMap(15, 15, 0.08, 0); //"Piece of cake", "Easy", "Medium", "Hard", "Pro", "Insane", Starting at 0.04 and adding 0.04
		doFullAreaRandomPick(myMap);
		
		int count = 0;
		for(int i = 1; i < 9; i++) {
			
			List<CoordinatesForSpot> sameNearMineCountSpots = searchNearMineCount(myMap, i);
			
			if(sameNearMineCountSpots.isEmpty()) {
				if (i == 8) {
					i = 0;
					doFullAreaRandomPick(myMap);
					
				}
			}
			
			if(myMap.isWon())
				System.out.println("\nYOU WON!!!!");
			
			if(myMap.isEnded())
				return;
			
			
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
		
		if (flaggedSpots.size() + unexploredSpots.size() == myMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount) {
			System.out.println("Complete flagging of the area around a spot (flagged spots: " + flaggedSpots.size() + ", spots to flag: " + unexploredSpots.size() + " , near mine count: " + myMap.getPos(0,0).nearMineCount + ")" );
			FinalAreaFlagger.flagSpots(myMap, unexploredSpots); //flag the unexplored spots
			
			for (CoordinatesForSpot spot : unexploredSpots) {
				assertEquals("(" + spot.rowCoord + ", " + spot.colCoord + ") " + "wasn't flagged", Spot.FLAG, myMap.getPos(spot.rowCoord, spot.colCoord).type);
				
			}
		} else if (flaggedSpots.size() == myMap.getPos(centre.rowCoord, centre.colCoord).nearMineCount && unexploredSpots.size() > 0) {
			System.out.println("Already Flagged, pick al the spots around it (flagged spots: " + flaggedSpots.size() + " , near mine count: " + myMap.getPos(0,0).nearMineCount+")");
			pickAllOnList(myMap, unexploredSpots);//pick all unexplored spots if not equal 0
			
			
			for (CoordinatesForSpot spot : unexploredSpots) {
				System.out.println(myMap.getPos(spot.rowCoord, spot.colCoord).type);
				assertNotSame("(" + spot.rowCoord + ", " + spot.colCoord + ") " + "was not picked being around a centre that's already flagged", Spot.UNEXPLORED, myMap.getPos(spot.rowCoord, spot.colCoord).type);
				
			}
			
			
		} else {
			System.out.println("No action taken");
		}
		
		
	}
	
	
	
	public static void pickAllOnList(MineMap theMapToSolve, List<CoordinatesForSpot> theListofAreasToPickFrom) {
		for (CoordinatesForSpot whatToPick : theListofAreasToPickFrom) {
			theMapToSolve.pickASpot(whatToPick.rowCoord, whatToPick.colCoord);
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
		System.out.println("RANDOM SPOT PICKED");
	}
	
	
	
	public List<CoordinatesForSpot> searchNearMineCount(MineMap aMap, int nearMineCount) {
		System.out.println("\n-NEAR MINE COUNT " + nearMineCount + ": " );
		List<CoordinatesForSpot> fullList = new ArrayList<CoordinatesForSpot>();
		for (int cc = 0; cc < aMap.cols; cc++) {
			for (int rc = 0; rc < aMap.rows; rc++) {
				ExploredSpot aSpot = aMap.getPos(rc, cc);
				if (Spot.SAFE.equals(aSpot.type)) {
					if (aSpot.nearMineCount == nearMineCount) {
						fullList.add(new CoordinatesForSpot(rc, cc));
						//PRINT SPOT
						System.out.print("(" + rc + ", " + cc +"): ");
						//SEARCH AROUND SPOT AND TAKE ACTIONS
						searchAroundCentre(aMap, new CoordinatesForSpot(rc, cc));
					}
				}
			}
		}
		return fullList;
	}
}
