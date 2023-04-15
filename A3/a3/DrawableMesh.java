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

import java.util.HashMap;

//The idea at play here is that the drawable model will integrate the mvstack by performing all operations before going on to its children then popping its own level of the stack.

public class DrawableMesh extends DrawableModel{


	public DrawableMesh(
		String modelPath, 
		String primaryTexturePath, 
		int renderingProgram,
		Vector3f position,
		Vector3f rotation,
		Vector3f scale
	){
		super(modelPath, primaryTexturePath, renderingProgram, position, rotation, scale);
		this.modelPath = modelPath;
	}
	
	@Override
	protected void draw(){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glDrawArrays(GL_LINES, 0, model.getNumVertices());
	}
}