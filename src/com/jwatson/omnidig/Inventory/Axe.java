package com.jwatson.omnidig.Inventory;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jwatson.omnidig.Inventory.ItemType.Swing_Type;
import com.jwatson.omnidig.World.WorldObj;

public class Axe extends ItemType {

	public Axe() {
		// TODO Auto-generated constructor stub
		
		desc = "An axe to chop wood with";
		swing_type = Swing_Type.Rotate;
		rotation_offset = 45;
		rotation_max = 90;
		swing_speed = 0.35f;
		reload_time = 0.1f;
		offset = new Vector2(0.25f,0.25f);
		bounds = new Rectangle(0, 0, 1, 0.5f);
	}

	@Override
	public void OnUse(ItemObject item,WorldObj obj,float x, float y) {
		// TODO Auto-generated method stub
		
	}

}
