package credibilitygraphs.model

import atb.interfaces.Experience
import atb.scenario.Transitive

class TransitiveWithDebugging : Transitive() {

    override fun generateExperiences(): MutableList<Experience> {
        val exp = super.generateExperiences()

        val printedCap = capabilities.asSequence()
                .sortedBy { it.value }
                .map { Pair(it.key, String.format("%.2f", it.value)) }
                .toList()
        println("$time: cap = $printedCap")

        return exp
    }

    override fun toString() = "Transitive (with debugging)"
}