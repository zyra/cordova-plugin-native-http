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
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Url;
import retrofit2.http.QueryMap;

public class NativeHttp extends CordovaPlugin {

    interface Service {
        @GET
        Call<ResponseBody> get(@Url() String path, @QueryMap Map<String, Object> params, @HeaderMap Map<String, Object> headers);

        @POST
        Call<ResponseBody> post(@Url() String path, @Body Map<String, Object> body, @HeaderMap Map<String, Object> headers);
    }

    private Service service;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        service = new Retrofit.Builder()
                .baseUrl("https://zyraapps.com/api/ping/")
                .build()
                .create(Service.class);
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

    private void get(final String path, final JSONObject params, final JSONObject headers, final CallbackContext callbackContext) {
        try {

            final HashMap<String, Object> _params = jsonObjectToHashMap(params);
            final HashMap<String, Object> _headers = jsonObjectToHashMap(headers);

            final Call<ResponseBody> call = service.get(path, _params, _headers);

            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.isSuccessful()) {
                                    final JSONObject res = new JSONObject();
                                    res.put("status", response.code());

                                    res.put("headers", parseHeaders(response.headers()));
                                    res.put("body", response.body().string());
                                    callbackContext.success(res);
                                } else {
                                    final JSONObject res = new JSONObject();
                                    res.put("status", response.code());
                                    res.put("headers", parseHeaders(response.headers()));
                                    res.put("body", response.body().string());
                                    callbackContext.error(res);
                                }
                            } catch (final Exception e) {
                                callbackContext.error(e.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            try {
                                final JSONObject res = new JSONObject();
                                res.put("error", t.getLocalizedMessage());
                                callbackContext.error(res);
                            } catch (final Exception e) {
                                callbackContext.error(e.getLocalizedMessage());
                            }
                        }
                    });
                }
            });

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

    private JSONObject parseHeaders(final Headers headers) {
        final JSONObject obj = new JSONObject();
        for (int i = 0; i < headers.size(); i++) {
            try {
                obj.put(headers.name(i), headers.value(i));
            } catch (final Exception e) {}
        }
        return obj;
    }

}
