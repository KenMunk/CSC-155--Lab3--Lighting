#version 430

//Loading the vbo locations
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 tex_coord;
out vec2 tc;

uniform mat4 mv_matrix;
uniform mat4 p_matrix;
layout (binding=0) uniform sampler2D s;

void main(void)
{	gl_Position = p_matrix * mv_matrix * vec4(position,1.0);
	tc = tex_coord;
}