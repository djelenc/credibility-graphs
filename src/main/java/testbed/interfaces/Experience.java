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
 * Represents an experience tuple.
 * <p>
 * <p>
 * An experience is a record of an interaction between some agent and agent
 * Alpha. We denote experience as a 4-tuple (agent, service, time, outcome),
 * where each member has the following meaning.
 * <ul>
 * <li>int: agent -- represents the agent that interacted with agent Alpha
 * <li>int: service -- represents the of service for which the interaction was
 * made
 * <li>int: time -- represents the time at which the interaction was made
 * <li>double: outcome -- represents the outcome of the interaction. This value
 * falls under [0, 1], where 1 is the best possible outcome and 0 is the worst
 * possible outcome.
 * </ul>
 *
 * @author David
 */
public final class Experience {
    /**
     * Agent that provides the service
     */
    public final int agent;

    /**
     * The type of service
     */
    public final int service;

    /**
     * Time of the interaction
     */
    public final int time;

    /**
     * The interaction outcome
     */
    public final double outcome;

    /**
     * Creates a new {@link Experience} tuple
     *
     * @param interaction counter-part
     * @param service     type of service
     * @param time        of the interaction
     * @param outcome     of the interaction
     */
    public Experience(int agent, int service, int time, double outcome) {
        this.agent = agent;
        this.service = service;
        this.time = time;
        this.outcome = outcome;
    }

    @Override
    public String toString() {
        return String.format("Exp<%d, %d, %d, %.2f>", agent, service, time,
                outcome);
    }
}
