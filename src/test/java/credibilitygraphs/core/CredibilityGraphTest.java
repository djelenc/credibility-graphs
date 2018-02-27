package credibilitygraphs.core;

import credibilitygraphs.App;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.GraphWalk;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CredibilityGraphTest {

    private CredibilityGraph graph = null;

    @Before
    public void setUp() {
        graph = new CredibilityGraph(App.EXAMPLE2);
    }

    @Test
    public void findPaths() {
        final List<GraphPath<String, CredibilityObject>> paths = graph.findPaths("A1", "A4");
        assertEquals(3, paths.size());
    }

    @Test
    public void expansionSuccess() {
        assertTrue(graph.expansion("A1", "F3", "A2"));
        assertTrue(graph.getGraph().containsEdge("A1", "F3"));
        assertEquals("A2", graph.getGraph().getEdge("A1", "F3").getReporter());
    }

    @Test
    public void expansionFailure() {
        final Set<CredibilityObject> before = graph.getGraph().edgeSet();
        assertFalse(graph.expansion("A4", "A1", "A2"));
        assertEquals(before, graph.getGraph().edgeSet());
    }

    @Test
    public void minimalSources() {
        final Set<CredibilityObject> expected = new HashSet<>();
        Collections.addAll(expected,
                new CredibilityObject("A1", "A2", "F1"),
                new CredibilityObject("A2", "A4", "B"),
                new CredibilityObject("A1", "A3", "F1"));

        assertEquals(expected, graph.getExtremesFromAllPaths("A1", "A4", CredibilityGraph.Extreme.MIN));
    }

    @Test
    public void minimalSourcesIncomparable() {
        final String input = "(4,5,A),(1,5,A),(2,5,A),(3,5,A),(4,5,A),(A,B,1),(A,C,5),(B,D,4),(C,D,3)";
        final CredibilityGraph graph = new CredibilityGraph(input);

        final Set<CredibilityObject> expected = new HashSet<>();
        Collections.addAll(expected,
                new CredibilityObject("A", "B", "1"),
                new CredibilityObject("B", "D", "4"),
                new CredibilityObject("C", "D", "3"));

        assertEquals(expected, graph.getExtremesFromAllPaths("A", "D", CredibilityGraph.Extreme.MIN));
    }

    @Test
    public void contraction() {
        final List<GraphPath<String, CredibilityObject>> before = graph.findPaths("A1", "A4");
        assertEquals(3, before.size());
        assertEquals(8, graph.getGraph().edgeSet().size());

        graph.contraction("A1", "A4");

        final List<GraphPath<String, CredibilityObject>> after = graph.findPaths("A1", "A4");
        assertEquals(0, after.size());
        assertEquals(5, graph.getGraph().edgeSet().size());
    }

    @Test
    public void prioritizedRevision() {
        final List<GraphPath<String, CredibilityObject>> before = graph.findPaths("A2", "A1");
        assertEquals(0, before.size());
        assertEquals(8, graph.getGraph().edgeSet().size());

        assertTrue(graph.prioritizedRevision("A4", "A1", "F3"));

        final List<GraphPath<String, CredibilityObject>> after = graph.findPaths("A2", "A1");
        assertEquals(1, after.size());
        assertEquals(6, graph.getGraph().edgeSet().size());
    }

    @Test
    public void prioritizedRevisionNoRemoval() {
        final List<GraphPath<String, CredibilityObject>> before = graph.findPaths("A1", "A4");
        assertEquals(3, before.size());
        assertEquals(8, graph.getGraph().edgeSet().size());

        assertTrue(graph.prioritizedRevision("A1", "A4", "F3"));

        final List<GraphPath<String, CredibilityObject>> after = graph.findPaths("A1", "A4");
        assertEquals(4, after.size());
        assertEquals(9, graph.getGraph().edgeSet().size());
    }

    @Test
    public void maximalSources() {
        final Set<CredibilityObject> expected = new HashSet<>();
        Collections.addAll(expected,
                new CredibilityObject("A1", "A2", "F1"),
                new CredibilityObject("A2", "A4", "F3"),
                new CredibilityObject("A3", "A4", "F2"));

        assertEquals(expected, graph.getExtremesFromAllPaths("A1", "A4", CredibilityGraph.Extreme.MAX));
    }

    @Test
    public void reliability() {
        final Set<String> expected = new HashSet<>();
        expected.add("F1");
        assertEquals(expected, graph.reliability("A1", "A4"));
    }

    @Test
    public void nonPrioritizedRevisionSimpleExpansion() {
        assertTrue(graph.nonPrioritizedRevision("A4", "F3", "B"));

        final List<GraphPath<String, CredibilityObject>> after = graph.findPaths("A4", "F3");
        assertEquals(1, after.size());
        assertEquals(9, graph.getGraph().edgeSet().size());
    }

    @Test
    public void nonPrioritizedRevisionRejection() {
        final List<GraphPath<String, CredibilityObject>> pathsBefore = graph.findPaths("A4", "A1");

        assertFalse(graph.nonPrioritizedRevision("A4", "A1", "B"));

        assertEquals(pathsBefore, graph.findPaths("A4", "A1"));
    }

    @Test
    public void nonPrioritizedRevisionMoreCredibleObject() {
        assertTrue(graph.nonPrioritizedRevision("A4", "A1", "F3"));

        final Set<CredibilityObject> expected = new HashSet<>();
        Collections.addAll(expected,
                new CredibilityObject("A2", "A4", "F3"),
                new CredibilityObject("A4", "A1", "F3"),
                new CredibilityObject("A3", "A4", "F2"),
                new CredibilityObject("B", "F1", "F2"),
                new CredibilityObject("F1", "F2", "F3"),
                new CredibilityObject("F2", "F3", "B"));

        assertEquals(expected, graph.getGraph().edgeSet());
    }

    @Test
    public void nonPrioritizedRevisionMoreCredibleObjectMultipleReliabilities() {
        final CredibilityGraph graph = new CredibilityGraph(App.EXAMPLE13);
        assertTrue(graph.nonPrioritizedRevision("L", "H", "G"));

        final Set<CredibilityObject> expected = new HashSet<>();
        Collections.addAll(expected,
                new CredibilityObject("F", "G", "D"),
                new CredibilityObject("J", "K", "E"),
                new CredibilityObject("D", "F", "E"),
                new CredibilityObject("E", "G", "F"),
                new CredibilityObject("H", "J", "G"),
                new CredibilityObject("I", "L", "G"),
                new CredibilityObject("D", "E", "G"),
                new CredibilityObject("L", "H", "G"));

        assertEquals(expected, graph.getGraph().edgeSet());
    }

    @Test
    public void copy() {
        final CredibilityGraph graph1 = new CredibilityGraph(App.EXAMPLE2);
        final CredibilityGraph graph2 = graph1.copy();

        final Set<CredibilityObject> edges1 = graph1.getGraph().edgeSet();
        final Set<CredibilityObject> edges2 = graph2.getGraph().edgeSet();

        assertEquals(edges1, edges2);
        assertNotSame(edges1, edges2);

        final CredibilityObject edge = edges1.iterator().next();
        graph1.getGraph().removeEdge(edge);

        assertFalse(graph1.getGraph().containsEdge(edge));
        assertTrue(graph2.getGraph().containsEdge(edge));
    }

    @Test
    public void buildPathSimple() {
        final CredibilityGraph graph = new CredibilityGraph("(A, B, X), (B, C, Y)");

        final List<String> vertexes = Arrays.asList("A", "B", "C");

        final Set<GraphWalk<String, CredibilityObject>> expected = Collections.singleton(new GraphWalk<>(graph.getGraph(), vertexes, 0));
        final Set<GraphWalk<String, CredibilityObject>> actual = graph.buildPaths(vertexes);
        assertEquals(expected, actual);
    }

    @Test
    public void buildPathComplex() {
        final CredibilityGraph graph = new CredibilityGraph("(A, B, X), (A, B, Y), (B, C, Z), (B, C, X), (B, C, Y)");
        final List<String> vertexes = Arrays.asList("A", "B", "C");

        final AllDirectedPaths<String, CredibilityObject> finder = new AllDirectedPaths<>(graph.getGraph());

        final Set<GraphPath<String, CredibilityObject>> expected = new HashSet<>(finder.getAllPaths(
                "A", "C", false, graph.getGraph().vertexSet().size()));

        final Set<GraphWalk<String, CredibilityObject>> actual = graph.buildPaths(vertexes);
        assertEquals(expected, actual);
    }

    @Test
    public void buildPathCycleSimple() {
        final CredibilityGraph graph = new CredibilityGraph("(A, B, X), (B, A, Y)");
        final List<String> vertexes = Arrays.asList("A", "B", "A");

        final AllDirectedPaths<String, CredibilityObject> finder = new AllDirectedPaths<>(graph.getGraph());

        final Set<GraphPath<String, CredibilityObject>> expected = finder.getAllPaths(
                "A", "A", false, graph.getGraph().vertexSet().size())
                .stream()
                .filter(e -> e.getLength() > 0) // drop paths with length 1
                .collect(Collectors.toSet());

        final Set<GraphWalk<String, CredibilityObject>> actual = graph.buildPaths(vertexes);
        assertEquals(expected, actual);
    }

    @Test
    public void buildPathCycleComplex() {
        final CredibilityGraph graph = new CredibilityGraph("(A, B, X), (A, B, Y), (B, C, X), (B, C, Y), (C, A, Z)");
        final List<String> vertexes = Arrays.asList("A", "B", "C", "A");

        final AllDirectedPaths<String, CredibilityObject> finder = new AllDirectedPaths<>(graph.getGraph());

        final Set<GraphPath<String, CredibilityObject>> expected = finder.getAllPaths(
                "A", "A", false, graph.getGraph().vertexSet().size())
                .stream()
                .filter(e -> e.getLength() > 0) // drop paths with length 1
                .collect(Collectors.toSet());

        final Set<GraphWalk<String, CredibilityObject>> actual = new HashSet<>(graph.buildPaths(vertexes));
        assertEquals(expected, actual);
    }

    @Test
    public void findMinimumCycles() {
        final CredibilityGraph graph = new CredibilityGraph("(A, B, X), (A, B, Y), (B, C, Z), (B, C, W), (C, A, W)");

        final Set<GraphWalk<String, CredibilityObject>> actual = graph.findCycles();
        assertEquals(4, actual.size());

        final AllDirectedPaths<String, CredibilityObject> finder = new AllDirectedPaths<>(graph.getGraph());

        final Set<GraphPath<String, CredibilityObject>> expected = finder.getAllPaths(
                "C", "C", false, graph.getGraph().vertexSet().size())
                .stream()
                .filter(e -> e.getLength() > 0) // drop paths with length 1
                .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }
}
