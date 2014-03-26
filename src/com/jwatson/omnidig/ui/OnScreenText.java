package com.jwatson.omnidig.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.jwatson.omnidig.Camera.OmniCam;

class worldText implements Poolable {

	String text = "";
	float counter,max_time,delay,x,y,max_y,speed;
	public Vector3 transform;
	
	boolean completed;

	public worldText() {
		transform = new Vector3();
	}
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		counter = 0;
		max_time = 0;
		x = 0;
		y = 0;
		text = "";
		
	}
	
	public void update(float delta) {
		
		counter += delta;
		
		y += delta * speed;
		
		if(y > max_y)
			y = max_y;
		
		if(counter > max_time)
			completed = true;
		
		transform.set(x, y, 0);
		OmniCam.instance.camera.project(transform);
		
	}
	
}

public class OnScreenText extends Actor {
	
	Pool<worldText> textPool;
	List<worldText> textBuffer;
	
//	--------------------------------------------------------------------------------------------------
	BitmapFont font;
	
	
	static OnScreenText instance;
	
	public OnScreenText() {
		
		instance = this;
		
		createBuffers();
		createFonts();
	}
	
	void createFonts() {
		font = new BitmapFont(Gdx.files.internal("data/default2.fnt"),false);
		font.setColor(1,0,0,1);
	}
	
	void createBuffers() {
		textBuffer = new ArrayList<worldText>();
		textPool = new Pool<worldText>(60) {

			@Override
			protected worldText newObject() {
				// TODO Auto-generated method stub
				return new worldText();
			}
			
		};
	}
	
	@Override
	public void act(float delta) {
		// TODO Auto-generated method stub
		
		for(int i = 0; i < textBuffer.size(); i++) {
			
			worldText txt = textBuffer.get(i);
			if(txt.completed) {
				textBuffer.remove(i);
			}
			else {
				txt.update(delta);
			}
			
		}
		
		super.act(delta);
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		// TODO Auto-generated method stub
			for(int i = 0; i < textBuffer.size(); i++) {
				worldText txt = textBuffer.get(i);
				if(!txt.completed) {
					font.draw(batch, txt.text, txt.transform.x, txt.transform.y);
				}
			}
	}
	
	public static void AddText(String text, float x, float y) {
		
		worldText txt = instance.textPool.obtain();
		
		txt.x = x;
		txt.y = y;
		txt.text = text;
		
		//set up speed
		txt.max_time = 0.8f; // 500 ms
		txt.speed = 3.5f;
		txt.max_y = y + 1;
		
		instance.textBuffer.add(txt);
		
		
	}
	
}
