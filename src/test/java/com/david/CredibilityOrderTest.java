package com.david;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.david.App.OBJECTS_EX2;
import static com.david.CredibilityOrders.merge;
import static com.david.CredibilityOrders.parseObjects;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CredibilityOrderTest {

    private Graph<String, ReporterEdge> graph = null;

    @Before
    public void setUp() {
        final Map<String, Graph<String, DefaultEdge>> graphs = parseObjects(OBJECTS_EX2);
        this.graph = merge(graphs);
    }

    @Test
    public void findPaths() {
        final List<GraphPath<String, ReporterEdge>> paths = CredibilityOrders.findPaths(graph, "A1", "A4");
        assertEquals(paths.size(), 3);
    }

    @Test
    public void expansionSuccess() {
        CredibilityOrders.expand(graph, "A1", "F3", "A2");

        assertTrue(graph.containsEdge("A1", "F3"));
        final ReporterEdge edge = graph.getEdge("A1", "F3");
        assertEquals(edge.getLabel(), "A2");
    }

    @Test
    public void expansionFailure() {
        CredibilityOrders.expand(graph, "A4", "A1", "A2");
        assertFalse(graph.containsEdge("A4", "A1"));
    }

    @Test
    public void minimalSources() {
        final Set<ReporterEdge> actual = CredibilityOrders.minimalSources(graph, "A1", "A4");

        final Set<ReporterEdge> expected = new HashSet<>();
        Collections.addAll(expected,
                new ReporterEdge("A1", "A2", "F1"),
                new ReporterEdge("A2", "A4", "B"),
                new ReporterEdge("A1", "A3", "F1"));

        assertEquals(expected, actual);
    }

    @Test
    public void minimalSourcesIncomparable() {
        final String input = "(4,5,A),(1,5,A),(2,5,A),(3,5,A),(4,5,A),(A,B,1),(A,C,5),(B,D,4),(C,D,3)";
        final Graph<String, ReporterEdge> graph = merge(parseObjects(input));

        final Set<ReporterEdge> actual = CredibilityOrders.minimalSources(graph, "A", "D");

        final Set<ReporterEdge> expected = new HashSet<>();
        Collections.addAll(expected,
                new ReporterEdge("A", "B", "1"),
                new ReporterEdge("B", "D", "4"),
                new ReporterEdge("C", "D", "3"));

        assertEquals(expected, actual);
    }

    @Test
    public void reliabilityContraction() {
        final AllDirectedPaths<String, ReporterEdge> finder = new AllDirectedPaths<>(graph);
        final List<GraphPath<String, ReporterEdge>> pathsBefore = CredibilityOrders.findPaths(graph, "A1", "A4");
        assertEquals(3, pathsBefore.size());
        assertEquals(8, graph.edgeSet().size());

        CredibilityOrders.reliabilityContraction(graph, "A1", "A4");

        final List<GraphPath<String, ReporterEdge>> pathsAfter = CredibilityOrders.findPaths(graph, "A1", "A4");
        assertEquals(0, pathsAfter.size());
        assertEquals(5, graph.edgeSet().size());
    }
}
