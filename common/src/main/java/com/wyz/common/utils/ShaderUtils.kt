package com.wyz.common.utils

import android.content.res.Resources
import android.opengl.GLES20

enum class ShaderUtils {
    ;

    companion object{
        fun readText(mRes: Resources, path: String): String? {
            val result = StringBuilder()
            try {
                val `is` = mRes.assets.open(path)
                var ch: Int
                val buffer = ByteArray(1024)
                while (-1 != `is`.read(buffer).also { ch = it }) {
                    result.append(String(buffer, 0, ch))
                }
            } catch (e: Exception) {
                return null
            }
            return result.toString().replace("\\r\\n".toRegex(), "\n")
        }

        /**
         * 加载Shader
         * @param shaderType Shader类型
         * @param source Shader代码
         * @return shaderId
         */
        open fun loadShader(shaderType: Int, source: String?): Int {
            var shader = GLES20.glCreateShader(shaderType)
            if (0 != shader) {
                GLES20.glShaderSource(shader, source)
                GLES20.glCompileShader(shader)
                val compiled = IntArray(1)
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
                if (compiled[0] == 0) {
                    GLES20.glDeleteShader(shader)
                    shader = 0
                }
            }
            return shader
        }

        /**
         * 通过字符串创建GL程序
         * @param vertexSource 顶点着色器
         * @param fragmentSource 片元着色器
         * @return programId
         */
        open fun createGLProgram(vertexSource: String?, fragmentSource: String?): Int {
            val vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
            if (vertex == 0) {
                return 0
            }
            val fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
            if (fragment == 0) {
                return 0
            }
            var program = GLES20.glCreateProgram()
            if (program != 0) {
                GLES20.glAttachShader(program, vertex)
                GLES20.glAttachShader(program, fragment)
                GLES20.glLinkProgram(program)
                val linkStatus = IntArray(1)
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
                if (linkStatus[0] != GLES20.GL_TRUE) {
                    GLES20.glDeleteProgram(program)
                    program = 0
                }
            }
            return program
        }

        /**
         * 通过assets中的文件创建GL程序
         * @param res res
         * @param vertex 顶点作色器路径
         * @param fragment 片元着色器路径
         * @return programId
         */
        open fun createGLProgramByAssetsFile(res: Resources, vertex: String, fragment: String): Int {
            return createGLProgram(readText(res, vertex), readText(res, fragment))
        }
    }

}