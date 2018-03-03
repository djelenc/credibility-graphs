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

/**
 * Interface for implementing selection of opinion providers in scenarios. A
 * scenario that would allow Alpha to select opinion providers, has to implement
 * this interface.
 */
public interface OpinionProviderSelection {

    /**
     * Conveys opinion request to the scenario.
     * <p>
     * <p>
     * This method is used to tell the scenario, which opinions it should
     * generate in the current tick. The set of opinionRequest should be
     * obtained form the trust model.
     * <p>
     * <p>
     * This method should ignore invalid opinion requests; requests for opinions
     * from or about non-existing agents or for non-existing services.
     *
     * @param opinionRequests A list of opinionRequests.
     */
    public void setOpinionRequests(List<OpinionRequest> opinionRequests);
}
