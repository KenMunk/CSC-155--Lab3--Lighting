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

//End Reusable Section

//Takes the texture coordinate from the vertex shader since the texture coordinate is from a VBO
in vec2 tc;
out vec4 color;

//Creates a texture sampler for the texture at binding 0
layout (binding=0) uniform sampler2D s;

void main(void)
{
	color = texture(s,tc);
}
