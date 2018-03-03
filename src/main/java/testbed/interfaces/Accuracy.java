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

import java.util.Map;

/**
 * Interface for metrics that evaluate the correctness of trust values.
 *
 * @author David
 */
public interface Accuracy extends Metric {

    /**
     * Evaluates given trust values against given capabilities.
     *
     * @param trustDegrees A map of trust values, where keys represent agents and values
     *                     represent their trust degrees
     * @param capabilities A map of capabilities, where keys represent agents and values
     *                     represent their capabilities
     * @return An evaluation score between 0 and 1, inclusively.
     */
    public <T extends Comparable<T>> double evaluate(
            Map<Integer, T> trustDegrees, Map<Integer, Double> capabilities);
}
