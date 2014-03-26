package com.jwatson.omnidig.netplay;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.OmniInput;
import com.jwatson.omnidig.OmniInput.input;
import com.jwatson.omnidig.Camera.OmniCam;
import com.jwatson.omnidig.Inventory.ItemObject;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.Terrain.TerrainChunk;
import com.jwatson.omnidig.World.NetObj;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.ui.Console;



public class Client {
	
	
	public class ServerCommand {
		
		public long ticks;
		public int direction;
		public int itemid = -1;
		public float rotation;
		public float x,y;
		public String anim;
		public int id;
		
		
		public ServerCommand(String anim, float x, float y,int direction, long ticks, int itemid, float rotation) {
			
			this.anim = anim;
			this.x = x;
			this.y = y;
			this.direction = direction;
			this.ticks = ticks;
			this.rotation = rotation;
			this.itemid = itemid;
			
		}
		
		public ServerCommand(String anim, float x, float y,int direction, long ticks) {
			
			this.anim = anim;
			this.x = x;
			this.y = y;
			this.direction = direction;
			this.ticks = ticks;
			
		}
		
		public ServerCommand(long ticks, int direction, int itemid) {
			
			this.ticks = ticks;
			this.direction = direction;
			this.itemid = itemid;
			
		}
		
		public ServerCommand() {
			
		}
		
	}
	
	
	
	
	public static Client instance;
	
	public static boolean isConnected;
	public static boolean isSpawned;
	
	//input buffer
	List<OmniInput.input> inputBuffer;
	
	//For strings
	CharsetDecoder charDecoder;
	
	DatagramChannel server;
	
	SocketAddress serverAddress;
	
	ByteBuffer recv;
	ByteBuffer send;
	
	Timer timer;
	
	int ClientID;
	
	int inputIndex;
	

	
	//Server objects
	List<NetObj> netObjects;
	
	public Client() {
		
		instance = this;
		
		timer = new Timer();
		
		
		try {
			
			server = DatagramChannel.open();
			server.configureBlocking(false);
			send = ByteBuffer.allocate(6655);
			recv = ByteBuffer.allocate(6655);
			netObjects = new ArrayList<NetObj>();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public void Connect(String ip) {
		
		//SetUpTimer();
		
		serverAddress = new InetSocketAddress(ip, 9999);
		
		send.putInt(-1);
		send.putChar("j".charAt(0));
		
		Console.AddLine("Trying to connect to server: " + ip + " on port:9999");
		isConnected = true;
	}
	
	
	public void update() {
		
		try {
		
			//Send any pending commands
			if(send.position() > 0) {
				

				
				send.flip();
				server.send(send, serverAddress);
				send.clear();			
				
				
				
			}
			
			
			
			
			recv.clear();
			SocketAddress serv = server.receive(recv);
			
			if(serv != null) {
				
				ReadPacket();
			}
		
		}
		catch(IOException e) {
			Console.AddLine(""+e.getMessage());
		}
	}
	
	
	
	void ReadPacket() {
		
		recv.flip();
		
		
		
		while(recv.remaining() > 0) {
			
			int packet = recv.getInt();
			
			if(packet == -1) {
				
				HandleCommand();

			}
			else {
				Console.AddLine("Broken "+recv.position() + " " +recv.limit());
				break;
			}
			
		}
		

		
	}
	
	void HandleCommand() {
		
		char cmd = recv.getChar();
		
		
		
		if(cmd == "j".charAt(0)) {
			
			
			HandleJoinCommand();
			
		}
		
		if(cmd == "f".charAt(0)) { //full update
			
			
			
			HandleFullUpdate();
			RequestPing();
			
		}
		
		if(cmd == "q".charAt(0)) { //server got key input
			
			HandlePrediction();
			
		}
		
		if(cmd == "p".charAt(0)) { //set position
			
			HandlePositionUpdate();
			
		}
		
		if(cmd == "s".charAt(0)) { // Ping
			
			HandlePingRequest();
			
		}
		
		if(cmd == "r".charAt(0)) { // remove object
			
			HandleRemoveObject();
			
		}
		
		if(cmd == "c".charAt(0)) { //map update
			
			int type = recv.getInt();
			
			if(type == 1)
				HandleMapUpdate();
			else if(type == 2)
				RequestFullUpdate(2);

		}
		
		if(cmd == 'z') {
			HandleInventoryUpdate();
		}
		
		if(cmd == 'v') { // Play swing animation
			HandleObjectFired();
		}
		if(cmd == '1') { //Tile update
			HandleTileUpdate();
		}
		if(cmd == '2') { //Map Damage update
			HandleMapDamageUpdate();
		}
		
	}
	
	int[] KeysToSend = {Keys.A, Keys.S, Keys.D, Keys.W, Keys.BUTTON_A};
	
	public void SendInput(int id,input key) {
		
		int keycode = key.keycode;
		
		boolean flag = false;
		
		for(int key2 : KeysToSend)
			if(keycode == key2 || keycode == key2 * 100)
				flag = true;
		
		if(!flag)
			return;
		
		if(keycode == Keys.BUTTON_A) {
			SendClick(keycode, key.x, key.y);
		}
		else {
		send.putInt(-1);
		send.putChar("i".charAt(0));
		send.putInt(keycode);
		send.putLong(OmniWorld.ticks);
		send.putInt(inputIndex);
		}
		inputIndex++;
		
		
	}
	
	public void SendClick(int keycode, float x, float y) {
		
		send.putInt(-1);
		send.putChar('a');
		send.putLong(OmniWorld.ticks);
		send.putFloat(x);
		send.putFloat(y);
		
	}
	
	
	void HandleFullUpdate() {
		long ticks = recv.getLong();
		
		Console.AddLine("Server at tick:"+ticks);
		OmniWorld.ticks = ticks;
		
		int size = recv.getInt();
		
		
		for(int i=0; i<size; i++) {
			int id = recv.getInt();
			float x = recv.getFloat();
			float y = recv.getFloat();
			float width = recv.getFloat();
			float height = recv.getFloat();

			
			
			
			byte[] anim = new byte[28];
			
			boolean flag = true;
			int counter = 0;
			while(flag) {
				
				byte b = recv.get();
				if(b>0)
				anim[counter] = b;
				else
					flag = false;
				
				counter++;
			}
			
			String a = new String(anim);
			
			

			
			
			boolean newObject = false;
			WorldObj obj2 = null;
			
			for(WorldObj obj : OmniWorld.instance.spawnedObjects)
				if(obj.id == id) {
					newObject = true;
					obj2 = obj;
				}
			
			
			if(!newObject) { //Spawn object 
				
				if(id == ClientID) {
					
					OmniWorld.instance.client = new Player();
					OmniWorld.instance.client.id = id;
					OmniWorld.instance.spawnObject(OmniWorld.instance.client, x, y, 0.60f, 0.8f);
					OmniCam.SetOwner(OmniWorld.instance.client);
					
				}
				else {
				NetObj o = new NetObj();
				o.id = id;
				o.usesPhysics = false;				
				OmniWorld.instance.spawnObject(o, x, y, 1, 1);
				netObjects.add(o);
				o.playAnim(a.trim());				
				}
				
				
				
			}
			else {
				if(obj2 != null) {
					obj2.pos.x = (x);
					obj2.pos.y = (y);
					obj2.bounds.setX(x);
					obj2.bounds.setY(y);
				}
				
			}
		}
		
		isSpawned = true;
	}
	
	void RequestPing() {
		send.putInt(-1);
		send.putChar("s".charAt(0));
		send.putInt(2);
	}
	
	public void SendSelection(int selection) {
		
		if(selection >= 0 && selection < Configuration.QuickBar_Capacity) {
			send.putInt(-1);
			send.putChar('l');
			send.putInt(selection);
		}
		
	}
	
	void RequestFullUpdate(int type) {
		
		
		
		send.putInt(-1);
		send.putChar("f".charAt(0));
		send.putInt(type);
		
	}
	
	public void RequestChunk(int x, int y) {
		
		send.putInt(-1);
		send.putChar("c".charAt(0));
		send.putInt(x);
		send.putInt(y);
		
	}
	
	void HandleRemoveObject() {

		
		
		int id = recv.getInt();
		
		for(NetObj o : netObjects)
			if(o.id == id)
				o.removalFlag = true;
		
	}

	void HandleTileUpdate() {
		int x = recv.getInt();
		int y = recv.getInt();
		int id = recv.getShort();
		long ticks = recv.getLong();
		
		ServerCommand cmd = new ServerCommand();
		cmd.x = x;
		cmd.y = y;
		cmd.itemid = id;
		cmd.ticks = ticks;
		
		//OmniTerrain.instance.blockUpdates.add(cmd);
	}
	
	void HandleMapDamageUpdate() {
		
		int x = recv.getInt();
		int y = recv.getInt();
		byte hp = recv.get();
		
		OmniTerrain.setDamageBlock(x, y, hp);
	}
	
	void HandleMapUpdate() {
		
		int x2 = recv.getInt();
		int y2 = recv.getInt();
		
		TerrainChunk ch = OmniTerrain.chunks[x2][y2];
		
		if(ch == null) {
			
			ch = new TerrainChunk(OmniWorld.instance.Terrain, x2*OmniTerrain.chunksize, y2*OmniTerrain.chunksize, OmniTerrain.chunksize);
		}
		
		for(int x = 0; x<OmniTerrain.chunksize; x++)
			for(int y = 0; y<OmniTerrain.chunksize; y++) {
				
				int blockid = recv.getShort();
				byte hp = recv.get();
				
				ch.map[x][y] = Items.values()[blockid];
				ch.map_damage[x][y] = hp;
				
			}
		
		
		OmniTerrain.chunks[x2][y2] = ch;
		
		OmniTerrain.chunks[x2][y2].Loaded = true;
		
		
	}
	
	void HandleObjectFired() {
		int id = recv.getInt();
		int itemid = recv.getShort();
				
			for(NetObj obj : netObjects) {
				if(obj.id == id) {
					if(itemid >= 0)
					obj.item_firing = Items.values()[itemid];
					return;
				}
			}
		
	}

	void HandlePingRequest() {
		
		long ticks = recv.getLong();
		
		
		send.putInt(-1);
		send.putChar("s".charAt(0));		
		send.putInt(1);
		
	}
	
	void HandlePositionUpdate() {
		int id = recv.getInt();
		long ticks = recv.getLong();
		int direction = recv.getShort();

				float x = recv.getFloat();
				float y = recv.getFloat();
				
				float width = recv.getFloat();
				float height = recv.getFloat();
				
				
				byte[] anim = new byte[28];
				
				boolean flag = true;
				int counter = 0;
				while(flag) {
					
					byte b = recv.get();
					if(b>0)
					anim[counter] = b;
					else
						flag = false;
					
					counter++;
				}
				
				String a = new String(anim);
				
				a.trim();
				int itemid = -1;
				float rotation = -1;

					itemid = recv.getShort();
					rotation = recv.getFloat();

				
				
				if(id == OmniWorld.instance.client.id) {
					
					OmniWorld.instance.client.bounds.x = x;
					OmniWorld.instance.client.bounds.y = y;
					
				}
				else {
					
					for(NetObj o : netObjects)
						if(o.id == id) {
							flag = true;
							if(itemid >= 0) {
								o.serverCommands.add(new ServerCommand(a.trim(), x, y,direction, ticks,itemid,rotation));
							}
							else
							o.serverCommands.add(new ServerCommand(a.trim(), x, y,direction, ticks));
							o.width = width;
							o.height = height;
							o.bounds.width = width;
							o.bounds.height = height;
							return;
						}
					
					NetObj newobj = new NetObj();
					newobj.width = width;
					newobj.height = height;
					newobj.bounds.width = width;
					newobj.bounds.height = height;
					newobj.id = id;
					netObjects.add(newobj);
					OmniWorld.instance.spawnObject(newobj, x, y, 1, 1);
					if(itemid >= 0) {
						newobj.serverCommands.add(new ServerCommand(a.trim(), x, y,direction, ticks,itemid,rotation));
					}
					else
					newobj.serverCommands.add(new ServerCommand(a.trim(), x, y,direction, ticks));
					
				}
				
				
				

						

				
//			}
//		}
	}

	void HandlePrediction() {
		long ticks = recv.getLong();
		int keycode = recv.getInt();
		float x = recv.getFloat();
		float y = recv.getFloat();
		
		//OmniWorld.instance.client.checkPrediction(keycode,ticks,x,y);
	}
	
	void HandleJoinCommand() {
		ClientID = recv.getInt();
		
		
		isConnected = true;
		
		OmniWorld.instance.resetMap();
		RequestFullUpdate(2);
	}
	
	void HandleInventoryUpdate() {
		
		
		
		for(int i=0; i<Configuration.Bag_MaxItems; i++) {
			
			short id = recv.getShort();
			short amount = recv.getShort();
			
			if(id == 0 || amount == 0)
			OmniWorld.instance.client.bagItems[i] = null;
			else {
				ItemObject newobj = new ItemObject(Items.values()[id], amount);
				OmniWorld.instance.client.bagItems[i] = newobj;
			}
			
			
		}
		
	}
	
	public void DropItem(int bagid, int amount) {
		
		send.putInt(-1);
		send.putChar('d');
		send.putShort((short)bagid);
		send.putShort((short)amount);
	}
	
	public void RequestMove(int from, int to, int amt) {
		
		send.putInt(-1);
		send.putChar('x');
		send.putShort((short)from);
		send.putShort((short)to);
		send.putShort((short)amt);
		
	}
	
}
