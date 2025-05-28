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

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.event;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.childTypeError;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStream;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.CTFJsonMetadataNode;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypeAliasParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypedefParser;
import org.eclipse.tracecompass.internal.ctf.core.event.types.ICTFMetadataNode;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;

/**
 *
 *
 * An event stream can be divided into contiguous event packets of variable
 * size. An event packet can contain a certain amount of padding at the end. The
 * stream header is repeated at the beginning of each event packet. The
 * rationale for the event stream design choices is explained in Stream header
 * rationale.
 * <p>
 * The event stream header will therefore be referred to as the event packet
 * header throughout the rest of this document.
 *
 * @author Matthew Khouzam - Initial API and implementation
 * @author Efficios - Documentation
 *
 */
public final class EventParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter object with trace and scope
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {

        private final DeclarationScope fCurrentScope;
        private final CTFTrace fTrace;

        /**
         * Constructor
         *
         * @param trace
         *            the trace
         * @param currentScope
         *            the scope
         */
        public Param(CTFTrace trace, DeclarationScope currentScope) {
            fTrace = trace;
            fCurrentScope = currentScope;
        }

    }

    /**
     * The instance
     */
    public static final EventParser INSTANCE = new EventParser();

    private EventParser() {
    }

    /**
     * Parses an enum declaration and returns the corresponding declaration.
     *
     * @param eventNode
     *            An event node.
     * @param param
     *            the parameter object
     *
     * @return The corresponding enum declaration.
     * @throws ParseException
     *             event stream was badly defined
     */
    @Override
    public EventDeclaration parse(ICTFMetadataNode eventNode, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        Param parameter = (Param) param;
        CTFTrace trace = ((Param) param).fTrace;
        EventDeclaration event = new EventDeclaration();
        DeclarationScope scope = new DeclarationScope(parameter.fCurrentScope, MetadataStrings.EVENT);

        if (eventNode instanceof CTFJsonMetadataNode) {
            EventDeclarationParser.INSTANCE.parse(eventNode, new EventDeclarationParser.Param(trace, event, scope));
        } else {
            List<ICTFMetadataNode> children = eventNode.getChildren();
            if (children == null) {
                throw new ParseException("Empty event block"); //$NON-NLS-1$
            }

            for (ICTFMetadataNode child : children) {
                String type = child.getType();
                if (CTFParser.tokenNames[CTFParser.TYPEALIAS].equals(type)) {
                    TypeAliasParser.INSTANCE.parse(child, new TypeAliasParser.Param(trace, scope));
                } else if (CTFParser.tokenNames[CTFParser.TYPEDEF].equals(type)) {
                    TypedefParser.INSTANCE.parse(child, new TypedefParser.Param(trace, scope));
                } else if (CTFParser.tokenNames[CTFParser.CTF_EXPRESSION_TYPE].equals(type)
                        || CTFParser.tokenNames[CTFParser.CTF_EXPRESSION_VAL].equals(type)) {
                    EventDeclarationParser.INSTANCE.parse(child, new EventDeclarationParser.Param(trace, event, scope));
                } else {
                    throw childTypeError(child);
                }
            }
        }

        if (!event.nameIsSet()) {
            throw new ParseException("Event name not set"); //$NON-NLS-1$
        }

        /*
         * If the event did not specify a stream, then the trace must be single
         * stream
         */
        if (!event.streamIsSet()) {
            if (trace.nbStreams() > 1) {
                throw new ParseException("Event without stream_id with more than one stream"); //$NON-NLS-1$
            }

            /*
             * If the event did not specify a stream, the only existing stream
             * must not have an id. Note: That behavior could be changed, it
             * could be possible to just get the only existing stream, whatever
             * is its id.
             */
            ICTFStream iStream = trace.getStream(null);
            if (iStream instanceof CTFStream) {
                CTFStream ctfStream = (CTFStream) iStream;
                event.setStream(ctfStream);
            } else {
                throw new ParseException("Event without stream_id, but there is no stream without id"); //$NON-NLS-1$
            }
        }

        /*
         * Add the event to the stream.
         */
        event.getStream().addEvent(event);

        return event;
    }

}
