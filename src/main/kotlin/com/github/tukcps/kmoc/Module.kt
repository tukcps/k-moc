package com.github.tukcps.kmoc


abstract class Module(val id: String) {
    abstract suspend fun processing()
}