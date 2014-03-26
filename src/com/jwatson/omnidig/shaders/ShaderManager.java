package com.jwatson.omnidig.shaders;

public class ShaderManager {
	
	public final static String BG_Shader_Vert = ""
			+"attribute vec4 a_position;\n"
			+"attribute vec4 a_color;\n"
			+"attribute vec2 a_texCoord0;\n"
			+"attribute vec2 a_texCoord1;\n"

			+"uniform mat4 u_projTrans;\n"

			+"varying vec2 v_texCoords;\n"
			+"varying vec2 v_texCoords2;\n"
			+"void main() {\n"

			+"		v_texCoords = a_texCoord0;\n"
			+"		v_texCoords2 = a_texCoord1;\n"
			+"		gl_Position =  u_projTrans * a_position;\n"
			+"	}\n";
			
	public final static String bg_Frag_Shader = ""
			+"#ifdef GL_ES \n"
			+"precision lowp float;\n"
			+"precision lowp int;\n"
			+"#endif\n"

			+"uniform float u_time;\n"
			+"uniform sampler2D u_texture;\n"


			//+"varying vec4 v_color;\n"
			+"varying vec2 v_texCoords;\n"
			+"varying vec2 v_texCoords2;\n"

	+"void main() { \n"
	//+"lowp vec4 col = texture2D(u_texture, v_texCoords);\n"
	+"gl_FragColor = (texture2D(u_texture, v_texCoords) - (texture2D(u_texture, v_texCoords)-texture2D(u_texture, v_texCoords2))*u_time);\n"	
	+"}\n";
	
	public final static String terrain_frag = ""
			+"#ifdef GL_ES \n"
			+"precision lowp float;\n"
			+"precision lowp int;\n"
			+"#endif\n"
			+"uniform sampler2D u_texture;\n"
			
			+"varying float v_brightness;\n"
			+"varying vec2 v_texCoords;\n"
			+"varying vec2 v_texCoords2;\n"
			+"varying vec4 v_color;\n"
			
			+"void main() { \n"
			//+"lowp vec4 col = texture2D(u_texture, v_texCoords);\n"
//			+"lowp float alpha = ;\n"
			+"gl_FragColor = texture2D(u_texture, v_texCoords) * vec4(vec3(v_brightness),texture2D(u_texture, v_texCoords2).a);\n"
			+"}\n";
	
	public final static String terrain_vertex = ""
			+"attribute vec4 a_position;\n"
			+"attribute vec4 a_color;\n"
			+"attribute vec2 a_texCoord0;\n"
			+"attribute vec2 a_texCoord1;\n"
			+"attribute float a_brightness;\n"

			+"uniform mat4 u_projTrans;\n"

			+"varying vec2 v_texCoords;\n"
			+"varying vec2 v_texCoords2;\n"
			+"varying float v_brightness;\n"
			+"varying vec4 v_color;\n"

			+"void main() {\n"

			+"v_texCoords = a_texCoord0;\n"
			+"v_texCoords2 = a_texCoord1;\n"
			+"v_brightness = a_brightness;\n"
			+"v_color = a_color;\n"
			+"gl_Position =  u_projTrans * a_position;\n"
			+"}\n";

}
