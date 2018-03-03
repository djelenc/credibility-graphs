package credibilitygraphs.model

import testbed.interfaces.*
import java.util.*


class Know : TrustModel<Double> {
    override fun getParametersPanel(): ParametersPanel? = null

    override fun setRandomGenerator(generator: RandomGenerator?) {}

    // cumulative interaction outcomes
    protected lateinit var exSum: DoubleArray

    // interaction count
    protected lateinit var exCnt: IntArray

    // received opinions
    protected lateinit var op: Array<DoubleArray>

    // computed reputation
    protected lateinit var rep: DoubleArray

    override fun initialize(vararg params: Any) {
        exSum = DoubleArray(0)
        exCnt = IntArray(0)
        op = Array(0) { DoubleArray(0) }
    }

    override fun processExperiences(experiences: List<Experience>) {
        for (e in experiences) {
            exSum[e.agent] += e.outcome
            exCnt[e.agent] += 1
        }
    }

    override fun processOpinions(opinions: List<Opinion>) {
        for (o in opinions) {
            op[o.agent1][o.agent2] = o.internalTrustDegree
        }
    }

    override fun calculateTrust() {
        // pass
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
            var w_e = 0.0
            var w_r = 0.0
            var t = 0.0

            // compute weights
            w_e = Math.min(exCnt[agent], 3) / 3.0
            w_r = if (java.lang.Double.isNaN(rep[agent])) 0.0 else 1 - w_e

            // aggregate data
            if (w_e > 0 || w_r > 0) {
                if (w_e > 0 && w_r > 0) { // experience & opinions
                    t = w_e * exSum[agent] / exCnt[agent] + w_r * rep[agent]
                } else if (w_r > 0) { // opinions only
                    t = rep[agent]
                } else { // only experiences
                    t = exSum[agent] / exCnt[agent]
                }

                trust[agent] = t
            }
        }

        return trust
    }

    override fun setCurrentTime(time: Int) {

    }

    override fun setAgents(agents: List<Int>) {
        // current size of opinions' data structure
        var max = Math.max(op.size - 1, exSum.size - 1)

        // find the maximum ID
        for (agent in agents)
            if (agent > max)
                max = agent

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

    override fun setServices(services: List<Int>) {}

    override fun toString(): String = "Kotlin Trust Model"

}
