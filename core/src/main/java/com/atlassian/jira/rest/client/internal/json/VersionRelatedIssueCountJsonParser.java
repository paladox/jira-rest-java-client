/*
 * Copyright (C) 2011 Atlassian
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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.VersionRelatedIssuesCount;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.net.URI;

public class VersionRelatedIssueCountJsonParser implements JsonElementParser<VersionRelatedIssuesCount> {
	@Override
	public VersionRelatedIssuesCount parse(JsonElement jsonElement) throws JsonParseException {
		final JsonObject json = jsonElement.getAsJsonObject();

		final URI selfUri = JsonParseUtil.getSelfUri(json);
		final int issuesFixedCount = JsonParseUtil.getAsInt(json, "issuesFixedCount");
		final int issuesAffectedCount = JsonParseUtil.getAsInt(json, "issuesAffectedCount");
		return new VersionRelatedIssuesCount(selfUri, issuesFixedCount, issuesAffectedCount);
	}
}