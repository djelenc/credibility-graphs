package credibilitygraphs

import credibilitygraphs.core.CredibilityObject
import credibilitygraphs.core.NumericKnowledgeBase
import credibilitygraphs.core.OriginalKnowledgeBase
import guru.nidi.graphviz.engine.Format

object App {

    private const val EXAMPLE2 = "(B,F1,F2),(F1,F2,F3),(F2,F3,B),(A1,A2,F1),(A1,A3,F1),(A2,A4,B)," +
            "(A2,A4,F3),(A3,A4,F2)"
    private const val EXAMPLE5 = "(D,F,J),(D,H,L),(F,G,M),(H,G,M),(G,E,K),(J,K,E),(K,L,G),(L,M,E)"
    private const val EXAMPLE13 = "(H,I,F),(H,L,D),(H,J,G),(I,L,G),(J,L,E),(J,L,F),(J,K,D)," +
            "(J,K,E),(K,L,D),(D,E,G),(D,F,E),(E,G,F),(F,G,D)"

    fun paper() {
        val kb1 = OriginalKnowledgeBase(EXAMPLE2)
        kb1.exportDOT("./fig-1", Format.PNG)

        val kb2 = OriginalKnowledgeBase(EXAMPLE2)
        kb2.expansion(CredibilityObject("A1", "A3", "F2"))
        kb2.exportDOT("./fig-2-expansion", Format.PNG)

        val kb3 = OriginalKnowledgeBase(EXAMPLE2)
        kb3.contraction("A1", "A4")
        kb3.exportDOT("./fig-3-contraction", Format.PNG)

        val kb4 = OriginalKnowledgeBase(EXAMPLE2)
        kb4.nonPrioritizedRevision(CredibilityObject("A4", "A1", "B"))
        kb4.exportDOT("./fig-4-npr-revision", Format.PNG)

        val kb5 = OriginalKnowledgeBase(EXAMPLE2)
        kb5.prioritizedRevision(CredibilityObject("A4", "A1", "B"))
        kb5.exportDOT("./fig-5-pr-revision", Format.PNG)

        val kb6 = OriginalKnowledgeBase(EXAMPLE13)
        kb6.exportDOT("./fig-6", Format.PNG)
        // kb6.exportGraphML("./fig-6")

        val kb7 = OriginalKnowledgeBase(EXAMPLE13)
        kb7.nonPrioritizedRevision(CredibilityObject("L", "H", "G"))
        kb7.exportDOT("./fig-7-npr-revision", Format.PNG)
    }

    fun merge() {
        val first = OriginalKnowledgeBase("(A, E, B), (D, B, A), (C, A, D), (B, A, E)")
        first.exportDOT("./merge-g1", Format.PNG)

        val second = OriginalKnowledgeBase("(A, C, E), (C, D, B)")
        second.exportDOT("./merge-g2", Format.PNG)

        first.merge(second)
        first.exportDOT("./merge-merged", Format.PNG)
    }

    fun numeric() {
        val first = NumericKnowledgeBase("(1, 5, 2), (4, 2, 1), (3, 1, 4), (2, 1, 5)")
        first.exportDOT("./numbers-1", Format.PNG)
    }

    fun paperEmptyStart() {
        val kb = NumericKnowledgeBase("(0, 1, 0), (2, 3, 0), (3, 4, 0)")
        kb.contraction(0, 1)
        kb.contraction(2, 3)
        kb.contraction(3, 4)
        kb.exportDOT("./1-step-0", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(0, 3, 3))
        kb.exportDOT("./1-step-1", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(1, 3, 3))
        kb.exportDOT("./1-step-2", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(1, 0, 2))
        kb.exportDOT("./1-step-3", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(0, 2, 2))
        kb.exportDOT("./1-step-4", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(1, 2, 2))
        kb.exportDOT("./1-step-5", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(3, 2, 2))
        kb.exportDOT("./1-step-6", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(0, 4, 2))
        kb.exportDOT("./1-step-7", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(2, 0, 1))
        kb.exportDOT("./1-step-8", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(0, 1, 1))
        kb.exportDOT("./1-step-9", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(4, 3, 1))
        kb.exportDOT("./1-step-10", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(1, 4, 1))
        kb.exportDOT("./1-step-11", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(2, 4, 1))
        kb.exportDOT("./1-step-12", Format.PNG, nodePrefix = "H")
    }

    fun paperPriorKnowledge() {
        val kb = NumericKnowledgeBase("(2, 0, 1), (0, 1, 1), (2, 1, 5), (3, 4, 0)")
        kb.contraction(3, 4)
        kb.exportDOT("./2-step-0", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(0, 3, 3))
        kb.exportDOT("./2-step-1", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(1, 3, 3))
        kb.exportDOT("./2-step-2", Format.PNG, nodePrefix = "H")

        kb.contraction(0, 1)
        kb.exportDOT("./2-step-3a", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(1, 0, 2))
        kb.exportDOT("./2-step-3b", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(0, 2, 2))
        kb.exportDOT("./2-step-4", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(1, 2, 2))
        kb.exportDOT("./2-step-5", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(3, 2, 2))
        kb.exportDOT("./2-step-6", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(0, 4, 2))
        kb.exportDOT("./2-step-7", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(2, 0, 1))
        kb.exportDOT("./2-step-8", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(0, 1, 1))
        kb.exportDOT("./2-step-9", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(4, 3, 1))
        kb.exportDOT("./2-step-10", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(1, 4, 1))
        kb.exportDOT("./2-step-11", Format.PNG, nodePrefix = "H")

        kb.expansion(CredibilityObject(2, 4, 1))
        kb.exportDOT("./2-step-12", Format.PNG, nodePrefix = "H")
    }
}

fun main(args: Array<String>) {
    // App.paper()
    // App.numeric()
    App.paperEmptyStart()
    App.paperPriorKnowledge()
    // App.merge()
}

