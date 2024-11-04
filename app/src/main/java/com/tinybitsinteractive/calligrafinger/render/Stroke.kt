package com.tinybitsinteractive.calligrafinger.render

import android.opengl.GLES32
import com.tinybitsinteractive.calligrafinger.util.Logger
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import java.nio.ShortBuffer

internal class ActiveStroke(capacity: Int) {
    companion object {
        private val logger by lazy { Logger.create("ActiveStroke") }
    }

    var width = 0.01f
    var chisel = v11

    private val vertices = VertexBuffer().also { it.create(capacity * 2) }
    private val indices = Array(vertices.capacity) {
        i -> i.toShort()
    }.run {
        createByteBackedShortBuffer(toShortArray())
    }
    private var pendingVertices = mutableListOf<Vertex>()

    fun destroy() {
        vertices.destroy()
    }

    fun clear() {
        vertices.clear()
        pendingVertices.clear()
    }

    fun copyStatic(): Pair<VertexBuffer, ShortBuffer> {
        return Pair(
            VertexBuffer().also { it.copyStatic(vertices) },
            Array(vertices.size) { i -> i.toShort() }.run { createByteBackedShortBuffer(toShortArray()) }
        )
    }

    fun queuePoint(pos: Vec2, color: Vec4) {
        synchronized(pendingVertices) {
            if (vertices.capacity - (vertices.size + pendingVertices.size) >= 2) {
                val scaledChisel = chisel * width * 0.5f
                pendingVertices.addAll(
                    listOf(
                        Vertex(pos - scaledChisel, color),
                        Vertex(pos + scaledChisel, color)
                    )
                )
            }
        }
    }

    fun flushQueuedPoints() {
        synchronized(pendingVertices) {
            vertices.add(pendingVertices)
            pendingVertices.clear()
        }
    }

    fun isNotEmpty() = vertices.size >= 3
    fun isEmpty() = vertices.size < 3

    fun draw() {
        if (vertices.size < 3) {
            return
        }
        assert(indices[1] == 1.toShort())
        //GLES32.glEnable(GLES32.GL_CULL_FACE)
        //GLES32.glCullFace(GLES32.GL_BACK)
        GLES32.glDisable(GLES32.GL_CULL_FACE)
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
        vertices.bind()
        GLES32.glDrawElements(
            GLES32.GL_TRIANGLE_STRIP,
            vertices.size,
            GLES32.GL_UNSIGNED_SHORT,
            indices)
    }
}

internal class SavedStroke(copySrc: ActiveStroke) {
    companion object {
        private val logger by lazy { Logger.create("SavedStroke") }
    }

    private val vertices: VertexBuffer
    private val indices: ShortBuffer

    init {
        val (vbuf, ibuf) = copySrc.copyStatic()
        vertices = vbuf
        indices = ibuf
    }

    fun destroy() {
        vertices.destroy()
    }

    fun draw() {
        if (vertices.size < 3) {
            return
        }
        //GLES32.glEnable(GLES32.GL_CULL_FACE)
        //GLES32.glCullFace(GLES32.GL_BACK)
        GLES32.glDisable(GLES32.GL_CULL_FACE)
        GLES32.glDisable(GLES32.GL_DEPTH_TEST)
        vertices.bind()
        GLES32.glDrawElements(
            GLES32.GL_TRIANGLE_STRIP,
            vertices.size,
            GLES32.GL_UNSIGNED_SHORT,
            indices)
    }
}
