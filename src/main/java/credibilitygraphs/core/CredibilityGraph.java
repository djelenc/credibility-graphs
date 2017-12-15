package credibilitygraphs.core;

import credibilitygraphs.parser.GraphLexer;
import credibilitygraphs.parser.GraphParser;
import credibilitygraphs.parser.Visitor;
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
import org.jgrapht.alg.cycle.DirectedSimpleCycles;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CredibilityGraph {

    public enum Extreme {
        MIN, MAX
    }

    public final Graph<String, CredibilityObject> graph;

    private CredibilityGraph(Graph<String, CredibilityObject> graph) {
        this.graph = graph;
    }

    public CredibilityGraph(String credibilityObjects) {
        graph = parseObjects(credibilityObjects);
    }

    /**
     * Parses given string of credibility objects and returns a directed multi graph
     *
     * @param graph
     * @return
     */
    protected Graph<String, CredibilityObject> parseObjects(String graph) {
        final ANTLRInputStream ais = new ANTLRInputStream(graph);
        final GraphLexer lexer = new GraphLexer(ais);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final GraphParser parser = new GraphParser(tokens);
        final ParseTree tree = parser.stat();
        final Visitor v = new Visitor();
        return v.visit(tree);
    }

    /**
     * Exports given graph into DOT format and saves it into file using given format.
     * The DOT exporter requires the graphviz be installed.
     *
     * @param fileName
     * @param format
     * @param edgeLabels include label in edges
     * @throws ExportException
     * @throws IOException
     */
    public void exportDOT(String fileName, Format format, boolean edgeLabels)
            throws IOException {
        final DOTExporter<String, CredibilityObject> exporter = new DOTExporter<>(
                Object::toString,
                null,
                edgeLabels ? CredibilityObject::getReporter : null);

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            exporter.exportGraph(graph, stream);
        } catch (ExportException e) {
            throw new IOException(e);
        }

        final InputStream is = new ByteArrayInputStream(stream.toByteArray());
        final MutableGraph mutableGraph = Parser.read(is);
        mutableGraph.generalAttrs().add(RankDir.BOTTOM_TO_TOP);
        Graphviz.fromGraph(mutableGraph)
                .render(format)
                .toFile(new File(fileName + "." + format.name().toLowerCase()));
    }

    /**
     * Exports given graph into GraphML format and saves it into file with
     * .graphml extension.
     *
     * @param fileName
     * @throws IOException
     */
    public void exportGraphML(String fileName) throws IOException {
        final GraphMLExporter<String, CredibilityObject> exporter = new GraphMLExporter<>();

        exporter.setVertexIDProvider(Object::toString);
        exporter.setVertexLabelProvider(Object::toString);

        exporter.setEdgeLabelProvider(CredibilityObject::getReporter);
        exporter.setEdgeIDProvider(e -> String.valueOf(e.hashCode()));

        exporter.setVertexLabelAttributeName("Text");
        exporter.setEdgeLabelAttributeName("Text");

        try {
            exporter.exportGraph(graph, new File(fileName + ".graphml"));
        } catch (ExportException e) {
            throw new IOException(e);
        }

    }

    /**
     * Finds all paths between given source and target vertex
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
    public boolean expansion(String source, String target, String reporter) {
        if (graph.containsVertex(source) && graph.containsVertex(target)) {
            final AllDirectedPaths<String, CredibilityObject> pathFinder = new AllDirectedPaths<>(graph);
            final List<GraphPath<String, CredibilityObject>> fromTarget2Source = pathFinder.getAllPaths(
                    target, source, true, null);

            if (fromTarget2Source.size() != 0) {
                return false;
            }
        }

        graph.addVertex(source);
        graph.addVertex(target);
        return graph.addEdge(source, target, new CredibilityObject(source, target, reporter));
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


    /**
     * Find extremes, defined in type, in a collection of CredibilityObjects
     *
     * @param allEdges
     * @param type
     * @return
     */
    protected Set<CredibilityObject> getExtremes(Collection<CredibilityObject> allEdges, Extreme type) {
        return getExtremes(allEdges, type, this);
    }

    /**
     * Find extremes, defined in type, in a collection of CredibilityObjects,
     * using CredibilityGraph to measure reliability
     *
     * @param allEdges
     * @param type
     * @param graph
     * @return
     */
    protected Set<CredibilityObject> getExtremes(Collection<CredibilityObject> allEdges, Extreme type,
                                                 CredibilityGraph graph) {
        final AllDirectedPaths<String, CredibilityObject> finder = new AllDirectedPaths<>(graph.graph);
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
    public void contraction(String source, String target) {
        final Set<CredibilityObject> toRemove = getExtremesFromAllPaths(source, target, Extreme.MIN);
        graph.removeAllEdges(toRemove);
    }

    /**
     * Add the provided credibility tuple to the knowledge-base, and assure the latter
     * is consistent
     *
     * @param source
     * @param target
     * @param reporter
     * @return
     */
    public boolean prioritizedRevision(String source, String target, String reporter) {
        contraction(target, source);
        return expansion(source, target, reporter);
    }

    /**
     * Estimate th reliability of source being less credible than the target.
     * The reliability is denoted with the set of least reliable reporters
     * that claim the source is less credible than the target.
     *
     * @param source
     * @param target
     * @return
     */
    protected Set<String> reliability(String source, String target) {
        final Set<CredibilityObject> minimalSources = getExtremesFromAllPaths(source, target, Extreme.MIN);
        final Set<CredibilityObject> maximalSources = getExtremes(minimalSources, Extreme.MAX);

        return maximalSources.stream().map(CredibilityObject::getReporter).collect(Collectors.toSet());
    }

    /**
     * Revise the knowledge-base by trying to add given credibility object.
     * <p>
     * The revision succeeds iff:</ul>
     * <li>it does not contradict current knowledge-base, or</li>
     * <li>it contradicts the current knowledge-base but the reliability of the
     * new information is higher than the one in the knowledge-base.</li>
     * </ul>
     *
     * @param source
     * @param target
     * @param reporter
     * @return
     */
    public boolean nonPrioritizedRevision(String source, String target, String reporter) {
        final List<GraphPath<String, CredibilityObject>> paths = findPaths(target, source);

        if (paths.isEmpty()) {
            return expansion(source, target, reporter);
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

    /**
     * Merge the current knowledge-base with provided one.
     *
     * @param input
     */
    public void merge(CredibilityGraph input) {
        // make a backup for resolving cycles later
        final CredibilityGraph old = copy();

        // copy all vertices and edges from input into this graph
        input.graph.edgeSet().forEach(edge -> {
            this.graph.addVertex(edge.getSrc());
            this.graph.addVertex(edge.getTgt());
            this.graph.addEdge(edge.getSrc(), edge.getTgt(), edge);
        });

        // in every cycle, remove the least reliable edge
        findCycles().forEach(cycle -> {
            final Set<CredibilityObject> leastReliable = getExtremes(cycle.getEdgeList(), Extreme.MIN, old);
            graph.removeAllEdges(leastReliable);
        });
    }

    /**
     * Creates a copy of this CredibilityGraph
     *
     * @return
     */
    public CredibilityGraph copy() {
        final Graph<String, CredibilityObject> newGraph = new DirectedMultigraph<>(CredibilityObject.class);

        graph.edgeSet().forEach(edge -> {
            newGraph.addVertex(edge.getSrc());
            newGraph.addVertex(edge.getTgt());
            newGraph.addEdge(edge.getSrc(), edge.getTgt(),
                    new CredibilityObject(edge.getSrc(), edge.getTgt(), edge.getReporter()));
        });

        return new CredibilityGraph(newGraph);
    }

    /**
     * Finds all minimal cycles in current knowledge-base
     *
     * @return
     */
    protected Set<GraphWalk<String, CredibilityObject>> findCycles() {
        final DirectedSimpleCycles<String, CredibilityObject> algorithm = new HawickJamesSimpleCycles<>(graph);
        return algorithm.findSimpleCycles().stream()
                .map(vertexes -> {
                    vertexes.add(vertexes.get(0)); // connect the cycle
                    Collections.reverse(vertexes); // cycles are found in reverse order
                    return buildPaths(vertexes);
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Builds a set of GraphWalks from a list of vertexes representing a cycle
     *
     * @param vertexes
     * @return
     */
    protected Set<GraphWalk<String, CredibilityObject>> buildPaths(List<String> vertexes) {
        final String source = vertexes.get(0);

        if (vertexes.size() == 1) {
            return Collections.singleton(GraphWalk.singletonWalk(graph, source));
        }

        final Set<GraphWalk<String, CredibilityObject>> allPaths = new HashSet<>();

        final String target = vertexes.get(1);

        final Set<CredibilityObject> edges = graph.getAllEdges(source, target);

        for (CredibilityObject edge : edges) {
            final GraphWalk<String, CredibilityObject> step = new GraphWalk<>(
                    graph, source, target, Collections.singletonList(edge), 0);
            final Set<GraphWalk<String, CredibilityObject>> nextPaths =
                    buildPaths(vertexes.subList(1, vertexes.size()));

            for (GraphWalk<String, CredibilityObject> path : nextPaths) {
                final GraphWalk<String, CredibilityObject> full = step.concat(path, e -> 0d);
                allPaths.add(full);
            }
        }

        return allPaths;
    }

    protected Stream<GraphWalk<String, CredibilityObject>> _buildPaths(List<String> vertexes) {
        final String source = vertexes.get(0);

        if (vertexes.size() == 1) {
            return Stream.of(GraphWalk.singletonWalk(graph, source));
        }

        final String target = vertexes.get(1);

        return graph.getAllEdges(source, target).stream().map(edge -> {
            final GraphWalk<String, CredibilityObject> step =
                    new GraphWalk<>(graph, source, target, Collections.singletonList(edge), 0);

            final Stream<GraphWalk<String, CredibilityObject>> nextPaths =
                    _buildPaths(vertexes.subList(1, vertexes.size()));

            return nextPaths.map(path -> {
                final GraphWalk<String, CredibilityObject> full = step.concat(path, e -> 0d);
                System.out.printf("Edge(%s), Vertexes(%s) => path: %s%n", edge, vertexes, full);
                return full;
            });
        }).flatMap(e -> e);
    }
}