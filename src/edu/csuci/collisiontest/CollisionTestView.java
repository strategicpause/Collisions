package edu.csuci.collisiontest;

/**
 * This file is a heavily modified version of "AnimatedGameLoopView.java" from
 * COMP425 at CSUCI
 *
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class CollisionTestView extends SurfaceView implements SurfaceHolder.Callback {
	private final int NUM_PARTICLES = 20;
	class TheGameLoopThread extends Thread {
	    // The drawable to use as the background of the animation canvas
	    private Bitmap mBackgroundImage;
	
	    // Current height of the surface/canvas.
	    private int mCanvasHeight = 320;
	
	    // Current width of the surface/canvas.
	    // set in setSurfaceSize
	    private int mCanvasWidth = 320;
	
	    // Used to figure out elapsed time between frames
	    private long mLastTime;

	    // Indicate whether the surface has been created & is ready to draw
	    private boolean mRun = false;
	
	    // Handle to the surface manager object we interact with
	    private SurfaceHolder mSurfaceHolder;
		
		private final Particle[] particles; 
		int i, j; // Used for for loops;

	
	    public TheGameLoopThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
	        // get handles to some important objects
	        mSurfaceHolder = surfaceHolder;
	        Resources res = context.getResources();
	        mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.background);
			// load the particles 
	        particles = new Particle[NUM_PARTICLES];
	        for(i = 0; i < NUM_PARTICLES; i++) {
	        	particles[i] = new Particle(10, 10);	
	        }
	        	
	    }
	
	    public void doStart() {
	        synchronized (mSurfaceHolder) {
	        	resetParticles();
	        	// set up timers
	            mLastTime = System.currentTimeMillis() + 100;
	        }
	    }
	
	    private void resetParticles() {
            // get random position
	    	for(i = 0; i < particles.length; i++) {
	    		particles[i].reset(mCanvasWidth, mCanvasHeight);
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
	                c = mSurfaceHolder.lockCanvas(null);
	                synchronized (mSurfaceHolder) {
	                    updatePhysics();
	                    doDraw(c);
	                }
	            } finally {
	                // do this in a finally so that if an exception is thrown
	                // during the above, we don't leave the Surface in an
	                // inconsistent state
	                if (c != null) {
	                    mSurfaceHolder.unlockCanvasAndPost(c);
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
	        synchronized (mSurfaceHolder) {
	            mCanvasWidth = width;
	            mCanvasHeight = height;
	            // don't forget to resize the background image
	            mBackgroundImage = Bitmap.createScaledBitmap(mBackgroundImage, width, height, true);
	        }
	    }
	
	    /**
	     * Draws the spaceship, score, and background to the provided
	     * Canvas.
	     */
	    private void doDraw(Canvas canvas) {
	    	long now = System.currentTimeMillis();
	        // Draw the background image. Operations on the Canvas accumulate
	        // so this is like clearing the screen.
	        canvas.drawBitmap(mBackgroundImage, 0, 0, null);
	        
	        // Draw the sprite (animation is handled within the AnimatedSprite class) 
	        for(i = 0; i < particles.length; i++) particles[i].draw(canvas);
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
	        
	        // Determine if there are any collisions
	        // http://www.kirupa.com/developer/actionscript/multiple_collision2.htm
	        for(i = 0; i < particles.length; i++) {
	        	particles[i].update(now);
	        	particles[i].disappearsFromEdge(mCanvasWidth, mCanvasHeight);
	        	for(j = i + 1; j < particles.length; j++) {
	        		particles[i].collidesWith(particles[j]);
	        	}
	        }
	    }
	}

	private TheGameLoopThread thread;

	public CollisionTestView(Context context, AttributeSet attrs) {
		super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new TheGameLoopThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
            	// process any messages
            }
        });

        setFocusable(true); // make sure we get key events
	}

	/* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}