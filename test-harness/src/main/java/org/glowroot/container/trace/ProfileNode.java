/*
 * Copyright 2013-2014 the original author or authors.
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
package org.glowroot.container.trace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import org.checkerframework.checker.nullness.qual.Nullable;

import static org.glowroot.container.common.ObjectMappers.checkNotNullItemsForProperty;
import static org.glowroot.container.common.ObjectMappers.checkRequiredProperty;
import static org.glowroot.container.common.ObjectMappers.nullToEmpty;

public class ProfileNode {

    @Nullable
    private final String stackTraceElement;
    @Nullable
    private final String leafThreadState;
    private final int sampleCount;
    private final ImmutableList<String> metricNames;
    private final ImmutableList<ProfileNode> childNodes;

    private ProfileNode(@Nullable String stackTraceElement, @Nullable String leafThreadState,
            int sampleCount, List<String> metricNames, List<ProfileNode> childNodes) {
        this.stackTraceElement = stackTraceElement;
        this.leafThreadState = leafThreadState;
        this.sampleCount = sampleCount;
        this.metricNames = ImmutableList.copyOf(metricNames);
        this.childNodes = ImmutableList.copyOf(childNodes);
    }

    // null for synthetic root only
    @Nullable
    public String getStackTraceElement() {
        return stackTraceElement;
    }

    @Nullable
    public String getLeafThreadState() {
        return leafThreadState;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public ImmutableList<String> getMetricNames() {
        return metricNames;
    }

    public ImmutableList<ProfileNode> getChildNodes() {
        return childNodes;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("stackTraceElement", stackTraceElement)
                .add("leafThreadState", leafThreadState)
                .add("sampleCount", sampleCount)
                .add("metricNames", metricNames)
                .add("childNodes", childNodes)
                .toString();
    }

    @JsonCreator
    static ProfileNode readValue(
            @JsonProperty("stackTraceElement") @Nullable String stackTraceElement,
            @JsonProperty("leafThreadState") @Nullable String leafThreadState,
            @JsonProperty("sampleCount") @Nullable Integer sampleCount,
            @JsonProperty("metricNames") @Nullable List</*@Nullable*/String> uncheckedMetricNames,
            @JsonProperty("childNodes") @Nullable List</*@Nullable*/ProfileNode> uncheckedChildNodes)
            throws JsonMappingException {
        List<String> metricNames =
                checkNotNullItemsForProperty(uncheckedMetricNames, "metricNames");
        List<ProfileNode> childNodes =
                checkNotNullItemsForProperty(uncheckedChildNodes, "childNodes");
        checkRequiredProperty(sampleCount, "sampleCount");
        return new ProfileNode(stackTraceElement, leafThreadState,
                sampleCount, nullToEmpty(metricNames), nullToEmpty(childNodes));
    }
}
