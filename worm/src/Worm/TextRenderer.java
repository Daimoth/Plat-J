package Worm;

import de.matthiasmann.twl.utils.PNGDecoder;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

//renderString(renderString.toString(), fontTexture, 16, -0.9F, 0, 0.3F, 0.225F);	


public class TextRenderer {

	//The texture object(id?) for the bitmap font
	public static int fontTexture;
	
	public static void setupTextures() throws IOException{
		//create a new texture for the bitmap font
		fontTexture = glGenTextures();
		//bind the texture object to the target, specify that it will be 2d
		glBindTexture(GL_TEXTURE_2D, fontTexture);
		//load the .png with twl utils
		PNGDecoder decoder = new PNGDecoder(new FileInputStream("res/font3.png"));//font url
		ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
		decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
		buffer.flip();
		//load the previously loaded texture data onto the texture object
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB/*A*/, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		//unbind the texture
		glBindTexture(GL_TEXTURE_2D, 0);//unbind
	}	
	
	public static void renderString(String string, int textureObject, int gridSize, float x, int y, float characterWidth, float characterHeight){
		glClear(GL_COLOR_BUFFER_BIT);
		glPushAttrib(GL_TEXTURE_BIT | GL_ENABLE_BIT);	
		glEnable(GL_CULL_FACE);
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, textureObject);//rebind
		//enable linear texture filtering for smoothening
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER ,GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER ,GL_LINEAR);
		//enable additing blending, which means that colors will be blended with already added colors
		//in the frame buffer. In practice, this makes the black parts of the texture become invisible.
		glEnable(GL_BLEND);
		glBlendFunc(GL_ONE, GL_ONE);
		//store the current model-view matrix
		glPushMatrix();
		//offset all (at least up to glPopMatrix) vertex coordinates
		glTranslatef(x, y, 0);
		glBegin(GL_QUADS);
		//iterate over all the characters in the string
		for(int i = 0; i < string.length(); i++){
			//get ASCII code by casting to int
			int asciiCode = (int)string.charAt(i);
			//there are 16 cells to a texture, and cell coords range from 0.0 to 1.0
			final float cellSize = 1.0F / gridSize;
			float cellX = ((int)asciiCode % gridSize) * cellSize;
			float cellY = ((int)asciiCode / gridSize) * cellSize;
			glTexCoord2f(cellX, cellY + cellSize);
			glVertex2f(i * characterWidth / 3, y);
			glTexCoord2f(cellX + cellSize, cellY + cellSize);
			glVertex2f(i * characterWidth / 3 + characterWidth / 2, y);
			glTexCoord2f(cellX + cellSize, cellY);
			glVertex2f(i * characterWidth / 3 + characterWidth / 2, y + characterHeight);
			glTexCoord2f(cellX, cellY);
			glVertex2f(i * characterWidth / 3, y + characterHeight);
		}
		glEnd();
		glPopMatrix();
		glPopAttrib();
	}
	
	
}
