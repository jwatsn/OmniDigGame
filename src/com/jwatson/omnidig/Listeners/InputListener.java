package com.jwatson.omnidig.Listeners;

import com.badlogic.gdx.Gdx;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.DamageEvent;
import com.jwatson.omnidig.events.Event;
import com.jwatson.omnidig.events.InputEvent;
import com.jwatson.omnidig.events.TouchEvent;

public class InputListener implements EventListener {


	WorldObj owner;
	public InputListener(WorldObj owner) {
		this.owner = owner;
	}

	@Override
	public void handle(Event e) {
		
		
		
		
		if(e instanceof InputEvent) {
		
		if(((InputEvent)e).target != owner)
			return;
		
			onMove((InputEvent)e);

		}
		else if(e instanceof TouchEvent) {
			if(((TouchEvent)e).target != owner)
				return;
			onTouch((TouchEvent)e);
		}
		else if(e instanceof DamageEvent) {
			
			
			DamageEvent dmg = (DamageEvent)e;
			
			if(owner == dmg.target)
				hit(dmg);
			else if(owner == dmg.dmged)
				onDamaged(dmg);
		}
	}
	
	public void onMove(InputEvent e) {
		
	}
	
	public void onTouch(TouchEvent e) {
		
	}
	
	public void hit(DamageEvent e) {
		
	}
	
	public void onDamaged(DamageEvent e) {
		
	}
	
	

}
