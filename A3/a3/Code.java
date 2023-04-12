package a3;

import java.nio.*;
import java.io.*;
import java.lang.Math;
import java.time.Instant;
import java.time.Duration;

import javax.swing.*;

import java.util.ArrayList;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import org.joml.*;

//Note no need too import the ImportedModel class

public class Code extends JFrame implements GLEventListener
{	private GLCanvas myCanvas;
	Instant snap;
	long elapsedTime;
	int frameCycle;
	private int renderingProgram;
	
	//VAO and VBO initialization
	//Always only one VAO but potentially 3 or more VBOs
	private int vao[] = new int[1];
	private int vbo[] = new int[2];
	
	private float cameraX, cameraY, cameraZ;
	
	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mvStack = new Matrix4fStack(5);
	private Matrix4f pMat = new Matrix4f();
	private int mvLoc, pLoc;
	private float aspect;
	private double tf;
	private float cameraRotation = 0;
	
	private DrawableModel anvil;
	private DrawableModel caltrop;
	private DrawableModel glaidus;
	private DrawableModel ringRune;
	
	private float camYaw = 0f; //Side to side
	private float camPitch = 0f; //up down
	private float camRoll = 0f; //roll screen
	
	
	
	private AxisState fwdAxis = new AxisState(0.1f, KeyEvent.VK_S, KeyEvent.VK_W);
	private AxisState sideAxis = new AxisState(0.1f, KeyEvent.VK_D, KeyEvent.VK_A);
	private AxisState verticalAxis = new AxisState(0.1f, KeyEvent.VK_Q, KeyEvent.VK_E);
	private AxisState pitchTurnAxis = new AxisState(0.01f, KeyEvent.VK_UP, KeyEvent.VK_DOWN);
	private AxisState yawTurnAxis = new AxisState(0.01f, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
	boolean showAxis = true;
	

	public Code()
	{	setTitle("Chapter 4 - program 4");
		setSize(600, 600);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
		
		//Control inputs
		this.addKeyListener(new KeyAdapter() {
			
			public void keyPressed(KeyEvent e){
				
				pitchTurnAxis.pressCheck(e.getKeyCode());
				yawTurnAxis.pressCheck(e.getKeyCode());
				
				fwdAxis.pressCheck(e.getKeyCode());
				sideAxis.pressCheck(e.getKeyCode());
				verticalAxis.pressCheck(e.getKeyCode());
				
				
			}
			
			public void keyReleased(KeyEvent e){
				
				pitchTurnAxis.releaseCheck(e.getKeyCode());
				yawTurnAxis.releaseCheck(e.getKeyCode());
				
				fwdAxis.releaseCheck(e.getKeyCode());
				sideAxis.releaseCheck(e.getKeyCode());
				verticalAxis.releaseCheck(e.getKeyCode());
				
				if(e.getKeyCode() == KeyEvent.VK_SPACE){
					showAxis = !showAxis;
				}
			}
			
		});
		
		/*
			Strategy:
			
			instead of trying to use the key listeners which don't seem to work here, we'll use the KeyStroke system which will 
			
			https://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyAdapter.html
			
			https://docs.oracle.com/javase/8/javafx/api/javafx/scene/input/KeyCode.html
			
			https://docs.oracle.com/javase/7/docs/api/java/awt/AWTKeyStroke.html
			
			https://docs.oracle.com/javase/7/docs/api/javax/swing/KeyStroke.html#getKeyStroke(char,%20boolean)
			
			https://jogamp.org/deployment/jogamp-current/javadoc/jogl/javadoc_jogl_spec/com/jogamp/opengl/GL2ES2.html#glVertexAttribPointer(int,int,int,boolean,int,long)
		
		
		//*/
		
	}
	
	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		elapsedTime = Duration.between(snap,Instant.now()).toMillis();
		snap = Instant.now();
		
		//System.out.println("Elapsed time = " + elapsedTime);
		
		float frames = elapsedTime/16f;
		frameCycle += frames;
		frameCycle %= 60;
		
		//System.out.println("Frames Elapsed = " + frames);
		
		gl.glUseProgram(renderingProgram);

		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		
		//gl.glMatrixMode(GL_PROJECTION);
		
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		

		// push view matrix onto the stack
		mvStack.pushMatrix();
		//So this translates the camera
		//*
		//Camera rotation radians needs to be done as a mod of 6.28319
		//in order to ensure that the camera completes a full circle
		cameraRotation = 6.28319f;
		//https://www.geeksforgeeks.org/java-todegrees-method-example/
		//https://www.tutorialspoint.com/java/number_abs.htm
		
		this.camYaw = (this.camYaw + yawTurnAxis.getValue()/frames) % cameraRotation; //Side to side
		
		//We're going to limit the vertical axis to 90 and -90
		camPitch = (camPitch + pitchTurnAxis.getValue()/frames); //up down
		if(Math.abs(camPitch) > (Math.PI/2)-0.05f){
			camPitch = (((float)Math.PI/2)-0.05f)*(camPitch/Math.abs(camPitch));
		}
		camRoll = 0f; //roll screen
		
		//Instead of having the angle assigned to the first values
		//we can set the angle to 1f as the magnitude of change,
		//and then assign a rotation angle to each axis
		
		cameraZ += fwdAxis.getValue()/frames;
		cameraY += verticalAxis.getValue()/frames;
		cameraX += sideAxis.getValue()/frames;
		
		mvStack.translate(-cameraX, -cameraY, -cameraZ);
		
		Vector3f cameraEye = new Vector3f(-cameraX, -cameraY, -cameraZ);
		
		Vector3f lookTarget = new Vector3f((float)Math.sin(this.camYaw)*1.0f, 0.0f, (float)Math.cos(this.camYaw)*1.0f*(float)Math.cos(camPitch));
		
		//For up down camera rotation we're going to fix the rotation to -90 and 90 as our bounds
		lookTarget.add(new Vector3f(0f, (float)Math.sin(camPitch)*1.0f, 0f));
		
		lookTarget.add(cameraEye);
		Vector3f upReference = new Vector3f(0,1,0);
		
		mvStack.setLookAt(cameraEye, lookTarget, upReference);
		
		tf = 5f;  // time factor
		
		//*
		// ----------------------  pyramid == sun  
		
		//Push translation matrix
		mvStack.pushMatrix();
		mvStack.translate(0.0f, 0.0f, 0.0f);
		
		//Then push rotations
		mvStack.pushMatrix();
		
		if(showAxis){
			
			mvStack.translate(10f,0f,0f);
			gl.glUniformMatrix4fv(mvLoc,1,false,mvStack.get(vals));
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
			gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0,0);
			gl.glEnableVertexAttribArray(0);
			gl.glDrawArrays(GL_LINES, 0, 6);
		}
		mvStack.popMatrix();
		mvStack.popMatrix();
		//-----------------------  cube == planet  -- converted to 4-face pyramid
		mvStack.pushMatrix();
		mvStack.translate((float)Math.sin(tf/frames)*4.0f, 0.0f, (float)Math.cos(tf/frames)*4.0f);
		mvStack.pushMatrix();
		mvStack.rotate((float)tf/frames*0.5f, 0.0f, 1.0f, 0.0f);
		gl.glUniformMatrix4fv(mvLoc, 1, false, mvStack.get(vals));
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glDrawArrays(GL_TRIANGLES, 0, 12);
		
		
		mvStack.popMatrix();
		mvStack.popMatrix();
		
		anvil.render(mvStack,pMat);
		
		glaidus.render(mvStack,pMat);
		ringRune.render(mvStack,pMat);
		
		caltrop.rotate(new Vector3f(0,0.005f/frames,0));
		caltrop.setScale(new Vector3f((float)Math.sin(((frameCycle-30)/15f)+0.1f)*0.5f,(float)Math.sin(((frameCycle-30)/15f)+0.1f)*0.5f,(float)Math.sin(((frameCycle-30)/15f)+0.1f)*0.5f));
		
		caltrop.render(mvStack,pMat);
		
		caltrop.translate(new Vector3f(-4f,0,0));
		
		caltrop.render(mvStack,pMat);
		caltrop.translate(new Vector3f(4f,0,0));
		
		//*/
		mvStack.popMatrix();
		
	}
	
	
	//Init
	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		
		snap = Instant.now();
		frameCycle = 0;
		
		/*//It seems that models and textures are loaded during initialization...
		//(Program 6 Shuttle loader)
		
		//*/
		
		
		/*//The perspective matrix is also setup here (Program 6 Shuttle loader)
		
		//*/
		
		renderingProgram = Utils.createShaderProgram("a3/vertShader.glsl", "a3/fragShader.glsl");
		setupVertices();
		
		int simpleObjRenderer = Utils.createShaderProgram(
			"a3/objVertShader.glsl", "a3/objFragShader.glsl"
		);
		
		//importing obj models
		anvil = (new DrawableModel("Anvil--02--Triangulated.obj","Anvil_Laptop_Sleeve.png", simpleObjRenderer));
		
		caltrop = (new DrawableModel("CaltropStar.obj","castleroof.jpg", simpleObjRenderer));
		
		//Texture Source http://texturelib.com/texture/?path=/Textures/metal/bare/metal_bare_0012
		
		glaidus = new DrawableModel("Gladius_Single.obj","metal_bare_0012_01_s.jpg",simpleObjRenderer);
		
		ringRune = new DrawableModel("GroundRing.obj","RunicRingSegment.png",simpleObjRenderer);
		
		anvil.loadModelData();
		anvil.setupVertices(vao,0);
		anvil.translate(new Vector3f(0f,1f,0f));
		
		caltrop.loadModelData();
		caltrop.setupVertices(vao,0);
		caltrop.translate(new Vector3f(2f,10f,0f));
		
		glaidus.loadModelData();
		glaidus.setupVertices(vao,0);
		glaidus.translate(new Vector3f(0,5f,0f));
		glaidus.setScale(new Vector3f(0.2f,0.2f,0.2f));
		
		ringRune.loadModelData();
		ringRune.setupVertices(vao,0);
		ringRune.setScale(new Vector3f(6f,0f,6f));
		ringRune.translate(new Vector3f(0f,-0.5f,0f));
		
		cameraX = 0.0f; cameraY = 0.0f; cameraZ = 12.0f;
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		
		/*//Process for preparing a .obj file from chapter 6
		
		//*/
		
		
		float[] worldAxesPositions =
		{	
			1.0f,  0, 0, 0, 0, 0, 
			0,  1.0f, 0, 0, 0, 0,
			0,  0, 1.0f, 0, 0, 0, 
		};
		
		float[] pyramidPositions =
		{	-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,      //front
			-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
		};

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(worldAxesPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit()*4, cubeBuf, GL_STATIC_DRAW);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer pyrBuf = Buffers.newDirectFloatBuffer(pyramidPositions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrBuf.limit()*4, pyrBuf, GL_STATIC_DRAW);
	}

	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}
}