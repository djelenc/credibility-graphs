package credibilitygraphs.runs

import atb.deceptionmodel.Complementary
import atb.deceptionmodel.RandomDeception
import atb.deceptionmodel.Truthful
import atb.metric.KendallsTauA
import atb.scenario.Random
import atb.scenario.RandomWithNewcomers
import atb.scenario.Transitive
import atb.scenario.TransitiveWithChanges
import atb.trustmodel.*
import atb.trustmodel.qad.QTM
import credibilitygraphs.ProgrammaticRun.Parameters
import credibilitygraphs.ProgrammaticRun.Setup
import credibilitygraphs.ProgrammaticRun.run
import credibilitygraphs.model.Loose
import credibilitygraphs.model.Orders
import credibilitygraphs.model.Strict

fun main() {
    val prefix = "evaluations/"

    val models = mapOf(
            AbdulRahmanHailes() to emptyArray(),
            BetaReputation() to arrayOf<Any>(0.1, 0.0),
            BRSWithFiltering() to arrayOf<Any>(0.1, 0.0, 0.01, 10.0),
            EigenTrust() to arrayOf(0.5, 0.5, 10, 0.1),
            Travos() to arrayOf(0.5, 10, 0.1, 0.95, 0.2),
            YuSinghSycara() to emptyArray(),
            QTM() to emptyArray(),
            Orders() to emptyArray()
    )

    val fromSeed = 1
    val toSeed = 15

    val scenarios = mapOf(
            // simple scenarios
            Random() to Setup(100, fromSeed, toSeed, mapOf(
                    // random truth
                    "random-truth-1-9-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(RandomDeception() to 0.1, Truthful() to 0.9), 0.0, 0.0, 0.1),
                    "random-truth-9-1-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(RandomDeception() to 0.9, Truthful() to 0.1), 0.0, 0.0, 0.1),
                    "random-truth-5-5-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(RandomDeception() to 0.5, Truthful() to 0.5), 0.0, 0.0, 0.1),
                    // random strict
                    "random-strict-1-9-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(RandomDeception() to 0.1, Strict() to 0.9), 0.0, 0.0, 0.1),
                    "random-strict-9-1-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(RandomDeception() to 0.9, Strict() to 0.1), 0.0, 0.0, 0.1),
                    "random-strict-5-5-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(RandomDeception() to 0.5, Strict() to 0.5), 0.0, 0.0, 0.1),
                    // random loose
                    "random-loose-1-9-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(RandomDeception() to 0.1, Loose() to 0.9), 0.0, 0.0, 0.1),
                    "random-loose-5-5-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(RandomDeception() to 0.5, Loose() to 0.5), 0.0, 0.0, 0.1),
                    "random-loose-9-1-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(RandomDeception() to 0.9, Loose() to 0.1), 0.0, 0.0, 0.1),
                    // complementary truth
                    "complementary-truth-1-9-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.1, Truthful() to 0.9), 0.0, 0.0, 0.1),
                    "complementary-truth-9-1-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.9, Truthful() to 0.1), 0.0, 0.0, 0.1),
                    "complementary-truth-5-5-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.5, Truthful() to 0.5), 0.0, 0.0, 0.1),
                    // complementary strict
                    "complementary-strict-1-9-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.1, Strict() to 0.9), 0.0, 0.0, 0.1),
                    "complementary-strict-9-1-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.9, Strict() to 0.1), 0.0, 0.0, 0.1),
                    "complementary-strict-5-5-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.5, Strict() to 0.5), 0.0, 0.0, 0.1),
                    // complementary loose
                    "complementary-loose-1-9-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.1, Loose() to 0.9), 0.0, 0.0, 0.1),
                    "complementary-loose-9-1-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.9, Loose() to 0.1), 0.0, 0.0, 0.1),
                    "complementary-loose-5-5-interactions-0.1" to arrayOf(100, 0.1, 0.05, mapOf(Complementary() to 0.5, Loose() to 0.5), 0.0, 0.0, 0.1)
            ), mapOf(KendallsTauA() to emptyArray())),
            // transitive
            Transitive() to Setup(100, fromSeed, toSeed, mapOf(
                    "tra-0.1" to arrayOf(100, 0.1, 0.05, 0.1, 1.0)),
                    mapOf(KendallsTauA() to emptyArray())
            ),
            // transitive with changes
            // TODO make sure BRS and BRSF use correct discounting values
            TransitiveWithChanges() to Setup(1000, fromSeed, toSeed, mapOf(
                    "dynamic-0.1" to arrayOf(100, 0.1, 0.05, 0.1, 1.0, 0.01, 1)),
                    mapOf(KendallsTauA() to emptyArray())
            ),
            // newcomers
            RandomWithNewcomers() to Setup(200, fromSeed, toSeed, mapOf(
                    "nc" to arrayOf(10, 0.1, 0.05, mapOf(Truthful() to 0.5, Complementary() to 0.5), 0.0, 0.0, 0.5, 10, 5)
            ), mapOf(KendallsTauA() to emptyArray()))
    )

    val configurations = mutableListOf<Parameters>()

    for ((scenario, setup) in scenarios) {
        for ((filename, scenarioParams) in setup.arguments) {
            for ((model, modelParams) in models) {
                configurations.add(Parameters(setup.duration, setup.fromSeed, setup.toSeed, model,
                        modelParams, scenario, scenarioParams, setup.metrics, prefix + filename))
            }
        }
    }

    for (parameters in configurations) {
        run(parameters)
    }
}