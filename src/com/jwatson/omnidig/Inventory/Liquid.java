package com.jwatson.omnidig.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Inventory.ItemType.Swing_Type;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.World.WorldObj;

public class Liquid extends ItemType {

	public Liquid() {
		// TODO Auto-generated constructor stub
		
		desc = "A liquid";
		swing_type = Swing_Type.Rotate;
		rotation_offset = 45;
		rotation_max = 90;
		swing_speed = 0.01f;
		reload_time = 0.1f;
		offset = new Vector2(0.25f,0.25f);
		bounds = new Rectangle(0, 0, 1, 0.5f);
	}

	@Override
	public void OnUse(ItemObject item,WorldObj obj,float x, float y) {
		// TODO Auto-generated method stub
		
		int x2 = (int)(x/OmniTerrain.chunksize);
		int y2 = (int)(y/OmniTerrain.chunksize);
		float x3 = (x%OmniTerrain.chunksize);
		float y3 = (y%OmniTerrain.chunksize);
		
		int id = (int)(x3*2) + (int)(y3*2) * (OmniTerrain.chunksize*2);
		
		
		OmniTerrain.chunks[x2][y2].water_map[id] = Configuration.Water_MaxBlocks;
		
	}

}
