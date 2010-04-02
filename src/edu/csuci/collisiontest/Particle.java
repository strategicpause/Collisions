package edu.csuci.collisiontest;
/**
 * @author Nick Peters
 */
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

public class Particle {
	public double radius;
	public Vector2D velocity, pos;
	public double mass;
	private long lastTime;
	private Paint circlePaint;
	private Random rand;
	/** Initialized now so we don't have to later */
	private int side;
	/** Used in resolveCollision() */
	private Vector2D delta, mtd, v, impulse;
	double length;
	float im1, im2, k;
	
	public Particle(int radius, int mass) {
		this.pos = new Vector2D(0, 0);
		this.velocity = new Vector2D(0,0);
		this.radius = radius;
		this.mass = mass;
		rand = new Random();
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor(Color.WHITE);
	}
	
	public void moveTo(double x, double y) {
		pos.x = x;
		pos.y = y;
	}
	
	public void moveTo(Vector2D pos) {
		this.pos = pos;
	}

	public void setVelocity(double velX, double velY)
	{
		velocity.x = velX;
		velocity.y = velY;
	}
	
	// http://cgp.wikidot.com/circle-to-circle-collision-detection
	public void collidesWith(Particle other) {
		delta = other.pos.sub(pos);
		if(delta.length() < (other.radius + radius)) {
				resolveCollision(other);
		}
	}
	
	// http://stackoverflow.com/questions/345838/ball-to-ball-collision-detection-and-handling
	public void resolveCollision(Particle other) {
		delta = pos.sub(other.pos);
		length = delta.length();
		// MTD - Minimum Translation Distance
		mtd = delta.mult((radius + other.radius - length) / length);
		//IM - Inverse Mass
		im1 = 1 / (float)mass;
		im2 = 1 / (float)other.mass;
		// Move particles apart
		pos = pos.add(mtd.mult(im1 / (im1 + im2)));
		other.pos.sub(mtd.mult(im2 / (im1 + im2)));
		// Impact Speed
		v = velocity.sub(other.velocity);
		double vn = v.dotProduct(mtd.normalize());
		if(vn > 0) return;
		k = (float) ((-1.0f * vn) / (im1 + im2));
		impulse = mtd.mult(k);
		velocity = velocity.add(impulse.mult(im1));
		other.velocity = other.velocity.sub(impulse.mult(im2));
	}
		
	public void update(long now) {
		if (now < this.lastTime)
			return;
		pos.x += velocity.x;
		pos.y += velocity.y;
		lastTime = now;
	}

	public void draw(Canvas canvas) {
		canvas.drawCircle((float)pos.x, (float)pos.y, (float)radius, circlePaint);
	}
		
	public void disappearsFromEdge(int canvasWidth, int canvasHeight) {
		if (pos.x - radius > canvasWidth || pos.x + radius < 0)	// hit left or right side
	        reset(canvasWidth, canvasHeight);
        if (pos.y - radius > canvasHeight || pos.y + radius < 0) // hit top or bottom
        	reset(canvasWidth, canvasHeight);
	}
	
	public void reset(int canvasWidth, int canvasHeight) {
		/** Determine which side the object appears from
		 * 0 - Left
		 * 1 - Top
		 * 2 - Right
		 * 3 - Bottom
		 */
		side = rand.nextInt(4);
		switch(side) {
		case 0:
			moveTo(0, Math.random()*(canvasHeight-radius));
			setVelocity(1, Math.pow(-1, rand.nextInt()));
			break;
		case 1:
			moveTo(Math.random() * canvasWidth, 0);
			setVelocity(Math.pow(-1, rand.nextInt()), 1);
			break;
		case 2:
			moveTo(canvasWidth, Math.random()*(canvasHeight));
			setVelocity(-1, Math.pow(-1, rand.nextInt()));
			break;
		case 3:
			moveTo(Math.random()*(canvasWidth), canvasHeight);
			setVelocity(Math.pow(-1, rand.nextInt()), -1);
			break;
		}
	}
}