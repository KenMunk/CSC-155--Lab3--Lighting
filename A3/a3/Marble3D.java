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

public class Marble3D extends Noise3D{

	
	public Marble3D(int width, int height, int depth, double frequency, double turbulencePow, double maxZoom){
		super(width,height,depth,frequency,turbulencePow,maxZoom);
	}
	
	//Taken from textbook program 14-5 marble
	@Override
	protected Color colorOperation(int noiseX, int noiseY, int noiseZ){
		double xyzValue = (float)noiseX/this.width + (float)noiseY/this.height + (float)noiseZ/this.depth
							+ this.turbulencePow * this.turbulence(noiseX, noiseY, noiseZ,this.maxZoom)/Math.max(Math.max(width,height),depth);

		double sineValue = this.logistic(Math.abs(Math.sin(xyzValue * 3.14159 * this.frequency)));
		sineValue = Math.max(-1.0, Math.min(sineValue*1.25-0.20, 1.0));
		
		Color color = new Color((float)sineValue,
				(float)Math.min(sineValue*1.5-0.25, 1.0),
				(float)sineValue);
				
		return(color);
	}
	
}