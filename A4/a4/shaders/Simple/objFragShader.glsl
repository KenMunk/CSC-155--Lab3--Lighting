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

//Takes the texture coordinate from the vertex shader since the texture coordinate is from a VBO
in vec3 vertEyeSpacePos;
in vec2 tc;

//End Reusable Section

out vec4 color;

//Creates a texture sampler for the texture at binding 0
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D textureSample;

void main(void)
{
	vec4 fogColor = vec4(0.3, 0, 0.4, 0.0);	
	
	float fogStart = 200;
	float fogEnd = 500;
	
	// the distance from the camera to the vertex in eye space is simply the length of a
	// vector to that vertex, because the camera is at (0,0,0) in eye space.
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	
	color = mix(fogColor,(texture(textureSample,tc)),fogFactor);
}
