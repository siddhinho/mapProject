package br.com.roadmaps.mapapplication;

import android.content.Context;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

/**
 * Created by sidd on 19/05/18.
 */

public class Network {
    public static final String URL_APPLICATION = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";



    private Context context;
//    PersistentCookieStore myCookie;


    public Network(Context context) {
        this.context = context;
//        myCookie = new PersistentCookieStore(context);
    }


    public void checkAddress(double lat, double lng, final HttpCallback cb) {
        final String url = URL_APPLICATION + lat +","+lng+ "&sensor=false&language=pt-BR";
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(30, TimeUnit.SECONDS);
        client.setWriteTimeout(30, TimeUnit.SECONDS);
//        client.setCookieHandler(new CookieManager(myCookie, CookiePolicy.ACCEPT_ALL));

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        final Call call = client.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                if (!call.isCanceled()) {
                    cb.onFailure(null, e);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String value = response.body().string();
                        JSONArray jsonArray = new JSONArray(value);
                        cb.onSuccess(jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    final String value = response.body().string();
                    cb.onFailure(value, null);
                }
            }
        });
    }

    public interface HttpCallback {
        void onSuccess (final JSONArray responseArray );
        void onSuccess(final String response);
        void onFailure(final String response, final Throwable throwable);
    }
}