/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.glowroot.plugin.logger;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.glowroot.Containers;
import org.glowroot.container.AppUnderTest;
import org.glowroot.container.Container;
import org.glowroot.container.TraceMarker;
import org.glowroot.container.trace.Trace;
import org.glowroot.container.trace.TraceEntry;

import static org.assertj.core.api.Assertions.assertThat;

public class Log4jTest {

    private static Container container;

    @BeforeClass
    public static void setUp() throws Exception {
        container = Containers.getSharedContainer();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        container.close();
    }

    @After
    public void afterEachTest() throws Exception {
        container.checkAndReset();
    }

    @Test
    public void testLog() throws Exception {
        // given
        container.getConfigService().setPluginProperty("logger",
                "traceErrorOnErrorWithoutThrowable", true);
        // when
        container.executeAppUnderTest(ShouldLog.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg");
        assertThat(entries).hasSize(4);
        assertThat(entries.get(1).getMessage().getText()).isEqualTo("log warn: def");
        assertThat(entries.get(2).getMessage().getText()).isEqualTo("log error: efg");
        assertThat(entries.get(3).getMessage().getText()).isEqualTo("log fatal: fgh");
    }

    @Test
    public void testLogWithThrowable() throws Exception {
        // given
        container.getConfigService().setPluginProperty("logger",
                "traceErrorOnErrorWithoutThrowable", true);
        // when
        container.executeAppUnderTest(ShouldLogWithThrowable.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg_");
        assertThat(entries).hasSize(4);

        TraceEntry warnEntry = entries.get(1);
        assertThat(warnEntry.getMessage().getText()).isEqualTo("log warn: def_");
        assertThat(warnEntry.getError().getText()).isEqualTo("456");
        assertThat(warnEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");

        TraceEntry errorEntry = entries.get(2);
        assertThat(errorEntry.getMessage().getText()).isEqualTo("log error: efg_");
        assertThat(errorEntry.getError().getText())
                .isEqualTo("567");
        assertThat(errorEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");

        TraceEntry fatalEntry = entries.get(3);
        assertThat(fatalEntry.getMessage().getText()).isEqualTo("log fatal: fgh_");
        assertThat(fatalEntry.getError().getText())
                .isEqualTo("678");
        assertThat(fatalEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");
    }

    @Test
    public void testLogWithNullThrowable() throws Exception {
        // given
        container.getConfigService().setPluginProperty("logger",
                "traceErrorOnErrorWithoutThrowable", true);
        // when
        container.executeAppUnderTest(ShouldLogWithNullThrowable.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg_");
        assertThat(entries).hasSize(4);

        TraceEntry warnEntry = entries.get(1);
        assertThat(warnEntry.getMessage().getText()).isEqualTo("log warn: def_");
        assertThat(warnEntry.getError().getText()).isEqualTo("def_");
        TraceEntry errorEntry = entries.get(2);
        assertThat(errorEntry.getMessage().getText()).isEqualTo("log error: efg_");
        assertThat(errorEntry.getError().getText()).isEqualTo("efg_");
        TraceEntry fatalEntry = entries.get(3);
        assertThat(fatalEntry.getMessage().getText()).isEqualTo("log fatal: fgh_");
        assertThat(fatalEntry.getError().getText()).isEqualTo("fgh_");
    }

    @Test
    public void testLogWithPriority() throws Exception {
        // given
        container.getConfigService().setPluginProperty("logger",
                "traceErrorOnErrorWithoutThrowable", true);
        // when
        container.executeAppUnderTest(ShouldLogWithPriority.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg__");
        assertThat(entries).hasSize(4);
        assertThat(entries.get(1).getMessage().getText()).isEqualTo("log warn: def__");
        assertThat(entries.get(2).getMessage().getText()).isEqualTo("log error: efg__");
        assertThat(entries.get(3).getMessage().getText()).isEqualTo("log fatal: fgh__");
    }

    @Test
    public void testLogWithPriorityAndThrowable() throws Exception {
        // given
        // when
        container.executeAppUnderTest(ShouldLogWithPriorityAndThrowable.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg___");
        assertThat(entries).hasSize(4);

        TraceEntry warnEntry = entries.get(1);
        assertThat(warnEntry.getMessage().getText()).isEqualTo("log warn: def___");
        assertThat(warnEntry.getError().getText())
                .isEqualTo("456_");
        assertThat(warnEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");

        TraceEntry errorEntry = entries.get(2);
        assertThat(errorEntry.getMessage().getText()).isEqualTo("log error: efg___");
        assertThat(errorEntry.getError().getText())
                .isEqualTo("567_");
        assertThat(errorEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");

        TraceEntry fatalEntry = entries.get(3);
        assertThat(fatalEntry.getMessage().getText()).isEqualTo("log fatal: fgh___");
        assertThat(fatalEntry.getError().getText())
                .isEqualTo("678_");
        assertThat(fatalEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");
    }

    @Test
    public void testLogWithPriorityAndNullThrowable() throws Exception {
        // given
        container.getConfigService().setPluginProperty("logger",
                "traceErrorOnErrorWithoutThrowable", true);
        // when
        container.executeAppUnderTest(ShouldLogWithPriorityAndNullThrowable.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg___null");
        assertThat(entries).hasSize(4);

        TraceEntry warnEntry = entries.get(1);
        assertThat(warnEntry.getMessage().getText()).isEqualTo("log warn: def___null");
        assertThat(warnEntry.getError().getText()).isEqualTo("def___null");
        TraceEntry errorEntry = entries.get(2);
        assertThat(errorEntry.getMessage().getText()).isEqualTo("log error: efg___null");
        assertThat(errorEntry.getError().getText()).isEqualTo("efg___null");
        TraceEntry fatalEntry = entries.get(3);
        assertThat(fatalEntry.getMessage().getText()).isEqualTo("log fatal: fgh___null");
        assertThat(fatalEntry.getError().getText()).isEqualTo("fgh___null");
    }

    @Test
    public void testLocalizedLog() throws Exception {
        // given
        // when
        container.executeAppUnderTest(ShouldLocalizedLog.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg____");
        assertThat(entries).hasSize(4);

        TraceEntry warnEntry = entries.get(1);
        assertThat(warnEntry.getMessage().getText()).isEqualTo("log warn (localized): def____");
        assertThat(warnEntry.getError().getText())
                .isEqualTo("456__");
        assertThat(warnEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");

        TraceEntry errorEntry = entries.get(2);
        assertThat(errorEntry.getMessage().getText()).isEqualTo("log error (localized): efg____");
        assertThat(errorEntry.getError().getText())
                .isEqualTo("567__");
        assertThat(errorEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");

        TraceEntry fatalEntry = entries.get(3);
        assertThat(fatalEntry.getMessage().getText()).isEqualTo("log fatal (localized): fgh____");
        assertThat(fatalEntry.getError().getText())
                .isEqualTo("678__");
        assertThat(fatalEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");
    }

    @Test
    public void testLocalizedLogWithNullThrowable() throws Exception {
        // given
        container.getConfigService().setPluginProperty("logger",
                "traceErrorOnErrorWithoutThrowable", true);
        // when
        container.executeAppUnderTest(ShouldLocalizedLogWithNullThrowable.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg____null");
        assertThat(entries).hasSize(4);

        TraceEntry warnEntry = entries.get(1);
        assertThat(warnEntry.getMessage().getText()).isEqualTo("log warn (localized): def____null");
        assertThat(warnEntry.getError().getText()).isEqualTo("def____null");
        TraceEntry errorEntry = entries.get(2);
        assertThat(errorEntry.getMessage().getText())
                .isEqualTo("log error (localized): efg____null");
        assertThat(errorEntry.getError().getText()).isEqualTo("efg____null");
        TraceEntry fatalEntry = entries.get(3);
        assertThat(fatalEntry.getMessage().getText())
                .isEqualTo("log fatal (localized): fgh____null");
        assertThat(fatalEntry.getError().getText()).isEqualTo("fgh____null");
    }

    @Test
    public void testLocalizedLogWithParameters() throws Exception {
        // given
        // when
        container.executeAppUnderTest(ShouldLocalizedLogWithParameters.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg____");
        assertThat(entries).hasSize(4);

        TraceEntry warnEntry = entries.get(1);
        assertThat(warnEntry.getMessage().getText())
                .isEqualTo("log warn (localized): def____ [d, e, f]");
        assertThat(warnEntry.getError().getText())
                .isEqualTo("456__");
        assertThat(warnEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");

        TraceEntry errorEntry = entries.get(2);
        assertThat(errorEntry.getMessage().getText())
                .isEqualTo("log error (localized): efg____ [e, f, g]");
        assertThat(errorEntry.getError().getText())
                .isEqualTo("567__");
        assertThat(errorEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");

        TraceEntry fatalEntry = entries.get(3);
        assertThat(fatalEntry.getMessage().getText())
                .isEqualTo("log fatal (localized): fgh____ [f, g, h]");
        assertThat(fatalEntry.getError().getText())
                .isEqualTo("678__");
        assertThat(fatalEntry.getError().getException().getStackTrace().get(0))
                .contains("traceMarker");
    }

    @Test
    public void testLocalizedLogWithParametersAndNullThrowable() throws Exception {
        // given
        container.getConfigService().setPluginProperty("logger",
                "traceErrorOnErrorWithoutThrowable", true);
        // when
        container.executeAppUnderTest(ShouldLocalizedLogWithParametersAndNullThrowable.class);
        // then
        Trace trace = container.getTraceService().getLastTrace();
        List<TraceEntry> entries = container.getTraceService().getEntries(trace.getId());
        assertThat(trace.getError()).isEqualTo("efg____null");
        assertThat(entries).hasSize(4);

        TraceEntry warnEntry = entries.get(1);
        assertThat(warnEntry.getMessage().getText())
                .isEqualTo("log warn (localized): def____null [d_, e_, f_]");
        assertThat(warnEntry.getError().getText()).isEqualTo("def____null [d_, e_, f_]");
        TraceEntry errorEntry = entries.get(2);
        assertThat(errorEntry.getMessage().getText())
                .isEqualTo("log error (localized): efg____null [e_, f_, g_]");
        assertThat(errorEntry.getError().getText()).isEqualTo("efg____null [e_, f_, g_]");
        TraceEntry fatalEntry = entries.get(3);
        assertThat(fatalEntry.getMessage().getText())
                .isEqualTo("log fatal (localized): fgh____null [f_, g_, h_]");
        assertThat(fatalEntry.getError().getText()).isEqualTo("fgh____null [f_, g_, h_]");
    }

    public static class ShouldLog implements AppUnderTest, TraceMarker {
        private static final Logger logger = Logger.getLogger(ShouldLog.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.trace("abc");
            logger.debug("bcd");
            logger.info("cde");
            logger.warn("def");
            logger.error("efg");
            logger.fatal("fgh");
        }
    }

    public static class ShouldLogWithThrowable implements AppUnderTest, TraceMarker {
        private static final Logger logger = Logger.getLogger(ShouldLogWithThrowable.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.trace("abc_", new IllegalStateException("123"));
            logger.debug("bcd_", new IllegalStateException("234"));
            logger.info("cde_", new IllegalStateException("345"));
            logger.warn("def_", new IllegalStateException("456"));
            logger.error("efg_", new IllegalStateException("567"));
            logger.fatal("fgh_", new IllegalStateException("678"));
        }
    }

    public static class ShouldLogWithNullThrowable implements AppUnderTest, TraceMarker {
        private static final Logger logger = Logger.getLogger(ShouldLogWithNullThrowable.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.trace("abc_", null);
            logger.debug("bcd_", null);
            logger.info("cde_", null);
            logger.warn("def_", null);
            logger.error("efg_", null);
            logger.fatal("fgh_", null);
        }
    }

    public static class ShouldLogWithPriority implements AppUnderTest, TraceMarker {
        private static final Logger logger = Logger.getLogger(ShouldLogWithPriority.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.log(Level.TRACE, "abc__");
            logger.log(Level.DEBUG, "bcd__");
            logger.log(Level.INFO, "cde__");
            logger.log(Level.WARN, "def__");
            logger.log(Level.ERROR, "efg__");
            logger.log(Level.FATAL, "fgh__");
        }
    }

    public static class ShouldLogWithPriorityAndThrowable implements AppUnderTest, TraceMarker {
        private static final Logger logger =
                Logger.getLogger(ShouldLogWithPriorityAndThrowable.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.log(Level.TRACE, "abc___", new IllegalStateException("123_"));
            logger.log(Level.DEBUG, "bcd___", new IllegalStateException("234_"));
            logger.log(Level.INFO, "cde___", new IllegalStateException("345_"));
            logger.log(Level.WARN, "def___", new IllegalStateException("456_"));
            logger.log(Level.ERROR, "efg___", new IllegalStateException("567_"));
            logger.log(Level.FATAL, "fgh___", new IllegalStateException("678_"));
        }
    }

    public static class ShouldLogWithPriorityAndNullThrowable implements AppUnderTest, TraceMarker {
        private static final Logger logger =
                Logger.getLogger(ShouldLogWithPriorityAndNullThrowable.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.log(Level.TRACE, "abc___null", null);
            logger.log(Level.DEBUG, "bcd___null", null);
            logger.log(Level.INFO, "cde___null", null);
            logger.log(Level.WARN, "def___null", null);
            logger.log(Level.ERROR, "efg___null", null);
            logger.log(Level.FATAL, "fgh___null", null);
        }
    }

    public static class ShouldLocalizedLog implements AppUnderTest, TraceMarker {
        private static final Logger logger = Logger.getLogger(ShouldLocalizedLog.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.l7dlog(Level.TRACE, "abc____", new IllegalStateException("123__"));
            logger.l7dlog(Level.DEBUG, "bcd____", new IllegalStateException("234__"));
            logger.l7dlog(Level.INFO, "cde____", new IllegalStateException("345__"));
            logger.l7dlog(Level.WARN, "def____", new IllegalStateException("456__"));
            logger.l7dlog(Level.ERROR, "efg____", new IllegalStateException("567__"));
            logger.l7dlog(Level.FATAL, "fgh____", new IllegalStateException("678__"));
        }
    }

    public static class ShouldLocalizedLogWithNullThrowable implements AppUnderTest, TraceMarker {
        private static final Logger logger =
                Logger.getLogger(ShouldLocalizedLogWithNullThrowable.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.l7dlog(Level.TRACE, "abc____null", null);
            logger.l7dlog(Level.DEBUG, "bcd____null", null);
            logger.l7dlog(Level.INFO, "cde____null", null);
            logger.l7dlog(Level.WARN, "def____null", null);
            logger.l7dlog(Level.ERROR, "efg____null", null);
            logger.l7dlog(Level.FATAL, "fgh____null", null);
        }
    }

    public static class ShouldLocalizedLogWithParameters implements AppUnderTest, TraceMarker {
        private static final Logger logger =
                Logger.getLogger(ShouldLocalizedLogWithParameters.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.l7dlog(Level.TRACE, "abc____", new Object[] {"a", "b", "c"},
                    new IllegalStateException("123__"));
            logger.l7dlog(Level.DEBUG, "bcd____", new Object[] {"b", "c", "d"},
                    new IllegalStateException("234__"));
            logger.l7dlog(Level.INFO, "cde____", new Object[] {"c", "d", "e"},
                    new IllegalStateException("345__"));
            logger.l7dlog(Level.WARN, "def____", new Object[] {"d", "e", "f"},
                    new IllegalStateException("456__"));
            logger.l7dlog(Level.ERROR, "efg____", new Object[] {"e", "f", "g"},
                    new IllegalStateException("567__"));
            logger.l7dlog(Level.FATAL, "fgh____", new Object[] {"f", "g", "h"},
                    new IllegalStateException("678__"));
        }
    }

    public static class ShouldLocalizedLogWithParametersAndNullThrowable implements AppUnderTest,
            TraceMarker {
        private static final Logger logger =
                Logger.getLogger(ShouldLocalizedLogWithParametersAndNullThrowable.class);
        @Override
        public void executeApp() {
            traceMarker();
        }
        @Override
        public void traceMarker() {
            logger.l7dlog(Level.TRACE, "abc____null", new Object[] {"a_", "b_", "c_"}, null);
            logger.l7dlog(Level.DEBUG, "bcd____null", new Object[] {"b_", "c_", "d_"}, null);
            logger.l7dlog(Level.INFO, "cde____null", new Object[] {"c_", "d_", "e_"}, null);
            logger.l7dlog(Level.WARN, "def____null", new Object[] {"d_", "e_", "f_"}, null);
            logger.l7dlog(Level.ERROR, "efg____null", new Object[] {"e_", "f_", "g_"}, null);
            logger.l7dlog(Level.FATAL, "fgh____null", new Object[] {"f_", "g_", "h_"}, null);
        }
    }
}
