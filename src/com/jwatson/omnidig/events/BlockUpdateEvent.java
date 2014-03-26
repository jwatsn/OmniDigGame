package com.jwatson.omnidig.events;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.jwatson.omnidig.Inventory.ItemObject;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Lighting.LightingManager;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.World.WorldObj;

public class BlockUpdateEvent extends ServerEvent {

	public static enum UpdateType {
		Damaged,
		Deleted,
		Created,
		Update
	}
	
	public long origTick;
	public int x,y,hp;
	public Items type;
	public UpdateType updateType;
	public Player owner;
	
	static Pool<BlockUpdateEvent> pool;
	
	public BlockUpdateEvent() {	
		super();
		
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		x=0;
		y=0;
		origTick=0;
		hp = 0;
		
		this.type = null;
		updateType = null;
		owner = null;
		
		super.reset();
	}
	
	public void set(Player owner,long ticks, int x, int y,Items item, int hp, UpdateType updateType) {
		super.set(ticks);
		
		this.x = x;
		this.y = y;
		this.type = item;
		this.hp = hp;
		this.updateType = updateType;
		this.owner = owner;
	}

	
	@Override
	public void called() {
		// TODO Auto-generated method stub
		LightingManager.instance.updateAll();
	}
	
	public static BlockUpdateEvent allocate() {
		
		if(pool == null) {
			pool = new Pool<BlockUpdateEvent>() {

				@Override
				protected BlockUpdateEvent newObject() {
					// TODO Auto-generated method stub
					return new BlockUpdateEvent();
				}
			};
		}
		
		return pool.obtain();
			
		
	}
	
}
