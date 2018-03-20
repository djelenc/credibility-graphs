package credibilitygraphs.model

import atb.interfaces.*
import credibilitygraphs.core.CredibilityObject
import credibilitygraphs.core.KnowledgeBase

const val EXP = "exp"

class CredibilityOrdersTM : TrustModel<PartialOrder> {
    // cumulative interaction outcomes
    private var exSum = DoubleArray(0)
    // interaction count
    private var exCnt = IntArray(0)

    // current time
    private var time = 0

    private val experiences = LinkedHashMap<Int, Double>()

    private var opinions = emptyList<Opinion>()

    private var kb = KnowledgeBase()

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
        val worstExperience = sortedExperiences.first()
        val (_, experienceKB) = sortedExperiences.drop(1).fold(Pair(worstExperience.first, KnowledgeBase()),
                { (prev, currentGraph), (current, _) ->
                    val co = CredibilityObject(prev.toString(), current.toString(), EXP)
                    currentGraph.expansion(co)

                    Pair(current, currentGraph)
                })

        kb = experienceKB

        // debugging
        /* val experiencePrint = kb.copy()
        experiencePrint.graph.removeVertex(EXP)
        experiencePrint.exportDOT("./tick-${time}-Experiences", Format.PNG) */

        // 3) Merge own (experience-based) KB with KBs obtain from other agents
        // TODO: This operation depends on the order in which the KBs are merged
        for (agent in agents) {
            // experiences are more a reliable source than this agent
            kb.expansion(CredibilityObject(agent.toString(), EXP, EXP))

            // all opinions from given agent, sorted by internal trust degrees
            val relevant = opinions.asSequence()
                    .filter { it.agent1 == agent && it.agent1 != it.agent2 }
                    .sortedBy { it.internalTrustDegree }

            // create a KB from the list of opinions
            val worstOpinion = relevant.first()
            val (_, opinionKB) = relevant.drop(1).fold(Pair(worstOpinion, KnowledgeBase()),
                    { (previous, knowledgeBase), current ->
                        val co = CredibilityObject(previous.agent2.toString(),
                                current.agent2.toString(),
                                agent.toString())
                        knowledgeBase.expansion(co)

                        Pair(current, knowledgeBase)
                    })

            // only for debugging
            /*val opinionPrint = opinionKB.copy()
            opinionPrint.graph.removeVertex(EXP)
            opinionPrint.exportDOT("./tick-${time}-Opinions-$agent", Format.PNG)*/

            // merge current KB with the KB from this agent
            kb.merge(opinionKB)
        }
    }

    override fun processOpinions(new: List<Opinion>) {
        opinions = new
    }

    override fun getTrust(service: Int): Map<Int, PartialOrder> {
        // remove EXP vertex
        kb.graph.removeVertex(EXP)
        // kb.exportDOT("./tick-$time-Trust", Format.PNG)

        return kb.graph.vertexSet().asSequence()
                .map { it.toInt() to PartialOrder(it, kb) }
                .toMap()
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

    override fun toString(): String = "Credibility Orders"

    override fun getParametersPanel(): ParametersPanel? = null

    override fun setCurrentTime(current: Int) {
        time = current
    }

    override fun initialize(vararg params: Any) {}

    override fun setServices(services: List<Int>) {}

    override fun setRandomGenerator(generator: RandomGenerator) {}
}

class PartialOrder(private val agent: String, private val kb: KnowledgeBase) : Comparable<PartialOrder> {
    override fun compareTo(other: PartialOrder): Int = when {
        kb.isLess(this.agent, other.agent) -> -1 // this < other
        kb.isLess(other.agent, this.agent) -> 1 // this > other
        else -> 0 // equal or incomparable
    }
}