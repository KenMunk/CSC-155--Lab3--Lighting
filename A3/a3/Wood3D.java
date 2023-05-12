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

public class Wood3D extends Noise3D{

	
	public Wood3D(int width, int height, int depth, double frequency, double turbulencePow, double maxZoom){
		super(width,height,depth,frequency,turbulencePow,maxZoom);
	}
	
	//Taken from textbook program 14-5 marble
	@Override
	protected Color colorOperation(int noiseX, int noiseY, int noiseZ){

		double xValue = (noiseX - (double)this.width/2.0) / (double)this.width;
		double yValue = (noiseY - (double)this.height/2.0) / (double)this.height;
		double distanceFromZ = Math.sqrt(xValue * xValue + yValue * yValue)
						+ this.turbulencePow * this.turbulence(noiseX, noiseY, noiseZ, this.maxZoom) / 256.0;
		double sineValue = 128.0 * Math.abs(Math.sin(2.0 * this.frequency * distanceFromZ * Math.PI));

		Color color = new Color((int)(60+(int)sineValue), (int)(10+(int)sineValue), 0);
		
		return(color);
	}
	
}