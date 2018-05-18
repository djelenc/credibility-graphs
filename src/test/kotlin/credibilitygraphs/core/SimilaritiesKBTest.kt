package credibilitygraphs.core

import guru.nidi.graphviz.engine.Format
import org.junit.Before
import org.junit.Test


class SimilaritiesKBTest {
    private lateinit var graph: SimilaritiesKnowledgeBase

    @Before
    fun setUp() {
        graph = SimilaritiesKnowledgeBase("(1, 2, 1), (2, 3, 2.1), (3, 4, .01)")
    }

    @Test
    fun maxCurrentIncomparable() {
        graph.exportDOT("./a", Format.PNG)
        // val a = "3.14".toDouble()
        // assertEquals(expected, actual)
    }
}