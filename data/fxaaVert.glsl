#version 130

uniform mat4 transform;

in vec4 vertex;
in vec2 texCoord;

out vec2 vertTexCoord;

void main() {
    vertTexCoord = texCoord;
    gl_Position = transform * vertex;
}