package com.example.xxfile.utils

import android.content.Context
import android.opengl.GLES20
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder


class OpenGLUtils {
    companion object{
        fun readRawTextFile(context: Context, rawId: Int): String {
            val inputStream: InputStream = context.resources.openRawResource(rawId)
            val br = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            val sb = StringBuilder()
            try {
                while (br.readLine().also { line = it } != null) {
                    sb.append(line)
                    sb.append("\n")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return sb.toString()
        }


        /**
         * 价值着色器并编译成GPU程序
         * @param vSource
         * @param fSource
         * @return
         */
        fun loadProgram(vSource: String?, fSource: String?): Int {
            /**
             * 顶点着色器
             */
            val vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
            //加载着色器代码
            GLES20.glShaderSource(vShader, vSource)
            //编译（配置）
            GLES20.glCompileShader(vShader)

            //查看配置 是否成功
            val status = IntArray(1)
            GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, status, 0)
            check(status[0] == GLES20.GL_TRUE) {
                //失败
                "load vertex shader:" + GLES20.glGetShaderInfoLog(vShader)
            }
            /**
             * 片元着色器
             * 流程和上面一样
             */
            val fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
            //加载着色器代码
            GLES20.glShaderSource(fShader, fSource)
            //编译（配置）
            GLES20.glCompileShader(fShader)

            //查看配置 是否成功
            GLES20.glGetShaderiv(fShader, GLES20.GL_COMPILE_STATUS, status, 0)
            check(status[0] == GLES20.GL_TRUE) {
                //失败
                "load fragment shader:" + GLES20.glGetShaderInfoLog(vShader)
            }
            /**
             * 创建着色器程序
             */
            val program = GLES20.glCreateProgram()
            //绑定顶点和片元
            GLES20.glAttachShader(program, vShader)
            GLES20.glAttachShader(program, fShader)
            //链接着色器程序
            GLES20.glLinkProgram(program)
            //获得状态
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
            check(status[0] == GLES20.GL_TRUE) {
                "link program:" + GLES20.glGetProgramInfoLog(
                    program
                )
            }
            GLES20.glDeleteShader(vShader)
            GLES20.glDeleteShader(fShader)
            return program
        }
    }
}