package com.jwatson.omnidig.events;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.jwatson.omnidig.World.WorldObj;

public class InputEvent extends TargetEvent {

	public int direction;
	public long origTick;
	public float x,y; //for prediction
	static Pool<InputEvent> pool;
	
	public InputEvent() {	
		super();
		
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
		direction = 0;
		super.reset();
	}
	
	public void set(long ticks, WorldObj target, int direction) {
		super.set(ticks, target);
		
		this.direction = direction; 
	}

	
	public static InputEvent allocate() {
		
		if(pool == null) {
			pool = new Pool<InputEvent>() {

				@Override
				protected InputEvent newObject() {
					// TODO Auto-generated method stub
					return new InputEvent();
				}
			};
		}
		
		return pool.obtain();
			
		
	}
	
}
