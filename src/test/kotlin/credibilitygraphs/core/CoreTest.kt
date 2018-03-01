package credibilitygraphs.core

import org.jgrapht.GraphPath
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.graph.GraphWalk
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class CoreTest {

    private val EXAMPLE2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1),(A2,A4,B),(A2,A4,F3),(A3,A4,F2)"
    private val EXAMPLE5 = "(D,F,J),(D,H,L),(F,G,M),(H,G,M),(G,E,K),(J,K,E),(K,L,G),(L,M,E)"
    private val EXAMPLE13 = "(H,I,F),(H,L,D),(H,J,G),(I,L,G),(J,L,E),(J,L,F),(J,K,D)," +
            "(J,K,E),(K,L,D),(D,E,G),(D,F,E),(E,G,F),(F,G,D)"

    private var graph: CredibilityGraph? = null

    @Before
    fun setUp() {
        graph = CredibilityGraph(EXAMPLE2)
    }

    @Test
    fun findPaths() {
        val paths = graph!!.getAllPaths("A1", "A4")
        assertEquals(3, paths.size.toLong())
    }

    @Test
    fun expansionSuccess() {
        assertTrue(graph!!.expansion(CredibilityObject("A1", "F3", "A2")))
        assertTrue(graph!!.graph.containsEdge("A1", "F3"))
        assertEquals("A2", graph!!.graph.getEdge("A1", "F3").reporter)
    }

    @Test
    fun expansionFailure() {
        val before = graph!!.graph.edgeSet()
        assertFalse(graph!!.expansion(CredibilityObject("A4", "A1", "A2")))
        assertEquals(before, graph!!.graph.edgeSet())
    }

    @Test
    fun minimalSources() {
        val expected = HashSet<CredibilityObject>()
        Collections.addAll(expected,
                CredibilityObject("A1", "A2", "F1"),
                CredibilityObject("A2", "A4", "B"),
                CredibilityObject("A1", "A3", "F1"))

        assertEquals(expected, graph!!.getExtremes("A1", "A4", Extreme.MIN))
    }

    @Test
    fun minimalSourcesIncomparable() {
        val input = "(4,5,A),(1,5,A),(2,5,A),(3,5,A),(4,5,A),(A,B,1),(A,C,5),(B,D,4),(C,D,3)"
        val graph = CredibilityGraph(input)

        val expected = HashSet<CredibilityObject>()
        Collections.addAll(expected,
                CredibilityObject("A", "B", "1"),
                CredibilityObject("B", "D", "4"),
                CredibilityObject("C", "D", "3"))

        assertEquals(expected, graph.getExtremes("A", "D", Extreme.MIN))
    }

    @Test
    fun contraction() {
        val before = graph!!.getAllPaths("A1", "A4")
        assertEquals(3, before.size.toLong())
        assertEquals(8, graph!!.graph.edgeSet().size.toLong())

        graph!!.contraction("A1", "A4")

        val after = graph!!.getAllPaths("A1", "A4")
        assertEquals(0, after.size.toLong())
        assertEquals(5, graph!!.graph.edgeSet().size.toLong())
    }

    @Test
    fun prioritizedRevision() {
        val before = graph!!.getAllPaths("A2", "A1")
        assertEquals(0, before.size.toLong())
        assertEquals(8, graph!!.graph.edgeSet().size.toLong())

        assertTrue(graph!!.prioritizedRevision(CredibilityObject("A4", "A1", "F3")))

        val after = graph!!.getAllPaths("A2", "A1")
        assertEquals(1, after.size.toLong())
        assertEquals(6, graph!!.graph.edgeSet().size.toLong())
    }

    @Test
    fun prioritizedRevisionNoRemoval() {
        val before = graph!!.getAllPaths("A1", "A4")
        assertEquals(3, before.size.toLong())
        assertEquals(8, graph!!.graph.edgeSet().size.toLong())

        assertTrue(graph!!.prioritizedRevision(CredibilityObject("A1", "A4", "F3")))

        val after = graph!!.getAllPaths("A1", "A4")
        assertEquals(4, after.size.toLong())
        assertEquals(9, graph!!.graph.edgeSet().size.toLong())
    }

    @Test
    fun maximalSources() {
        val expected = HashSet<CredibilityObject>()
        Collections.addAll(expected,
                CredibilityObject("A1", "A2", "F1"),
                CredibilityObject("A2", "A4", "F3"),
                CredibilityObject("A3", "A4", "F2"))

        assertEquals(expected, graph!!.getExtremes("A1", "A4", Extreme.MAX))
    }

    @Test
    fun reliability() {
        val expected = HashSet<String>()
        expected.add("F1")
        assertEquals(expected, graph!!.reliability("A1", "A4"))
    }

    @Test
    fun nonPrioritizedRevisionSimpleExpansion() {
        assertTrue(graph!!.nonPrioritizedRevision(CredibilityObject("A4", "F3", "B")))

        val after = graph!!.getAllPaths("A4", "F3")
        assertEquals(1, after.size.toLong())
        assertEquals(9, graph!!.graph.edgeSet().size.toLong())
    }

    @Test
    fun nonPrioritizedRevisionRejection() {
        val pathsBefore = graph!!.getAllPaths("A4", "A1")

        assertFalse(graph!!.nonPrioritizedRevision(CredibilityObject("A4", "A1", "B")))

        assertEquals(pathsBefore, graph!!.getAllPaths("A4", "A1"))
    }

    @Test
    fun nonPrioritizedRevisionMoreCredibleObject() {
        assertTrue(graph!!.nonPrioritizedRevision(CredibilityObject("A4", "A1", "F3")))

        val expected = HashSet<CredibilityObject>()
        Collections.addAll(expected,
                CredibilityObject("A2", "A4", "F3"),
                CredibilityObject("A4", "A1", "F3"),
                CredibilityObject("A3", "A4", "F2"),
                CredibilityObject("B", "F1", "F2"),
                CredibilityObject("F1", "F2", "F3"),
                CredibilityObject("F2", "F3", "B"))

        assertEquals(expected, graph!!.graph.edgeSet())
    }

    @Test
    fun nonPrioritizedRevisionMoreCredibleObjectMultipleReliabilities() {
        val graph = CredibilityGraph(EXAMPLE13)
        assertTrue(graph.nonPrioritizedRevision(CredibilityObject("L", "H", "G")))

        val expected = HashSet<CredibilityObject>()
        Collections.addAll(expected,
                CredibilityObject("F", "G", "D"),
                CredibilityObject("J", "K", "E"),
                CredibilityObject("D", "F", "E"),
                CredibilityObject("E", "G", "F"),
                CredibilityObject("H", "J", "G"),
                CredibilityObject("I", "L", "G"),
                CredibilityObject("D", "E", "G"),
                CredibilityObject("L", "H", "G"))

        assertEquals(expected, graph.graph.edgeSet())
    }

    @Test
    fun copy() {
        val graph1 = CredibilityGraph(EXAMPLE2)
        val graph2 = graph1.copy()

        val edges1 = graph1.graph.edgeSet()
        val edges2 = graph2.graph.edgeSet()

        assertEquals(edges1, edges2)
        assertNotSame(edges1, edges2)

        val edge = edges1.iterator().next()
        graph1.graph.removeEdge(edge)

        assertFalse(graph1.graph.containsEdge(edge))
        assertTrue(graph2.graph.containsEdge(edge))
    }

    @Test
    fun buildPathSimple() {
        val graph = CredibilityGraph("(A, B, X), (B, C, Y)")

        val vertexes = Arrays.asList("A", "B", "C")

        val expected = setOf(GraphWalk(graph.graph, vertexes, 0.0))
        val actual = graph.buildPaths(vertexes)
        assertEquals(expected, actual)
    }

    @Test
    fun buildPathComplex() {
        val graph = CredibilityGraph("(A, B, X), (A, B, Y), (B, C, Z), (B, C, X), (B, C, Y)")
        val vertexes = Arrays.asList("A", "B", "C")

        val finder = AllDirectedPaths(graph.graph)

        val expected = HashSet(finder.getAllPaths(
                "A", "C", false, graph.graph.vertexSet().size))

        val actual = graph.buildPaths(vertexes)
        assertEquals(expected as Set<GraphPath<String, CredibilityObject>>, actual)
    }

    @Test
    fun buildPathCycleSimple() {
        val graph = CredibilityGraph("(A, B, X), (B, A, Y)")
        val vertexes = Arrays.asList("A", "B", "A")

        val finder = AllDirectedPaths(graph.graph)

        val expected = finder.getAllPaths(
                "A", "A", false, graph.graph.vertexSet().size)
                .filter { e -> e.length > 0 } // drop paths with length 1
                .toSet()

        val actual = graph.buildPaths(vertexes)
        assertEquals(expected, actual)
    }

    @Test
    fun buildPathCycleComplex() {
        val graph = CredibilityGraph("(A, B, X), (A, B, Y), (B, C, X), (B, C, Y), (C, A, Z)")
        val vertexes = Arrays.asList("A", "B", "C", "A")

        val finder = AllDirectedPaths(graph.graph)

        val expected = finder.getAllPaths(
                "A", "A", false, graph.graph.vertexSet().size)
                .filter { e -> e.length > 0 } // drop paths with length 1
                .toSet()

        val actual = HashSet(graph.buildPaths(vertexes))
        assertEquals(expected, actual)
    }

    @Test
    fun findMinimumCycles() {
        val graph = CredibilityGraph("(A, B, X), (A, B, Y), (B, C, Z), (B, C, W), (C, A, W)")

        val actual = graph.findCycles()
        assertEquals(4, actual.size.toLong())

        val finder = AllDirectedPaths(graph.graph)

        val expected = finder.getAllPaths(
                "C", "C", false, graph.graph.vertexSet().size)
                .filter { e -> e.length > 0 } // drop paths with length 1
                .toSet()

        assertEquals(expected, actual)
    }

    @Test
    fun comparable() {
        assertEquals(Comparison.LESS, graph!!.compare("A1", "A2"))
        assertEquals(Comparison.LESS, graph!!.compare("A1", "A4"))
        assertEquals(Comparison.LESS, graph!!.compare("A1", "A3"))
        assertEquals(Comparison.LESS, graph!!.compare("A3", "A4"))
        assertEquals(Comparison.LESS, graph!!.compare("A2", "A4"))

        assertEquals(Comparison.MORE, graph!!.compare("A2", "A1"))
        assertEquals(Comparison.MORE, graph!!.compare("A4", "A1"))
        assertEquals(Comparison.MORE, graph!!.compare("A3", "A1"))
        assertEquals(Comparison.MORE, graph!!.compare("A4", "A3"))
        assertEquals(Comparison.MORE, graph!!.compare("A4", "A2"))

        assertEquals(Comparison.LESS, graph!!.compare("B", "F1"))
        assertEquals(Comparison.LESS, graph!!.compare("B", "F2"))
        assertEquals(Comparison.LESS, graph!!.compare("B", "F3"))
        assertEquals(Comparison.LESS, graph!!.compare("F1", "F2"))
        assertEquals(Comparison.LESS, graph!!.compare("F1", "F3"))
        assertEquals(Comparison.LESS, graph!!.compare("F2", "F3"))

        assertEquals(Comparison.MORE, graph!!.compare("F1", "B"))
        assertEquals(Comparison.MORE, graph!!.compare("F2", "B"))
        assertEquals(Comparison.MORE, graph!!.compare("F3", "B"))
        assertEquals(Comparison.MORE, graph!!.compare("F2", "F1"))
        assertEquals(Comparison.MORE, graph!!.compare("F3", "F1"))
        assertEquals(Comparison.MORE, graph!!.compare("F3", "F2"))

        for (g1 in listOf("A1", "A2", "A3", "A4")) {
            for (g2 in listOf("F1", "F2", "F3", "B")) {
                assertEquals(Comparison.INCOMPARABLE, graph!!.compare(g1, g2))
            }
        }
    }
}