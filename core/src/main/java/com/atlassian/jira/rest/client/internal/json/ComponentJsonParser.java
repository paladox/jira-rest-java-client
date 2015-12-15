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

import com.atlassian.jira.rest.client.api.domain.AssigneeType;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicUser;
import com.atlassian.jira.rest.client.api.domain.Component;
import com.atlassian.jira.rest.client.internal.domain.AssigneeTypeConstants;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ComponentJsonParser implements JsonElementParser<Component> {
	@Override
	public Component parse(JsonElement jsonElement) throws JsonParseException {
		final JsonObject json = jsonElement.getAsJsonObject();

		final BasicComponent basicComponent = BasicComponentJsonParser.parseBasicComponent(json);
		final JsonObject leadJson = json.getAsJsonObject("lead");
		final BasicUser lead = leadJson != null ? JsonParseUtil.parseBasicUser(leadJson) : null;
		final String assigneeTypeStr = JsonParseUtil.getOptionalString(json, "assigneeType");
		final Component.AssigneeInfo assigneeInfo;
		if (assigneeTypeStr != null) {
			final AssigneeType assigneeType = parseAssigneeType(assigneeTypeStr);
			final JsonObject assigneeJson = json.getAsJsonObject("assignee");
			final BasicUser assignee = assigneeJson != null ? JsonParseUtil.parseBasicUser(assigneeJson) : null;
			final AssigneeType realAssigneeType = parseAssigneeType(JsonParseUtil.getAsString(json, "realAssigneeType"));
			final JsonObject realAssigneeJson = json.getAsJsonObject("realAssignee");
			final BasicUser realAssignee = realAssigneeJson != null ? JsonParseUtil.parseBasicUser(realAssigneeJson) : null;
			final boolean isAssigneeTypeValid = json.get("isAssigneeTypeValid").getAsBoolean();
			assigneeInfo = new Component.AssigneeInfo(assignee, assigneeType, realAssignee, realAssigneeType, isAssigneeTypeValid);
		} else {
			assigneeInfo = null;
		}

		return new Component(basicComponent.getSelf(), basicComponent.getId(), basicComponent.getName(), basicComponent
				.getDescription(), lead, assigneeInfo);
	}

	AssigneeType parseAssigneeType(String str) throws JsonParseException {
		// JIRA 4.4+ adds full assignee info to component resource
		if (AssigneeTypeConstants.COMPONENT_LEAD.equals(str)) {
			return AssigneeType.COMPONENT_LEAD;
		}
		if (AssigneeTypeConstants.PROJECT_DEFAULT.equals(str)) {
			return AssigneeType.PROJECT_DEFAULT;
		}
		if (AssigneeTypeConstants.PROJECT_LEAD.equals(str)) {
			return AssigneeType.PROJECT_LEAD;
		}
		if (AssigneeTypeConstants.UNASSIGNED.equals(str)) {
			return AssigneeType.UNASSIGNED;
		}
		throw new JsonParseException("Unexpected value of assignee type [" + str + "]");
	}
}