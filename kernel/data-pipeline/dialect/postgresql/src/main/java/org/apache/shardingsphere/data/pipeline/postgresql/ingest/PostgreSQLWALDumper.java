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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.PostgreSQLLogicalReplication;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALEventConverter;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLTimestampUtils;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.TestDecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.CommitTXEvent;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PostgreSQL WAL dumper.
 */
@Slf4j
public final class PostgreSQLWALDumper extends AbstractPipelineLifecycleRunnable implements IncrementalDumper {
    
    private final IncrementalDumperContext dumperContext;
    
    private final AtomicReference<WALPosition> walPosition;
    
    private final PipelineChannel channel;
    
    private final WALEventConverter walEventConverter;
    
    private final PostgreSQLLogicalReplication logicalReplication;
    
    private final boolean decodeWithTX;
    
    private List<AbstractRowEvent> rowEvents = new LinkedList<>();
    
    public PostgreSQLWALDumper(final IncrementalDumperContext dumperContext, final IngestPosition position,
                               final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        ShardingSpherePreconditions.checkState(StandardPipelineDataSourceConfiguration.class.equals(dumperContext.getCommonContext().getDataSourceConfig().getClass()),
                () -> new UnsupportedSQLOperationException("PostgreSQLWALDumper only support PipelineDataSourceConfiguration"));
        this.dumperContext = dumperContext;
        walPosition = new AtomicReference<>((WALPosition) position);
        this.channel = channel;
        walEventConverter = new WALEventConverter(dumperContext, metaDataLoader);
        logicalReplication = new PostgreSQLLogicalReplication();
        this.decodeWithTX = dumperContext.isDecodeWithTX();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Override
    protected void runBlocking() {
        AtomicInteger reconnectTimes = new AtomicInteger();
        while (isRunning()) {
            try {
                dump();
                break;
            } catch (final SQLException ex) {
                int times = reconnectTimes.incrementAndGet();
                log.error("Connect failed, reconnect times={}", times, ex);
                if (isRunning()) {
                    Thread.sleep(5000);
                }
                if (times >= 5) {
                    throw new IngestException(ex);
                }
            }
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    private void dump() throws SQLException {
        // TODO use unified PgConnection
        try (
                Connection connection = logicalReplication.createConnection((StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig());
                PGReplicationStream stream = logicalReplication.createReplicationStream(connection, PostgreSQLIngestPositionManager.getUniqueSlotName(connection, dumperContext.getJobId()),
                        walPosition.get().getLogSequenceNumber())) {
            PostgreSQLTimestampUtils utils = new PostgreSQLTimestampUtils(connection.unwrap(PgConnection.class).getTimestampUtils());
            DecodingPlugin decodingPlugin = new TestDecodingPlugin(utils);
            while (isRunning()) {
                ByteBuffer message = stream.readPending();
                if (null == message) {
                    Thread.sleep(10L);
                    continue;
                }
                AbstractWALEvent event = decodingPlugin.decode(message, new PostgreSQLLogSequenceNumber(stream.getLastReceiveLSN()));
                if (decodeWithTX) {
                    processEventWithTX(event);
                } else {
                    processEventIgnoreTX(event);
                }
                walPosition.set(new WALPosition(event.getLogSequenceNumber()));
            }
        }
    }
    
    private void processEventWithTX(final AbstractWALEvent event) {
        if (event instanceof BeginTXEvent) {
            rowEvents = new ArrayList<>();
            return;
        }
        if (event instanceof AbstractRowEvent) {
            rowEvents.add((AbstractRowEvent) event);
            return;
        }
        if (event instanceof CommitTXEvent) {
            List<Record> records = new LinkedList<>();
            for (AbstractWALEvent each : rowEvents) {
                records.add(walEventConverter.convert(each));
            }
            records.add(walEventConverter.convert(event));
            channel.push(records);
        }
    }
    
    private void processEventIgnoreTX(final AbstractWALEvent event) {
        if (event instanceof BeginTXEvent) {
            return;
        }
        channel.push(Collections.singletonList(walEventConverter.convert(event)));
    }
    
    @Override
    protected void doStop() {
    }
}
