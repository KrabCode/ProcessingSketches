/*
  Part of the Processing project - http://processing.org
  Copyright (c) 2012-15 The Processing Foundation
  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

uniform mat4 modelviewMatrix;
uniform mat4 transformMatrix;
uniform mat3 normalMatrix;
uniform mat4 texMatrix;

uniform int lightCount;
uniform vec4 lightPosition[8];
uniform vec3 lightNormal[8];
uniform vec3 lightAmbient[8];
uniform vec3 lightDiffuse[8];
uniform vec3 lightSpecular[8];      
uniform vec3 lightFalloff[8];
uniform vec2 lightSpot[8];

attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;
attribute vec3 tangent;
attribute vec2 texCoord;

out vec4 vColor;

attribute vec4 ambient;
attribute vec4 specular;
attribute vec4 diffuse;
attribute vec4 emissive;
attribute float shininess;

out vec4 vAmbient;
out vec4 vSpecular;
out vec4 vEmissive;
out vec4 vDiffuse;
out float vShininess;

out vec4 vertTexCoord;

out vec3 ecVertex;
out vec3 vNormal;
out vec3 vTangent;

const float zero_float = 0.0;
const float one_float = 1.0;
const vec3 zero_vec3 = vec3(0);

void main() {
  // Vertex in clip coordinates
  gl_Position = transformMatrix * position;
    
  // Vertex in eye coordinates
  ecVertex = vec3(modelviewMatrix * position);
  
  // Normal vector in eye coordinates
  vNormal = normalize(normal);
  vTangent = normalize(tangent);

  vertTexCoord = texMatrix * vec4(texCoord, 1.0, 1.0);        
	
	vColor = color;
	vAmbient = ambient;
  vDiffuse = diffuse;
	vSpecular = specular;
	vShininess = shininess;
}