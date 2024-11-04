package com.tinybitsinteractive.calligrafinger.util

import android.util.Log

typealias LoggerFactory = (tag: String) -> Logger

interface Logger {
    companion object {
        val appTag = "CGFinger"
        var create: LoggerFactory = DefaultLoggerFactory
        val shared: Logger by lazy { create(appTag) }
    }
    fun info(msg: String)
    fun warn(msg: String)
    fun err(msg: String)
    fun metric(msg: String)
}

val DefaultLoggerFactory =  { tag: String -> DefaultLogger(tag) }
val PrintLoggerFactory =  { tag: String -> PrintLogger(tag) }

class DefaultLogger(private val tag: String) : Logger {
    override fun info(msg: String) { Log.i(tag, msg) }
    override fun warn(msg: String) { Log.w(tag, msg) }
    override fun err(msg: String) { Log.e(tag, msg) }
    override fun metric(msg: String) { Log.i(tag, "metric: $msg") }
}

class PrintLogger(private val tag: String) : Logger {
    override fun info(msg: String) { println("info $tag: $msg") }
    override fun warn(msg: String) { println("warn $tag: $msg") }
    override fun err(msg: String) { println("err $tag: $msg") }
    override fun metric(msg: String) { println("metric $tag: $msg") }
}