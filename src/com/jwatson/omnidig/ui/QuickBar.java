package com.jwatson.omnidig.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Assets.AssetManager;
import com.jwatson.omnidig.Inventory.ItemObject;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.netplay.Client;


public class QuickBar extends Actor {
	
	Stage stage;

	//textures
	TextureRegion quickbar_texture;
	TextureRegion selected_texture;
	TextureRegion bagbutton_texture;
	TextureRegion[] hpBar_texture;
	
	//bounds
	Rectangle quickbar_bounds;
	Rectangle bagbutton_bounds;
	Rectangle slot_bounds[];
	
	//Selected quick bar item
	
	public QuickBar(Stage stage) {
		
		this.stage = stage;
		
		SetUpTextures();
		SetUpBounds();
		
		stage.addListener(new QuickBarInput(this));
		
	}
	
	void SetUpTextures() {
		quickbar_texture = AssetManager.getUITexture("GUI_Quickbar");
		selected_texture = AssetManager.getUITexture("selected");
		bagbutton_texture = AssetManager.getUITexture("GUI_Button_Inventory");
		hpBar_texture = new TextureRegion[]{ AssetManager.getUITexture("HUD_Health_Overlay"),  AssetManager.getUITexture("HUD_Health") };
	}
	void SetUpBounds() {
		
		int qbwidth = quickbar_texture.getRegionWidth() * Configuration.QuickBar_Scale;
		int qbheight = quickbar_texture.getRegionHeight() * Configuration.QuickBar_Scale;
		
		quickbar_bounds = new Rectangle((Gdx.graphics.getWidth()/2)-(qbwidth/2), 15,qbwidth,qbheight);
		
		slot_bounds = new Rectangle[Configuration.QuickBar_Capacity];
		
		float startX = quickbar_bounds.x + Configuration.QuickBar_PaddingX;
		
		for(int i = 0; i < Configuration.QuickBar_Capacity; i++) {
			slot_bounds[i] = new Rectangle(startX, Configuration.QuickBar_Y + Configuration.QuickBar_PaddingY, Configuration.QuickBar_Size,Configuration.QuickBar_Size);
			startX += Configuration.QuickBar_SlotSpace;
		}
		
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		
		batch.draw(quickbar_texture, quickbar_bounds.x, quickbar_bounds.y,quickbar_bounds.width,quickbar_bounds.height);
		
		for(int i=0; i < Configuration.QuickBar_Capacity; i++) {
			
			ItemObject obj = OmniWorld.instance.client.bagItems[i];
			
			if(obj != null)
				if(obj.stack > 0)
					batch.draw(obj.item.texture, slot_bounds[i].x, slot_bounds[i].y,slot_bounds[i].width,slot_bounds[i].height);
			
		}
		
		if(OmniWorld.instance.client.selected >= 0) {
			
			float selectedX = slot_bounds[OmniWorld.instance.client.selected].x - 4;
			float selectedY = slot_bounds[OmniWorld.instance.client.selected].y - 4;
			
			batch.draw(selected_texture, selectedX, selectedY, 40,40);
			
		}
		
		DrawHealthBar(batch);
		
	}
	
	public void DrawHealthBar(SpriteBatch batch) {
		
		float a =  1 - ((float)OmniWorld.instance.client.HP / (float)OmniWorld.instance.client.MaxHP);
		
		if(a < 0)
			a = 0;
		
		int hp_width = (int) (Configuration.HealthBar_Width * a);
		
		batch.draw(hpBar_texture[0], Configuration.HealthBar_X, Configuration.HealthBar_Y,Configuration.HealthBar_Width,Configuration.HealthBar_Height);
		batch.draw(hpBar_texture[1], Configuration.HealthBar_X, Configuration.HealthBar_Y,hp_width,Configuration.HealthBar_Height);
	}
	
}

class QuickBarInput extends InputListener {
	
	QuickBar parent;
	
	public QuickBarInput(QuickBar parent) {
		this.parent = parent;
	}
	
	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer,
			int button) {
		// TODO Auto-generated method stub
		
		for(int i=0; i<parent.slot_bounds.length; i++) {
			
			if(parent.slot_bounds[i].contains(x, y)) {
				OmniWorld.instance.client.selected = i;
				
				if(Client.isConnected)
					if(Client.isSpawned)
						Client.instance.SendSelection(i);
				
			}
		}
		
		
		return super.touchDown(event, x, y, pointer, button);
	}
	
}
