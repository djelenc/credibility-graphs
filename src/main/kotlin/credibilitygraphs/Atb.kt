package credibilitygraphs

import atb.app.gui.main
import atb.infrastructure.*
import atb.interfaces.Metric
import atb.interfaces.Scenario
import atb.interfaces.TrustModel
import java.io.File
import java.util.concurrent.CountDownLatch

/**
 * Runs the ATB GUI
 */
fun main(args: Array<String>) = main(args)


object ProgrammaticRun {
    /**
     * Parameters of a single programmatic run
     */
    class Parameters(val duration: Int, val fromSeed: Int, val toSeed: Int,
                     val model: TrustModel<*>, val modelParams: Array<Any>,
                     val scenario: Scenario, val scenarioParams: Array<Any>,
                     val metrics: Map<Metric, Array<Any>>,
                     val resultDir: String)

    /**
     * A scenario evaluation setup
     */
    class Setup(val duration: Int, val fromSeed: Int, val toSeed: Int, val arguments: Map<String, Array<Any>>,
                val metrics: Map<Metric, Array<Any>>)

    /*
    Creates a programmatic run using given parameters. To make the runs thread safe,
    the instances of trust model, scenario and metrics are copied
     */
    fun run(params: Parameters) {

        val tasks = (params.fromSeed..params.toSeed).map { seed ->

            val copiedMetrics = params.metrics.map {
                Pair(it.key.javaClass.newInstance(), it.value)
            }.toMap()

            // protocol
            val protocol = createProtocol(params.model::class.java.newInstance(), params.modelParams,
                    params.scenario::class.java.newInstance(), params.scenarioParams, copiedMetrics, seed)

            setupEvaluation(protocol, params.duration, copiedMetrics.keys)
        }

        val latch = CountDownLatch(1)

        runBatch(tasks, { results ->
            when {
                results.all { it is Completed } -> {
                    val directory = File(params.resultDir)
                    if (!directory.exists()) {
                        directory.mkdirs()
                    }

                    BatchEvaluationData(results.map { (it as Completed).data }).toJSON(directory.path)
                    println("Saved to $directory")
                }
                else -> println("The run encountered errors")
            }


            latch.countDown()
        }, {
            when (it) {
                is Completed -> println("Completed ${it.data.protocol.scenario} (seed ${it.data.seed}, ${it.data.protocol.trustModel})")
                is Interrupted -> println("Interrupted at ${it.tick}")
                is Faulted -> println("An exception (${it.thrown}) occurred at ${it.tick}")
                else -> println("Something else went wrong ...")
            }
        })

        latch.await()
    }
}