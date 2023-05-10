package a3;

import java.io.*;
import java.lang.Math;
import java.nio.*;
import java.util.*;
import java.awt.Color;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.common.nio.Buffers;
import org.joml.*;

public abstract class Texture3D{
	
	protected int textureBinding;
	
	protected int width;
	protected int height;
	protected int depth;
	
	//private double[][][] textureSpace;
	
	public Texture3D(int width, int height, int depth){
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	protected abstract void fillDataArray(byte data[]);
	
	public void bindTexture(){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		
		byte[] textureSpace = new byte[this.width*this.height*this.depth*4];
		
		fillDataArray(textureSpace);
		
		ByteBuffer texByteBuffer = Buffers.newDirectByteBuffer(textureSpace);
		
		//Note, can't really define this as a straight
		//integer because the integer reference will not be
		//passed to anything when used in a parameter
		// #JavaThings
		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];
		
		gl.glBindTexture(GL_TEXTURE_3D, textureID);
		
		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, this.width, this.height, this.depth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				this.width, this.height, this.depth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texByteBuffer);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		
		this.textureBinding = textureID;
		
	}
	
	
}