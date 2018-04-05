package credibilitygraphs.core

import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class NumericKBTest {
    private lateinit var graph: NumericKnowledgeBase

    @Before
    fun setUp() {
        graph = NumericKnowledgeBase("(A1,A2,F1),(A1,A3,F1),(A2,A4,B),(A2,A4,F3),(A3,A4,F2)")
        graph.pastAccuracy["B"] = NumericKnowledgeBase.PastAccuracy(0, 3)
        graph.pastAccuracy["F1"] = NumericKnowledgeBase.PastAccuracy(1, 2)
        graph.pastAccuracy["F2"] = NumericKnowledgeBase.PastAccuracy(2, 1)
        graph.pastAccuracy["F3"] = NumericKnowledgeBase.PastAccuracy(3, 0)
    }

    @Test
    fun expansionSuccess() {
        assertTrue(graph.expansion(CredibilityObject("A1", "F3", "A2")))
        assertTrue(graph.graph.containsEdge("A1", "F3"))
        assertEquals("A2", graph.graph.getEdge("A1", "F3").reporter)
    }

    @Test
    fun expansionFailure() {
        val before = graph.graph.edgeSet()
        assertFalse(graph.expansion(CredibilityObject("A4", "A1", "A2")))
        assertEquals(before, graph.graph.edgeSet())
    }

    @Test
    fun minimalSources() {
        val expected = HashSet<CredibilityObject>()
        Collections.addAll(expected,
                CredibilityObject("A1", "A2", "F1"),
                CredibilityObject("A2", "A4", "B"),
                CredibilityObject("A1", "A3", "F1"))

        assertEquals(expected, graph.getExtremes("A1", "A4", Extreme.MIN))
    }

    @Test
    fun minimalSourcesIncomparable() {
        val graph = NumericKnowledgeBase("(A,B,1),(A,C,5),(B,D,4),(C,D,3)")
        // credibility: (4,5,A),(1,5,A),(2,5,A),(3,5,A)
        graph.pastAccuracy["5"] = NumericKnowledgeBase.PastAccuracy(1, 0)

        val expected = setOf(
                CredibilityObject("A", "B", "1"),
                CredibilityObject("B", "D", "4"),
                CredibilityObject("C", "D", "3"))

        assertEquals(expected, graph.getExtremes("A", "D", Extreme.MIN))
    }

    @Test
    fun contraction() {
        val before = graph.getAllPaths("A1", "A4")
        assertEquals(3, before.size)
        assertEquals(5, graph.graph.edgeSet().size)

        graph.contraction("A1", "A4")

        val after = graph.getAllPaths("A1", "A4")
        assertEquals(0, after.size)
        assertEquals(2, graph.graph.edgeSet().size)
    }

    @Test
    fun prioritizedRevision() {
        val before = graph.getAllPaths("A2", "A1")
        assertEquals(0, before.size)
        assertEquals(5, graph.graph.edgeSet().size)

        assertTrue(graph.prioritizedRevision(CredibilityObject("A4", "A1", "F3")))

        val after = graph.getAllPaths("A2", "A1")
        assertEquals(1, after.size)
        assertEquals(3, graph.graph.edgeSet().size)
    }

    @Test
    fun prioritizedRevisionNoRemoval() {
        val before = graph.getAllPaths("A1", "A4")
        assertEquals(3, before.size)
        assertEquals(5, graph.graph.edgeSet().size)

        assertTrue(graph.prioritizedRevision(CredibilityObject("A1", "A4", "F3")))

        val after = graph.getAllPaths("A1", "A4")
        assertEquals(4, after.size)
        assertEquals(6, graph.graph.edgeSet().size)
    }

    @Test
    fun maximalSources() {
        val expected = setOf(
                CredibilityObject("A1", "A2", "F1"),
                CredibilityObject("A2", "A4", "F3"),
                CredibilityObject("A3", "A4", "F2"))

        assertEquals(expected, graph.getExtremes("A1", "A4", Extreme.MAX))
    }

    @Test
    fun reliability() {
        val expected = setOf("F1")
        assertEquals(expected, graph.reliability("A1", "A4"))
    }

    @Test
    fun nonPrioritizedRevisionSimpleExpansion() {
        assertTrue(graph.nonPrioritizedRevision(CredibilityObject("A1", "A4", "B")))

        val after = graph.getAllPaths("A1", "A4")
        assertEquals(4, after.size)
        assertEquals(6, graph.graph.edgeSet().size)
    }

    @Test
    fun nonPrioritizedRevisionRejection() {
        val pathsBefore = graph.getAllPaths("A4", "A1")
        assertFalse(graph.nonPrioritizedRevision(CredibilityObject("A4", "A1", "B")))
        assertEquals(pathsBefore, graph.getAllPaths("A4", "A1"))
    }

    @Test
    fun nonPrioritizedRevisionMoreCredibleObject() {
        assertTrue(graph.nonPrioritizedRevision(CredibilityObject("A4", "A1", "F3")))

        val expected = setOf(
                CredibilityObject("A2", "A4", "F3"),
                CredibilityObject("A4", "A1", "F3"),
                CredibilityObject("A3", "A4", "F2"))

        assertEquals(expected, graph.graph.edgeSet())
    }

    @Test
    fun isLess() {
        assertTrue(graph.isLess("A1", "A2"))
        assertTrue(graph.isLess("A1", "A4"))
        assertTrue(graph.isLess("A1", "A3"))
        assertTrue(graph.isLess("A3", "A4"))
        assertTrue(graph.isLess("A2", "A4"))

        assertFalse(graph.isLess("A2", "A1"))
        assertFalse(graph.isLess("A4", "A1"))
        assertFalse(graph.isLess("A3", "A1"))
        assertFalse(graph.isLess("A4", "A3"))
        assertFalse(graph.isLess("A4", "A2"))
    }

    @Test
    fun maxCurrentEmpty() {
        val graph = NumericKnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z)")
        val current = setOf<CredibilityObject>()
        val co = CredibilityObject("A", "B", "X")

        val expected = setOf(co)
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun maxCurrentSmaller() {
        val graph = NumericKnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z)")
        graph.pastAccuracy["A"] = NumericKnowledgeBase.PastAccuracy(0, 0)
        graph.pastAccuracy["B"] = NumericKnowledgeBase.PastAccuracy(1, 0)
        graph.pastAccuracy["C"] = NumericKnowledgeBase.PastAccuracy(2, 0)
        graph.pastAccuracy["D"] = NumericKnowledgeBase.PastAccuracy(3, 0)

        val current = setOf(CredibilityObject("1", "4", "C"))
        val co = CredibilityObject("2", "3", "D")

        val expected = setOf(co)
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun maxCurrentBigger() {
        val graph = NumericKnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z)")
        graph.pastAccuracy["A"] = NumericKnowledgeBase.PastAccuracy(0, 0)
        graph.pastAccuracy["B"] = NumericKnowledgeBase.PastAccuracy(1, 0)
        graph.pastAccuracy["C"] = NumericKnowledgeBase.PastAccuracy(2, 0)
        graph.pastAccuracy["D"] = NumericKnowledgeBase.PastAccuracy(3, 0)

        val current = setOf(CredibilityObject("1", "4", "D"))
        val co = CredibilityObject("2", "3", "C")

        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(current, actual)
    }

    @Test
    fun maxCurrentIncomparable() {
        val graph = NumericKnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z), (A, E, W)")
        graph.pastAccuracy["A"] = NumericKnowledgeBase.PastAccuracy(0, 0)
        graph.pastAccuracy["B"] = NumericKnowledgeBase.PastAccuracy(1, 0)
        graph.pastAccuracy["C"] = NumericKnowledgeBase.PastAccuracy(2, 0)
        graph.pastAccuracy["D"] = NumericKnowledgeBase.PastAccuracy(3, 0)
        graph.pastAccuracy["E"] = NumericKnowledgeBase.PastAccuracy(3, 0)
        // D and E are incomparable
        val current = setOf(CredibilityObject("1", "4", "D"))
        val co = CredibilityObject("2", "3", "E")

        val expected = current + co
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun getExtremeFromCollection() {
        val graph = NumericKnowledgeBase("(A, D, Y), (B, D, Y), (E, A, Y), (E, B, Y)")
        graph.pastAccuracy["E"] = NumericKnowledgeBase.PastAccuracy(0, 5)
        graph.pastAccuracy["D"] = NumericKnowledgeBase.PastAccuracy(1, 0)
        val collection = setOf(
                CredibilityObject("1", "2", "A"),
                CredibilityObject("3", "4", "B"),
                CredibilityObject("7", "8", "D"),
                CredibilityObject("9", "0", "E")
        )

        val expectedMax = setOf(CredibilityObject("7", "8", "D"))
        val actualMax = graph.getExtremes(collection, Extreme.MAX)

        assertEquals(expectedMax, actualMax)

        val expectedMin = setOf(CredibilityObject("9", "0", "E"))
        val actualMin = graph.getExtremes(collection, Extreme.MIN)

        assertEquals(expectedMin, actualMin)
    }
}