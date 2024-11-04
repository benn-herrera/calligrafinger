package com.tinybitsinteractive.calligrafinger.render

import android.opengl.GLES32
import com.tinybitsinteractive.calligrafinger.util.Logger
import java.nio.IntBuffer


internal class IndexBuffer {
    private data class GpuState(val bufferIds: IntBuffer)

    companion object {
        private val logger by lazy { Logger.create("IndexBuffer") }
        private val kTarget = GLES32.GL_ELEMENT_ARRAY_BUFFER
        val kGlIndexType = GLES32.GL_UNSIGNED_SHORT
        val kIndexSizeBytes = 2
    }

    private var _size : Int = 0
    private var gpuState: GpuState? = null

    val size get() = _size

    private val sizeBytes get() = _size * kIndexSizeBytes

    fun create(indices: Array<Short>) {
        assert(gpuState == null)

        clearGLErrors()

        genBuffer().let { idBuf ->
            createByteBackedShortBuffer(indices.size).let { shortBuf ->
                shortBuf.put(indices.toShortArray())
                shortBuf.position(0)
                GLES32.glBindBuffer(kTarget, idBuf[0])
                GLES32.glBufferData(
                    kTarget,
                    sizeBytes,
                    shortBuf,
                    GLES32.GL_DYNAMIC_DRAW
                )
            }
            gpuState = GpuState(idBuf)
        }
    }

    fun destroy() {
        gpuState?.let {
            GLES32.glDeleteBuffers(it.bufferIds.capacity(), it.bufferIds)
            gpuState = null
        }
    }

    fun bind() {
        gpuState?.apply {
            GLES32.glBindBuffer(kTarget, bufferIds[0])
        }
    }
}
