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
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * An example of the random number generator implementation. This class can be
 * used in testing or when developing plug-ins.
 *
 * @author David
 */
public class ExampleGenerator implements RandomGenerator {
    private static final String MEAN_EX = "The mean must be between [0, 1], but was %.2f.";
    private static final String TOTAL_PROBABILIT_EX = "Total probabilit in pmf %s does not sum to %.2f, but is %.2f";
    private static final String INVALID_PROBABILITY_EX = "Invalid probability %.2f of element %s in pmf %s.";
    private static final String UNREACHABLE_CODE = "This part of code should be unreachable.";

    private final int seed;

    private final Random random;

    public ExampleGenerator(int seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    @Override
    public double nextDoubleFromUnitTND(double mean, double sd) {
        if (mean > 1d || mean < 0d) {
            throw new IllegalArgumentException(String.format(MEAN_EX, mean));
        }

        double number;

        do {
            number = mean + random.nextGaussian() * sd;
        } while (number > 1 || number < 0);

        return number;
    }

    @Override
    public double nextDoubleFromTo(double min, double max) {
        return random.nextDouble() * (max - min) - min;
    }

    @Override
    public int nextIntFromTo(int min, int max) {
        return min + random.nextInt(max + 1);
    }

    @Override
    public <T> T fromWeights(TreeMap<T, Double> pmf) {
        if (pmf == null || pmf.isEmpty()) {
            return null;
        }

        double totalProbability = 0;
        for (Map.Entry<T, Double> e : pmf.entrySet()) {
            final double probability = e.getValue();
            final T element = e.getKey();

            if (probability < 0d || probability > 1d) {
                throw new IllegalArgumentException(String.format(
                        INVALID_PROBABILITY_EX, probability, element, pmf));
            }

            totalProbability += probability;
        }

        if (Math.abs(1d - totalProbability) > 0.00001) {
            throw new IllegalArgumentException(String.format(
                    TOTAL_PROBABILIT_EX, pmf, 1d, totalProbability));
        }

        double rnd = nextDoubleFromTo(0, 1), weight = 0;

        for (Map.Entry<T, Double> e : pmf.entrySet()) {
            weight += e.getValue();

            if (weight > rnd) {
                return e.getKey();
            }
        }

        throw new Error(UNREACHABLE_CODE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Comparable<T>> Collection<T> chooseRandom(
            Collection<T> allItems, double fraction) {
        final long numItems = Math.round(allItems.size() * fraction);
        final Collection<T> selectedItems;

        try {
            selectedItems = allItems.getClass().newInstance();
        } catch (Exception e) {
            throw new Error(e);
        }

        final TreeMap<T, Double> pmf = new TreeMap<T, Double>();

        for (T item : allItems) {
            pmf.put(item, 1d / allItems.size());
        }

        int counter = 0;
        while (counter < numItems) {
            final T item = fromWeights(pmf);
            selectedItems.add(item);
            pmf.remove(item);
            counter += 1;

            for (T key : pmf.keySet())
                pmf.put(key, 1d / (allItems.size() - counter));
        }

        return selectedItems;
    }

    /**
     * Returns the random seed.
     *
     * @return Random seed
     */
    public int getSeed() {
        return seed;
    }
}
