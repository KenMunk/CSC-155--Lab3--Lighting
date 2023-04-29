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

out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;


/*//
Instead of hard-coding the material properties, the material properties will be
handled in the material textures in order to make it easier to diversify materials quickly and with discression with regards to the object in question

Yes, I'm literally mimicking how Unity is doing their shaders
//*/

layout (binding=0) uniform sampler2D textureSample;
layout (binding=1) uniform sampler2D ambientColor;
layout (binding=2) uniform sampler2D diffuseColor;
layout (binding=3) uniform sampler2D specularColor;
layout (binding=4) uniform sampler2D shininessMap;

void main(void)
{	
	tc = tex_coord;
	
	//*
	
	varyingVertPos = (m_matrix * vec4(position,1.0)).xyz;
	varyingLightDir = light.position.xyz - varyingVertPos;
	varyingNormal = (norm_matrix * vec4(normal,1.0)).xyz;
	
	varyingHalfVector =
		normalize(normalize(varyingLightDir)
		+ normalize(-varyingVertPos)).xyz;

	gl_Position = p_matrix * v_matrix * m_matrix * vec4(position,1.0);
	
	
	vertEyeSpacePos = (v_matrix * m_matrix * vec4(position,1.0)).xyz;
	//*/
	
}