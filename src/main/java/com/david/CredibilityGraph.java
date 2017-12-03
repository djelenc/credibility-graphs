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
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class CredibilityGraph {

    public enum Extreme {
        MIN, MAX
    }

    protected final Graph<String, CredibilityObject> graph;

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

    protected Graph<String, CredibilityObject> merge(Map<String, Graph<String, DefaultEdge>> graphs) {
        final DirectedMultigraph<String, CredibilityObject> merged = new DirectedMultigraph<>(
                new ClassBasedEdgeFactory<String, CredibilityObject>(CredibilityObject.class));

        for (Map.Entry<String, Graph<String, DefaultEdge>> entry : graphs.entrySet()) {
            final Graph<String, DefaultEdge> graph = entry.getValue();
            final String reporter = entry.getKey();

            for (String vertex : graph.vertexSet()) {
                merged.addVertex(vertex);
            }

            for (DefaultEdge edge : entry.getValue().edgeSet()) {
                final String source = graph.getEdgeSource(edge);
                final String target = graph.getEdgeTarget(edge);
                merged.addEdge(source, target, new CredibilityObject(source, target, reporter));
            }
        }

        return merged;
    }

    /**
     * Exports given graph into DOT file and invokes the dot-parser to create a PNG image
     *
     * @param pathName
     * @throws ExportException
     * @throws IOException
     */
    public void exportDOT(String pathName)
            throws ExportException, IOException {
        final DOTExporter<String, CredibilityObject> exporter = new DOTExporter<>(
                Object::toString,
                null,
                CredibilityObject::getReporter);

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
        final CycleDetector<String, CredibilityObject> cycleDetector = new CycleDetector<>(graph);
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
    protected List<GraphPath<String, CredibilityObject>> findPaths(String sourceVertex, String targetVertex) {
        final AllDirectedPaths<String, CredibilityObject> pathFinder = new AllDirectedPaths<>(graph);
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
        final AllDirectedPaths<String, CredibilityObject> pathFinder = new AllDirectedPaths<>(graph);
        final List<GraphPath<String, CredibilityObject>> paths = pathFinder.getAllPaths(
                target, source, true, null);

        if (paths.size() == 0) {
            graph.addEdge(source, target, new CredibilityObject(source, target, reporter));
            return true;
        }

        return false;
    }

    /**
     * Find extreme objects [regarding the credibility of reporters] on all
     * paths between source and target.
     *
     * @param source
     * @param target
     * @param type
     * @return
     */
    protected Set<CredibilityObject> getExtremesFromAllPaths(String source, String target, Extreme type) {
        final List<GraphPath<String, CredibilityObject>> paths = findPaths(source, target);
        final Set<CredibilityObject> sources = new HashSet<>();

        for (GraphPath<String, CredibilityObject> path : paths) {
            final List<CredibilityObject> edges = path.getEdgeList();
            final Set<CredibilityObject> candidates = getExtremes(edges, type);
            sources.addAll(candidates);
        }

        return sources;
    }

    protected Set<CredibilityObject> getExtremes(Collection<CredibilityObject> allEdges, Extreme type) {
        final AllDirectedPaths<String, CredibilityObject> finder = new AllDirectedPaths<>(graph);
        final Set<CredibilityObject> filtered = new HashSet<>(allEdges);

        for (CredibilityObject one : allEdges) {
            for (CredibilityObject two : allEdges) {
                if (one.getReporter().equals(two.getReporter())) {
                    continue;
                }

                final List<GraphPath<String, CredibilityObject>> one2two = finder.getAllPaths(
                        one.getReporter(), two.getReporter(), true, null);
                final List<GraphPath<String, CredibilityObject>> two2one = finder.getAllPaths(
                        two.getReporter(), one.getReporter(), true, null);

                if (one2two.isEmpty() && two2one.isEmpty()) {
                    continue;
                }

                if (type == Extreme.MAX) {
                    if (!one2two.isEmpty()) {
                        filtered.remove(one);
                    } else {
                        filtered.remove(two);
                    }
                } else {
                    if (one2two.isEmpty()) {
                        filtered.remove(one);
                    } else {
                        filtered.remove(two);
                    }
                }
            }
        }

        return filtered;
    }

    /**
     * Removes all paths from source to target by removing the minimal number of credibility objects.
     *
     * @param source
     * @param target
     */
    public void reliabilityContraction(String source, String target) {
        final Set<CredibilityObject> toRemove = getExtremesFromAllPaths(source, target, Extreme.MIN);
        graph.removeAllEdges(toRemove);
    }

    public boolean prioritizedRevision(String source, String target, String reporter) {
        reliabilityContraction(target, source);
        return expand(source, target, reporter);
    }

    protected Set<String> reliability(String source, String target) {
        final Set<CredibilityObject> minimalSources = getExtremesFromAllPaths(source, target, Extreme.MIN);
        final Set<CredibilityObject> maximalSources = getExtremes(minimalSources, Extreme.MAX);

        return maximalSources.stream().map(CredibilityObject::getReporter).collect(Collectors.toSet());
    }

    public boolean nonPrioritizedRevision(String source, String target, String reporter) {
        final List<GraphPath<String, CredibilityObject>> paths = findPaths(target, source);

        if (paths.isEmpty()) {
            return expand(source, target, reporter);
        } else {
            final Set<String> reliabilities = reliability(target, source);
            final AllDirectedPaths<String, CredibilityObject> finder = new AllDirectedPaths<>(graph);

            for (String existingReporter : reliabilities) {
                final List<GraphPath<String, CredibilityObject>> existing2reporter = finder.getAllPaths(
                        existingReporter, reporter, true, null);

                if (existing2reporter.isEmpty()) {
                    // an existing reporter is either more credible than or incomparable to reporter
                    return false;
                }
            }

            // all existing reporters are less credible than the new one
            return prioritizedRevision(source, target, reporter);
        }
    }
}
