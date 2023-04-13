#version 430

//Loading the vbo locations
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 tex_coord;
out vec2 tc;

uniform mat4 mv_matrix;
uniform mat4 p_matrix;

/*//
Instead of hard-coding the material properties, the material properties will be
handled in the material textures in order to make it easier to diversify materials quickly and with discression with regards to the object in question

Yes, I'm literally mimicking how Unity is doing their shaders
//*/

layout (binding=0) uniform sampler2D ambientColor;
layout (binding=1) uniform sampler2D diffuseColor;
layout (binding=1) uniform sampler2D specularColor;
layout (binding=1) uniform sampler2D shininessMap;

void main(void)
{	gl_Position = p_matrix * mv_matrix * vec4(position,1.0);
	tc = tex_coord;
}