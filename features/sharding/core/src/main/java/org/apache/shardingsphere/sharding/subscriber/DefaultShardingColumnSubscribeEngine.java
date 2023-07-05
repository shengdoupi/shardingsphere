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

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.mode.subsciber.RuleItemChangedSubscribeEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.AlterDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.event.strategy.shardingcolumn.DropDefaultShardingColumnEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;

/**
 * Default sharding column subscribe engine.
 */
public final class DefaultShardingColumnSubscribeEngine extends RuleItemChangedSubscribeEngine<ShardingRuleConfiguration, String> {
    
    @Override
    protected String swapRuleItemConfigurationFromEvent(final AlterRuleItemEvent event, final String yamlContent) {
        return yamlContent;
    }
    
    @Override
    protected ShardingRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(ShardingRule.class).map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElseGet(ShardingRuleConfiguration::new);
    }
    
    @Override
    protected void changeRuleItemConfiguration(final AlterRuleItemEvent event, final ShardingRuleConfiguration currentRuleConfig, final String toBeChangedItemConfig) {
        currentRuleConfig.setDefaultShardingColumn(toBeChangedItemConfig);
    }
    
    @Override
    protected void dropRuleItemConfiguration(final DropRuleItemEvent event, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.setDefaultShardingColumn(null);
    }
    
    @Override
    public String getType() {
        return AlterDefaultShardingColumnEvent.class.getName();
    }
    
    @Override
    public Collection<String> getTypeAliases() {
        return Collections.singleton(DropDefaultShardingColumnEvent.class.getName());
    }
}