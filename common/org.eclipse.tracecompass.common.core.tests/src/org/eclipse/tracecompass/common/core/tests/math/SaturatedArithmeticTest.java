/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.math;

import org.eclipse.tracecompass.common.core.math.SaturatedArithmetic;

/**
 * Test suite for the {@link SaturatedArithmetic} class.
 * <p>
 * @author Matthew Khouzam
 */
public class SaturatedArithmeticTest extends BaseSaturatedArithmeticTest {
    @Override
    protected int multiply(int left, int right) {
        return SaturatedArithmetic.multiply(left, right);
    }

    @Override
    protected long multiply(long left, long right) {
        return SaturatedArithmetic.multiply(left, right);    }

    @Override
    protected int add(int left, int right) {
        return SaturatedArithmetic.add(left, right);
    }

    @Override
    protected long add(long left, long right) {
        return SaturatedArithmetic.add(left, right);
    }

    @Override
    protected boolean sameSign(long left, long right) {
        return SaturatedArithmetic.sameSign(left, right);
    }
}
