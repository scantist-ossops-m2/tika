/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.parser.multiple.AbstractMultipleParser.MetadataPolicy;
import org.apache.tika.parser.multiple.FallbackParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Decorator base class for the {@link Parser} interface.
 *
 * <p>This class simply delegates all parsing calls to an underlying decorated parser instance.
 * Subclasses can provide extra decoration by overriding the parse method.
 *
 * <p>To decorate several different parsers at the same time, wrap them in a {@link CompositeParser}
 * instance first.
 */
public class ParserDecorator implements Parser {

    /** Serial version UID */
    private static final long serialVersionUID = -3861669115439125268L;

    /** The decorated parser instance. */
    private final Parser parser;

    /**
     * Creates a decorator for the given parser.
     *
     * @param parser the parser instance to be decorated
     */
    public ParserDecorator(Parser parser) {
        this.parser = parser;
    }

    /**
     * Decorates the given parser so that it always claims to support parsing of the given media
     * types.
     *
     * @param parser the parser to be decorated
     * @param types supported media types
     * @return the decorated parser
     */
    public static final Parser withTypes(Parser parser, final Set<MediaType> types) {
        return new ParserDecorator(parser) {
            private static final long serialVersionUID = -7345051519565330731L;

            @Override
            public Set<MediaType> getSupportedTypes(ParseContext context) {
                return types;
            }

            @Override
            public String getDecorationName() {
                return "With Types";
            }
        };
    }

    /**
     * Decorates the given parser so that it never claims to support parsing of the given media
     * types, but will work for all others.
     *
     * @param parser the parser to be decorated
     * @param excludeTypes excluded/ignored media types
     * @return the decorated parser
     */
    public static final Parser withoutTypes(Parser parser, final Set<MediaType> excludeTypes) {
        return new ParserDecorator(parser) {
            private static final long serialVersionUID = 7979614774021768609L;

            @Override
            public Set<MediaType> getSupportedTypes(ParseContext context) {
                // Get our own, writable copy of the types the parser supports
                Set<MediaType> parserTypes = new HashSet<>(super.getSupportedTypes(context));
                // Remove anything on our excludes list
                parserTypes.removeAll(excludeTypes);
                // Return whatever is left
                return parserTypes;
            }

            @Override
            public String getDecorationName() {
                return "Without Types";
            }
        };
    }

    /**
     * Decorates the given parsers into a virtual parser, where they'll be tried in preference order
     * until one works without error.
     *
     * @deprecated This has been replaced by {@link FallbackParser}
     */
    @Deprecated
    public static final Parser withFallbacks(
            final Collection<? extends Parser> parsers, final Set<MediaType> types) {
        // Delegate to the new FallbackParser for now, until people upgrade
        // Keep old behaviour on metadata, which was to preseve all
        MediaTypeRegistry registry = MediaTypeRegistry.getDefaultRegistry();
        Parser p = new FallbackParser(registry, MetadataPolicy.KEEP_ALL, parsers);

        if (types == null || types.isEmpty()) {
            return p;
        }
        return withTypes(p, types);
    }

    /**
     * Delegates the method call to the decorated parser. Subclasses should override this method
     * (and use <code>super.getSupportedTypes()</code> to invoke the decorated parser) to implement
     * extra decoration.
     */
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return parser.getSupportedTypes(context);
    }

    /**
     * Delegates the method call to the decorated parser. Subclasses should override this method
     * (and use <code>super.parse()</code> to invoke the decorated parser) to implement extra
     * decoration.
     */
    public void parse(
            InputStream stream, ContentHandler handler, Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {
        parser.parse(stream, handler, metadata, context);
    }

    /**
     * @return A name/description of the decoration, or null if none available
     */
    public String getDecorationName() {
        return null;
    }

    /**
     * Gets the parser wrapped by this ParserDecorator
     *
     * @return the parser wrapped by this ParserDecorator
     */
    public Parser getWrappedParser() {
        return this.parser;
    }
}
