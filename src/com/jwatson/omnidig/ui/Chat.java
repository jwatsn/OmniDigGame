package com.jwatson.omnidig.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.jwatson.omnidig.Configuration;

public class Chat extends Actor {

	List<String> chatBuffer;
	Label[] TF;
	
	Texture chatBox_texture;
	Texture input_texture;
	
	BitmapFont font;
	
	TextField input;
	
	Stage stage;
	
	float stateTime;
	
	public Chat(Stage stage) {
		this.stage = stage;
		setUpTextures();
		SetUpText();
		
		stage.addListener(new ChatInput(this));
	}
	
	void setUpTextures() {
		chatBox_texture = DrawConsoleBox(Configuration.ChatBox_Width, Configuration.ChatBox_Height);
		input_texture = DrawConsoleBox(Configuration.ChatBox_Width, 20);
	}
	
	void SetUpText() {
		
		chatBuffer = new ArrayList<String>(10);
		TF = new Label[5];
		LabelStyle tfs = new LabelStyle();
		font = new BitmapFont(Gdx.files.internal("data/default.fnt"),false);
		tfs.font = font;
		tfs.fontColor = new Color(0, 0, 255, 255);
		for(int i=0; i<TF.length; i++) {
			
			TF[i] = new Label("",tfs);
			TF[i].setPosition(Configuration.ChatBox_X, Configuration.ChatBox_Y + 20 +i*16);
			TF[i].setVisible(false);
			TF[i].setHeight(16);
			TF[i].setWidth(Configuration.ChatBox_Width);
			stage.addActor(TF[i]);
			
		}
		
		TextFieldStyle tfs2 = new TextFieldStyle();
		tfs2.font = font;
		tfs2.fontColor = Color.BLACK;
		input = new TextField("", tfs2);
		input.setPosition(Configuration.ChatBox_X + 2,Configuration.ChatBox_Y + 2);
		input.setText("");
		input.setMessageText("");
		stage.addActor(input);
		
		
	}
	
	public Texture DrawConsoleBox(int width, int height) {
		
		Pixmap border = new Pixmap(width, height, Format.RGBA4444);
		Color color = new Color();
		color.set(Color.WHITE);
		color.a = 100;
		border.setColor(color);
		border.fill();
		border.setColor(Color.BLACK);
		border.drawRectangle(0, 0, width, height);
		Texture tex = new Texture(border);
		
		return tex;
	}
	
	@Override
	public void setVisible(boolean visible) {
		
		input.setDisabled(!visible);
		input.setVisible(visible);
		for(Label tf : TF) {
			tf.setVisible(visible);
		}
		
		super.setVisible(visible);
		
		input.setText("");
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		
		batch.draw(chatBox_texture, Configuration.ChatBox_X, Configuration.ChatBox_Y+20);
		batch.draw(input_texture, Configuration.ChatBox_X, Configuration.ChatBox_Y);
		
	}

	void RefreshChat() {
		for(int i=0; i<chatBuffer.size(); i++) {
			if(i<TF.length) {
				TF[i].setText(chatBuffer.get((chatBuffer.size()-1)-i));
			}
		}
		
	}
	
	@Override
	public void act(float delta) {
		// TODO Auto-generated method stub
		
		if(stateTime == 0)
			input.setText("");
		
		stateTime += delta;
	}
	
}


class ChatInput extends InputListener {
	
	Chat parent;
	
	public ChatInput(Chat parent) {
		this.parent = parent;
	}
	
	@Override
	public boolean keyDown(InputEvent event, int keycode) {
		
		
		if(keycode == Keys.ESCAPE) {
			parent.setVisible(false);
			parent.input.setText("");
		}
		else if(keycode == Keys.T) {
			if(!parent.isVisible()) {
			parent.setVisible(true);
			parent.stage.setKeyboardFocus(parent.input);
			parent.stateTime = 0;
			}
		}
		else if(keycode == Keys.ENTER) {
			parent.chatBuffer.add(parent.input.getText());
			parent.input.setText("");
			parent.RefreshChat();
		}
		
		
		
		return true;
	}
	
}
