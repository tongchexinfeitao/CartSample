package com.ali.cartdemo;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by mumu on 2018/6/14.
 */

public class OkhtttpUtils {
    private static OkhtttpUtils mOkhtttpUtils;
    private OkHttpClient mOkHttpClien;
    private final Handler mHandler;

    private OkhtttpUtils() {

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Map<String, String> map = new HashMap<>();
        map.put("source", "android");

        PublicParamInterceptor publicParamInterceptor = new PublicParamInterceptor(map);

        //创建一个主线程的handler
        mHandler = new Handler(Looper.getMainLooper());
        mOkHttpClien = new OkHttpClient.Builder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .writeTimeout(5000, TimeUnit.MILLISECONDS)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(publicParamInterceptor)
                .build();
    }


    public static OkhtttpUtils getInstance() {
        if (mOkhtttpUtils == null) {
            synchronized (OkhtttpUtils.class) {
                if (mOkhtttpUtils == null) {
                    return mOkhtttpUtils = new OkhtttpUtils();
                }
            }
        }
        return mOkhtttpUtils;
    }

    public void doGet(String url, final OkCallback okCallback) {
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();

        final Call call = mOkHttpClien.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (okCallback != null) {

                    //切换到主线程
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            okCallback.onFailure(e);
                        }
                    });

                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                try {
                    if (response != null && response.isSuccessful()) {
                        final String json = response.body().string();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (okCallback != null) {
                                    okCallback.onResponse(json);
                                    return;
                                }

                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void doPost(String url, Map<String, String> map, final OkCallback okCallback) {

        FormBody.Builder builder = new FormBody.Builder();
        if (map != null) {
            for (String key : map.keySet()) {
                builder.add(key, map.get(key));
            }
        }
        FormBody formBody = builder.build();
        Request request = new Request.Builder()
                .post(formBody)
                .url(url)
                .build();
        final Call call = mOkHttpClien.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (okCallback != null) {

                    //切换到主线程
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            okCallback.onFailure(e);
                        }
                    });

                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                try {
                    if (response != null && response.isSuccessful()) {
                        final String json = response.body().string();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (okCallback != null) {
                                    okCallback.onResponse(json);
                                    return;
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (okCallback != null) {
                    okCallback.onFailure(new Exception("网络异常"));
                }

            }
        });
    }

    public interface OkCallback {
        void onFailure(Exception e);

        void onResponse(String json);
    }


    //自定义一个拦截器，封装公共请求参数
    public class PublicParamInterceptor implements Interceptor {
        Map<String, String> paramMap = new HashMap<>();

        public PublicParamInterceptor(Map<String, String> paramMap) {
            this.paramMap = paramMap;
        }

        @Override

        public Response intercept(Chain chain) throws IOException {
            //拿到原来的request
            Request oldRequest = chain.request();
            //拿到请求的url
            String url = oldRequest.url().toString();
            //判断是GET还是POST请求
            if (oldRequest.method().equalsIgnoreCase("GET")) {
                if (paramMap != null && paramMap.size() > 0) {
                    StringBuilder urlBuilder = new StringBuilder(url);
                    //拼接公共请求参数
                    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                        urlBuilder.append("&" + entry.getKey() + "=" + entry.getValue());
                    }
                    url = urlBuilder.toString();
                    //如果之前的url没有？号，我们需要手动给他添加一个？号
                    if (!url.contains("?")) {
                        url = url.replaceFirst("&", "?");
                    }

                    //依据原来的request构造一个新的request,
                    Request request = oldRequest.newBuilder()
                            .url(url)
                            .build();
                    return chain.proceed(request);
                }
            } else {
                if (paramMap != null && paramMap.size() > 0) {
                    RequestBody body = oldRequest.body();
                    if (body != null && body instanceof FormBody) {
                        FormBody formBody = (FormBody) body;
                        //1.把原来的的body里面的参数添加到新的body中
                        FormBody.Builder builder = new FormBody.Builder();
                        //为了防止重复添加相同的key和value
                        Map<String, String> temMap = new HashMap<>();
                        for (int i = 0; i < formBody.size(); i++) {
                            builder.add(formBody.encodedName(i), formBody.encodedValue(i));
                            temMap.put(formBody.encodedName(i), formBody.encodedValue(i));
                        }
                        //2.把公共请求参数添加到新的body中
                        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                            if(!temMap.containsKey(entry.getKey())){
                                builder.add(entry.getKey(), entry.getValue());
                            }
                        }
                        FormBody newFormBody = builder.build();

                        //依据原来的request构造一个新的request,
                        Request newRequest = oldRequest.newBuilder()
                                .post(newFormBody)
                                .build();
                        return chain.proceed(newRequest);
                    }

                }
            }

            return chain.proceed(oldRequest);
        }
    }
}


