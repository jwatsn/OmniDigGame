package com.jwatson.omnidig.ui;

import javax.swing.GroupLayout.Alignment;
import javax.swing.text.GlyphView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.OnscreenKeyboard;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Assets.AssetManager;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.World.OmniWorld;


public class MessageBox extends Actor {

	Stage stage;
	
	BitmapFont font1;
	
	int tempoffset = 56;
	public static MessageBox messageBox;
	public static boolean MessageBoxActive;
	public boolean input;
	boolean checkMark;
	Vector2 CoinPos;
	float stateTime;
	float maxHeight;
	TextureRegion[] border;
	TextureRegion GUI_Check_Mark;
	Vector2 pos;
	float Timer;
	String title,message;
	MessageBoxButton[] buttons;
	Rectangle[] button_bounds;
	int width,height;
	Vector2 interpolateAnim;
	private float animLength;
	BitmapFont font;
	BitmapFont font2;
	int button_padding = 2;
	Rectangle touch_bounds;
	
	TextField amtInput;
	int ItemAmt;
	int MaxAMT;
	private Integer id;
	private boolean isChestItem;
	
	float offset_y = 0;
	private Rectangle CM_Bounds;
	
	public MessageBox(Stage stage) {
		
	
		this.stage = stage;
		border = AssetManager.getUITexture("border").split(3, 3)[0];
		GUI_Check_Mark = AssetManager.getUITexture("GUI_Button_Check");
		pos = new Vector2();
		
		if(messageBox != null)
			messageBox = null;
		touch_bounds = new Rectangle();
		messageBox = this;
		interpolateAnim = new Vector2();
		font = new BitmapFont(Gdx.files.internal("data/default.fnt"),false);
		font1 = new BitmapFont(Gdx.files.internal("data/invnumbers.fnt"), false);
		font2 = new BitmapFont(Gdx.files.internal("data/smallfnt.fnt"),false);
		TextFieldStyle tfs = new TextFieldStyle();
		tfs.font = new BitmapFont(Gdx.files.internal("data/invnumbers2.fnt"),false);
		TextureRegionDrawable cursor = new TextureRegionDrawable(AssetManager.getUITexture("textcursor"));
		TextureRegionDrawable selection = new TextureRegionDrawable(AssetManager.getUITexture("textsel"));
		selection.setMinHeight(20f);
		
		tfs.cursor = cursor;
		tfs.selection = selection;
		//Gdx.app.debug("", ""+tfs.font.getCapHeight());
		tfs.fontColor = Color.BLACK;
		font.setColor(Color.BLACK);
		amtInput = new TextField("32", tfs);
		amtInput.setVisible(false);
		amtInput.setWidth(32f);
		amtInput.setMaxLength(3);
		
		stage.addActor(amtInput);
		
		CM_Bounds = new Rectangle(220, 12, GUI_Check_Mark.getRegionWidth()*4,GUI_Check_Mark.getRegionHeight()*4);
		
	}
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		// TODO Auto-generated method stub
		
		render(Gdx.graphics.getDeltaTime(), batch);
		
		super.draw(batch, parentAlpha);
	}
	
	public void render(float deltaTime, SpriteBatch batch) {
		
		if(stateTime < Timer) {
			
			if(amtInput.isVisible()) {
				
				
				
			
			
			try {
				ItemAmt = Integer.valueOf(amtInput.getText());		
				if(ItemAmt > MaxAMT)
					ItemAmt = MaxAMT;
			}
			catch(NumberFormatException e) {
				ItemAmt = 0;
			}
			
			}
			MessageBoxActive = true;
			OmniWorld.Paused = true;
			
			float x0 = (Gdx.input.getX(0) / (float)Gdx.graphics.getWidth()) * Configuration.ResolutionX;
			float y0 = Configuration.ResolutionY - (Gdx.input.getY(0) / (float)Gdx.graphics.getHeight()) * Configuration.ResolutionY;
			
			
			boolean close_button = (x0 > pos.x + width-20 && x0 < pos.x + width && y0 > pos.y+height-20 && y0 < pos.y+height && Gdx.input.justTouched() && stateTime > 0.01f);
			
			
				if(close_button)
					stateTime = Timer;
				
				
			if(Gdx.input.justTouched() && stateTime > 0.01f) {
				
				
				touch_bounds.set(x0,y0,1,1);
				
				if(checkMark) {
					
					if(CM_Bounds.contains(x0-pos.x, y0-pos.y)) {
						buttons[0].onClicked(ItemAmt,Integer.valueOf(buttons[0].txt));
						
						stateTime = Timer;
					}
				}
				else
				for(int i = 0; i < button_bounds.length; i++) {
					
					Gdx.app.debug(""+button_bounds[i].x, ""+button_bounds[i].y + " "+ x0 + " " +y0);
					
					if(button_bounds[i].contains(x0, y0)) {
						
						
						
						if(input) {
						if(buttons[i].type == MessageBoxButton.ITEM) {
							try {
							buttons[i].onClicked(ItemAmt);
							}
							catch(NumberFormatException e) {
								
								Gdx.app.debug("WRONGNUMBER", "");
								continue;
							}
						}
						}
						else
						buttons[i].onClicked();
						stateTime = Timer;
					}
				}
				
			}
				
		
			
			float height2 = (stateTime / animLength) * height;
			if(height2 > height)
				height2 = height;

			pos.set(Configuration.ResolutionX/2 - width/2, Configuration.ResolutionY/2-height2/2);
			
			batch.getProjectionMatrix().setToOrtho2D(0, 0, Configuration.ResolutionX, Configuration.ResolutionY);
			//batch.begin();
			batch.draw(border[6], pos.x, pos.y,width,height2);
			
			//draw border
			batch.draw(border[5], pos.x, pos.y,width,5);
			batch.draw(border[5], pos.x, pos.y+height2,width,5);
			batch.draw(border[4], pos.x, pos.y,5,height2);
			batch.draw(border[4], pos.x+width, pos.y,5,height2);
			batch.draw(border[2], pos.x, pos.y,5,5);
			batch.draw(border[0], pos.x, pos.y+height2,5,5);
			batch.draw(border[1], pos.x+width, pos.y+height2,5,5);
			batch.draw(border[3], pos.x+width, pos.y,5,5);
			if(title.length() > 0)
			font.draw(batch, title+":", pos.x + 8, pos.y + height2 - 5);
			if(height2 > maxHeight+25) {
			font2.drawWrapped(batch, message, pos.x+ 4, pos.y + height2 - 25, width-8,BitmapFont.HAlignment.CENTER);

			
			if(height2 >= height) {
				
			int counter = 1;
			int c0 = 0;
			for(MessageBoxButton but: buttons) {
				
				float posx = (pos.x+width) - ((width/(buttons.length+1) * counter))-22; 
				if(but.type == MessageBoxButton.ITEM)
				DrawButton(but,posx,pos.y+5+offset_y,52,25,batch);
				else {
					
					
					DrawButton(but.txt,button_bounds[c0].x,button_bounds[c0].y,button_bounds[c0].width,button_bounds[c0].height,batch);
				}
				counter++;
				c0++;
			}
			

			}
			
			if(input) {
				
				amtInput.setPosition(pos.x + (width/2) -(amtInput.getStyle().font.getBounds(amtInput.getText()).width/2), pos.y + 18+offset_y);
				if(!amtInput.isVisible()) {
					
					
					amtInput.setVisible(true);
					amtInput.setDisabled(false);
					stage.setKeyboardFocus(amtInput);
					amtInput.setTextFieldFilter(new TextFieldFilter.DigitsOnlyFilter());
					amtInput.selectAll();
					amtInput.setOnscreenKeyboard(new OnscreenKeyboard() {
						
						@Override
						public void show(boolean visible) {
							// TODO Auto-generated method stub
							
						}
					});
					OnScreenNumpad.CurrentNumpad.setPos(pos.x + (width/2)-52, pos.y - 110);
					OnScreenNumpad.setVisible(true);
					
					
					SetButtonBounds();
				}
				DrawButton("",pos.x+width/2-23,pos.y+10+offset_y,42,20,batch,true);
				
				if(checkMark) {
					batch.draw(GUI_Check_Mark, pos.x + CM_Bounds.x,pos.y + CM_Bounds.y,CM_Bounds.width,CM_Bounds.height);
				}
			}
				
			
			DrawButton("x",pos.x+width-21,pos.y+height-21,20,20,batch);
			}
			
			//batch.end();
		}
		else if(MessageBoxActive) {
			MessageBoxActive = false;
			OmniWorld.Paused = false;
			amtInput.setVisible(false);
			amtInput.setDisabled(true);
			input=false;
			isChestItem = false;
			OnScreenNumpad.setVisible(false);
			checkMark = false;
		}
		
		
		stateTime += deltaTime;
		
	}
	
	
	void SetButtonBounds() {
		
	
		button_bounds = new Rectangle[buttons.length];
		int counter = 1;
		for(MessageBoxButton but: buttons) {
			
			if(but.type == MessageBoxButton.ITEM) {
				float posx = pos.x + this.width/2 - 16;
				button_bounds[counter-1] = new Rectangle(221,213,42,42);
				this.height += 32;
				MaxAMT = but.maxAmt;
			}
			else {
			float posx = (pos.x+width) - ((width/(buttons.length+1) * counter))-22;
			button_bounds[counter-1] = new Rectangle(posx,pos.y+offset_y,52,25);
			}
			counter++;
		}
		
	}
	
	
	void DrawButton(String txt,float x, float y, float width, float height, SpriteBatch batch) {
		

		
		batch.draw(border[5], x, y,width,5);
		batch.draw(border[5], x, y+height,width,5);
		batch.draw(border[4], x, y,5,height);
		batch.draw(border[4], x+width, y,5,height);
		batch.draw(border[2], x, y,5,5);
		batch.draw(border[0], x, y+height,5,5);
		batch.draw(border[1], x+width, y+height,5,5);
		batch.draw(border[3], x+width, y,5,5);
		font.drawWrapped(batch, txt, x+5, y+(height/2)+font.getBounds(txt).height , width-7,BitmapFont.HAlignment.CENTER);
		
	}
	
void DrawButton(String txt,float x, float y, int width, int height, SpriteBatch batch, boolean white_bg) {
		
		batch.draw(border[7], x, y,width,height);
		batch.draw(border[5], x, y,width,5);
		batch.draw(border[5], x, y+height,width,5);
		batch.draw(border[4], x, y,5,height);
		batch.draw(border[4], x+width, y,5,height);
		batch.draw(border[2], x, y,5,5);
		batch.draw(border[0], x, y+height,5,5);
		batch.draw(border[1], x+width, y+height,5,5);
		batch.draw(border[3], x+width, y,5,5);
		font.drawWrapped(batch, txt, x+7, y, width,BitmapFont.HAlignment.CENTER);
		
	}

void DrawButton(MessageBoxButton but,float x2, float y, int width, int height, SpriteBatch batch) {
	
	float x = pos.x + this.width/2 - 16;
	width = 32;
	height = 32;
	String txt = but.txt;	
	id = Integer.valueOf(txt);
	y+= 40;
	batch.draw(border[5], x-5, y-5,width+5,5);
	batch.draw(border[5], x-5, y+height,width+5,5);
	batch.draw(border[4], x-5, y-5,5,height+5);
	batch.draw(border[4], x+width, y-5,5,height+5);
	batch.draw(border[2], x-5, y-5,5,5);
	batch.draw(border[0], x-5, y+height,5,5);
	batch.draw(border[1], x+width, y+height,5,5);
	batch.draw(border[3], x+width, y-5,5,5);
	batch.draw(Items.values()[id].texture,x,y,32,32);
	
//	if(Item.Items[id].type == InvObject.Type.BLUEPRINT)
//		batch.draw(Item.Items[Item.Items[id].BluePrintID].thumbnail,x+8,y+8,16,16);
	
	
	font1.draw(batch, ""+ItemAmt,x + 26,y + 10);
}
	
	public static void CreateMessageBox(String title,String msg, float time, int width, int height, float animLength) {
		
		messageBox.stateTime = 0;
		messageBox.offset_y = 0;
		messageBox.pos.set(Configuration.ResolutionX/2 - width/2, Configuration.ResolutionY/2-height/2);
		messageBox.width = width;
		messageBox.height = height;
		messageBox.Timer = time;
		messageBox.stateTime = 0;
		messageBox.animLength = animLength;
		messageBox.title = title;
		messageBox.message = msg;
		messageBox.maxHeight = messageBox.font2.getWrappedBounds(msg, (float)width).height;
		
	}
	
	public static void CreateMessageBox(String title,String msg, float time, int width, int height, float animLength, MessageBoxButton... buttons) {
		
		
		messageBox.stateTime = 0;
		messageBox.offset_y = 0;
		messageBox.pos.set(Configuration.ResolutionX/2 - width/2, Configuration.ResolutionY/2-height/2);
		messageBox.width = width;
		messageBox.height = height+30;
		float msgbox = messageBox.font2.getWrappedBounds(msg, width-8).height;

		
		
		messageBox.Timer = time;
		messageBox.stateTime = 0;
		messageBox.animLength = animLength;
		messageBox.title = title;
		messageBox.message = msg;
		messageBox.maxHeight = messageBox.font2.getWrappedBounds(msg, (float)width).height;
		messageBox.buttons = buttons;
		messageBox.SetButtonBounds();
		
	}
	
public static void CreateInputMessageBox(String title,String msg, float time, int width, int height, float animLength, MessageBoxButton... buttons) {
		
		
		messageBox.stateTime = 0;
		messageBox.offset_y = 150;
		messageBox.pos.set(Configuration.ResolutionX/2 - width/2, Configuration.ResolutionY/2-height/2);
		messageBox.width = width;
		messageBox.height = height+30;
		messageBox.input = true;
		messageBox.Timer = time;
		messageBox.stateTime = 0;
		messageBox.animLength = animLength;
		messageBox.title = title;
		messageBox.message = msg;
		messageBox.maxHeight = messageBox.font2.getWrappedBounds(msg, (float)width).height;
		messageBox.buttons = buttons;
		
		
	}

public static void CreateInputMessageBox_Buy(String title,String msg, float time, int width, int height, float animLength, MessageBoxButton... buttons) {
	
	
	messageBox.stateTime = 0;
	messageBox.offset_y = 160;
	messageBox.pos.set(Configuration.ResolutionX/2 - width/2, Configuration.ResolutionY/2-height/2);
	messageBox.width = width;
	messageBox.height = height+30;
	messageBox.input = true;
	messageBox.Timer = time;
	messageBox.stateTime = 0;
	messageBox.animLength = animLength;
	messageBox.title = title;
	messageBox.message = msg;
	messageBox.maxHeight = messageBox.font2.getWrappedBounds(msg, (float)width).height;
	messageBox.buttons = buttons;
	messageBox.checkMark = true;
	
}



	private String parseMessage(String msg) {
		
		String ret = msg;
		int index = msg.indexOf("%P");
		TextBounds bounds = font2.getWrappedBounds(msg, width);
		float width2 = Float.valueOf(bounds.width);
		
		if(index > 1) {
			float x = Float.valueOf(font2.getBounds(ret,0,index).width);
			//Gdx.app.debug(""+width2, ""+(x%width2));
			CoinPos = new Vector2(pos.x+(x%width2),pos.y);
			
		}
		
//		String[] ret = msg.split("%P");
//		
//		if(ret.length > 1) {
//			
//			TextBounds bounds = font2.getWrappedBounds(ret[0], width);
//			int x = (int)(font2.getBounds(ret[0]).width/width);
//			float x2 = (font2.getBounds(ret[0]).width - (x * width));
//			float capheight = font2.getCapHeight();
//			CoinPos = new Vector2(pos.x + x2 + 50,pos.y + bounds.height + 7);
//			
//		}
//		else
//			CoinPos = null;
		return ret;
	}

}
