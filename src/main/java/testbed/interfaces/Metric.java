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

/**
 * An umbrella interface for metrics. This interface provides a basis for the
 * making interfaces for other (more specific) metrics, such as {@link Accuracy}
 * or {@link Utility}.
 *
 * @author David
 */
public interface Metric {

    /**
     * Initializes the metric with optional parameters.
     *
     * @param params Optional parameters
     */
    public void initialize(Object... params);

    /**
     * Returns {@link ParametersPanel} instance, which defines GUI for setting
     * parameters for this metric.
     * <p>
     * If the metric needs no parameters, this method should return null.
     *
     * @return Instance of the {@link ParametersPanel} or null if parameters are
     * not needed.
     */
    public ParametersPanel getParametersPanel();
}
