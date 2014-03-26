package com.jwatson.omnidig.Listeners;

import com.badlogic.gdx.Gdx;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.BlockUpdateEvent;
import com.jwatson.omnidig.events.Event;
import com.jwatson.omnidig.events.InputEvent;
import com.jwatson.omnidig.events.TouchEvent;

public class BlockUpdateListener implements EventListener {

	

	public BlockUpdateListener() {

	}

	@Override
	public void handle(Event e) {
		
		
		if(e instanceof BlockUpdateEvent) {
		
		
			onUpdate((BlockUpdateEvent)e);

		}
	}
	
	public void onUpdate(BlockUpdateEvent e) {
		
		
		
	}
	

}
