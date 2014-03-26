package com.jwatson.omnidig.netplay;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer.Task;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.OmniInput;
import com.jwatson.omnidig.OmniInput.input;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.Player.PlayerClient;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.Terrain.TerrainChunk;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.ui.Console;

public class Server {
	
	public static boolean isHosting;
	
	public static int NET_DELAY = 7;
	
	public static int PING_DELAY = 100;
	
	public static Server instance;
	
	
	int pingTimer;
	
	//input buffer
	List<OmniInput.input> inputBuffer;
	
	DatagramChannel server;
	
	//Thread for accepting connections
	Thread listener;
	
	//List of sockets connected
	public Map<SocketAddress,PlayerClient> Clients;
	
	
	//Recieving buffer
	ByteBuffer recv;
	
	//Input reader
	
	boolean started;
	
	public Server() {
		
		instance = this;
		
		Clients = new HashMap<SocketAddress,PlayerClient>();
		
		
	}
	
	public void StartServer() {
		try {
			
			inputBuffer = new ArrayList<OmniInput.input>();
			recv = ByteBuffer.allocate(66655);
			server = DatagramChannel.open();
			server.socket().bind(new InetSocketAddress(9999));
			Console.AddLine("Hosting server on port: 9999");
			server.configureBlocking(false);
			
			started = true;
			//SetUpTimer();
			isHosting = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Console.AddLine(e.getMessage());
		}
	}
	

	float lastUpdate;
	public void update() {
		
		if(!started)
			return;
		
		
		
		try {
			
			recv.clear();
			SocketAddress sender = server.receive(recv);
			
			
			
			if(sender != null) {			
				HandleClientPacket(sender);
			}
			
			for(PlayerClient cl : Clients.values()) {
				cl.update();
				
				if(cl.cmds.position() > 0)
					SendClientCommand(cl);
					
			}
			
			
			UpdateSnapshots();
			
			if(pingTimer >= PING_DELAY) {
				pingTimer = 0;
				ping();
			}
			
			pingTimer++;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Console.AddLine(e.getMessage());
		}
		
	}
	
	void HandleClientPacket(SocketAddress sender) {
		
		recv.flip();
		
		while(recv.remaining() > 0) {
			
			if(recv.getInt() == -1) {
				ReadPacket(sender);
				
			}
			
		}
		
	}
	
	void ReadPacket(SocketAddress sender) {
		
		char cmd = recv.getChar();
		
		
		
		if(cmd == "j".charAt(0)) { //Client joined
			
			HandleClientJoined(sender);
		}
		
		if(cmd == "i".charAt(0)) { //Client input
			
			HandleInput(sender);
		}
		
		if(cmd == "f".charAt(0)) { //full update request
			
			HandleFullUpdateRequest(sender);
			
		}
		
		if(cmd == "s".charAt(0)) { //ping
			
			HandlePingRequest(sender);

		}
		
		if(cmd == "c".charAt(0)) { //map update
			HandleChunkRequest(sender);
		}
		
		if(cmd == 'l') { //quickbar selection changed
			HandleSelectionChange(sender);
		}
		if(cmd == 'x') { //moved item around in inventory
			HandleItemMove(sender);
		}
		if(cmd == 'd') {
			HandleDropItem(sender);
		}
		if(cmd == 'a') { //Handle user click
			HandleUserclick(sender);
		}
	}
	
	void HandleUserclick(SocketAddress sender) {
		
		PlayerClient client = Clients.get(sender);
		long ticks = recv.getLong();
		float x = recv.getFloat();
		float y = recv.getFloat();
		int delay = (int)(NET_DELAY +client.delay);
		
		input key = OmniInput.instance.new input(client.id, Keys.BUTTON_A, ticks + delay);
		key.x = x;
		key.y = y;
		OmniInput.inputs.add(key);
	}
	
	public void HandleUpdateTile(int x, int y, Items item) {
		for(PlayerClient client : Clients.values()) {
			client.SendTileUpdate(x, y, item);
		}
	}
	
	void HandleItemMove(SocketAddress sender) {
		
		PlayerClient client = Clients.get(sender);
		int from = recv.getShort();
		int to = recv.getShort();
		int amt = recv.getShort();
		
		
		client.TryMoveHeld(from, to, amt);
		client.UpdateInventory();
	}
	
	void HandleSelectionChange(SocketAddress sender) {
		
		PlayerClient cl = Clients.get(sender);
		
		int selection = recv.getInt();
		if(selection >= 0 && selection < Configuration.QuickBar_Capacity) {
			cl.selected = selection;
			cl.UpdateInventory();
		}
		
		
	}
	
	void SendClientCommand(PlayerClient cl) throws IOException {
		
		cl.cmds.flip();
		server.send(cl.cmds, cl.socket);
		cl.cmds.clear();
	}
	
	public void SendMapDamageUpdate(int x, int y, byte hp) {
		
		for(PlayerClient client : Clients.values()) {
			client.UpdateMapDamage(x, y, hp);
		}
		
	}
	
	void HandleDropItem(SocketAddress sender) {
		PlayerClient client = Clients.get(sender);
		int bagid = recv.getShort();
		int amount = recv.getShort();
		
		client.DropItem(bagid, amount);
	}
	
	public void HandleFullUpdateRequest(SocketAddress addr) {
		
		int type = recv.getInt();
		
		PlayerClient cl = Clients.get(addr);
		
		if(type == 1) {
				SendFullMap(cl);
		}
		else if(type == 2) {
			
				SendFullUpdate(cl);
		}
	}
	
	void HandleClientJoined(SocketAddress sender) {
		
		if(!isJoined(sender)) {
			Console.AddLine("Player joined");
			PlayerClient client = new PlayerClient(sender);
			Clients.put(sender, client);
			client.AddItemToBag(Items.TOOL_PickAxe_Wood, 1);
			client.AddItemToBag(Items.TOOL_Axe_Wood, 1);
			
			OmniWorld.instance.spawnObject(client,(OmniWorld.SpawnX*OmniTerrain.chunksize) + (OmniTerrain.chunksize/2), (OmniWorld.SpawnY * OmniTerrain.chunksize) + (OmniTerrain.chunksize/2), 0.60f, 0.8f);
			client.cmds.putInt(-1);
			client.cmds.putChar("j".charAt(0));
			client.cmds.putInt(client.id);
			
			
			
			
			

		}
	}
	
	public void HandlePingRequest(SocketAddress sender) {
		
		PlayerClient cl = Clients.get(sender);
		
		int type = recv.getInt();
		
		if(type == 1) {

					cl.ping = OmniWorld.ticks - cl.pingbuf;
					cl.ready = true;
					if(cl.ping > 20)
						cl.delay = 20;
					else
						cl.delay = 0;
					cl.ping_attempts = 0;
					
		}
		else if(type == 2) {

					ping(cl);				
		}
		
	}
	
	public void ping() {
		
		for(PlayerClient client : Clients.values()) {
			client.cmds.putInt(-1);
			client.cmds.putChar("s".charAt(0));
			client.cmds.putLong(OmniWorld.ticks);
			client.pingbuf = OmniWorld.ticks;
			client.ping_attempts++;
			
			if(client.ping_attempts > 3) { //byebye
				client.removalFlag = true;
			}
			
		}
		
	}
	
	public void ping(PlayerClient client) {
		
			client.cmds.putInt(-1);
			client.cmds.putChar("s".charAt(0));
			client.cmds.putLong(OmniWorld.ticks);
			client.pingbuf = OmniWorld.ticks;
		
	}
	
	boolean isJoined(SocketAddress sender) {
		
		return Clients.containsKey(sender);
	}
	
	void HandleInput(SocketAddress sender) {
		
		
		int clientID = Clients.get(sender).id;
		int keycode = recv.getInt();
		long ticks = recv.getLong();
		int inputIndex = recv.getInt();
		
		int delay = (int)(NET_DELAY + getClient(sender).delay);
		
		OmniInput.AddKey(clientID, ticks + delay, ticks, keycode, inputIndex); //delay
		
		
		

	}
	
	List<WorldObj> updateList = new ArrayList<WorldObj>();
	void SendFullUpdate(PlayerClient client) {
		
		
		
		
		client.cmds.putInt(-1);
		client.cmds.putChar("f".charAt(0)); //full update
		client.cmds.putLong(OmniWorld.ticks);
		client.cmds.putInt(OmniWorld.instance.spawnedObjects.size());
		for(int i=0; i<OmniWorld.instance.spawnedObjects.size(); i++) {
			WorldObj obj = OmniWorld.instance.spawnedObjects.get(i);
			client.cmds.putInt(obj.id);
			client.cmds.putFloat(obj.bounds.x);
			client.cmds.putFloat(obj.bounds.y);
			client.cmds.putFloat(obj.getWidth());
			client.cmds.putFloat(obj.getHeight());
			client.cmds.put(obj.animName.getBytes());
			client.cmds.put((byte)0);
			
			
		}
	}

	public void SendSwingCommand(int id, Items item) {
		
		for(PlayerClient client : Clients.values()) {
			if(client.id == id)
				continue;
			
			client.SendSwingCommand(id, item);
		}
		
	}
	
	void UpdateSnapshots() {
		
		for(WorldObj obj : OmniWorld.instance.spawnedObjects) {
			if(obj.needsUpdate) {
				
				
				for(PlayerClient cl : Clients.values()) {
										
					if(obj.id == cl.id || !cl.ready)
						continue;
					
					cl.cmds.putInt(-1);
					cl.cmds.putChar("p".charAt(0));
					cl.cmds.putInt(obj.id);
					cl.cmds.putLong(OmniWorld.ticks + NET_DELAY);
					cl.cmds.putShort((short)obj.direction);
					cl.cmds.putFloat(obj.bounds.x);
					cl.cmds.putFloat(obj.bounds.y);
					cl.cmds.putFloat(obj.width);
					cl.cmds.putFloat(obj.height);
					cl.cmds.put(obj.animName.getBytes());
					cl.cmds.put((byte)0);
					if(obj.item_inuse != null) {
						cl.cmds.putShort((short)obj.item_inuse.ordinal());
						cl.cmds.putFloat(obj.item_rotation);
					}
					else {
						cl.cmds.putShort((short)-1);
						cl.cmds.putFloat(-1);
					}
						
				}
				
			
					obj.needsUpdate = false;
				
			}
		}
	}
	
	void SendFullMap(PlayerClient client) {
		
		for(int x = 0; x < OmniTerrain.width; x++)
			for(int y = 0; y < OmniTerrain.height; y++) {
				if(OmniTerrain.chunks[x][y] != null)
					SendChunkInfo(client, x, y);
			}
		
		client.cmds.putInt(-1);
		client.cmds.putChar("c".charAt(0));
		client.cmds.putInt(2);
		
	}
	
	void SendChunkInfo(PlayerClient client, int x2, int y2) {
		
		client.chunksToSend.push(new Vector2(x2,y2));
			
	}
	
	void HandleChunkRequest(SocketAddress sender) {
		
		int x = recv.getInt();
		int y = recv.getInt();
		
		if(OmniTerrain.chunks[x][y] == null) {
			OmniTerrain.genChunk(x,y);
		}
		
		PlayerClient cl = getClient(sender);
		
		SendChunkInfo(cl, x, y);
		
	}
	
	PlayerClient getClient(SocketAddress sender) {
		
				return Clients.get(sender);
		
	}
	
	public void ForceMove(PlayerClient client) {
		
		client.cmds.putInt(-1);
		client.cmds.putChar("p".charAt(0));
		client.cmds.putInt(client.id);
		client.cmds.putLong(OmniWorld.ticks);
		client.cmds.putFloat(client.bounds.x);
		client.cmds.putFloat(client.bounds.y);
		client.cmds.put(client.animName.getBytes());
		client.cmds.put((byte)0);
		
	}
	
	public void RemoveObject(WorldObj obj) {
		
		PlayerClient cl = Clients.get(obj);
		if(cl != null)
			Clients.remove(cl);
		
		for(PlayerClient client : Clients.values()) {
		client.cmds.putInt(-1);
		client.cmds.putChar("r".charAt(0));
		client.cmds.putInt(obj.id);	
		}
		
	}
}
