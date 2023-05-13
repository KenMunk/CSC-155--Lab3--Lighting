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

uniform mat4 primaryColor;
uniform mat4 secondaryColor;
uniform mat4 tertiaryColor;

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

in vec3 vecPosition;

in vec4 shadow_coord;

out vec4 color;

//the material struct has been removed and replaced with 4 textures
//Creates a texture sampler for the texture at binding 0

layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D textureSample;
layout (binding=2) uniform sampler2D ambientColor;
layout (binding=3) uniform sampler2D diffuseColor;
layout (binding=4) uniform sampler2D specularColor;
layout (binding=5) uniform sampler2D shininessMap;
layout (binding=6) uniform sampler2D normMap;
layout (binding=7) uniform samplerCube environmentMap;
layout (binding=8) uniform sampler2D channelMap;
layout (binding=9) uniform sampler3D primaryNoise;
layout (binding=10) uniform sampler3D secondaryNoise;
layout (binding=11) uniform sampler3D tertiaryNoise;


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


float lookup(float x, float y)
{  	float t = textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
                                                         y * 0.001 * shadow_coord.w,
                                                         -0.01, 0.0));
	return t;
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
	vec4 channelColor = texture(channelMap, tc);
	
	vec4 primaryNC = primaryColor * texture(primaryNoise, vecPosition/4.0+0.05);
	vec4 secondaryNC = secondaryColor * texture(secondaryNoise, vecPosition/4.0+0.05);
	vec4 tertiaryNC = tertiaryColor * texture(tertiaryNoise, vecPosition/4.0+0.05);
	
	
	if(channelColor.x > 0.3 || channelColor.y > 0.3 || channelColor.z > 0.3){
		
		textureColor = (
			(primaryNC * channelColor.x) + 
			(secondaryNC * channelColor.y) + 
			(tertiaryNC * channelColor.z)
		);
		
	}
	
	
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
	vec3 H = normalize(varyingHalfVector);
	
	// get angle between the normal and the halfway vector
	float cosPhi = dot(H,N);

	// compute ADS contributions (per pixel):
	//*Anything mentioning material. is invalid with this implementation
	
	
	/*
		Working out the light
	
	*/
	
	vec3 ambient = ((globalAmbient * ambientColor) + (light.ambient * ambientColor)).xyz;
	vec3 diffuse = light.diffuse.xyz * diffuseColor.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * specularColor.xyz * pow(max(cosPhi,0.0), shininessLevel.x*3.0);
	
	vec3 adsRaw = (ambient + diffuse + specular);
	float maxValue =  max(max(adsRaw.x, adsRaw.y), adsRaw.z);
	if(maxValue < 1){
		maxValue = 1;
	}
	
	
	
	
	//Taking the environment reflection and then filtering it based off the material
	
	vec3 r = -reflect(normalize(-vVertPos), N);
	
	vec3 reflectionFilter = (textureColor.xyz+specularColor.xyz);
	float maxFilter = max(max(reflectionFilter.x, reflectionFilter.y), reflectionFilter.z);
	if(maxFilter < 1){
		maxFilter = 1;
	}
	vec3 reflectionColor = ((texture(environmentMap,r)).xyz) *  (reflectionFilter/maxFilter) * shininessLevel.xyz/10;
	
	/*
		Now on to the dark subject of shadows
	*/
	
	//*
	float shadowFactor=0.0;
	
	float swidth = 1;
	vec2 o = mod(floor(gl_FragCoord.xy), 1) * swidth;
	shadowFactor += lookup(-0.5*swidth + o.x,  0.5*swidth - o.y);
	shadowFactor += lookup(-0.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x,  0.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor = shadowFactor / 4.0;
	
	
	vec4 shadowColor = vec4(ambient,1.0)*textureColor;
	//*/
	/*
		Combining all of the colors from the lights, and shadows
	*/
	/*
	vec4 litColor = (textureColor * vec4((adsRaw), 1.0));
	vec4 colorSample = litColor+vec4(reflectionColor, 1.0);
	
	//*/ 
	//*
	
	vec4 litColor = (textureColor * vec4((adsRaw), 1.0));
	
	vec4 colorSample = mix(shadowColor,litColor,shadowFactor)+vec4(reflectionColor, 1.0);
	
	color = mix(fogColor,colorSample,fogFactor);
	//color = texture(tertiaryNoise, vecPosition/4.0+0.05);
	//color = vec4(ambient,1);
	//color = light.diffuse;
	//color = ambientColor;
	//color = textureColor;
	
	//Observation
	//Running Clip Studio Paint in parallel to the program causes the program to fail
	
	//*/
}
