package com.jwatson.omnidig.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.jwatson.omnidig.Assets.AssetManager;

public class OnScreenNumpad extends ChangeListener {

	Vector2 numpad_pos;
	//public AndroidOnscreenKeyboard keyboard;
	public static OnScreenNumpad CurrentNumpad;
	public boolean isActive;
	TextureRegionDrawable[] button_small = {new TextureRegionDrawable(AssetManager.getUITexture("button_small_unpressed")),
										 	new TextureRegionDrawable(AssetManager.getUITexture("button_small_pressed"))};
		
		
	TextureRegionDrawable[] button_large = {new TextureRegionDrawable(AssetManager.getUITexture("button_medium_unpressed")),
											new TextureRegionDrawable(AssetManager.getUITexture("button_medium_pressed"))};
	TextButtonStyle style;
	TextButtonStyle style_small;
	TextButton[] buttons;
	
	Stage stage;
	
	
	public OnScreenNumpad(Stage stage) {
		// TODO Auto-generated constructor stub
		
		this.stage = stage;
		
		numpad_pos = new Vector2(0,0);
		BitmapFont font = new BitmapFont(Gdx.files.internal("data/default.fnt"),false);
		style = new TextButtonStyle();
		style.font =  font;
		style.down = button_large[1];
		style.up = button_large[0];
		style.unpressedOffsetX = -1;
		style.unpressedOffsetY = 1;
		
		style_small = new TextButtonStyle();
		style_small.font =  font;
		style_small.down = button_small[1];
		style_small.up = button_small[0];
		style_small.unpressedOffsetX = -1;
		style_small.unpressedOffsetY = 1;
		
		
		
		buttons = new TextButton[12];
		int counter = 0;
		for(int i=0; i<12; i++) {

			if(i==0) {
				buttons[i] = new TextButton(""+counter, style);
				i++;
			}
			else if(i == 2)
				buttons[i] = new TextButton("Del", style_small);
			else
			buttons[i] = new TextButton(""+counter, style_small);
			
			if(i!=1)
			counter++;
		}
		
		for(int i2=0; i2<12; i2++) {
			if(buttons[i2] != null) {
				
				buttons[i2].setName(buttons[i2].getText().toString());
				buttons[i2].addListener(this);
				buttons[i2].setPosition(numpad_pos.x + ((i2%3)*35), numpad_pos.y + (35*(i2/3)));
				stage.addActor(buttons[i2]);
				buttons[i2].setVisible(false);
				}
		}
		
		if(CurrentNumpad != null)
			CurrentNumpad = null;
		
		CurrentNumpad = this;
		
	}

	
	public static void setVisible(boolean visible) {
		
		if(visible) {
			for(int i=0; i<CurrentNumpad.buttons.length; i++) {
				if(CurrentNumpad.buttons[i] != null)
				CurrentNumpad.buttons[i].setVisible(true);
			}
			CurrentNumpad.isActive = true;
		}
		else {
			for(int i=0; i<CurrentNumpad.buttons.length; i++) {
				if(CurrentNumpad.buttons[i] != null)
				CurrentNumpad.buttons[i].setVisible(false);
			}
			CurrentNumpad.isActive = false;
		}
		
	}


	@Override
	public void changed(ChangeEvent event, Actor actor) {
		// TODO Auto-generated method stub
		if(!actor.getName().equals("Del")) {
		char typed = actor.getName().charAt(0);
		stage.keyTyped(typed);
		}
		else {
			char typed = '\b';
			stage.keyTyped(typed);
		}
	}
	
	public void setPos(float x, float y) {
		numpad_pos.set(x,y);
		for(int i2=0; i2<12; i2++) {
			if(buttons[i2] != null) {
				
				buttons[i2].setPosition(numpad_pos.x + ((i2%3)*38), numpad_pos.y + (38*(i2/3)));
				}
		}
	}
	
	
}
