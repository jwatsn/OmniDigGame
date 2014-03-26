package com.jwatson.omnidig.Assets;

import java.io.UnsupportedEncodingException;



import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
import com.jwatson.omnidig.Inventory.Items;




public class AssetManager {
	
	public static AssetManager m_class;
	
	TextureAtlas atlas;
	
	TextureRegion[] textures;
	
	
	
	public LinkedHashMap<String, Animation> animations;
	
	public static void Create() {
		
		if(m_class != null)
			m_class = null;
		
		m_class = new AssetManager();
	}

	public AssetManager() {
		// TODO Auto-generated constructor stub
		
		animations = new LinkedHashMap<String, Animation>();
		
		//GenTextures();
		atlas = new TextureAtlas("data/rtiles.atlas");
		GenAnimations();
		
		
	}
	
	public void GenTextures() {
		
		TexturePacker2.process("/data/data/gfx", "data", "rtiles");
				
		
		
		
	}
	
	void GenAnimations() {		
		
		AddAnimation(0.3f, "bobidle", "bobIdleRight", 8, 8, false);
		AddAnimation(0.3f, "bobidle", "bobIdleLeft", 8, 8, true);
		AddAnimation(0.3f, "bobidleattack", "bobIdleLeft#", 8, 8, true);
		AddAnimation(0.3f, "bobidleattack", "bobIdleRight#", 8, 8, false);
		
		AddAnimation(0.3f, "bobrun", "bobRight", 8, 8, false);
		AddAnimation(0.3f, "bobrun", "bobLeft", 8, 8, true);
		AddAnimation(0.3f, "bobattack", "bobLeft#", 8, 8, true);
		AddAnimation(0.3f, "bobattack", "bobRight#", 8, 8, false);
		
		AddAnimation(0.3f, "bobjump", "bobJumpRight", 8, 8, false);
		AddAnimation(0.3f, "bobjump", "bobJumpLeft", 8, 8, true);
		AddAnimation(0.3f, "bobjumpattack", "bobJumpLeft#", 8, 8, true);
		AddAnimation(0.3f, "bobjumpattack", "bobJumpRight#", 8, 8, false);
		
		AddAnimation(0.3f, "greendeath", "bobDying", 8, 8, false);		
		
		
		
	}
	
	public static Animation getAnim(String name) {
		return m_class.animations.get(name);
	}
	
	public static int GetAnimIndex(String name) {
		
		Animation anim = m_class.animations.get(name);
		
		
		return -1;
		
	}
	
	public static TextureRegion getWorldTexture(String name) {
		
		return m_class.atlas.findRegion("world/"+name);
	}
	public static TextureRegion getUITexture(String name) { 
		
		
		return m_class.atlas.findRegion("UI/"+name);
	}
	
	public void AddAnimation(float time, String tex, String name, int width, int height, boolean flip) {
		
		
		
		TextureRegion texture = atlas.findRegion("world/"+tex);
		
		TextureRegion split[] = new TextureRegion(texture).split(width, height)[0];
		
		if(flip) {
			
			
			for(TextureRegion region : split)
				region.flip(true, false);
		}
		
		

			animations.put(name, new Animation(time, split));
		
	}
	
	

}
