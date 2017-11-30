package com.david;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.david.App.OBJECTS_EX2;
import static com.david.CredibilityOrders.merge;
import static com.david.CredibilityOrders.parseObjects;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CredibilityOrderTest {

    Graph<String, ReporterEdge> graph = null;

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
        assertEquals(edge.toString(), "A2");
    }

    @Test
    public void expansionFailure() {
        CredibilityOrders.expand(graph, "A4", "A1", "A2");
        assertFalse(graph.containsEdge("A4", "A1"));
    }

    @Test
    public void minimalSources() {
        final Set<ReporterEdge> toRemove = CredibilityOrders.minimalSources(graph, "A1", "A4");
        System.out.println(toRemove.stream()
                .map(e -> String.format("[(%s, %s), %s]", e.getV1(), e.getV2(), e.toString()))
                .collect(Collectors.toSet()));
    }
}
