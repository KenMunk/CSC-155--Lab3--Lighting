#version 430
#include "common.glsl"

//Takes the texture coordinate from the vertex shader since the texture coordinate is from a VBO
in vec2 tc;


in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;

out vec4 color;

uniform vec4 globalAmbient;
uniform PositionalLight light;
//the material struct has been removed and replaced with 4 textures
uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;

//Creates a texture sampler for the texture at binding 0
layout (binding=0) uniform sampler2D ambientColor;
layout (binding=1) uniform sampler2D diffuseColor;
layout (binding=2) uniform sampler2D specularColor;
layout (binding=3) uniform sampler2D shininessMap;

void main(void)
{
	color = texture(s,tc);
	
	/*
	
	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
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
	vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
	vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
	fragColor = vec4((ambient + diffuse + specular), 1.0);
	
	//*/
}
