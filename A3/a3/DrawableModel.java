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

public class DrawableModel{
	
	protected Vector3f globalPosition;
	protected Vector3f rotation;
	protected Vector3f scale;
	
	protected String modelPath;
	
	protected ImportedModel model;
	protected int numObjVertices;
	
	protected int vbo[] = new int[3];
	
	protected int renderingProgram;
	
	//For lab4 I really want to convert this to a hashmap
	//Making children public so that it is easier to edit them
	public DrawableModel[] child;
	
	/*
	Storing other matrices into a hash map 
	so it's easier to support other shaders
	*/
	private HashMap<String, Matrix4fc> otherMatrices;
	private HashMap<String, Vector4f> vector4Properties;
	
	/*
	Storing other textures in a hash map for the same reason
	*/
	private HashMap<Integer, TextureBinding> textures; 
	
	public DrawableModel(String modelPath, String primaryTexturePath, int renderingProgram){
		
		this.modelPath = modelPath;
		
		this.globalPosition = new Vector3f(0f,0f,0f);
		this.rotation = new Vector3f(0f,0f,0f);
		this.scale = new Vector3f(1f,1f,1f);
		
		this.renderingProgram = renderingProgram;
		
		this.otherMatrices = new HashMap<String, Matrix4fc>();
		this.textures = new HashMap<Integer, TextureBinding>();
		this.vector4Properties = new HashMap<String, Vector4f>();
		
		this.addTexture(GL_TEXTURE0, primaryTexturePath);
	}
	
	
	public DrawableModel(
		String modelPath, 
		String primaryTexturePath, 
		int renderingProgram,
		Vector3f position,
		Vector3f rotation,
		Vector3f scale
	){
		
		this.modelPath = modelPath;
		
		this.globalPosition = new Vector3f(0f,0f,0f);
		this.rotation = new Vector3f(0f,0f,0f);
		this.scale = new Vector3f(1f,1f,1f);
		
		this.renderingProgram = renderingProgram;
		
		this.otherMatrices = new HashMap<String, Matrix4fc>();
		this.textures = new HashMap<Integer, TextureBinding>();
		this.vector4Properties = new HashMap<String, Vector4f>();
		
		this.addTexture(GL_TEXTURE0, primaryTexturePath);
		
		this.translate(position);
		this.rotate(rotation);
		this.setScale(scale);
	}
	
	
	public void loadModelData(){
		
		this.model = new ImportedModel(modelPath);
		
	}
	
	public void setupVertices(){
		this.setupVertices(new int[1],0);
	}
	
	public void setupVertices(int vao[], int vaoIndex){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		numObjVertices = model.getNumVertices();
		Vector3f[] vertices = model.getVertices();
		Vector2f[] texCoords = model.getTexCoords();
		Vector3f[] normals = model.getNormals();
		//Need to get indicies
		
		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   = (float) (vertices[i]).x();
			pvalues[i*3+1] = (float) (vertices[i]).y();
			pvalues[i*3+2] = (float) (vertices[i]).z();
			tvalues[i*2]   = (float) (texCoords[i]).x();
			tvalues[i*2+1] = (float) (texCoords[i]).y();
			nvalues[i*3]   = (float) (normals[i]).x();
			nvalues[i*3+1] = (float) (normals[i]).y();
			nvalues[i*3+2] = (float) (normals[i]).z();
		}
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		//vaoIndex would typically be 0 for this class
		gl.glBindVertexArray(vao[vaoIndex]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer normBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, normBuf, GL_STATIC_DRAW);
		
	}
	
	public void addChild(DrawableModel newChild){
		newChild.loadModelData();
		newChild.setupVertices();
		
		if(this.child == null ){
			this.child = new DrawableModel[1];
			this.child[0] = newChild;
		}
		else{
			DrawableModel[] updatedChildren = new DrawableModel[this.child.length+1];
			
			for(int i = 0; i<this.child.length; i++){
				updatedChildren[i] = this.child[i];
			}
			
			updatedChildren[updatedChildren.length-1] = newChild;
		}
		
	}
	
	public void translate(Vector3f move){
		this.globalPosition.add(move);
	}
	
	public void rotate(Vector3f move){
		this.rotation.add(move);
		
		float x = this.rotation.x();
		float y = this.rotation.y();
		float z = this.rotation.z();
		
		x %= 6.28319f;
		y %= 6.28319f;
		z %= 6.28319f;
		
		this.rotation = new Vector3f(x,y,z);
	}
	
	public void setScale(Vector3f scale){
		
		this.scale = scale;
		
	}
	
	public void addScale(Vector3f scale){
		this.scale.add(scale);
	}
	
	public Vector3f getPosition(){
		return(this.globalPosition);
	}
	
	public Vector3f getRotation(){
		return(this.rotation);
	}
	
	public void startRenderer(){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glUseProgram(this.renderingProgram);
	}
	
	public void addADSSTextures(
		String ambientColorMapPath,
		String diffuseColorMapPath,
		String specularColorMapPath,
		String shininessMapPath
	){
		
		this.addTexture(GL_TEXTURE0, ambientColorMapPath);
		this.addTexture(GL_TEXTURE1, diffuseColorMapPath);
		this.addTexture(GL_TEXTURE2, specularColorMapPath);
		this.addTexture(GL_TEXTURE3, shininessMapPath);
		
	}
	
	public void addTexture(int textureUnit, String texturePath){
		this.textures.put(textureUnit, new TextureBinding(texturePath));
	}
	
	public void bindTextures(){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		this.textures.forEach((key,target) -> this.bindTexture(key,target));
	}
	
	private void bindTexture(int textureUnit, TextureBinding texture){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glActiveTexture(textureUnit);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		gl.glBindTexture(GL_TEXTURE_2D, texture.getLocation());
	}
	
	public void createModelMatrix(Matrix4fStack stackMat){
		
		stackMat.translate(this.getPosition());
		stackMat.scale(this.scale);
		stackMat.rotateX(this.getRotation().x());
		stackMat.rotateY(this.getRotation().y());
		stackMat.rotateZ(this.getRotation().z());
	}
	
	public void renderChildren(Matrix4fStack stackMat, Matrix4f perspectiveMat){
		
		if(this.child != null){
			for(int i = 0; i<this.child.length; i++){
				
				int childIndex = i;
				this.otherMatrices.forEach(
					(key,target) -> this.child[childIndex].addOtherMatrix(key,target)
				);
				
				this.child[i].render(stackMat,perspectiveMat);
			}
		}
		
	}
	
	
	public void createNormMatrix(Matrix4fc modelMatrix){
		
		Matrix4f invTrMat = new Matrix4f();
		
		modelMatrix.invert(invTrMat);
		//invTrMat.transpose(invTrMat);
		
		this.addOtherMatrix("norm_matrix", invTrMat);
	}
	
	public void addOtherMatrix(String matRef, Matrix4fc matrix){
		this.otherMatrices.put(matRef, matrix);
	}
	
	protected void bindAllOtherMatrix(){
		this.otherMatrices.forEach(
			(key,target) -> this.bindOtherMatrix(key,target)
		);
	}
	
	protected void bindOtherMatrix(String matRef, Matrix4fc matrix){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
		
		int matLoc = gl.glGetUniformLocation(
			this.renderingProgram, 
			matRef
		);
		
		gl.glUniformMatrix4fv(
			matLoc, 
			1, 
			false, 
			matrix.get(vals)
		);
	}
	
	//Referencing:
	//private HashMap<String, Vector4f> vector4Properties;
	//*
	public void addMultipleVectorProperties(HashMap<String, Vector4f> hashedProperties){
		
		
		hashedProperties.forEach((key,target) -> this.addVectorProperty(key,target));
		
	}
	//*/
	
	public void addVectorProperty(String propRef, Vector4f propertyValue){
		this.vector4Properties.put(propRef, propertyValue);
	}
	
	protected void bindAllProperties(){
		
		this.vector4Properties.forEach(
			(key,target) -> this.bindProperty(key,target)
		);
	}
	
	protected void bindProperty(String propRef, Vector4f propertyValue){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		FloatBuffer vals = Buffers.newDirectFloatBuffer(4);
		
		//float[] testValues = new float[]{propertyValue.x,propertyValue.y,propertyValue.z,propertyValue.w};
		//System.out.println(propRef + " = ");
		float[] lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
		
		int propLoc = gl.glGetUniformLocation(
			this.renderingProgram, 
			propRef
		);
		//*
		gl.glProgramUniform4fv(
			this.renderingProgram,
			propLoc, 
			1, 
			propertyValue.get(vals)
		);/*/
		gl.glProgramUniform4fv(
			this.renderingProgram,
			propLoc, 
			1, 
			lightAmbient,
			0
		);//*/
	}
	
	/*Refactoring to support shaders with different configs
	
		This render method will take a hash map of matrices and
		then bind them all 
	
	*/
	public void render(Matrix4fStack stackMat, Matrix4f perspectiveMat){
		
		this.startRenderer();
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
		
		//Get the matrices 
		int mvLoc = gl.glGetUniformLocation(this.renderingProgram, "m_matrix");
		
		//Keeping this here because it's simply easier
		int pLoc = gl.glGetUniformLocation(this.renderingProgram, "p_matrix");
		
		stackMat.pushMatrix();
		
		//Adds all of the local transforms to the stack
		this.createModelMatrix(stackMat);
		this.createNormMatrix(stackMat);
		
		gl.glUniformMatrix4fv(
			mvLoc, 
			1, 
			false, 
			stackMat.get(vals)
		);
		
		gl.glUniformMatrix4fv(
			pLoc, 
			1, 
			false, 
			perspectiveMat.get(vals)
		);
		
		this.bindAllProperties();
		this.bindAllOtherMatrix();
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glEnable(GL_REPEAT);
		
		this.bindTextures();
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		
		
		gl.glDrawArrays(GL_TRIANGLES, 0, model.getNumVertices());
		
		this.renderChildren(stackMat, perspectiveMat);
		
		stackMat.popMatrix();
	}
	
}