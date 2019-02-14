package credibilitygraphs.model

import atb.interfaces.DeceptionModel

/**
 * Creates stricter opinions. Changed opinions are consistent.
 *
 * The function being used is: x^4
 */
class Strict : DeceptionModel {

    override fun calculate(trustDegree: Double): Double = Math.pow(trustDegree, DOWNER)

    override fun initialize(vararg params: Any?) = Unit

    override fun toString(): String = "Strict"

    companion object {
        const val DOWNER = 4.0
        const val UPPER = 1.0 / DOWNER
    }
}

/**
 * Creates looser opinions. Changed opinions are consistent.
 *
 * The function being used is: x^(1/4)
 */
class Loose : DeceptionModel {
    override fun calculate(trustDegree: Double): Double = Math.pow(trustDegree, Strict.UPPER)

    override fun initialize(vararg params: Any?) = Unit

    override fun toString(): String = "Loose"
}