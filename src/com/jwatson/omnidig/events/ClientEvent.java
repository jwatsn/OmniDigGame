package com.jwatson.omnidig.events;

import com.badlogic.gdx.Gdx;
import com.jwatson.omnidig.netplay.Client;

public class ClientEvent extends Event {

	public ClientEvent() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void called() {
		
		
		
		if(!Client.isSpawned)
			return;
		
		Client.HandleEvent(this);
		
		
		
		super.called();
	}

}

