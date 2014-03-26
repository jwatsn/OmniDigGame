package com.jwatson.omnidig.events;

import com.jwatson.omnidig.netplay.Server;

public class ServerEvent extends Event {

	public ServerEvent() {
		
	}
	
	@Override
	public void called() {
		// TODO Auto-generated method stub
		
		if(Server.isHosting) {
			Server.instance.HandleEvent(this);
		}
		
	}

}
