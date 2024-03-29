package com.jwatson.omnidig.QuadTree;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.jwatson.omnidig.World.WorldObj;

public class Quadtree {

	private int MAX_OBJECTS = 10;
	  private int MAX_LEVELS = 5;
	 
	  private int level;
	  private List<Rectangle> objects;
	  private Rectangle bounds;
	  private Quadtree[] nodes;
	 
	 /*
	  * Constructor
	  */
	  public Quadtree(int pLevel, Rectangle pBounds) {
	   level = pLevel;
	   objects = new ArrayList<Rectangle>();
	   bounds = pBounds;
	   nodes = new Quadtree[4];
	  }
	  
	  public void clear() {
		   objects.clear();
		 
		   for (int i = 0; i < nodes.length; i++) {
		     if (nodes[i] != null) {
		       nodes[i].clear();
		       nodes[i] = null;
		     }
		   }
	  }
	  
	  private void split() {
		   int subWidth = (int)(bounds.getWidth() / 2);
		   int subHeight = (int)(bounds.getHeight() / 2);
		   int x = (int)bounds.getX();
		   int y = (int)bounds.getY();
		 
		   nodes[0] = new Quadtree(level+1, new Rectangle(x + subWidth, y, subWidth, subHeight));
		   nodes[1] = new Quadtree(level+1, new Rectangle(x, y, subWidth, subHeight));
		   nodes[2] = new Quadtree(level+1, new Rectangle(x, y + subHeight, subWidth, subHeight));
		   nodes[3] = new Quadtree(level+1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
	 }
	  
	  private int getIndex(Rectangle pRect) {
		   int index = -1;
		   double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
		   double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);
		 
		   // Object can completely fit within the top quadrants
		   boolean topQuadrant = (pRect.getY() < horizontalMidpoint && pRect.getY() + pRect.getHeight() < horizontalMidpoint);
		   // Object can completely fit within the bottom quadrants
		   boolean bottomQuadrant = (pRect.getY() > horizontalMidpoint);
		 
		   // Object can completely fit within the left quadrants
		   if (pRect.getX() < verticalMidpoint && pRect.getX() + pRect.getWidth() < verticalMidpoint) {
		      if (topQuadrant) {
		        index = 1;
		      }
		      else if (bottomQuadrant) {
		        index = 2;
		      }
		    }
		    // Object can completely fit within the right quadrants
		    else if (pRect.getX() > verticalMidpoint) {
		     if (topQuadrant) {
		       index = 0;
		     }
		     else if (bottomQuadrant) {
		       index = 3;
		     }
		   }
		   
		   
		 
		   return index;
		 }
	  
	  public void insert(Rectangle pRect) {
		   if (nodes[0] != null) {
		     int index = getIndex(pRect);
		 
		     if (index != -1) {
		       nodes[index].insert(pRect);
		 
		       return;
		     }
		   }
		 
		   objects.add(pRect);
		 
		   if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
		      if (nodes[0] == null) {
		         split();
		      }
		 
		     int i = 0;
		     while (i < objects.size()) {
		       int index = getIndex(objects.get(i));
		       if (index != -1) {
		         nodes[index].insert(objects.remove(i));
		       }
		       else {
		         i++;
		       }
		     }
		   }
		 }
	  
	  /*
	   * Return all objects that could collide with the given object
	   */
	   public List retrieve(List returnObjects, Rectangle pRect) {
	     int index = getIndex(pRect);
	     if (index != -1 && nodes[0] != null) {
	       nodes[index].retrieve(returnObjects, pRect);
	     }
	   
	     returnObjects.addAll(objects);
	   
	     return returnObjects;
	   }

}
