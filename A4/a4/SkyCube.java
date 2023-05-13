package a4;

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

//Largely derived from the implemention found in Chapter 9 Program 2
public class SkyCube extends DrawableModel{
	
	protected int vbo[] = new int[4];
	protected String primaryTexturePath;
	protected int primaryModelTexture;
	
	//Making children public so that it is easier to edit them
	public DrawableModel[] child;
	
	public SkyCube(String primaryTexturePath, int renderingProgram){
		
		super("", primaryTexturePath, renderingProgram);
		
		this.primaryTexturePath = primaryTexturePath;
		
		this.globalPosition = new Vector3f(0f,0f,0f);
		this.rotation = new Vector3f(0f,0f,0f);
		this.scale = new Vector3f(1f,1f,1f);
		
		this.renderingProgram = renderingProgram;
		this.loadModelData();
		
	}
	
	@Override
	public void addTexture(int textureUnit, String texturePath){
		
	}
	
	@Override
	public void loadModelData(){
		GL4 gl = (GL4) GLContext.getCurrentGL();
		this.primaryModelTexture = Utils.loadCubeMap(this.primaryTexturePath);
		gl.glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
		
	}
	
	@Override
	public void setupVertices(int vao[], int vaoIndex){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		float[] cubeVertexPositions =
		{	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[vaoIndex]);
		gl.glGenBuffers(4, vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cvertBuf = Buffers.newDirectFloatBuffer(cubeVertexPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cvertBuf.limit()*4, cvertBuf, GL_STATIC_DRAW);
		
	}
	
	@Override
	public void render(Matrix4fStack stackMat, Matrix4f perspectiveMat){
		
		this.startRenderer();
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
		
		//Get the matrices 
		int mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		int pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		
		stackMat.pushMatrix();
		this.createModelMatrix(stackMat);
		
		
		gl.glUniformMatrix4fv(mvLoc, 1, false, stackMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, perspectiveMat.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, primaryModelTexture);
		
		gl.glEnable(GL_REPEAT);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	     // cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		
		//this.bindTextures();
		
		gl.glEnable(GL_DEPTH_TEST);
		
		this.renderChildren(stackMat, perspectiveMat);
		
		stackMat.popMatrix();
	}
}