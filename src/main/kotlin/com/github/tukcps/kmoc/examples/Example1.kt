package com.github.tukcps.kmoc.examples

import kotlinx.coroutines.channels.Channel
import com.github.tukcps.kmoc.Process

class Example1 {

    class Source(id: String): Process(id) {
        lateinit var output: Channel<Double>
        override suspend fun processing() {
            output.send(1.0)
        }
    }

    class P1(id: String): Process(id) {
        lateinit var input: Channel<Double>
        lateinit var output: Channel<Double>

        override suspend fun processing() {
            val input = input.receive()

            output.send(input)
            output.send(input)
            output.send(input)
            output.send(input)
        }
    }

    class P2(id: String): Process(id) {
        lateinit var input: Channel<Double>
        lateinit var output: Channel<Double>

        override suspend fun processing() {
            val inputSample1 = input.receive()
            val inputSample2 = input.receive()

            val result = inputSample1+inputSample2

            output.send(result)
        }
    }

    class Sink(id: String): Process(id) {
        lateinit var input: Channel<Double>

        override suspend fun processing() {
            val inputSample = input.receive()
            println("     - Sink received: $inputSample")
        }
    }
}

suspend fun main() {
    // The processes
    val source = Example1.Source("Source")
    val p1 = Example1.P1("P1")
    val p2 = Example1.P2("P2")
    val sink = Example1.Sink("Sink")

    // The channels
    val sourceToP1 = Channel<Double>(1)
    val p1ToP2 = Channel<Double>(4)
    val p2ToSink = Channel<Double>(2)

    // Connections
    source.output = sourceToP1
    p1.input = sourceToP1
    p1.output = p1ToP2
    p2.input = p1ToP2
    p2.output = p2ToSink
    sink.input = p2ToSink

    // The schedule, as computed in the 1st lecture example
    val schedule = listOf(source, p1, p2, p2, sink, sink)

    while(true) {
        println("\nExecuting schedule of cluster ... ")
        schedule.forEach {
            println("   - ${it.id}")
            it.processing()
        }
    }
}