package com.tinybitsinteractive.calligrafinger.render
import android.view.MotionEvent
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import glm_.vec4.Vec4
import androidx.compose.ui.graphics.Color
import glm_.vec4.swizzle.xyz
import glm_.glm

fun Color.toVec4() = Vec4(red, green, blue, alpha)
fun Color.toVec3() = Vec3(red, green, blue)
fun Color.Companion.fromVec3(v: Vec3) = Color(v.r, v.g, v.b, 1.0f)
fun Color.Companion.fromVec4(v: Vec4) = Color(v.r, v.g, v.b, v.a)
fun saturate(v: Float) = glm.clamp(v, 0.0f, 1.0f)
fun saturate(v: Vec2) = glm.clamp(v, 0.0f, 1.0f)
fun saturate(v: Vec3) = glm.clamp(v, 0.0f, 1.0f)
fun saturate(v: Vec4) = glm.clamp(v, 0.0f, 1.0f)

fun maxof(v: Vec2) = v.x.coerceAtLeast(v.y)
fun maxof(v: Vec3) = v.x.coerceAtLeast(v.y).coerceAtLeast(v.z)
fun maxof(v: Vec4) = v.x.coerceAtLeast(v.y).coerceAtLeast(v.z).coerceAtLeast(v.w)

fun minof(v: Vec2) = v.x.coerceAtMost(v.y)
fun minof(v: Vec3) = v.x.coerceAtMost(v.y).coerceAtMost(v.z)
fun minof(v: Vec4) = v.x.coerceAtMost(v.y).coerceAtMost(v.z).coerceAtMost(v.w)

fun aspect(v: Vec2) = v.x / v.y
fun area(v: Vec2) = v.x * v.y
fun volume(v: Vec3) = v.x * v.y * v.z

val v00 = Vec2(0, 0)
val v01 = Vec2(0, 1)
val v10 = Vec2(1, 0)
val v11 = Vec2(1, 1)

val v000 = Vec3(0, 0, 0)
val v010 = Vec3(0, 1, 0)
val v100 = Vec3(1, 0, 0)
val v110 = Vec3(1, 1, 0)
val v001 = Vec3(0, 0, 1)
val v011 = Vec3(0, 1, 1)
val v101 = Vec3(1, 0, 1)
val v111 = Vec3(1, 1, 1)

val v0000 = Vec4(0, 0, 0, 0)
val v0100 = Vec4(0, 1, 0, 0)
val v1000 = Vec4(1, 0, 0, 0)
val v1100 = Vec4(1, 1, 0, 0)
val v0010 = Vec4(0, 0, 1, 0)
val v0110 = Vec4(0, 1, 1, 0)
val v1010 = Vec4(1, 0, 1, 0)
val v1110 = Vec4(1, 1, 1, 0)
val v0001 = Vec4(0, 0, 0, 1)
val v0101 = Vec4(0, 1, 0, 1)
val v1001 = Vec4(1, 0, 0, 1)
val v1101 = Vec4(1, 1, 0, 1)
val v0011 = Vec4(0, 0, 1, 1)
val v0111 = Vec4(0, 1, 1, 1)
val v1011 = Vec4(1, 0, 1, 1)
val v1111 = Vec4(1, 1, 1, 1)

// alpha preserving scaling
operator fun Vec4.times(v3: Vec3) = Vec4(v3 * xyz, w)
operator fun Vec4.div(v3: Vec3) = Vec4(v3 / xyz, w)

fun Vec4.toPackedIntColor(): Int = (saturate(this) * 255.0f).run {
    (a.toInt() shl 24) or (b.toInt() shl 16) or (g.toInt() shl 8) or r.toInt()
}

fun Vec4.Companion.fromPackedIntColor(packed: Int): Vec4 {
    return Vec4(
        (packed and 255),
        ((packed ushr 8) and 255),
        ((packed ushr 16) and 255),
        ((packed ushr 24) and 255)
    ) / 255.0f
}

fun MotionEvent.toVec2() = Vec2(x, y)
fun MotionEvent.PointerCoords.toVec2() = Vec2(x, y)
fun MotionEvent.historicalToVec2(historyIndex: Int) = Vec2(getHistoricalX(historyIndex), getHistoricalY(historyIndex))
