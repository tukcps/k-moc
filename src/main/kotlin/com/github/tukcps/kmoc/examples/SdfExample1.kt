package com.github.tukcps.kmoc.examples

import com.github.tukcps.kmoc.Module
import com.github.tukcps.kmoc.SdfChannel
import kotlinx.coroutines.delay

class SdfExample1 {

    /** As source we use a source that produces just a sequence of increasing numbers, starting with 1.0 */
    class Source(id: String): Module(id) {
        lateinit var output: SdfChannel<Double>
        var last = 0.0
        override suspend fun processing() {
            output.write(++last )
        }
    }

    /** P1 just clones the values */
    class P1(id: String): Module(id) {
        lateinit var input: SdfChannel<Double>
        lateinit var output: SdfChannel<Double>

        override suspend fun processing() {
            val input = input.read()

            output.write(input)
            output.write(input+1.0)
            output.write(input+2.0)
            output.write(input+3.0)
        }
    }

    /** P2 is a software process that adds two inputs */
    class P2(id: String): Module(id) {
        lateinit var input: SdfChannel<Double>
        lateinit var output: SdfChannel<Double>

        override suspend fun processing() {
            val inputSample1 = input.read()
            val inputSample2 = input.read()

            val result = inputSample1+inputSample2

            output.write(result)
        }
    }

    class Sink(id: String): Module(id) {
        lateinit var input: SdfChannel<Double>

        override suspend fun processing() {
            val inputSample = input.read()
        }
    }
}

suspend fun main() {
    // The processes
    val source = SdfExample1.Source("Source")
    val p1 = SdfExample1.P1("P1")
    val p2 = SdfExample1.P2("P2")
    val sink = SdfExample1.Sink("Sink")

    // The channels
    val sourceToP1 = SdfChannel<Double>("source output", 1, true)
    val p1ToP2     = SdfChannel<Double>("p1 output", 4, true)
    val p2ToSink   = SdfChannel<Double>("p2 output", 2, true)

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
            delay(100)
            println("   - ${it.id}")
            it.processing()
        }
    }
}