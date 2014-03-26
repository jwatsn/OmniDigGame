package com.jwatson.omnidig.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.BlockUpdateEvent;
import com.jwatson.omnidig.events.EventManager;
import com.jwatson.omnidig.ui.Console;

public class Pickaxe extends ItemType {
	
	float mine_step = 0.1f;
	int mine_distance = 3;
	
	public Pickaxe(int str) {
		desc = "A Pickaxe to mine blocks with";
		ATK = str;
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
		
		float angle = getAngle(obj, x, y);
		
		for(float i=0; i < mine_distance; i+=mine_step) {
			
			float x2 = (float) (((int)obj.bounds.x + 0.5f) + (i * Math.cos(angle)));
			float y2 = (float) (((int)obj.bounds.y + 0.5f) + (i * Math.sin(angle)));
			
			if(OmniTerrain.isBlock((int)x2, (int)y2)) {
				
				BlockUpdateEvent event = BlockUpdateEvent.allocate();
				event.set((Player)obj,OmniWorld.ticks, (int)x2, (int)y2, item.item, 1,BlockUpdateEvent.UpdateType.Damaged);
				EventManager.addEvent(event);
	
				
				break;
			}
			
		}
		
	}

	
	
	
}
