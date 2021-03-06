package fr.neraud.padlistener.http.client;

import android.content.Context;
import android.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Map;

import fr.neraud.log.MyLog;
import fr.neraud.padlistener.http.constant.HttpMethod;
import fr.neraud.padlistener.http.exception.HttpCallException;
import fr.neraud.padlistener.http.helper.HttpPatch;
import fr.neraud.padlistener.http.model.MyHttpRequest;
import fr.neraud.padlistener.http.model.MyHttpResponse;
import fr.neraud.padlistener.util.VersionUtil;

/**
 * Abstract HttpClient. Handles the basic calls.
 *
 * @param <R> the response
 * @author Neraud
 */
public abstract class MyHttpClientClient<R extends MyHttpResponse> {

	private final Context context;
	private final String endpointUrl;
	private final HttpClient httpclient = new DefaultHttpClient();

	public MyHttpClientClient(Context context, String endpointUrl) {
		super();
		this.context = context;
		this.endpointUrl = endpointUrl;
	}

	public R call(MyHttpRequest httpRequest) throws HttpCallException {
		MyLog.entry();

		final String fullUrl = createFullUrl(httpRequest);
		final HttpRequestBase httpMethod = createMethod(httpRequest.getMethod(), fullUrl);
		httpMethod.setHeader("user-agent", "PADListener/" + VersionUtil.getVersion(context));

		if (httpRequest.getAuthMode() != null) {
			switch (httpRequest.getAuthMode()) {
				case BASIC:
					MyLog.debug("adding basic auth with user " + httpRequest.getAuthUserName());
					final byte[] authorizationBytes = (httpRequest.getAuthUserName() + ":" + httpRequest.getAuthUserPassword()) .getBytes();
					final String authorizationString = "Basic " + Base64.encodeToString(authorizationBytes, Base64.NO_WRAP);
					httpMethod.setHeader("Authorization", authorizationString);
					break;
				case X_HEADER:
					MyLog.debug("adding x-header auth with user " + httpRequest.getAuthUserName());

					httpMethod.setHeader("X-Username", httpRequest.getAuthUserName());
					httpMethod.setHeader("X-Password", httpRequest.getAuthUserPassword());
					break;
				default:
			}
		}
		if (httpRequest.getHeaderAccept() != null) {
			httpMethod.setHeader("Accept", httpRequest.getHeaderAccept());
		}
		if (httpRequest.getHeaderContentType() != null) {
			httpMethod.setHeader("Content-type", httpRequest.getHeaderContentType());
		}

		try {
			if (httpRequest.getBody() != null) {
				MyLog.debug("setting body : " + httpRequest.getBody());
				final StringEntity entity = new StringEntity(httpRequest.getBody(), "UTF-8");
				switch (httpRequest.getMethod()) {
					case POST:
						((HttpPost) httpMethod).setEntity(entity);
						break;
					case PUT:
						((HttpPut) httpMethod).setEntity(entity);
						break;
					case PATCH:
						((HttpPatch) httpMethod).setEntity(entity);
						break;
					default:
						MyLog.warn("cannot set body for method : " + httpRequest.getMethod());
				}
			}

			MyLog.debug(httpRequest.getMethod() + " " + httpMethod.getURI());
			final HttpResponse httpResponse = httpclient.execute(httpMethod);

			final R result = createResultFromResponse(httpResponse);
			MyLog.exit();
			return result;
		} catch (final ClientProtocolException e) {
			throw new HttpCallException(e);
		} catch (final IOException e) {
			throw new HttpCallException(e);
		}
	}

	protected abstract R createResultFromResponse(final HttpResponse httpResponse) throws HttpCallException;

	private String createFullUrl(MyHttpRequest httpRequest) {
		final StringBuilder urlBuilder = new StringBuilder(endpointUrl);

		final String apiUrl = httpRequest.getUrl();
		if (apiUrl != null) {
			if (endpointUrl.endsWith("/") && apiUrl.startsWith("/")) {
				urlBuilder.deleteCharAt(urlBuilder.length() - 1);
			}
			urlBuilder.append(apiUrl);
		}
		final Map<String, String> urlParams = httpRequest.getUrlParams();
		if (urlParams != null) {
			boolean first = true;
			for (final String paramName : urlParams.keySet()) {
				if (first) {
					first = false;
					urlBuilder.append("?");
				} else {
					urlBuilder.append("&");
				}
				urlBuilder.append(paramName).append("=").append(urlParams.get(paramName));
			}
		}
		return urlBuilder.toString();
	}

	private HttpRequestBase createMethod(HttpMethod method, String fullUrl) {
		switch (method) {
			case GET:
				return new HttpGet(fullUrl);
			case POST:
				return new HttpPost(fullUrl);
			case PUT:
				return new HttpPut(fullUrl);
			case PATCH:
				return new HttpPatch(fullUrl);
			case DELETE:
				return new HttpDelete(fullUrl);
			default:
				throw new IllegalArgumentException(method + " is not an allowed method");
		}
	}
}
