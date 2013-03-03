/**
 * Copyright 2011-2013 the original author or authors.
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
package io.informant.local.store;

import io.informant.config.ConfigModule;
import io.informant.util.OnlyUsedByTests;
import io.informant.util.ThreadSafe;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import checkers.igj.quals.ReadOnly;

/**
 * @author Trask Stalnaker
 * @since 0.5
 */
@ThreadSafe
public class DataSourceModule {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceModule.class);

    private final DataSource dataSource;

    public DataSourceModule(ConfigModule configModule, @ReadOnly Map<String, String> properties)
            throws Exception {
        File dataDir = configModule.getDataDir();
        // mem db is only used for testing (by informant-testkit)
        String h2MemDb = properties.get("internal.h2.memdb");
        if (Boolean.parseBoolean(h2MemDb)) {
            dataSource = new DataSource();
        } else {
            dataSource = new DataSource(new File(dataDir, "informant.h2.db"));
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @OnlyUsedByTests
    public void close() {
        logger.debug("close()");
        try {
            dataSource.close();
        } catch (SQLException e) {
            // warning only since it occurs during shutdown anyways
            logger.warn(e.getMessage(), e);
        }
    }
}
