package com.jwatson.omnidig.events;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.jwatson.omnidig.World.WorldObj;

public class Event implements Poolable {
	
	static Pool<Event> pool;

	public long tick;
	public int index;
	public static int num_events = 0;
	
	
	public Event() {
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		tick = 0;
	}

	
	public void set(long tick) {
		num_events ++;
		index = num_events;
		this.tick = tick;
	}
	
	public static Event allocate() {
		
		if(pool == null) {
			pool = new Pool<Event>() {

				@Override
				protected Event newObject() {
					// TODO Auto-generated method stub
					return new Event();
				}
			};
		}
		
		return pool.obtain();
			
		
	}

	public void called() {
		
	}
	

}
