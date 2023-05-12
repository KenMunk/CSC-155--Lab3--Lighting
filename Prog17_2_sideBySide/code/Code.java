package code;

import java.nio.*;
import javax.swing.*;
import java.lang.Math;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import org.joml.*;

public class Code extends JFrame implements GLEventListener
{
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int vao[] = new int[1];
	private int vbo[] = new int[3];
	
	private float cameraX, cameraY, cameraZ;
	private float terLocX, terLocY, terLocZ;
	
	private ImportedModel ground;
	private int numGroundVertices;
	
	private int rockyTexture;
	private int heightMap;
	
	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private int mvLoc, pLoc;
	private float aspect;
	private float rotAmt = 0.0f;
	private double prevTime;
	private double elapsedTime;
	
	// VR stuff
	private float IOD = 0.01f;  // tunable interocular distance – we arrived at 0.01 for this scene by trial-and-error
	private float near = 0.01f;
	private float far = 100.0f;
	private int sizeX = 1920, sizeY = 1080;

	public Code()
	{	setSize(sizeX, sizeY);
		setTitle("Chapter 17 - Program 2");
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
	}
	
	private void computePerspectiveMatrix(float leftRight)
	{	float top = (float)Math.tan(1.0472f / 2.0f) * (float)near;
		float bottom = -top;
		float frustumshift = (IOD / 2.0f) * near / far;
		float left = -aspect * top - frustumshift * leftRight;
		float right = aspect * top - frustumshift * leftRight;
		pMat.setFrustum(left, right, bottom, top, near, far);
	}
	
	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glViewport(0, 0, sizeX, sizeY);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(0.7f, 0.8f, 0.9f, 1.0f);	// the fog color is bluish-grey	
		gl.glClear(GL_COLOR_BUFFER_BIT);

		gl.glViewport(0, 0, sizeX/2, sizeY);
		scene(-1.0f);

		gl.glViewport(sizeX/2, 0, sizeX/2, sizeY);
		scene(1.0f);
	}

	public void scene(float leftRight)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(renderingProgram);

		computePerspectiveMatrix(leftRight);
		
		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		
		vMat.identity().setTranslation(-(cameraX + leftRight * IOD/2.0f), -cameraY, -cameraZ);
		mMat.identity();
		mMat.translate(terLocX, terLocY, terLocZ);
		elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		rotAmt += elapsedTime * 0.0001f;
		mMat.rotateX((float)Math.toRadians(10.0f));
		mMat.rotateY(rotAmt);

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
	
		// vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// texture coordinate buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		// normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		// texture
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, rockyTexture);

		// height map
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, heightMap);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, numGroundVertices);
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		renderingProgram = Utils.createShaderProgram("code/vertShader.glsl", "code/fragShader.glsl");
		
		cameraX = 0.0f; cameraY = 0.13f; cameraZ = 0.3f;
		terLocX = 0.0f; terLocY = 0.05f; terLocZ = 0.0f;
		
		ground = new ImportedModel("../grid.obj");
		setupVertices();

		rockyTexture = Utils.loadTexture("bkgd1.jpg");
		heightMap = Utils.loadTexture("height.jpg");

		prevTime = System.currentTimeMillis();
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		numGroundVertices = ground.getNumVertices();
		Vector3f[] vertices = ground.getVertices();
		Vector2f[] texCoords = ground.getTexCoords();
		Vector3f[] normals = ground.getNormals();

		float[] pvalues = new float[numGroundVertices*3];
		float[] tvalues = new float[numGroundVertices*2];
		float[] nvalues = new float[numGroundVertices*3];
		
		for (int i=0; i<numGroundVertices; i++)
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
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(3, vbo, 0);

		//  ground vertices
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		//  ground texture coordinates
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer groundTexBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, groundTexBuf.limit()*4, groundTexBuf, GL_STATIC_DRAW);

		// ground normals
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer groundNorBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, groundNorBuf.limit()*4, groundNorBuf, GL_STATIC_DRAW);
	}

	public static void main(String[] args) { new Code(); }
	public void dispose(GLAutoDrawable drawable) { }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(50.0f), aspect, 0.1f, 1000.0f);
	}
}