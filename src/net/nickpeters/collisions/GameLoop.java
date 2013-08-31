package net.nickpeters.collisions;

import java.util.Set;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.view.SurfaceHolder;

public class GameLoop extends Thread {
	private final int NUM_PARTICLES = 50;
	private final int DEFAULT_RADIUS = 10;
	private final int DEFAULT_MASS = 10;
    // Current height of the surface/canvas.
    private int canvasHeight = 320;

    // Current width of the surface/canvas.
    // set in setSurfaceSize
    private int canvasWidth = 320;

    // Used to figure out elapsed time between frames
    private long mLastTime;

    // Indicate whether the surface has been created & is ready to draw
    private boolean mRun = false;

    // Handle to the surface manager object we interact with
    private SurfaceHolder surfaceHolder;
	private final Particle[] particles;
	private final QuadTree tree; 

    public GameLoop(SurfaceHolder surfaceHolder, Context context, Handler handler) {
        // get handles to some important objects
        this.surfaceHolder = surfaceHolder;
		// load the particles 
        particles = new Particle[NUM_PARTICLES];
        tree = new QuadTree(canvasHeight, canvasWidth);
        for(int i = 0; i < NUM_PARTICLES; i++) { 
        	particles[i] = new Particle(DEFAULT_RADIUS, DEFAULT_MASS);
        }
    }

    public void doStart() {
        resetParticles();
        // set up timers
        mLastTime = System.currentTimeMillis() + 100;
    }

    private void resetParticles() {
        // get random position
    	for(int i = 0; i < particles.length; i++) {
    		particles[i].init(canvasWidth, canvasHeight);
    	}
	}

	@Override
    public void run() {
		// Initialize
    	doStart();
    	// Start game loop
        while (mRun) {
            Canvas c = null;
            try {
                c = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                    updatePhysics();
                    doDraw(c);
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }

    /**
     * Used to signal the thread whether it should be running or not.
     * Passing true allows the thread to run; passing false will shut it
     * down if it's already running. Calling start() after this was most
     * recently called with false will result in an immediate shutdown.
     * 
     * @param b true to run, false to shut down
     */
    public void setRunning(boolean b) {
        mRun = b;
    }
	
    /* Callback invoked when the surface dimensions change. */
    public void setSurfaceSize(int width, int height) {
        // synchronized to make sure these all change atomically
        synchronized (surfaceHolder) {
            canvasWidth = width;
            canvasHeight = height;
        }
    }

    /**
     * Draws the spaceship, score, and background to the provided
     * Canvas.
     */
    private void doDraw(Canvas canvas) {
    	long now = System.currentTimeMillis();
    	if (canvas != null) {
    		canvas.drawColor(Color.BLACK);
    		// Draw the sprite (animation is handled within the AnimatedSprite class) 
    		for(int i = 0; i < particles.length; i++) particles[i].draw(canvas);
    	}
        mLastTime = now;
    }

    /**
     * Figures the game state (x, y, bounce, ...) based on the passage of
     * realtime. Does not invalidate(). Called as part of the game loop).
     */
    private void updatePhysics() {
        long now = System.currentTimeMillis();
        // Do nothing if mLastTime is in the future.
        // This allows the game-start to delay the start of the physics
        // by 100ms or whatever.
        if (mLastTime > now) return;
        int n = particles.length;
        tree.clear();
        for (int i = 0; i < n; i++) {
          tree.insert(particles[i]);
        }
        // Determine if there are any collisions
        // http://www.kirupa.com/developer/actionscript/multiple_collision2.htm
        for (int i = 0; i < n; i++) {
        	particles[i].update(now);
        	particles[i].disappearsFromEdge(canvasWidth, canvasHeight);
			Set<Particle> nearBy = tree.retrieve(particles[i]);
			for (Particle particle : nearBy) {
				if (particle != particles[i])
					particles[i].collidesWith(particle);
			}
        }
    }

}
