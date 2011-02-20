package com.teamwut.plasma.plasmapong;

import java.util.ArrayList;

import processing.core.PApplet;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.teamwut.plasma.plasmapong.mt.Cursor;
import com.teamwut.plasma.plasmapong.mt.MTCallback;
import com.teamwut.plasma.plasmapong.mt.MTManager;
import com.teamwut.plasma.plasmapong.pong.Const;
import com.teamwut.plasma.plasmapong.pong.Game;

public class PlasmaPong extends PApplet implements MTCallback {
	public final static String PLAYER_KEY = PlasmaPong.class+"PLAYER";
	public final static String ONE_PLAYER_PLAY = "ONE_PLAYER_PLAY";
	public final static String TWO_PLAYER_PLAY = "TWO_PLAYER_PLAY";

	public int sketchWidth() { return this.screenWidth; }
	public int sketchHeight() { return this.screenHeight; }
	public String sketchRenderer() { return PApplet.OPENGL; }

	PlasmaFluid fluid;
	
	MTManager mtManager;
		
	Game g;
	boolean paused = false;
	
	View pauseoverlay;
	
	public void onCreate(Bundle savedinstance) {
		super.onCreate(savedinstance);
		
    	pauseoverlay = this.getLayoutInflater().inflate(com.teamwut.plasma.plasmapong.R.layout.pause_screen_on, null);
    	this.addContentView(pauseoverlay, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	Button unpause = (Button) this.findViewById(com.teamwut.plasma.plasmapong.R.id.unpause);
    	unpause.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				pauseoverlay.setVisibility(View.GONE);
				paused = false;
			}});
    	Button quit = (Button) this.findViewById(com.teamwut.plasma.plasmapong.R.id.quit);
    	quit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			}});
    	
    	pauseoverlay.setVisibility(View.GONE);
    	
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		paused = true;
		pauseoverlay.setVisibility(View.VISIBLE);
		return false;
	}

	
	public void setup() {
	    // use OPENGL rendering for bilinear filtering on texture
	    //size(screen.width * 49/50, screen.height * 49/50, OPENGL);
	    //hint( ENABLE_OPENGL_4X_SMOOTH );    // Turn on 4X antialiasing
		hint(DISABLE_DEPTH_TEST);
		hint(DISABLE_OPENGL_ERROR_REPORT);
	    frameRate(40);
	
	    fluid = new PlasmaFluid(this);
	    mtManager = new MTManager();
	    
	    //GAME CODE
	    Intent lastIntent = this.getIntent();
		String playerKey = lastIntent.getStringExtra(PLAYER_KEY);
		int players = 0;
		if (playerKey.equals(ONE_PLAYER_PLAY))
			players = 1;
		else if (playerKey.equals(TWO_PLAYER_PLAY))
			players = 2;
			g = new Game(this, fluid, players);
	    initPong(); 
	    
	    debug();
	}
	
	
	public void debug() {
		  // Place this inside your setup() method
		  DisplayMetrics dm = new DisplayMetrics();
		  getWindowManager().getDefaultDisplay().getMetrics(dm);
		  float density = dm.density; 
		  int densityDpi = dm.densityDpi;
		  println("density is " + density); 
		  println("densityDpi is " + densityDpi);
		  println("HEY! the screen size is "+width+"x"+height);
	}
	
	
	//mt version
	public boolean surfaceTouchEvent(MotionEvent me) {
		if (mtManager != null) mtManager.surfaceTouchEvent(me);
		return super.surfaceTouchEvent(me);
	}
	
	public void addForce(float x, float y) {
		addForce(x,y,x,y);
	}
	public void addForce(float x, float y, float targetx, float targety) {
		float vx, vy;	
		
		vy = 30;

		vx = 0;
		
		float distancesqrt = (x-targetx)*(x-targetx) + (y-targety)*(y-targety);
		if (distancesqrt < 60010) {
			println("Redirecting!");
			float diffx = (targetx-x)/width;
			float diffy = Math.abs((targety-y)/height);
//			float diff = diffx/diffy;
			vx = diffx*30/diffy;
			vy = diffy*30/Math.abs(diffx);
		}
		
		if (y/height > 0.5f) 
			vy = -vy;

		
		if (y / height > 0.5f) {
			fluid.addForce(this, x/width, y/height, vx/width, vy/height, Const.PLAYER_1_OFFSET);
			fluid.addForce(this, (x+5)/width, y/height, -vy/width/4, vy/height/2, Const.PLAYER_1_OFFSET);
			fluid.addForce(this, (x-5)/width, y/height, vy/width/4, vy/height/2, Const.PLAYER_1_OFFSET);
		}
		else {
			fluid.addForce(this, x/width, y/height, vx/width, vy/height, Const.PLAYER_2_OFFSET);
			fluid.addForce(this, (x+5)/width, y/height, vy/width/4, vy/height/2, Const.PLAYER_2_OFFSET);
			fluid.addForce(this, (x-5)/width, y/height, -vy/width/4, vy/height/2, Const.PLAYER_2_OFFSET);
		}
	}
	
	public void updateCursors() {
		ArrayList<Cursor> cursors = (ArrayList<Cursor>) mtManager.cursors.clone();
		for (Cursor c : cursors ) {
			if (c != null && c.currentPoint != null)
				addForce(c.currentPoint.x, c.currentPoint.y, g.getBall().x, g.getBall().y);
		}
	}
	
	
	public void draw() {
		updateCursors();
		
	    background(0);
	    fluid.draw(this, !paused);
	    drawPong();
	    
	    if (this.frameCount % 60 == 0) println(this.frameRate+"");
	}

	public void initPong() {
		g.initPong();
	}

	public void drawPong() {
		g.drawPong(!paused);
	}
	@Override
	public void touchEvent(MotionEvent me, int i, float x, float y, float vx,
			float vy, float size) {
	}

}
