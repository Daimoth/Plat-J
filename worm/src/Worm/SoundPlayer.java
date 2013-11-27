package Worm;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

public class SoundPlayer {
  
	/** Maximum data buffers we will need. */
  public static final int NUM_BUFFERS = 2;
  
  /** Index of JUMP sound */
  public static final int JUMP = 0;
  
  /** Index of SHOOT sound */
  public static final int SHOOT = 1;
  
	/** Buffers hold sound data. */
  IntBuffer buffer = BufferUtils.createIntBuffer(NUM_BUFFERS);

  /** Sources are points emitting sound. */
  IntBuffer source = BufferUtils.createIntBuffer(NUM_BUFFERS);
  
  /** Position of the source sound. */
  FloatBuffer sourcePos = BufferUtils.createFloatBuffer(3*NUM_BUFFERS);

  /*
   * These are 3D cartesian vector coordinates. A structure or class would be
   * a more flexible of handling these, but for the sake of simplicity we will
   * just leave it as is.
   */  
  
  /** Velocity of the source sound. */
  FloatBuffer sourceVel = BufferUtils.createFloatBuffer(3*NUM_BUFFERS);

  /** Position of the listener. */
  FloatBuffer listenerPos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });

  /** Velocity of the listener. */
  FloatBuffer listenerVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });

  /** Orientation of the listener. (first 3 elements are "at", second 3 are "up")
  Also note that these should be units of '1'. */
  FloatBuffer listenerOri = BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f });
  
  public SoundPlayer() {
  	// !CRUCIAL!
	// any buffer that has data added, must be flipped to establish its position and limits
    listenerPos.flip();
    listenerVel.flip();
    listenerOri.flip();
    
    // Initialize OpenAL and clear the error bit.
	try {
		AL.create();
	} catch (LWJGLException le) {
		le.printStackTrace();
	  return;
	}
	AL10.alGetError();
	
	// Load the wav data.
	if(loadALData() == AL10.AL_FALSE) {
	  System.out.println("Error loading data.");
	  return;
	}
	
	setListenerValues();
  }
  
  /**
   * boolean LoadALData()
   *
   *  This function will load our sample data from the disk using the Alut
   *  utility and send the data into OpenAL as a buffer. A source is then
   *  also created to play that buffer.
   */
  int loadALData() {
	    // Load wav data into a buffers.
	AL10.alGenBuffers(buffer);
	
	if(AL10.alGetError() != AL10.AL_NO_ERROR)
	  return AL10.AL_FALSE;
	
	WaveData waveFile = WaveData.create("res/sfx/shoot.wav");
	AL10.alBufferData(buffer.get(JUMP), waveFile.format, waveFile.data, waveFile.samplerate);
	waveFile.dispose();
	
	waveFile = WaveData.create("res/sfx/jump.wav");
	AL10.alBufferData(buffer.get(SHOOT), waveFile.format, waveFile.data, waveFile.samplerate);
	waveFile.dispose();
	
	
	// Bind buffers into audio sources.
	AL10.alGenSources(source);
	
	if(AL10.alGetError() != AL10.AL_NO_ERROR)
	  return AL10.AL_FALSE;
	
	AL10.alSourcei(source.get(JUMP), AL10.AL_BUFFER,   buffer.get(JUMP) );
	AL10.alSourcef(source.get(JUMP), AL10.AL_PITCH,    1.0f          );
	AL10.alSourcef(source.get(JUMP), AL10.AL_GAIN,     1.0f          );
	AL10.alSource (source.get(JUMP), AL10.AL_POSITION, (FloatBuffer) sourcePos.position(JUMP*3));
	AL10.alSource (source.get(JUMP), AL10.AL_VELOCITY, (FloatBuffer) sourceVel.position(JUMP*3));
	AL10.alSourcei(source.get(JUMP), AL10.AL_LOOPING,  AL10.AL_TRUE  );
	
	AL10.alSourcei(source.get(SHOOT), AL10.AL_BUFFER,   buffer.get(SHOOT) );
	AL10.alSourcef(source.get(SHOOT), AL10.AL_PITCH,    1.0f          );
	AL10.alSourcef(source.get(SHOOT), AL10.AL_GAIN,     1.0f          );
	AL10.alSource (source.get(SHOOT), AL10.AL_POSITION, (FloatBuffer) sourcePos.position(SHOOT*3));
	AL10.alSource (source.get(SHOOT), AL10.AL_VELOCITY, (FloatBuffer) sourceVel.position(SHOOT*3));
	AL10.alSourcei(source.get(SHOOT), AL10.AL_LOOPING,  AL10.AL_FALSE  );

// Do another error check and return.
    if(AL10.alGetError() == AL10.AL_NO_ERROR)
      return AL10.AL_TRUE;

    return AL10.AL_FALSE;
  }
  
  /**
   * void setListenerValues()
   *
   *  We already defined certain values for the Listener, but we need
   *  to tell OpenAL to use that data. This function does just that.
   */
  void setListenerValues() {
    AL10.alListener(AL10.AL_POSITION,    listenerPos);
    AL10.alListener(AL10.AL_VELOCITY,    listenerVel);
    AL10.alListener(AL10.AL_ORIENTATION, listenerOri);
  }  

  /**
   * void killALData()
   *
   *  We have allocated memory for our buffers and sources which needs
   *  to be returned to the system. This function frees that memory.
   */
  void killALData() {
    AL10.alDeleteSources(source);
    AL10.alDeleteBuffers(buffer);
  }
  

  
	public void playSound(int sfx) {	
		switch (sfx){
		case JUMP: 
			AL10.alSourcePlay(source.get(JUMP));
			break;
		case SHOOT:
			AL10.alSourcePlay(source.get(SHOOT));	
			break;
		default:
			System.out.println("invalid sound chosen");
			break;
		}
	}
}