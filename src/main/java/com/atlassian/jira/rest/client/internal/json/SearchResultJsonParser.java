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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Collection;

public class SearchResultJsonParser implements JsonObjectParser<SearchResult> {
    private final boolean full;

    public SearchResultJsonParser(boolean full) {
        this.full = full;
    }

    @Override
	public SearchResult parse(JSONObject json) throws JSONException {
		final int startAt = json.getInt("startAt");
		final int maxResults = json.getInt("maxResults");
		final int total = json.getInt("total");
        final JSONObject names = JsonParseUtil.getOptionalJsonObject(json, "names");
        final JSONObject schema = JsonParseUtil.getOptionalJsonObject(json, "schema");
		final Collection<? extends BasicIssue> issues = JsonParseUtil.parseJsonArray(
                json.getJSONArray("issues"), full ? new IssueJsonParser(true, names, schema) : new BasicIssueJsonParser());
		return new SearchResult(startAt, maxResults, total, issues);
	}
}
