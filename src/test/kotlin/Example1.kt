
import kotlinx.coroutines.channels.Channel
import com.github.tukcps.kmoc.Process
import org.junit.jupiter.api.Test

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
            println("    Sink received: $inputSample")
        }
    }

    @Test
    suspend fun simulate() {
        // The processes
        val source = Source("Source")
        val p1 = P1("P1")
        val p2 = P2( "P2")
        val sink = Sink("Sink")

        // The channels
        val sourceToP1 = Channel<Double>()
        val p1ToP2 = Channel<Double>()
        val p2ToSink = Channel<Double>()

        // Connections
        source.output = sourceToP1
        p1.input = sourceToP1
        p1.output = p1ToP2
        p2.input = p2ToSink
        p2.output = p2ToSink

        // The schedule, as computed in the 1st lecture example
        val schedule = listOf(source, p1, p2, p2, sink, sink)

        while(true) {
            schedule.forEach {
                it.processing()
            }
        }
    }
}
