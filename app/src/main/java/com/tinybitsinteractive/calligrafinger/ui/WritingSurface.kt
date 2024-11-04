package com.tinybitsinteractive.calligrafinger.ui

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_OUTSIDE
import android.view.MotionEvent.ACTION_POINTER_DOWN
import android.view.MotionEvent.ACTION_POINTER_UP
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.tinybitsinteractive.calligrafinger.render.WritingRenderer
import com.tinybitsinteractive.calligrafinger.render.toVec2
import com.tinybitsinteractive.calligrafinger.render.toVec4
import com.tinybitsinteractive.calligrafinger.ui.theme.CalligrafingerTheme
import com.tinybitsinteractive.calligrafinger.ui.theme.Biro
import com.tinybitsinteractive.calligrafinger.ui.theme.Charcoal
import com.tinybitsinteractive.calligrafinger.ui.theme.Cream
import com.tinybitsinteractive.calligrafinger.ui.theme.Grader
import com.tinybitsinteractive.calligrafinger.util.Logger

enum class WritingCommand {
    BlackInk, BlueInk, RedInk, ClearPage
}

@Composable
fun WritingSurface(writingCommand: WritingCommand, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WritingView(context).apply {
                requestFocus()
            }
        },
        update = {
            view ->
            view.applyCommand(writingCommand)
        }
    )
}

internal class WritingView(context: Context) : GLSurfaceView(context) {
    private val renderer: WritingRenderer
    private val logger by lazy { Logger.create("WritingView") }

    init {
        setEGLContextClientVersion(WritingRenderer.kRequiredGLVersion)
        renderer = WritingRenderer()
        renderer.backgroundColor = Cream.toVec4()
        renderer.strokeColor = Charcoal.toVec4()
        setRenderer(renderer)
        // HAZARD: renderMode is a computed field
        //         assigning to it requires that setRenderer has been called first
        //         or you'll get an NRE
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun applyCommand(option: WritingCommand) {
        when (option) {
            WritingCommand.BlackInk -> {
                renderer.strokeColor = Charcoal.toVec4()
            }
            WritingCommand.BlueInk -> {
                renderer.strokeColor = Biro.toVec4()
            }
            WritingCommand.RedInk -> {
                renderer.strokeColor = Grader.toVec4()
            }
            WritingCommand.ClearPage -> {
                renderer.clear()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { event ->
            when (event.actionMasked) {
                ACTION_DOWN -> {
                    renderer.beginStroke(event.toVec2())
                }

                ACTION_UP -> {
                    renderer.endStroke(event.toVec2())
                }

                ACTION_POINTER_DOWN -> {
                    //logger.info("PTR_DOWN")
                }

                ACTION_POINTER_UP -> {
                    //logger.info("PTR_UP")
                }
                ACTION_MOVE -> {
                    renderer.extendStroke(event.toVec2())
                }

                ACTION_OUTSIDE -> {

                }

                ACTION_CANCEL -> {

                }
                else -> {
                    return super.onGenericMotionEvent(event)
                }
            }

            this.requestRender()
            return true
        }
        return super.onGenericMotionEvent(event)
    }
}

@Preview(showBackground = true)
@Composable
fun WritingSurfacePreview() {
    CalligrafingerTheme {
        WritingSurface(WritingCommand.BlackInk)
    }
}
