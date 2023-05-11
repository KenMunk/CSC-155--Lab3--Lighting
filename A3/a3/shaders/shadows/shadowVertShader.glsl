#version 430

layout (location=0) in vec3 vertPos;

//out vec4 posColor;

uniform mat4 m_matrix;
uniform mat4 norm_matrix;
uniform mat4 lightView;
uniform mat4 lightPerspective;
uniform mat4 lightMVP;

void main(void)
{	
	//I could probably get away with this since none of the objects within the scene are using height mapping
	//gl_Position = m_matrix * lightView * lightPerspective * vec4(vertPos,1.0);
	//gl_Position = vec4(vertPos,1.0);
	//gl_Position = lightPerspective * vec4(vertPos,1.0);
	gl_Position = lightMVP *  vec4(vertPos,1.0);
	//posColor = gl_Position;
	
}
