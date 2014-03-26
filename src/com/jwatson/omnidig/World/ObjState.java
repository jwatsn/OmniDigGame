package com.jwatson.omnidig.World;

public enum ObjState {
		Spawning,
		Ready,
		Stunned;
		
		public float startUpDelay;
		
		private ObjState() {
			
		}
		
		private ObjState(float startUpDelay) {
			this.startUpDelay = startUpDelay;
		}
}
