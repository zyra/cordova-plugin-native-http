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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

        String path;
        JSONObject paramsOrBody;
        Headers headers;
        Boolean isJSON;

        if (httpMethods.contains(action)) {
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
                    responseObject.put("body", response.body().string());

                    if (response.isSuccessful()) {
                        callbackContext.success(responseObject);
                    } else {
                        callbackContext.error(responseObject);
                    }
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void request(final String method, final String path, final JSONObject params, final Headers headers, final CallbackContext callbackContext) {
        try {

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

            makeRequest(request, callbackContext);

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

    }

    private void upload(final String remotePath, final String localPath, final JSONObject params, final Headers headers, final CallbackContext callbackContext) {

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
