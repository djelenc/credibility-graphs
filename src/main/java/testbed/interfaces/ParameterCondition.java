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
 * Interface for functor instances to perform validation on the varargs
 *
 * @param <T> Parameter type
 * @author David
 */
public interface ParameterCondition<T> {
    /**
     * If the parameter is valid the method does NOT throw an
     * {@link IllegalArgumentException}. If the parameter is invalid the method
     * throws an {@link IllegalArgumentException}.
     *
     * @param var
     */
    public void eval(T var) throws IllegalArgumentException;
}
