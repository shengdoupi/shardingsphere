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

package org.apache.shardingsphere.data.pipeline.common.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.context.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.database.core.metadata.database.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Importer configuration.
 */
@RequiredArgsConstructor
@Getter
@ToString(exclude = {"dataSourceConfig", "tableAndSchemaNameMapper"})
public final class ImporterConfiguration {
    
    private final PipelineDataSourceConfiguration dataSourceConfig;
    
    // TODO columnName case-insensitive?
    private final Map<LogicTableName, Set<String>> shardingColumnsMap;
    
    private final TableAndSchemaNameMapper tableAndSchemaNameMapper;
    
    private final int batchSize;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final int retryTimes;
    
    private final int concurrency;
    
    /**
     * Get logic table names.
     *
     * @return logic table names
     */
    public Collection<String> getLogicTableNames() {
        return Collections.unmodifiableList(shardingColumnsMap.keySet().stream().map(LogicTableName::getOriginal).collect(Collectors.toList()));
    }
    
    /**
     * Get sharding columns.
     *
     * @param logicTableName logic table name
     * @return sharding columns
     */
    public Set<String> getShardingColumns(final String logicTableName) {
        return shardingColumnsMap.getOrDefault(new LogicTableName(logicTableName), Collections.emptySet());
    }
    
    /**
     * Find schema name.
     *
     * @param logicTableName logic table name
     * @return schema name
     */
    public Optional<String> findSchemaName(final String logicTableName) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(dataSourceConfig.getDatabaseType()).getDialectDatabaseMetaData();
        return dialectDatabaseMetaData.isSchemaAvailable() ? Optional.of(tableAndSchemaNameMapper.getSchemaName(logicTableName)) : Optional.empty();
    }
}
