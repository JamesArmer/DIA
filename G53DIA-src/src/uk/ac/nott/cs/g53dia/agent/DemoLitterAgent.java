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
	Point largePoint = new Point(10000, 10000);
	Point currentTarget = largePoint;
	Point nearestRecharge = new Point(0,0);
	Point nearestWasteStation = largePoint;
	Point nearestRecyclingStation = largePoint;

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
			localScan(view);
			return new RechargeAction(); //if on a recharge point then recharge if possible
		}else if((getCurrentCell(view) instanceof WasteStation) && (getWasteLevel() > 0)) {
			return new DisposeAction();  //dispose of waste if possible
		} else if((getCurrentCell(view) instanceof RecyclingStation) && (getRecyclingLevel() > 0)){
			return new DisposeAction(); //dispose of recycling if possible
		}else if((getCurrentCell(view) instanceof WasteBin) && (getRecyclingLevel() == 0)) {
			localScan(view);
			return loadWaste(view); //load waste if possible
		} else if((getCurrentCell(view) instanceof RecyclingBin) && (getWasteLevel() == 0)){
			localScan(view);
			return loadRecycling(view); //load recycling if possible
		}else if ((getChargeLevel() <= MAX_CHARGE / 2) && !(getCurrentCell(view) instanceof RechargePoint)) {
			return new MoveTowardsAction(nearestRecharge); //move to the nearest recharge station
		}else if(getWasteLevel() > 0) { //find waste station
			localScan(view);
			return new MoveTowardsAction(nearestWasteStation);
		} else if(getRecyclingLevel() > 0){ //find recycling station
			localScan(view);
			return new MoveTowardsAction(nearestRecyclingStation);
		}else { //find waste or recycling bin with task
			if(currentTarget == largePoint){
				localScan(view);
				return new MoveAction(1);
			}
			return new MoveTowardsAction(currentTarget);
		}
	}

	public void localScan(Cell[][] view){
		for(int i=0;i<(VIEW_RANGE*2);i++) {
			for (int j=0;j<(VIEW_RANGE*2);j++) {
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
				if(view[i][j] instanceof  RecyclingStation){ //check for recycling station
					RecyclingStation rs = (RecyclingStation) view[i][j];
					if(newPointCloser(nearestRecyclingStation, rs.getPoint())){
						nearestRecyclingStation = rs.getPoint();
					}
				}
				if(view[i][j] instanceof WasteBin){ //check for waste bin
					WasteBin wb = (WasteBin) view[i][j];
					WasteTask wt = wb.getTask();
					if(wt != null) {
						Point wPos = wb.getPoint();
						if(newPointCloser(currentTarget, wPos)){
							currentTarget = wPos;
						}
					}
				}
				if(view[i][j] instanceof RecyclingBin){ //check for recycling bin
					RecyclingBin rb = (RecyclingBin) view[i][j];
					RecyclingTask rt = rb.getTask();
					if(rt != null){
						Point rPos = rb.getPoint();
						if(newPointCloser(currentTarget, rPos)){
							currentTarget = rPos;
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
			currentTarget = largePoint;
			return new LoadAction(wt);
		}
		return new MoveAction(this.r.nextInt(8));
	}

	public Action loadRecycling(Cell[][] view){
		RecyclingBin rb = (RecyclingBin) getCurrentCell(view);
		RecyclingTask rt = rb.getTask();
		if((rt != null) && (getRecyclingCapacity() >= rt.getRemaining())){
			currentTarget = largePoint;
			return new LoadAction(rt);
		}
		return new MoveAction(this.r.nextInt(8));
	}

}
