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
uniform mat4 lightView;
uniform mat4 lightPerspective;

uniform float fogStart;
uniform float fogEnd;

out vec3 vertEyeSpacePos;
out vec2 tc;

//End Reusable Section

//Loading the vbo locations
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 tex_coord;
layout (location = 2) in vec3 normal;
layout (location = 3) in vec3 vertTangent;

out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingNormal;
out vec3 varyingTangent;
out vec3 varyingHalfVector;
out vec4 shadow_coord;

out vec3 vNormal;
out vec3 vVertPos;


/*//
Instead of hard-coding the material properties, the material properties will be
handled in the material textures in order to make it easier to diversify materials quickly and with discression with regards to the object in question

Yes, I'm literally mimicking how Unity is doing their shaders
//*/

layout (binding=00) uniform sampler2DShadow shadowTex;
layout (binding=01) uniform sampler2D textureSample;
layout (binding=02) uniform sampler2D ambientColor;
layout (binding=03) uniform sampler2D diffuseColor;
layout (binding=04) uniform sampler2D specularColor;
layout (binding=05) uniform sampler2D shininessMap;
layout (binding=06) uniform sampler2D normMap;
layout (binding=07) uniform samplerCube environmentMap;
layout (binding=08) uniform sampler2D channelMap;
layout (binding=09) uniform sampler3D primaryNoise;
layout (binding=10) uniform sampler3D secondaryNoise;
layout (binding=11) uniform sampler3D tertiaryNoise;

void main(void)
{	
	tc = tex_coord;
	
	//*
	
	vVertPos = (m_matrix * v_matrix * vec4(position,1.0)).xyz;
	vNormal = (norm_matrix * vec4(normal,1.0)).xyz;
	
	varyingVertPos = (m_matrix * vec4(position,1.0)).xyz;
	varyingLightDir = light.position.xyz - varyingVertPos;
	varyingNormal = (norm_matrix * vec4(normal,1.0)).xyz;
	
	varyingTangent = (norm_matrix * vec4(vertTangent, 1.0)).xyz;
	
	varyingHalfVector =
		normalize(normalize(varyingLightDir)
		+ normalize(-varyingVertPos)).xyz;

	gl_Position = p_matrix * v_matrix * m_matrix * vec4(position,1.0);
	
	shadow_coord = m_matrix * lightView * lightPerspective * vec4(position,1.0);
	
	vertEyeSpacePos = (v_matrix * m_matrix * vec4(position,1.0)).xyz;
	//*/
	
}