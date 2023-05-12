package a3;

import java.io.*;
import java.lang.Math;
import java.nio.*;
import java.util.*;
import java.awt.Color;
import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.common.nio.Buffers;
import org.joml.*;

public abstract class Noise3D extends Texture3D{
	
	protected double[][][] noise;
	protected java.util.Random random = new java.util.Random();
	protected double frequency;
	protected double turbulencePow;
	protected double maxZoom;
	protected Matrix4fc colorTransform;
	
	public Noise3D(int width, int height, int depth, double frequency, double turbulencePow, double maxZoom){
		super(width,height,depth);
		this.noise = new double[this.width][this.height][this.depth];
		this.generateNoise();
		
		this.frequency = frequency;
		this.turbulencePow = turbulencePow;
		this.maxZoom = maxZoom;
		this.colorTransform = new Matrix4f().identity();
	}

	protected void generateNoise()
	{	for (int x=0; x<this.width; x++)
		{	for (int y=0; y<this.height; y++)
			{	for (int z=0; z<this.depth; z++)
				{	noise[x][y][z] = random.nextDouble();
	}	}	}	}
	
	@Override
	protected void fillDataArray(byte data[])
	{ double veinFrequency = 2.0;
	  double turbPower = 3.0;
	  double maxZoom =  32.0;
	  for (int i=0; i<this.width; i++)
	  { for (int j=0; j<this.height; j++)
	    { for (int k=0; k<this.depth; k++)
	      {	
			Color c = colorOperation(i,j,k);
			
			Vector3f colorVec = new Vec3((float)c.getRed()/255f,(float)c.getGreen()/255f,(float)c.getBlue()/255f);
			
			Matrix4f colorMatrix = new Matrix4f();
			colorMatrix.translate(colorVec);
			colorMatrix.mul(colorTransform);
			colorMatrix.getTranslation(colorVec);
			
			c = colorOperation(
				(byte)Math.min(Math.abs(Math.round(color.x*255)),255),
				(byte)Math.min(Math.abs(Math.round(color.y*255)),255),
				(byte)Math.min(Math.abs(Math.round(color.z*255)),255)
			);

	        data[i*(this.width*this.height*4)+j*(this.height*4)+k*4+0] = (byte) c.getRed();
	        data[i*(this.width*this.height*4)+j*(this.height*4)+k*4+1] = (byte) c.getGreen();
	        data[i*(this.width*this.height*4)+j*(this.height*4)+k*4+2] = (byte) c.getBlue();
	        data[i*(this.width*this.height*4)+j*(this.height*4)+k*4+3] = (byte) 255;
	} } } }
	
	protected abstract Color colorOperation(int noiseX, int noiseY, int noiseZ);
	
	protected double smoothNoise(double zoom, double x1, double y1, double z1)
	{	//get fractional part of x, y, and z
		double fractX = x1 - (int) x1;
		double fractY = y1 - (int) y1;
		double fractZ = z1 - (int) z1;

		//neighbor values that wrap
		double x2 = x1 - 1; if (x2<0) x2 = (Math.round(this.width / zoom)) - 1;
		double y2 = y1 - 1; if (y2<0) y2 = (Math.round(this.height / zoom)) - 1;
		double z2 = z1 - 1; if (z2<0) z2 = (Math.round(this.depth / zoom)) - 1;

		//smooth the noise by interpolating
		double value = 0.0;
		value += fractX       * fractY       * fractZ       * noise[(int)x1][(int)y1][(int)z1];
		value += (1.0-fractX) * fractY       * fractZ       * noise[(int)x2][(int)y1][(int)z1];
		value += fractX       * (1.0-fractY) * fractZ       * noise[(int)x1][(int)y2][(int)z1];	
		value += (1.0-fractX) * (1.0-fractY) * fractZ       * noise[(int)x2][(int)y2][(int)z1];
				
		value += fractX       * fractY       * (1.0-fractZ) * noise[(int)x1][(int)y1][(int)z2];
		value += (1.0-fractX) * fractY       * (1.0-fractZ) * noise[(int)x2][(int)y1][(int)z2];
		value += fractX       * (1.0-fractY) * (1.0-fractZ) * noise[(int)x1][(int)y2][(int)z2];
		value += (1.0-fractX) * (1.0-fractY) * (1.0-fractZ) * noise[(int)x2][(int)y2][(int)z2];
		
		return value;
	}
	
	protected double turbulence(double x, double y, double z, double maxZoom)
	{	double sum = 0.0, zoom = maxZoom;
		while(zoom >= 0.9)
		{	sum = sum + smoothNoise(zoom, x/zoom, y/zoom, z/zoom) * zoom;
			zoom = zoom / 2.0;
		}
		sum = 128.0 * sum / maxZoom;
		return sum;
	}
	
	
	protected double logistic(double x)
	{	double k = 3.0;
		return (1.0/(1.0+Math.pow(2.718,-k*x)));
	}
}