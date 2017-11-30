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
import org.jgrapht.io.StringComponentNameProvider;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CredibilityOrders {

    private static Map<String, Graph<String, DefaultEdge>> parseStringGraph(String graph) {
        final ANTLRInputStream ais = new ANTLRInputStream(graph);
        final GraphLexer lexer = new GraphLexer(ais);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final GraphParser parser = new GraphParser(tokens);
        final ParseTree tree = parser.stat();
        final Visitor v = new Visitor();
        return v.visit(tree);
    }

    public static Graph<String, DefaultEdge> parseTuples(String tuples) {
        final Map<String, Graph<String, DefaultEdge>> graphs = parseStringGraph(tuples);

        if (graphs.size() == 1 && graphs.containsKey("")) {
            return graphs.get("");
        }

        throw new IllegalArgumentException("Invalid tuples: " + tuples);
    }

    public static Map<String, Graph<String, DefaultEdge>> parseObjects(String objects) {
        final Map<String, Graph<String, DefaultEdge>> graphs = parseStringGraph(objects);

        if (!graphs.containsKey("")) {
            return graphs;
        }

        throw new IllegalArgumentException("Invalid objects: " + objects);
    }

    public static Graph<String, ReporterEdge> merge(Map<String, Graph<String, DefaultEdge>> graphs) {
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

    public static <Vertex, Edge> Map<Vertex, Set<Vertex>> findCycles(Graph<Vertex, Edge> graph) {
        final CycleDetector<Vertex, Edge> cycleDetector = new CycleDetector<>(graph);
        final Map<Vertex, Set<Vertex>> cylceMap = new HashMap<>();

        if (cycleDetector.detectCycles()) {
            final Set<Vertex> cycles = cycleDetector.findCycles();

            for (Vertex cycle : cycles) {
                cylceMap.put(cycle, cycleDetector.findCyclesContainingVertex(cycle));
            }
        }

        return cylceMap;
    }

    /**
     * Finds all paths in given graph between given source and target vertex
     *
     * @param graph
     * @param sourceVertex
     * @param targetVertex
     * @return
     */
    public static List<GraphPath<String, ReporterEdge>> findPaths(Graph<String, ReporterEdge> graph, String sourceVertex, String targetVertex) {
        final AllDirectedPaths<String, ReporterEdge> pathFinder = new AllDirectedPaths<>(graph);
        return pathFinder.getAllPaths(sourceVertex, targetVertex,
                false, graph.edgeSet().size());
    }

    /**
     * Expands given graph by adding a new edge from source to target that is prvided by the reporter
     *
     * @param graph
     * @param source
     * @param target
     * @param reporter
     */
    public static void expand(Graph<String, ReporterEdge> graph, String source, String target, String reporter) {
        final AllDirectedPaths<String, ReporterEdge> pathFinder = new AllDirectedPaths<>(graph);
        final List<GraphPath<String, ReporterEdge>> paths = pathFinder.getAllPaths(target, source, true, null);

        if (paths.size() == 0) {
            graph.addEdge(source, target, new ReporterEdge(source, target, reporter));
        }
    }

    public static void minimalSources(Graph<String, ReporterEdge> graph, String source, String target) {
        final List<GraphPath<String, ReporterEdge>> paths = findPaths(graph, source, target);

        for (GraphPath<String, ReporterEdge> path : paths) {
            System.out.println("Path: " + path);
            final TopologicalOrderIterator<String, ReporterEdge> iterator = new TopologicalOrderIterator<>(path.getGraph());
            iterator.setCrossComponentTraversal(false);

            while (iterator.hasNext()) {
                final String current = iterator.next();
                System.out.print(current + ", ");
            }
            System.out.println();

//            final AllDirectedPaths<String, ReporterEdge> finder = new AllDirectedPaths<>(path.getGraph());
//            final Set<String> reporters = path.getEdgeList().stream().map(ReporterEdge::toString).collect(Collectors.toSet());
//            final List<GraphPath<String, ReporterEdge>> allPaths = finder.getAllPaths(reporters, reporters, true, null);
//            System.out.printf("Path: %s, Reporters: %s, all-paths: %s%n", path, reporters, allPaths);
        }

    }


    /**
     * Exports given graph into DOT file and invokes the dot-parser to create a PNG image
     *
     * @param graph
     * @param pathName
     * @param labels
     * @param <Vertex>
     * @param <Edge>
     * @throws ExportException
     * @throws IOException
     */
    public static <Vertex, Edge> void exportDOT(Graph<Vertex, Edge> graph, String pathName, boolean labels)
            throws ExportException, IOException {
        final DOTExporter<Vertex, Edge> exporter = new DOTExporter<>(
                new StringComponentNameProvider<>(),
                null,
                labels ? new StringComponentNameProvider<>() : null);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exporter.exportGraph(graph, baos);
        final InputStream is = new ByteArrayInputStream(baos.toByteArray());

        final MutableGraph mutableGraph = Parser.read(is);
        mutableGraph.generalAttrs().add(RankDir.BOTTOM_TO_TOP);

        Graphviz.fromGraph(mutableGraph)
                .render(Format.PNG)
                .toFile(new File(pathName));
    }
}
