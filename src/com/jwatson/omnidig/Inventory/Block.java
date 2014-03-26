package com.jwatson.omnidig.Inventory;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.jwatson.omnidig.Assets.AssetManager;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.BlockUpdateEvent;
import com.jwatson.omnidig.events.EventManager;

public class Block extends ItemType {

	
	public Block(int maxhp) {

		placeable = true;
		solid = true;
		desc = "A placeable map block.";
		stack_limit = 32;
		swing_type = Swing_Type.Rotate;
		swing_speed = 0.3f;
		deadtime = 0.1f;
		this.offset = new Vector2(0.375f,0.25f);
		this.rotation_offset = 0;
		this.rotation_max = 90;
		scale = 0.5f;
		this.maxhp = maxhp;
		bounds = new Rectangle(0, 0, 1, 1);
		
		damaged_alpha = AssetManager.getWorldTexture("breakanim").split(8, 8)[0];
		
	}
	
	@Override
	public void OnUse(ItemObject item, WorldObj obj,float x, float y) {
		// TODO Auto-generated method stub
		
		if(OmniTerrain.canPlace((int)x, (int)y)) {
			BlockUpdateEvent event = BlockUpdateEvent.allocate();
			event.set((Player)obj,OmniWorld.ticks, (int)x, (int)y, item.item,0, BlockUpdateEvent.UpdateType.Created);
			EventManager.addEvent(event);
			item.stack--;
		}
		
		
	}

}
