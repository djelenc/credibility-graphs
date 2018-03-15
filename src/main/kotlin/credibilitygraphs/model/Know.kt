package credibilitygraphs.model

import atb.interfaces.*
import credibilitygraphs.core.CredibilityGraph
import credibilitygraphs.core.CredibilityObject

const val EXP = 1337

class Know : TrustModel<Double> {
    // cumulative interaction outcomes
    private lateinit var exSum: DoubleArray

    // interaction count
    private lateinit var exCnt: IntArray

    // computed reputation
    private lateinit var rep: DoubleArray

    override fun initialize(vararg params: Any) {
        exSum = DoubleArray(0)
        exCnt = IntArray(0)
    }

    private val experiences = LinkedHashMap<Int, Double>()
    private var kb = CredibilityGraph()

    override fun processExperiences(new: List<Experience>) {
        for (e in new) {
            exSum[e.agent] += e.outcome
            exCnt[e.agent] += 1
            experiences[e.agent] = exSum[e.agent] / exCnt[e.agent]
        }

        // 1) sort agents by experiences
        val sortedExperiences = experiences.asSequence()
                .map { it.key to it.value }
                .sortedBy { (_, value) -> value }

        // 2) create a KB from experiences
        val (mostCredible, experienceKB) = sortedExperiences.fold(Pair(-1, CredibilityGraph()), { acc, pair ->
            val (prev, currentGraph) = acc
            val (current, _) = pair

            if (prev != -1) {
                val co = CredibilityObject(prev.toString(), current.toString(), EXP.toString())
                currentGraph.expansion(co)
            }

            Pair(current, currentGraph)
        })

        experienceKB.expansion(CredibilityObject(mostCredible.toString(), EXP.toString(), EXP.toString()))
        kb = experienceKB
    }

    override fun calculateTrust() {

    }

    override fun processOpinions(opinions: List<Opinion>) {
        println("EXP ONLY: ${kb.graph.edgeSet()}")

        for (agent in agents) {
            // all opinions where given agent is the source,
            // sorted from the least to the most trustworthy target
            val fromAgent = opinions.asSequence()
                    .filter { it.agent1 == agent && it.agent1 != it.agent2 }
                    .map { it.agent2 to it.internalTrustDegree }
                    .sortedBy { (_, value) -> value }

            // convert the list of opinions to an actual credibility base
            val (_, opinionKB) = fromAgent.fold(Pair(-1, CredibilityGraph()), { acc, pair ->
                val (prev, currentGraph) = acc
                val (current, _) = pair

                if (prev != -1) {
                    val co = CredibilityObject(prev.toString(), current.toString(), agent.toString())
                    currentGraph.expansion(co)
                }

                Pair(current, currentGraph)
            })

            kb.merge(opinionKB)
        }

        println("MERGED: ${kb.graph.edgeSet()}")

        // println("OPS: ${opinionKB.graph.edgeSet()}")
        // kb.merge(opinionKB)
        //println(opinions.filter { it.agent1 == agent && it.agent1 != it.agent2 }.map { it.agent2 to it.internalTrustDegree })
        //println(kbFromAgent.graph)
    }

    override fun getTrust(service: Int): Map<Int, Double> {
        val trust = LinkedHashMap<Int, Double>()

        for (agent in exCnt.indices) {
            val expWeight = Math.min(exCnt[agent], 3) / 3.0

            // aggregate data
            if (expWeight > 0) {
                trust[agent] = exSum[agent] / exCnt[agent]
            }
        }

        return trust
    }

    private var agents: List<Int> = emptyList()

    override fun setAgents(agents: List<Int>) {
        this.agents = agents

        val max = agents.max() ?: exSum.size-1

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
