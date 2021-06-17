package com.github.tukcps.kmoc

/**
 * For modeling time, we use a long integer.
 * Double arithmetic is not appropriate, because progress cannot be guaranteed
 * due to round-off errors in FP - arithmetic (large number + small number = large number).
 */

var simulatedTime: Long = 0
var timeUnit: String = "ms"