package com.jwatson.omnidig.Player;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Inventory.ItemObject;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.InputEvent;
import com.jwatson.omnidig.netplay.Server;
import com.jwatson.omnidig.ui.Console;

public class PlayerClient extends Player {
	
	public static int TOTAL_CLIENTS;

	
	public Stack<Vector2> chunksToSend;
	
	public ByteBuffer cmds;
	
	public long lastInputTick;
	
	public long ping;
	public long pingbuf; //holds the tick when the ping was sent
	public int ping_attempts; //kick the client if this is too high
	
	public long ticks;
	
	public long delay; // delay in ticks. 10 ticks * 30 ms = 300 ms
	
	public boolean ready;
	
	public PlayerClient(SocketAddress sender) {
		super();
		
		
		this.socket = sender;
		
		cmds = ByteBuffer.allocate(6655);
		chunksToSend = new Stack<Vector2>();
		
	}
	

	public void update() {
		
		
		if(!chunksToSend.isEmpty()) {
			
			Vector2 chunkPos = chunksToSend.pop();
			SendChunk((int)chunkPos.x,(int)chunkPos.y);
			
		}
		

	}
	
	void SendChunk(int x2, int y2) {
		
		
		cmds.putInt(-1);
		cmds.putChar("c".charAt(0));
		cmds.putInt(1);
		cmds.putShort((short)OmniTerrain.instance.biomes[x2][y2].ordinal());
		cmds.putInt(x2);
		cmds.putInt(y2);
		
		for(int x=0; x<OmniTerrain.chunksize; x++) 
			for(int y=0; y<OmniTerrain.chunksize; y++) {				
				cmds.putShort((short)OmniTerrain.chunks[x2][y2].map[x][y].ordinal()); //block item
				cmds.put(OmniTerrain.chunks[x2][y2].map_damage[x][y]); //dmg
				cmds.put(OmniTerrain.chunks[x2][y2].ambient[x][y]); //light info
			}
	}
	
	public void UpdateInventory() {
		
		cmds.putInt(-1);
		cmds.putChar('z');
		for(int i=0; i<Configuration.Bag_MaxItems; i++) {
			if(bagItems[i] == null) {
				cmds.putShort((short)0);
				cmds.putShort((short)0);
			}
			else {
				cmds.putShort((short)bagItems[i].item.ordinal());
				cmds.putShort((short)bagItems[i].stack);
			}
			
			
		}
		
	}
	
	public void SendTileUpdate(int id,int x, int y,int hp, Items item) {
		
		cmds.putInt(-1);
		cmds.putChar('1');
		cmds.putShort((short)id);
		cmds.putInt(x);
		cmds.putInt(y);
		cmds.putShort((short)item.ordinal());	
		cmds.putLong(OmniWorld.ticks + delay + Server.NET_DELAY);
		cmds.put((byte)hp);
		
	}
	
	
	public void SendSwingCommand(int id, Items item) {
		cmds.putInt(-1);
		cmds.putChar('v');
		cmds.putInt(id);
		if(item != null)
			cmds.putShort((short)item.ordinal());
		else
			cmds.putShort((short)-1);
	}
	
	public void UpdateMapDamage(int x, int y, byte hp) {
		
		cmds.putInt(-1);
		cmds.putChar('2');
		cmds.putLong(OmniWorld.ticks + (Server.NET_DELAY + delay));
		cmds.putInt(x);
		cmds.putInt(y);
		cmds.put(hp);
		
	}

	@Override
	public void onCollision(WorldObj obj) {
		// TODO Auto-generated method stub
		super.onCollision(obj);
		
		if(obj.item != null)
			UpdateInventory();
	}

	@Override
	public void Move(InputEvent e) {
		
		
		super.Move(e);
		cmds.putInt(-1);
		cmds.putChar('q');
		cmds.putLong(e.origTick);
		cmds.putInt(e.direction);
		cmds.putFloat(e.x);
		cmds.putFloat(e.y);
		cmds.putFloat(accel.x);
		cmds.putFloat(accel.y);
		cmds.putFloat((float)velX);
		cmds.putFloat((float)velY);
		
	}
	
	public void sendDmgText(float x, float y, int amount) {
		cmds.putInt(-1);
		cmds.putChar('$'); 
		cmds.putShort((short)amount);
		cmds.putFloat(x);
		cmds.putFloat(y);
	}
	

	public void handleDamage(WorldObj obj, Items item) {
		
		
		if(!is_stunned) {
		cmds.putInt(-1);
		cmds.putChar('#'); //damaged
		cmds.putShort((short)obj.id);
		cmds.putShort((short)obj.direction);
		cmds.putLong(OmniWorld.ticks + delay + Server.NET_DELAY);
		cmds.putShort((short)item.ordinal());
		cmds.putShort((short)HP);
		cmds.putFloat(bounds.x);
		cmds.putFloat(bounds.y);
		}
	}
	
}
