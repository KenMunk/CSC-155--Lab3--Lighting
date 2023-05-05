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

public class CubeMapBinding extends TextureBinding{
	

	public CubeMapBinding(String texturePath){
		super(texturePath);
		this.setTexture(texturePath);
	}
	
	@Override
	public void setTexture(String texturePath){
		this.texturePath = texturePath;
		this.memoryLocation = Utils.loadCubeMap(texturePath);
		this.textureType = GL_TEXTURE_CUBE_MAP;
	}
	
}