package com.jwatson.omnidig.events;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.jwatson.omnidig.World.WorldObj;

public class TouchEvent extends TargetEvent {

	public long origTick;
	public float x,y;
	static Pool<TouchEvent> pool;
	
	public TouchEvent() {	
		super();
		
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		x=0;
		y=0;
		origTick=0;
		super.reset();
	}
	
	public void set(long ticks, WorldObj target, float x, float y) {
		super.set(ticks, target);
		
		this.x = x;
		this.y = y;
	}

	
	public static TouchEvent allocate() {
		
		if(pool == null) {
			pool = new Pool<TouchEvent>() {

				@Override
				protected TouchEvent newObject() {
					// TODO Auto-generated method stub
					return new TouchEvent();
				}
			};
		}
		
		return pool.obtain();
			
		
	}
	
}
