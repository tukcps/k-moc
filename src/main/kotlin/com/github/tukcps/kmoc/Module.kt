package com.github.tukcps.kmoc

import kotlinx.coroutines.channels.Channel

abstract class Module(val id: String) {
    abstract suspend fun processing()
    fun initialize() {}
}

typealias SimpleChannel = Channel<Double>