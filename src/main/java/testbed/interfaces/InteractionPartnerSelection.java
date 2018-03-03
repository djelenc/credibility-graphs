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
 * Interface for implementing scenarios that allow agent Alpha to select
 * interaction partners.
 */
public interface InteractionPartnerSelection {

    /**
     * Sets the next interaction partners for the given services.
     * <p>
     * <p>
     * This method is used to tell the scenario, with which agents does agent
     * Alpha want to interact next. The parameter partners is a map, where keys
     * represent services and values agents. For instance, partners.put(3, 1),
     * indicates that agent Alpha wants to interact with agent 1 for the service
     * 3.
     * <p>
     * <p>
     * The programmer of the Scenario must ensure that the given values are
     * correct -- if, for instance, agent 1 (or service 3) are invalid values,
     * the method should throw {@link IllegalArgumentException}.
     *
     * @param partners Map of services and agents
     */
    public void setInteractionPartners(Map<Integer, Integer> partners);
}
