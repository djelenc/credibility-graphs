package credibilitygraphs.core

import credibilitygraphs.model.PartialOrder
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

    private val example2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1),(A2,A4,B),(A2,A4,F3),(A3,A4,F2)"
    // private val example5 = "(D,F,J),(D,H,L),(F,G,M),(H,G,M),(G,E,K),(J,K,E),(K,L,G),(L,M,E)"
    private val example13 = "(H,I,F),(H,L,D),(H,J,G),(I,L,G),(J,L,E),(J,L,F),(J,K,D)," +
            "(J,K,E),(K,L,D),(D,E,G),(D,F,E),(E,G,F),(F,G,D)"

    private lateinit var graph: KnowledgeBase

    @Before
    fun setUp() {
        graph = KnowledgeBase(example2)
    }

    @Test
    fun findPaths() {
        val paths = graph.getAllPaths("A1", "A4")
        assertEquals(3, paths.size.toLong())
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
        val input = "(4,5,A),(1,5,A),(2,5,A),(3,5,A),(4,5,A),(A,B,1),(A,C,5),(B,D,4),(C,D,3)"
        val graph = KnowledgeBase(input)

        val expected = HashSet<CredibilityObject>()
        Collections.addAll(expected,
                CredibilityObject("A", "B", "1"),
                CredibilityObject("B", "D", "4"),
                CredibilityObject("C", "D", "3"))

        assertEquals(expected, graph.getExtremes("A", "D", Extreme.MIN))
    }

    @Test
    fun contraction() {
        val before = graph.getAllPaths("A1", "A4")
        assertEquals(3, before.size.toLong())
        assertEquals(8, graph.graph.edgeSet().size.toLong())

        graph.contraction("A1", "A4")

        val after = graph.getAllPaths("A1", "A4")
        assertEquals(0, after.size.toLong())
        assertEquals(5, graph.graph.edgeSet().size.toLong())
    }

    @Test
    fun prioritizedRevision() {
        val before = graph.getAllPaths("A2", "A1")
        assertEquals(0, before.size.toLong())
        assertEquals(8, graph.graph.edgeSet().size.toLong())

        assertTrue(graph.prioritizedRevision(CredibilityObject("A4", "A1", "F3")))

        val after = graph.getAllPaths("A2", "A1")
        assertEquals(1, after.size.toLong())
        assertEquals(6, graph.graph.edgeSet().size.toLong())
    }

    @Test
    fun prioritizedRevisionNoRemoval() {
        val before = graph.getAllPaths("A1", "A4")
        assertEquals(3, before.size.toLong())
        assertEquals(8, graph.graph.edgeSet().size.toLong())

        assertTrue(graph.prioritizedRevision(CredibilityObject("A1", "A4", "F3")))

        val after = graph.getAllPaths("A1", "A4")
        assertEquals(4, after.size.toLong())
        assertEquals(9, graph.graph.edgeSet().size.toLong())
    }

    @Test
    fun maximalSources() {
        val expected = HashSet<CredibilityObject>()
        Collections.addAll(expected,
                CredibilityObject("A1", "A2", "F1"),
                CredibilityObject("A2", "A4", "F3"),
                CredibilityObject("A3", "A4", "F2"))

        assertEquals(expected, graph.getExtremes("A1", "A4", Extreme.MAX))
    }

    @Test
    fun reliability() {
        val expected = HashSet<String>()
        expected.add("F1")
        assertEquals(expected, graph.reliability("A1", "A4"))
    }

    @Test
    fun nonPrioritizedRevisionSimpleExpansion() {
        assertTrue(graph.nonPrioritizedRevision(CredibilityObject("A4", "F3", "B")))

        val after = graph.getAllPaths("A4", "F3")
        assertEquals(1, after.size.toLong())
        assertEquals(9, graph.graph.edgeSet().size.toLong())
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

        val expected = HashSet<CredibilityObject>()
        Collections.addAll(expected,
                CredibilityObject("A2", "A4", "F3"),
                CredibilityObject("A4", "A1", "F3"),
                CredibilityObject("A3", "A4", "F2"),
                CredibilityObject("B", "F1", "F2"),
                CredibilityObject("F1", "F2", "F3"),
                CredibilityObject("F2", "F3", "B"))

        assertEquals(expected, graph.graph.edgeSet())
    }

    @Test
    fun nonPrioritizedRevisionMoreCredibleObjectMultipleReliabilities() {
        val graph = KnowledgeBase(example13)
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
        val graph1 = KnowledgeBase(example2)
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
        val graph = KnowledgeBase("(A, B, X), (B, C, Y)")

        val vertexes = Arrays.asList("A", "B", "C")

        val expected = setOf(GraphWalk(graph.graph, vertexes, 0.0))
        val actual = graph.buildPaths(vertexes)
        assertEquals(expected, actual)
    }

    @Test
    fun buildPathSimpleLazy1() {
        val graph = KnowledgeBase("(A, B, X), (B, C, Y)")

        val vertexes = Arrays.asList("A", "B", "C")

        val expected = setOf(GraphWalk(graph.graph, vertexes, 0.0))
        val actual = graph.buildPathsLazy(vertexes).toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun buildPathSimpleLazy2() {
        val graph = KnowledgeBase("""(A, B, X), (A, B, Y),
            |(A, B, Z), (B, C, X), (B, C, Y), (B, C, Z),
            |(C, D, X)""".trimMargin())

        val vertexes = listOf("A", "B", "C", "D")
        val sequence = graph.buildPathsLazy(vertexes)
        assertEquals(9, sequence.count())
    }

    @Test
    fun buildPathComplex() {
        val graph = KnowledgeBase("(A, B, X), (A, B, Y), (B, C, Z), (B, C, X), (B, C, Y)")
        val vertexes = Arrays.asList("A", "B", "C")

        val finder = AllDirectedPaths(graph.graph)

        val expected: Set<GraphPath<String, CredibilityObject>> = HashSet(finder.getAllPaths(
                "A", "C", false, graph.graph.vertexSet().size))

        val actual = graph.buildPaths(vertexes)
        assertEquals(expected, actual)
    }

    @Test
    fun buildPathComplexLazy() {
        val graph = KnowledgeBase("(A, B, X), (A, B, Y), (B, C, Z), (B, C, X), (B, C, Y)")
        val vertexes = Arrays.asList("A", "B", "C")

        val finder = AllDirectedPaths(graph.graph)

        val expected: Set<GraphPath<String, CredibilityObject>> = HashSet(finder.getAllPaths(
                "A", "C", false, graph.graph.vertexSet().size))

        val actual = graph.buildPathsLazy(vertexes).toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun buildPathCycleSimple() {
        val graph = KnowledgeBase("(A, B, X), (B, A, Y)")
        val vertexes = Arrays.asList("A", "B", "A")

        val finder = AllDirectedPaths(graph.graph)

        val expected = finder.getAllPaths(
                "A", "A", false, graph.graph.vertexSet().size)
                .filter { it.length > 0 } // drop paths with length 0
                .toSet()

        val actual = graph.buildPaths(vertexes)
        assertEquals(expected, actual)
    }

    @Test
    fun buildPathCycleSimpleLazy() {
        val graph = KnowledgeBase("(A, B, X), (B, A, Y)")
        val vertexes = Arrays.asList("A", "B", "A")

        val finder = AllDirectedPaths(graph.graph)

        val expected = finder.getAllPaths(
                "A", "A", false, graph.graph.vertexSet().size)
                .filter { it.length > 0 } // drop paths with length 0
                .toSet()

        val actual = graph.buildPathsLazy(vertexes).toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun buildPathCycleComplex() {
        val graph = KnowledgeBase("(A, B, X), (A, B, Y), (B, C, X), (B, C, Y), (C, A, Z)")
        val vertexes = Arrays.asList("A", "B", "C", "A")

        val finder = AllDirectedPaths(graph.graph)

        val expected = finder.getAllPaths(
                "A", "A", false, graph.graph.vertexSet().size)
                .filter { it.length > 0 } // drop paths with length 0
                .toSet()

        val actual = graph.buildPaths(vertexes)
        assertEquals(expected, actual)
    }

    @Test
    fun buildPathCycleComplexLazy() {
        val graph = KnowledgeBase("(A, B, X), (A, B, Y), (B, C, X), (B, C, Y), (C, A, Z)")
        val vertexes = Arrays.asList("A", "B", "C", "A")

        val finder = AllDirectedPaths(graph.graph)

        val expected = finder.getAllPaths(
                "A", "A", false, graph.graph.vertexSet().size)
                .filter { it.length > 0 } // drop paths with length 0
                .toSet()

        val actual = graph.buildPathsLazy(vertexes).toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun findMinimumCycles() {
        val graph = KnowledgeBase("(A, B, X), (A, B, Y), (B, C, Z), (B, C, W), (C, A, W)")

        val actual = graph.findCycles()
        assertEquals(4, actual.size.toLong())

        val finder = AllDirectedPaths(graph.graph)

        val expected = finder.getAllPaths(
                "C", "C", false, graph.graph.vertexSet().size)
                .filter { it.length > 0 } // drop paths with length 0
                .toSet()

        assertEquals(expected, actual)
    }


    @Test
    fun findMinimumCyclesLazy() {
        val graph = KnowledgeBase("(A, B, X), (A, B, Y), (B, C, Z), (B, C, W), (C, A, W)")

        val actual = graph.findCyclesLazy().toSet()
        assertEquals(4, actual.size.toLong())

        val finder = AllDirectedPaths(graph.graph)

        val expected = finder.getAllPaths(
                "C", "C", false, graph.graph.vertexSet().size)
                .filter { it.length > 0 } // drop paths with length 0
                .toSet()

        assertEquals(expected, actual)
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

        assertTrue(graph.isLess("B", "F1"))
        assertTrue(graph.isLess("B", "F2"))
        assertTrue(graph.isLess("B", "F3"))
        assertTrue(graph.isLess("F1", "F2"))
        assertTrue(graph.isLess("F1", "F3"))
        assertTrue(graph.isLess("F2", "F3"))

        assertFalse(graph.isLess("F1", "B"))
        assertFalse(graph.isLess("F2", "B"))
        assertFalse(graph.isLess("F3", "B"))
        assertFalse(graph.isLess("F2", "F1"))
        assertFalse(graph.isLess("F3", "F1"))
        assertFalse(graph.isLess("F3", "F2"))

        for (g1 in listOf("A1", "A2", "A3", "A4")) {
            for (g2 in listOf("F1", "F2", "F3", "B")) {
                assertFalse(graph.isLess(g1, g2))
            }
        }
    }

    @Test
    fun maxCurrentEmpty() {
        val graph = KnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z)")
        val current = setOf<CredibilityObject>()
        val co = CredibilityObject("A", "B", "X")

        val expected = setOf(co)
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun maxCurrentSmaller() {
        val graph = KnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z)")
        val current = setOf(CredibilityObject("1", "4", "C"))
        val co = CredibilityObject("2", "3", "D")

        val expected = setOf(co)
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun maxCurrentBigger() {
        val graph = KnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z)")
        val current = setOf(CredibilityObject("1", "4", "D"))
        val co = CredibilityObject("2", "3", "C")

        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(current, actual)
    }

    @Test
    fun maxCurrentIncomparable() {
        val graph = KnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z), (A, E, W)")
        val current = setOf(CredibilityObject("1", "4", "D"))
        // D and E are incomparable
        val co = CredibilityObject("2", "3", "E")

        val expected = current + co
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun maxCurrentIncomparableToSomeBiggerThanOthers() {
        val graph = KnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z), (A, E, W), (E, F, U)")
        val current = setOf(
                CredibilityObject("1", "4", "D"),
                CredibilityObject("4", "47", "E")
        )
        // D and E are incomparable
        val co = CredibilityObject("2", "3", "F")

        val expected = setOf(co, CredibilityObject("1", "4", "D"))
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun maxCurrentIncomparableToSomeSmallerThanSome() {
        val graph = KnowledgeBase("(A, B, X), (B, C, Y), (C, D, Z), (A, E, W), (E, F, U)")
        val current = setOf(
                CredibilityObject("1", "4", "D"),
                CredibilityObject("4", "47", "F")
        )
        // D and E are incomparable
        val co = CredibilityObject("2", "3", "E")

        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(current, actual)
    }

    @Test
    fun maxCurrentIncomparableToSomeBiggerThanSome() {
        val graph = KnowledgeBase("(A, D, Y), (B, D, Y), (E, A, Y), (E, B, Y), (E, C, Y)")
        val current = setOf(
                CredibilityObject("1", "4", "A"),
                CredibilityObject("4", "7", "B"),
                CredibilityObject("4", "8", "C")
        )
        val co = CredibilityObject("2", "3", "D")

        val expected = setOf(CredibilityObject("4", "8", "C"), co)
        val actual = graph.extreme(current, co, Extreme.MAX, graph)

        assertEquals(expected, actual)
    }

    @Test
    fun getExtremeFromCollection() {
        val graph = KnowledgeBase("(A, D, Y), (B, D, Y), (E, A, Y), (E, B, Y), (E, C, Y)")

        val collection = setOf(
                CredibilityObject("1", "2", "A"),
                CredibilityObject("3", "4", "B"),
                CredibilityObject("5", "6", "C"),
                CredibilityObject("7", "8", "D"),
                CredibilityObject("9", "0", "E")
        )

        val expectedMax = setOf(
                CredibilityObject("7", "8", "D"),
                CredibilityObject("5", "6", "C"))
        val actualMax = graph.getExtremes(collection, Extreme.MAX)

        assertEquals(expectedMax, actualMax)

        val expectedMin = setOf(CredibilityObject("9", "0", "E"))
        val actualMin = graph.getExtremes(collection, Extreme.MIN)

        assertEquals(expectedMin, actualMin)
    }

    @Test
    fun partialOrders() {
        val kb = KnowledgeBase(example2)

        val orders = kb.graph.vertexSet().map { it to PartialOrder(it, kb) }.toMap()

        assert(orders["B"]!! < orders["F1"]!!)
        assert(orders["B"]!! < orders["F2"]!!)
        assert(orders["B"]!! < orders["F3"]!!)
        assert(orders["F1"]!! < orders["F2"]!!)
        assert(orders["F1"]!! < orders["F3"]!!)
        assert(orders["F1"]!! > orders["B"]!!)
        assert(orders["F2"]!! > orders["B"]!!)
        assert(orders["F3"]!! > orders["B"]!!)
        assert(orders["F2"]!! > orders["F1"]!!)
        assert(orders["F3"]!! > orders["F1"]!!)

        for (node1 in listOf("A1", "A2", "A3", "A4")) {
            for (node2 in listOf("B", "F1", "F2", "F3")) {
                assert(orders[node1]!!.compareTo(orders[node2]!!) == 0)
                assert(orders[node2]!!.compareTo(orders[node1]!!) == 0)
            }
        }

        assert(orders["A1"]!! < orders["A2"]!!)
        assert(orders["A1"]!! < orders["A3"]!!)
        assert(orders["A1"]!! < orders["A4"]!!)
        assert(orders["A2"]!! < orders["A4"]!!)
        assert(orders["A3"]!! < orders["A4"]!!)
        assert(orders["A3"]!!.compareTo(orders["A2"]!!) == 0)
        assert(orders["A2"]!! > orders["A1"]!!)
        assert(orders["A3"]!! > orders["A1"]!!)
        assert(orders["A4"]!! > orders["A1"]!!)
        assert(orders["A4"]!! > orders["A2"]!!)
        assert(orders["A4"]!! > orders["A3"]!!)
    }
}