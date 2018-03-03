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
 * The core interface for implementing trust models.
 *
 * @param <T> The data type in which the trust model conveys trust
 * @author David
 */
public interface TrustModel<T extends Comparable<T>> {

    /**
     * Initializes the trust model with an optional array of varargs Objects.
     * Called only once, at the very start of the test run.
     *
     * @param params Optional parameters
     */
    public void initialize(Object... params);

    /**
     * Notifies the trust model of the current time. Called once at every step
     * of the test run.
     *
     * @param time Current time
     */
    public void setCurrentTime(int time);

    /**
     * Conveys the list of available services to the trust model.
     *
     * @param services
     */
    public void setServices(List<Integer> services);

    /**
     * Conveys the list of available agents to the trust model.
     *
     * @param agents
     */
    public void setAgents(List<Integer> agents);

    /**
     * Conveys opinions to the trust model.
     *
     * @param opinions List of opinions
     */
    public void processOpinions(List<Opinion> opinions);

    /**
     * Conveys experiences to the trust model.
     *
     * @param experiences List of experiences
     */
    public void processExperiences(List<Experience> experiences);

    /**
     * Calculates trust towards agents in the system. The calculated trust must
     * be stored internally. The method is called once at every step of the test
     * run.
     */
    public void calculateTrust();

    /**
     * Returns the calculated trust values for a given service. The trust values
     * have to be packaged in a map, whose keys represent agents and its values
     * represent their computed trust values.
     *
     * @param service type of service
     * @return Map where keys represent agents and values computed trust values
     */
    public Map<Integer, T> getTrust(int service);

    /**
     * Returns an {@link ParametersPanel} instance, which is responsible for
     * generating a graphical user interface for setting trust model's
     * parameters.
     * <p>
     * <p>
     * When a model does not need parameters, the method should return null.
     *
     * @return
     */
    public ParametersPanel getParametersPanel();

    /**
     * Sets the random number generator. In order to produce repeatable results,
     * all random numbers should be generated with this generator.
     *
     * @param generator Generator to be set
     */
    public void setRandomGenerator(RandomGenerator generator);
}
