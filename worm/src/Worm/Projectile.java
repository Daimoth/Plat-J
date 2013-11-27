package Worm;

import java.awt.Rectangle;

public class Projectile extends Rectangle {

	private static final long serialVersionUID = 1L;		//What is this?
	 float x;
	 float y;
	 float velX;
	 float velY;
	 int dir;

	public Projectile(float x, float y, float velX, float velY, int dir){//0 = right, 1 = left
		this.x = x;
		this.y = y;
		this.velX = velX;
		this.velY = velY;
		this.height = 5;//arbitrary
		this.width = 20;//arbitrary
		this.dir = dir;
	}
	
	public boolean inBounds(float canvassWidth, float canvassHeight){
		if(x > canvassWidth + 50 || x < -50 || y > canvassHeight || y < -50){
			return false;
		} else {
			return true;
		}
	}
}
