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
import org.jgrapht.graph.DefaultEdge
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

/**
 * Represents a credibility object with source, target and a reporter.
 *
 * In a credibility graph, the credibility object is effectively an edge between
 * source and target with the reporter being its label.
 */
data class CredibilityObject(val source: String, val target: String, val reporter: String) : DefaultEdge() {
    override fun toString() = "$reporter ($source-$target)"
}

enum class Extreme {
    MIN, MAX
}

enum class Comparison {
    LESS, MORE, INCOMPARABLE
}


/**
 * Represents a knowledge-base of credibility objects
 */
class CredibilityGraph(val graph: Graph<String, CredibilityObject>) {
    // allows finding paths between graph nodes
    private val finder = AllDirectedPaths(graph)

    constructor(credibilityObjects: String) : this(parseObjects(credibilityObjects))

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
    fun exportDOT(fileName: String, format: Format, edgeLabels: Boolean = true) {
        val exporter = DOTExporter<String, CredibilityObject>(
                ComponentNameProvider<String> { it.toString() }, null,
                if (edgeLabels) ComponentNameProvider<CredibilityObject> { it.reporter } else null)

        val stream = ByteArrayOutputStream()
        try {
            exporter.exportGraph(graph, stream)
        } catch (e: ExportException) {
            throw IOException(e)
        }

        val mutableGraph = Parser.read(ByteArrayInputStream(stream.toByteArray()))
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
        val exporter = GraphMLExporter<String, CredibilityObject>().apply {
            setVertexIDProvider({ it.toString() })
            setVertexLabelProvider({ it.toString() })
            setEdgeLabelProvider({ it.reporter })
            setEdgeIDProvider { it.hashCode().toString() }
            vertexLabelAttributeName = "Text"
            edgeLabelAttributeName = "Text"
        }

        try {
            exporter.exportGraph(graph, File(fileName + ".graphml"))
        } catch (e: ExportException) {
            throw IOException(e)
        }

    }

    /**
     * Compares [source] to [target] and returns a Comparison
     *
     * @param source node
     * @param target node
     * @return [Comparison.INCOMPARABLE], [Comparison.MORE], or [Comparison.INCOMPARABLE]
     */
    internal fun compare(source: String, target: String): Comparison {
        val source2target = finder.getAllPaths(source, target, true, null)
        val target2source = finder.getAllPaths(target, source, true, null)

        return when {
            source2target.isEmpty() && target2source.isEmpty() -> Comparison.INCOMPARABLE
            source2target.isNotEmpty() && target2source.isEmpty() -> Comparison.LESS
            source2target.isEmpty() && target2source.isNotEmpty() -> Comparison.MORE
            else -> throw IllegalStateException("Cycle: $source-$target-$source")
        }
    }

    /**
     * Finds all paths between given source and target vertex
     *
     * @param sourceVertex
     * @param targetVertex
     * @return
     */
    internal fun getAllPaths(sourceVertex: String, targetVertex: String): List<GraphPath<String, CredibilityObject>> =
            finder.getAllPaths(sourceVertex, targetVertex, false, graph.edgeSet().size)

    /**
     * Expands the graph by adding a new edge from source to target that is provided by the reporter.
     *
     * @param obj credibility object to be added
     * @return true on success, false otherwise
     */
    fun expansion(obj: CredibilityObject): Boolean {
        if (graph.containsVertex(obj.source) && graph.containsVertex(obj.target)) {
            val reversePathExists = finder
                    .getAllPaths(obj.target, obj.source, true, null)
                    .isEmpty()
                    .not()

            if (reversePathExists) {
                return false
            }
        }

        graph.addVertex(obj.source)
        graph.addVertex(obj.target)
        return graph.addEdge(obj.source, obj.target, obj)
    }

    /**
     * Find extremes (w.r.t the credibility of reporters) on all paths between source and target.
     *
     * @param source node
     * @param target node
     * @param type of extreme
     * @return set extremes
     */
    internal fun getExtremes(source: String, target: String, type: Extreme): Set<CredibilityObject> =
            getAllPaths(source, target) // get all paths
                    .flatMap { getExtremes(it.edgeList, type) } // get extreme(s) of each path
                    .toSet() // convert to set


    /**
     * Find extremes in a collection of CredibilityObjects. Use given graph to determine the
     * reliability of credibility objects.
     *
     * @param objects collection of credibility objects
     * @param extreme the type of extreme
     * @param graph to measure reliability of credibility objects
     * @return set of credibility objects that have extreme reliability
     */
    internal fun getExtremes(objects: Collection<CredibilityObject>, extreme: Extreme,
                             graph: CredibilityGraph = this): Set<CredibilityObject> {
        /*objects.fold(Collections.EMPTY_SET) { acc, e ->
            TODO()
        }

        TODO()*/

        val finder = if (graph == this) finder else AllDirectedPaths(graph.graph)
        val filtered = HashSet(objects)

        for (one in objects) {
            for (two in objects) {
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

                if (extreme == Extreme.MAX) {
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

    fun min(current: Set<CredibilityObject>, co: CredibilityObject): Set<CredibilityObject> {
        val allPaths = current.map {
            it to Pair(
                    finder.getAllPaths(it.reporter, co.reporter, true, null).isNotEmpty(),
                    finder.getAllPaths(co.reporter, it.reporter, true, null).isNotEmpty()
            )
        }

        val incomparableToAll = allPaths.all { !it.second.first && !it.second.second }
        if (incomparableToAll) return current + co

        val greaterThanAll = allPaths.all { it.second.first && !it.second.second }
        if (greaterThanAll) return current

        // current.filter {  }

        TODO("Have to implement min")
    }

    /**
     * Removes all paths from source to target by removing the minimal number of credibility objects.
     *
     * @param source
     * @param target
     */
    fun contraction(source: String, target: String) {
        val toRemove = getExtremes(source, target, Extreme.MIN)
        graph.removeAllEdges(toRemove)
    }

    /**
     * Adds the provided credibility object to the knowledge-base, and assures the latter
     * is consistent
     *
     * @param obj credibility object to be added
     * @return true on success, false otherwise
     */
    fun prioritizedRevision(obj: CredibilityObject): Boolean {
        contraction(obj.target, obj.source)
        return expansion(obj)
    }

    /**
     * Estimates the reliability of source being less credible than the target.
     * The reliability is denoted with the set of least credible reporters
     * that claim the source is less credible than the target.
     *
     * @param source
     * @param target
     * @return
     */
    internal fun reliability(source: String, target: String): Set<String> {
        val minimalSources = getExtremes(source, target, Extreme.MIN)
        val maximalSources = getExtremes(minimalSources, Extreme.MAX)

        return maximalSources.map { it.reporter }.toSet()
    }

    /**
     * Revises the knowledge-base by trying to add given credibility object.
     *
     * The revision succeeds iff:
     *  * it does not contradict current knowledge-base, or
     *  * it contradicts the current knowledge-base but the reliability of the
     * new information is higher.
     *
     * @param obj credibility object to be added
     * @return true on success, false otherwise
     */
    fun nonPrioritizedRevision(obj: CredibilityObject): Boolean {
        val reliabilityOfOpposite = reliability(obj.target, obj.source)
        val objIsMoreReliable = reliabilityOfOpposite.all {
            finder.getAllPaths(it, obj.reporter, true, null).isNotEmpty()
            // Q: what if the reporter and it are incomparable?
            // A: objects from incomparable reporters are not added
        }

        return if (objIsMoreReliable) {
            prioritizedRevision(obj)
        } else {
            false
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
            this.graph.addVertex(edge.source)
            this.graph.addVertex(edge.target)
            this.graph.addEdge(edge.source, edge.target, edge)
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
            newGraph.addVertex(edge.source)
            newGraph.addVertex(edge.target)
            newGraph.addEdge(edge.source, edge.target,
                    CredibilityObject(edge.source, edge.target, edge.reporter))
        }

        return CredibilityGraph(newGraph)
    }

    /**
     * Finds all minimal cycles in current knowledge-base
     *
     * @return
     */
    internal fun findCycles(): Set<GraphWalk<String, CredibilityObject>> {
        val algorithm = HawickJamesSimpleCycles(graph)
        return algorithm.findSimpleCycles()
                .map { vertexes ->
                    vertexes.add(vertexes[0]) // connect the cycle
                    vertexes.reverse() // cycles are found in reverse order
                    buildPaths(vertexes)
                }
                .flatMap { it }
                .toSet()
    }

    /**
     * Builds a set of GraphWalks from a list of vertexes representing a cycle
     *
     * @param vertexes
     * @return
     */
    internal fun buildPaths(vertexes: List<String>): Set<GraphWalk<String, CredibilityObject>> {
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