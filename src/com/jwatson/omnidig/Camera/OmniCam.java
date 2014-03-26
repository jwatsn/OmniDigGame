package com.jwatson.omnidig.Camera;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Matrix4;
import com.jwatson.omnidig.World.WorldObj;

public class OmniCam {
	
	public static OmniCam instance;
	
	WorldObj owner;
	
	public float lastX = -1;
	public float deltaX;
	
	public OrthographicCamera camera;
	
	public OmniCam() {
		
		if(instance != null)
			instance = null;
		
		camera = new OrthographicCamera(17, 10);
		
	}
	
	private void Update() {
		
		if(owner != null)
			camera.position.set(owner.pos.x, owner.pos.y, 0);
		
		deltaX = lastX - camera.position.x;
		
		if(lastX == -1)
			deltaX = 0;
		
		lastX = camera.position.x;
		
		camera.update();
	}
	
	public static void CreateCam() {
		instance = new OmniCam();
	}
	
	public static void UpdateCam() {
		
		
		
		instance.Update();
	}
	
	public static Matrix4 getCombined() {
		return instance.camera.combined;
	}
	public static Frustum getFrustrum() {		
		return instance.camera.frustum;
	}
	public static void AddPos(float x, float y) {
		instance.camera.position.add(x, y, 0);
	}
	
	public static void SetPos(float x, float y) {
		instance.camera.position.set(x,y,0);
	}
	
	public static void SetOwner(WorldObj obj) {
		instance.owner = obj;
	}
	
	public static Camera getCamera() {
		return instance.camera;
	}
	
	public static OmniCam getInstance() {
		return instance;
	}
	

}
