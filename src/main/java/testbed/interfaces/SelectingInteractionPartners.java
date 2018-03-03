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
 * Interface for implementing trust models that select interaction partners.
 *
 * @author David
 */
public interface SelectingInteractionPartners {

    /**
     * This method should return a map that defines partner agents, with which
     * Alpha wants to interact in the current tick.
     * <p>
     * <p>
     * Keys in the resulting map must represent services, while values must
     * represent agents. Thus it is possible to interact with a single agent
     * more than once per time tick, but each interaction must be for a
     * different service.
     * <p>
     * <p>
     * The map must contain only valid agents and services, otherwise an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param services List of available types of services
     * @return A map representing partner selections.
     */
    public Map<Integer, Integer> getInteractionPartners(List<Integer> services);

}
