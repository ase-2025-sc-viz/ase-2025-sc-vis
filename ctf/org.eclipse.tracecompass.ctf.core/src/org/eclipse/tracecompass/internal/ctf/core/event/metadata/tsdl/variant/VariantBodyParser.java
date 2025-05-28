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
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.variant;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.VariantDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.JsonStructureFieldMemberMetadataNode;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypeAliasParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypedefParser;
import org.eclipse.tracecompass.internal.ctf.core.event.types.ICTFMetadataNode;

/**
 * Variant body parser. This handles all the inside of a variant, so it handles
 * everything between the '{' and '}' of a TSDL variant declaration.
 *
 * @author Matthew Khouzam
 *
 */
public final class VariantBodyParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter object
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final DeclarationScope fDeclarationScope;
        private final @Nullable String fName;
        private final VariantDeclaration fVariantDeclaration;
        private final CTFTrace fTrace;

        /**
         * Constructor
         *
         * @param variantDeclaration
         *            the declaration to populate
         * @param trace
         *            the trace
         * @param name
         *            the variant name
         * @param scope
         *            the current scope
         */
        public Param(VariantDeclaration variantDeclaration, CTFTrace trace, @Nullable String name, DeclarationScope scope) {
            fVariantDeclaration = variantDeclaration;
            fTrace = trace;
            fDeclarationScope = scope;
            fName = name;
        }
    }

    /**
     * The instance
     */
    public static final VariantBodyParser INSTANCE = new VariantBodyParser();

    private VariantBodyParser() {
    }

    /**
     * Parse the variant body, fills the variant with the results.
     *
     * @param variantBody
     *            the variant body AST node
     * @param param
     *            the {@link Param} parameter object
     * @return a populated {@link VariantDeclaration}
     * @throws ParseException
     *             if the AST is malformed
     */
    @Override
    public VariantDeclaration parse(ICTFMetadataNode variantBody, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }

        String variantName = ((Param) param).fName;
        VariantDeclaration variantDeclaration = ((Param) param).fVariantDeclaration;
        List<ICTFMetadataNode> variantDeclarations = variantBody.getChildren();

        final DeclarationScope scope = new DeclarationScope(((Param) param).fDeclarationScope, variantName == null ? MetadataStrings.VARIANT : variantName);
        CTFTrace trace = ((Param) param).fTrace;
        for (ICTFMetadataNode declarationNode : variantDeclarations) {
            String type = declarationNode.getType();
            if (CTFParser.tokenNames[CTFParser.TYPEALIAS].equals(type)) {
                TypeAliasParser.INSTANCE.parse(declarationNode, new TypeAliasParser.Param(trace, scope));
            } else if (CTFParser.tokenNames[CTFParser.TYPEDEF].equals(type)) {
                Map<String, IDeclaration> decs = TypedefParser.INSTANCE.parse(declarationNode, new TypedefParser.Param(trace, scope));
                for (Entry<String, IDeclaration> declarationEntry : decs.entrySet()) {
                    variantDeclaration.addField(declarationEntry.getKey(), declarationEntry.getValue());
                }
            } else if (CTFParser.tokenNames[CTFParser.SV_DECLARATION].equals(type) || variantBody instanceof JsonStructureFieldMemberMetadataNode) {
                VariantDeclarationParser.INSTANCE.parse(declarationNode, new VariantDeclarationParser.Param(variantDeclaration, trace, scope));
            } else {
                throw childTypeError(declarationNode);
            }
        }
        return variantDeclaration;
    }
}
