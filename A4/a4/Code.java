package a4;

import java.nio.*;
import java.io.*;
import java.lang.Math;
import java.time.Instant;
import java.time.Duration;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import java.util.ArrayList;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.FocusAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseMotionListener;

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
	float frames;
	int lightTimer = 0;
	private int renderingProgram;
	
	//VAO and VBO initialization
	//Always only one VAO but potentially 3 or more VBOs
	private int vao[] = new int[1];
	private int vbo[] = new int[2];
	
	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4fStack mStack = new Matrix4fStack(10);
	private Matrix4f pMat = new Matrix4f();
	private int mvLoc, pLoc;
	private float aspect;
	private double tf;
	
	//Implementing a hash map to make managing so many models easier
	private Map<String, DrawableModel> model = new HashMap<String, DrawableModel>();
	
	private HashMap<String, Vector4f> lightingProperties = new HashMap<String, Vector4f>();
	
	private SkyCube spaceBox;
	
	private Camera mainCamera = new Camera();
	
	private float cameraRotation = 0;
	private float cameraX, cameraY, cameraZ;
	private float camYaw = 0f; //Side to side
	private float camPitch = 0f; //up down
	private float camRoll = 0f; //roll screen
	
	private AxisState fwdAxis = new AxisState(0.2f, KeyEvent.VK_S, KeyEvent.VK_W);
	private AxisState sideAxis = new AxisState(0.2f, KeyEvent.VK_D, KeyEvent.VK_A);
	private AxisState verticalAxis = new AxisState(0.1f, KeyEvent.VK_Q, KeyEvent.VK_E);
	private AxisState pitchTurnAxis = new AxisState(0.01f, KeyEvent.VK_UP, KeyEvent.VK_DOWN);
	private AxisState yawTurnAxis = new AxisState(0.01f, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
	private AxisState spaceBar = new AxisState(1f, KeyEvent.VK_SPACE, KeyEvent.VK_BACK_SPACE);
	boolean showAxis = true;
	
	boolean clickPress = false;
	Vector3f lanternLocation = new Vector3f(2,0,10);
	
	//Shadow stuff
	
	private int scSizeX, scSizeY;
	private int [] shadowTex = new int[1];
	private int [] shadowBuffer = new int[1];
	private Matrix4f lightVmat = new Matrix4f();
	private Matrix4f lightPmat = new Matrix4f();
	private Matrix4f b = new Matrix4f();
	
	private boolean shadowMap = true;
	private boolean showShadowMap = false;
	
	private boolean pauseEverything = false;
	
	private int shadowRenderProgram;

	public Code()
	{	setTitle("Chapter 4 - program 4");
		setSize(600, 600);
		
		GLProfile glp = GLProfile.getMinimum(true);
		GLCapabilities caps = new GLCapabilities(glp);
		myCanvas = new GLCanvas(caps);
		//myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
		
		//Control inputs
		myCanvas.setFocusable(true);
		myCanvas.addKeyListener(new KeyAdapter() {
			
			public void keyPressed(KeyEvent e){
				
				pitchTurnAxis.pressCheck(e.getKeyCode());
				yawTurnAxis.pressCheck(e.getKeyCode());
				
				fwdAxis.pressCheck(e.getKeyCode());
				sideAxis.pressCheck(e.getKeyCode());
				verticalAxis.pressCheck(e.getKeyCode());
				spaceBar.pressCheck(e.getKeyCode());
				
				
				
				if(e.getKeyCode() == KeyEvent.VK_M){
					showShadowMap = true;
				}
				
			}
			
			public void keyReleased(KeyEvent e){
				
				pitchTurnAxis.releaseCheck(e.getKeyCode());
				yawTurnAxis.releaseCheck(e.getKeyCode());
				
				fwdAxis.releaseCheck(e.getKeyCode());
				sideAxis.releaseCheck(e.getKeyCode());
				verticalAxis.releaseCheck(e.getKeyCode());
				spaceBar.releaseCheck(e.getKeyCode());
				
				if(e.getKeyCode() == KeyEvent.VK_SPACE){
					showAxis = !showAxis;
				}
				
				
				
				if(e.getKeyCode() == KeyEvent.VK_L){
					shadowMap = !shadowMap;
					System.out.println("Shadow Map On: " + shadowMap);
				}
				
				if(e.getKeyCode() == KeyEvent.VK_M){
					showShadowMap = false;
				}
				
				if(e.getKeyCode() == KeyEvent.VK_P){
					pauseEverything = !pauseEverything;
				}
				
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
					System.exit(0);
				}
			}
			
		});
		
		/*
		The concept at play here is the idea that the focus loss event won't happen until the user clicks back into the window.  So in order to re-enable interactability, I just need to force the window focus to be requested once the focus loss event happens
		
		Originally, I wasn't going to go the direction of using the focus in window but that caused an unfortunate situation where the user cannot stop interacting with the app resulting in a practical lock-out of their computer
		
		The focus listener boiler plate code was generated by chatGPT since I was trying to figure out the focus situation
		//https://docs.oracle.com/javase/7/docs/api/java/awt/Component.html#requestFocus(boolean)
		
		*/
		myCanvas.addFocusListener(new FocusAdapter() {
			
			public void focusGained(FocusEvent e) {
				//System.out.println("Focus gained");
			}
			public void focusLost(FocusEvent e) {
				requestFocusInWindow();
			}
		});//*/
		
		
		myCanvas.addMouseListener(new MouseAdapter(){
			
			@Override
			public void mouseClicked(MouseEvent e){
				//clickPress = true;
				//System.out.println("Click Event");
			}
			
			@Override
			public void mousePressed(MouseEvent e){
				clickPress = true;
				//System.out.println("Click Press");
			}
			
			@Override
			public void mouseReleased(MouseEvent e){
				
				clickPress = false;
			}
			
			@Override
			public void mouseExited(MouseEvent e){
				
				clickPress = false;
				System.out.println("Mouse has left the room");
			}
		});
		
		myCanvas.addMouseMotionListener(new MouseAdapter(){
			
			@Override
			public void mouseDragged(MouseEvent e){
				
				
				if(clickPress){
					float xPosition = ((float)e.getX()/((float)myCanvas.getWidth()+0.1f)-0.5f)*(-15);
					float yPosition = ((float)e.getY()/((float)myCanvas.getHeight()+0.1f)-0.5f)*(10);
					
					//System.out.println("Mouse at: " + xPosition + ", " + yPosition);
					//aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
					
					
					lanternLocation = lanternLocation.set(
						xPosition,
						yPosition,
						lanternLocation.z
					);
					
					//System.out.println("Lantern Location = " + lanternLocation);
					
				}
			}
		});
		
		myCanvas.addMouseWheelListener(new MouseAdapter(){
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e){
				float movementSteps = ((float)e.getWheelRotation())*0.3f;
				
				
				lanternLocation = lanternLocation.add(0,0,movementSteps);
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
		if(!pauseEverything){
			
			GL4 gl = (GL4) GLContext.getCurrentGL();
			int viewWidth =  myCanvas.getWidth();
			int viewHeight = myCanvas.getHeight();
			
			//gl.glEnable(GL_DEBUG_OUTPUT);
			
			
			gl.glViewport(0, 0, viewWidth, viewHeight);
			gl.glClear(GL_DEPTH_BUFFER_BIT);
			gl.glClear(GL_COLOR_BUFFER_BIT);
			
			gl.glViewport(0,0,viewWidth/2,viewHeight);
			
			render(-1);
			
			gl.glViewport(viewWidth/2,0,viewWidth/2,viewHeight);
			
			render(1);
		}
		
		
	}
	
	private void animationFrame(){
		
		//System.out.println("skybox done");
		//Skybox end
		
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
		
		/*
		model.get("anvil").translate(new Vector3f(0f,0.05f*((float)frameCycle-30)/30f,0f));
		//*/
		
		//Using the lambda function iteration approach
		//https://www.geeksforgeeks.org/how-to-iterate-hashmap-in-java/#
		
	}
	
	public void render(int eyePolarity){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		//gl.setGL(new DebugGL4(gl));
		
        //gl.glEnable(GL_DEBUG_OUTPUT);
		
		aspect = ((float) myCanvas.getWidth()/2) / (float) myCanvas.getHeight();
		//gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		//gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		//System.out.println("Cleared");
		
		elapsedTime = Duration.between(snap,Instant.now()).toMillis();
		snap = Instant.now();
		
		//System.out.println("Elapsed time = " + elapsedTime);
		
		frames = elapsedTime/16f;
		frameCycle += frames;
		frameCycle %= 60;
		
		//System.out.println("Frames Elapsed = " + frames);
		
		//gl.glMatrixMode(GL_PROJECTION);
		
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		

		mStack.pushMatrix();
		
		
		//System.out.println("matrix ops start");
		
		
		
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
		
		Vector3f eyeOffset = new Vector3f(0.5f*((float)eyePolarity),0.0f,0.0f);
		
		mainCamera.setOffset(eyeOffset);
		
		if(frameCycle == 0){
			lightTimer++;
			lightTimer%=61;
		}
		
		Matrix4f lanternMat = new Matrix4f(mainCamera.returnMatrix());
		
		lanternMat.translateLocal(lanternLocation);
		lanternMat.translateLocal(new Vector3f(0,0.5f,0));
		lanternMat.invert();
		Vector3f lanternPosition = new Vector3f();
		lanternMat.getTranslation(lanternPosition);
		
		//System.out.println("lantern pos: " + lanternPosition);
		
		lightingProperties.put("light.position", new Vector4f(lanternPosition, 
			1.0f
		));
		
		model.get("lantern").setPosition(lanternPosition);
		
		//Remove when implementing lighting
		
		model.forEach((key,target) -> target.addOtherMatrix("v_matrix",mainCamera.returnMatrix()));
		
		///////////////////////////////////////////////////////////////////////////////////////////////
		//Object rendering starts here
		///////////////////////////////////////////////////////////////////////////////////////////////
		
		//skybox
		//*
		Matrix4fStack spaceBoxMat = new Matrix4fStack(5);
		
		spaceBoxMat.set(mStack);
		spaceBoxMat.mul(mainCamera.returnMatrix());
		
		spaceBox.render(spaceBoxMat,pMat);
		//*/
		this.animationFrame();
		
		model.forEach((key,target) -> target.addMultipleVectorProperties(lightingProperties));
		
		
		
		
		//Shadow pass ( pass 1 )
		
		setupShadowBuffers();
		
		if(shadowMap){
				
			gl.glUseProgram(shadowRenderProgram);
			
			lightVmat.identity().setLookAt(lanternPosition, new Vector3f(0f,0f,0f), new Vector3f(0f,1f,0f));	// vector from light to origin
			lightPmat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
			
			if(!showShadowMap){
				gl.glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer[0]);
				gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowTex[0], 0);
				gl.glDrawBuffer(GL_NONE);
			}
			else{
				gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
			}
			
			gl.glEnable(GL_DEPTH_TEST);
			gl.glEnable(GL_POLYGON_OFFSET_FILL);	//  for reducing
			gl.glPolygonOffset(3.0f, 5.0f);		//  shadow artifacts
			
			//Draw the shadows of each object 
			mStack.pushMatrix();
			
			//System.out.println("lantern view pos: " + lightVmat);
			model.forEach((key,target) -> target.renderShadows(mStack, lightVmat, lightPmat));
			mStack.popMatrix();
			gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
			
			/* Bind the shadow texture to all shaders 
			gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
			*/
			
			//Rendering pass 2
			
		}
		else{
			gl.glClear(GL_DEPTH_BUFFER_BIT);
		}
		
		model.forEach((key,target) -> target.addTexture(GL_TEXTURE0, shadowTex[0], GL_TEXTURE_2D));
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		
		if(!showShadowMap){
			model.forEach((key,target) -> target.render(mStack,pMat));
		}
		
		/*
		
		if(spaceBar.getValue() != 0){
			System.out.println("mStack is: " + mStack);
			System.out.println("pMat is: " + pMat);
			System.out.println("Lantern mat is: " + lanternMat);
			System.out.println("Camera Mat is: " + mainCamera.returnMatrix());
		}
		
		//System.out.println("objects rendered");
		
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
		
		snap = Instant.now();
		frameCycle = 0;
		
		lightingProperties.put("globalAmbient", new Vector4f(0.5f, 0.3f, 0.6f, 1.0f));
		
		lightingProperties.put("light.ambient", new Vector4f(0.3f, 0.3f, 0.3f, 1.0f));
		
		lightingProperties.put("light.diffuse", new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
		
		lightingProperties.put("light.specular", new Vector4f(0.8f, 0.7f, 1f, 1.0f));
		
		lightingProperties.put("light.position", new Vector4f(10.0f, 5f, 3.0f, 1.0f));
		
		/*//It seems that models and textures are loaded during initialization...
		//(Program 6 Shuttle loader)
		
		//*/
		
		
		/*//The perspective matrix is also setup here (Program 6 Shuttle loader)
		
		//*/
		
		/*Shaders====================================
		=============================================
		*/
		
		System.out.println("Building world origin shader");
		renderingProgram = Utils.createShaderProgram(
			"a4/shaders/WorldOrigin/vertShader.glsl",
			"a4/shaders/WorldOrigin/fragShader.glsl"
		);
		
		
		System.out.println("Building simple obj shader");
		int simpleObjRenderer = Utils.createShaderProgram(
			"a4/shaders/Simple/objVertShader.glsl", 
			"a4/shaders/Simple/objFragShader.glsl"
		);
		
		System.out.println("Building skycube shader");
		int SkyCubeRenderer = Utils.createShaderProgram(
			"a4/shaders/SkyCube/vertCShader.glsl", 
			"a4/shaders/SkyCube/fragCShader.glsl"
		);
		
		System.out.println("Building PBR shader");
		int objPBRenderer = Utils.createShaderProgram(
			"a4/shaders/pbr/obj_PBR_VertShader.glsl",
			"a4/shaders/pbr/obj_PBR_FragShader.glsl"
		);
		
		System.out.println("Building PBR Texture3D shader");
		int objPB3DRenderer = Utils.createShaderProgram(
			"a4/shaders/pbr/Permutation_Noise/channelized3DMaterial_Vert.glsl",
			"a4/shaders/pbr/Permutation_Noise/channelized3DMaterial_Frag.glsl"
		);
		
		System.out.println("Building shadow shader");
		shadowRenderProgram = Utils.createShaderProgram(
			"a4/shaders/shadows/shadowVertShader.glsl",
			"a4/shaders/shadows/shadowFragShader.glsl"
		);
		
		
		System.out.println("Setting up space");
		spaceBox = new SkyCube("NebulaSky",SkyCubeRenderer);
		spaceBox.setupVertices();
		
		System.out.println("Setting up models");
		//importing obj models
		model.put("anvil", (new DrawableModel(
			"Anvil--02--Triangulated.obj",
			"Anvil_Laptop_Sleeve.png", 
			simpleObjRenderer,
			new Vector3f(0f,1f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(1f,1f,1f)
		)));
		
		DrawableModel glaidus = new DrawableModel(
			"Gladius_Single.obj",
			"Glaidus_Single_Type0--Default.png",
			objPB3DRenderer,
			new Vector3f(2f,6f,0f),
			new Vector3f(3f,3.1f,4.72f),
			new Vector3f(0.2f,0.2f,0.2f)
		);
		
		glaidus.addADSSTextures(
			"Glaidus_Single_Type0--Ambient.png",
			"Glaidus_Single_Type0--Diffuse.png",
			"Glaidus_Single_Type0--Specular.png",
			"Glaidus_Single_Type0--Shininess.png",
			"NormalDefault.png",
			"NebulaSky"
		);
		
		
		System.out.println("Your computer is screaming but it's ok, it's just noise");
		System.out.println("Making the primary noise");
		Marble3D bladeNoise = new Marble3D(128,128,128,0.2,3.0,5.0);
		
		Matrix4f bladeColorMod = new Matrix4f().identity();
		
		bladeColorMod.translateLocal(new Vector3f(-0.2f, -0.5f, 0.5f));
		
		System.out.println("Making the secondary noise");
		Wood3D handleNoise = new Wood3D(256,256,256,0.3,0.015,40.0);
		
		
		
		System.out.println("Making the tertiary noise");
		Marble3D detailNoise = new Marble3D(256,256,256,0.02,20.0,32.0);
		
		
		Matrix4f detailColorMod = new Matrix4f().identity();
		detailColorMod.rotateLocalX(0.5f);
		detailColorMod.rotateLocalY(0.2f);
		detailColorMod.rotateLocalY(0.3f);
		
		//detailNoise.setColorTransform(detailColorMod);
		
		glaidus.addOtherMatrix("primaryColor",bladeColorMod);
		glaidus.addOtherMatrix("secondaryColor",new Matrix4f().identity());
		glaidus.addOtherMatrix("tertiaryColor",detailColorMod);
		
		System.out.println("Cramming 3 noise textures into a sword");
		glaidus.add3DTextures(
			"Glaidus_Single_Type0--Channels.png",
			bladeNoise,
			handleNoise,
			detailNoise
		);
		
		model.put("glaidus", glaidus);
		
		/*//Glaidus Test//
		model.get("anvil").addChild(
			glaidus
		);
		//*/
		
		model.put("caltrop1",(new DrawableModel(
			"CaltropStar.obj",
			"castleroof.jpg", 
			simpleObjRenderer,
			new Vector3f(3f,10f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(1f,1f,1f)
		)));
		
		model.put("caltrop2",(new DrawableModel(
			"CaltropStar.obj",
			"castleroof.jpg", 
			simpleObjRenderer,
			new Vector3f(-6f,15f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(1f,1f,1f)
		)));
		
		//Texture Source http://texturelib.com/texture/?path=/Textures/metal/bare/metal_bare_0012
		
		
		model.put("ringRune",new DrawableModel(
			"GroundRing.obj",
			"RunicRingSegment.png",
			simpleObjRenderer,
			new Vector3f(0f,-0.5f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(6f,0f,6f)
		));
		
		
		model.put("coreIsland", new DrawableModel(
			"Hex-Tile-Room -- Floor -- V-UV-03.obj",
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top--pbr--MarbleLime.png",
			objPBRenderer,
			new Vector3f(0f,-0.6f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(2f,2f,2f)
		));
		
		model.get("coreIsland").addADSSTextures(
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top--pbr--ambient.png",
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top--pbr--diffuse.png",
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top--pbr--specular.png",
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top--pbr--shininess.png",
			"NormalDefault.png",
			"NebulaSky"
		);
		
		model.put("Island2", new DrawableModel(
			"Hex-Tile-Room -- Floor -- V-UV-03.obj",
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top.png",
			simpleObjRenderer,
			new Vector3f(-50f,-0.6f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(2f,2f,2f)
		));
		
		model.put("Island3", new DrawableModel(
			"Hex-Tile-Room -- Floor -- V-UV-03.obj",
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top.png",
			simpleObjRenderer,
			new Vector3f(-100f,-0.6f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(2f,2f,2f)
		));
		
		model.put("Island4", new DrawableModel(
			"Hex-Tile-Room -- Floor -- V-UV-03.obj",
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top.png",
			simpleObjRenderer,
			new Vector3f(-150f,-0.6f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(2f,2f,2f)
		));
		
		model.put("Island5", new DrawableModel(
			"Hex-Tile-Room -- Floor -- V-UV-03.obj",
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top.png",
			simpleObjRenderer,
			new Vector3f(-200f,-0.6f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(2f,2f,2f)
		));
		
		model.put("Island6", new DrawableModel(
			"Hex-Tile-Room -- Floor -- V-UV-03.obj",
			"Hex-Tile-Room -- Floor -- V-UV-03--Marbel_Top.png",
			simpleObjRenderer,
			new Vector3f(-250f,-0.6f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(2f,2f,2f)
		));
		
		model.put("woodCrate", new DrawableModel(
			"TimberCrate--Complete--Default.obj",
			"TimberBoxTexture--Lab3.png",
			simpleObjRenderer,
			new Vector3f(-15f,-0.5f, 6f),
			new Vector3f(0,5f,0),
			new Vector3f(2f,2f,2f)
		));
		
		model.put("longTable", new DrawableModel(
			"LongTable--Simple--SquareLegs.obj",
			"LongTable--Simple--SquareLegs--Weathered.png",
			simpleObjRenderer,
			new Vector3f(0f,-0.4f, 13f),
			new Vector3f(0,30f,0),
			new Vector3f(1f,1f,1f)
		));
		
		//Simple_Street_Light--High_Poly.obj
		//StreetLamp--High_Poly--TextureLabsMetals.png
		//*
		
		model.put("sign", new DrawableModel(
			"Sign_on_wood_post.obj",
			"World404Sign.png",
			objPBRenderer,
			new Vector3f(-10f,-1f,0f),
			new Vector3f(0f,0f,0f),
			new Vector3f(1f,1f,1f)
		));
		
		model.get("sign").addADSSTextures(
			"Sign_on_wood_post--World404--Ambient.png",
			"Sign_on_wood_post--World404--Diffuse.png",
			"Sign_on_wood_post--World404--Specular.png",
			"Sign_on_wood_post--World404--Shininess.png",
			"castleroofNORMAL.jpg",
			"NebulaSky"
		);
		
		model.put("streetLamp", new DrawableModel(
			"Simple_Street_Light--High_Poly.obj",
			"StreetLamp--High_Poly--TextureLabsMetals.png",
			objPBRenderer,
			new Vector3f(10f,-1f, -9f),
			new Vector3f(0,-40f,0),
			new Vector3f(3f,3f,3f)
		));
		
		model.get("streetLamp").addADSSTextures(
			"StreetLamp--High_Poly--basic--test.png",
			"StreetLamp--High_Poly--TextureLabsMetals--diffuse.png",
			"StreetLamp--High_Poly--TextureLabsMetals--specularpng.png",
			"StreetLamp--High_Poly--TextureLabsMetals--shininessmap.png",
			"NormalDefault.png",
			"NebulaSky"
		);
		
		model.put("OldGoldBox", new DrawableModel(
			"TimberCrate--Complete--Default.obj",
			"TimberBoxTexture--Lab3--GoldPanel--WoodTrim--Base.png",
			objPBRenderer,
			new Vector3f(-15f,-0.5f, -6f),
			new Vector3f(0,8f,0),
			new Vector3f(2f,2f,2f)
		));
		
		model.get("OldGoldBox").addADSSTextures(
			"TimberBoxTexture--Lab3--GoldPanel--WoodTrim--Ambient.png",
			"TimberBoxTexture--Lab3--GoldPanel--Diffuse.png",
			"TimberBoxTexture--Lab3--GoldPanel--Specular.png",
			"TimberBoxTexture--Lab3--GoldPanel--Shininess.png",
			"NormalDefault.png",
			"NebulaSky"
		);
			
		//*/
		
		Vector3f lanternPosition = new Vector3f(
			lightingProperties.get("light.position").x,
			lightingProperties.get("light.position").y,
			lightingProperties.get("light.position").z
		);
		
		model.put("lantern", new DrawableModel(
			"Simple_Lantern--With_Internal_Faces.obj",
			"Simple_Lantern--MagicLantern--Transparent.png",
			simpleObjRenderer,
			lanternPosition,
			new Vector3f(0,30f,0),
			new Vector3f(0.3f,0.3f,0.3f)
		));
		
		model.get("lantern").allowDrawInternals();
		
		System.out.println("loading model data for all models");
		
		
		model.forEach((key,target) -> target.loadModelData());
		model.forEach((key,target) -> target.setupVertices(vao,0));
		
		
		//cameraX = 0.0f; cameraY = 0.0f; cameraZ = 12.0f;
		mainCamera.setPosition(0,-5,-24f);
		
		gl.getContext().enableGLDebugMessage(true);
		
		gl.glEnable(GL_DEBUG_OUTPUT);
		
		gl.getContext().setGLDebugSynchronous(true);
		
		gl.glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
		
		
		b.set(
			0.5f, 0.0f, 0.0f, 0.0f,
			0.0f, 0.5f, 0.0f, 0.0f,
			0.0f, 0.0f, 0.5f, 0.0f,
			0.5f, 0.5f, 0.5f, 1.0f);
			
		
		System.out.println("initialization complete");
	}

	
	private void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadowBuffer, 0);
	
		gl.glGenTextures(1, shadowTex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadowTex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	}

	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		//Making it so that the screen can be resized without knocking the graphics offline
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glViewport(0,0,width,height);
	}
	public void dispose(GLAutoDrawable drawable) {}
}