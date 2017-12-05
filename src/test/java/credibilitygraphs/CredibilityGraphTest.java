package credibilitygraphs;

import org.jgrapht.GraphPath;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        assertTrue(graph.expand("A1", "F3", "A2"));
        assertTrue(graph.graph.containsEdge("A1", "F3"));
        assertEquals("A2", graph.graph.getEdge("A1", "F3").getReporter());
    }

    @Test
    public void expansionFailure() {
        final Set<CredibilityObject> before = graph.graph.edgeSet();
        assertFalse(graph.expand("A4", "A1", "A2"));
        assertEquals(before, graph.graph.edgeSet());
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
        assertEquals(8, graph.graph.edgeSet().size());

        graph.contraction("A1", "A4");

        final List<GraphPath<String, CredibilityObject>> after = graph.findPaths("A1", "A4");
        assertEquals(0, after.size());
        assertEquals(5, graph.graph.edgeSet().size());
    }

    @Test
    public void prioritizedRevision() {
        final List<GraphPath<String, CredibilityObject>> before = graph.findPaths("A2", "A1");
        assertEquals(0, before.size());
        assertEquals(8, graph.graph.edgeSet().size());

        assertTrue(graph.prioritizedRevision("A4", "A1", "F3"));

        final List<GraphPath<String, CredibilityObject>> after = graph.findPaths("A2", "A1");
        assertEquals(1, after.size());
        assertEquals(6, graph.graph.edgeSet().size());
    }

    @Test
    public void prioritizedRevisionNoRemoval() {
        final List<GraphPath<String, CredibilityObject>> before = graph.findPaths("A1", "A4");
        assertEquals(3, before.size());
        assertEquals(8, graph.graph.edgeSet().size());

        assertTrue(graph.prioritizedRevision("A1", "A4", "F3"));

        final List<GraphPath<String, CredibilityObject>> after = graph.findPaths("A1", "A4");
        assertEquals(4, after.size());
        assertEquals(9, graph.graph.edgeSet().size());
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
        assertEquals(9, graph.graph.edgeSet().size());
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

        assertEquals(expected, graph.graph.edgeSet());
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

        assertEquals(expected, graph.graph.edgeSet());
    }
}
