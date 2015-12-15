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

package com.atlassian.jira.rest.client.internal.json;

import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.Visibility;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.joda.time.DateTime;

import java.net.URI;

public class WorklogJsonParser implements JsonElementParser<Worklog> {

	@Override
	public Worklog parse(JsonElement jsonElement) throws JsonParseException {
		final JsonObject json = jsonElement.getAsJsonObject();

		final URI self = JsonParseUtil.getSelfUri(json);
		final URI issueUri = JsonParseUtil.parseURI(JsonParseUtil.getAsString(json, "issue"));
		final BasicUser author = JsonParseUtil.parseBasicUser(json.getAsJsonObject("author"));
		final BasicUser updateAuthor = JsonParseUtil.parseBasicUser(json.getAsJsonObject("updateAuthor"));
		// it turns out that somehow it can be sometimes omitted in the resource representation - JRJC-49
		final String comment = JsonParseUtil.getOptionalString(json, "comment");
		final DateTime creationDate = JsonParseUtil.parseDateTime(json, "created");
		final DateTime updateDate = JsonParseUtil.parseDateTime(json, "updated");
		final DateTime startDate = JsonParseUtil.parseDateTime(json, "started");
		final int minutesSpent = JsonParseUtil.getAsInt(json, "minutesSpent");
		final Visibility visibility = new VisibilityJsonParser().parseVisibility(json);
		return new Worklog(self, issueUri, author, updateAuthor, comment, creationDate, updateDate, startDate, minutesSpent, visibility);
	}
}