/*
 * Copyright (C) 2010 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.restjavaclient.domain;

import com.google.common.base.Objects;

import java.util.Collection;

/**
 * TODO: Document this class / interface here
 *
 * @since v0.1
 */
public class Watchers extends BasicWatchers {
    private final Collection<User> watchers;

    public Watchers(BasicWatchers basicWatchers, Collection<User> watchers) {
        super(basicWatchers.getSelf(), basicWatchers.isWatching(), basicWatchers.getNumWatchers());
        this.watchers = watchers;
    }

    public Iterable<User> getWatchers() {
        return watchers;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(super.toString()).
                add("watchers", watchers).
                toString();
    }


}
