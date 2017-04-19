/**使用GL_OES_EGL_image_external扩展处理，来增强GLSL*/
#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES texture; //定义扩展的的纹理取样器amplerExternalOES
varying vec2 v_TexCoordinate;

void main () {
    vec4 color = texture2D(texture, v_TexCoordinate);
    gl_FragColor = color;
}