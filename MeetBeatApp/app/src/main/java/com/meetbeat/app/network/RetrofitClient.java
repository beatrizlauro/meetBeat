package com.meetbeat.app.network;

import java.util.concurrent.TimeUnit;

import com.meetbeat.app.BuildConfig;
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

    // Definida por tipo de build em app/build.gradle.kts:
    //  - debug: http://127.0.0.1:8080/ (celular no cabo USB com "adb reverse tcp:8080 tcp:8080")
    //  - nuvem: URL da API publicada (propriedade meetbeat.cloudUrl em gradle.properties)
    // Emulador Android Studio: troque a URL do debug para "http://10.0.2.2:8080/".
    public static final String BASE_URL = BuildConfig.API_URL;

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