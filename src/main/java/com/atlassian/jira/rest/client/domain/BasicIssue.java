/*
 * Copyright (C) 2011 Atlassian
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

package com.atlassian.jira.rest.client.domain;

import com.atlassian.jira.rest.client.AddressableEntity;
import com.atlassian.jira.rest.client.IdentifiableEntity;
import com.google.common.base.Objects;

import javax.annotation.Nullable;
import java.net.URI;

/**
 * Very basic (key and link only) representation of a JIRA issue.
 *
 * @since v0.2
 */
public class BasicIssue implements AddressableEntity, IdentifiableEntity<Long> {
	private final URI self;

	private final String key;
    @Nullable
    private final Long id;

	public BasicIssue(URI self, String key, @Nullable Long id) {
		this.self = self;
		this.key = key;
        this.id = id;
	}

    @Override
    public Long getId() {
        return id;
    }

    /**
	 * @return URI of this issue
	 */
	@Override
	public URI getSelf() {
		return self;
	}

	/**
	 * @return issue key
	 */
	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).
				add("self", self).
				add("key", key).
				toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicIssue) {
			BasicIssue that = (BasicIssue) obj;
			return Objects.equal(this.self, that.self)
					&& Objects.equal(this.key, that.key);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(self, key);
	}

}
