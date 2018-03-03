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

import java.util.List;
import java.util.Map;

/**
 * Interface for defining capabilities, assigning deception models and selecting
 * collaborators for interactions.
 */
public interface Scenario {

    /**
     * Sets the random number generator. In order to produce repeatable results,
     * all random numbers should be generated with this generator.
     *
     * @param generator Generator to be set
     */
    public void setRandomGenerator(RandomGenerator generator);

    /**
     * Initializes the scenario with an optional array of varargs Objects.
     *
     * @param parameters Optional scenario parameters
     */
    public void initialize(Object... parameters);

    /**
     * Notifies the scenario of the current time. The testbed calls this method
     * at the beginning of every every step.
     *
     * @param time Current time
     */
    public void setCurrentTime(int time);

    /**
     * Gets the capabilities of agents that concern the given service.
     *
     * @param service
     * @return
     */
    public Map<Integer, Double> getCapabilities(int service);

    /**
     * Generates a set of {@link Opinion} tuples.
     *
     * @return
     */
    public List<Opinion> generateOpinions();

    /**
     * Generates a set of {@link Experience} tuples.
     *
     * @return
     */
    public List<Experience> generateExperiences();

    /**
     * Returns a set of ID numbers of agents.
     *
     * @return
     */
    public List<Integer> getAgents();

    /**
     * Returns a set of service types.
     *
     * @return
     */
    public List<Integer> getServices();

    /**
     * Returns an {@link ParametersPanel} instance, which is responsible for
     * generating a graphical user interface for setting scenario parameters.
     * <p>
     * <p>
     * When a scenario does not need parameters, the method should return null.
     *
     * @return
     */
    public ParametersPanel getParametersPanel();
}
