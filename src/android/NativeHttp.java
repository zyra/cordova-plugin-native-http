package ca.zyra.cordova.NativeHttp;

import android.content.Context;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.ParseException;

/**
 * This class echoes a string called from JavaScript.
 */
public class NativeHttp extends CordovaPlugin {

    private AsyncHttpClient client;
    private Context context;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        client = new AsyncHttpClient();
        context = cordova.getActivity().getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("get")) {
            String path = args.getString(0);
            JSONObject params = args.getJSONObject(1);
            JSONObject headers = args.getJSONObject(2);
            this.get(path, params, headers, callbackContext);
            return true;
        }
        return false;
    }

    private void get(final String path, JSONObject params, JSONObject headers, CallbackContext callbackContext) {
        RequestParams _params = new RequestParams();
        List<Header> _headers = new ArrayList<Header>();

        try {
            if (params != null) {
                Iterator<String> paramKeys = params.keys();
                while(paramKeys.hasNext()) {
                    final String key = paramKeys.next();
                    final String value = params.getString(key);
                    _params.add(key, value);
                }
            }

            if (headers != null) {
                Iterator<String> headerKeys = headers.keys();
                while(headerKeys.hasNext()) {
                    final String key = headerKeys.next();
                    final String value = params.getString(key);
                    final Header header = new Header() {
                        @Override
                        public String getName() {
                            return key;
                        }

                        @Override
                        public String getValue() {
                            return value;
                        }

                        @Override
                        public HeaderElement[] getElements() throws ParseException {
                            return new HeaderElement[0];
                        }
                    };

                    _headers.add(header);
                }
            }

            final Header[] __headers = _headers.toArray(new Header[_headers.size()]);

            client.get(this.context, path, __headers, _params, getJSONResponseHandler(callbackContext));

        } catch (Exception e) {
            callbackContext.error(e.getLocalizedMessage());
        }

    }

    private AsyncHttpResponseHandler getRequestHandler(final CallbackContext callbackContext) {
        return new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String bodyAsString = new String(responseBody, "UTF-8");
                    JSONObject response = new JSONObject();
                    response.put("status", statusCode);
                    response.put("headers", headers);
                    response.put("body", bodyAsString);
                    callbackContext.success(response);
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    String bodyAsString = new String(responseBody, "UTF-8");
                    JSONObject response = new JSONObject();
                    response.put("status", statusCode);
                    response.put("headers", headers);
                    response.put("body", bodyAsString);
                    response.put("error", error.getLocalizedMessage());
                    callbackContext.error(response);
                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        };
    }

    private ResponseHandlerInterface getJSONResponseHandler(final CallbackContext callbackContext) {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject responseBody) {
                try {
                    JSONObject response = new JSONObject();
                    response.put("status", statusCode);
                    response.put("headers", headers);
                    response.put("body", responseBody);
                    callbackContext.success(response);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
                try {
                    JSONObject response = new JSONObject();
                    response.put("status", statusCode);
                    response.put("headers", headers);
                    response.put("body", responseBody);
                    response.put("error", error.getLocalizedMessage());
                    callbackContext.error(response);
                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                }
            }
        };
    }

}
