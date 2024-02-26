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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.match;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.enums.EdgeDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.match.MatchOptionExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.match.ShortestPathOptionExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.match.SimpleMatchOptionExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.match.item.EdgeExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.match.item.NodeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.match.ExpectedMatchOption;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.match.ExpectedShortestPathOptionExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.match.ExpectedSimpleMatchOptionExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.match.item.ExpectedEdgeExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.match.item.ExpectedNode;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Graph match assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GraphMatchAssert {
    
    /**
     * Assert match option segment.
     *
     * @param assertContext assert context
     * @param actual        match option segment
     * @param expected      expected match option segment
     * @throws UnsupportedOperationException When match option class type is not supported.
     */
    public static void assertMatchOption(final SQLCaseAssertContext assertContext, final MatchOptionExpression actual, final ExpectedMatchOption expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual match option segment should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual match option segment should exist."));
            if (actual instanceof ShortestPathOptionExpression) {
                assertShortestPathOptionExpression(assertContext, (ShortestPathOptionExpression) actual, expected.getShortestPathOption());
            } else if (actual instanceof SimpleMatchOptionExpression) {
                assertSimpleMatchOptionExpression(assertContext, (SimpleMatchOptionExpression) actual, expected.getSimpleMatchOption());
            } else {
                throw new UnsupportedOperationException(String.format("Unsupported match option: %s", actual.getClass().getName()));
            }
        }
    }
    
    /**
     * Assert shortest path option expression.
     *
     * @param assertContext assert context
     * @param actual        shortest path option expression
     * @param expected      expected  shortest path option expression
     */
    private static void assertShortestPathOptionExpression(final SQLCaseAssertContext assertContext, final ShortestPathOptionExpression actual, final ExpectedShortestPathOptionExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual shortest path option expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual shortest path option expression should exist."));
            assertNode(assertContext, actual.getBeginNode(), expected.getBeginNode());
            assertNode(assertContext, actual.getEndNode(), expected.getEndNode());
            assertEdgeExpression(assertContext, actual.getEdge(), expected.getEdge());
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
            assertThat(assertContext.getText("Shortest path option expression assertion error."), actual.getText(), is(expected.getText()));
        }
    }
    
    /**
     * Assert simple match option expression.
     *
     * @param assertContext assert context
     * @param actual        simple match option expression
     * @param expected      expected simple match option expression
     */
    private static void assertSimpleMatchOptionExpression(final SQLCaseAssertContext assertContext, final SimpleMatchOptionExpression actual, final ExpectedSimpleMatchOptionExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual simple match option expression should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual simple match option expression should exist."));
            assertNode(assertContext, actual.getBeginNode(), expected.getBeginNode());
            assertNode(assertContext, actual.getEndNode(), expected.getEndNode());
            assertEdgeExpression(assertContext, actual.getEdge(), expected.getEdge());
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
            assertThat(assertContext.getText("Simple match option expression assertion error."), actual.getText(), is(expected.getText()));
        }
    }
    
    /**
     * Assert node segment.
     *
     * @param assertContext assert context
     * @param actual        node segment
     * @param expected      expected node segment
     */
    private static void assertNode(final SQLCaseAssertContext assertContext, final NodeSegment actual, final ExpectedNode expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual node segment should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual node segment should exist."));
            Optional<String> actualTableName = Optional.ofNullable(actual.getNodeName()).map(TableNameSegment::getIdentifier).map(IdentifierValue::getValue);
            assertTrue(actualTableName.isPresent());
            assertThat(actualTableName.get(), is(expected.getTableName()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
    
    /**
     * Assert edge segment.
     *
     * @param assertContext assert context
     * @param actual        edge segment
     * @param expected      expected edge segment
     */
    private static void assertEdgeExpression(final SQLCaseAssertContext assertContext, final EdgeExpression actual, final ExpectedEdgeExpression expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual edge segment should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual edge segment should exist."));
            Optional<String> actualTableName = Optional.ofNullable(actual.getEdgeName()).map(TableNameSegment::getIdentifier).map(IdentifierValue::getValue);
            assertTrue(actualTableName.isPresent());
            assertThat(actualTableName.get(), is(expected.getTableName()));
            Optional<String> direction = Optional.ofNullable(actual.getDirection()).map(EdgeDirection::name);
            assertTrue(direction.isPresent());
            assertThat(direction.get(), is(expected.getEdgeDirection()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
            assertThat(assertContext.getText("Edge expression assertion error."), actual.getText(), is(expected.getText()));
        }
    }
}
