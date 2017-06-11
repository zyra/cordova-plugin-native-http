package ca.zyra.cordova.NativeHttp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class NativeHttp extends CordovaPlugin {

    private OkHttpClient client;
    private final List<String> httpMethods = Arrays.asList("get", "post", "head", "put", "delete", "patch", "put");

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        client = new OkHttpClient();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (httpMethods.contains(action)) {
            String path;
            JSONObject paramsOrBody;
            Headers headers;
            Boolean isJSON;

            path = args.getString(0);
            paramsOrBody = args.getJSONObject(1);
            headers = jsonToHeaders(args.getJSONObject(2));
            if (action.equals("post") || action.equals("put") || action.equals("patch")) {
                isJSON = args.getBoolean(3);
                this.postRequest(action, path, paramsOrBody, headers, isJSON, callbackContext);
            } else {
                this.request(action, path, paramsOrBody, headers, callbackContext);
            }
            return true;
        } else if (action.equals("download") || action.equals("upload")) {

            String remotePath = args.getString(0);
            String localPath = args.getString(1);
            Headers headers;
            JSONObject params;

            if (action.equals("download")) {
                headers = jsonToHeaders(args.getJSONObject(3));
                params = args.getJSONObject(2);
                download(remotePath, localPath, params, headers, callbackContext);
            } else {
                JSONObject options = args.getJSONObject(2);
                upload(remotePath, localPath, options, callbackContext);
            }

            return true;
        }

        return false;
    }

    private void makeRequest(final Request request, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                JSONObject responseObject = new JSONObject();
                try {
                    Response response = client.newCall(request).execute();
                    responseObject.put("headers", headersToJson(response.headers()));
                    responseObject.put("status", response.code());

                    if (response.isSuccessful()) {
                        responseObject.put("body", response.body().string());
                        callbackContext.success(responseObject);
                    } else {
                        responseObject.put("error", response.body().string());
                        callbackContext.error(responseObject);
                    }
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private Request createRequest(final String method, final String path, final JSONObject params, final Headers headers) throws JSONException {
        final Request.Builder requestBuilder = new Request.Builder();
        final HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(path).newBuilder();

        final Iterator<String> paramKeys = params.keys();

        while (paramKeys.hasNext()) {
            String key = paramKeys.next();
            httpUrlBuilder.addQueryParameter(key, params.getString(key));
        }

        final HttpUrl httpUrl = httpUrlBuilder.build();

        requestBuilder.method(method, null);
        requestBuilder.url(httpUrl);
        requestBuilder.headers(headers);

        final Request request = requestBuilder.build();
        return request;
    }

    private void request(final String method, final String path, final JSONObject params, final Headers headers, final CallbackContext callbackContext) {
        try {
            makeRequest(createRequest(method, path, params, headers), callbackContext);
        } catch (final Exception e) {
            callbackContext.error(e.getLocalizedMessage());
        }
    }

    private void postRequest(final String method, final String path, final JSONObject body, final Headers headers, final Boolean isJson, final CallbackContext callbackContext) {
        try {
            final Request.Builder requestBuilder = new Request.Builder();

            final RequestBody requestBody;

            if (isJson) {
                requestBody = RequestBody.create(MediaType.parse("application/json"), body.toString());
            } else {
                final FormBody.Builder formBodyBuilder = new FormBody.Builder();
                Iterator<String> bodyKeys = body.keys();
                while(bodyKeys.hasNext()) {
                    String key = bodyKeys.next();
                    formBodyBuilder.add(key, body.getString(key));
                }
                requestBody = formBodyBuilder.build();
            }


            requestBuilder.method(method, requestBody);
            requestBuilder.url(path);
            requestBuilder.headers(headers);

            final Request request = requestBuilder.build();

            makeRequest(request, callbackContext);


        } catch (final Exception e) {
            callbackContext.error(e.getLocalizedMessage());
        }
    }

    private void download(final String remotePath, final String localPath, final JSONObject params, final Headers headers, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Request request = createRequest("get", remotePath, params, headers);

                    Response response = client.newCall(request).execute();
                    JSONObject responseObject = new JSONObject();
                    responseObject.put("headers", headersToJson(response.headers()));
                    responseObject.put("status", response.code());

                    if (response.isSuccessful()) {
                        File file = new File(localPath);
                        BufferedSink bufferedSink = Okio.buffer(Okio.sink(file));
                        bufferedSink.writeAll(response.body().source());
                        bufferedSink.close();
                        callbackContext.success(responseObject);
                    } else {
                        responseObject.put("error", response.body().string());
                        callbackContext.error(responseObject);
                    }

                } catch (final Exception e) {
                    callbackContext.error(e.getLocalizedMessage());
                }
            }
        });
    }

    private void upload(final String remotePath, final String localPath, final JSONObject options, final CallbackContext callbackContext) {
        try {
            Headers headers = jsonToHeaders(options.getJSONObject("headers"));
            JSONObject params = options.getJSONObject("params");

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(options.getString("fileKey"), options.getString("fileName"), RequestBody.create(MediaType.parse(options.getString("mimeType")), new File(localPath)))
                    .build();

            final HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(remotePath).newBuilder();

            final Iterator<String> paramKeys = params.keys();

            while (paramKeys.hasNext()) {
                String key = paramKeys.next();
                httpUrlBuilder.addQueryParameter(key, params.getString(key));
            }

            final HttpUrl httpUrl = httpUrlBuilder.build();

            Request request = new Request.Builder()
                    .method(options.getString("httpMethod"), requestBody)
                    .url(httpUrl)
                    .headers(headers)
                    .build();

            makeRequest(request, callbackContext);

        } catch (final Exception e) {
            callbackContext.error(e.getLocalizedMessage());
        }
    }

    private HashMap<String, Object> jsonObjectToHashMap(final JSONObject obj) {
        if (obj != null && obj.length() > 0) {
            final Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
            return new Gson().fromJson(obj.toString(), type);
        } else {
            return new HashMap<String, Object>();
        }
    }

    private Headers jsonToHeaders(final JSONObject jsonObject) {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> headersMap = new Gson().fromJson(jsonObject.toString(), type);
        return Headers.of(headersMap);
    }

    private JSONObject headersToJson(final Headers headers) {
        final JSONObject obj = new JSONObject();
        for (int i = 0; i < headers.size(); i++) {
            try {
                obj.put(headers.name(i), headers.value(i));
            } catch (final Exception e) {}
        }
        return obj;
    }

}
