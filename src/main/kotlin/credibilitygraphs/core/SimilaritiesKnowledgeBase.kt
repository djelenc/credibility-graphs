package credibilitygraphs.core

import org.jgrapht.Graph
import org.jgrapht.graph.DirectedMultigraph

class SimilaritiesKnowledgeBase(graph: Graph<Int, CredibilityObject<Int, Double>>)
    : KnowledgeBase<Int, Double>(graph) {

    constructor(text: String = "")
            : this(parseText(text, { it.toInt() }, { it.toDouble() },
            { s, t, l -> CredibilityObject(s, t, l) }, graphMaker))

    override fun makeInstance(graph: Graph<Int, CredibilityObject<Int, Double>>): KnowledgeBase<Int, Double> =
            SimilaritiesKnowledgeBase(graph)

    override fun isLessCredible(source: Double, target: Double, graph: KnowledgeBase<Int, Double>): Boolean =
            source < target

    companion object {
        val graphMaker: () -> Graph<Int, CredibilityObject<Int, Double>> = {
            val g = CredibilityObject(0, 0, 0.0)
            DirectedMultigraph<Int, CredibilityObject<Int, Double>>(g::class.java)
        }
    }
}