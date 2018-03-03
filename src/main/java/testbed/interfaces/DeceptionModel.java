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
 * Interface for defining deception models; ways of defining how agents report
 * opinions to agent Alpha.
 *
 * @author David
 */
public interface DeceptionModel {

    /**
     * Initialization method. Called at the beginning of the evaluation.
     *
     * @param params
     */
    public void initialize(Object... params);

    /**
     * Transforms a given trust degree to a trust degree that is given to agent
     * Alpha.
     *
     * @param trustDegree Given trust degree.
     * @return Transformed trust degree.
     */
    public double calculate(double trustDegree);
}
