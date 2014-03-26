package com.jwatson.omnidig.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.ui.Console;

public class Sword extends ItemType {
	
	
	public Sword(int ATK) {
		desc = "A sword to fight with";
		
		this.ATK = ATK;
		
		swing_type = Swing_Type.Thrust;
		rotation_offset = -45;
		rotation_max = 0.5f;
		swing_speed = 0.35f;
		reload_time = 0.3f;
		scale = 0.75f;
		offset = new Vector2(0.25f,0.25f);
		bounds = new Rectangle(0, 0, 1.6f, 0.5f);
		width = 1;
		height = 0.5f;
	}

	@Override
	public void OnUse(ItemObject item,WorldObj obj,float x, float y) {
		
		
	}

	
	
	
}
