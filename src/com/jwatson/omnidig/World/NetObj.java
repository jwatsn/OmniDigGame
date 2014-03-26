package com.jwatson.omnidig.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.netplay.Client;
import com.jwatson.omnidig.netplay.Client.ServerCommand;
import com.jwatson.omnidig.ui.Console;

public class NetObj extends WorldObj {
	
	public List<ServerCommand> serverCommands;
	
	ServerCommand nextCommand;
	
	public Items item_firing;
	public boolean cancle_fire;
	
	public NetObj() {
		
		usesPhysics = false;
		serverCommands = new ArrayList<ServerCommand>();
		netObj = true;
		
	}
	ServerCommand last_next;
	ServerCommand last_prev;
	
	int b;
	
	Vector2 prevPos = new Vector2();
	Vector2 nextPos = new Vector2();
	@Override
	public void update(float delta) {
		// TODO Auto-generated method stub
		
		
		super.update(delta);
		
		HandleInterpolation();
		
		
	}
	
	void checkFiring() {
		
		if(animName.charAt(animName.length()-1) == '#') {
			
			if(item_next != item_firing) {
				PlayItemAnim(item_firing);
			}
			
		}
		else {
			item_next = null;
			item_inuse = null;
		}
		
	}
	
	void HandleInterpolation() {
		ServerCommand next = null;
		ServerCommand prev = null;
		ServerCommand target = null;
		
		
		
		//Look for update
		for(int i=0; i < serverCommands.size()-1; i++) {			
			next = serverCommands.get(i+1);			
			prev = serverCommands.get(i);
				
			if(next.ticks > OmniWorld.ticks && prev.ticks <= OmniWorld.ticks && next.ticks - prev.ticks <= 2) {
				target = next;
				b = 0;
				break;
			}	
		}
		b++;
		//Interpolate
		if(target != null) {
			
			long diff = next.ticks - OmniWorld.ticks;
			long maxdiff = next.ticks - prev.ticks;
			
			float a = ((float)diff/(float)maxdiff);
			
			
			prevPos.set(prev.x,prev.y);
			nextPos.set(next.x,next.y);
			
			prevPos.lerp(nextPos, 1-a);
			
			bounds.x = prevPos.x;
			bounds.y = prevPos.y;
			
			playAnim(next.anim);
			direction = next.direction;
			
			if(target.itemid > 0) {
				
				if(item_inuse == null)
					item_inuse = Items.values()[target.itemid];
				
				float rotation = prev.rotation + (next.rotation - prev.rotation) * (1-a);
				
				item_usetimer = 0;
				item_rotation = rotation;
			}
			else
			{
				item_inuse = null;
			}
			
		}
		else if(next != null) {
			
			if(OmniWorld.ticks > next.ticks) {
			bounds.x = next.x;
			bounds.y = next.y;
			playAnim(next.anim);
			direction = next.direction;
			
			}			
			
		}
		
		
			
	
		
		
		
		
		last_next = next;
		
		if(serverCommands.size() > 60) {
			serverCommands.remove(0);
			serverCommands.remove(1);
		}
	}

}
