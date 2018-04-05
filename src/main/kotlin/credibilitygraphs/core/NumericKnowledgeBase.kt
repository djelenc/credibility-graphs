package credibilitygraphs.core

class NumericKnowledgeBase(input: String) : KnowledgeBase(input) {

    class PastAccuracy(var correct: Int, var incorrect: Int)

    internal val pastAccuracy: MutableMap<String, PastAccuracy> = HashMap()

    fun storeCorrect(agent: String) {
        val result = pastAccuracy.getOrPut(agent, { PastAccuracy(0, 0) })
        result.correct += 1
    }

    fun storeIncorrect(agent: String) {
        val result = pastAccuracy.getOrPut(agent, { PastAccuracy(0, 0) })
        result.incorrect += 1
    }

    override fun isLessCredible(source: String, target: String, graph: KnowledgeBase): Boolean {
        val accSource = pastAccuracy.getOrPut(source, { PastAccuracy(0, 0) })
        val accTarget = pastAccuracy.getOrPut(target, { PastAccuracy(0, 0) })
        val sourceTotal = accSource.correct - accSource.incorrect
        val targetTotal = accTarget.correct - accTarget.incorrect
        return sourceTotal < targetTotal
    }
}