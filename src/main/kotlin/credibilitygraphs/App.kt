package credibilitygraphs

import credibilitygraphs.core.CredibilityGraph
import credibilitygraphs.core.CredibilityObject
import guru.nidi.graphviz.engine.Format

object App {

    const val EXAMPLE2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1), (A2,A4,B),(A2,A4,F3),(A3,A4,F2)"
    const val EXAMPLE5 = "(D,F,J),(D,H,L),(F,G,M),(H,G,M),(G,E,K),(J,K,E),(K,L,G),(L,M,E)"
    const val EXAMPLE13 = "(H,I,F),(H,L,D),(H,J,G),(I,L,G),(J,L,E),(J,L,F),(J,K,D)," +
            "(J,K,E),(K,L,D),(D,E,G),(D,F,E),(E,G,F),(F,G,D)"

    fun basic() {
        /*val graph = CredibilityGraph(EXAMPLE2)
        graph.exportDOT("./fig-1", Format.PNG)

        graph.expansion(CredibilityObject("C", "F1", "B"))
        graph.exportDOT("./fig-2-expansion", Format.PNG)

        graph.contraction("C", "F1")
        graph.exportDOT("./fig-3-contraction", Format.PNG)

        graph.nonPrioritizedRevision(CredibilityObject("A4", "A1", "B"))
        graph.exportDOT("./fig-4-npr-revision", Format.PNG)

        graph.prioritizedRevision(CredibilityObject("A4", "A1", "B"))
        graph.exportDOT("./fig-5-pr-revision", Format.PNG)*/

        val graph2 = CredibilityGraph(EXAMPLE13)
        graph2.exportDOT("./fig-6", Format.PNG)
        graph2.exportGraphML("./fig-6")

        graph2.nonPrioritizedRevision(CredibilityObject("L", "H", "G"))
        graph2.exportDOT("./fig-7-npr-revision", Format.PNG)
    }

    fun informants() {
        val inf = CredibilityGraph(
                "(B, D, inf), (A, D, inf)")
        inf.graph.addVertex("C")
        inf.graph.addVertex("E")
        inf.graph.addVertex("F")
        inf.graph.addVertex("G")
        inf.exportDOT("./inf", Format.SVG)
    }

    fun opinions() {
        val op1 = CredibilityGraph(
                "(A, C, D), (C, B, D)")
        op1.exportDOT("./ops1", Format.SVG)

        val op2 = CredibilityGraph(
                "(E, F, B), (F, G, B), (A, F, B)")
        op2.exportDOT("./ops2", Format.SVG)
    }

    fun experiences() {
        val graph = CredibilityGraph(
                "(A, B, exp), (B, C, exp)")
        graph.exportDOT("./exp", Format.SVG)
    }
}

fun main(args: Array<String>) {
    // App.experiences();
    // App.opinions();
    // App.informants();

    /*val graph = CredibilityGraph("(A, E, B), (D, B, A), (C, A, D), (B, A, E)")
    graph.exportDOT("./g1", Format.PNG)

    val newGraph = CredibilityGraph("(A, C, E), (C, D, B)")
    newGraph.exportDOT("./g2", Format.PNG)

    graph.merge(newGraph)
    graph.exportDOT("./merged", Format.PNG)*/
    App.basic()
}

