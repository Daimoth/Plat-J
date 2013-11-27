/*TO DO
 * PHYSICS:
 * 	turning mid-air while running works unrealistically
 * 		replace Dir enums with a single velX variable
 * 	
 * FADE-INS
 * 	doesn't work right in-game
 * 
 * TILES
 * 	probably need to switch to entity-based code, too
 * 
 * INPUT
 * 	move logic from flying code to tick()
 * 	if key B is pressed while key A is being pressed, A will trigger again
 * 
 * SIDESCROLLING
 * 
 * */

package Worm;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.input.Keyboard;
//import org.lwjgl.input.Mouse;


import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
//import java.util.Random;




//import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.opengl.GL11.*;

public class WormCanvass {
	
	private static enum State {
        INTRO, MAIN_MENU, GAME
    }
	
	private static enum Dir {
        LEFT, RIGHT
    }
	
    private static State state = State.INTRO;
    private static Dir dir = Dir.RIGHT;
    
    static ArrayList<Projectile> bullets = new ArrayList<Projectile>();
    static Texture logo;
    
	private static float fade = 90F;
	private static float velX = 3.0F;
	private static int width = 640;
	private static int height = 480;
	private static float spriteWidth = 30.0F;
	private static float spriteHeight = 30.0F;
	private static float initPosX = 50.0F;
	private static float initPosY = height - spriteHeight - 60.0F;
	private static float jumpVel = 16.0F;
	private static float minVel = 1.05F;
	private static float fallVel = 1.15F;
	private static float terminalVelocity = 11.5F;
	
	//audio		
    public static final int NUM_BUFFERS = 2;  	/** Maximum data buffers we will need. */      	
    public static final int NUM_SOURCES = 2;   	/** Maximum emissions we will need. */   
    public static final int JUMP = 0;        	/** Index of JUMP sound */  
    public static final int SHOOT = 1;      	/** Index of SHOOT sound */
    public static IntBuffer buffer = BufferUtils.createIntBuffer(NUM_BUFFERS);
	public static IntBuffer source = BufferUtils.createIntBuffer(NUM_BUFFERS); 
	
	private static boolean isJumping = false;
	private static boolean isRunning = false;
	private static boolean flyEnabled = false;
	private static boolean paused = false;
	
	//objects
	TextRenderer textRenderer = new TextRenderer();
	
	public WormCanvass(){
		try{
			TextRenderer.setupTextures();
		} catch (IOException e){
			System.out.println("ERROR AT FONT SETUP");
			e.printStackTrace();
			Display.destroy();
			//AL.destroy();
			System.exit(1);
		}
	}

	public static void main(String[] args) {

		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setTitle("W O R M");
			Display.setVSyncEnabled(true);
			Display.create();
			AL.create();
			setupAudio();
		} catch (LWJGLException e) {
			e.printStackTrace();
			Display.destroy();
			AL.destroy();
			System.exit(1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logo = loadTexture("logo1");
		
		glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        // (0, 0) is center, (1, 1) is the upper-right, (-1, -1) is the bottom-left
        //glOrtho(1, 1, 1, 1, 1, -1);
        glOrtho(0, 640, 480, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        // Enable trancluency
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // GAME LOOP \\
		while(!Display.isCloseRequested()) {
			if(!paused){
				tick();
			}
			checkInput();
			render();
			Display.update();
			Display.sync(60);
		}
		// GAME LOOP \\
		
        logo.release();
		Display.destroy();
		AL.destroy();
		System.exit(0);
	}
	
	public static void playSound(int sfx) throws FileNotFoundException{
        alSourcePlay(source.get(sfx));
	}
	
	public static void setupAudio() throws FileNotFoundException{
		AL10.alGenBuffers(buffer);
		
		WaveData waveFile = WaveData.create(new BufferedInputStream(new FileInputStream("res" + File.separatorChar + "sfx" + File.separatorChar + "jump.wav")));
		AL10.alBufferData(buffer.get(JUMP), waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		
		waveFile = WaveData.create(new BufferedInputStream(new FileInputStream("res" + File.separatorChar + "sfx" + File.separatorChar + "shoot.wav")));
		AL10.alBufferData(buffer.get(SHOOT), waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		
		// Bind buffers into audio sources.
		AL10.alGenSources(source);
		
        //Specifies the position and other properties as taken into account during sound processing.
        alSourcei(source.get(JUMP), AL_BUFFER, buffer.get(JUMP));
        alSourcei(source.get(SHOOT), AL_BUFFER, buffer.get(SHOOT));
	}
		
    private static void tick() {    	
	 	//X accel
    	if(isRunning && (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT))){
    		if(dir == Dir.RIGHT){
				if(initPosX < width - spriteWidth){                
    				initPosX += velX;
                }
		 		if(velX <= 8.0F && onGround()){
		 			velX *= 1.25F;
		 		} else if (velX <= 8.0F && !onGround()){
		 			velX *= 1.120F;
		 		}
		 		
		 		if(velX > 8.0F){
		 			velX = 8.0F;
		 		}
    		} else if(dir == Dir.LEFT){
    			if(initPosX > 0){
    				initPosX -= velX;
    			}
    			
		 		if(velX <= 8.0F && onGround()){
		 			velX *= 1.25F;
		 		} else if (velX <= 8.0F && !onGround()){
		 			velX *= 1.120F;
		 		}
		 		
		 		if(velX > 8.0F){
		 			velX = 8.0F;
		 		} else if (velX < -8.0F){
		 			velX = -8.0F;
		 		}
    		}
    	} else if(isRunning){//X decel
    		if(onGround()){
        		velX *= 0.88F;
    		} else if (!onGround()){
    			velX *= 0.975F;
    		}

    		if(inBoundsX()){
    			if(dir == Dir.RIGHT){
    				initPosX += velX;
    			} else if (dir == Dir.LEFT){
    				initPosX -= velX;
    			}
    		}
    		
    		if(velX <= 2.0F){
        		isRunning = false;	
    		}
    	}
	 		
    	//jumping
    	if(isJumping)
        {
    		if(jumpPressed() || (isJumping && jumpVel > 8.0F)){
				if(jumpVel > minVel){
		       	 initPosY -= jumpVel;
		       	 jumpVel *= 0.9F;
				}
    		} else {
    			isJumping = false;
    		}
       	 }
        
    	//falling
        if(!onGround() && (!isJumping || jumpVel < minVel)){
       	 	initPosY += fallVel;
       		 isJumping = false;
       		 if(fallVel < terminalVelocity){
       			 fallVel *= 1.1;
       		 }
        }
        
        //reset vels once on ground
        if(onGround() && (fallVel != 3.0F || jumpVel != 16.0F)){		//reset vanilla velocity values once on ground
       	 fallVel = 3.0F;
       	 jumpVel = 16.0F;
        }                   
        
        if(!bullets.isEmpty()){//bullet logic
   		 	for (int i = 0; i < bullets.size(); i++) {
		        Projectile theBullet = bullets.get(i);
		        
		        theBullet.x += theBullet.velX;
	            if(!theBullet.inBounds(width, height)){
	           	 	bullets.remove(i);
	        	}   
   		 	}
        }
	}

	private static void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        switch (state) {
        
            case INTRO:

                glColor3f(0.065F, 0.065F, 0.065F);
                glRectf(0, 0, 640, 480);
                
                //RENDER LOGO
                if(logo != null){

	            	logo.bind();
	                glBegin(GL_TRIANGLES);
	                /*
	                 * 192 PIXELS ON EACH SIDE OF LOGO, 
	                 * 112 ON TOP AND BOTTOM 
	                 * 
	                 * pseudo-code: ((WIDTH - PNGWIDTH)/2, (HEIGHT- PNGHEIGHT)/2)	               
	                */
	                
	                glTexCoord2f(1, 0);
	                glVertex2i(450, 10);
	                glTexCoord2f(0, 0);
	                glVertex2i(10, 10);
	                glTexCoord2f(0, 1);
	                glVertex2i(10, 450);
	                glTexCoord2f(0, 1);
	                glVertex2i(10, 450);
	                glTexCoord2f(1, 1);
	                glVertex2i(450, 450);
	                glTexCoord2f(1, 0);
	                glVertex2i(450, 10);
	                glEnd();
                } else {
                	System.out.println("logo1 null");
                }
                if(fade >= 0){
                    glColor4f(0F, 0F, 0F, (float) Math.sin(Math.toRadians(fade)));
                    glRectf(0, 0, 640, 480); 
                    System.out.println(fade);
                	fade -= 2F;
                } else if(fade <= 0){
                	fade = 90;
                	state = State.MAIN_MENU;
                }               
                break;
                
            case GAME:
				 glColor3f(0.065f, 0.065f, 0.065f);
				 glRectf(0, 0, 640, 480);
				 glBegin(GL_QUADS);
             // >> glVertex commands are used within glBegin/glEnd pairs to specify point, line, and polygon vertices.
             // >> glColor sets the current colour. (All subsequent calls to glVertex will be assigned this colour)
             // >> The number after 'glVertex'/'glColor' indicates the amount of components. (xyzw/rgba)
             // >>      (for 'glVertex' = d: Double, f: Float, i: Integer)
             // >>      (for 'glColor'  = d: Double, f: Float, b: Signed Byte, ub: Unsigned Byte)         
             
             
             //RENDER BULLETS
             if(!bullets.isEmpty()){
        		 for (int i = 0; i < bullets.size(); i++) {
    		        Projectile theBullet = bullets.get(i);
    		        if(theBullet.dir == 0){//FACING RIGHT
    		             glColor3b((byte) 0, (byte) 0, (byte) 0);									//BLACK
    		             glVertex2f(theBullet.x, theBullet.y);										// Upper-left
    		             glColor3b((byte) 50, (byte) 0, (byte) 0);									//RED
    		             glVertex2f(theBullet.x + theBullet.width, theBullet.y);					// Upper-right
    		             glColor3b((byte) 50, (byte) 0, (byte) 0);									//RED
    		             glVertex2f(theBullet.x + theBullet.width, theBullet.y + theBullet.height); // Bottom-right
    		             glColor3b((byte) 0, (byte) 0, (byte) 0);									//BLACK
    		             glVertex2f(theBullet.x, theBullet.y + theBullet.height);					// Bottom-left
    	             } else if (theBullet.dir == 1){//FACING LEFT
    		             glColor3b((byte) 50, (byte) 0, (byte) 0);				//RED
    		             glVertex2f(theBullet.x, theBullet.y);                                 		// Upper-left
    		             glColor3b((byte) 0, (byte) 0, (byte) 0);				//Black
    		             glVertex2f(theBullet.x + theBullet.width, theBullet.y);                   	// Upper-right
    		             glColor3b((byte) 0, (byte) 0, (byte) 0);				//black
    		             glVertex2f(theBullet.x + theBullet.width, theBullet.y + theBullet.height); // Bottom-right
    		             glColor3b((byte) 50, (byte) 0, (byte) 0);				//RED
    		             glVertex2f(theBullet.x, theBullet.y + theBullet.height);                  	// Bottom-left 		             
    	             }
            	 }            	
             }
             
             //RENDER SPRITE
             if(dir == Dir.RIGHT){
	             glColor3b((byte) 0, (byte) 0, (byte) 0);				//BLACK
	             glVertex2f(initPosX, initPosY);                                 // Upper-left
	             glColor3b((byte) 50, (byte) 50, (byte) 100);			//BLUE
	             glVertex2f(initPosX + spriteWidth, initPosY);                   // Upper-right
	             glColor3b((byte) 50, (byte) 50, (byte) 100);			//BLUE
	             glVertex2f(initPosX + spriteWidth, initPosY + spriteHeight);    // Bottom-right
	             glColor3b((byte) 0, (byte) 0, (byte) 0);				//BLACK
	             glVertex2f(initPosX, initPosY + spriteHeight);                  // Bottom-left
             } else if (dir == Dir.LEFT){
	             glColor3b((byte) 50, (byte) 50, (byte) 100);			//BLUE
	             glVertex2f(initPosX, initPosY);                                 // Upper-left
	             glColor3b((byte) 0, (byte) 0, (byte) 0);				//Black
	             glVertex2f(initPosX + spriteWidth, initPosY);                   // Upper-right
	             glColor3b((byte) 0, (byte) 0, (byte) 0);				//black
	             glVertex2f(initPosX + spriteWidth, initPosY + spriteHeight);    // Bottom-right
	             glColor3b((byte) 50, (byte) 50, (byte) 100);			//BLUE
	             glVertex2f(initPosX, initPosY + spriteHeight);                  // Bottom-left
             }
             glEnd();
             
             //RENDER PAUSE MENU
             if(paused){
	             glColor4b((byte) 50, (byte) 50, (byte) 100, (byte)60);
	             glRectf(width - 175, height - 150, 175, 150);
	             //TextRenderer.renderString("Paused", TextRenderer.fontTexture, 16, 0.9F, 0, 0.3F, 0.225F);	             
             }
             
             if(fade >= 0){
                 glColor4f(0F, 0F, 0F, (float) Math.sin(Math.toRadians(fade)));
                 glRectf(0, 0, 640, 480); 
                 System.out.println(fade);
             	fade -= 2F;
             }             
                break;
            case MAIN_MENU:
            	glColor3f(0.065f, 0.065f, 0.065f);
                glRectf(0, 0, 640, 480);
                if(logo != null){
	                //RENDER LOGO
	            	logo.bind();
	                glBegin(GL_TRIANGLES);
	                /*
	                 * 192 PIXELS ON EACH SIDE OF LOGO, 
	                 * 112 ON TOP AND BOTTOM 
	                 * 
	                 * pseudo-code: ((WIDTH - PNGWIDTH)/2, (HEIGHT- PNGHEIGHT)/2)
	                 * 
	                */
	                
	                //glColor3f(0.9f, 0.9f, 0.9f); //MAKES THE CLEAR PART OF THE SCREEN WHITE
	                glTexCoord2f(1, 0);
	                glVertex2i(640, 0);//UPPER RIGHT
	                glTexCoord2f(0, 0);
	                glVertex2i(0, 0); //UPPER LEFT
	                glTexCoord2f(0, 1);
	                glVertex2i(0, 480);
	                glTexCoord2f(0, 1);
	                glVertex2i(0, 480);
	                glTexCoord2f(1, 1);
	                glVertex2i(640, 480);
	                glTexCoord2f(1, 0);
	                glVertex2i(640, 0);
	                glEnd();
                } else {
                	System.out.println("logo1 null");
                }
                break;
        }
    }
	
    private static void checkInput() {
    	
        switch (state) {
            case INTRO:
            	while (Keyboard.next()) {
	                if (Keyboard.next()) {
	                    state = State.MAIN_MENU;
	                }
	                if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
	                    Display.destroy();
	                    AL.destroy();
	                    System.exit(0);
	                }
            	}
                break;
            case GAME:
            	while (Keyboard.next()) {
	            	//PAUSE
	            	if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
	            		if(!paused){
	            			paused = true;
	            		} else if (paused){
	            			paused = false;
	            		}            	
	 	            }
	            	if(!paused){
		            	//RUNNING
			            if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)){
			 	                if(initPosX > 0)
			 	                {
			 	                	dir = Dir.LEFT;
		
			 				 		if(!isRunning){
			 				 			isRunning = true;
			 				 		}	 				 	
			 	                }	 	                
			 	            } else if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			 				 	if(initPosX < width - spriteWidth)
			 				 	{
			 				 		dir = Dir.RIGHT;
			 				 		if(!isRunning){
			 				 			isRunning = true;
			 				 		}
			 				 	}
			 	            }
			 			 
			             //JUMPING
			 			 if (jumpPressed() && (isJumping == false) && (onGround() && !flyEnabled)) {
			                    jump();
			                }
			 			 
			 			 //SHOOTING
			 			 if (Keyboard.isKeyDown(Keyboard.KEY_X)){
			                    shoot();
			                }
			 			 
			 			//FLYING
			 			 if(flyEnabled){			
				 			 if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				 				 	if(initPosY > 0){
				 	                	initPosY = initPosY - velX;
				 				 	}
				 	            }
				 			 
				 			 if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			 					 if(initPosY < height - spriteHeight - 75){
			 					 		initPosY = initPosY + velX;
			 					 }
				 			 }	
			 			 }
	            	}
            	}
                break;
            case MAIN_MENU:   
            	while (Keyboard.next()) {
	            	if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
	                    Display.destroy();
	                    AL.destroy();
	                    System.exit(0);
	                }
	                if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
	                    state = State.GAME;
	                }
            	}
                break;
        }
    }
    
    public static void jump()
    {
    	try {
			playSound(JUMP);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	isJumping = true;
    }
    
    public static boolean onGround(){
    	if(initPosY >= height - spriteHeight - 75){
    		return true;
    	}else {
    		return false;
    	}
    }
    
    public static void shoot(){
    	//play the sound
    	
    	try {
			playSound(SHOOT);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	//SoundPlayer.playSound(SoundPlayer.SHOOT);
    	//create and shoot the bullet
    	if(dir == Dir.RIGHT){
    		bullets.add(new Projectile(initPosX, initPosY + 10, 20, 0, 0));//x, x, velX, velY, direction(0 = right, 1 = left
    	} else if(dir == Dir.LEFT){
    		bullets.add(new Projectile(initPosX, initPosY + 10, -20, 0, 1));
    	}
    }
    
    public static boolean jumpPressed(){
    	if(Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Keyboard.isKeyDown(Keyboard.KEY_W)){
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static boolean inBoundsX(){
    	if(initPosX < width - spriteWidth && initPosX > 0){
    		return true;
    	} else {
    		return false;
    	}
    }
  
    public static Texture loadTexture(String key){
    	try {
			//return TextureLoader.getTexture("PNG", new FileInputStream(new File("res/" + key + ".png")));
    		return TextureLoader.getTexture("PNG", new FileInputStream(new File("res/logo1.png")));
		} catch (IOException e) {
            System.out.println("texture is null");
			e.printStackTrace();
            Display.destroy();
            AL.destroy();
            System.exit(1);
        	return null;
		}
    }

    /*
     *  Check for keyboard hit
     */  
    public boolean kbhit() {
	  	try {
	  	  return (System.in.available() != 0);
	  	} catch (IOException ioe) {
	  		//crash();
	  	}
	  	return false;
    }
    
    void terminate(){
    	//move repeated "free resources and exit" code here
    	//soundplayer.killALData(); REPLACE WITH NEW AL RELEASE CODE
        logo.release();
		Display.destroy();
		AL.destroy();//remove once soundplayer is properly implemented
		System.exit(0);
    }
}


//NOTES

/*
 *			Immediate mode drawing
 *
 *	In this tutorial I will use the immediate drawing mode. The immediate drawing mode, as well as many other 
 *	things, was deprecated in OpenGL 3.0, but many of the same principles still apply. This video covers 
 *	alternate rendering methods.
 *
 *			Where to find the methods
 *
 *	All the OpenGL methods are static. You can find the OpenGL 1.1 methods in the org.lwjgl.opengl.GL11 class. 
 *	Note that '11' stands for version '1.1'. Similarly, you can find the methods that were added in OpenGL 1.5 
 *	in the org.lwjgl.opengl.GL15 class. If you want to have some documentation, be sure to have a look at the 
 *	OpenGL 2.1 manual pages.
 * 
 * */

//Terminology:
//- Vertex: a point in either 2D or 3D space
//- Primitive: a simple shape consisting of one or more vertices
