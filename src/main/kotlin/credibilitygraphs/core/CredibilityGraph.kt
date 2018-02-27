package credibilitygraphs.core

import credibilitygraphs.parser.GraphLexer
import credibilitygraphs.parser.GraphParser
import credibilitygraphs.parser.Visitor
import guru.nidi.graphviz.attribute.RankDir
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.parse.Parser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.CommonTokenStream
import org.jgrapht.Graph
import org.jgrapht.GraphPath
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles
import org.jgrapht.alg.shortestpath.AllDirectedPaths
import org.jgrapht.graph.DirectedMultigraph
import org.jgrapht.graph.GraphWalk
import org.jgrapht.io.ComponentNameProvider
import org.jgrapht.io.DOTExporter
import org.jgrapht.io.ExportException
import org.jgrapht.io.GraphMLExporter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import java.util.stream.Collectors


class CredibilityGraph(val graph: Graph<String, CredibilityObject>) {

    constructor(credibilityObjects: String) : this(parseObjects(credibilityObjects))

    enum class Extreme {
        MIN, MAX
    }

    companion object {
        /**
         * Parses given string of credibility objects and returns a directed multi graph
         *
         * @param graph
         * @return directed multi-graph
         */
        fun parseObjects(graph: String): Graph<String, CredibilityObject> {
            val ais = ANTLRInputStream(graph)
            val lexer = GraphLexer(ais)
            val tokens = CommonTokenStream(lexer)
            val parser = GraphParser(tokens)
            val tree = parser.stat()
            val v = Visitor()
            return v.visit(tree)
        }
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
    @Throws(IOException::class)
    fun exportDOT(fileName: String, format: Format, edgeLabels: Boolean) {
        val exporter = DOTExporter<String, CredibilityObject>(
                ComponentNameProvider<String> { it.toString() }, null,
                if (edgeLabels) ComponentNameProvider<CredibilityObject> { it.reporter } else null)

        val stream = ByteArrayOutputStream()
        try {
            exporter.exportGraph(graph, stream)
        } catch (e: ExportException) {
            throw IOException(e)
        }

        val `is` = ByteArrayInputStream(stream.toByteArray())
        val mutableGraph = Parser.read(`is`)
        mutableGraph.generalAttrs().add(RankDir.BOTTOM_TO_TOP)
        Graphviz.fromGraph(mutableGraph)
                .render(format)
                .toFile(File(fileName + "." + format.name.toLowerCase()))
    }

    /**
     * Exports given graph into GraphML format and saves it into file with
     * .graphml extension.
     *
     * @param fileName
     * @throws IOException
     */
    @Throws(IOException::class)
    fun exportGraphML(fileName: String) {
        val exporter = GraphMLExporter<String, CredibilityObject>()

        exporter.setVertexIDProvider(ComponentNameProvider<String> { it.toString() })
        exporter.setVertexLabelProvider(ComponentNameProvider<String> { it.toString() })

        exporter.setEdgeLabelProvider(ComponentNameProvider<CredibilityObject> { it.reporter })
        exporter.setEdgeIDProvider { e -> e.hashCode().toString() }

        exporter.vertexLabelAttributeName = "Text"
        exporter.edgeLabelAttributeName = "Text"

        try {
            exporter.exportGraph(graph, File(fileName + ".graphml"))
        } catch (e: ExportException) {
            throw IOException(e)
        }

    }

    /**
     * Finds all paths between given source and target vertex
     *
     * @param sourceVertex
     * @param targetVertex
     * @return
     */
    protected fun findPaths(sourceVertex: String, targetVertex: String): List<GraphPath<String, CredibilityObject>> =
            AllDirectedPaths(graph).getAllPaths(sourceVertex, targetVertex, false, graph.edgeSet().size)

    /**
     * Expands the graph by adding a new edge from source to target that is provided by the reporter.
     * Returns true on success, false otherwise.
     *
     * @param source
     * @param target
     * @param reporter
     * @return
     */
    fun expansion(source: String, target: String, reporter: String): Boolean {
        if (graph.containsVertex(source) && graph.containsVertex(target)) {
            val reversePathExists = AllDirectedPaths(graph)
                    .getAllPaths(target, source, true, null)
                    .isEmpty()
                    .not()

            if (reversePathExists) {
                return false
            }
        }

        graph.addVertex(source)
        graph.addVertex(target)
        return graph.addEdge(source, target, CredibilityObject(source, target, reporter))
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
    protected fun getExtremesFromAllPaths(source: String, target: String, type: Extreme): Set<CredibilityObject> {
        val paths = findPaths(source, target)
        val sources = HashSet<CredibilityObject>()

        for (path in paths) {
            val edges = path.edgeList
            val candidates = getExtremes(edges, type)
            sources.addAll(candidates)
        }

        return sources
    }


    /**
     * Find extremes, defined in extreme, in a collection of CredibilityObjects
     *
     * @param allEdges
     * @param extreme
     * @return
     */
    protected fun getExtremes(allEdges: Collection<CredibilityObject>, extreme: Extreme) =
            getExtremes(allEdges, extreme, this)

    /**
     * Find extremes, defined in type, in a collection of CredibilityObjects,
     * using CredibilityGraph to measure reliability
     *
     * @param allEdges
     * @param type
     * @param graph
     * @return
     */
    protected fun getExtremes(allEdges: Collection<CredibilityObject>, type: Extreme,
                              graph: CredibilityGraph): Set<CredibilityObject> {
        val finder = AllDirectedPaths(graph.graph)
        val filtered = HashSet(allEdges)

        for (one in allEdges) {
            for (two in allEdges) {
                if (one.reporter == two.reporter) {
                    continue
                }

                val one2two = finder.getAllPaths(
                        one.reporter, two.reporter, true, null)
                val two2one = finder.getAllPaths(
                        two.reporter, one.reporter, true, null)

                if (one2two.isEmpty() && two2one.isEmpty()) {
                    continue
                }

                if (type == Extreme.MAX) {
                    if (!one2two.isEmpty()) {
                        filtered.remove(one)
                    } else {
                        filtered.remove(two)
                    }
                } else {
                    if (one2two.isEmpty()) {
                        filtered.remove(one)
                    } else {
                        filtered.remove(two)
                    }
                }
            }
        }

        return filtered
    }

    /**
     * Removes all paths from source to target by removing the minimal number of credibility objects.
     *
     * @param source
     * @param target
     */
    fun contraction(source: String, target: String) {
        val toRemove = getExtremesFromAllPaths(source, target, Extreme.MIN)
        graph.removeAllEdges(toRemove)
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
    fun prioritizedRevision(source: String, target: String, reporter: String): Boolean {
        contraction(target, source)
        return expansion(source, target, reporter)
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
    protected fun reliability(source: String, target: String): Set<String> {
        val minimalSources = getExtremesFromAllPaths(source, target, Extreme.MIN)
        val maximalSources = getExtremes(minimalSources, Extreme.MAX)

        return maximalSources.map { it.reporter }.toSet()
        // return maximalSources.stream().map<String>(Function<CredibilityObject, String> { it.getReporter() }).collect<Set<String>, Any>(Collectors.toSet())
    }

    /**
     * Revise the knowledge-base by trying to add given credibility object.
     *
     *
     * The revision succeeds iff:
     *  * it does not contradict current knowledge-base, or
     *  * it contradicts the current knowledge-base but the reliability of the
     * new information is higher than the one in the knowledge-base.
     *
     *
     * @param source
     * @param target
     * @param reporter
     * @return
     */
    fun nonPrioritizedRevision(source: String, target: String, reporter: String): Boolean {
        val paths = findPaths(target, source)

        if (paths.isEmpty()) {
            return expansion(source, target, reporter)
        } else {
            val reliabilities = reliability(target, source)
            val finder = AllDirectedPaths(graph)

            for (existingReporter in reliabilities) {
                val existing2reporter = finder.getAllPaths(
                        existingReporter, reporter, true, null)

                if (existing2reporter.isEmpty()) {
                    // an existing reporter is either more credible than or incomparable to reporter
                    return false
                }
            }

            // all existing reporters are less credible than the new one
            return prioritizedRevision(source, target, reporter)
        }
    }

    /**
     * Merge the current knowledge-base with provided one.
     *
     * @param input
     */
    fun merge(input: CredibilityGraph) {
        // make a backup for resolving cycles later
        val old = copy()

        // copy all vertices and edges from input into this graph
        input.graph.edgeSet().forEach { edge ->
            this.graph.addVertex(edge.src)
            this.graph.addVertex(edge.tgt)
            this.graph.addEdge(edge.src, edge.tgt, edge)
        }

        // in every cycle, remove the least reliable edge
        findCycles().forEach { cycle ->
            val leastReliable = getExtremes(cycle.edgeList, Extreme.MIN, old)
            graph.removeAllEdges(leastReliable)
        }
    }

    /**
     * Creates a copy of this CredibilityGraph
     *
     * @return
     */
    fun copy(): CredibilityGraph {
        val newGraph = DirectedMultigraph<String, CredibilityObject>(CredibilityObject::class.java)

        graph.edgeSet().forEach { edge ->
            newGraph.addVertex(edge.src)
            newGraph.addVertex(edge.tgt)
            newGraph.addEdge(edge.src, edge.tgt,
                    CredibilityObject(edge.src, edge.tgt, edge.reporter))
        }

        return CredibilityGraph(newGraph)
    }

    /**
     * Finds all minimal cycles in current knowledge-base
     *
     * @return
     */
    protected fun findCycles(): Set<GraphWalk<String, CredibilityObject>> {
        val algorithm = HawickJamesSimpleCycles(graph)
        return algorithm.findSimpleCycles().stream()
                .map { vertexes ->
                    vertexes.add(vertexes[0]) // connect the cycle
                    vertexes.reverse() // cycles are found in reverse order
                    buildPaths(vertexes)
                }
                .flatMap { it.stream() }
                .collect(Collectors.toSet())
    }

    /**
     * Builds a set of GraphWalks from a list of vertexes representing a cycle
     *
     * @param vertexes
     * @return
     */
    protected fun buildPaths(vertexes: List<String>): Set<GraphWalk<String, CredibilityObject>> {
        val source = vertexes[0]

        if (vertexes.size == 1) {
            return setOf(GraphWalk.singletonWalk(graph, source))
        }

        val allPaths = HashSet<GraphWalk<String, CredibilityObject>>()

        val target = vertexes[1]

        val edges = graph.getAllEdges(source, target)

        for (edge in edges) {
            val step = GraphWalk(graph, source, target, listOf(edge), 0.0)
            val nextPaths = buildPaths(vertexes.subList(1, vertexes.size))

            for (path in nextPaths) {
                val full = step.concat(path) { _ -> 0.0 }
                allPaths.add(full)
            }
        }

        return allPaths
    }
}