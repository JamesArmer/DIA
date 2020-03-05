package uk.ac.nott.cs.g53dia.agent;

import uk.ac.nott.cs.g53dia.library.*;
import java.util.Random;


/**
 * A simple example LitterAgent
 * 
 * @author Julian Zappala
 */
/*
 * Copyright (c) 2011 Julian Zappala
 * 
 * See the file "license.terms" for information on usage and redistribution of
 * this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
public class DemoLitterAgent extends LitterAgent {
	Point currentTarget = null;
	Point nearestRecharge = new Point(0,0);
	Point nearestWasteStation = new Point(10000, 10000);

	public DemoLitterAgent() {
		this(new Random());
	}

	/**
	 * The tanker implementation makes random moves. For reproducibility, it
	 * can share the same random number generator as the environment.
	 * 
	 * @param r
	 *            The random number generator.
	 */
	public DemoLitterAgent(Random r) {
		this.r = r;
	}

	/*
	 * The following is a simple demonstration of how to write a tanker. The
	 * code below is very stupid and simply moves the tanker randomly until the
	 * charge agt is half full, at which point it returns to a charge pump.
	 */
	public Action senseAndAct(Cell[][] view, long timestep) {

		if((getCurrentCell(view) instanceof RechargePoint) && (getChargeLevel() < MAX_CHARGE)){
			return new RechargeAction(); //if on a recharge point then recharge if possible
		}else if((getCurrentCell(view) instanceof WasteStation) && (getWasteLevel() > 0)){
			return new DisposeAction();	//dispose of waste
		}else if((getCurrentCell(view) instanceof WasteBin)){ //load waste from bin
			return loadWaste(view); //load waste if possible
		}else if ((getChargeLevel() <= MAX_CHARGE / 2) && !(getCurrentCell(view) instanceof RechargePoint)) {
			return new MoveTowardsAction(nearestRecharge); //move to the nearest recharge station
		}else if(getWasteLevel() > 0){ //find waste station
			return new MoveTowardsAction(nearestWasteStation);
		}else { //find waste bin
			if(currentTarget == null){
				localScan(view);
				return new MoveAction(1);
			}
			return new MoveTowardsAction(currentTarget);
		}
	}

	public void localScan(Cell[][] view){
		for(int i=0;i<VIEW_RANGE;i++) {
			for (int j = 0; j < VIEW_RANGE; j++) {
				if (view[i][j] instanceof RechargePoint) { //check for recharge
					RechargePoint rp = (RechargePoint) view[i][j];
					if (newPointCloser(nearestRecharge, rp.getPoint())) {
						nearestRecharge = rp.getPoint();
					}
				}
				if(view[i][j] instanceof WasteStation){ //check for waste station
					WasteStation ws = (WasteStation) view[i][j];
					if(newPointCloser(nearestWasteStation, ws.getPoint())){
						nearestWasteStation = ws.getPoint();
					}
				}
				if(view[i][j] instanceof WasteBin){
					if (view[i][j] instanceof WasteBin){
						WasteBin wb = (WasteBin) view[i][j];
						WasteTask wt = wb.getTask();
						if(wt != null) {
							Point newPos = wb.getPoint();
							currentTarget = newPos;
						}
					}
				}
			}
		}
	}

	public boolean newPointCloser(Point oldPoint, Point newPoint){
		Point agent = getPosition();
		if(agent.distanceTo(oldPoint) > agent.distanceTo(newPoint)){
			return true;
		}
		return false;
	}

	public Action loadWaste(Cell[][] view){
		WasteBin wb = (WasteBin) getCurrentCell(view);
		WasteTask wt = wb.getTask();
		if((wt != null) && (getWasteCapacity() >= wt.getRemaining())){
			currentTarget = null;
			return new LoadAction(wt);
		}
		return new MoveAction(1);
	}

}
