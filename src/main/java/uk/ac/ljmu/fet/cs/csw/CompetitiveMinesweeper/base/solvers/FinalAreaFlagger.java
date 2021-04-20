package uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers;

import java.util.Collections;
import java.util.List;

import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.MineMap;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.Spot;
import uk.ac.ljmu.fet.cs.csw.CompetitiveMinesweeper.base.solvers.onepriority.CoordinatesForSpot;

public class FinalAreaFlagger extends AbstractSolver {
	
	
	@Override
	public void run() {
		// two boilerplate lines that is a strong requirement for almost all solvers
		super.run();
		MineMap theMapToSolve = getMyMap();

	}
	
	public static void flagSpots(MineMap myMap, List<CoordinatesForSpot> spotsToFlag) {
		
		
		for(CoordinatesForSpot spot : spotsToFlag) {
					
			myMap.flagASpot(spot.rowCoord, spot.colCoord);
		
			
		}
		
	}


}
