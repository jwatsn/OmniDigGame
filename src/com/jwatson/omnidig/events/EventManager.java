package com.jwatson.omnidig.events;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.jwatson.omnidig.Listeners.EventListener;

public class EventManager extends Actor {
//	------------------------------------------------------------------------------------
	private static EventManager instance;
	
	Pool<Event> pool;
	
	
//	------------------------------------------------------------------------------------
//	buffers
	List<Event> eventBuffer;
	List<Event> toProcess;
	
//	------------------------------------------------------------------------------------
//	Listeners
	List<EventListener> listenerBuffer;
//	------------------------------------------------------------------------------------
	long curTick;

	
	public EventManager() {
		if(instance != null)
			instance = null;
		
		instance = this;
		
		createBuffers();
	}
	
	void createBuffers() {
		eventBuffer = new ArrayList<Event>();
		toProcess = new ArrayList<Event>();
		listenerBuffer = new ArrayList<EventListener>();
		
		pool = new Pool<Event>() {
			
			@Override
			protected Event newObject() {
				// TODO Auto-generated method stub
				return new Event();
			}
		};
	}
	
	public static void processEvents(long tick) {
		
		List<Event> events = instance.eventBuffer;
		int counter = 0;
		for(int i=0; i<events.size(); i++) {
			Event e = events.get(i);
			
			
			
			if(e.tick <= tick) {
				
				instance.toProcess.add(e);
				instance.processEvent(e);
			}
		}
		
		instance.eventBuffer.removeAll(instance.toProcess);
		instance.toProcess.clear();
		
		
	}
	
	
	
	void processEvent(Event e) {
		for(int i=0; i<listenerBuffer.size(); i++) {
			EventListener listener = listenerBuffer.get(i);
			listener.handle(e);
			
		}
		e.called();	
	}
	
//	------------------------------------------------------------------------------------
	
	
	
//	------------------------------------------------------------------------------------
	public static void setTick(long tick) { instance.curTick = tick; }
	public static void addEvent(Event event) { instance.eventBuffer.add(event); }
	public static void addListener(EventListener listener) { instance.listenerBuffer.add(listener); }
	public static Event allocateEvent() { return instance.pool.obtain(); }
	
	public static void clearE() {
		instance.eventBuffer.clear();
		instance.listenerBuffer.clear();
	}
}

	