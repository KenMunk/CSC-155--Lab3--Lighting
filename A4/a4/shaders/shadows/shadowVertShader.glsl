#version 430

layout (location=0) in vec3 vertPos;


//uniform mat4 m_matrix;
//uniform mat4 norm_matrix;
//uniform mat4 lightView;
//uniform mat4 lightPerspective;
uniform mat4 lightMVP;

void main(void)
{	
	//I could probably get away with this since none of the objects within the scene are using height mapping
	gl_Position = lightMVP * vec4(vertPos,1.0);
}
