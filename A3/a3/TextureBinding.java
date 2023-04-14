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
	
	private String texturePath;
	private int memoryLocation;

	public TextureBinding(String texturePath){
		this.setTexture(texturePath);
	}
	
	public void setTexture(String texturePath){
		this.texturePath = texturePath;
		this.memoryLocation = Utils.loadTexture(texturePath);
	}
	
	public int getLocation(){
		int memLoc = this.memoryLocation;
		return(memLoc);
	}
	
	public String getPath(){
		return(""+this.texturePath);
	}
}