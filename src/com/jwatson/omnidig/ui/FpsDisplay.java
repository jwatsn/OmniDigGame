package com.jwatson.omnidig.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.jwatson.omnidig.Camera.OmniCam;
import com.jwatson.omnidig.Screens.GameScreen;
import com.jwatson.omnidig.netplay.Server;

public class FpsDisplay extends TextField {

	BitmapFont font;
	float counter = 0;
	
	public FpsDisplay(String text, TextFieldStyle style) {
		super(text, style);
		// TODO Auto-generated constructor stub
		style.fontColor = Color.RED;
	}

	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		super.draw(batch,parentAlpha);
		// TODO Auto-generated method stub
		
		
		counter += Gdx.graphics.getDeltaTime();
		if(counter >= 1) {
			this.setText(""+Server.bps);
			Server.bps = 0;
			counter = 0;
		}
		
		//this.setText(""+GameScreen.fps);
	}
}
