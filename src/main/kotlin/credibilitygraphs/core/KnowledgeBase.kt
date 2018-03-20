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
 * Represents a credibility object with [source], [target] and a [reporter].
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

/**
 * Represents a knowledge-base of credibility objects
 */
class KnowledgeBase(val graph: Graph<String, CredibilityObject>) {
    // finder finds paths between graph nodes
    private val finder = AllDirectedPaths(graph)

    constructor(credibilityObjects: String = "") : this(parseObjects(credibilityObjects))

    companion object {
        /**
         * Parses given string of credibility objects and returns a directed multi graph
         *
         * @param graph
         * @return directed multi-graph
         */
        fun parseObjects(graph: String): Graph<String, CredibilityObject> {
            if (graph == "") {
                // TODO: improve this!
                return DirectedMultigraph<String, CredibilityObject>(CredibilityObject::class.java)
            }

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
     */
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
        mutableGraph.generalAttrs().add(RankDir.LEFT_TO_RIGHT)
        Graphviz.fromGraph(mutableGraph)
                .render(format)
                .toFile(File(fileName + "." + format.name.toLowerCase()))
    }

    /**
     * Exports given graph into GraphML format and saves it into file with
     * .graphml extension.
     *
     * @param fileName
     */
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
            exporter.exportGraph(graph, File("$fileName.graphml"))
        } catch (e: ExportException) {
            throw IOException(e)
        }

    }

    /**
     * Returns true iff [source] < [target] in the transitive closure; false otherwise
     */
    internal fun isLess(source: String, target: String, graph: KnowledgeBase = this): Boolean {
        val algorithm = if (graph == this) this.finder else AllDirectedPaths(graph.graph)
        return graph.graph.containsVertex(source) &&
                graph.graph.containsVertex(target) &&
                algorithm.getAllPaths(source, target, true, null).isNotEmpty()

    }

    /**
     * Finds all paths between given [source] and [target] vertex
     * @return A list of paths
     */
    internal fun getAllPaths(source: String, target: String): List<GraphPath<String, CredibilityObject>> =
            finder.getAllPaths(source, target, false, graph.edgeSet().size)

    /**
     * Expands the knowledge-base by adding given [credibilityObject]. The expansion fails the [credibilityObject]
     * contradicts current knowledge-base.
     * @return true on success, false otherwise
     */
    fun expansion(credibilityObject: CredibilityObject): Boolean = when {
        !isLess(credibilityObject.target, credibilityObject.source) -> {
            graph.addVertex(credibilityObject.source)
            graph.addVertex(credibilityObject.target)
            graph.addEdge(credibilityObject.source, credibilityObject.target, credibilityObject)
        }
        else -> false
    }

    /**
     * Find extremes ([Extreme.MIN] or [Extreme.MAX]) (w.r.t the credibility of reporters) on all paths between
     * [source] and [target].
     * @return set of extremes
     */
    internal fun getExtremes(source: String, target: String, type: Extreme): Set<CredibilityObject> =
            getAllPaths(source, target) // get all paths
                    .flatMap { getExtremes(it.edgeList, type) } // get extreme(s) of each path
                    .toSet() // convert to set

    /**
     * Get extreme credibility objects (w.r.t. reliability of reporters) from a collection of credibility [objects].
     * @return Set of credibility objects that have extreme reliability
     */
    internal fun getExtremes(objects: Collection<CredibilityObject>, extreme: Extreme,
                             graph: KnowledgeBase = this): Set<CredibilityObject> =
            objects.fold(setOf(), { acc, credibilityObject -> extreme(acc, credibilityObject, extreme, graph) })


    /**
     * Compares all objects in [set] with [credibilityObject] and returns whichever is more extreme
     * (bigger or smaller) according to the [graph]:
     *  * if [credibilityObject] is bigger (smaller) than all elements in [set], returns a singleton set of
     *  [credibilityObject];
     *  * if [credibilityObject] is smaller (bigger) than all elements in [set], returns [set];
     *  * if [credibilityObject] is incomparable to all elements in [set], returns [set] and [credibilityObject];
     *  * if [credibilityObject] is bigger (smaller) than some in [set] and incomparable to the remaining,
     *  returns all incomparable elements plus the biggest (smalles) among [credibilityObject] and the remaining
     *  comparable elements.
     */
    internal fun extreme(set: Set<CredibilityObject>, credibilityObject: CredibilityObject,
                         extreme: Extreme, graph: KnowledgeBase): Set<CredibilityObject> {
        data class ComparisonToCredibilityObject(val obj: CredibilityObject, val isLess: Boolean, val isMore: Boolean)

        val existing = set.map {
            ComparisonToCredibilityObject(obj = it,
                    isLess = isLess(it.reporter, credibilityObject.reporter, graph),
                    isMore = isLess(credibilityObject.reporter, it.reporter, graph))
        }

        return when (extreme) {
            Extreme.MAX -> when {
                existing.all { it.isLess && !it.isMore } -> setOf(credibilityObject) // bigger than all in set
                existing.all { it.isMore && !it.isLess } -> set // smaller than all in set
                existing.all { !it.isLess && !it.isMore } -> set + credibilityObject // incomparable to all in set
                else -> { // bigger than some
                    val incomparable = existing.filter { !it.isLess && !it.isMore }.map { it.obj }
                    val more = existing.filter { it.isMore }.map { it.obj }
                    if (more.isEmpty()) incomparable.plus(credibilityObject).toSet() else set
                }
            }
            Extreme.MIN -> when {
                existing.all { it.isMore && !it.isLess } -> setOf(credibilityObject)
                existing.all { it.isLess && !it.isMore } -> set
                existing.all { !it.isLess && !it.isMore } -> set + credibilityObject
                else -> {
                    val incomparable = existing.filter { !it.isLess && !it.isMore }.map { it.obj }
                    val less = existing.filter { it.isLess }.map { it.obj }
                    if (less.isEmpty()) incomparable.plus(credibilityObject).toSet() else set
                }
            }
        }
    }

    /**
     * Removes all paths from [source] to [target] by removing the minimal number of credibility objects.
     */
    fun contraction(source: String, target: String) {
        val toRemove = getExtremes(source, target, Extreme.MIN)
        graph.removeAllEdges(toRemove)
    }

    /**
     * Adds the [obj]ect to the knowledge-base, and assures the latter is consistent
     * @return true on success, false otherwise
     */
    fun prioritizedRevision(obj: CredibilityObject): Boolean {
        contraction(obj.target, obj.source)
        return expansion(obj)
    }

    /**
     * Estimates the reliability of [source] being less than the [target].
     * @return the set of reporters claiming the [source] is less than the [target].
     */
    internal fun reliability(source: String, target: String): Set<String> {
        val minimalSources = getExtremes(source, target, Extreme.MIN)
        val maximalSources = getExtremes(minimalSources, Extreme.MAX)

        return maximalSources.map { it.reporter }.toSet()
    }

    /**
     * Revises the knowledge-base by adding given [credibilityObject]. The revision succeeds iff
     * the new [credibilityObject]:
     *  * does not contradict current knowledge-base, or
     *  * it does contradict the current knowledge-base but the reliability of the [credibilityObject] is higher.
     * @return true on success, false otherwise
     */
    fun nonPrioritizedRevision(credibilityObject: CredibilityObject): Boolean {
        val reliabilityOfOpposite = reliability(credibilityObject.target, credibilityObject.source)
        val objIsMoreReliable = reliabilityOfOpposite.all { isLess(it, credibilityObject.reporter) }

        return if (objIsMoreReliable) {
            prioritizedRevision(credibilityObject)
        } else {
            false
        }
    }

    /**
     * Merge the current knowledge-base with provided one.
     *
     * @param input
     */
    fun merge(input: KnowledgeBase) {
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
     * Creates a copy of this KnowledgeBase
     *
     * @return
     */
    fun copy(): KnowledgeBase {
        val newGraph = DirectedMultigraph<String, CredibilityObject>(CredibilityObject::class.java)

        graph.edgeSet().forEach { edge ->
            newGraph.addVertex(edge.source)
            newGraph.addVertex(edge.target)
            newGraph.addEdge(edge.source, edge.target,
                    CredibilityObject(edge.source, edge.target, edge.reporter))
        }

        return KnowledgeBase(newGraph)
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