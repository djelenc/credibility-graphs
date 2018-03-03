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
 * Interface for metrics that evaluate the obtained utility during a test run.
 *
 * @author David
 */
public interface Utility extends Metric {

    /**
     * Returns the normalized cumulative utility that has been obtained so far.
     *
     * @param capabilities A map of capabilities, where agents' ID numbers are used for
     *                     keys and their respective capabilities as values
     * @param agent        Agent with whom Alpha made an interaction.
     * @return An evaluation score between 0 and 1, inclusively.
     */
    public double evaluate(Map<Integer, Double> capabilities, int agent);
}
