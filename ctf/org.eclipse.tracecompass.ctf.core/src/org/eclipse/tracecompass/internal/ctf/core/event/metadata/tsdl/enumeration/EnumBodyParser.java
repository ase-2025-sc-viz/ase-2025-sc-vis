/*******************************************************************************
 * Copyright (c) 2015, 2023 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.enumeration;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.JsonStructureFieldMemberMetadataNode;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.types.ICTFMetadataNode;

/**
 * Body parser for an enumeration, this parses the list of elements in an enum
 *
 * @author Matthew Khouzam
 *
 */
public final class EnumBodyParser implements ICommonTreeParser {

    /**
     * Enum declaration parameter object
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {

        private final EnumDeclaration fEnumDeclaration;

        /**
         * Constructor
         *
         * @param enumDeclaration
         *            the enumeration
         */
        public Param(EnumDeclaration enumDeclaration) {
            fEnumDeclaration = enumDeclaration;
        }

    }

    /**
     * The instance
     */
    public static final EnumBodyParser INSTANCE = new EnumBodyParser();

    private EnumBodyParser() {
    }

    @Override
    public EnumDeclaration parse(ICTFMetadataNode tree, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        Param parameter = (Param) param;
        EnumDeclaration enumDeclaration = parameter.fEnumDeclaration;

        if (tree instanceof JsonStructureFieldMemberMetadataNode) {
            EnumeratorParser.INSTANCE.parse(tree, new EnumeratorParser.Param(enumDeclaration));
        } else {
            List<ICTFMetadataNode> enumerators = tree.getChildren();
            /*
             * Start at -1, so that if the first enumerator has no explicit
             * value, it will choose 0
             */
            for (ICTFMetadataNode enumerator : enumerators) {
                EnumeratorParser.INSTANCE.parse(enumerator, new EnumeratorParser.Param(enumDeclaration));
            }
        }
        return enumDeclaration;
    }

}
