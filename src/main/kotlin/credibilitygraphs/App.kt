package credibilitygraphs

import credibilitygraphs.core.CredibilityObject
import credibilitygraphs.core.KnowledgeBase
import guru.nidi.graphviz.engine.Format

object App {

    private const val EXAMPLE2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1),(A2,A4,B)," +
            "(A2,A4,F3),(A3,A4,F2)"
    private const val EXAMPLE5 = "(D,F,J),(D,H,L),(F,G,M),(H,G,M),(G,E,K),(J,K,E),(K,L,G),(L,M,E)"
    private const val EXAMPLE13 = "(H,I,F),(H,L,D),(H,J,G),(I,L,G),(J,L,E),(J,L,F),(J,K,D)," +
            "(J,K,E),(K,L,D),(D,E,G),(D,F,E),(E,G,F),(F,G,D)"

    fun paper() {
        val kb1 = KnowledgeBase(EXAMPLE2)
        kb1.exportDOT("./fig-1", Format.PNG)

        val kb2 = KnowledgeBase(EXAMPLE2)
        kb2.expansion(CredibilityObject("A1", "A3", "F2"))
        kb2.exportDOT("./fig-2-expansion", Format.PNG)

        val kb3 = KnowledgeBase(EXAMPLE2)
        kb3.contraction("A1", "A4")
        kb3.exportDOT("./fig-3-contraction", Format.PNG)

        val kb4 = KnowledgeBase(EXAMPLE2)
        kb4.nonPrioritizedRevision(CredibilityObject("A4", "A1", "B"))
        kb4.exportDOT("./fig-4-npr-revision", Format.PNG)

        val kb5 = KnowledgeBase(EXAMPLE2)
        kb5.prioritizedRevision(CredibilityObject("A4", "A1", "B"))
        kb5.exportDOT("./fig-5-pr-revision", Format.PNG)

        val kb6 = KnowledgeBase(EXAMPLE13)
        kb6.exportDOT("./fig-6", Format.PNG)
        // kb6.exportGraphML("./fig-6")

        val kb7 = KnowledgeBase(EXAMPLE13)
        kb7.nonPrioritizedRevision(CredibilityObject("L", "H", "G"))
        kb7.exportDOT("./fig-7-npr-revision", Format.PNG)
    }

    fun merge() {
        val first = KnowledgeBase("(A, E, B), (D, B, A), (C, A, D), (B, A, E)")
        first.exportDOT("./merge-g1", Format.PNG)

        val second = KnowledgeBase("(A, C, E), (C, D, B)")
        second.exportDOT("./merge-g2", Format.PNG)

        first.merge(second)
        first.exportDOT("./merge-merged", Format.PNG)
    }
}

fun main(args: Array<String>) {
    App.paper()
    // App.merge()
}

