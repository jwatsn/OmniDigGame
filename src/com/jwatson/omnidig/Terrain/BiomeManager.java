package com.jwatson.omnidig.Terrain;

import java.util.Vector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Player.Biome;
import com.jwatson.omnidig.World.OmniWorld;

public class BiomeManager {
	
	public static int BIOME_UNDERGROUND = 1;
	public static int BIOME_FOREST = 2;
	public static int BIOME_FOREST_CAMP = 3;
	public static int BIOME_ICE = 4;
	public static int BIOME_DESERT = 5;

	public BiomeManager() {
		// TODO Auto-generated constructor stub
	}
	
	public static Items[][] GetForestBiome(int width, int height,int thickness,Items[] hill_line_item,Items[] under_item,Items tree_item,int hills_height,float hill_roughness, Vector3... items) {
		
		Items[][] ret = new Items[width][height];
		
		for(int x=0; x<width; x++)
			for(int y=0; y<height; y++)
				ret[x][y] = Items.Empty;
		
		
		ret = generateHills(ret,width,hills_height,hill_line_item, hill_roughness);
		
		boolean flag = false;
		boolean flag2 = false;
		for(int x=0; x<width; x++) {
			flag = false;
			for(int y=height-1; y>=0; y--) {
				
				for(int i = 0; i<hill_line_item.length; i++)
				if(ret[x][y] == hill_line_item[i]) {
					
//					if(x-1 >= 0) {
//						if(ret[x-1][y] != tree_item) {
//							if(MathUtils.random(100) < thickness)
//								ret[x][y] = tree_item;
//						}
//					}
					
					flag2 = true;
					break;
				}
				if(flag2) {
					flag = true;
					flag2 = false;
					continue;
				}
				
				if(flag) {
					int rand = MathUtils.random(under_item.length-1);
					ret[x][y] = under_item[rand];
					
					for(Vector3 i : items) {
						int id = (int)i.x;
						float chance = i.y;
						double roll = Math.random()*100f;
						if(roll < chance) {
							MakePatch(ret, x,y,Items.values()[id],(int)(i.z*Math.random()));
						}
						
					}
				}
				
			}
		}
		
		float radius = 3;
		float mid = width/2;
		
		for(int x=0; x<width; x++) {
			boolean ground = false;
			int groundy = 0;
			for(int y=height-1; y>0; y--) {
				for(int i = 0; i<hill_line_item.length; i++)
					if(ret[x][y] == hill_line_item[i]) {
						ground = true;
						groundy = y;
					}
				
				if(ground) {
					
						float a = x-(mid-radius);
						if(a >= 0 && a < radius*2) {
							float rot = (a/(radius*2)) * 180;
							Gdx.app.debug(""+rot, ""+(groundy - y) + " " + y);
						}
					
					
				}
			}
		}
		return ret;
		
		
	}
	
	public static Items[][] generateCorner(int chunkSize,int beginningHeight,int endingHeight, Items hill_line_item,Items... matGrass) {
		
		Items[][] ret = new Items[chunkSize][chunkSize];
		Pixmap line = new Pixmap(chunkSize, chunkSize, Format.RGB888);
		line.setColor(Color.GREEN);
		line.drawLine(0, beginningHeight, chunkSize, endingHeight);
		
		for(int x = 0; x<chunkSize; x++) {
			boolean flag = false;
			for(int y = chunkSize-1; y >= 0; y--) {
				ret[x][y] = Items.Empty;
				
				if(line.getPixel(x, y) == Color.rgba8888(Color.GREEN)) {
					
					
					
					ret[x][y] = hill_line_item;
					flag = true;
				}
				else if(flag) {
					ret[x][y] = matGrass[0];
				}
			}
		}
		
		return ret;
	}
	

public static Items[][] generateHills(Items[][] ret, int width, int height, Items[] hill_line_item, float smooth,Items... matGrass) {
		// TODO Auto-generated method stub
	
		Vector2[] heights = new Vector2[(int)smooth-1];
		
		for(int i = 1; i< smooth; i++) {
			
			
			heights[i-1] = new Vector2(width * (float)((1/smooth)*i), (MathUtils.random(height-1)));
			
		}
		
		//Gdx.app.debug("", ""+heights[0].x);
	

		Pixmap test = new Pixmap(width, height, Format.RGB888);
		Vector2 LastPos = new Vector2(0,0);
		for(Vector2 pos : heights) {
			test.setColor(Color.GREEN);
			test.drawLine((int)LastPos.x, (int)LastPos.y, (int)pos.x, (int)pos.y);
			LastPos = pos;
		}
		test.drawLine((int)LastPos.x, (int)LastPos.y, width, 0);
		
		for(int x = 0; x<width; x++) {
			for(int y = 0; y<height; y++) {
				if(test.getPixel(x, y) == Color.rgba8888(Color.GREEN)) {
					
					if(y > 0) //make sure grass doesnt stack ontop
						if(test.getPixel(x, y-1) == Color.rgba8888(Color.GREEN)) {
							continue;
						}
					
					int hill = MathUtils.random(hill_line_item.length-1);
					ret[x][y] = hill_line_item[hill];
				}
			}
		}
	
		return ret;
	}

public static Items[][] GetUndergroundBiome(int width, int height,int caves,Items main,Vector3... item) {
		

	
		Items[][] ret = generateCaves(width,height,main,2,caves);
		int n = 0;
		for(int x=0; x<width; x++) {
			for(int y=0; y<height; y++) {

				if(ret[x][y] != Items.Empty)
				for(Vector3 i : item) {
					
					int id = (int)i.x;
					float chance = i.y;
					double roll = Math.random()*100f;
					if(roll < chance) {
					
						MakePatch(ret, x,y,Items.values()[id],(int)(i.z*Math.random()));
						
					}
					
				}
				
			}
		}
		
		return ret;
		
		
	}

private static Items[][] generateCaves(int width, int height, Items main_tile, int smoothness, int percent) {
	
	int ii,jj;
	

	Items[][] map1 = new Items[width][height];
	Items[][] map2 = new Items[width][height];
	
	for(int x2=0; x2<width; x2++) {
		for(int y2=0; y2<height; y2++) {
			map1[x2][y2] = main_tile;
			map2[x2][y2] = main_tile;
		}
	}
	
	for(int x=0; x<width; x++) {
		for(int y=0; y<height; y++) {
			if(MathUtils.random(100) <= percent) {
				map1[x][y] = Items.Empty;				
		}
	}
}
	
	
	
	for(int i=0; i<smoothness; i++) {
	
	for(int x=1; x<width-1; x++) {
		for(int y=1; y<height-1; y++) {
			
			int pass = 0;
			int pass2 = 0;

	 		for(ii=-1; ii<=1; ii++)
			for(jj=-1; jj<=1; jj++)
	 		{
	 			if(map1[x+ii][y+jj] != Items.Empty)
	 				pass++;
	 		}
	 		for(ii=x-2; ii<=x+2; ii++)
	 	 		for(jj=y-2; jj<=y+2; jj++)
	 	 		{
	 	 			if(Math.abs(ii-x)==2 && Math.abs(jj-y)==2)
	 	 				continue;
	 	 			if(ii<0 || jj<0 || ii>=width || jj>=height)
	 	 				continue;
	 	 			if(map1[ii][jj] != Items.Empty)
	 	 				pass2++;
	 	 		}
	 		if(pass >= 5 || pass2 <= 3) {
	 			map2[x][y] = main_tile;
	 			
	 		}
	 		else {
	 			map2[x][y] = Items.Empty;
	 		}
		}
	}
	for(int x2=0; x2<width; x2++)
		for(int y2=0; y2<height; y2++)
			map1[x2][y2] = map2[x2][y2];
	}
	
	
	return map1;
	
}

private static void MakePatch(Items[][] ret, int x, int y, Items id,int len) {
	
	if(x<0)
		return;
	if(y<0)
		return;
	if(x >= OmniTerrain.chunksize)
		return;
	if(y >= OmniTerrain.chunksize)
		return;
	
	if( len <= 0)
		return;
	
	if(ret[x][y] == id)
		return;
	
	if(ret[x][y] == Items.Empty)
		return;
	
	len--;
	
	ret[x][y] = id;
	
	MakePatch(ret,x,y+1,id,len);
	MakePatch(ret,x+1,y,id,len);
	MakePatch(ret,x,y-1,id,len);
	MakePatch(ret,x-1,y,id,len);
	
}

public static void MakeLiquidPatch(float[][] ret,Items[][]tmap, int x, int y, float i,int len) {
	
	if(x<0)
		return;
	if(y<0)
		return;
	if(x >= OmniTerrain.chunksize)
		return;
	if(y >= OmniTerrain.chunksize)
		return;
	
	if( len <= 0)
		return;
	
	if(ret[x][y] == i)
		return;
	
	if(tmap[x/2][y/2] != Items.Empty)
		return;
	len--;
	
	ret[x][y] = i;
	
	MakeLiquidPatch(ret,tmap,x,y+1,i,len);
	MakeLiquidPatch(ret,tmap,x+1,y,i,len);
	MakeLiquidPatch(ret,tmap,x,y-1,i,len);
	MakeLiquidPatch(ret,tmap,x-1,y,i,len);
	
}

public static Biome[][] generateMountains(Biome[][] ret,int y_offset,Biome biome_line,Biome corner_left, Biome corner_right, float smooth) {
	// TODO Auto-generated method stub

	int width = OmniTerrain.width;
	int height = OmniTerrain.height;
	
	Vector2[] heights = new Vector2[(int)smooth-1];
	
	for(int i = 1; i< smooth; i++) {
		
		
		heights[i-1] = new Vector2(width * (float)((1/smooth)*i),y_offset + (MathUtils.random(10)));
		
	}
	
	//Gdx.app.debug("", ""+heights[0].x);


	Pixmap test = new Pixmap(width, height, Format.RGB888);
	Vector2 LastPos = new Vector2(0,0);
	for(Vector2 pos : heights) {
		test.setColor(Color.GREEN);
		test.drawLine((int)LastPos.x, (int)LastPos.y, (int)pos.x, (int)pos.y);
		LastPos = pos;
	}
	test.drawLine((int)LastPos.x, (int)LastPos.y, width, 0);
	
	for(int x = 0; x<width; x++) {
		for(int y = 0; y<height; y++) {
			if(test.getPixel(x, y) == Color.rgba8888(Color.GREEN)) {
				
				ret[x][y] = biome_line;
				if(x == width/2) {
					OmniWorld.SpawnX = (x*OmniTerrain.chunksize) + (OmniTerrain.chunksize/2);
					OmniWorld.SpawnY = (y * OmniTerrain.chunksize) + (OmniTerrain.chunksize/2);
				}
					
				
				boolean flag = true;;
				int y2 = y;
				while(true) {
					y2--;
					if( y2 <= 0)
						break;
					
					if(ret[x][y2] == Biome.Underground)
						break;
					ret[x][y2] = Biome.Underground;
				}
			
			}
		}
	}
	
	for(int x = 1; x<width-1; x++) {
		for(int y = 1; y<height-1; y++) {
			
			if(ret[x][y] == biome_line) {
				if(ret[x+1][y] == Biome.Underground)
					ret[x][y] = Biome.ForestCR;
				if(ret[x-1][y] == Biome.Underground)
					ret[x][y] = Biome.ForestCL;
			}
			
		}
	}

	return ret;
}

}
