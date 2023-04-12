package code;

import org.joml.*;
import static java.lang.Math.*;

public class Torus
{
	private int numVertices, numIndices, prec=48;
	private int[] indices;
	private Vector3f[] vertices;
	private Vector2f[] texCoords;
	private Vector3f[] normals;
	private float inner = 0.6f, outer = 0.25f;
	private Vector3f[] sTangents, tTangents;
	
	public Torus()
	{	initTorus();
	}
	
	public Torus(float in, float out, int p)
	{	inner=in; outer=out; prec=p;
		initTorus();
	}

	private void initTorus()
	{	numVertices = (prec+1)*(prec+1);
		numIndices = prec * prec * 6;
		indices = new int[numIndices];
		vertices = new Vector3f[numVertices];
		texCoords = new Vector2f[numVertices];
		normals = new Vector3f[numVertices];
		sTangents = new Vector3f[numVertices];
		tTangents = new Vector3f[numVertices];
		for (int i=0; i<numVertices; i++)
		{	vertices[i] = new Vector3f();
			texCoords[i] = new Vector2f();
			normals[i] = new Vector3f();
			sTangents[i] = new Vector3f();
			tTangents[i] = new Vector3f();
		}

		// calculate first ring.
		for (int i=0; i<prec+1; i++)
		{	float amt = (float) toRadians(i*360.0f/prec);
		
			Vector3f initPos = new Vector3f(0.0f, outer, 0.0f);
			initPos.rotateAxis(amt, 0.0f, 0.0f, 1.0f);
			initPos.add(new Vector3f(inner, 0.0f, 0.0f));
			vertices[i].set(initPos);
			
			texCoords[i].set(0.0f, ((float)i)/((float)prec));

			tTangents[i] = new Vector3f(0.0f, -1.0f, 0.0f);
			tTangents[i].rotateAxis(amt+(3.14159f/2.0f), 0.0f, 0.0f, 1.0f);
			sTangents[i].set(0.0f, 0.0f, -1.0f);
			
			normals[i] = tTangents[i].cross(sTangents[i]);
		}

		//  rotate the first ring about Y to get the other rings
		for (int ring=1; ring<prec+1; ring++)
		{	for (int i=0; i<prec+1; i++)
			{	float amt = (float) toRadians((float)ring*360.0f/(prec));
				Vector3f vp = new Vector3f(vertices[i]);
				vp.rotateAxis(amt, 0.0f, 1.0f, 0.0f);
				vertices[ring*(prec+1)+i].set(vp);

				texCoords[ring*(prec+1)+i].set((float)ring*2.0f/(float)prec, texCoords[i].y());

				sTangents[ring*(prec+1)+i].set(sTangents[i]);
				sTangents[ring*(prec+1)+i].rotateAxis(amt, 0.0f, 1.0f, 0.0f);
				tTangents[ring*(prec+1)+i].set(tTangents[i]);
				tTangents[ring*(prec+1)+i].rotateAxis(amt, 0.0f, 1.0f, 0.0f);

				normals[ring*(prec+1)+i].set(normals[i]);
				normals[ring*(prec+1)+i].rotateAxis(amt, 0.0f, 1.0f, 0.0f);
			}
		}

		// calculate triangle indices
		for(int ring=0; ring<prec; ring++)
		{	for(int i=0; i<prec; i++)
			{	indices[((ring*prec+i)*2)  *3+0]= ring*(prec+1)+i;
				indices[((ring*prec+i)*2)  *3+1]=(ring+1)*(prec+1)+i;
				indices[((ring*prec+i)*2)  *3+2]= ring*(prec+1)+i+1;
				indices[((ring*prec+i)*2+1)*3+0]= ring*(prec+1)+i+1;
				indices[((ring*prec+i)*2+1)*3+1]=(ring+1)*(prec+1)+i;
				indices[((ring*prec+i)*2+1)*3+2]=(ring+1)*(prec+1)+i+1;
			}
		}
	}

	public int getNumIndices() { return numIndices; }
	public int[] getIndices() { return indices; }
	public int getNumVertices() { return numVertices; }
	public Vector3f[] getVertices() { return vertices; }
	public Vector2f[] getTexCoords() { return texCoords; }
	public Vector3f[] getNormals() { return normals; }
	public Vector3f[] getStangents() { return sTangents; }
	public Vector3f[] getTtangents() { return tTangents; }
}
