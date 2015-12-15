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

import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.net.URI;

public class IssuelinksTypeJsonParserV5 implements JsonElementParser<IssuelinksType> {
	@Override
	public IssuelinksType parse(JsonElement jsonElement) throws JsonParseException {
		final JsonObject json = jsonElement.getAsJsonObject();

		final URI selfUri = JsonParseUtil.getSelfUri(json);
		final String id = JsonParseUtil.getAsString(json, "id");
		final String name = JsonParseUtil.getAsString(json, "name");
		final String inward = JsonParseUtil.getAsString(json, "inward");
		final String outward = JsonParseUtil.getAsString(json, "outward");

		return new IssuelinksType(selfUri, id, name, inward, outward);
	}
}
