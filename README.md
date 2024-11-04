Calligrafinger
==============
An Android OpenGL ES 3.2 via Kotlin Experiment

Usage
=====
* CAVEAT: Unlikely to work on Android emulator.
    * In general the emulator tends to fail for graphical applications using Vulkan or OpenGL ES beyond 2.0
    * Developed and tested using a One Plus 8 on Android 13. YMMV.
* Scribble with your finger.
* Change colors with Blk, Blu, Red
* Clear the page with Ers

![calligraphinger_small](https://github.com/user-attachments/assets/db8dabe4-53d5-4d23-b720-a8d5b6c63b59)

Purpose
=======
This is an experiment intended to test the basic performance and behavior of Kotlin + OpenGL ES 3.2
applied to the task of hand drawn vector graphics.

Status
======
Initial implementation working

Next Steps
==========
* Collect performance stats
    * Latency & FPS primarily
* Anticipating that a first implementation is not going to be optimal
    * Track bottlenecks in the JVM side point accummulation -> GPU vector buffer
    * Do something smart for eliminating unnecessary points
 * Play with some drawing beautification options
    * Dynamic chisel point angle
    * Outline for traces
    * Textured paper surface
    * Textured pen/pencil trace
