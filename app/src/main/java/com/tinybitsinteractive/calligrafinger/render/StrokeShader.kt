package com.tinybitsinteractive.calligrafinger.render

import android.opengl.GLES32
import com.tinybitsinteractive.calligrafinger.util.Logger
import glm_.mat3x3.Mat3

internal class StrokeShader : ShaderProgram() {
    companion object {
        private val logger by lazy { Logger.create("StrokeShaderProgram") }

        // uniform location id is arbitrary. use a non-zero value to help ensure correct usage
        // instead of accidentally benefitting from a default 0 somewhere.
        private val uMVPLocaion = 9
        private fun varying(dir: String) = "$dir vec4 vColor;\n" +
                if (Vertex.kHasTexAttrib) "$dir vec2 vTex;\n" else ""
    }

    override fun logger() = logger

    override fun vertexShaderCode() =
                "layout(location = $uMVPLocaion) uniform mat3 uMVP;\n" +
                Vertex.glslDecl +
                varying("out") +
                """
                    void main() {
                        vColor = aColor;
                        ${if (Vertex.kHasTexAttrib) "vTex = aTex;" else ""}
                        gl_Position = vec4((uMVP * vec3(aPos, 1.0)).xy, 0.0, 1.0);
                    }
                """.trimIndent()

    override fun fragmentShaderCode() =
                varying("in") +
                """
                    out vec4 fragColor;
                    void main() {
                        fragColor = vColor;
                    }
                """.trimIndent()


    fun use(mvp: Mat3) {
        super.use {
            GLES32.glUniformMatrix3fv(
                uMVPLocaion, 1, false,
                mvp.toFloatArray(), 0
            )
        }
    }
}
