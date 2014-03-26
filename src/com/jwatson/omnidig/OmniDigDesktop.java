package com.jwatson.omnidig;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

public class OmniDigDesktop {


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new LwjglApplication(new OmniDig(), "Omni-Dig", 480, 320, true);
		
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		
	}

}
