package credibilitygraphs

import atb.deceptionmodel.Complementary
import atb.deceptionmodel.Truthful
import atb.infrastructure.*
import atb.interfaces.Metric
import atb.interfaces.Scenario
import atb.interfaces.TrustModel
import atb.metric.KendallsTauA
import atb.scenario.Random
import atb.trustmodel.*
import atb.trustmodel.qad.QTM
import credibilitygraphs.model.Loose
import credibilitygraphs.model.Orders
import credibilitygraphs.model.Strict
import java.io.File
import java.util.concurrent.CountDownLatch

class Parameters(val duration: Int, val fromSeed: Int, val toSeed: Int,
                 val model: TrustModel<*>, val modelParams: Array<Any>,
                 val scenario: Scenario, val scenarioParams: Array<Any>,
                 val metrics: Map<Metric, Array<Any>>,
                 val resultDir: String)

class Setup(val duration: Int, val fromSeed: Int, val toSeed: Int, val arguments: Map<String, Array<Any>>,
            val metrics: Map<Metric, Array<Any>>)

fun main() {
    val prefix = "results/"

    val models = mapOf(
            /*AbdulRahmanHailes() to emptyArray(),
            BetaReputation() to arrayOf<Any>(0.1, 0.0),
            // BRSWithFiltering() to emptyArray<Any>(),
            EigenTrust() to arrayOf(0.5, 0.5, 10, 0.1),
            Travos() to arrayOf(0.5, 10, 0.1, 0.95, 0.2),
            YuSinghSycara() to emptyArray(),
            QTM() to emptyArray()//,*/
            Orders() to emptyArray<Any>()
    )

    val fromSeed = 1
    val toSeed = 30

    val scenarios = mapOf(
            Random() to Setup(100, fromSeed, toSeed, mapOf(
                    "complementary-truth-9-1-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.9, Truthful() to 0.1), 0.0, 0.0, 0.1),
                    "complementary-truth-9-1-interactions-1.0" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.9, Truthful() to 0.1), 0.0, 0.0, 1.0),
                    "complementary-loose-5-5-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.5, Loose() to 0.5), 0.0, 0.0, 0.1),
                    "complementary-loose-5-5-interactions-1.0" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.5, Loose() to 0.5), 0.0, 0.0, 1.0),
                    "complementary-strict-9-1-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.9, Strict() to 0.1), 0.0, 0.0, 0.1),
                    "complementary-strict-9-1-interactions-1.0" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.9, Strict() to 0.1), 0.0, 0.0, 1.0),
                    "loose-strict-5-5-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Loose() to 0.5, Strict() to 0.5), 0.0, 0.0, 0.1),
                    "loose-strict-5-5-interactions-1.0" to arrayOf(100, 0.1, 0.05, mapOf(Loose() to 0.5, Strict() to 0.5), 0.0, 0.0, 1.0),
                    "loose-10-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Loose() to 1.0), 0.0, 0.0, 0.1),
                    "loose-10-interactions-1.0" to arrayOf(100, 0.1, 0.05, mapOf(Loose() to 1.0), 0.0, 0.0, 1.0),
                    "strict-10-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Strict() to 1.0), 0.0, 0.0, 0.1),
                    "strict-10-interactions-1.0" to arrayOf(100, 0.1, 0.05, mapOf(Strict() to 1.0), 0.0, 0.0, 1.0)
            ),
                    mapOf(KendallsTauA() to emptyArray())
            )
            /*Transitive() to Setup(100, fromSeed, toSeed, mapOf(
                    "tra-0.1" to arrayOf(100, 0.1, 0.05, 0.1, 1.0),
                    "tra-1.0" to arrayOf(100, 0.1, 0.05, 1.0, 1.0)),
                    mapOf(KendallsTauA() to emptyArray())
            ),
            TargetedAttack() to Setup(500, fromSeed, toSeed, mapOf(
                    "ta-l1-40" to arrayOf(100, 40, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_1),
                    "ta-l1-60" to arrayOf(100, 60, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_1),
                    "ta-l2-40" to arrayOf(100, 40, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_2),
                    "ta-l2-60" to arrayOf(100, 60, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_2),
                    "ta-l3-40" to arrayOf(100, 40, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_3),
                    "ta-l3-60" to arrayOf(100, 60, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_3)),
                    mapOf(KTAOfTargetedAgents() to emptyArray()))*/
    )

    val configurations = mutableListOf<Parameters>()

    for ((scenario, setup) in scenarios) {
        for ((filename, scenarioParams) in setup.arguments) {
            for ((model, modelParams) in models) {
                configurations.add(Parameters(setup.duration, setup.fromSeed, setup.toSeed, model, modelParams, scenario, scenarioParams,
                        setup.metrics, prefix + filename))
            }
        }
    }

    for (parameters in configurations) {
        run(parameters)
    }
}

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