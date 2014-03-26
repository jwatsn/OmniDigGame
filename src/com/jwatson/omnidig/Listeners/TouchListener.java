package com.jwatson.omnidig.Listeners;

import com.badlogic.gdx.Gdx;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.Event;
import com.jwatson.omnidig.events.InputEvent;
import com.jwatson.omnidig.events.TouchEvent;

public class TouchListener implements EventListener {


	WorldObj owner;
	public TouchListener(WorldObj owner) {
		this.owner = owner;
	}

	@Override
	public void handle(Event e) {
		
		
		if(e instanceof TouchEvent) {
		
		if(((TouchEvent)e).target != owner)
			return;
		
			onTouch((TouchEvent)e);

		}
	}
	
	public void onTouch(TouchEvent e) {
		
	}
	

}
