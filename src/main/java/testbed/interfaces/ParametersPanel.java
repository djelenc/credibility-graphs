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

import java.awt.*;
import java.util.Observer;

/**
 * Provides method with which various components (implementations of
 * {@link TrustModel}, {@link Scenario}, ...) provide GUI for setting their
 * parameters.
 *
 * @author David
 */
public interface ParametersPanel {

    /**
     * Initializes the parameters panel.
     *
     * @param o      An observer instance, which can be notified when parameters
     *               are valid by calling
     *               {@link Observer#update(java.util.Observable, Object)}. The
     *               first argument can be null, while the second should be True if
     *               the parameters are valid, or false, when they are invalid.
     * @param params Optional parameters.
     */
    public void initialize(Observer o, Object... params);

    /**
     * Returns an {@link java.awt.Component} instance that holds the GUI for the
     * parameters.
     *
     * @return
     */
    public Component getComponent();

    /**
     * Returns an object of parameters for a particular component.
     *
     * @return
     */
    public Object[] getParameters();
}
