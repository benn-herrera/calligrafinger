package com.tinybitsinteractive.calligrafinger.ui

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_OUTSIDE
import android.view.MotionEvent.ACTION_POINTER_DOWN
import android.view.MotionEvent.ACTION_POINTER_UP
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.tinybitsinteractive.calligrafinger.render.WritingRenderer
import com.tinybitsinteractive.calligrafinger.ui.theme.CalligrafingerTheme

@Composable
fun WritingSurface(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WritingView(context).apply {
                requestFocus()
            }
        }
    )
}

internal class WritingView(context: Context) : GLSurfaceView(context) {
    private val renderer: WritingRenderer
    private val leftColor = Color.Red
    private val rightColor = Color.Blue
    private val topColor = Color.White
    private val bottomColor = Color.Black

    init {
        setEGLContextClientVersion(WritingRenderer.requiredGLVersion)
        renderer = WritingRenderer()
        setRenderer(renderer)
        // HAZARD: renderMode is a computed field
        // assigning to it requires that setRenderer has been called first.
        renderMode = RENDERMODE_WHEN_DIRTY
        // renderMode = RENDERMODE_CONTINUOUSLY
    }

    private fun texCoords(x: Float, y: Float) = Pair(x / width.toFloat(), y / height.toFloat())

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { event ->
            when (event.action) {
                ACTION_DOWN -> {
                    Log.i("CFWS", "DOWN")
                    renderer.drawColor = Color.Gray
                    this.requestRender()
                    return true
                }

                ACTION_UP -> {
                    Log.i("CFWS", "UP")
                    renderer.drawColor = Color.Green
                    this.requestRender()
                    return true
                }

                ACTION_POINTER_DOWN -> {
                    Log.i("CFWS", "PTR_DOWN")
                    return true
                }

                ACTION_POINTER_UP -> {
                    Log.i("CFWS", "PTR_UP")
                    return true
                }

                ACTION_MOVE -> {
                    val (l2r, t2b) = texCoords(event.x, event.y)
                    Log.i("CFWS", "MOVE $l2r, $t2b")
                    renderer.drawColor = blend(
                        blend(leftColor, rightColor, l2r),
                        blend(topColor, bottomColor, t2b),
                        0.5f
                    )
                    this.requestRender()
                    return true
                }

                ACTION_OUTSIDE -> {

                }

                ACTION_CANCEL -> {

                }
                else -> {
                }
            }
        }
        return super.onGenericMotionEvent(event)
    }
}

internal fun blend(a: Color, b: Color, t: Float): Color {
    val t0 = Math.max(0.0f, Math.min(1.0f, t))
    val t1 = 1.0f - t0
    return Color(
        a.red * t1 + b.red * t0,
        a.green * t1 + b.green * t0,
        a.blue * t1 + b.blue * t0,
        a.alpha * t1 + b.alpha * t0
    )
}

@Preview(showBackground = true)
@Composable
fun WritingSurfacePreview() {
    CalligrafingerTheme {
        WritingSurface()
    }
}
