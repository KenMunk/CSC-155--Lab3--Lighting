#version 430

//Takes the texture coordinate from the vertex shader since the texture coordinate is from a VBO
in vec2 tc;
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 p_matrix;

//Creates a texture sampler for the texture at binding 0
layout (binding=0) uniform sampler2D s;

void main(void)
{
	color = texture(s,tc);
}
