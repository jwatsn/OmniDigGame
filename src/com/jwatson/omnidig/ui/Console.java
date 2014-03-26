package com.jwatson.omnidig.ui;

import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Lighting.LightingManager;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.Terrain.TerrainChunk;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.liquid.LiquidManager;
import com.jwatson.omnidig.netplay.Client;
import com.jwatson.omnidig.netplay.Server;


public class Console extends Actor {
	
	
	
	public static Console instance;
	
	Texture consoleBG;
	Texture inputBG;
	
	//font
 	public BitmapFont font;
	
	//Chat log
	Stack<String> Log;
	
	//array of labels
	Label[] TF;
	
	//input
	TextField input;
	
	//parent stage
	Stage stage;
	
	public Console(Stage stage) {
		
		instance = this;
		
		consoleBG = DrawConsoleBox(480, 180);
		inputBG = DrawConsoleBox(480, 25);
		
		Log = new Stack<String>();
		
		this.stage = stage;
		
		SetUpText();
		SetUpInputListener();
		
	}
	
	
	public static void SetVisible(boolean visible) {
		instance.SetActive(visible);
	}
	
	public static boolean getVisible() {
		
		return instance.isVisible();
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		// TODO Auto-generated method stub
		
		
		
		batch.draw(consoleBG, 0, 140);
		batch.draw(inputBG, 0, 114);
		
		super.draw(batch, parentAlpha);
	}
	
	public Texture DrawConsoleBox(int width, int height) {
		
		Pixmap border = new Pixmap(width, height, Format.RGBA4444);
		Color color = new Color();
		color.set(Color.WHITE);
		color.a = 100;
		border.setColor(color);
		border.fill();
		border.setColor(Color.BLACK);
		border.drawRectangle(0, 0, width, height);
		Texture tex = new Texture(border);
		
		return tex;
	}
	
	public static void AddLine(String line) {
		instance.Log.add(line);
		instance.RefreshChat();
	}
	
	void RefreshChat() {
		for(int i=0; i<Log.size(); i++) {
			if(i<TF.length) {
				TF[i].setText(Log.get((Log.size()-1)-i));
			}
		}
		
	}
	
	public void SetActive(boolean flag) {

		if(flag) {
			for(int i=0; i<TF.length; i++) {
				TF[i].setVisible(true);
			}
			input.setVisible(true);
			input.setDisabled(false);
			stage.setKeyboardFocus(input);
			
			
		}
		else {
			for(int i=0; i<TF.length; i++) {
				TF[i].setVisible(false);
			}
			input.setVisible(false);
			input.setDisabled(true);
		}
		
		this.setVisible(flag);
	}
	
	void SetUpText() {
		
		TF = new Label[15];
		LabelStyle tfs = new LabelStyle();
		font = new BitmapFont(Gdx.files.internal("data/default.fnt"),false);
		tfs.font = font;
		tfs.fontColor = new Color(0, 0, 255, 255);
		
		
		for(int i=0; i<TF.length; i++) {
			
			TF[i] = new Label("",tfs);
			TF[i].setPosition(5, 140+i*16);
			TF[i].setVisible(false);
			TF[i].setHeight(16);
			TF[i].setWidth(480);
			stage.addActor(TF[i]);
			
		}
		
		TextFieldStyle tfs2 = new TextFieldStyle();
		tfs2.font = font;
		tfs2.fontColor = new Color(0, 0, 255, 255);
		input = new TextField("", tfs2);
		input.setPosition(5,120);
		input.setText("");
		input.setVisible(false);
		input.setDisabled(true);
		input.setMessageText("");
		stage.addActor(input);
		
		FpsDisplay fps = new FpsDisplay("to",tfs2);
		fps.setPosition(400,300);
		stage.addActor(fps);
		
	}
	
	void SetUpInputListener() {
		
		InputListener enterListener = new InputListener() {
			public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
				
				if(keycode == Keys.F1) {
					SetActive(!getVisible());
					
					
				}
				else if(keycode == Keys.ENTER) {
					if(!input.getText().isEmpty()) {
						
						
						ProcessCommand(input.getText());
						input.setText("");

						
					}
				}
				
				return true;
			};
		};
		
		stage.addListener(enterListener);
		
	}
	
	void ProcessCommand(String cmd) {
		
		String[] cmds = cmd.split(" ");
		
		if(cmds[0].equals("host")) {
			Server.instance.StartServer();
		}
		else if(cmds[0].equals("connect")) {
			
			if(cmds.length < 2)
				Console.AddLine("Please enter an IP");
			else
				Client.instance.Connect(cmds[1]);
		}
		else if(cmds[0].equals("dmg")) {
			
			OmniWorld.instance.client.onDamage(OmniWorld.instance.client, Items.DEP_Tent);

		}
		else if(cmds[0].equals("ping")) {
			
			Server.instance.ping();
		}
		else if(cmds[0].equals("net_delay")) {
			
			if(cmds.length < 1)
				Console.AddLine("usage: net_delay amount");
			else
				Server.NET_DELAY = Integer.valueOf(cmds[1]);
		}
		else if(cmds[0].equals("pos")) {
			
			Player cl = OmniWorld.instance.client;
			
			int x = (int) (cl.bounds.x % OmniTerrain.chunksize);
			int y = (int) (cl.bounds.y % OmniTerrain.chunksize);
			
			Console.AddLine(""+cl.chunk.x2 + " " +cl.chunk.y2 + " : "+ ""+x + " " + y);

		}
		else if(cmds[0].equals("bin")) {
			
			int num = Integer.parseInt(cmds[1], 2);
			
			Console.AddLine(""+num);

		}
		else if(cmds[0].equals("give")) {
			
			if(cmds.length < 4) {
				
				Console.AddLine("usage: give id item amount");
				
			}
			else {
				int id = Integer.valueOf(cmds[1]);
				
				Items item = null;
				
				try {item = Items.valueOf(cmds[2]);}
				catch(IllegalArgumentException e) {  Console.AddLine("Item not found"); return; }
				
				
				int amount = Integer.valueOf(cmds[3]);
				
				if(item == null) {
					
					Console.AddLine("Item not found");
					return;
				}
				
				Player player = null;
				for(Player pl : player.spawnedPlayers) {
					if(pl.id == id) {
						player = pl;
						break;
					}
				}
				
				if(player == null) {
					Console.AddLine("Player not found");
					return;
				}
				
				OmniWorld.instance.client.AddItemToBag(item, amount);
				
			}
			
		}
	}

}
