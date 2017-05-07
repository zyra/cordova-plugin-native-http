package ca.zyra.cordova.NativeHttp;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Url;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;



/**
 * This class echoes a string called from JavaScript.
 */
public class NativeHttp extends CordovaPlugin {

    interface Service {
        @GET
        Call<ResponseBody> get(@Url() String path, @QueryMap Map<String, Object> params, @HeaderMap Map<String, Object> headers);
        @POST
        Call<ResponseBody> post(@Url() String path, @Body Map<String, Object> body, @HeaderMap Map<String, Object> headers);
    }

    private Context context;
    private Retrofit client;
    private Service service;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = cordova.getActivity().getApplicationContext();
        client = new Retrofit.Builder()
                .baseUrl("https://zyraapps.com/api/ping/")
//                .addConverterFactory(
//                        GsonConverterFactory
//                                .create(
//                                        new GsonBuilder()
//                                                .setLenient()
//                                                .create()
//                                )
//                )
                .build();
        service = client.create(Service.class);
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



    private void get(final String path, JSONObject params, JSONObject headers, final CallbackContext callbackContext) {
        try {

            HashMap<String, Object> _params = jsonObjectToHashMap(params);
            HashMap<String, Object> _headers = jsonObjectToHashMap(headers);

            Call<ResponseBody> call = service.get(path, _params, _headers);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject res = new JSONObject();
                            res.put("status", response.code());

                            res.put("headers", parseHeaders(response.headers()));
                            res.put("body", response.body().string());
                            callbackContext.success(res);
                        } else {
                            JSONObject res = new JSONObject();
                            res.put("status", response.code());
                            res.put("headers", parseHeaders(response.headers()));
                            res.put("body", response.body().string());
                            callbackContext.error(res);
                        }
                    } catch (Exception e) {
                        callbackContext.error(e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    try {
                        JSONObject res = new JSONObject();
                        res.put("error", t.getLocalizedMessage());
                        callbackContext.error(res);
                    } catch (Exception e) {
                        callbackContext.error(e.getLocalizedMessage());
                    }
                }
            });

        } catch (Exception e) {
            callbackContext.error(e.getLocalizedMessage());
        }
    }

    private HashMap<String, Object> jsonObjectToHashMap(JSONObject obj) {
        if (obj != null && obj.length() > 0) {
            Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
            return new Gson().fromJson(obj.toString(), type);
        } else {
            return new HashMap<String, Object>();
        }
    }

    private Object parseResponse(String res) {
        char firstChar = res.charAt(0);

        try {
            if ("{".equals(firstChar)) {
                return new JSONObject(res);
            } else if ("[".equals(firstChar)) {
                return new JSONArray(res);
            }
        } catch (Exception e) { }

        return res;

    }

    private JSONObject parseHeaders(Headers headers) {
        JSONObject obj = new JSONObject();
        for (int i = 0; i < headers.size(); i++) {
            try {
                obj.put(headers.name(i), headers.value(i));
            } catch (Exception e) {}
        }
        return obj;
    }

//    private AsyncHttpResponseHandler getRequestHandler(final CallbackContext callbackContext) {
//        return new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                try {
//                    String bodyAsString = new String(responseBody, "UTF-8");
//                    JSONObject response = new JSONObject();
//                    response.put("status", statusCode);
//                    response.put("headers", headersToJson(headers));
//                    response.put("body", bodyAsString);
//                    callbackContext.success(response);
//                } catch (Exception e) {
//                    callbackContext.error(e.getMessage());
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                try {
//                    String bodyAsString = new String(responseBody, "UTF-8");
//                    JSONObject response = new JSONObject();
//                    response.put("status", statusCode);
//                    response.put("headers", headersToJson(headers));
//                    response.put("body", bodyAsString);
//                    response.put("error", error.getLocalizedMessage());
//                    callbackContext.error(response);
//                } catch (Exception e) {
//                    callbackContext.error(e.getMessage());
//                }
//            }
//        };
//    }
//
//
//    private JSONObject headersToJson(Header[] headers) {
//
//        JSONObject _headers = new JSONObject();
//
//        try {
//            if (headers != null) {
//                for (Header header:
//                     headers) {
//                    _headers.put(header.getName(), header.getValue());
//                }
//            }
//        } catch (Exception e) {
//            Log.d("NativeHttp", e.getLocalizedMessage());
//        }
//
//        return _headers;
//
//    }

}
