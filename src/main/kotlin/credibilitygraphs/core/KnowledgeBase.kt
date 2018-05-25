package credibilitygraphs.core

import credibilitygraphs.parser.GraphLexer
import credibilitygraphs.parser.GraphParser
import credibilitygraphs.parser.Visitor
import guru.nidi.graphviz.attribute.RankDir
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.parse.Parser
import org.antlr.v4.runtime.CharStreams
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

/**
 * Represents a credibility object with [source], [target] and a [reporter].
 *
 * In a credibility graph, the credibility object is effectively an edge between
 * src and tgt with the reporter being its label.
 */
data class CredibilityObject<Node, Edge>(val src: Node, val tgt: Node, val reporter: Edge) : DefaultEdge() {
    override fun toString(): String = "$reporter ($src-$tgt)"
}

enum class Extreme { MIN, MAX }

enum class Comparison { LESS, MORE, INCOMPARABLE }

/**
 * Represents a knowledge-base of credibility objects
 */
abstract class KnowledgeBase<Node, Edge>(val graph: Graph<Node, CredibilityObject<Node, Edge>>) {

    // finds paths between graph nodes
    private val pathFinder = AllDirectedPaths(graph)

    // finds cycles in a graph
    private val cycleFinder = HawickJamesSimpleCycles(graph)

    abstract fun makeInstance(graph: Graph<Node, CredibilityObject<Node, Edge>>): KnowledgeBase<Node, Edge>

    /**
     * Returns true iff [source] is less credible than the [target] in the transitive
     * closure of given graph; false otherwise.
     */
    abstract fun isLessCredible(source: Edge, target: Edge, graph: KnowledgeBase<Node, Edge> = this): Boolean

    /**
     * Returns true iff [source] < [target] in the transitive closure; false otherwise
     */
    internal fun isLess(source: Node, target: Node, graph: KnowledgeBase<Node, Edge> = this): Boolean {
        val algorithm = if (graph == this) this.pathFinder else AllDirectedPaths(graph.graph)
        return graph.graph.containsVertex(source) &&
                graph.graph.containsVertex(target) &&
                algorithm.getAllPaths(source, target, true, null).isNotEmpty()
    }

    /**
     * Compares [source] to [target] and returns [Comparison.LESS], [Comparison.MORE], or
     * [Comparison.INCOMPARABLE].
     *
     * Throws and error if the knowledge-base is inconsistent.
     */
    internal fun compare(source: Node, target: Node, kb: KnowledgeBase<Node, Edge> = this): Comparison = when {
        kb.isLess(source, target) && !kb.isLess(target, source) -> Comparison.LESS
        !kb.isLess(source, target) && kb.isLess(target, source) -> Comparison.MORE
        !kb.isLess(source, target) && !kb.isLess(target, source) -> Comparison.INCOMPARABLE
        else -> throw Error("$source < $target AND $target < $source -- KB is inconsistent")
    }

    /**
     * Finds all paths between given [source] and [target] vertex
     * @return A list of paths
     */
    internal fun getAllPaths(source: Node, target: Node): List<GraphPath<Node, CredibilityObject<Node, Edge>>> = when {
        graph.vertexSet().containsAll(listOf(source, target)) -> pathFinder.getAllPaths(source, target, false, graph.edgeSet().size)
        else -> emptyList()
    }


    /**
     * Expands the knowledge-base by adding given [credibilityObject]. The expansion fails if
     * the [credibilityObject] contradicts current knowledge-base.
     * @return true on success, false otherwise
     */
    fun expansion(credibilityObject: CredibilityObject<Node, Edge>): Boolean = when {
        !isLess(credibilityObject.tgt, credibilityObject.src) -> {
            graph.addVertex(credibilityObject.src)
            graph.addVertex(credibilityObject.tgt)
            graph.addEdge(credibilityObject.src, credibilityObject.tgt, credibilityObject)
        }
        else -> false
    }

    /**
     * Find extremes ([Extreme.MIN] or [Extreme.MAX]) (w.r.t the credibility of reporters) on all
     * paths between [source] and [target].
     * @return set of extremes
     */
    internal fun getExtremes(source: Node, target: Node, type: Extreme): Set<CredibilityObject<Node, Edge>> =
            getAllPaths(source, target) // get all paths
                    .flatMap { getExtremes(it.edgeList, type) } // get extreme(s) of each path
                    .toSet() // convert to set

    /**
     * Get extreme credibility objects (w.r.t. reliability of reporters) from a collection of
     * credibility [objects].
     *
     * @return Set of credibility objects that have extreme reliability
     */
    internal fun getExtremes(objects: Collection<CredibilityObject<Node, Edge>>, extreme: Extreme,
                             graph: KnowledgeBase<Node, Edge> = this): Set<CredibilityObject<Node, Edge>> =
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
    internal fun extreme(set: Set<CredibilityObject<Node, Edge>>, credibilityObject: CredibilityObject<Node, Edge>,
                         extreme: Extreme, graph: KnowledgeBase<Node, Edge>): Set<CredibilityObject<Node, Edge>> {
        data class ComparisonToCredibilityObject(val obj: CredibilityObject<Node, Edge>, val isLess: Boolean, val isMore: Boolean)

        val existing = set.map {
            ComparisonToCredibilityObject(obj = it,
                    isLess = isLessCredible(it.reporter, credibilityObject.reporter, graph),
                    isMore = isLessCredible(credibilityObject.reporter, it.reporter, graph))
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
    fun contraction(source: Node, target: Node) {
        val toRemove = getExtremes(source, target, Extreme.MIN)
        graph.removeAllEdges(toRemove)
    }

    /**
     * Adds the [obj]ect to the knowledge-base, and assures the latter is consistent
     * @return true on success, false otherwise
     */
    fun prioritizedRevision(obj: CredibilityObject<Node, Edge>): Boolean {
        contraction(obj.tgt, obj.src)
        return expansion(obj)
    }

    /**
     * Estimates the reliability of [source] being less than the [target].
     * @return the set of reporters claiming the [source] is less than the [target].
     */
    internal fun reliability(source: Node, target: Node): Set<Edge> {
        val minimalSources = getExtremes(source, target, Extreme.MIN)
        val maximalSources = getExtremes(minimalSources, Extreme.MAX)

        return maximalSources.map { it.reporter }.toSet()
    }

    /**
     * Revises the knowledge-base by adding given [credibilityObject]. The revision succeeds iff
     * the new [credibilityObject]:
     *  * does not contradict current knowledge-base, or
     *  * it contradicts the current knowledge-base but the reliability of the new
     * [credibilityObject] is higher than the existing ones that contradict it.
     * @return true on success, false otherwise
     */
    fun nonPrioritizedRevision(credibilityObject: CredibilityObject<Node, Edge>): Boolean {
        val reliabilityOfOpposite = reliability(credibilityObject.tgt, credibilityObject.src)
        val objIsMoreReliable = reliabilityOfOpposite.all { isLessCredible(it, credibilityObject.reporter) }

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
    fun merge(input: KnowledgeBase<Node, Edge>) {
        // make a backup for resolving cycles later
        val old = copy()

        // copy all vertices and edges from input into this graph
        input.graph.edgeSet().forEach {
            graph.addVertex(it.src)
            graph.addVertex(it.tgt)
            graph.addEdge(it.src, it.tgt, it)
        }

        // in every cycle, remove the least reliable edge
        findCycles().forEach {
            val leastReliable = getExtremes(it.edgeList, Extreme.MIN, old)
            graph.removeAllEdges(leastReliable)
        }
    }

    /** Creates a copy of this KnowledgeBase */
    fun copy(): KnowledgeBase<Node, Edge> {
        // due to type erasure, generic types are unknown at runtime,
        // so we have to inspect actual instances to know the type
        val iterator = graph.edgeSet().iterator()

        if (!iterator.hasNext()) {
            throw Error("Could not copy an empty graph!")
        }

        val someCo = iterator.next()

        val newGraph = DirectedMultigraph<Node, CredibilityObject<Node, Edge>>(someCo::class.java)

        graph.edgeSet().forEach {
            newGraph.addVertex(it.src)
            newGraph.addVertex(it.tgt)
            newGraph.addEdge(it.src, it.tgt, it.copy())
        }

        // return  javaClass.getDeclaredConstructor(javaClass).newInstance(newGraph)
        // val new = javaClass.newInstance()
        return makeInstance(newGraph)
    }

    /**
     * Finds all minimal cycles in current knowledge-base using lazy evaluation
     */
    internal fun findCyclesLazy(): Sequence<GraphWalk<Node, CredibilityObject<Node, Edge>>> = cycleFinder
            .findSimpleCycles()
            .asSequence()
            .map {
                it.add(it[0]) // connect the cycle
                it.reverse() // because cycles are found in reverse order
                buildPathsLazy(it)
            }.flatten()

    /**
     * Builds a sequence of GraphWalk instances from a list of vertexes usin lazy evaluation
     */
    internal fun buildPathsLazy(vertexes: List<Node>): Sequence<GraphWalk<Node, CredibilityObject<Node, Edge>>> {
        val source = vertexes[0]

        if (vertexes.size == 1) {
            return sequenceOf(GraphWalk.singletonWalk(graph, source))
        }

        val target = vertexes[1]

        return graph.getAllEdges(source, target).asSequence().map {
            val step = GraphWalk<Node, CredibilityObject<Node, Edge>>(graph, source, target, listOf(it), 0.0)
            val nextPaths = buildPathsLazy(vertexes.subList(1, vertexes.size))
            nextPaths.map { step.concat(it, { 0.0 }) }
        }.flatten()
    }

    /**
     * Finds all minimal cycles in current knowledge-base
     */
    internal fun findCycles(): Set<GraphWalk<Node, CredibilityObject<Node, Edge>>> = HawickJamesSimpleCycles(graph)
            .findSimpleCycles()
            .map {
                it.add(it[0]) // connect the cycle
                it.reverse() // cycles are found in reverse order
                buildPaths(it)
            }
            .flatten()
            .toSet()


    /**
     * Builds a sequence of GraphWalk instances from a list of vertexes
     */
    internal fun buildPaths(vertexes: List<Node>): Set<GraphWalk<Node, CredibilityObject<Node, Edge>>> {
        val source = vertexes[0]

        if (vertexes.size == 1) {
            return setOf(GraphWalk.singletonWalk(graph, source))
        }

        val allPaths = LinkedHashSet<GraphWalk<Node, CredibilityObject<Node, Edge>>>()
        val target = vertexes[1]
        val edges = graph.getAllEdges(source, target)

        for (edge in edges) {
            val step = GraphWalk(graph, source, target, listOf(edge), 0.0)
            val nextPaths = buildPaths(vertexes.subList(1, vertexes.size))

            for (path in nextPaths) {
                val full = step.concat(path) { 0.0 }
                allPaths.add(full)
            }
        }

        return allPaths
    }

    /**
     * Exports the underlying knowledge base into DOT format graph and saves it into file using given format.
     * The DOT exporter requires the graphviz be installed.
     *
     * @param fileName
     * @param format
     * @param edgeLabels include label in edges
     * @throws ExportException
     */
    fun exportDOT(fileName: String, format: Format, edgeLabels: Boolean = true) {
        val exporter = DOTExporter<Node, CredibilityObject<Node, Edge>>(
                ComponentNameProvider<Node> { it.toString() }, null,
                if (edgeLabels) ComponentNameProvider<CredibilityObject<Node, Edge>> { it.reporter.toString() } else null)

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
        val exporter = GraphMLExporter<Node, CredibilityObject<Node, Edge>>().apply {
            setVertexIDProvider({ it.toString() })
            setVertexLabelProvider({ it.toString() })
            setEdgeLabelProvider({ it.reporter.toString() })
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

    companion object {
        /**
         * Parses given string of credibility objects and returns a directed multi graph
         */
        fun <Node, Edge, CredObj : CredibilityObject<Node, Edge>>
                parseText(text: String,
                          nodeMaker: (String) -> Node,
                          labelMaker: (String) -> Edge,
                          coMaker: (Node, Node, Edge) -> CredObj,
                          graphBuilder: () -> Graph<Node, CredObj>): Graph<Node, CredObj> {
            if (text.isEmpty()) {
                return graphBuilder()
            }

            val ais = CharStreams.fromString(text)
            val lexer = GraphLexer(ais)
            val tokens = CommonTokenStream(lexer)
            val parser = GraphParser(tokens)
            val tree = parser.stat()

            val v = Visitor<Node, Edge, CredObj>(nodeMaker, labelMaker, coMaker)
            val credibilityObjects = v.visit(tree)

            val g = graphBuilder()

            credibilityObjects.forEach {
                g.addVertex(it.src)
                g.addVertex(it.tgt)
                g.addEdge(it.src, it.tgt, it)
            }

            return g
        }
    }
}