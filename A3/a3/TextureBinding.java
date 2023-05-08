package a3;

import java.nio.*;
import java.lang.Math;

import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import org.joml.*;

public class TextureBinding{
	
	protected String texturePath;
	protected int memoryLocation;
	protected int textureType;

	public TextureBinding(String texturePath){
		this.setTexture(texturePath);
	}
	
	public TextureBinding(int textureMemPos, int textureType){
		
		this.texturePath = "pre-defined texture";
		this.memoryLocation = textureMemPos;
		this.textureType = textureType;
		
	}
	
	public void setTexture(String texturePath){
		this.texturePath = texturePath;
		this.memoryLocation = Utils.loadTexture(texturePath);
		this.textureType = GL_TEXTURE_2D;
	}
	
	public int getLocation(){
		int memLoc = this.memoryLocation;
		return(memLoc);
	}
	
	public String getPath(){
		return(""+this.texturePath);
	}
	
	public int getTextureType(){
		int tempTexType = this.textureType;
		return(tempTexType);
	}
}