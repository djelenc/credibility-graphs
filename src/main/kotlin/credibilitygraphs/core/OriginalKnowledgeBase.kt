package credibilitygraphs.core

import org.jgrapht.Graph
import org.jgrapht.graph.DirectedMultigraph

class OriginalKnowledgeBase(graph: Graph<String, CredibilityObject<String, String>>) : KnowledgeBase<String, String, CredibilityObject<String, String>>(graph) {

    constructor(credibilityObjects: String = "")
            : this(parseText(credibilityObjects, { it }, { it },
            { s, t, l -> CredibilityObject(s, t, l) }, graphMaker))

    override fun isLessCredible(source: String, target: String, graph: KnowledgeBase<String, String, CredibilityObject<String, String>>): Boolean =
            isLess(source, target, graph)

    override fun makeInstance(graph: Graph<String, CredibilityObject<String, String>>): KnowledgeBase<String, String, CredibilityObject<String, String>> =
            OriginalKnowledgeBase(graph)

    companion object {
        val graphMaker: () -> Graph<String, CredibilityObject<String, String>> = {
            val g = CredibilityObject("", "", "")
            DirectedMultigraph<String, CredibilityObject<String, String>>(g::class.java)
        }
    }
}

