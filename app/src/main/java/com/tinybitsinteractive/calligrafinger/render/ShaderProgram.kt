package com.tinybitsinteractive.calligrafinger.render

import android.opengl.GLES32
import com.tinybitsinteractive.calligrafinger.util.Logger

internal abstract class ShaderProgram {
    private data class GpuState(
        val shaderProgramId: Int
    )

    companion object {
        private val glslVersionDecl = "#version 320 es\n"
        private val precisionDecl = "precision highp float;\nprecision highp int\n;"
        private val commonDecls = glslVersionDecl + precisionDecl
    }

    abstract fun logger(): Logger

    abstract fun vertexShaderCode(): String

    abstract fun fragmentShaderCode(): String

    private var gpuState: GpuState? = null

    private fun checkCompileResult(shaderId: Int, code: String? = null): Boolean {
        return checkCompileResult(logger(), shaderId, code)
    }

    fun createGpuStateSafe() {
        Vertex.createVertexLayoutSafe()
        if (gpuState != null) {
            return
        }

        var success = true
        val vertexShaderCode = commonDecls + this.vertexShaderCode()
        val vertexShaderId = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER)
        GLES32.glShaderSource(vertexShaderId, vertexShaderCode)
        GLES32.glCompileShader(vertexShaderId)
        success = checkCompileResult(vertexShaderId, vertexShaderCode) && success

        val fragmentShaderCode = commonDecls + this.fragmentShaderCode()
        val fragmentShaderId = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER)
        GLES32.glShaderSource(fragmentShaderId, fragmentShaderCode)
        GLES32.glCompileShader(fragmentShaderId)
        success = checkCompileResult(fragmentShaderId, fragmentShaderCode) && success

        val shaderProgramId = GLES32.glCreateProgram()
        GLES32.glAttachShader(shaderProgramId, vertexShaderId)
        GLES32.glAttachShader(shaderProgramId, fragmentShaderId)
        GLES32.glLinkProgram(shaderProgramId)
        success = checkCompileResult(shaderProgramId) && success

        assert(success)

        clearGLErrors()

        // once the final program is linked the stages are no longer needed.
        if (GLES32.glIsShader(vertexShaderId)) {
            GLES32.glDeleteShader(vertexShaderId)
        }
        if (GLES32.glIsShader(fragmentShaderId)) {
            GLES32.glDeleteShader(fragmentShaderId)
        }

        gpuState = GpuState(
            shaderProgramId = shaderProgramId)

        logger().info("program created.")
    }

    fun destroyGpuStateOnce() {
        gpuState?.apply {
            GLES32.glDeleteProgram(shaderProgramId)
            gpuState = null
            logger().info("program destroyed.")
        }
    }

    fun use(onUse: () -> Unit) {
        gpuState?.apply {
            GLES32.glUseProgram(shaderProgramId)
            onUse()
        }
    }
}
