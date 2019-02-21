package credibilitygraphs

import atb.infrastructure.*
import atb.interfaces.Metric
import atb.interfaces.Scenario
import atb.interfaces.TrustModel
import atb.metric.KTAOfTargetedAgents
import atb.scenario.TargetedAttack
import atb.scenario.TargetedAttackStrategy
import atb.trustmodel.*
import atb.trustmodel.qad.QTM
import credibilitygraphs.model.Orders
import java.io.File
import java.util.concurrent.CountDownLatch

class Parameters(val duration: Int, val fromSeed: Int, val toSeed: Int,
                 val model: TrustModel<*>, val modelParams: Array<Any>,
                 val scenario: Scenario, val scenarioParams: Array<Any>,
                 val metrics: Map<Metric, Array<Any>>,
                 val resultDir: String)


fun main() {
    val models = mapOf(
            AbdulRahmanHailes() to emptyArray(),
            BetaReputation() to arrayOf<Any>(0.1, 0.0),
            EigenTrust() to arrayOf(0.5, 0.5, 10, 0.1),
            Travos() to arrayOf(0.5, 10, 0.1, 0.95, 0.2),
            YuSinghSycara() to emptyArray(),
            QTM() to emptyArray(),
            // BRSWithFiltering() to emptyArray(),
            Orders() to emptyArray()
    )

    val scenarios = mapOf(
            TargetedAttack() to arrayOf(100, 40, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_1),
            TargetedAttack() to arrayOf(100, 60, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_1),
            TargetedAttack() to arrayOf(100, 40, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_2),
            TargetedAttack() to arrayOf(100, 60, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_2),
            TargetedAttack() to arrayOf(100, 40, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_3),
            TargetedAttack() to arrayOf(100, 60, 20, 20, 0.1, 0.05, TargetedAttackStrategy.LEVEL_3)
    )

    val configurations = mutableListOf<Parameters>()

    for ((scenario, scenarioParams) in scenarios) {
        val dirname = "results/ta-${scenarioParams[6]}-${scenarioParams[1]}"
        for ((model, modelParams) in models) {
            configurations.add(Parameters(500, 1, 30, model, modelParams, scenario, scenarioParams, mapOf(KTAOfTargetedAgents() to emptyArray()), dirname))
        }
    }

    for (config in configurations) {
        run(config)
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