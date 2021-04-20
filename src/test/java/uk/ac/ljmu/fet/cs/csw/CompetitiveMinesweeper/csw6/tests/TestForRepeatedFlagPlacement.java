package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.csw6.tests;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.Spot;

import java.util.List;
import java.util.Random;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.FinalAreaFlagger;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.onepriority.CoordinatesForSpot;

public class TestForRepeatedFlagPlacement {
	@Test(timeout = 50)
	public void testFlagCompleteList() {
		//create map
		int height = 10;
		int width = 10;
		MineMap myMap = new MineMap(height, width, 0.04, 0); //0.04 ratio is the "Piece of cake" level
		
		List<CoordinatesForSpot> spotsToFlag = new ArrayList<CoordinatesForSpot>(); //list of spots to flag
		Random rand = new Random(); //instance of random class
		for(int i=0; i<3; i++) {
			int rc = rand.nextInt(width);
			int cc = rand.nextInt(height);
			//System.out.println("Random generated coordinates: " + rc+", "+ cc);
			spotsToFlag.add(new CoordinatesForSpot(rc, cc));//generate 3 random coordinates 
		}
		
		FinalAreaFlagger.flagSpots(myMap, spotsToFlag); //flag spots
		
		for(CoordinatesForSpot flaggedSpot : spotsToFlag) {
			
			assertEquals(flaggedSpot.rowCoord + ", " + flaggedSpot.colCoord + " WAS NOT FLAGGED", Spot.FLAG, myMap.getPos(flaggedSpot.rowCoord, flaggedSpot.colCoord).type);
		
			
		}
		//loop through spotsToflag
			//if myMap.getPos(x,y).equals(spot.FLAG) Assert.assetEquals()
			//else Assert.fail()
		
		
	}

}
