package com.sahib.avocado.utils

import android.app.Activity
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class GestureDetection(val context: Activity, val listener: SimpleGestureListener) : View.OnTouchListener {

    var halfWidth: Int = 0
    var halfHeight: Int = 0

    companion object {
        val BLANK : Int = -1
        val VOLUME_UP : Int = 1
        val VOLUME_DOWN : Int = 2
        val BRIGHTNESS_UP : Int = 3
        val BRIGHTNESS_DOWN : Int = 4
        val SEEK_REWIND : Int = 5
        val SEEK_FORWARD : Int = 6
    }

    var deltaX = 0f
    var deltaY = 0f
    var maxValX = 0f
    var maxValY = 0f
    var firstTouchX = 0f
    var firstTouchY = 0f
    var currentX = 0f
    var currentY = 0f
    var currentSwipe: Int = -1
    private var SWIPE_THRESHOLD = 10.0f

    //TODO handle orientations!

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val result: Boolean
        if (halfWidth == 0|| halfHeight == 0) {
            halfWidth = v.width / 2
            halfHeight = v.height / 2
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //Register the first touch on TouchDown and this should not change unless finger goes up.
                currentSwipe = BLANK
                firstTouchX = event.x
                firstTouchY = event.y
                maxValX = 0.0f
                maxValY = 0.0f
                //As the event is consumed, return true
                result = true
            }
            MotionEvent.ACTION_MOVE -> {
                //CurrentX/Y are the continues changing values of one single touch session. Change
                //when finger slides on view
                currentX = event.x
                currentY = event.y
                //setting the maximum value of X or Y so far. Any deviation in this means a  change of direction so reset the firstTouch value to last known max value i.e MaxVal X/Y.
                if (maxValX < currentX) {
                    maxValX = currentX
                } else {
                    firstTouchX = maxValX
                    maxValX = 0.0f
                }
                if (maxValY < currentY) {
                    maxValY = currentY
                } else {
                    firstTouchY = maxValY
                    maxValY = 0.0f
                }
                //DeltaX/Y are the difference between current touch and the value when finger first touched screen.
                //If its negative that means current value is on left side of first touchdown value i.e Going left and
                //vice versa.
                deltaX = currentX - firstTouchX
                deltaY = currentY - firstTouchY
                if (abs(deltaX) > abs(deltaY)) {
                    //Horizontal swipe
                    if (abs(deltaX) > SWIPE_THRESHOLD) {
                        if (deltaX > 0) {
                            //means we are going right
                            if (currentSwipe == BLANK || currentSwipe == SEEK_FORWARD || currentSwipe == SEEK_REWIND) {
                                currentSwipe = SEEK_FORWARD
                                listener.onSwipe(SEEK_FORWARD)
                            }
                        } else {
                            //means we are going left
                            if (currentSwipe == BLANK || currentSwipe == SEEK_FORWARD || currentSwipe == SEEK_REWIND) {
                                currentSwipe = SEEK_REWIND
                                listener.onSwipe(SEEK_REWIND)
                            }
                        }
                    }
                } else {
                    //It's a vertical swipe
                    if (abs(deltaY) > SWIPE_THRESHOLD) {
                        if (deltaY > 0) {
                            //means we are going down
                            if (currentX > halfWidth) { //Right Side
                                if (currentSwipe == BLANK || currentSwipe == VOLUME_DOWN || currentSwipe == VOLUME_UP) {
                                    currentSwipe = VOLUME_DOWN
                                    listener.onSwipe(VOLUME_DOWN)
                                }
                            } else {    //Left Side
                                if (currentSwipe == BLANK || currentSwipe ==  BRIGHTNESS_DOWN || currentSwipe == BRIGHTNESS_UP) {
                                    currentSwipe = BRIGHTNESS_DOWN
                                    listener.onSwipe(BRIGHTNESS_DOWN)
                                }
                            }
                        }
                    } else {
                        //means we are going up
                            if (currentX > halfWidth) { //Right Side
                                if (currentSwipe == BLANK || currentSwipe == VOLUME_DOWN || currentSwipe == VOLUME_UP) {
                                    currentSwipe = VOLUME_UP
                                    listener.onSwipe(VOLUME_UP)
                                }
                            } else {    //Left Side
                                if (currentSwipe == BLANK || currentSwipe ==  BRIGHTNESS_DOWN || currentSwipe == BRIGHTNESS_UP) {
                                    currentSwipe = BRIGHTNESS_UP
                                    listener.onSwipe(BRIGHTNESS_UP)
                                }
                        }
                    }
                }
                result = true
            }
            MotionEvent.ACTION_UP -> {
                //Clean UP
                currentSwipe = BLANK
                firstTouchX = 0.0f
                firstTouchY = 0.0f
                result = true
            }
            else -> result = false
        }

        return result
    }

    interface SimpleGestureListener {
        fun onSwipe(direction: Int)
    }

}