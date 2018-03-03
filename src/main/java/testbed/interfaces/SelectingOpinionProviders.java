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
 * Interface for implementing trust models that selects opinion providers.
 *
 * @author David
 */
public interface SelectingOpinionProviders {

    /**
     * This method returns a list of opinion requests that will be generated in
     * current tick.
     * <p>
     * <p>
     * Invalid request (for invalid agents or services) are ignored.
     *
     * @return A list of opinion requests.
     */
    public List<OpinionRequest> getOpinionRequests();

}
