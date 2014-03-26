package com.jwatson.omnidig.events;

import com.jwatson.omnidig.World.WorldObj;

public class TargetServerEvent extends ServerEvent {

	public WorldObj target;
	
	public TargetServerEvent() {
		super();
		
		
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
		target = null;
		
		super.reset();
	}
	

	public void set(long tick, WorldObj target) {
		this.target = target;
		super.set(tick);
	}

}
