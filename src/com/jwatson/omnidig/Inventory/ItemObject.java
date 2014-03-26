package com.jwatson.omnidig.Inventory;

import com.jwatson.omnidig.World.WorldObj;

public class ItemObject extends WorldObj {
	

	public ItemObject(Items item, int stack) {
		super();
		
		this.stack = stack;
		this.item = item;
		
		//playAnim(item.name());
		
	}

	public ItemObject(ItemObject item2) {
		this.stack = item2.stack;
		this.item = item2.item;
		//playAnim(item.name());
	}

	@Override
	public void update(float delta) {
		// TODO Auto-generated method stub
		
		if(grounded)
			velX = 0;
		
		super.update(delta);
	}
	
	
}
