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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer.Task;
import com.jwatson.omnidig.OmniInput;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.Player.PlayerClient;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.Terrain.TerrainChunk;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.ui.Console;

public class CopyOfServer {
	
	public static boolean isHosting;
	
	public static int NET_DELAY = 10;
	
	public static int PING_DELAY = 100;
	
	public static CopyOfServer instance;
	
	
	int pingTimer;
	
	//input buffer
	List<OmniInput.input> inputBuffer;
	
	DatagramChannel server;
	
	//Thread for accepting connections
	Thread listener;
	
	//List of sockets connected
	public List<PlayerClient> Clients;
	
	
	//Recieving buffer
	ByteBuffer recv;
	
	//Input reader
	ObjectInputStream input;
	
	boolean started;
	
	public CopyOfServer() {
		
		instance = this;
		
		Clients = new ArrayList<PlayerClient>();
		
		
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
			
			for(PlayerClient cl : Clients) {
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
			
			HandleInput();
		}
		
		if(cmd == "f".charAt(0)) { //full update request
			
			HandleFullUpdateRequest();
			
		}
		
		if(cmd == "s".charAt(0)) { //ping
			
			HandlePingRequest();

		}
		
		if(cmd == "c".charAt(0)) { //map update
			HandleChunkRequest();
		}
		
	}
	
	
	
	void SendClientCommand(PlayerClient cl) throws IOException {
		
		cl.cmds.flip();
		server.send(cl.cmds, cl.socket);
		cl.cmds.clear();
	}
	
	public void HandleFullUpdateRequest() {
		
		int id = recv.getInt();
		int type = recv.getInt();
		
		if(type == 1) {
		for(PlayerClient cl : Clients)
			if(id == cl.id)
				SendFullMap(cl);
		}
		else if(type == 2) {
			
			for(PlayerClient cl : Clients)
				if(id == cl.id) {
					SendChunkInfo(cl, cl.chunk.x2, cl.chunk.y2);
					SendFullUpdate(cl);
				}
		}
	}
	
	void HandleClientJoined(SocketAddress sender) {
		
		if(!isJoined(sender)) {
			Console.AddLine("Player joined");
			PlayerClient client = new PlayerClient(sender);
			Clients.add(client);
			OmniWorld.instance.spawnObject(client,(OmniWorld.SpawnX*OmniTerrain.chunksize) + (OmniTerrain.chunksize/2), (OmniWorld.SpawnY * OmniTerrain.chunksize) + (OmniTerrain.chunksize/2), 0.60f, 0.8f);
			client.cmds.putInt(-1);
			client.cmds.putChar("j".charAt(0));
			client.cmds.putInt(client.id);
			
			
			
			

		}
	}
	
	public void HandlePingRequest() {
		
		
		
		int id = recv.getInt();
		int type = recv.getInt();
		
		if(type == 1) {
			for(PlayerClient cl : Clients) {
				
				if(cl.id == id) {
					cl.ping = OmniWorld.ticks - cl.pingbuf;
					cl.ready = true;
					if(cl.ping > 20)
						cl.delay = 20;
					else
						cl.delay = 0;
					cl.ping_attempts = 0;
					
				}
			}
		}
		else if(type == 2) {
			for(PlayerClient cl : Clients)
				if(cl.id == id) {
					ping(cl);
				}
		}
		
	}
	
	public void ping() {
		
		for(int i=0; i<Clients.size(); i++) {
			PlayerClient client = Clients.get(i);
			client.cmds.putInt(-1);
			client.cmds.putChar("s".charAt(0));
			client.cmds.putLong(OmniWorld.ticks);
			client.pingbuf = OmniWorld.ticks;
			client.ping_attempts++;
			
			if(client.ping_attempts > 3) { //byebye
				Clients.remove(i);
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
		
		for(PlayerClient client : Clients) {
			
			InetSocketAddress addr = (InetSocketAddress)sender;
			InetSocketAddress cl = (InetSocketAddress)client.socket;
			
			
			if(cl.getPort() == addr.getPort())
				return true;
			
		}
		
		return false;
	}
	
	void HandleInput() {
		
		
		int clientID = recv.getInt();
		int keycode = recv.getInt();
		long ticks = recv.getLong();
		int inputIndex = recv.getInt();
		
		int delay = (int)(NET_DELAY + getClient(clientID).delay);
		
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

	
	void UpdateSnapshots() {
		
		for(WorldObj obj : OmniWorld.instance.spawnedObjects) {
			if(obj.needsUpdate) {
				
				
				for(PlayerClient cl : Clients) {
										
					if(obj.id == cl.id || !cl.ready)
						continue;
					
					cl.cmds.putInt(-1);
					cl.cmds.putChar("p".charAt(0));
					cl.cmds.putInt(obj.id);
					cl.cmds.putLong(OmniWorld.ticks + NET_DELAY + cl.ping);
					cl.cmds.putFloat(obj.bounds.x);
					cl.cmds.putFloat(obj.bounds.y);
					cl.cmds.put(obj.animName.getBytes());
					cl.cmds.put((byte)0);
					
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
	
	void HandleChunkRequest() {
		
		int id = recv.getInt();
		int x = recv.getInt();
		int y = recv.getInt();
		
		if(OmniTerrain.chunks[x][y] == null) {
			OmniTerrain.genChunk(x,y);
		}
		
		PlayerClient cl = getClient(id);
		
		SendChunkInfo(cl, x, y);
		
	}
	
	PlayerClient getClient(int id) {
		
		for(PlayerClient cl : Clients)
			if(cl.id == id)
				return cl;
		
		return null;
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
		
		for(PlayerClient client : Clients) {
		client.cmds.putInt(-1);
		client.cmds.putChar("r".charAt(0));
		client.cmds.putInt(obj.id);	
		}
		
	}
}
