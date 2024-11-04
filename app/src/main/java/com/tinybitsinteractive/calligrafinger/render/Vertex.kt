package com.tinybitsinteractive.calligrafinger.render

import android.opengl.GLES32
import com.tinybitsinteractive.calligrafinger.util.Logger
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import java.nio.IntBuffer

internal data class Vertex(
    val pos: Vec2,
    val color: Vec4,
    val tex: Vec2 = v00
)
{
    companion object {
        // GLM convention is that length is element count
        // and size is byte count
        val kHasTexAttrib = false

        private val logger by lazy { Logger.create("Vertex") }

        private val kPosAttrib = 0
        private val kPosChannelCount = 2
        private val kPosChannelType = GLES32.GL_FLOAT
        private val kPosSize = kPosChannelCount * sizeBytesForGLNumericType(kPosChannelType)

        private val kColorAttrib = 1
        private val kColorChannelCount = 4
        private val kColorChannelType = GLES32.GL_UNSIGNED_BYTE // GLES32.GL_FLOAT
        private val kColorSize = kColorChannelCount * sizeBytesForGLNumericType(kColorChannelType)

        private val kTexAttrib = 2
        private val kTexChannelCount = 2
        private val kTexChannelType = GLES32.GL_FLOAT
        private val kTexSize = kTexChannelCount * sizeBytesForGLNumericType(kTexChannelType)

        val kSizeBytes = kPosSize + kColorSize + (if (kHasTexAttrib) kTexSize else 0)
        val kSizeInts = run { val si = kSizeBytes / 4; assert(si * 4 == kSizeBytes); si }
        val kStride = kSizeBytes
        // arbitrary value. choose non-zero number to ensure correct usage instead of
        // possibly benefitting from accidental default 0
        val kFormatBindingId = 7

        private val kPosOffsetBytes = 0
        private val kColorOffsetBytes = kPosOffsetBytes + kPosSize
        private val kTexOffsetBytes = kColorOffsetBytes + kColorSize
        private var vertexArrayId : IntBuffer? = null

        val glslDecl =
            "layout(location = $kPosAttrib) in vec2 aPos;\n" +
            "layout(location = $kColorAttrib) in vec4 aColor;\n" +
            if (kHasTexAttrib) "layout(location = $kTexAttrib) in vec2 aTex;\n" else ""

        fun createVertexLayoutSafe() {
            if (vertexArrayId != null) {
                return
            }

            // https://stackoverflow.com/questions/21652546/what-is-the-role-of-glbindvertexarrays-vs-glbindbuffer-and-what-is-their-relatio

            vertexArrayId = genVertexArray().also {
                GLES32.glBindVertexArray(it[0])
                GLES32.glEnableVertexAttribArray(kPosAttrib)
                GLES32.glVertexAttribFormat(
                    kPosAttrib,
                    kPosChannelCount,
                    kPosChannelType,
                    false,
                    kPosOffsetBytes
                )
                GLES32.glVertexAttribBinding(kPosAttrib, kFormatBindingId)

                GLES32.glEnableVertexAttribArray(kColorAttrib)
                GLES32.glVertexAttribFormat(
                    kColorAttrib,
                    kColorChannelCount,
                    kColorChannelType,
                    true,
                    kColorOffsetBytes
                )
                GLES32.glVertexAttribBinding(kColorAttrib, kFormatBindingId)

                if (kHasTexAttrib) {
                    GLES32.glEnableVertexAttribArray(kTexAttrib)
                    GLES32.glVertexAttribFormat(
                        kTexAttrib,
                        kTexChannelCount,
                        kTexChannelType,
                        false,
                        kTexOffsetBytes
                    )
                    GLES32.glVertexAttribBinding(kTexAttrib, kFormatBindingId)
                }
            }

            logger.info("layout created")
        }

        fun destroyVertexLayoutOnce() {
            vertexArrayId?.let {
                GLES32.glDeleteVertexArrays(it.capacity(), it)
                vertexArrayId = null
                logger.info("layout destroyed")
            }
        }
    }

    constructor(buf: IntBuffer, intOffset: Int) :
            this(
                Vec2(Float.fromBits(buf[intOffset + 0]), Float.fromBits(buf[intOffset + 1])),
                Vec4.fromPackedIntColor(buf[intOffset + 2]),
                if (kHasTexAttrib)
                    Vec2(Float.fromBits(buf[intOffset + 3]), Float.fromBits(buf[intOffset + 4]))
                else v00
            )

    override operator fun equals(other: Any?): Boolean {
        return other === this || (other as Vertex?)?.let {
                rhs ->
            pos == rhs.pos && color == rhs.color && (!kHasTexAttrib || tex == rhs.tex)
        } ?: false
    }

    override fun toString() = if (kHasTexAttrib) "(pos=$pos clr=$color tex=$tex)" else "(pos=$pos clr=$color)"
}

internal fun IntBuffer.write(v: Vertex) {
    put(v.pos.x.toBits())
    put(v.pos.y.toBits())
    put(v.color.toPackedIntColor())
    if (Vertex.kHasTexAttrib) {
        put(v.tex.s.toBits())
        put(v.tex.t.toBits())
    }
}
