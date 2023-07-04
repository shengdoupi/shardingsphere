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

package org.apache.shardingsphere.sharding.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.sharding.event.strategy.table.AlterDefaultTableShardingStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.table.DropDefaultTableShardingStrategyEvent;

/**
 * Default table sharding strategy subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class DefaultTableShardingStrategySubscriber implements RuleChangedSubscriber<AlterDefaultTableShardingStrategyEvent, DropDefaultTableShardingStrategyEvent> {
    
    private DefaultTableShardingStrategySubscribeEngine engine;
    
    @Override
    public void setContextManager(final ContextManager contextManager) {
        engine = new DefaultTableShardingStrategySubscribeEngine(contextManager);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final AlterDefaultTableShardingStrategyEvent event) {
        engine.renew(event);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final DropDefaultTableShardingStrategyEvent event) {
        engine.renew(event);
    }
}
