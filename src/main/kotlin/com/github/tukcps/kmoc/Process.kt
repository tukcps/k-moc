package com.github.tukcps.kmoc


abstract class Process(val id: String) {
    abstract suspend fun processing()
}