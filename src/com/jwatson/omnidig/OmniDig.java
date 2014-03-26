package com.jwatson.omnidig;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.jwatson.omnidig.Screens.GameScreen;

public class OmniDig extends Game {

	public OmniDig() {
		
	}

	@Override
	public void create() {
		
		setScreen(new GameScreen());
	}

}
