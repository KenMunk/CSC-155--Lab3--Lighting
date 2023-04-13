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

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import org.joml.*;


public class Camera{
	
	protected Matrix4f transformation;
	
	/*
	private float camYaw = 0f; //Side to side
	private float camPitch = 0f; //up down
	private float camRoll = 0f; //roll screen
	*/
	
	
	public Camera(){
		this.transformation = new Matrix4f();
	}
	
	public void setPosition(float x, float y, float z){
		this.setPosition(new Vector3f(x,y,z));
	}
	
	public void setPosition(Vector3f position){
		this.transformation.setTranslation(position);
	}
	
	public void setRotation(Quaternionf rotation){
		this.transformation.rotation(rotation);
	}
	
	/*
	
	All transformations will be occurring locally to mitigate issues with
	dealing with the math of complex transforms
	
	*///////////////////////////////////////////////////////////////////
	
	
	/*
	local translations
	https://joml-ci.github.io/JOML/apidocs/org/joml/Matrix4f.html#translateLocal(float,float,float)
	
	*/
	public void localTranslations(Vector3f increment){
		this.transformation.translateLocal(increment);
	}
	
	
	/*
	local rotations x then y then z
	
	https://joml-ci.github.io/JOML/apidocs/org/joml/Matrix4f.html
	Matrix4fc.rotateLocalX(float ang)
	Matrix4fc.rotateLocalY(float ang)
	Matrix4fc.rotateLocalZ(float ang)
	
	*/
	public void localRotation(Vector3f rotationsXYZ){
		
		this.transformation.rotateLocalX(rotationsXYZ.x);
		this.transformation.rotateLocalY(rotationsXYZ.y);
		this.transformation.rotateLocalZ(rotationsXYZ.z);
		
	}
	
	
	/*
	
	local transforms (local translations and local rotations combined)
	
	*/
	
	public void localRotateThenTranslate(Vector3f increment, Vector3f rotationsXYZ){
		
		this.localRotation(rotationsXYZ);
		this.localTranslations(increment);
		
	}
	
	public Matrix4f returnMatrix(){
		
		return(new Matrix4f(this.transformation));
		
	}
	
	
	
	
	
}