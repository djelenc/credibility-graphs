package credibilitygraphs.core

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
        // graph.exportDOT("./z", Format.PNG)
    }
}