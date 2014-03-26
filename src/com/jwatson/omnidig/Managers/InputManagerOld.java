package com.jwatson.omnidig.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Camera.OmniCam;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.EventManager;
import com.jwatson.omnidig.events.InputEvent;
import com.jwatson.omnidig.events.TouchEvent;

public class InputManagerOld extends Manager {

	
	
	public boolean leftPressed,rightPressed,upPressed,downPressed,isTouched;
	boolean moveLeft,moveRight,moveUp,moveDown,jumping;

	
	int lastMove;
	long lastMoveTick;
	
	public Vector2 touchPos;
	Vector3 transform; // get world coordinates
	
	Player owner;
	
	public InputManagerOld(Player owner) {
		this.owner = owner;
		touchPos = new Vector2();
		transform = new Vector3();
	}
	

	@Override
	public void update(float delta) {
		
		
		if(owner.is_stunned)
			return;
		
		checkInput();
		
		super.update(delta);
	}
	
	void checkInput() {
		
//		-----------------------------------------------------------------------------------------------

		if(isTouched) {
					
			if(owner.canFire()) {
				
				transform.set(Gdx.input.getX(), Gdx.input.getY(),0);
				OmniCam.instance.camera.unproject(transform);
				
				TouchEvent tEvent = TouchEvent.allocate();
				tEvent.set(OmniWorld.ticks, owner, transform.x, transform.y);
				EventManager.addEvent(tEvent);
				
			}
			
		}
		
		
//		-----------------------------------------------------------------------------------------------
//		check left
		if(leftPressed) {
			if(lastMove != -1) { //first press
				InputEvent event = InputEvent.allocate();
				event.set(OmniWorld.ticks, owner, -1);
				EventManager.addEvent(event);
				lastMove = -1;
			}	
		}

//		-----------------------------------------------------------------------------------------------
//		check right
		else if(rightPressed) {
			if(lastMove != 1) { //first press
				InputEvent event = InputEvent.allocate();
				event.set(OmniWorld.ticks, owner, 1);
				EventManager.addEvent(event);
				
				
				lastMove = 1;
			}	
		}
		
//		-----------------------------------------------------------------------------------------------
//		check to see if not moving
		if(!rightPressed && !leftPressed) {
			if(lastMove != 0) { //first press
				InputEvent event = InputEvent.allocate();
				event.set(OmniWorld.ticks, owner, 0);
				EventManager.addEvent(event);
				lastMove = 0;
			}			
		}
//		-----------------------------------------------------------------------------------------------
//		check jump
		if(upPressed) {
			if(owner.grounded) {
				InputEvent event = InputEvent.allocate();
				event.set(OmniWorld.ticks, owner, 2);
				EventManager.addEvent(event);
				lastMoveTick = OmniWorld.ticks;
				jumping = true;
			}
		}
		else {
			if(jumping) {
				if(lastMoveTick - OmniWorld.ticks < Configuration.Movement_JumpCancleTime) {
					InputEvent event = InputEvent.allocate();
					event.set(OmniWorld.ticks, owner, 2);
					EventManager.addEvent(event);
					lastMoveTick = OmniWorld.ticks;
				}
				jumping = false;
			}
		}
		
		
	}
	
	
}
