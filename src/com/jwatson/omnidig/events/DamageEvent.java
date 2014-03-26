package com.jwatson.omnidig.events;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.World.WorldObj;

public class DamageEvent extends TargetServerEvent {

	public int dmg;
	public long origTick;
	public float x,y; //dmg position
	static Pool<DamageEvent> pool;
	public WorldObj dmged;
	public Items item;
	
	public DamageEvent() {	
		super();
		
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		dmged = null;
		item = null;
		dmg = 0;
		super.reset();
	}
	
	public void set(long ticks, WorldObj target,WorldObj dmged,Items item, int dmg, float x, float y) {
		super.set(ticks, target);
		
		this.dmged = dmged;
		this.dmg = dmg; 
		this.item = item;
		this.x = x;
		this.y = y;
	}

	
	public static DamageEvent allocate() {
		
		if(pool == null) {
			pool = new Pool<DamageEvent>() {

				@Override
				protected DamageEvent newObject() {
					// TODO Auto-generated method stub
					return new DamageEvent();
				}
			};
		}
		
		return pool.obtain();
			
		
	}
	
}
