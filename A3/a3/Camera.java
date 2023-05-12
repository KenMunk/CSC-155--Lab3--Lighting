package a3;

import java.nio.*;
import java.io.*;
import java.lang.*;
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
	protected Vector3f offset;
	
	/*
	private float camYaw = 0f; //Side to side
	private float camPitch = 0f; //up down
	private float camRoll = 0f; //roll screen
	*/
	
	
	public Camera(){
		this.transformation = new Matrix4f();
	}
	
	public void setOffset(Vector3f offsetVec){
		this.offset = new Vector3f(offsetVec);
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
		
		Matrix4f snapshotTransform = new Matrix4f(this.transformation);
		
		this.transformation.translateLocal(increment);
		
		if((""+this.transformation).contains("NaN")){
			System.out.println("Camera matrix corrupt after transformation, ignoring transform");
			this.transformation = new Matrix4f(snapshotTransform);
		}
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
		
		//https://www.geeksforgeeks.org/searching-for-characters-and-substring-in-a-string-in-java/
		if((""+this.transformation).contains("NaN")){
			System.out.println("Camera matrix corrupt after rotation");
			
			//https://stackoverflow.com/questions/2670956/how-to-quit-a-java-app-from-within-the-program
			System.exit(0);
		}
		
		this.localTranslations(increment);
		
		//https://www.geeksforgeeks.org/searching-for-characters-and-substring-in-a-string-in-java/
		if((""+this.transformation).contains("NaN")){
			System.out.println("Camera matrix corrupt after transformation");
			System.exit(0);
		}
		
	}
	
	public Matrix4f returnMatrix(){
		
		Matrix4f cameraLocation = new Matrix4f(this.transformation);
		cameraLocation.translateLocal(this.offset);
		
		return(cameraLocation);
		
	}
	
	
	
	
	
}