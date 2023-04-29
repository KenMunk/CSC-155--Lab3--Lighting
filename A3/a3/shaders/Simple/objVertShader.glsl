#version 430

//Reusable Section

struct PositionalLight
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec4 position;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;

uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;

out vec3 vertEyeSpacePos;
out vec2 tc;

//End Reusable Section

//Loading the vbo locations
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 tex_coord;
layout (location = 2) in vec3 normal;



layout (binding=0) uniform sampler2D s;

void main(void)
{	gl_Position = p_matrix * v_matrix * m_matrix * vec4(position,1.0);
	tc = tex_coord;
	
	
	vertEyeSpacePos = (v_matrix * m_matrix * vec4(position,1.0)).xyz;
}