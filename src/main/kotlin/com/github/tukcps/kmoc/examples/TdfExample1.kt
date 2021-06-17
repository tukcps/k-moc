package com.github.tukcps.kmoc.examples

import com.github.tukcps.kmoc.Module
import com.github.tukcps.kmoc.TdfChannel
import com.github.tukcps.kmoc.simulatedTime
import kotlinx.coroutines.delay

class TdfExample1 {

    class Source(id: String): Module(id) {
        lateinit var output: TdfChannel<Double>
        var last = 0.0
        override suspend fun processing() {
            output.write(++last )
        }
    }

    class P1(id: String): Module(id) {
        lateinit var input: TdfChannel<Double>
        lateinit var output: TdfChannel<Double>

        override suspend fun processing() {
            val input = input.read()

            output.write(input)
            output.write(input+1.0)
            output.write(input+2.0)
            output.write(input+3.0)
        }
    }

    class P2(id: String): Module(id) {
        lateinit var input: TdfChannel<Double>
        lateinit var output: TdfChannel<Double>

        override suspend fun processing() {
            val inputSample1 = input.read()
            val inputSample2 = input.read()

            val result = inputSample1+inputSample2

            output.write(result)
        }
    }

    class Sink(id: String): Module(id) {
        lateinit var input: TdfChannel<Double>

        override suspend fun processing() {
            val inputSample = input.read()
        }
    }
}

suspend fun main() {
    // The processes
    val source = TdfExample1.Source("Source")
    val p1 = TdfExample1.P1("P1")
    val p2 = TdfExample1.P2("P2")
    val sink = TdfExample1.Sink("Sink")

    // The channels
    val sourceToP1 = TdfChannel<Double>("source output", 1, true)
    val p1ToP2     = TdfChannel<Double>("p1 output", 4, true)
    val p2ToSink   = TdfChannel<Double>("p2 output", 2, true)

    // Connections
    source.output = sourceToP1
    p1.input = sourceToP1
    p1.output = p1ToP2
    p2.input = p1ToP2
    p2.output = p2ToSink
    sink.input = p2ToSink

    // The schedule, as computed in the 1st lecture example
    val schedule = listOf(source, p1, p2, p2, sink, sink)
    val clusterTimeStep = 4

    while(true) {
        println("\nExecuting schedule of cluster at time $simulatedTime ")
        schedule.forEach {
            delay(1000)
            println("   - ${it.id}")
            it.processing()
        }
        simulatedTime += clusterTimeStep
    }
}