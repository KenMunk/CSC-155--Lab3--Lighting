package code;

import java.io.*;
import java.lang.Math;
import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.common.nio.Buffers;
import org.joml.*;

public class Code extends JFrame implements GLEventListener
{
	private GLCanvas myCanvas;

	private int renderingProgram;
	private int[] vao = new int[1];
	private int[] vbo = new int[3];

	private ImportedModel dolphinObj;
	private int numDolphinVertices;
	
	private float cameraX, cameraY, cameraZ;
	private float objLocX, objLocY, objLocZ;
	
	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private int mvLoc, pLoc;
	private float aspect;

	private int stripeTexture;
	private int texWidth = 200;
	private int texHeight= 200;
	private int texDepth = 200;
	private double[][][] tex3Dpattern = new double[texWidth][texHeight][texDepth];

	public Code()
	{	setTitle("Chapter 14 - program4");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glUseProgram(renderingProgram);
		
		mvLoc = gl.glGetUniformLocation(renderingProgram, "mv_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		
		vMat.identity().setTranslation(-cameraX,-cameraY,-cameraZ);
		
		mMat.identity();
		mMat.translate(objLocX, objLocY, objLocZ);
		mMat.rotateX((float)Math.toRadians(15.0f));
		mMat.rotateY((float)Math.toRadians(45.0f));

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_3D, stripeTexture);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numDolphinVertices);
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		dolphinObj = new ImportedModel("dolphinLowPoly.obj");
		renderingProgram = Utils.createShaderProgram("code/vertShader.glsl", "code/fragShader.glsl");

		float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		setupVertices();
		cameraX = 0.0f; cameraY = 0.0f; cameraZ = 2.0f;
		objLocX = 0.0f; objLocY = 0.0f; objLocZ = 0.0f;
		
		generate3Dpattern();	
		stripeTexture = load3DTexture();
	}
	
	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		numDolphinVertices = dolphinObj.getNumVertices();
		Vector3f[] vertices = dolphinObj.getVertices();
		Vector2f[] texCoords = dolphinObj.getTexCoords();
		Vector3f[] normals = dolphinObj.getNormals();
		
		float[] pvalues = new float[numDolphinVertices*3];
		float[] tvalues = new float[numDolphinVertices*2];
		float[] nvalues = new float[numDolphinVertices*3];
		
		for (int i=0; i<numDolphinVertices; i++)
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
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
	}

	// 3D Texture section

	private void fillDataArray(byte data[])
	{ for (int i=0; i<texWidth; i++)
	  { for (int j=0; j<texHeight; j++)
	    { for (int k=0; k<texDepth; k++)
	      {
		if (tex3Dpattern[i][j][k] == 1.0)
		{	// yellow color
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 255; //red
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 255; //green
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 0;   //blue
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0;   //alpha
		}
		else
		{	// blue color
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 0;   //red
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 0;   //green
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 255; //blue
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0;   //alpha
		}
	} } } }

	private int load3DTexture()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		byte[] data = new byte[texWidth*texHeight*texDepth*4];
		
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, texWidth, texHeight, texDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				texWidth, texHeight, texDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}

	void generate3Dpattern()
	{	for (int x=0; x<texWidth; x++)
		{	for (int y=0; y<texHeight; y++)
			{	for (int z=0; z<texDepth; z++)
				{	if ((y/10)%2 == 0)
						tex3Dpattern[x][y][z] = 0.0;
					else
						tex3Dpattern[x][y][z] = 1.0;
	}	}	}	}
	
	//  replace above function with the one below
	//	to change the stripes to a checkerboard.
	/*
	void generate3Dpattern()
	{	int xStep, yStep, zStep, sumSteps;
		for (int x=0; x<texWidth; x++)
		{	for (int y=0; y<texHeight; y++)
			{	for (int z=0; z<texDepth; z++)
				{	xStep = (x / 10) % 2;
					yStep = (y / 10) % 2;
					zStep = (z / 10) % 2;
					sumSteps = xStep + yStep + zStep;
					if ((sumSteps % 2) == 0)
						tex3Dpattern[x][y][z] = 0.0;
					else
						tex3Dpattern[x][y][z] = 1.0;
	}	}	}	}
	*/

	public static void main(String[] args) { new Code(); }
	public void dispose(GLAutoDrawable drawable) {}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	float aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);
	}
}