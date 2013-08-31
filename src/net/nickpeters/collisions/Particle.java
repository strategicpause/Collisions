package net.nickpeters.collisions;

/**
 * @author Nick Peters
 */
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Particle {
	private final static Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	static {
		circlePaint.setColor(Color.WHITE);
	}
	private final static Random rand = new Random();
	private final static int MAX_VELOCITY = 10;
	
	public double radius;
	public Vector2D velocity, pos;
	public double mass;
	private long lastTime;

	public Particle(int radius, int mass) {
		this.pos = new Vector2D(0, 0);
		this.velocity = new Vector2D(0, 0);
		this.radius = radius;
		this.mass = mass;
	}
	
	public void init(int canvasWidth, int canvasHeight) {
		moveTo(Math.random() * canvasWidth, Math.random() * canvasHeight);
		setVelocity(getRandVelocity(), getRandVelocity());
	}
	
	public double getRandVelocity() {
		return rand.nextDouble() * MAX_VELOCITY * (rand.nextBoolean() ? 1 : -1);
	}

	public void moveTo(double x, double y) {
		pos.x = x;
		pos.y = y;
	}

	public void moveTo(Vector2D pos) {
		this.pos = pos;
	}

	public void setVelocity(double velX, double velY) {
		velocity.x = velX;
		velocity.y = velY;
	}

	// http://cgp.wikidot.com/circle-to-circle-collision-detection
	public void collidesWith(Particle other) {
		Vector2D delta = other.pos.sub(pos);
		if (delta.length() < (other.radius + radius)) {
			resolveCollision(other);
		}
	}

	public void resolveCollision(Particle other) {
		Vector2D delta = pos.sub(other.pos);
		double length = delta.length();
		// MTD - Minimum Translation Distance
		Vector2D mtd = delta.mult((radius + other.radius - length) / length);
		double totalMass = mass + other.mass;
		// Move particles apart
		pos = pos.add(mtd.mult(other.mass / totalMass));
		other.pos.sub(mtd.mult(mass / totalMass));
		// Impact Speed
		Vector2D v = velocity.sub(other.velocity);
		double vn = v.dotProduct(mtd.normalize());
		if(vn > 0) return;
		//IM - Inverse Mass
		double im1 = 1 / mass;
		double im2 = 1 / other.mass;
		double k = (-1.0f * vn) / (im1 + im2);
		Vector2D impulse = mtd.mult(k);
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
		canvas.drawCircle((float) pos.x, (float) pos.y, (float) radius,
			circlePaint);
	}

	public boolean disappearsFromEdge(int canvasWidth, int canvasHeight) {
		// hit left or right side
		if (pos.x - radius > canvasWidth || pos.x + radius < 0) {
			reset(canvasWidth, canvasHeight);
			return true;
		}	
		// hit top or bottom
		if (pos.y - radius > canvasHeight || pos.y + radius < 0) {
			reset(canvasWidth, canvasHeight);
			return true;
		}
		return false;
	}

	public void reset(int canvasWidth, int canvasHeight) {
		/**
		 * Determine which side the object appears from 0 - Left 1 - Top 2 -
		 * Right 3 - Bottom
		 */
		int side = rand.nextInt(4);
		setVelocity(getRandVelocity(), getRandVelocity());
		switch (side) {
		case 0:
			moveTo(0, Math.random() * (canvasHeight - radius));
			break;
		case 1:
			moveTo(Math.random() * canvasWidth, 0);
			break;
		case 2:
			moveTo(canvasWidth, Math.random() * (canvasHeight));
			break;
		case 3:
			moveTo(Math.random() * (canvasWidth), canvasHeight);
			break;
		}
	}
	
	public int getX() {
		return (int) pos.x;
	}
	
	public int getY() {
		return (int) pos.y;
	}
}