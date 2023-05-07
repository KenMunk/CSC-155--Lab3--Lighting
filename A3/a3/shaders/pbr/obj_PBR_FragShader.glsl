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
uniform mat4 shadowMVP;

uniform float fogStart;
uniform float fogEnd;

//Takes the texture coordinate from the vertex shader since the texture coordinate is from a VBO
in vec3 vertEyeSpacePos;
in vec2 tc;

//End Reusable Section



in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingTangent;
in vec3 varyingHalfVector;

in vec3 vNormal;
in vec3 vVertPos;

out vec4 color;

//the material struct has been removed and replaced with 4 textures
//Creates a texture sampler for the texture at binding 0
layout (binding=0) uniform sampler2D textureSample;
layout (binding=1) uniform sampler2D ambientColor;
layout (binding=2) uniform sampler2D diffuseColor;
layout (binding=3) uniform sampler2D specularColor;
layout (binding=4) uniform sampler2D shininessMap;
layout (binding=5) uniform sampler2D normMap;
layout (binding=6) uniform samplerCube environmentMap;


vec3 calcNewNormal()
{
	vec3 normal = normalize(varyingNormal);
	vec3 tangent = normalize(varyingTangent);
	tangent = normalize(tangent - dot(tangent, normal) * normal);
	vec3 bitangent = cross(tangent, normal);
	mat3 tbn = mat3(tangent, bitangent, normal);
	vec3 retrievedNormal = texture(normMap,tc).xyz;
	retrievedNormal = retrievedNormal * 2.0 - 1.0;
	vec3 newNormal = tbn * retrievedNormal;
	newNormal = normalize(newNormal);
	return newNormal;
}

void main(void)
{
	//Fog Works
	vec4 fogColor = vec4(0.3, 0, 0.4, 0.0);	
	
	float fogStart = 200;
	float fogEnd = 500;
	
	
	// the distance from the camera to the vertex in eye space is simply the length of a
	// vector to that vertex, because the camera is at (0,0,0) in eye space.
	float dist = length(vertEyeSpacePos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	//End Fog Works
	
	//color = texture(s,tc);
	vec4 textureColor = texture(textureSample, tc);
	
	vec4 ambientColor = texture(ambientColor, tc);
	vec4 diffuseColor = texture(diffuseColor, tc);
	vec4 specularColor = texture(specularColor, tc);
	vec4 shininessLevel = texture(shininessMap, tc);
	
	
	//*
	// Taken from Prog 7-3 blinnPhong and adapting it
	// This is mostly because I like the output of the blinnPhong
	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = calcNewNormal();
	vec3 V = normalize(-v_matrix[3].xyz - varyingVertPos);
	
	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	
	// halfway vector varyingHalfVector was computed in the vertex shader,
	// and interpolated prior to reaching the fragment shader.
	// It is copied into variable H here for convenience later.
	vec3 H = normalize(reflect(-L,N));
	
	// get angle between the normal and the halfway vector
	float cosPhi = dot(H,N);

	// compute ADS contributions (per pixel):
	//*Anything mentioning material. is invalid with this implementation
	
	
	vec3 ambient = ((globalAmbient * ambientColor) + (light.ambient * ambientColor)).xyz;
	vec3 diffuse = light.diffuse.xyz * diffuseColor.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * specularColor.xyz * pow(max(cosPhi,0.0), shininessLevel.x*3.0);
	
	//Taking the environment reflection and then filtering it based off the material
	
	vec3 r = -reflect(normalize(-vVertPos), N);
	
	vec3 reflectionFilter = (textureColor.xyz+specularColor.xyz);
	float maxFilter = max(max(reflectionFilter.x, reflectionFilter.y), reflectionFilter.z);
	if(maxFilter < 1){
		maxFilter = 1;
	}
	vec3 reflectionColor = ((texture(environmentMap,r)).xyz) *  (reflectionFilter/maxFilter) * shininessLevel.x;
	
	vec3 adsRaw = (ambient + diffuse + specular);
	float maxValue =  max(max(adsRaw.x, adsRaw.y), adsRaw.z);
	if(maxValue < 1){
		maxValue = 1;
	}
	
	
	vec4 colorSample = (textureColor * vec4((adsRaw), 1.0))+vec4(reflectionColor, 1.0);
	
	color = mix(fogColor,colorSample,fogFactor);
	//color = vec4(ambient,1);
	//color = light.diffuse;
	//color = ambientColor;
	//color = textureColor;
	
	//Observation
	//Running Clip Studio Paint in parallel to the program causes the program to fail
	
	//*/
}
