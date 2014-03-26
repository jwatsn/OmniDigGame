package com.jwatson.omnidig.Listeners;

import com.jwatson.omnidig.events.Event;
import com.jwatson.omnidig.events.InputEvent;

public interface EventListener {
	
	public abstract void handle(Event e);

}
