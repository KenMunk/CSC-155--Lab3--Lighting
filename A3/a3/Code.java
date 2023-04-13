package a3;

import java.nio.*;
import java.io.*;
import java.lang.Math;
import java.time.Instant;
import java.time.Duration;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import java.util.ArrayList;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
	
	
	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mStack = new Matrix4fStack(5);
	private Matrix4f pMat = new Matrix4f();
	private int mvLoc, pLoc;
	private float aspect;
	private double tf;
	
	//Implementing a hash map to make managing so many models easier
	private Map<String, DrawableModel> model = new HashMap<String, DrawableModel>();
	
	private SkyCube spaceBox;
	
	private Camera mainCamera = new Camera();
	
	private float cameraRotation = 0;
	private float cameraX, cameraY, cameraZ;
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
		this.setFocusable(true);
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
		The concept at play here is the idea that the focus loss event won't happen until the user clicks back into the window.  So in order to re-enable interactability, I just need to force the window focus to be requested once the focus loss event happens
		
		Originally, I wasn't going to go the direction of using the focus in window but that caused an unfortunate situation where the user cannot stop interacting with the app resulting in a practical lock-out of their computer
		
		The focus listener boiler plate code was generated by chatGPT since I was trying to figure out the focus situation
		//https://docs.oracle.com/javase/7/docs/api/java/awt/Component.html#requestFocus(boolean)
		
		*/
		this.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				System.out.println("Focus gained");
			}
			public void focusLost(FocusEvent e) {
				requestFocusInWindow();
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
	{	
	
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		elapsedTime = Duration.between(snap,Instant.now()).toMillis();
		snap = Instant.now();
		
		//System.out.println("Elapsed time = " + elapsedTime);
		
		float frames = elapsedTime/16f;
		frameCycle += frames;
		frameCycle %= 60;
		
		//System.out.println("Frames Elapsed = " + frames);
		
		//gl.glMatrixMode(GL_PROJECTION);
		
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		

		mStack.pushMatrix();
		
		
		
		
		
		//////////////////////////////////////////////////
		//Camera stuff starts here
		//////////////////////////////////////////////////
		
		mainCamera.localRotateThenTranslate(
			new Vector3f(
			
				-sideAxis.getValue()/frames,
				-verticalAxis.getValue()/frames,
				-fwdAxis.getValue()/frames
			),
			new Vector3f(
				-pitchTurnAxis.getValue()*frames,
				-yawTurnAxis.getValue()*frames,
				0
			)
		);
		
		//Remove when implementing lighting
		mStack.mul(mainCamera.returnMatrix());
		
		///////////////////////////////////////////////////////////////////////////////////////////////
		//Object rendering starts here
		///////////////////////////////////////////////////////////////////////////////////////////////
		
		//skybox
		spaceBox.render(mStack,pMat);
		
		tf = 5f;  // time factor
		
		/*Broken Axis (Need to bring back code for this or break the axis into another class)
		if(showAxis){
			
			mStack.translate(10f,0f,0f);
			gl.glUniformMatrix4fv(mvLoc,1,false,mStack.get(vals));
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
			gl.glVertexAttribPointer(0,3,GL_FLOAT,false, 0,0);
			gl.glEnableVertexAttribArray(0);
			gl.glDrawArrays(GL_LINES, 0, 6);
		}
		//*/
		
		model.get("caltrop1").rotate(new Vector3f(0,0.005f*frames,0));
		model.get("caltrop1").setScale(new Vector3f((float)Math.sin(((frameCycle-30)/15f)+0.1f)*0.5f,(float)Math.sin(((frameCycle-30)/15f)+0.1f)*0.5f,(float)Math.sin(((frameCycle-30)/15f)+0.1f)*0.5f));
		
		model.get("caltrop2").rotate(new Vector3f(0,0.005f/frames,0));
		model.get("caltrop2").setScale(new Vector3f((float)Math.sin(((frameCycle-30)/15f)+0.1f)*0.5f,(float)Math.sin(((frameCycle-30)/15f)+0.1f)*0.5f,(float)Math.sin(((frameCycle-30)/15f)+0.1f)*0.5f));
		
		//Using the lambda function iteration approach
		//https://www.geeksforgeeks.org/how-to-iterate-hashmap-in-java/#
		model.forEach((key,target) -> target.render(mStack,pMat));
		
		
		////////////////////////////////////////////
		// Done
		//////////////////////////////////////
		
		//*/
		mStack.popMatrix();
		
	}
	
	//Init
	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		
		//Camera Prep Stuff
		
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		
		//viewMatrix.identity();
		
		snap = Instant.now();
		frameCycle = 0;
		
		/*//It seems that models and textures are loaded during initialization...
		//(Program 6 Shuttle loader)
		
		//*/
		
		
		/*//The perspective matrix is also setup here (Program 6 Shuttle loader)
		
		//*/
		
		/*Shaders====================================
		=============================================
		*/
		renderingProgram = Utils.createShaderProgram(
			"a3/vertShader.glsl", 
			"a3/fragShader.glsl"
		);
		
		int simpleObjRenderer = Utils.createShaderProgram(
			"a3/objVertShader.glsl", 
			"a3/objFragShader.glsl"
		);
		
		int SkyCubeRenderer = Utils.createShaderProgram("a3/vertCShader.glsl", "a3/fragCShader.glsl");
		
		spaceBox = new SkyCube("NebulaSky",SkyCubeRenderer);
		spaceBox.setupVertices();
		
		//importing obj models
		model.put("anvil", (new DrawableModel("Anvil--02--Triangulated.obj","Anvil_Laptop_Sleeve.png", simpleObjRenderer)));
		
		model.put("caltrop1",(new DrawableModel("CaltropStar.obj","castleroof.jpg", simpleObjRenderer)));
		
		model.put("caltrop2",(new DrawableModel("CaltropStar.obj","castleroof.jpg", simpleObjRenderer)));
		
		//Texture Source http://texturelib.com/texture/?path=/Textures/metal/bare/metal_bare_0012
		
		model.put("glaidus",new DrawableModel("Gladius_Single.obj","metal_bare_0012_01_s.jpg",simpleObjRenderer));
		
		model.put("ringRune",new DrawableModel("GroundRing.obj","RunicRingSegment.png",simpleObjRenderer));
		
		model.put("sign", new DrawableModel("Sign_on_wood_post.obj","World404Sign.png",simpleObjRenderer));
		
		model.forEach((key,target) -> target.loadModelData());
		model.forEach((key,target) -> target.setupVertices(vao,0));
		
		//Initial positions
		
		model.get("anvil").translate(new Vector3f(0f,1f,0f));
		
		model.get("caltrop1").translate(new Vector3f(3f,10f,0f));
		
		model.get("caltrop2").translate(new Vector3f(-3f,10f,0f));
		
		model.get("glaidus").translate(new Vector3f(0,5f,0f));
		model.get("glaidus").setScale(new Vector3f(0.2f,0.2f,0.2f));
		
		model.get("ringRune").setScale(new Vector3f(6f,0f,6f));
		model.get("ringRune").translate(new Vector3f(0f,-0.5f,0f));
		
		model.get("sign").translate(new Vector3f(-10f,1f,0f));
		
		//cameraX = 0.0f; cameraY = 0.0f; cameraZ = 12.0f;
		mainCamera.setPosition(0,0,-12f);
	}

	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}
}