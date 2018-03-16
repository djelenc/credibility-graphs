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

    override fun initialize(vararg params: Any) {
        exSum = DoubleArray(0)
        exCnt = IntArray(0)
    }

    private val experiences = LinkedHashMap<Int, Double>()

    private var opinions = emptyList<Opinion>()

    private var kb = CredibilityGraph()

    override fun processExperiences(new: List<Experience>) {
        for (e in new) {
            exSum[e.agent] += e.outcome
            exCnt[e.agent] += 1
            experiences[e.agent] = exSum[e.agent] / exCnt[e.agent]
        }
    }

    override fun calculateTrust() {
        // 1) sort agents by average experience outcomes
        val sortedExperiences = experiences.asSequence()
                .map { it.key to it.value }
                .sortedBy { (_, value) -> value }

        // 2) create a KB from experiences
        val (_, experienceKB) = sortedExperiences.fold(
                Pair(-1, CredibilityGraph()), { (prev, currentGraph), (current, _) ->
            if (prev != -1) {
                val co = CredibilityObject(prev.toString(), current.toString(), EXP.toString())
                currentGraph.expansion(co)
            }
            Pair(current, currentGraph)
        })

        kb = experienceKB

        for (agent in agents) {
            // experiences are more a reliable source than this agent
            kb.expansion(CredibilityObject(agent.toString(), EXP.toString(), EXP.toString()))

            // all opinions from given agent, sorted by internal trust degrees
            val relevant = opinions.asSequence()
                    .filter { it.agent1 == agent && it.agent1 != it.agent2 }
                    .sortedBy { it.internalTrustDegree }

            // create a KB from the list of opinions
            val first = relevant.first()
            val (_, opinionKB) = relevant.drop(1).fold(Pair(first, CredibilityGraph()),
                    { (previous, knowledgeBase), current ->
                        val co = CredibilityObject(previous.agent2.toString(),
                                current.agent2.toString(),
                                agent.toString())
                        knowledgeBase.expansion(co)

                        Pair(current, knowledgeBase)
                    })
            // merge current KB with the KB from this agent
            kb.merge(opinionKB)
        }
    }

    override fun processOpinions(new: List<Opinion>) {
        opinions = new
    }

    override fun getTrust(service: Int): Map<Int, Double> {
        println("BEFORE: ${kb.graph.edgeSet()}")

        // remove EXP vertex
        kb.graph.removeVertex(EXP.toString())

        println("AFTER: ${kb.graph.edgeSet()}")

        return mapOf()
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
