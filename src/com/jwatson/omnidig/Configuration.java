package com.jwatson.omnidig;

public class Configuration {

	
	//Water vars
	public static float Water_MaxBlocks = 8f;
	public static int Water_UpdateSpeed = 6; // num ticks
	
	//player vars
	public static float Player_SpawnTime = 3f;
	
	//World tick config
	public static float TICK_RATE = 0.033f;
	
	//lighting
	public static float Lighting_MaxBrightness = 16;
	public static int Lighting_Speed = 1;
	public static int Lighting_Drain = 1;
	
	//Resolution for UI
	public static int ResolutionX = 480;
	public static int ResolutionY = 320;
	
	//Mouse stuff
	public static float Mouse_DoubleClickSpeed = 0.3f;
	public static float Mouse_DoubleClickSize = 3;
	
	//Player movement
	public static int Movement_JumpCancleTime = 5; //time in frames
	public static int Movement_AutoJumpDelay = 5;
	
	//Bag Stuff
	public static int Bag_Scale = 4;
	public static int Bag_Width = 7;
	public static int Bag_Height = 4;
	public static int Bag_MaxItems = Bag_Width * Bag_Height;
	
	
	//Bag slot stuff
	public static int Slot_StartX = 3 * Bag_Scale;
	public static int Slot_StartY = 47 * Bag_Scale;
	public static int Slot_PaddingX = 11 * Bag_Scale;
	public static int Slot_PaddingY = 11 * Bag_Scale;
	public static int Slot_Size = 8 * Bag_Scale;
	
	//Bag button stuff
	//Drop item
	public static int Button_DropItem_x = 97 * Bag_Scale;
	public static int Button_DropItem_y = 2 * Bag_Scale;
	public static int Button_DropItem_width = 10 * Bag_Scale;
	public static int Button_DropItem_height = 10 * Bag_Scale;
	//Close
	public static int Button_Close_x = 108 * Bag_Scale;
	public static int Button_Close_y = 2 * Bag_Scale;
	public static int Button_Close_width = 10 * Bag_Scale;
	public static int Button_Close_height = 10 * Bag_Scale;
	
	//Bag info text
	public static int Text_BagInfo_x = 80 * Bag_Scale;
	public static int Text_BagInfo_y = 54 * Bag_Scale;
	public static int Text_BagInfo_width = 38 * Bag_Scale;
	
	
	//Quickbar
	public static int QuickBar_Scale = 4;
	public static int QuickBar_Capacity = 4;
	public static int QuickBar_PaddingX = 1 * QuickBar_Scale;
	public static int QuickBar_PaddingY = 1 * QuickBar_Scale;
	public static int QuickBar_Size = 8 * QuickBar_Scale;
	public static int QuickBar_SlotSpace = 12 * QuickBar_Scale;
	public static int QuickBar_Y = 15;
	
	//Chat stuff
	public static int ChatBox_Width = 200;
	public static int ChatBox_Height = 80;
	public static int ChatBox_X = 5;
	public static int ChatBox_Y = 20;
	
	//Health bar
	public static int HealthBar_Width = 150;
	public static int HealthBar_Height = 5;
	public static int HealthBar_X = (ResolutionX/2) - (HealthBar_Width/2);
	public static int HealthBar_Y = 60;
	
	
	//network
	public boolean SendBlockHPOverNetwork = true;
	public static float NET_SnapshotDelay = 0.05f;
	
}
