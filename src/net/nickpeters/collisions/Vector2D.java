package net.nickpeters.collisions;
/**
 * 
 * @author Nick Peters
 *
 */
public class Vector2D {
	public double x;
	public double y;
	
	public Vector2D() {
		this.x = 0;
		this.y = 0;
	}
	
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2D add(Vector2D other) {
		return new Vector2D(x + other.x, y + other.y);
	}
	
	public Vector2D sub(Vector2D other) {
		return new Vector2D(x - other.x, y - other.y);
	}
	
	public double dotProduct(Vector2D other) {
		return x * other.x + y * other.y;
	}
	
	public double length() {
		return Math.sqrt((x * x) + (y * y));
	}
	
	public Vector2D normalize() {
		double length = length();
		return new Vector2D(x / length, y / length);
	}
	
	public Vector2D normalizeInPlace() {
		double length = length();
		x /= length;
		y /= length;
		return this;
	}
	
	public Vector2D mult(double scalar) {
		return new Vector2D(x * scalar, y * scalar);
	}
}
