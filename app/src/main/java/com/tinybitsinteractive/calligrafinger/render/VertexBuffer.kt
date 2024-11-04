package com.tinybitsinteractive.calligrafinger.render

import android.opengl.GLES32
import com.tinybitsinteractive.calligrafinger.util.Logger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

internal class VertexBuffer {
    companion object {
        private val logger by lazy { Logger.create("VertexBuffer") }
        private val kTarget = GLES32.GL_ARRAY_BUFFER
    }
    private data class GpuState(
        val bufferId: IntBuffer
    )

    private var gpuState: GpuState? = null

    private var _capacity : Int = 0
    private var _size : Int = 0
    private var _isModifiable = false

    val capacity get() = _capacity
    val size get() = _size
    val isModifiable get() = _isModifiable

    private val capacityBytes get() = _capacity * Vertex.kSizeBytes

    fun create(capacity: Int) {
        assert(gpuState == null)

        _capacity = capacity
        _size = 0
        _isModifiable = true

        val idBuf = genBuffer()

        clearGLErrors()

        GLES32.glBindBuffer(kTarget, idBuf[0])

        createByteBackedIntBuffer(capacity * Vertex.kSizeInts).let {
            it.clear()
            GLES32.glBufferData(
                kTarget,
                capacityBytes,
                it,
                GLES32.GL_DYNAMIC_DRAW
            )
        }

        logGLErrors(logger)

        gpuState = GpuState(idBuf)

        logger.info("created for writing.")
    }

    fun copyStatic(other: VertexBuffer) {
        assert(gpuState == null)

        _capacity = other.size
        _size = _capacity
        _isModifiable = false

        val idBuf = genBuffer()

        clearGLErrors()

        other.useMappedBuf(Usage.Copy) { srcBuf ->
            GLES32.glBindBuffer(kTarget, idBuf[0])
            GLES32.glBufferData(
                kTarget,
                capacityBytes,
                srcBuf,
                GLES32.GL_STATIC_DRAW
            )
        }

        logGLErrors(logger)

        gpuState = GpuState(idBuf)

        logger.info("created by copy.")
    }

    fun destroy() {
        gpuState?.let {
            GLES32.glDeleteBuffers(it.bufferId.capacity(), it.bufferId)
            logger.info("destroyed.")
            gpuState = null
        }
    }

    fun clear() {
        assert(isModifiable)
        _size = 0
    }

    enum class Usage {
        Read,
        Copy,
        Append
    }

    //
    // TODO: try to map once and keep mapped.
    //       are there any flags or options that
    //       prevent requirement of needing to unmap & remap each time an append is done?
    //       NOTE: specifying manual flush did not produce the desired behavior.
    //
    private fun useMappedBuf(usage: Usage, useFunc: (IntBuffer) -> Unit) {
        assert(gpuState != null && (usage == Usage.Read || isModifiable))

        gpuState!!.run {
            val bindTarget = if (usage == Usage.Copy)
                GLES32.GL_COPY_READ_BUFFER
            else
                kTarget
            val access = if (usage == Usage.Append)
                GLES32.GL_MAP_WRITE_BIT
            else
                GLES32.GL_MAP_READ_BIT
            val mapSizeBytes = if (usage == Usage.Append)
                capacityBytes
            else
                _size * Vertex.kSizeBytes
            val startPosition = if (usage == Usage.Append)
                _size * Vertex.kSizeInts
            else
                0

            GLES32.glBindBuffer(bindTarget, bufferId[0])
            val byteBuf = GLES32.glMapBufferRange(
                bindTarget,
                0,
                mapSizeBytes,
                access
            ) as ByteBuffer

            byteBuf.order(ByteOrder.nativeOrder())
            byteBuf.asIntBuffer().let { intBuf ->
                intBuf.position(startPosition)
                useFunc(intBuf)
            }
            GLES32.glUnmapBuffer(bindTarget)
        }
    }

    fun add(verts: Array<Vertex>) = add(verts.asList())

    fun add(verts: List<Vertex>) {
        if (verts.isEmpty()) {
            return
        }
        assert(isModifiable && gpuState != null)
        useMappedBuf(Usage.Append) { vertBuf ->
            assert(verts.size + _size <= _capacity)
            for (v in verts) {
                vertBuf.write(v)
                // logger.info("add: $v")
            }
            _size += verts.size
        }
    }

    fun bind() {
        gpuState?.apply {
            GLES32.glBindVertexBuffer(
                Vertex.kFormatBindingId,
                bufferId[0],
                0,
                Vertex.kStride)
        }
    }
}
