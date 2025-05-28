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

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;
import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.isAnyUnaryString;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.metadata.DeclarationScope;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStream;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.AbstractScopedCommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.CTFAntlrMetadataNode;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.CTFJsonMetadataNode;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.JsonEventRecordMetadataNode;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.JsonStructureFieldMetadataNode;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TypeSpecifierListParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryIntegerParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.UnaryStringParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.stream.StreamIdParser;
import org.eclipse.tracecompass.internal.ctf.core.event.types.ICTFMetadataNode;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;

/**
 * The declaration of an event
 *
 * @author Matthew Khouzam - Initial API and implementation
 *
 */
public final class EventDeclarationParser extends AbstractScopedCommonTreeParser {

    /**
     * Parameter object, contains a trace, an event and a scope
     *
     * @author Matthew Khouzam
     *
     */
    @NonNullByDefault
    public static final class Param implements ICommonTreeParserParameter {
        private final EventDeclaration fEvent;
        private final CTFTrace fTrace;
        private final DeclarationScope fDeclarationScope;

        /**
         * Constructor
         *
         * @param trace
         *            the trace
         * @param event
         *            the event to populate
         * @param scope
         *            the current scope
         */
        public Param(CTFTrace trace, EventDeclaration event, DeclarationScope scope) {
            fTrace = trace;
            fEvent = event;
            fDeclarationScope = scope;
        }

    }

    /**
     * Instance
     */
    public static final EventDeclarationParser INSTANCE = new EventDeclarationParser();

    private EventDeclarationParser() {
    }

    @Override
    public EventDeclaration parse(ICTFMetadataNode eventDecl, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        DeclarationScope scope = ((Param) param).fDeclarationScope;
        EventDeclaration event = ((Param) param).fEvent;
        CTFTrace fTrace = ((Param) param).fTrace;

        if (eventDecl instanceof CTFJsonMetadataNode) {
            JsonEventRecordMetadataNode rec = (JsonEventRecordMetadataNode) eventDecl;

            String name = rec.getName();
            if (name != null) {
                event.setName(name);
            }

            // default is 0
            long id = rec.getId();
            verifyEventId(event, id);
            event.setId(id);

            // default is 0
            long streamId = rec.getDataStreamClassId();
            CTFStream ctfStream = verifyStream(fTrace, streamId);
            event.setStream(ctfStream);

            JsonStructureFieldMetadataNode context = rec.getSpecificContextClass();
            if (context != null) {
                IDeclaration contextDecl = TypeSpecifierListParser.INSTANCE.parse(rec.getSpecificContextClass(), new TypeSpecifierListParser.Param(fTrace, null, null, scope));
                verifyContext(contextDecl);
                event.setContext((StructDeclaration) contextDecl);
            }

            JsonStructureFieldMetadataNode payload = rec.getPayloadFieldClass();
            if (payload != null) {
                IDeclaration fieldsDecl = TypeSpecifierListParser.INSTANCE.parse(rec.getPayloadFieldClass(), new TypeSpecifierListParser.Param(fTrace, null, null, scope));
                StructDeclaration fields = verifyFieldsDeclaration(fieldsDecl);
                event.setFields(fields);
            }

        } else if (eventDecl instanceof CTFAntlrMetadataNode) {
            /* There should be a left and right */

            ICTFMetadataNode leftNode = eventDecl.getChild(0);
            ICTFMetadataNode rightNode = eventDecl.getChild(1);

            List<ICTFMetadataNode> leftStrings = leftNode.getChildren();

            if (!isAnyUnaryString(leftStrings.get(0))) {
                throw new ParseException("left side of CTF assignment must be a string"); //$NON-NLS-1$
            }

            String left = concatenateUnaryStrings(leftStrings);

            if (left.equals(MetadataStrings.NAME2)) {
                if (event.nameIsSet()) {
                    throw new ParseException("name already defined"); //$NON-NLS-1$
                }

                String name = EventNameParser.INSTANCE.parse(rightNode, null);

                event.setName(name);
            } else if (left.equals(MetadataStrings.ID)) {
                long id = EventIDParser.INSTANCE.parse(rightNode, null);

                verifyEventId(event, id);
                event.setId((int) id);
            } else if (left.equals(MetadataStrings.STREAM_ID)) {
                if (event.streamIsSet()) {
                    throw new ParseException("stream id already defined"); //$NON-NLS-1$
                }

                long streamId = StreamIdParser.INSTANCE.parse(rightNode, null);

                CTFStream ctfStream = verifyStream(fTrace, streamId);
                event.setStream(ctfStream);
            } else if (left.equals(MetadataStrings.CONTEXT)) {
                if (event.contextIsSet()) {
                    throw new ParseException("context already defined"); //$NON-NLS-1$
                }

                ICTFMetadataNode typeSpecifier = rightNode.getChild(0);

                if (!(CTFParser.tokenNames[CTFParser.TYPE_SPECIFIER_LIST].equals(typeSpecifier.getType()))) {
                    throw new ParseException("context expects a type specifier"); //$NON-NLS-1$
                }

                IDeclaration contextDecl = TypeSpecifierListParser.INSTANCE.parse(typeSpecifier, new TypeSpecifierListParser.Param(fTrace, null, null, scope));

                verifyContext(contextDecl);

                event.setContext((StructDeclaration) contextDecl);
            } else if (left.equals(MetadataStrings.FIELDS_STRING)) {
                if (event.fieldsIsSet()) {
                    throw new ParseException("fields already defined"); //$NON-NLS-1$
                }

                ICTFMetadataNode typeSpecifier = rightNode.getChild(0);

                if (!(CTFParser.tokenNames[CTFParser.TYPE_SPECIFIER_LIST].equals(typeSpecifier.getType()))) {
                    throw new ParseException("fields expects a type specifier"); //$NON-NLS-1$
                }

                IDeclaration fieldsDecl;
                fieldsDecl = TypeSpecifierListParser.INSTANCE.parse(typeSpecifier, new TypeSpecifierListParser.Param(fTrace, null, null, scope));

                final StructDeclaration fields = verifyFieldsDeclaration(fieldsDecl);
                event.setFields(fields);
            } else if (left.equals(MetadataStrings.LOGLEVEL2)) {
                long logLevel = UnaryIntegerParser.INSTANCE.parse(rightNode.getChild(0), null);
                event.setLogLevel(logLevel);
            } else {
                /* Custom event attribute, we'll add it to the attributes map */
                String right = UnaryStringParser.INSTANCE.parse(rightNode.getChild(0), null);
                event.setCustomAttribute(left, right);
            }
        }
        return event;
    }

    private static void verifyContext(IDeclaration contextDecl) throws ParseException {
        if (!(contextDecl instanceof StructDeclaration)) {
            throw new ParseException("context expects a struct"); //$NON-NLS-1$
        }
    }

    private static StructDeclaration verifyFieldsDeclaration(IDeclaration fieldsDecl) throws ParseException {
        if (!(fieldsDecl instanceof StructDeclaration)) {
            throw new ParseException("fields expects a struct"); //$NON-NLS-1$
        }
        /*
         * The underscores in the event names. These underscores were added by
         * the LTTng tracer.
         */
        return (StructDeclaration) fieldsDecl;
    }

    /*
     * If the event has a stream and it is defined, look up the stream, if it is
     * available, assign it. If not, the event is malformed.
     */
    private static CTFStream verifyStream(CTFTrace fTrace, long streamId) throws ParseException {
        ICTFStream iStream = fTrace.getStream(streamId);
        if (!(iStream instanceof CTFStream)) {
            throw new ParseException("Event specified stream with ID " + streamId + ". But no stream with that ID was defined"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return (CTFStream) iStream;
    }

    private static void verifyEventId(EventDeclaration event, long id) throws ParseException {
        if (event.idIsSet()) {
            throw new ParseException("id already defined"); //$NON-NLS-1$
        }
        if (id > Integer.MAX_VALUE) {
            throw new ParseException("id is greater than int.maxvalue, unsupported. id : " + id); //$NON-NLS-1$
        }
        if (id < 0) {
            throw new ParseException("negative id, unsupported. id : " + id); //$NON-NLS-1$
        }
    }
}
