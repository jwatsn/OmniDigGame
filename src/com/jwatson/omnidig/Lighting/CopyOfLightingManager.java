package com.jwatson.omnidig.Lighting;

import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.jwatson.omnidig.Configuration;
import com.jwatson.omnidig.Camera.OmniCam;
import com.jwatson.omnidig.Inventory.Items;
import com.jwatson.omnidig.Terrain.OmniTerrain;
import com.jwatson.omnidig.Terrain.TerrainChunk;
import com.jwatson.omnidig.ui.Console;

public class CopyOfLightingManager {
	
	Pool<r_light2> lightPool;
	Stack<r_light2> lightStack;
	
	public static CopyOfLightingManager instance;
	Frustum frustrum;
	public CopyOfLightingManager() {
		
		if(instance != null)
			instance = null;
		
		instance = this;
		frustrum = OmniCam.getFrustrum();
		
		SetUpRecursiveStack();
	}
	
	void SetUpRecursiveStack() {
		lightStack = new Stack<r_light2>();
		lightPool = new Pool<r_light2>(250) {

			@Override
			protected r_light2 newObject() {
				// TODO Auto-generated method stub
				return new r_light2();
			}
			
		};
	}
	
	public byte[][] createSkyAmbient(TerrainChunk ch) {
		
		byte[][] ret = new byte[OmniTerrain.chunksize][OmniTerrain.chunksize];
		
		for(int x=0; x < OmniTerrain.chunksize; x++) {
			for(int y=OmniTerrain.chunksize-1; y >= 0; y--) {
				
				if(ch.map[x][y] == Items.Empty) {
					ret[x][y] = (byte) Configuration.Lighting_MaxBrightness;
				}
				else {
					recursiveLight(ch,x,y,(byte) Configuration.Lighting_MaxBrightness);
						break;
				}
				
				
			}
		}
		
		
		return ret;
		
	}
	
	
	public void updateAll() {
		
		//ClearLight();
		long time = System.nanoTime();
		int b = 0;
		for(int x=(int)((frustrum.planePoints[0].x-OmniTerrain.chunksize/2)/OmniTerrain.chunksize); x < ((frustrum.planePoints[1].x+OmniTerrain.chunksize/2)/OmniTerrain.chunksize); x++)
			for(int y=(int)((frustrum.planePoints[0].y-OmniTerrain.chunksize/2)/OmniTerrain.chunksize); y < ((frustrum.planePoints[2].y+OmniTerrain.chunksize/2)/OmniTerrain.chunksize); y++)
				{
						TerrainChunk ch = OmniTerrain.chunks[x][y];
						if(ch != null)
							updateLight(ch);
							b++;
				}
		Gdx.app.debug("TOOK:", ""+(System.nanoTime() - time));
	}
	
	public void ClearLight() {
		
		
		for(int x=(int)(frustrum.planePoints[0].x/OmniTerrain.chunksize); x < (frustrum.planePoints[1].x/OmniTerrain.chunksize); x++)
			for(int y=(int)(frustrum.planePoints[0].y/OmniTerrain.chunksize); y < (frustrum.planePoints[2].y/OmniTerrain.chunksize); y++)
				{
						TerrainChunk ch = OmniTerrain.chunks[x][y];
						
						if(ch == null)
							continue;
						
						System.arraycopy(ch.ambient, 0, ch.lighting, 0, ch.lighting.length);
				}
	}
	
	public void updateLight(TerrainChunk ch) {
		
	
	for(int x=1; x<OmniTerrain.chunksize-1; x++)
		for(int y=1; y<OmniTerrain.chunksize-1; y++) {
			
			byte mybright = (byte) (ch.lighting[x][y]);

			if(mybright > ch.lighting[x][y+1]) {
				r_light2 l = lightPool.obtain();
				l.set(x,y+1,mybright,ch);
				lightStack.add(l);
			}
			if(mybright > ch.lighting[x+1][y]) {
				r_light2 l = lightPool.obtain();
				l.set(x+1,y,mybright,ch);
				lightStack.add(l);
			}
			if(mybright > ch.lighting[x][y-1]) {
				r_light2 l = lightPool.obtain();
				l.set(x,y-1,mybright,ch);
				lightStack.add(l);
			}
			if(mybright > ch.lighting[x-1][y]) {
				r_light2 l = lightPool.obtain();
				l.set(x-1,y,mybright,ch);
				lightStack.add(l);
			}
		}
	}
	
	public static int b = 0;
	
	
	byte getBrightness(TerrainChunk ch, int x, int y) {
		
		if(x >= OmniTerrain.chunksize) {			
			x = 0;
			ch = OmniTerrain.chunks[ch.x2+1][ch.y2];		
		}
		if(y >= OmniTerrain.chunksize) {			
			y = 0;
			ch = OmniTerrain.chunks[ch.x2][ch.y2+1];		
		}
		
		if(x < 0) {			
			x = OmniTerrain.chunksize - 1;
			ch = OmniTerrain.chunks[ch.x2-1][ch.y2];		
		}
		
		if(y < 0) {			
			y = OmniTerrain.chunksize - 1;
			ch = OmniTerrain.chunks[ch.x2][ch.y2-1];		
		}
		
		if(ch == null)
			return 0;
		
		return ch.lighting[x][y];
		
	}
	
	public void recursiveStackLight() {
		
		while(!lightStack.empty()) {
			r_light2 l = lightStack.pop();
			
			int x = l.x;
			int y = l.y;
			byte bright = l.brightness;
			TerrainChunk ch = l.ch;
			
			if(x >= OmniTerrain.chunksize) {			
				x = 0;
				ch = OmniTerrain.chunks[ch.x2+1][ch.y2];		
			}
			if(y >= OmniTerrain.chunksize) {			
				y = 0;
				ch = OmniTerrain.chunks[ch.x2][ch.y2+1];		
			}
			
			if(x < 0) {			
				x = OmniTerrain.chunksize - 1;
				ch = OmniTerrain.chunks[ch.x2-1][ch.y2];		
			}
			
			if(y < 0) {			
				y = OmniTerrain.chunksize - 1;
				ch = OmniTerrain.chunks[ch.x2][ch.y2-1];		
			}
			
			
			if(ch == null)
				continue;
			
			
			
			
			if(ch.lighting[x][y] >= bright || bright <= 0)
				continue;
			
			
				if(ch.map[x][y] == Items.Empty)
				bright -= 1;
				else
					bright -= 3;
			
				ch.lighting[x][y] = bright;
				
				r_light2 up,down,left,right;
				up = lightPool.obtain();
				up.set(x, y+1, bright, ch);
				
				down = lightPool.obtain();
				down.set(x, y-1, bright, ch);
				
				left = lightPool.obtain();
				left.set(x-1, y, bright, ch);
				
				right = lightPool.obtain();
				right.set(x+1, y, bright, ch);
				
				lightStack.push(up);
				lightStack.push(down);
				lightStack.push(left);
				lightStack.push(right);
			
		}
		
	}
	
	public void recursiveLight(TerrainChunk ch,int x, int y, byte bright) {
		
		
		if(x >= OmniTerrain.chunksize) {			
			x = 0;
			ch = OmniTerrain.chunks[ch.x2+1][ch.y2];		
		}
		if(y >= OmniTerrain.chunksize) {			
			y = 0;
			ch = OmniTerrain.chunks[ch.x2][ch.y2+1];		
		}
		
		if(x < 0) {			
			x = OmniTerrain.chunksize - 1;
			ch = OmniTerrain.chunks[ch.x2-1][ch.y2];		
		}
		
		if(y < 0) {			
			y = OmniTerrain.chunksize - 1;
			ch = OmniTerrain.chunks[ch.x2][ch.y2-1];		
		}
		
		
		if(ch == null)
			return;
		
		
		
		
		if(ch.lighting[x][y] >= bright || bright <= 0)
			return;
		
		
			if(ch.map[x][y] == Items.Empty)
			bright -= 1;
			else
				bright -= 3;
		
			ch.lighting[x][y] = bright;
			
			recursiveLight(ch, x, y+1, bright);
			recursiveLight(ch, x, y-1, bright);
			recursiveLight(ch, x+1, y, bright);
			recursiveLight(ch, x-1, y, bright);

		b++;
	}

}

class r_light2 implements Poolable {

	int x,y;
	byte brightness;
	TerrainChunk ch;
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		x=0;
		y=0;
		brightness = 0;
	}
	
	public void set(int x, int y, byte brightness, TerrainChunk ch) {
		this.x = x;
		this.y = y;
		this.brightness = brightness;
		this.ch = ch;
	}
}
