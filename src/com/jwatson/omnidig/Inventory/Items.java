package com.jwatson.omnidig.Inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.jwatson.omnidig.Assets.AssetManager;

public enum Items {
	Empty(new EmptyBlock()),
	cave_wall(new EmptyBlock()),
	
	//Hopefully nothing bad comes from using this format... hopefully
	//TODO: update item format
	MAT_Water(new Block(4),"Water") {{
		
		this.name = "Water";
		this.type = new Liquid();
	}},
	
	
	MAT_Grass(new Block(4),"Grass") {{
		
		this.name = "Grass";
		this.type = new Block(1);
//		this.type.bounds = new Rectangle(0,0,1,0.25f);
		this.type.is_step = true;
	}},
	
	MAT_Dirt(new Block(4),"Dirt"),
	MAT_Stone(new Block(6),"Stone"),
	MAT_Coal(new Block(6),"Coal"),
	MAT_Copper(new Block(6),"Copper"),
	MAT_Iron(new Block(7),"Iron"),
	MAT_Sand(new Block(3),"Sand"),
	
	WEP_Sword_Wood(new Sword(13), "Wood Sword",10),
	
	
	
	
	DEP_Tent(null,"Tent Attack") {{
		
		this.type = new Sword(60);
		
	}},
	
	TOOL_PickAxe_Wood(new Pickaxe(1),"Wood PickAxe",10),
	TOOL_Axe_Wood(new Axe(), "Wood Axe",10);
	
	
	
	public ItemType type;
	public TextureRegion texture;
	public String name;
	
	public int Price;
	
	private Items(ItemType type) {
		
		
		
		this.type = type;
		
		texture = AssetManager.getWorldTexture(this.toString());
		if(!this.toString().equals("Empty")) {
			Animation anim = new Animation(0.3f, texture);
			AssetManager.m_class.animations.put(this.toString(), anim);
		}
		 
	}
	
	private Items() {
		
		texture = AssetManager.getWorldTexture(this.toString());
		//AssetManager.m_class.AddAnimation(0.3f, this.toString(), this.toString(), 1, 1, false);
	}
	
	private Items(ItemType type, String name) {
		
		
		
		this.type = type;
		this.name = name;
		texture = AssetManager.getWorldTexture(this.toString());
		Animation anim = new Animation(0.3f, texture);
		AssetManager.m_class.animations.put(this.toString(), anim);
	}
	
	private Items(ItemType type, String name, int price) {
		
		
		
		this.type = type;
		this.name = name;
		this.Price = price;
		texture = AssetManager.getWorldTexture(this.toString());
		Animation anim = new Animation(0.3f, texture);
		AssetManager.m_class.animations.put(this.toString(), anim);
		
		
	}
	
}
