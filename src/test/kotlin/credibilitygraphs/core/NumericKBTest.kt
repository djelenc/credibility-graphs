package credibilitygraphs.core

import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test


class NumericKBTest {
    private lateinit var graph: NumericKnowledgeBase

    @Before
    fun setUp() {
        graph = NumericKnowledgeBase("(11,12,1),(11,13,1),(12,14,0),(12,14,3),(13,14,2)")
        graph.pastAccuracy[0] = NumericKnowledgeBase.PastAccuracy(0, 3)
        graph.pastAccuracy[1] = NumericKnowledgeBase.PastAccuracy(1, 2)
        graph.pastAccuracy[2] = NumericKnowledgeBase.PastAccuracy(2, 1)
        graph.pastAccuracy[3] = NumericKnowledgeBase.PastAccuracy(3, 0)
    }


    @Test
    fun expansionSuccess() {
        assertTrue(graph.expansion(CredibilityObject(11, 3, 12)))
        assertTrue(graph.graph.containsEdge(11, 3))
        assertEquals(12, graph.graph.getEdge(11, 3).reporter)
    }

    @Test
    fun expansionFailure() {
        val before = graph.graph.edgeSet()
        assertFalse(graph.expansion(CredibilityObject(14, 11, 12)))
        assertEquals(before, graph.graph.edgeSet())
    }

    @Test
    fun minimalSources() {
        val expected = setOf(
                CredibilityObject(11, 12, 1),
                CredibilityObject(12, 14, 0),
                CredibilityObject(11, 13, 1))

        assertEquals(expected, graph.getExtremes(11, 14, Extreme.MIN))
    }

    @Test
    fun minimalSourcesIncomparable() {
        // credibility: (4,5,A),(1,5,A),(2,5,A),(3,5,A)
        val graph = NumericKnowledgeBase("(11,12,1),(11,13,5),(12,14,4),(13,14,3)")
        graph.pastAccuracy[5] = NumericKnowledgeBase.PastAccuracy(1, 0)

        val expected = setOf(
                CredibilityObject(11, 12, 1),
                CredibilityObject(12, 14, 4),
                CredibilityObject(13, 14, 3))

        assertEquals(expected, graph.getExtremes(11, 14, Extreme.MIN))
    }

    @Test
    fun contraction() {
        val before = graph.getAllPaths(11, 14)
        assertEquals(3, before.size)
        assertEquals(5, graph.graph.edgeSet().size)

        graph.contraction(11, 14)

        val after = graph.getAllPaths(11, 14)
        assertEquals(0, after.size)
        assertEquals(2, graph.graph.edgeSet().size)
    }

    @Test
    fun prioritizedRevision() {
        val before = graph.getAllPaths(12, 11)
        assertEquals(0, before.size)
        assertEquals(5, graph.graph.edgeSet().size)

        assertTrue(graph.prioritizedRevision(CredibilityObject(14, 11, 3)))

        val after = graph.getAllPaths(12, 11)
        assertEquals(1, after.size)
        assertEquals(3, graph.graph.edgeSet().size)
    }

    @Test
    fun prioritizedRevisionNoRemoval() {
        val before = graph.getAllPaths(11, 14)
        assertEquals(3, before.size)
        assertEquals(5, graph.graph.edgeSet().size)

        assertTrue(graph.prioritizedRevision(CredibilityObject(11, 14, 3)))

        val after = graph.getAllPaths(11, 14)
        assertEquals(4, after.size)
        assertEquals(6, graph.graph.edgeSet().size)
    }

    @Test
    fun maximalSources() {
        val expected = setOf(
                CredibilityObject(11, 12, 1),
                CredibilityObject(12, 14, 3),
                CredibilityObject(13, 14, 2))

        assertEquals(expected, graph.getExtremes(11, 14, Extreme.MAX))
    }

    @Test
    fun reliability() {
        val expected = setOf(1)
        assertEquals(expected, graph.reliability(11, 14))
    }

    @Test
    fun nonPrioritizedRevisionSimpleExpansion() {
        assertTrue(graph.nonPrioritizedRevision(CredibilityObject(11, 14, 0)))

        val after = graph.getAllPaths(11, 14)
        assertEquals(4, after.size)
        assertEquals(6, graph.graph.edgeSet().size)
    }

    @Test
    fun nonPrioritizedRevisionRejection() {
        val pathsBefore = graph.getAllPaths(14, 11)
        assertFalse(graph.nonPrioritizedRevision(CredibilityObject(14, 11, 0)))
        assertEquals(pathsBefore, graph.getAllPaths(14, 11))
    }

    @Test
    fun nonPrioritizedRevisionMoreCredibleObject() {
        assertTrue(graph.nonPrioritizedRevision(CredibilityObject(14, 11, 3)))

        val expected = setOf(
                CredibilityObject(12, 14, 3),
                CredibilityObject(14, 11, 3),
                CredibilityObject(13, 14, 2))

        assertEquals(expected, graph.graph.edgeSet())
    }

    @Test
    fun isLess() {
        assertTrue(graph.isLess(11, 12))
        assertTrue(graph.isLess(11, 14))
        assertTrue(graph.isLess(11, 13))
        assertTrue(graph.isLess(13, 14))
        assertTrue(graph.isLess(12, 14))

        assertFalse(graph.isLess(12, 11))
        assertFalse(graph.isLess(14, 11))
        assertFalse(graph.isLess(13, 11))
        assertFalse(graph.isLess(14, 13))
        assertFalse(graph.isLess(14, 12))
    }

    @Test
    fun maxCurrentEmpty() {
        val graph = NumericKnowledgeBase("(11, 12, 1), (12, 13, 2), (13, 14, 3)")
        val current = setOf<CredibilityObject<Int, Int>>()
        val co = CredibilityObject(11, 12, 1)

        val expected = setOf(co)
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun maxCurrentSmaller() {
        val graph = NumericKnowledgeBase("(1, 2, 5), (2, 3, 6), (3, 4, 7)")
        graph.pastAccuracy[1] = NumericKnowledgeBase.PastAccuracy(0, 0)
        graph.pastAccuracy[2] = NumericKnowledgeBase.PastAccuracy(1, 0)
        graph.pastAccuracy[3] = NumericKnowledgeBase.PastAccuracy(2, 0)
        graph.pastAccuracy[4] = NumericKnowledgeBase.PastAccuracy(3, 0)

        val current = setOf(CredibilityObject(11, 14, 3))
        val co = CredibilityObject(12, 13, 4)

        val expected = setOf(co)
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun maxCurrentBigger() {
        val graph = NumericKnowledgeBase("(1, 2, 5), (2, 3, 6), (3, 4, 7)")
        graph.pastAccuracy[1] = NumericKnowledgeBase.PastAccuracy(0, 0)
        graph.pastAccuracy[2] = NumericKnowledgeBase.PastAccuracy(1, 0)
        graph.pastAccuracy[3] = NumericKnowledgeBase.PastAccuracy(2, 0)
        graph.pastAccuracy[4] = NumericKnowledgeBase.PastAccuracy(3, 0)

        val current = setOf(CredibilityObject(11, 14, 4))
        val co = CredibilityObject(12, 13, 3)

        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(current, actual)
    }

    @Test
    fun maxCurrentIncomparable() {
        val graph = NumericKnowledgeBase("(1, 2, 6), (2, 3, 7), (3, 4, 8), (1, 5, 9)")
        graph.pastAccuracy[1] = NumericKnowledgeBase.PastAccuracy(0, 0)
        graph.pastAccuracy[2] = NumericKnowledgeBase.PastAccuracy(1, 0)
        graph.pastAccuracy[3] = NumericKnowledgeBase.PastAccuracy(2, 0)
        graph.pastAccuracy[4] = NumericKnowledgeBase.PastAccuracy(3, 0)
        graph.pastAccuracy[5] = NumericKnowledgeBase.PastAccuracy(3, 0)
        // 4 and 5 are incomparable
        val current = setOf(CredibilityObject(11, 14, 4))
        val co = CredibilityObject(12, 13, 5)

        val expected = current + co
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun getExtremeFromCollection() {
        val graph = NumericKnowledgeBase("(1, 4, 33), (2, 4, 33), (5, 1, 33), (5, 2, 33)")
        graph.pastAccuracy[5] = NumericKnowledgeBase.PastAccuracy(0, 5)
        graph.pastAccuracy[4] = NumericKnowledgeBase.PastAccuracy(1, 0)
        val collection = setOf(
                CredibilityObject(11, 12, 1),
                CredibilityObject(13, 14, 2),
                CredibilityObject(17, 18, 4),
                CredibilityObject(19, 10, 5)
        )

        val expectedMax = setOf(CredibilityObject(17, 18, 4))
        val actualMax = graph.getExtremes(collection, Extreme.MAX)

        assertEquals(expectedMax, actualMax)

        val expectedMin = setOf(CredibilityObject(19, 10, 5))
        val actualMin = graph.getExtremes(collection, Extreme.MIN)

        assertEquals(expectedMin, actualMin)
    }
}