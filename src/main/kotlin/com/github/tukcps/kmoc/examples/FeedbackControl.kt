package com.github.tukcps.kmoc.examples

import com.github.tukcps.kmoc.Process
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay

class FeedbackControl {

    class UnitStep(id: String): Process(id) {
        lateinit var output: Channel<Double>
        override suspend fun processing() {
            output.send(1.0)
        }
    }

    class Plant(id: String): Process(id) {
        lateinit var controlledInput: Channel<Double>
        lateinit var response: Channel<Double>

        override suspend fun processing() {
            val distortion = 1.5
            response.send(controlledInput.receive() + distortion)
        }
    }


    class Controller(id: String): Process(id) {
        lateinit var setValue: Channel<Double>
        lateinit var isValue:  Channel<Double>
        lateinit var output: Channel<Double>

        var integral = 0.0

        override suspend fun processing() {
            val setSample = setValue.receive()
            val isSample = isValue.receive()
            val error = setSample - isSample
            println("      setValue is: $setSample, isValue is: $isSample")

            integral += error
            output.send(integral * 0.25)
        }
    }
}

suspend fun main() {
    // The processes
    val unitStep = FeedbackControl.UnitStep("Unit step")
    val controller = FeedbackControl.Controller("Controller")
    val plant = FeedbackControl.Plant("Plant")

    // The channels
    val reference = Channel<Double>(1)
    val processInput = Channel<Double>(1)
    val processResponse = Channel<Double>(1)

    // Connect processes with channels
    unitStep.output=reference
    controller.setValue=reference
    controller.isValue=processResponse
    controller.output=processInput
    plant.controlledInput=processInput
    plant.response=processResponse

    // Initial value in feedback loop
    processResponse.send(0.0)

    val schedule = listOf(unitStep, controller, plant)

    while(true) {
        delay(1000)
        println("\nExecuting schedule of cluster ... ")
        schedule.forEach {
            println("   - ${it.id}")
            it.processing()
        }
    }
}

