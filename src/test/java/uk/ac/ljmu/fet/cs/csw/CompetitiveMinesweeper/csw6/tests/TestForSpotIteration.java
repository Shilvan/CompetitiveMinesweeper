package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.csw6.tests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
		
		MineMap myMap = new MineMap(15, 15, 0.16, 0); //"Piece of cake", "Easy", "Medium", "Hard", "Pro", "Insane", Starting at 0.04 and adding 0.04
		doFullAreaRandomPick(myMap);
		
		int count = 0;
		for(int i = 1; i < 9; i++) {
			int nearMine = i;
			boolean spotPicked = false;
			List<CoordinatesForSpot> sameNearMineCountSpots = searchNearMineCount(myMap, i);
			if(sameNearMineCountSpots.isEmpty()) {
				if (i == 8) {
					i = 0;
					doFullAreaRandomPick(myMap);
					spotPicked =true;
					
				}
			} else {
				count +=1;
				//NOT EMPTY
				
			}
			
			System.out.print("\n-NEAR MINE COUNT " + nearMine + ": " );
			for(CoordinatesForSpot spot : sameNearMineCountSpots) {
				System.out.print("(" + spot.rowCoord + ", " + spot.colCoord + "), ");
				
			}
		
			if (spotPicked)
				System.out.println("\n\nSpot picked");
				
			if(myMap.isEnded())
				return;
			
			
			//allOnes.isEmpty()
			//Collections.shuffle(allOnes);
			
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
	}
	
	
	
	public List<CoordinatesForSpot> searchNearMineCount(MineMap aMap, int nearMineCount) {
		List<CoordinatesForSpot> fullList = new ArrayList<CoordinatesForSpot>();
		for (int cc = 0; cc < aMap.cols; cc++) {
			for (int rc = 0; rc < aMap.rows; rc++) {
				ExploredSpot aSpot = aMap.getPos(rc, cc);
				if (Spot.SAFE.equals(aSpot.type)) {
					if (aSpot.nearMineCount == nearMineCount) {
						fullList.add(new CoordinatesForSpot(rc, cc));
					}
				}
			}
		}
		return fullList;
	}
}
