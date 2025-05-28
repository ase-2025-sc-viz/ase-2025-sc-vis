/**********************************************************************
 * Copyright (c) 2020, 2023 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.core.dataprovider;

/**
 * Enumeration used to describe the type of data that a value represents. The
 * data type will help decide how to format the data to be displayed to the user
 *
 * @author Geneviève Bastien
 * @since 6.1
 */
public enum DataType {
    /**
     * Data represent a decimal number
     */
    NUMBER,
    /**
     * Binary data, where the size orders are powers of 2.
     */
    BINARY_NUMBER,
    /**
     * Data represent a timestamp in nanoseconds, can be negative
     */
    TIMESTAMP,
    /**
     * Data represents a duration
     */
    DURATION,
    /**
     * Data is textual data
     */
    STRING,
    /**
     * Data representing a time range of string: [start,end], where `start` and
     * `end` are timestamps in nanoseconds
     *
     * @since 9.0
     */
    TIME_RANGE;
}