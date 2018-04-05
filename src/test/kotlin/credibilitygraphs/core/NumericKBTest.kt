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
}
