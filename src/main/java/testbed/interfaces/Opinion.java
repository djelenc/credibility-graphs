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
 * Represents an opinion tuple.
 * <p>
 * <p>
 * An opinion is a statement about trust that was given by one agent about a
 * third party to agent Alpha. We denote an opinion as a 5-tuple (agent1,
 * agent2, service, time, internalTrustDegree, uncertainty), where each member
 * has the following meaning.
 * <ul>
 * <li>int: agent1 -- represents the agent that provides the opinion
 * <li>int: agent2 -- represents the agent whom the opinion is about
 * <li>int: service -- represents the service for which the opinion is about
 * <li>int: time -- represents the time at which the opinion was given
 * <li>double internalTrustDegree -- represents the reported trust degree of
 * agent1 towards agent2. This value falls under [0, 1], where 1 is the best
 * possible value and 0 is the worst possible value.
 * <li>double uncertainty -- represents the amount of uncertainty. The value
 * falls into [0, 1], where 0 represents completely certain and 1 completely
 * uncertain opinion.
 * </ul>
 *
 * @author David
 */
public final class Opinion {
    /**
     * Opinion provider
     */
    public final int agent1;

    /**
     * The agent who the opinion is about
     */
    public final int agent2;

    /**
     * The type of service
     */
    public final int service;

    /**
     * Time when the opinion was given
     */
    public final int time;

    /**
     * The expressed level of trust between agent1 and agent2
     */
    public final double internalTrustDegree;

    /**
     * The level of uncertainty
     */
    public final double uncertainty;

    /**
     * Creates a new {@link Opinion} tuple.
     *
     * @param agent1  Opinion provider
     * @param agent2  The subject of the opinion
     * @param service The type of service
     * @param time    Time at which the opinion was given
     * @param itd     The internal trust degree
     */
    public Opinion(int agent1, int agent2, int service, int time, double itd,
                   double uncertainty) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.service = service;
        this.time = time;
        this.internalTrustDegree = itd;
        this.uncertainty = uncertainty;

    }

    @Override
    public String toString() {
        return String.format("Opinion<%d, %d, %d, %d, %.2f, %.2f>", agent1,
                agent2, service, time, internalTrustDegree, uncertainty);
    }
}
