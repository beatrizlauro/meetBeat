package com.meetbeat.app.network;

import java.util.concurrent.TimeUnit;

import com.meetbeat.app.MeetBeatApplication;
import com.meetbeat.app.util.UserSession;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    // Celular físico via cabo USB: rode "adb reverse tcp:8080 tcp:8080" e use 127.0.0.1.
    // Emulador Android Studio: troque para "http://10.0.2.2:8080/" (não funciona em device físico).
    public static final String BASE_URL = "http://127.0.0.1:8080/";

    private static Retrofit retrofit;

    public static ApiService getApiService() {
        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                String token = UserSession.getToken(MeetBeatApplication.getAppContext());

                if (token == null || token.isEmpty()) {
                    return chain.proceed(original);
                }

                Request comToken = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
                return chain.proceed(comToken);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}