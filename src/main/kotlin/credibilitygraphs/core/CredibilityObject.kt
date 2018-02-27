package credibilitygraphs.core

import org.jgrapht.graph.DefaultEdge

data class CredibilityObject(val src: String, val tgt: String, val reporter: String) : DefaultEdge() {
    override fun toString() = "$reporter ($src-$tgt)"
}

