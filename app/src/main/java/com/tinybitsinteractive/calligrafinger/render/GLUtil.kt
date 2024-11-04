package com.tinybitsinteractive.calligrafinger.render

import android.opengl.GLES32
import com.tinybitsinteractive.calligrafinger.util.Logger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.nio.ShortBuffer

internal fun genBuffer(): IntBuffer {
    val idBuf = IntBuffer.allocate(1)
    GLES32.glGenBuffers(idBuf.capacity(), idBuf)
    assert(idBuf[0] >= 0)
    return idBuf
}

internal fun genVertexArray(): IntBuffer {
    val idBuf = IntBuffer.allocate(1)
    GLES32.glGenVertexArrays(idBuf.capacity(), idBuf)
    assert(idBuf[0] >= 0)
    return idBuf
}

internal fun createByteBackedIntBuffer(capacity: Int): IntBuffer {
    return ByteBuffer.allocateDirect(capacity * 4).run {
        order(ByteOrder.nativeOrder())
        asIntBuffer()
    }
}

internal fun createByteBackedShortBuffer(capacity: Int): ShortBuffer {
    return ByteBuffer.allocateDirect(capacity * 2).run {
        order(ByteOrder.nativeOrder())
        asShortBuffer()
    }
}

internal fun createByteBackedShortBuffer(shortArr: ShortArray): ShortBuffer {
    return createByteBackedShortBuffer(shortArr.size).also {
        it.put(shortArr)
        it.position(0)
    }
}

internal fun sizeBytesForGLNumericType(glType: Int) = when(glType) {
    GLES32.GL_BYTE, GLES32.GL_UNSIGNED_BYTE -> {
        1
    }
    GLES32.GL_SHORT, GLES32.GL_UNSIGNED_SHORT, GLES32.GL_HALF_FLOAT -> {
        2
    }
    GLES32.GL_INT, GLES32.GL_UNSIGNED_INT, GLES32.GL_FLOAT -> {
        4
    }
    else -> {
        assert(false) { "unsupported numeric type $glType" }
        0
    }
}

internal fun glErrorToString(errCode: Int): String? {
    when(errCode) {
        GLES32.GL_NO_ERROR -> {
            return null
        }
        GLES32.GL_INVALID_ENUM -> {
            return "GL_INVALID_ENUM"
        }
        GLES32.GL_INVALID_VALUE -> {
            return "GL_INVALID_ENUM"
        }
        GLES32.GL_INVALID_OPERATION -> {
            return "GL_INVALID_OPERATION"
        }
        GLES32.GL_INVALID_FRAMEBUFFER_OPERATION -> {
            return "GL_INVALID_FRAMEBUFFER_OPERATION"
        }
        GLES32.GL_OUT_OF_MEMORY -> {
            return "GL_OUT_OF_MEMORY"
        }
        else -> {
            return "UNKNOWN_ERROR"
        }
    }
}

internal fun clearGLErrors() {
    while(GLES32.glGetError() != GLES32.GL_NO_ERROR);
}

internal fun logGLErrors(logger: Logger) {
    while (true) {
        glErrorToString(GLES32.glGetError())?.let{
            logger.err(it)
        } ?: break
    }
}

internal fun checkCompileResult(logger: Logger, shaderId: Int, code: String? = null): Boolean {
    if (GLES32.glIsShader(shaderId)) {
        GLES32.glGetShaderInfoLog(shaderId).let {
            if (it.isNotEmpty()) {
                code?.let {
                    logger.err("===shader=code====\n$it\n================")
                }
                logger.err(it)
                return false
            }
        }
        return true
    }
    else if (GLES32.glIsProgram(shaderId)) {
        GLES32.glGetProgramInfoLog(shaderId).let {
            if (it.isNotEmpty()) {
                logger.err(it)
                return false
            }
        }
        return true
    }
    logger.err("$shaderId is not a valid shader or program id")
    return false
}
