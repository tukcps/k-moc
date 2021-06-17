package com.github.tukcps.kmoc.examples

import com.github.tukcps.kmoc.Module
import com.github.tukcps.kmoc.TdfChannel
import com.github.tukcps.kmoc.simulatedTime
import kotlinx.coroutines.delay

class FeedbackControl {

    class UnitStep(id: String): Module(id) {
        lateinit var output: TdfChannel<Double>
        override suspend fun processing() {
            output.write(1.0)
        }
    }

    class Plant(id: String): Module(id) {
        lateinit var controlledInput: TdfChannel<Double>
        lateinit var response: TdfChannel<Double>

        override suspend fun processing() {
            val distortion = 1.5
            response.write(controlledInput.read() + distortion)
        }
    }


    class Controller(id: String): Module(id) {
        lateinit var setValue: TdfChannel<Double>
        lateinit var isValue:  TdfChannel<Double>
        lateinit var output: TdfChannel<Double>

        var integral = 0.0

        override suspend fun processing() {
            val setSample = setValue.read()
            val isSample = isValue.read()
            val error = setSample - isSample

            integral += error
            output.write(integral * 0.35)
        }
    }
}

suspend fun main() {
    // The processes
    val unitStep = FeedbackControl.UnitStep("Unit step")
    val controller = FeedbackControl.Controller("Controller")
    val plant = FeedbackControl.Plant("Plant")

    // The TdfChannels
    val reference = TdfChannel<Double>("reference",1)
    val processInput = TdfChannel<Double>("processInput", 1)
    val processResponse = TdfChannel<Double>("processResponse", 1, true)

    // Connect processes with TdfChannels
    unitStep.output=reference
    controller.setValue=reference
    controller.isValue=processResponse
    controller.output=processInput
    plant.controlledInput=processInput
    plant.response=processResponse

    // Initial value in feedback loop
    processResponse.write(0.0)

    val schedule = listOf(unitStep, controller, plant)

    while(true) {
        delay(1000)
        simulatedTime += 100
        println("\nExecuting schedule of cluster ... ")
        schedule.forEach {
            println("   - ${it.id}")
            it.processing()
        }
    }
}

