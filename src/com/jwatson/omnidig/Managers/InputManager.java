package com.jwatson.omnidig.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.jwatson.omnidig.World.WorldObj;

public class InputManager extends Manager {

	
	
	public byte leftPressed,rightPressed,upPressed,downPressed,isTouched,jumping;
	boolean moveLeft,moveRight,moveUp,moveDown;


	
	
	public Vector2 touchPos;
	public Vector3 transform; // get world coordinates
	WorldObj owner;
	
	public InputManager(WorldObj owner) {
		touchPos = new Vector2();
		transform = new Vector3();
		this.owner = owner;
	}
	

	@Override
	public void update(float delta) {
	
		
		if(jumping == 1) {
			if(owner.grounded)
				owner.velY = 5;
				owner.grounded = false;
		}
		
		if(leftPressed == 1) {
			owner.accel.x = -10;
		}
		else	if(rightPressed == 1) {
			owner.accel.x = 10;
		}
		else {
			owner.accel.x = 0;
		}
	}
	
	public int encode() {
		
		
		
		return 0;
	}
	
	
}
