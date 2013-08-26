/**
 * ADD LICENSE
 */
package com.salesforce.omakase;

import static com.google.common.base.Preconditions.checkNotNull;

import com.salesforce.omakase.error.ErrorManager;
import com.salesforce.omakase.error.ThrowingErrorManager;
import com.salesforce.omakase.parser.ParserFactory;
import com.salesforce.omakase.parser.Stream;
import com.salesforce.omakase.plugin.Plugin;

/**
 * TODO Description
 * 
 * <p>
 * doesn't support namespaces, imports, charset, at-page, cdo, cdc, most escaping, font-face, media queries, comments,
 * strings?
 * 
 * <p>
 * This library is <em>not</em> thread-safe.
 * 
 * @author nmcwilliams
 */
public final class Omakase {
    /** do not construct */
    private Omakase() {}

    /**
     * TODO Description
     * 
     * @param source
     *            TODO
     * @return TODO
     */
    public static Omakase.Request source(CharSequence source) {
        checkNotNull(source, "source cannot be null");
        return new Request(source);
    }

    /**
     * TODO Description
     */
    public static final class Request {
        private final Context context;
        private final Stream stream;
        private ErrorManager em;

        Request(CharSequence source) {
            this.stream = new Stream(source.toString());
            this.context = new Context();
            this.em = new ThrowingErrorManager();
        }

        /**
         * TODO Description
         * 
         * @param plugins
         *            TODO
         * @return TODO
         */
        public Request request(Plugin... plugins) {
            context.plugins(plugins);
            return this;
        }

        /**
         * TODO Description
         * 
         * @param plugins
         *            TODO
         * @return TODO
         */
        public Request add(Plugin... plugins) {
            return request(plugins);
        }

        /**
         * TODO Description
         * 
         * @param em
         *            TODO
         * @return TODO
         */
        public Request errorManager(ErrorManager em) {
            this.em = checkNotNull(em, "the error manager cannot be null");
            return this;
        }

        /**
         * TODO Description
         * 
         * @return TODO
         */
        public Context process() {
            context.before();
            ParserFactory.stylesheetParser().parse(stream, context);
            context.after();
            return context;
        }
    }
}
