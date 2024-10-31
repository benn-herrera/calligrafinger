package com.tinybitsinteractive.calligrafinger.render

import android.opengl.GLES32
import androidx.compose.ui.graphics.Color
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class WritingRenderer : GLSurfaceView.Renderer {
    companion object {
        val requiredGLVersion = 3
    }

    private var _drawColor = Color.Green

    var drawColor: Color
        get() = _drawColor
        set(value) { _drawColor = value }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES32.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES32.glClearColor(drawColor.red, drawColor.green, drawColor.blue, drawColor.alpha)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
    }
}
