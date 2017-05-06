package ca.zyra.cordova.NativeHttp;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.*;

import java.util.HashMap;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpHeaders;

/**
 * This class echoes a string called from JavaScript.
 */
public class NativeHttp extends CordovaPlugin {


    private AsyncHttpClient client;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        client = new AsyncHttpClient();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if (action.equals("get")) {
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
      Iterator<String> paramKeys = params.keys();

      try {
        while(paramKeys.hasNext()) {
          String key = paramKeys.next();
          String value = params.getString(key);
          _params.add(key, value);
        }
      } catch (JSONException e) {
        callbackContext.error(e.getLocalizedMessage());
        return;
      }

        client.get(path, new RequestParams(params), getRequestHandler(callbackContext));
    }

    private AsyncHttpResponseHandler getRequestHandler(final CallbackContext callbackContext) {
        return new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
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
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
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

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
