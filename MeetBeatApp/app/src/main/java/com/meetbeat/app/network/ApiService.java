package com.meetbeat.app.network;

import com.meetbeat.app.model.MatchSuggestion;
import com.meetbeat.app.model.SwipeResponse;
import com.meetbeat.app.model.MatchConfirmado;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @GET("usuarios/{id}/sugestoes")
    Call<List<MatchSuggestion>> getSugestoes(@Path("id") long id);

    @POST("usuarios/{id}/curtir/{targetId}")
    Call<SwipeResponse> curtir(@Path("id") long id, @Path("targetId") long targetId);

    @GET("usuarios/{id}/matches")
    Call<List<MatchConfirmado>> getMatches(@Path("id") long id);
}