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

package com.atlassian.jira.rest.client.internal.jersey;

import com.atlassian.jira.rest.client.ProgressMonitor;
import com.atlassian.jira.rest.client.RestClientException;
import com.atlassian.jira.rest.client.domain.util.ErrorCollection;
import com.atlassian.jira.rest.client.internal.json.JsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.JsonObjectParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import com.atlassian.jira.rest.client.internal.json.gen.JsonGenerator;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Parent class for Jersey-based implementation of REST clients
 *
 * @since v0.1
 */
public abstract class AbstractJerseyRestClient {
	protected final ApacheHttpClient client;
	protected final URI baseUri;

	public AbstractJerseyRestClient(URI baseUri, ApacheHttpClient client) {
		this.baseUri = baseUri;
		this.client = client;
	}

	protected <T> T invoke(Callable<T> callable) throws RestClientException {
		try {
			return callable.call();
		} catch (UniformInterfaceException uniformEx) {
			//todo: try to handle captcha -> read change history
			try {
				final ClientResponse response = uniformEx.getResponse();
				final String body = response.getEntity(String.class);
				final Collection<ErrorCollection> errorMessages = extractErrors(response.getStatus(), body);
				throw new RestClientException(errorMessages, uniformEx);
			} catch (JSONException ignoredEx) {
				//if we can't parse the response, we rethrow original exception
				throw new RestClientException(uniformEx);
			}
		} catch (RestClientException restEx) {
			throw restEx;
		} catch (Exception ex) {
			throw new RestClientException(ex);
		}
	}

	protected <T> T getAndParse(final URI uri, final JsonObjectParser<T> parser, ProgressMonitor progressMonitor) {
		return invoke(new Callable<T>() {
			@Override
			public T call() throws Exception {
				final WebResource webResource = client.resource(uri);
				final JSONObject s = webResource.get(JSONObject.class);
				return parser.parse(s);
			}
		});

	}

	protected <T> T getAndParse(final URI uri, final JsonArrayParser<T> parser, ProgressMonitor progressMonitor) {
		return invoke(new Callable<T>() {
			@Override
			public T call() throws Exception {
				final WebResource webResource = client.resource(uri);
				final JSONArray jsonArray = webResource.get(JSONArray.class);
				return parser.parse(jsonArray);
			}
		});
	}

	protected void delete(final URI uri, ProgressMonitor progressMonitor) {
		invoke(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final WebResource webResource = client.resource(uri);
				webResource.delete();
				return null;
			}
		});
	}

	protected <T> T postAndParse(final URI uri, @Nullable final JSONObject postEntity, final JsonObjectParser<T> parser, ProgressMonitor progressMonitor) {
		return invoke(new Callable<T>() {
			@Override
			public T call() throws Exception {
				final WebResource webResource = client.resource(uri);
				final JSONObject s = postEntity != null ? webResource.post(JSONObject.class, postEntity) : webResource.post(JSONObject.class);
				return parser.parse(s);
			}
		});
	}

	protected void post(final URI uri, @Nullable final JSONObject postEntity, ProgressMonitor progressMonitor) {
		post(uri, new Callable<JSONObject>() {
			@Override
			public JSONObject call() throws Exception {
				return postEntity;

			}
		}, progressMonitor);
	}

	protected void post(final URI uri, final Callable<JSONObject> callable, ProgressMonitor progressMonitor) {
		invoke(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				final WebResource webResource = client.resource(uri);
				final JSONObject postEntity = callable.call();
				if (postEntity != null) {
					webResource.post(postEntity);
				} else {
					webResource.post();
				}
				return null;
			}
		});

	}

	protected <T> T postAndParse(final URI uri, final Callable<JSONObject> callable, final JsonObjectParser<T> parser, ProgressMonitor progressMonitor) {
		return impl(uri, Method.POST, callable, parser);
	}

	protected <T> T putAndParse(final URI uri, final Callable<JSONObject> callable, final JsonObjectParser<T> parser, ProgressMonitor progressMonitor) {
		return impl(uri, Method.PUT, callable, parser);
	}

	enum Method {
		PUT, POST
	}

	private <T> T impl(final URI uri, final Method method, final Callable<JSONObject> callable, final JsonObjectParser<T> parser) {
		return invoke(new Callable<T>() {
			@Override
			public T call() throws Exception {
				final WebResource webResource = client.resource(uri);
				final JSONObject postEntity = callable.call();
				final JSONObject s;
				s = doHttpMethod(webResource, postEntity, method);
				return parser.parse(s);
			}
		});
	}

	private JSONObject doHttpMethod(WebResource webResource, @Nullable JSONObject postEntity, Method method) {
		if (postEntity != null) {
			if (method == Method.POST) {
				return webResource.post(JSONObject.class, postEntity);
			} else {
				return webResource.put(JSONObject.class, postEntity);
			}
		} else {
			if (method == Method.POST) {
				return webResource.post(JSONObject.class);
			} else {
				return webResource.put(JSONObject.class);
			}
		}
	}


	static Collection<ErrorCollection> extractErrors(final int status, final String body) throws JSONException {
		final JSONObject jsonObject = new JSONObject(body);
		final JSONArray issues = jsonObject.optJSONArray("issues");
		final ImmutableList.Builder<ErrorCollection> results  = ImmutableList.builder();
		if (issues != null && issues.length() == 0) {
			final JSONArray errors = jsonObject.optJSONArray("errors");
			for (int i = 0; i < errors.length(); i++ ) {
				final JSONObject currentJsonObject = errors.getJSONObject(i);
				results.add(getErrorsFromJson(currentJsonObject.getInt("status"), currentJsonObject.optJSONObject("elementErrors")));
			}
		} else {
			results.add(getErrorsFromJson(status, jsonObject));
		}
		return results.build();
	}


	protected static class InputGeneratorCallable<T> implements Callable<JSONObject> {

		private final JsonGenerator<T> generator;
		private final T bean;

		public static <T> InputGeneratorCallable<T> create(JsonGenerator<T> generator, T bean) {
			return new InputGeneratorCallable<T>(generator, bean);
		}

		public InputGeneratorCallable(JsonGenerator<T> generator, T bean) {
			this.generator = generator;
			this.bean = bean;
		}

		@Override
		public JSONObject call() throws Exception {
			return generator.generate(bean);
		}
	}

	private static ErrorCollection getErrorsFromJson(final int status, final JSONObject jsonObject) throws JSONException {
		final JSONObject jsonErrors = jsonObject.optJSONObject("errors");
		final JSONArray jsonErrorMessages = jsonObject.optJSONArray("errorMessages");

		final Collection<String> errorMessages;
		if (jsonErrorMessages != null) {
			errorMessages = JsonParseUtil.toStringCollection(jsonErrorMessages);
		} else {
			errorMessages = Collections.emptyList();
		}

		final Map<String,String> errors;
		if (jsonErrors != null && jsonErrors.length() > 0) {
			errors = JsonParseUtil.toStringMap(jsonErrors.names(), jsonErrors);
		}  else {
			errors = Collections.emptyMap();
		}
		return new ErrorCollection(status, errorMessages, errors);
	}

}
