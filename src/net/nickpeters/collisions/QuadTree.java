package net.nickpeters.collisions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuadTree {
	private int MAX_OBJECTS = 10;
	private int MAX_LEVELS = 5;

	private int level;
	private List<Particle> objects;
	private Rectangle bounds;
	private QuadTree[] nodes;
	
	public QuadTree(int height, int width) {
		this(0, new Rectangle(0, 0, height, width));
	}

	public QuadTree(int level, Rectangle bounds) {
		this.level = level;
		this.objects = new ArrayList<Particle>();
		this.bounds = bounds;
		this.nodes = new QuadTree[4];
	}

	public void clear() {
		objects.clear();
		int n = nodes.length;
		for (int i = 0; i < n; i++) {
			if (nodes[i] != null) {
				nodes[i].clear();
				nodes[i] = null;
			}
		}
	}

	private void split() {
		int subWidth = (int) (bounds.getWidth() / 2);
		int subHeight = (int) (bounds.getHeight() / 2);
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();
		nodes[0] = new QuadTree(level + 1, new Rectangle(x + subWidth, y,
				subWidth, subHeight));
		nodes[1] = new QuadTree(level + 1, new Rectangle(x, y, subWidth,
				subHeight));
		nodes[2] = new QuadTree(level + 1, new Rectangle(x, y + subHeight,
				subWidth, subHeight));
		nodes[3] = new QuadTree(level + 1, new Rectangle(x + subWidth, y
				+ subHeight, subWidth, subHeight));
	}

	/*
	 * Determine which node the object belongs to. -1 means object cannot
	 * completely fit within a child node and is part of the parent node
	 */
	private int getIndex(Particle particle) {
		int index = -1;
		double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
		double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);
		// Object can completely fit within the top quadrants
		boolean topQuadrant = particle.getY() < horizontalMidpoint
			&& particle.getY() + particle.radius < horizontalMidpoint;
		// Object can completely fit within the bottom quadrants
		boolean bottomQuadrant = !topQuadrant
			&& particle.getY() > horizontalMidpoint;
		// Object can completely fit within the left quadrants
		boolean leftQuadrant = particle.getX() < verticalMidpoint
			&& particle.getX() + particle.radius < verticalMidpoint;
		// Object can completely fit within the right quadrants
		boolean rightQuadrant = !leftQuadrant
			&& particle.getX() > verticalMidpoint;
		if (leftQuadrant) {
			if (topQuadrant) {
				index = 1;
			} else if (bottomQuadrant) {
				index = 2;
			}
		} else if (rightQuadrant) {
			if (topQuadrant) {
				index = 0;
			} else if (bottomQuadrant) {
				index = 3;
			}
		}
		return index;
	}

	/*
	 * Insert the object into the quadtree. If the node exceeds the capacity, it
	 * will split and add all objects to their corresponding nodes.
	 */
	public void insert(Particle particle) {
		if (nodes[0] != null) {
			int index = getIndex(particle);
			if (index != -1) {
				nodes[index].insert(particle);
				return;
			}
		}
		objects.add(particle);
		if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
			if (nodes[0] == null) {
				split();
			}
			int i = 0;
			while (i < objects.size()) {
				int index = getIndex(objects.get(i));
				if (index != -1) {
					nodes[index].insert(objects.remove(i));
				} else {
					i++;
				}
			}
		}
	}
	
	public Set<Particle> retrieve(Particle particle) {
		return retrieve(new HashSet<Particle>(), particle);
	}

	public Set<Particle> retrieve(Set<Particle> returnObjects, Particle particle) {
		int index = getIndex(particle);
		if (index != -1 && nodes[0] != null) {
			nodes[index].retrieve(returnObjects, particle);
		}
		returnObjects.addAll(objects);
		return returnObjects;
	}
}