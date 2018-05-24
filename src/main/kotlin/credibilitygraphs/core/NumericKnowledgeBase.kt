package credibilitygraphs.core

import org.jgrapht.Graph
import org.jgrapht.graph.DirectedMultigraph

class NumericKnowledgeBase(graph: Graph<Int, CredibilityObject<Int, Int>>)
    : KnowledgeBase<Int, Int>(graph) {

    constructor(text: String = "")
            : this(parseText(text, { Integer.parseInt(it) }, { Integer.parseInt(it) },
            { s, t, l -> CredibilityObject(s, t, l) }, graphMaker))

    override fun makeInstance(graph: Graph<Int, CredibilityObject<Int, Int>>): KnowledgeBase<Int, Int> =
            NumericKnowledgeBase(graph)

    override fun isLessCredible(source: Int, target: Int, graph: KnowledgeBase<Int, Int>): Boolean {
        val accSource = pastAccuracy.getOrPut(source, { PastAccuracy(0, 0) })
        val accTarget = pastAccuracy.getOrPut(target, { PastAccuracy(0, 0) })
        val sourceTotal = accSource.correct - accSource.incorrect
        val targetTotal = accTarget.correct - accTarget.incorrect
        return sourceTotal < targetTotal
    }

    companion object {
        val graphMaker: () -> Graph<Int, CredibilityObject<Int, Int>> = {
            val g = CredibilityObject(0, 0, 0)
            DirectedMultigraph<Int, CredibilityObject<Int, Int>>(g::class.java)
        }
    }

    class PastAccuracy(var correct: Int, var incorrect: Int)

    internal val pastAccuracy: MutableMap<Int, PastAccuracy> = HashMap()

    fun updatePastAccuracy(agent: Int, correct: Int, incorrect: Int) {
        val result = pastAccuracy.getOrPut(agent, { PastAccuracy(0, 0) })
        result.correct += correct
        result.incorrect += incorrect
    }
}