package com.jwatson.omnidig.ui;

import java.nio.channels.ClosedByInterruptException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Assets.AssetManager;
import com.jwatson.omnidig.Inventory.ItemObject;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.World.OmniWorld;
import com.jwatson.omnidig.netplay.Client;
import com.jwatson.omnidig.ui.InventoryManager.InventoryScreen;
import com.jwatson.omnidig.ui.MessageBoxButton;



public class InventoryManager extends Actor {
	
	
	public enum InventoryScreen {
		Bag,
		Chest,
		Craft,
		Merchant
	}
	
	//MessageBox Stuff
	MessageBoxButton take;
	
	//double click
	int lastTouchX;
	int lastTouchY;
	float lastClick;
	float clickTimer;
	
	//Dragging stuff
	int drag_pointer;
	ItemObject heldItem;
	Rectangle heldBounds;
	
	//Selected item
	ItemObject selectedItem;
	int selectedID;
	
	//Side Text
	public String sideText = "";
	public String totalExploredString = "2";
	
	//Current screen
	InventoryScreen screen;
	
	//Textures
	TextureRegion bagTexture;
	
	//Bounding boxes
	Rectangle bagBounds;
	Rectangle[][] slotBounds;
	Rectangle DropBounds;
	Rectangle CloseBounds;
	
	//font
	BitmapFont font;
	BitmapFont infoFont;
	
	//inventory input
	InventoryInput input;
	
	public InventoryManager(Stage stage) {
		
		
		SetUpTextures();
		SetUpBounds();
		SetUpFont();
		screen = InventoryScreen.Bag;
		input = new InventoryInput(this);
		stage.addListener(input);
		setVisible(false);
	}
	
	void SetUpTextures() {
		
		bagTexture = AssetManager.getUITexture("bag");
		
	}
	
	void SetUpMsgBoxButtons() {
		
		
		
	}
	
	void SetUpBounds() {
		
		//Inventory bounds
		bagBounds = new Rectangle();
		bagBounds.setWidth(bagTexture.getRegionWidth()*Configuration.Bag_Scale);
		bagBounds.setHeight(bagTexture.getRegionHeight()*Configuration.Bag_Scale);
		bagBounds.setX( (Configuration.ResolutionX/2) - (bagBounds.getWidth()/2) );
		bagBounds.setY( (Configuration.ResolutionY/2) - (bagBounds.getHeight()/2) );
		
		//Button Bounds
		DropBounds = new Rectangle(bagBounds.x + Configuration.Button_DropItem_x, bagBounds.y + Configuration.Button_DropItem_y, Configuration.Button_DropItem_width, Configuration.Button_DropItem_height);
		CloseBounds = new Rectangle(bagBounds.x + Configuration.Button_Close_x, bagBounds.y + Configuration.Button_Close_y, Configuration.Button_Close_width, Configuration.Button_Close_height);
		
		//The held item
		heldBounds = new Rectangle();
		
		//Slot bounds
		slotBounds = new Rectangle[Configuration.Bag_Width][Configuration.Bag_Height];
		
		for(int x = 0; x < Configuration.Bag_Width; x++) {
			for(int y = 0; y < Configuration.Bag_Height; y++) {
				
				int slotX = (int)bagBounds.x + (Configuration.Slot_StartX + (x * Configuration.Slot_PaddingX));
				int slotY = (int)bagBounds.y + (Configuration.Slot_StartY - (y * Configuration.Slot_PaddingY));
				
				Rectangle r = new Rectangle(slotX, slotY, Configuration.Slot_Size, Configuration.Slot_Size);
				
				slotBounds[x][y] = r;
			}
		}
		
	}
	
	void SetUpFont() {
		font = new BitmapFont(Gdx.files.internal("data/invnumbers.fnt"), false);
		infoFont = new BitmapFont(Gdx.files.internal("data/info.fnt"), false);
	}
	
	
	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		// TODO Auto-generated method stub
		
		
		
		switch(screen) {
		case Bag:
			drawBag(batch);
			break;
		}
		
		DrawSideText(batch);
		
		if(heldItem != null) {
			drawHeldItem(batch);
		}
		
		super.draw(batch, parentAlpha);
	}

	void DrawSideText(SpriteBatch batch) {
		
		infoFont.drawWrapped(batch, sideText, bagBounds.x + Configuration.Text_BagInfo_x, bagBounds.y + Configuration.Text_BagInfo_y,Configuration.Text_BagInfo_width,HAlignment.LEFT);
		
	}
	
	void drawBag(SpriteBatch batch) {
		
		if(CheckDoubleClick()) {
			if(selectedItem != null)
				HandleDoubleClick();
		}
		
		batch.draw(bagTexture, bagBounds.x, bagBounds.y, bagBounds.getWidth(), bagBounds.getHeight());
		
		ItemObject[] items = OmniWorld.instance.client.bagItems;
		
		for(int x = 0; x < Configuration.Bag_Width; x++) {
			for(int y = 0; y < Configuration.Bag_Height; y++) {
				
				ItemObject obj = items[x + y*Configuration.Bag_Width];
				
				if(obj != null)
					if(obj.stack > 0) {
						batch.draw(obj.item.texture, slotBounds[x][y].x, slotBounds[x][y].y, slotBounds[x][y].getWidth(), slotBounds[x][y].getHeight() );
						font.drawWrapped(batch, ""+obj.stack, slotBounds[x][y].x, slotBounds[x][y].y + (3 * Configuration.Bag_Scale), slotBounds[x][y].width, HAlignment.RIGHT);
					}
			}
		}
	}
	
	void drawHeldItem(SpriteBatch batch) {
		
		float x0 = (Gdx.input.getX() / (float)Gdx.graphics.getWidth()) * Configuration.ResolutionX;
		float y0 = Configuration.ResolutionY - (Gdx.input.getY() / (float)Gdx.graphics.getHeight()) * Configuration.ResolutionY;
		
		
		
		batch.draw(heldItem.item.texture, x0 - Configuration.Slot_Size/2, y0 - Configuration.Slot_Size/2, Configuration.Slot_Size, Configuration.Slot_Size);
		font.draw(batch, ""+heldItem.stack, x0 + 10, y0 - 6);
	}

	boolean CheckDoubleClick() {
		
		if(MessageBox.MessageBoxActive)
			return false;
		
		if(Gdx.input.justTouched()) {
			
			int x = (int)(((float)Gdx.input.getX() / (float)Gdx.graphics.getWidth()) * Configuration.ResolutionX);
			int y = (int)(((float)Gdx.input.getY() / (float)Gdx.graphics.getHeight()) * Configuration.ResolutionY);
			
			
			
			if(clickTimer - lastClick <= Configuration.Mouse_DoubleClickSpeed) {
				
				if(Math.abs(x-lastTouchX) <= Configuration.Mouse_DoubleClickSize)
					if(Math.abs(y-lastTouchY) <= Configuration.Mouse_DoubleClickSize)
						return true;
				
				
			}
			lastClick = clickTimer;
			lastTouchX = x;
			lastTouchY = y;
		}
		
		clickTimer += Gdx.graphics.getDeltaTime();
		
		
		return false;
		
	}
	
	void HandleDoubleClick() {
		
		MessageBoxButton item = new MessageBoxButton(""+selectedItem.item.ordinal()) {

			@Override
			public void onClicked(int... args) {
				// TODO Auto-generated method stub
				heldItem = new ItemObject(selectedItem.item, args[0]);
				input.lastHeldId = selectedID;
			}
			
		};
		item.type = MessageBoxButton.ITEM;
		item.maxAmt = selectedItem.stack;
		MessageBox.CreateInputMessageBox("", "How many would you like to take?\n", 90, 270, 220, 0.1f,item);
		
	}
	
	
}

class InventoryInput extends InputListener {
	
	InventoryManager parent;
	
	int lastHeldId;
	
	public InventoryInput(InventoryManager parent) {
		
		this.parent = parent;
		
	}
	
	@Override
	public boolean keyDown(InputEvent event, int keycode) {
		// TODO Auto-generated method stub
		
		if(Console.getVisible())
			return false;
		
		UpdateSideText();
		
		if(keycode == Keys.I) {
			
			
			
			parent.setVisible(!parent.isVisible());
			
		}
		 
		return super.keyDown(event, keycode);
	}
	
	boolean CheckTouchSlot(float x, float y) {
		
		for(int x2 = 0; x2 < Configuration.Bag_Width; x2++) {
			for(int y2 = 0; y2 < Configuration.Bag_Height; y2++) {
				
				if(parent.slotBounds[x2][y2].contains(x, y)) {
					
					ItemObject item = OmniWorld.instance.client.bagItems[x2 + y2*Configuration.Bag_Width];
					
					if(item != null)
						if(item.stack > 0) {
							parent.selectedItem = OmniWorld.instance.client.bagItems[x2 + y2*Configuration.Bag_Width];
							parent.selectedID = x2 + y2*Configuration.Bag_Width;
							return true;
						}
				}
				
			}
		}
		
		return false;
		
		
	}
	
	boolean CheckDragSlot(float x, float y) {
		
		for(int x2 = 0; x2 < Configuration.Bag_Width; x2++) {
			for(int y2 = 0; y2 < Configuration.Bag_Height; y2++) {
				
				if(parent.slotBounds[x2][y2].contains(x, y)) {
					
					ItemObject item = OmniWorld.instance.client.bagItems[x2 + y2*Configuration.Bag_Width];
					
					if(item != null)
						if(item.stack > 0) {
							parent.heldItem = OmniWorld.instance.client.bagItems[x2 + y2*Configuration.Bag_Width];
							parent.heldBounds.set(parent.slotBounds[x2][y2]);
							lastHeldId = x2 + y2*Configuration.Bag_Width;
							return true;
						}
				}
				
			}
		}
		

		return false;
		
	}
	
	boolean CheckDropButton(float x, float y) {
		
		if(parent.selectedItem != null)
		if(parent.DropBounds.contains(x, y)) {			
			
			MessageBoxButton item = new MessageBoxButton(""+parent.selectedItem.item.ordinal()) {
				
				@Override
				public void onClicked(int... args) {
					
					if(args[0] > maxAmt) 						
						args[0] = maxAmt;
						
					
					if(Client.isSpawned)
						Client.instance.DropItem(parent.selectedID, args[0]);
					else
						OmniWorld.instance.client.DropItem(parent.selectedID, args[0]);
						
					
				}
			};
			
			item.type = MessageBoxButton.ITEM;
			item.maxAmt = parent.selectedItem.stack;
			
			MessageBox.CreateInputMessageBox_Buy("", "How many do you want to drop?", 90, 270, 220, 0.1f,item);
			return true;
		}
		return false;
		
	}
	
	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer,
			int button) {
		
		if(Console.getVisible() || !parent.isVisible())
			return false;
		
		if(MessageBox.MessageBoxActive)
			return true;
		
		switch(parent.screen) {
		case Bag:
			return BagTouchDown(x,y);
			
		}
		
		return super.touchDown(event, x, y, pointer, button);
	}
	
	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer,
			int button) {
		
		
		switch(parent.screen) {
		case Bag:
			BagTouchUp(x,y);
			break;
		}
		
		
		super.touchUp(event, x, y, pointer, button);
	}
	
	void BagTouchUp(float x, float y) {
		
		
		if(parent.heldItem != null) {
		
			for(int x2 = 0; x2 < Configuration.Bag_Width; x2++) {
				for(int y2 = 0; y2 < Configuration.Bag_Height; y2++) {
					
					if(parent.slotBounds[x2][y2].contains(x, y)) {
						
						
						TryMoveHeld(x2,y2);
						return;
					}
					
				}
			}
			
			parent.heldBounds.set(-1,-1,0,0);
			parent.heldItem = null;
		
		}
		
	}
	
	void TryMoveHeld(int x, int y) {
		
//		ItemObject item = OmniWorld.instance.client.bagItems[x + y*Configuration.Bag_Width];
//		
//		if(x + y*Configuration.Bag_Width == lastHeldId) {
//			parent.heldItem = null;
//			return;
//		}
//		
//		if(item == null) {
//			
//			if(OmniWorld.instance.client.bagItems[lastHeldId].stack - parent.heldItem.stack > 0) {
//				OmniWorld.instance.client.bagItems[lastHeldId].stack -= parent.heldItem.stack;
//			}
//			else {
//			OmniWorld.instance.client.bagItems[lastHeldId] = null;
//			}
//			OmniWorld.instance.client.bagItems[x + y*Configuration.Bag_Width] = parent.heldItem;
//			parent.heldItem = null;
//			return;
//		}
//		
//		if(item.item == parent.heldItem.item) {
//			
//			if(item.stack + parent.heldItem.stack > item.item.type.GetStackLimit()) {
//				
//				
//				
//				parent.heldItem.stack -= item.item.type.GetStackLimit() - item.stack;
//				
//				item.stack = item.item.type.GetStackLimit();
//				parent.heldItem = null;
//				return;
//			}
//			else {
//				item.stack += parent.heldItem.stack;
//				parent.heldItem = null;
//				return;
//			}
//			
//		}
//		else {
//			
//			if(OmniWorld.instance.client.bagItems[lastHeldId].stack - parent.heldItem.stack <= 0) {
//				
//				OmniWorld.instance.client.bagItems[lastHeldId] = item;
//				OmniWorld.instance.client.bagItems[x + y*Configuration.Bag_Width] = parent.heldItem;
//				parent.heldItem = null;
//				return;
//				
//			}
//			
//		}
		
		OmniWorld.instance.client.TryMoveHeld(lastHeldId,x + y*Configuration.Bag_Width, parent.heldItem.stack);
		
		if(Client.isConnected)
			if(Client.isSpawned) {
				Client.instance.RequestMove(lastHeldId,x + y*Configuration.Bag_Width, parent.heldItem.stack);
			}
		
		parent.heldItem = null;
	}
	
	boolean BagTouchDown(float x, float y) {
		
		if(parent.CloseBounds.contains(x, y)) {
			parent.setVisible(false);
		}
		
		CheckDropButton(x, y);
		
		boolean ret = CheckTouchSlot(x, y);
		
		if(!ret)
			parent.selectedItem = null;
		
		UpdateSideText();
		
		return ret;
		
	}
	
	void UpdateSideText() {
		
		String str = "";
		if(parent.screen != InventoryScreen.Merchant && parent.screen != InventoryScreen.Chest) {
		str += "HP:"+OmniWorld.instance.client.HP + "/"+OmniWorld.instance.client.MaxHP + "\n";
		int def = OmniWorld.instance.client.DEF;
		if(def > 0) {
			str += "DEF:"+def+"\n";
		}
		str += "Explored:"+parent.totalExploredString+ "%\n";
		str += "\n";
		}
		
		if(parent.selectedItem != null) {
			if(parent.selectedItem.item.name == null)
		str += ""+parent.selectedItem.toString() + ""; 
			else
		str += ""+parent.selectedItem.item.name + ""; 
			
		if(parent.screen == InventoryScreen.Merchant) {
			str +="\n$ x" + parent.selectedItem.item.Price + "\n";
		}
			
			if(parent.screen != InventoryScreen.Craft) {
				str += "\n\n";
				if(parent.selectedItem.item.type.desc != null)
					str += parent.selectedItem.item.type.desc + "\n\n";
			
			}
//			else {
//				if(selectedItem.CraftingMulti > 1)
//					str += " (makes "+selectedItem.CraftingMulti+")";
//				
//				str += "\n\n";
//				
//				str += "Requirements:\n";
//				for(int i=0; i<Requirements.length; i+=2) {
//					
//					InvObject obj = Items[Requirements[i]];
//					
//					if(obj.descName != null)
//					str += obj.descName + " x"+Requirements[i+1]+"";
//					else
//					str += obj.name + " x"+Requirements[i+1]+"";
//					
//					
//					
//					str += "\n";
//				}
//				
//			}
		}
		
		parent.sideText = str;
		
	}
	
	@Override
	public void touchDragged(InputEvent event, float x, float y, int pointer) {
		// TODO Auto-generated method stub
		
		if(MessageBox.MessageBoxActive)
			return;
		
		switch(parent.screen) {
		case Bag:
			bagDragged(x, y);
			break;
		}
		
		super.touchDragged(event, x, y, pointer);
	}
	
	void bagDragged(float x, float y) {
		
		if(parent.heldItem == null) {
			CheckDragSlot(x, y);
		}
		else {
			parent.heldBounds.setX(x - (parent.heldBounds.width/2));
			parent.heldBounds.setY(y - (parent.heldBounds.height/2));
		}
		
	}
	
	
	
}
