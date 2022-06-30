attribute vec4 vPosition;
attribute vec4 vCoord;
varying vec2 aCoord;

uniform mat4 textureMatrix;

void main(){
    gl_Position = vPosition;
    aCoord = (textureMatrix * vCoord).xy;
}
