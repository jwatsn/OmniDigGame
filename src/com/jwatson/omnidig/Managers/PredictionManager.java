package com.jwatson.omnidig.Managers;

import java.security.acl.Owner;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Listeners.InputListener;
import com.jwatson.omnidig.Player.Player;
import com.jwatson.omnidig.OmniInput.input;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.World.WorldObj;
import com.jwatson.omnidig.events.EventManager;
import com.jwatson.omnidig.events.InputEvent;
import com.jwatson.omnidig.netplay.Server;



public class PredictionManager extends Manager {

	Player owner;
	
	Pool<PredictionObject> predictionPool;
	List<PredictionObject> predictionHistory;
	List<PredictionObject> predictionRemove;
	
	public PredictionManager(Player owner) {
		this.owner = owner;
		SetUpPool();
		SetUpListeners();
	}
	
	void SetUpListeners() {
		
		EventManager.addListener(new PredictionListener(this));
		
	}
	
	void SetUpPool() {
		
		predictionHistory = new ArrayList<PredictionObject>();
		predictionRemove = new ArrayList<PredictionObject>();
		predictionPool = new Pool<PredictionObject>(60) {
			
			@Override
			protected PredictionObject newObject() {
				// TODO Auto-generated method stub
				return new PredictionObject();
			}
		};
	}
	
	
public void checkPrediction(int direction,long ticks, float x, float y, float accelx, float accely, float velx, float vely) {
		
		int lastInputIndex = -1;
		PredictionObject in = null;	
		
		for(int i=0; i<predictionHistory.size(); i++) {
			
			in = predictionHistory.get(i);
			
			
			
			if(in.tick == ticks)
			if(in.direction == direction) {
				
				
				
				if(x != in.x || y != in.y) {
									
					Gdx.app.debug(""+(x - in.x), ""+(y - in.y));
					lastInputIndex = i;
					break;
				}
				
			}
			
		}
		
		if(lastInputIndex != -1) {
			
			
			
			
			
			
			owner.bounds.setX(x);
			owner.bounds.setY(y);
			owner.velX = velx;
			owner.velY = vely;
			owner.accel.x = accelx;
			owner.accel.y = accely;
			owner.grounded = in.grounded;
			
			int size = predictionHistory.size();
			
			int counter = 0;
			for(long i = in.tick; i < OmniWorld.ticks-1; i++) {
				


				
				owner.super_update(Configuration.TICK_RATE);
				predictionRemove.add(in);

				
				
				if(lastInputIndex < size-1) {
					
					if(i > predictionHistory.get(lastInputIndex+1).tick) {
						in = predictionHistory.get(lastInputIndex+1);
						lastInputIndex++;
						
						if(in.direction < 2) {
							owner.accel.x = in.direction  * owner.SPEED;
						} else {
							owner.Jump(in.e);
						}
						
					}
					
					
					
					
				}
				
				
				

				
				
				

			}
			
		
		}
		
		predictionHistory.removeAll(predictionRemove);
		predictionRemove.clear();
		
	}
}

class PredictionListener extends InputListener {
	
	PredictionManager manager;
	
	
	public PredictionListener(PredictionManager manager) {
		super(manager.owner);
		
		this.manager = manager;
	}
	
	
	@Override
	public void onMove(InputEvent e) {
		// TODO Auto-generated method stub
		PredictionObject p = manager.predictionPool.obtain();
		p.set(e.direction, e.x, e.y, e.tick,manager.owner.grounded,e);
		manager.predictionHistory.add(p);
		super.onMove(e);
	}
	

}

class PredictionObject implements Poolable {
	
	int direction;
	float x,y,velx,vely;
	long tick;
	boolean grounded;
	InputEvent e;
	
	Vector2 accel;
	
	public PredictionObject() { accel = new Vector2(); }
	
	void set(int direction, float x, float y, long tick, boolean grounded, InputEvent e) {
		this.direction = direction;
		this.x = x;
		this.y = y;
		this.tick = tick;
		velx = 0;
		vely = 0;
		this.e = e;
		this.grounded = grounded;
		
		accel.set(0,0);
	}

	@Override
	public void reset() {
		x=0;
		y=0;
		tick=0;
		direction=0;	
		e = null;
	}
	
}
