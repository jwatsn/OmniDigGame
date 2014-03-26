package com.jwatson.omnidig.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.jwatson.omnidig.Camera.OmniCam;
import com.jwatson.omnidig.World.WorldObj;

public class LocalInput {
	
	public WorldObj owner;
	
	public LocalInput(WorldObj owner) {
		this.owner = owner;
	}
	
	public void update() {
		
//		-----------------------------------------------------------------------------------------------

		if(Gdx.input.isTouched()) {
			owner.inputManager.isTouched = 1;
			owner.inputManager.transform.set(Gdx.input.getX(), Gdx.input.getY(),0);
			OmniCam.instance.camera.unproject(owner.inputManager.transform);
		}
		else
			owner.inputManager.isTouched = 0;
		

		
//		-----------------------------------------------------------------------------------------------
//		check left
		if(Gdx.input.isKeyPressed(Keys.A))
			owner.inputManager.leftPressed = 1;
		else
			owner.inputManager.leftPressed = 0;

//		-----------------------------------------------------------------------------------------------
//		check right
		
		if(Gdx.input.isKeyPressed(Keys.D))
			owner.inputManager.rightPressed = 1;
		else
			owner.inputManager.rightPressed = 0;
		

//		-----------------------------------------------------------------------------------------------
//		check jump
		if(Gdx.input.isKeyPressed(Keys.W))
			owner.inputManager.jumping = 1;
		else
			owner.inputManager.jumping = 0;
		
		
	}
	

}
