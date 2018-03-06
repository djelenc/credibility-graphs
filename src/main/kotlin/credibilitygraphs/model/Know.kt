package credibilitygraphs.model

import atb.interfaces.*
import credibilitygraphs.core.CredibilityGraph
import credibilitygraphs.core.CredibilityObject


class Know : TrustModel<Double> {
    // cumulative interaction outcomes
    private lateinit var exSum: DoubleArray

    // interaction count
    private lateinit var exCnt: IntArray

    // received opinions
    private lateinit var op: Array<DoubleArray>

    // computed reputation
    private lateinit var rep: DoubleArray

    override fun initialize(vararg params: Any) {
        exSum = DoubleArray(0)
        exCnt = IntArray(0)
        op = Array(0) { DoubleArray(0) }
    }

    private val experiences = LinkedHashMap<Int, Double>()
    private val kb = CredibilityGraph()

    override fun processExperiences(new: List<Experience>) {
        for (e in new) {
            exSum[e.agent] += e.outcome
            exCnt[e.agent] += 1
            experiences[e.agent] = exSum[e.agent] / exCnt[e.agent]
        }

        // build KB with experiences
        val sorted = experiences.toList()
                .sortedBy { (_, value) -> value }
                .toMap()

        val iterator = sorted.iterator()
        var (prevAgent, _) = iterator.next()

        while (iterator.hasNext()) {
            val (currentAgent, _) = iterator.next()
            kb.expansion(CredibilityObject("$prevAgent", "$currentAgent", "EXP"))
            prevAgent = currentAgent
        }
    }

    override fun calculateTrust() {

    }

    override fun processOpinions(opinions: List<Opinion>) {
        for (o in opinions) {
            op[o.agent1][o.agent2] = o.internalTrustDegree
        }

        val opinionGraphs = ArrayList<LinkedHashMap<Int, Double>>()
        
        for (reporter in op.indices) {
            // create a graph

        }

    }

    override fun getTrust(service: Int): Map<Int, Double> {
        val trust = LinkedHashMap<Int, Double>()

        // compute reputations
        rep = DoubleArray(exSum.size)

        for (target in op.indices) {
            var sum = 0.0
            var count = 0

            for (reporter in op.indices) {
                if (!java.lang.Double.isNaN(op[reporter][target])) {
                    sum += op[reporter][target]
                    count += 1
                }
            }

            if (count > 0)
                rep[target] = sum / count
            else
                rep[target] = java.lang.Double.NaN
        }

        // combine experiences and reputation into trust
        for (agent in exCnt.indices) {
            // compute weights
            val expWeight = Math.min(exCnt[agent], 3) / 3.0
            val opWeight = if (java.lang.Double.isNaN(rep[agent])) 0.0 else 1 - expWeight

            // aggregate data
            if (expWeight > 0 || opWeight > 0) {
                trust[agent] = when {
                // experience & opinions
                    expWeight > 0 && opWeight > 0 -> expWeight * exSum[agent] / exCnt[agent] + opWeight * rep[agent]
                    opWeight > 0 -> rep[agent] // opinions only
                    else -> exSum[agent] / exCnt[agent] // only experiences
                }
            }
        }

        return trust
    }

    override fun setAgents(agents: List<Int>) {
        val max = agents.max() ?: Math.max(op.size - 1, exSum.size - 1)

        // resize opinions' array
        if (max > op.size - 1) {
            val newOp = Array(max + 1) { DoubleArray(max + 1) }

            for (i in newOp.indices)
                for (j in newOp.indices)
                    newOp[i][j] = java.lang.Double.NaN

            for (i in op.indices)
                System.arraycopy(op[i], 0, newOp[i], 0, op.size)

            op = newOp
        }

        // resize experiences' array
        if (max > exSum.size - 1) {
            val newExSum = DoubleArray(max + 1)
            System.arraycopy(exSum, 0, newExSum, 0, exSum.size)
            exSum = newExSum

            val newExCnt = IntArray(max + 1)
            System.arraycopy(exCnt, 0, newExCnt, 0, exCnt.size)
            exCnt = newExCnt
        }
    }

    override fun toString(): String = "Kotlin Trust Model"

    override fun getParametersPanel(): ParametersPanel? = null

    override fun setCurrentTime(time: Int) = Unit

    override fun setServices(services: List<Int>) = Unit

    override fun setRandomGenerator(generator: RandomGenerator) = Unit
}
