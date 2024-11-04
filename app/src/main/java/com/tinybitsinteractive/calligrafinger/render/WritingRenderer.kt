package com.tinybitsinteractive.calligrafinger.render

import android.opengl.GLES32
import androidx.compose.ui.graphics.Color
import android.opengl.GLSurfaceView
import com.tinybitsinteractive.calligrafinger.util.Logger
import glm_.Java.Companion.glm
import glm_.mat3x3.Mat3
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

//
// TODO:
//     * Paper background using vellum texture
//     * Specular highlights on ink surface - wet ink look
//

class WritingRenderer : GLSurfaceView.Renderer {
    companion object {
        val kRequiredGLVersion = 3
        private val logger by lazy { Logger.create("WritingRenderer") }
        private val kMaxStrokePointCount = 1024
    }

    private data class GpuState(
        val strokeShader: StrokeShader,
        val activeStroke: ActiveStroke,
        val savedStrokes: MutableList<SavedStroke>
    )

    private var gpuState: GpuState? = null
    private var surfaceSize = Vec2()
    private var mvp: Mat3 = Mat3(1)
    private var isSavePending = false
    // private var pointScale = 0.0f

    //
    // may not need these to be cache backed properties
    // but let's leave it for now
    //
    private var _backgroundColor = Color.White.toVec4()
    var backgroundColor: Vec4
        get() = _backgroundColor
        set(value) {
            _backgroundColor = value
        }

    private var _strokeColor = Color.Black.toVec4()
    var strokeColor: Vec4
        get() = _strokeColor
        set(value) {
            _strokeColor = value
        }

    private fun saveActiveStroke() {
        if (!isSavePending) {
            return
        }
        isSavePending = false
        gpuState?.apply {
            if (activeStroke.isNotEmpty()) {
                savedStrokes.add(SavedStroke(activeStroke))
            }
            activeStroke.clear()
        }
    }

    private fun addPoint(pt: Vec2, isLastPoint: Boolean) {
        if (isSavePending) {
            return
        }
        gpuState?.apply {
            val pn = ((pt / surfaceSize) * 2.0f - v11) * Vec2(1, -1)
            if (glm.clamp(pn,-1.0f, 1.0f) == pn) {
                // logger.info("wr: $pt -> $pn")
                activeStroke.queuePoint(pn, strokeColor)
            }
            isSavePending = isLastPoint
        }
    }

    fun beginStroke(pt: Vec2) {
        addPoint(pt, false)
    }

    fun extendStroke(pt: Vec2) {
        addPoint(pt, false)
    }

    fun endStroke(pt: Vec2) {
        addPoint(pt, true)
    }

    fun clear() {
        gpuState?.apply {
            for (ss in savedStrokes) {
                ss.destroy()
            }
            savedStrokes.clear()
            activeStroke.clear()
            isSavePending = false
        }
    }

    private fun createGpuStateSafe() {
        if (gpuState != null) {
            return
        }

        val strokeShader = StrokeShader().also {
            it.createGpuStateSafe()
        }

        gpuState = GpuState(
            strokeShader = strokeShader,
            activeStroke = ActiveStroke(kMaxStrokePointCount),
            savedStrokes = mutableListOf()
        )
    }

    private fun destroyGpuStateOnce() {
        gpuState?.apply {
            activeStroke.destroy()
            for (ss in savedStrokes) {
                ss.destroy()
            }
            savedStrokes.clear()
            strokeShader.destroyGpuStateOnce()
            gpuState = null
        }
        Vertex.destroyVertexLayoutOnce()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        destroyGpuStateOnce()
        createGpuStateSafe()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceSize = Vec2(width, height)
        // are width and height 0 if we lose the surface?
        if (surfaceSize == v00) {
            logger.info("surface lost")
            destroyGpuStateOnce()
        } else {
            createGpuStateSafe()
            logger.info("surface size: $surfaceSize")
            val maxDim = maxof(surfaceSize)
            // pointScale = 2.0f / maxDim
            val aspectScale = 1.0f // minof(surfaceSize) / maxof(surfaceSize)
            mvp = Mat3(
                v100, // * surfaceSize.x / maxDim,
                v010, // * surfaceSize.y / maxDim,
                v000 + v001
            )
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        gpuState?.apply {
            activeStroke.flushQueuedPoints()
            saveActiveStroke()
            drawClear()
            strokeShader.use(mvp)
            for (ss in savedStrokes) {
                ss.draw()
            }
            activeStroke.draw()
        } ?: drawClear()
    }

    private fun drawClear() {
        GLES32.glViewport(0, 0, surfaceSize.x.toInt(), surfaceSize.y.toInt())
        GLES32.glClearColor(
            _backgroundColor.r,
            _backgroundColor.g,
            _backgroundColor.b,
            _backgroundColor.a)
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT)
    }
}
