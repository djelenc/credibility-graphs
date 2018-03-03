/*
 * Copyright (c) 2013 David Jelenc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     David Jelenc - initial API and implementation
 */
package testbed.interfaces;

import java.util.Collection;
import java.util.TreeMap;

/**
 * Interface for random number generators.
 *
 * @author David
 */
public interface RandomGenerator {

    /**
     * Generates a random number between [0, 1] using a truncated normal
     * distribution with given mean and standard deviation.
     * <p>
     * <p>
     * The mean parameter must be within [0, 1] or an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param mean Mean value
     * @param sd   Standard deviation
     * @return random number
     * @see <a href=
     * 'http://en.wikipedia.org/wiki/Truncated_normal_distribution'>
     * Truncated normal distribution</a>
     */
    public double nextDoubleFromUnitTND(double mean, double sd);

    /**
     * Returns a random number from an uniform distribution from (min, max).
     *
     * @param min Minimum value (exclusively)
     * @param max Maximum value (exclusively)
     * @return Generated random number
     */
    public double nextDoubleFromTo(double min, double max);

    /**
     * Returns a random integer from an uniform distribution between [min, max].
     * Useful for selecting a random index.
     *
     * @param min Minimum value (inclusively)
     * @param max Maximum value (inclusively)
     * @return random index
     */
    public int nextIntFromTo(int min, int max);

    /**
     * Returns a random element from the set of given elements. The elements to
     * be selected must be given as a map, in which keys represent elements and
     * values represent their respective probabilities.
     * <p>
     * <p>
     * To warrant a deterministic behavior, the distribution must be given as a
     * {@link TreeMap}.
     *
     * @param pmf Probability mass function of possible outcomes expressed as a
     *            {@link TreeMap}
     * @return A random element
     */
    public <T> T fromWeights(TreeMap<T, Double> pmf);

    /**
     * Returns a collection, which is subset of items from allItems. The number
     * of items in the returned collection is determined by the fraction
     * parameter.
     * <p>
     * <p>
     * To enforce repeatable behavior, the items in the collection have to
     * implement the {@link Comparable} interface.
     *
     * @param allItems Collection of all items
     * @param fraction Fraction of items to be selected
     * @return Subset of items
     */
    public <T extends Comparable<T>> Collection<T> chooseRandom(
            Collection<T> allItems, double fraction);

    /**
     * Returns the seed that initialized this instance.
     *
     * @return The seed.
     */
    public int getSeed();

}
