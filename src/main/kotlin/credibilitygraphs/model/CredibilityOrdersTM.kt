package credibilitygraphs.model

import atb.interfaces.*
import credibilitygraphs.core.CredibilityObject
import credibilitygraphs.core.KnowledgeBase
import credibilitygraphs.core.SimilaritiesKnowledgeBase

val EXP = Double.MAX_VALUE

class CredibilityOrdersTM : TrustModel<PartialOrder<Int, Double, CredibilityObject<Int, Double>>> {
    private var time = 0

    private var agents: List<Int> = emptyList()

    data class InteractionOutcome(var count: Int = 0, var sum: Double = 0.0)

    private val experiences = LinkedHashMap<Int, InteractionOutcome>()

    private var kbExp = SimilaritiesKnowledgeBase()

    override fun processExperiences(newExpriences: List<Experience>) {
        // TODO: incremental KB-EXP creation (instead of making it from dict)
        for (e in newExpriences) {
            val record = experiences.getOrPut(e.agent, { InteractionOutcome() })
            record.count += 1
            record.sum += e.outcome
        }

        // 1) sort agents by average experience outcomes
        val sortedExperiences = experiences.asSequence()
                .map { it.key to it.value.sum / it.value.count }
                .sortedBy { (_, value) -> value }

        // 2) create a KB from experiences
        val worstExperience = sortedExperiences.first()
        val (_, experienceKB) = sortedExperiences.drop(1).fold(Pair(worstExperience.first, SimilaritiesKnowledgeBase()),
                { (prev, currentGraph), (current, _) ->
                    val co = CredibilityObject(prev, current, EXP)
                    currentGraph.expansion(co)

                    Pair(current, currentGraph)
                })

        kbExp = experienceKB

        /*for (exp in newExpriences) {
            // go through all opinions about the newExpriences experiences
            for (agent in agents) {
                var total = 0
                var score = 0

                val kb = kbOp.getOrPut(agent.toString(), { KnowledgeBase() })

                for (comparisonAgent in agents) {
                    if (exp.agent == comparisonAgent) {
                        continue
                    }

                    val cmpExp = kbExp.compare(comparisonAgent.toString(), exp.agent.toString())
                    val cmpOp = kb.compare(comparisonAgent.toString(), exp.agent.toString())

                    score += if (cmpExp == cmpOp) {
                        1 // we agree, reward
                    } else if (cmpExp == Comparison.LESS && cmpOp == Comparison.MORE ||
                            cmpExp == Comparison.MORE && cmpOp == Comparison.LESS) {
                        -1 // they claim the opposite, punish
                    } else if (cmpExp == Comparison.INCOMPARABLE && cmpOp != Comparison.INCOMPARABLE) {
                        -1 // if EXP says they are incomparable and they say otherwise, punish
                    } else {
                        0 // if they say incomparable, and EXP says MORE/LESS, do not punish
                    }

                    total += 1
                }

                val final = score.toFloat() / total.toFloat()

                println("Score for $agent: $final")
            }
        }*/
    }

    override fun calculateTrust() = Unit

    override fun processOpinions(opinions: List<Opinion>) {
        /*for (agent in agents) {
            // all opinions from given agent, sorted by internal trust degrees
            val relevant = opinions.asSequence()
                    .filter { it.agent1 == agent && it.agent1 != it.agent2 }
                    .sortedBy { it.internalTrustDegree }

            // create a KB from the list of opinions
            val worstOpinion = relevant.first()
            val (_, opinionKB) = relevant.drop(1).fold(Pair(worstOpinion, OriginalKnowledgeBase()),
                    { (previous, knowledgeBase), current ->
                        val co = CredibilityObject(previous.agent2.toString(),
                                current.agent2.toString(),
                                agent.toString())
                        knowledgeBase.expansion(co)

                        Pair(current, knowledgeBase)
                    })

            kbOp[agent.toString()] = opinionKB
        }*/
    }

    override fun getTrust(service: Int): Map<Int, PartialOrder<Int, Double, CredibilityObject<Int, Double>>> {
        return kbExp.graph.vertexSet().asSequence()
                .map { it.toInt() to PartialOrder(it, kbExp) }
                .toMap()

        // return HashMap()
        // remove EXP vertex
        /*kb.graph.removeVertex(EXP)
        // kb.exportDOT("./tick-$time-Trust", Format.PNG)

        return kb.graph.vertexSet().asSequence()
                .map { it.toInt() to PartialOrder(it, kb) }
                .toMap()*/
    }

    override fun setAgents(agents: List<Int>) {
        this.agents = agents
    }

    override fun toString(): String = "Credibility Orders"

    override fun getParametersPanel(): ParametersPanel? = null

    override fun setCurrentTime(current: Int) {
        time = current
    }

    override fun initialize(vararg params: Any) = Unit

    override fun setServices(services: List<Int>) = Unit

    override fun setRandomGenerator(generator: RandomGenerator) = Unit
}

class PartialOrder<Node, Edge, CredObj : CredibilityObject<Node, Edge>>(
        private val agent: Node, private val kb: KnowledgeBase<Node, Edge>)
    : Comparable<PartialOrder<Node, Edge, CredObj>> {
    override fun compareTo(other: PartialOrder<Node, Edge, CredObj>): Int = when {
        kb.isLess(this.agent, other.agent) -> -1 // this < other
        kb.isLess(other.agent, this.agent) -> 1 // this > other
        else -> 0 // equal or incomparable
    }
}