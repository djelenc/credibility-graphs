package com.david;

import com.david.parser.GraphLexer;
import com.david.parser.GraphParser;
import com.david.parser.Visitor;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.StringComponentNameProvider;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.*;

public final class CredibilityGraph {

    public enum Sources {
        MINIMAL, MAXIMAL
    }

    protected final Graph<String, ReporterEdge> graph;

    public CredibilityGraph(String credibilityObjects) {
        graph = merge(parseObjects(credibilityObjects));
    }

    protected Map<String, Graph<String, DefaultEdge>> parseObjects(String objects) {
        final Map<String, Graph<String, DefaultEdge>> graphs = parseStringGraph(objects);

        if (!graphs.containsKey("")) {
            return graphs;
        }

        throw new IllegalArgumentException("Invalid objects: " + objects);
    }

    private static Map<String, Graph<String, DefaultEdge>> parseStringGraph(String graph) {
        final ANTLRInputStream ais = new ANTLRInputStream(graph);
        final GraphLexer lexer = new GraphLexer(ais);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final GraphParser parser = new GraphParser(tokens);
        final ParseTree tree = parser.stat();
        final Visitor v = new Visitor();
        return v.visit(tree);
    }

    protected Graph<String, ReporterEdge> merge(Map<String, Graph<String, DefaultEdge>> graphs) {
        final DirectedMultigraph<String, ReporterEdge> merged = new DirectedMultigraph<>(
                new ClassBasedEdgeFactory<String, ReporterEdge>(ReporterEdge.class));

        for (Map.Entry<String, Graph<String, DefaultEdge>> entry : graphs.entrySet()) {
            final Graph<String, DefaultEdge> graph = entry.getValue();
            final String reporter = entry.getKey();

            for (String vertex : graph.vertexSet()) {
                merged.addVertex(vertex);
            }

            for (DefaultEdge edge : entry.getValue().edgeSet()) {
                final String source = graph.getEdgeSource(edge);
                final String target = graph.getEdgeTarget(edge);
                merged.addEdge(source, target, new ReporterEdge(source, target, reporter));
            }
        }

        return merged;
    }

    /**
     * Exports given graph into DOT file and invokes the dot-parser to create a PNG image
     *
     * @param pathName
     * @param labels   If true, prints edges' labels
     * @throws ExportException
     * @throws IOException
     */
    public void exportDOT(String pathName, boolean labels)
            throws ExportException, IOException {
        final DOTExporter<String, ReporterEdge> exporter = new DOTExporter<>(
                new StringComponentNameProvider<>(),
                null,
                labels ? (ComponentNameProvider<ReporterEdge>) component -> component.getLabel() : null);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.exportGraph(graph, baos);
        final InputStream is = new ByteArrayInputStream(baos.toByteArray());

        final MutableGraph mutableGraph = Parser.read(is);
        mutableGraph.generalAttrs().add(RankDir.BOTTOM_TO_TOP);

        Graphviz.fromGraph(mutableGraph)
                .render(Format.PNG)
                .toFile(new File(pathName));
    }

    protected Map<String, Set<String>> findCycles() {
        final CycleDetector<String, ReporterEdge> cycleDetector = new CycleDetector<>(graph);
        final Map<String, Set<String>> cycleMap = new HashMap<>();

        if (cycleDetector.detectCycles()) {
            final Set<String> cycles = cycleDetector.findCycles();

            for (String cycle : cycles) {
                cycleMap.put(cycle, cycleDetector.findCyclesContainingVertex(cycle));
            }
        }

        return cycleMap;
    }

    /**
     * Finds all paths in given graph between given source and target vertex
     *
     * @param sourceVertex
     * @param targetVertex
     * @return
     */
    protected List<GraphPath<String, ReporterEdge>> findPaths(String sourceVertex, String targetVertex) {
        final AllDirectedPaths<String, ReporterEdge> pathFinder = new AllDirectedPaths<>(graph);
        return pathFinder.getAllPaths(sourceVertex, targetVertex,
                false, graph.edgeSet().size());
    }

    /**
     * Expands the graph by adding a new edge from source to target that is provided by the reporter.
     * Returns true on success, false otherwise.
     *
     * @param source
     * @param target
     * @param reporter
     * @return
     */
    public boolean expand(String source, String target, String reporter) {
        final AllDirectedPaths<String, ReporterEdge> pathFinder = new AllDirectedPaths<>(graph);
        final List<GraphPath<String, ReporterEdge>> paths = pathFinder.getAllPaths(
                target, source, true, null);

        if (paths.size() == 0) {
            graph.addEdge(source, target, new ReporterEdge(source, target, reporter));
            return true;
        }

        return false;
    }

    /**
     * ind all paths between source and target and then for each path return those edges
     * (credibility objects) whose credibility is the largest/lowest (depends on type).
     *
     * @param source
     * @param target
     * @param type
     * @return
     */
    protected Set<ReporterEdge> getSources(String source, String target, Sources type) {
        final List<GraphPath<String, ReporterEdge>> paths = findPaths(source, target);

        final AllDirectedPaths<String, ReporterEdge> finder = new AllDirectedPaths<>(graph);

        final Set<ReporterEdge> sources = new HashSet<>();

        for (GraphPath<String, ReporterEdge> path : paths) {
            final List<ReporterEdge> edges = path.getEdgeList();
            final Set<ReporterEdge> candidates = new HashSet<>(edges);

            for (ReporterEdge reporterOne : edges) {
                for (ReporterEdge reporterTwo : edges) {
                    if (reporterOne.getLabel().equals(reporterTwo.getLabel())) {
                        continue;
                    }

                    final List<GraphPath<String, ReporterEdge>> one2two = finder.getAllPaths(
                            reporterOne.getLabel(), reporterTwo.getLabel(), true, null);
                    final List<GraphPath<String, ReporterEdge>> two2one = finder.getAllPaths(
                            reporterTwo.getLabel(), reporterOne.getLabel(), true, null);

                    if (one2two.isEmpty() && two2one.isEmpty()) {
                        continue;
                    }

                    if (type == Sources.MAXIMAL) {
                        if (!one2two.isEmpty()) {
                            candidates.remove(reporterOne);
                        } else {
                            candidates.remove(reporterTwo);
                        }
                    } else {
                        if (one2two.isEmpty()) {
                            candidates.remove(reporterOne);
                        } else {
                            candidates.remove(reporterTwo);
                        }
                    }
                }
            }

            sources.addAll(candidates);
        }

        return sources;
    }

    /**
     * Removes all paths from source to target by removing the minimal number of credibility objects.
     *
     * @param source
     * @param target
     */
    public void reliabilityContraction(String source, String target) {
        final Set<ReporterEdge> toRemove = getSources(source, target, Sources.MINIMAL);
        graph.removeAllEdges(toRemove);
    }

    public void prioritizedRevision(String source, String target, String reporter) {
        reliabilityContraction(target, source);
        expand(source, target, reporter);
    }

    public void nonPrioritizedRevision(String source, String target, String reporter) {
        final List<GraphPath<String, ReporterEdge>> paths = findPaths(target, source);

        if (paths.isEmpty()) {
            expand(source, target, reporter);
        } else {
            throw new NotImplementedException();
        }
    }
}
