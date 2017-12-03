package com.david;

import org.jgrapht.GraphPath;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.david.App.EXAMPLE13;
import static com.david.App.EXAMPLE2;
import static org.junit.Assert.*;

public class CredibilityGraphTest {

    private CredibilityGraph graph = null;

    @Before
    public void setUp() {
        graph = new CredibilityGraph(EXAMPLE2);
    }

    @Test
    public void findPaths() {
        final List<GraphPath<String, ReporterEdge>> paths = graph.findPaths("A1", "A4");
        assertEquals(3, paths.size());
    }

    @Test
    public void expansionSuccess() {
        assertTrue(graph.expand("A1", "F3", "A2"));
        assertTrue(graph.graph.containsEdge("A1", "F3"));
        assertEquals("A2", graph.graph.getEdge("A1", "F3").getLabel());
    }

    @Test
    public void expansionFailure() {
        assertFalse(graph.expand("A4", "A1", "A2"));
        assertFalse(graph.graph.containsEdge("A4", "A1"));
    }

    @Test
    public void minimalSources() {
        final Set<ReporterEdge> expected = new HashSet<>();
        Collections.addAll(expected,
                new ReporterEdge("A1", "A2", "F1"),
                new ReporterEdge("A2", "A4", "B"),
                new ReporterEdge("A1", "A3", "F1"));

        assertEquals(expected, graph.getExtremesFromAllPaths("A1", "A4", CredibilityGraph.Extreme.MIN));
    }

    @Test
    public void minimalSourcesIncomparable() {
        final String input = "(4,5,A),(1,5,A),(2,5,A),(3,5,A),(4,5,A),(A,B,1),(A,C,5),(B,D,4),(C,D,3)";
        final CredibilityGraph graph = new CredibilityGraph(input);

        final Set<ReporterEdge> expected = new HashSet<>();
        Collections.addAll(expected,
                new ReporterEdge("A", "B", "1"),
                new ReporterEdge("B", "D", "4"),
                new ReporterEdge("C", "D", "3"));

        assertEquals(expected, graph.getExtremesFromAllPaths("A", "D", CredibilityGraph.Extreme.MIN));
    }

    @Test
    public void reliabilityContraction() {
        final List<GraphPath<String, ReporterEdge>> pathsBefore = graph.findPaths("A1", "A4");
        assertEquals(3, pathsBefore.size());
        assertEquals(8, graph.graph.edgeSet().size());

        graph.reliabilityContraction("A1", "A4");

        final List<GraphPath<String, ReporterEdge>> pathsAfter = graph.findPaths("A1", "A4");
        assertEquals(0, pathsAfter.size());
        assertEquals(5, graph.graph.edgeSet().size());
    }

    @Test
    public void prioritizedRevision() {
        final List<GraphPath<String, ReporterEdge>> pathsBefore = graph.findPaths("A2", "A1");
        assertEquals(0, pathsBefore.size());
        assertEquals(8, graph.graph.edgeSet().size());

        graph.prioritizedRevision("A4", "A1", "F3");

        final List<GraphPath<String, ReporterEdge>> pathsAfter = graph.findPaths("A2", "A1");
        assertEquals(1, pathsAfter.size());
        assertEquals(6, graph.graph.edgeSet().size());
    }

    @Test
    public void prioritizedRevisionNoRemoval() {
        final List<GraphPath<String, ReporterEdge>> pathsBefore = graph.findPaths("A1", "A4");
        assertEquals(3, pathsBefore.size());
        assertEquals(8, graph.graph.edgeSet().size());

        graph.prioritizedRevision("A1", "A4", "F3");

        final List<GraphPath<String, ReporterEdge>> pathsAfter = graph.findPaths("A1", "A4");
        assertEquals(4, pathsAfter.size());
        assertEquals(9, graph.graph.edgeSet().size());
    }

    @Test
    public void maximalSources() {
        final Set<ReporterEdge> expected = new HashSet<>();
        Collections.addAll(expected,
                new ReporterEdge("A1", "A2", "F1"),
                new ReporterEdge("A2", "A4", "F3"),
                new ReporterEdge("A3", "A4", "F2"));

        assertEquals(expected, graph.getExtremesFromAllPaths("A1", "A4", CredibilityGraph.Extreme.MAX));
    }

    @Test
    public void priority() {
        final Set<String> expected = new HashSet<>();
        expected.add("F1");
        assertEquals(expected, graph.reliability("A1", "A4"));
    }

    @Test
    public void nonPrioritizedRevisionSimpleExpansion() {
        graph.nonPrioritizedRevision("A4", "F3", "B");

        final List<GraphPath<String, ReporterEdge>> pathsAfter = graph.findPaths("A4", "F3");
        assertEquals(1, pathsAfter.size());
        assertEquals(9, graph.graph.edgeSet().size());
    }

    @Test
    public void nonPrioritizedRevisionRejection() {
        final List<GraphPath<String, ReporterEdge>> pathsBefore = graph.findPaths("A4", "A1");

        graph.nonPrioritizedRevision("A4", "A1", "B");

        assertEquals(pathsBefore, graph.findPaths("A4", "A1"));
    }

    @Test
    public void nonPrioritizedRevisionMoreCredibleObject() {
        graph.nonPrioritizedRevision("A4", "A1", "F3");

        final Set<ReporterEdge> expected = new HashSet<>();
        Collections.addAll(expected,
                new ReporterEdge("A2", "A4", "F3"),
                new ReporterEdge("A4", "A1", "F3"),
                new ReporterEdge("A3", "A4", "F2"),
                new ReporterEdge("B", "F1", "F2"),
                new ReporterEdge("F1", "F2", "F3"),
                new ReporterEdge("F2", "F3", "B"));

        assertEquals(expected, graph.graph.edgeSet());
    }

    @Test
    public void nonPrioritizedRevisionMoreCredibleObjectMultipleReliabilities() {
        final CredibilityGraph graph = new CredibilityGraph(EXAMPLE13);
        graph.nonPrioritizedRevision("L", "H", "G");

        final Set<ReporterEdge> expected = new HashSet<>();
        Collections.addAll(expected,
                new ReporterEdge("F", "G", "D"),
                new ReporterEdge("J", "K", "E"),
                new ReporterEdge("D", "F", "E"),
                new ReporterEdge("E", "G", "F"),
                new ReporterEdge("H", "J", "G"),
                new ReporterEdge("I", "L", "G"),
                new ReporterEdge("D", "E", "G"),
                new ReporterEdge("L", "H", "G"));

        assertEquals(expected, graph.graph.edgeSet());
    }
}
