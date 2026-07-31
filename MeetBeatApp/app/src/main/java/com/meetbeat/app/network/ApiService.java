package com.meetbeat.app.network;

import com.meetbeat.app.model.MatchSuggestion;
import com.meetbeat.app.model.SwipeResponse;
import com.meetbeat.app.model.MatchConfirmado;
import com.meetbeat.app.model.Genero;
import com.meetbeat.app.model.NovoUsuario;
import com.meetbeat.app.model.UsuarioSimples;
import com.meetbeat.app.model.LoginRequest;
import com.meetbeat.app.model.LoginResponse;
import com.meetbeat.app.model.AtualizarBio;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.PUT;

import okhttp3.MultipartBody;

public interface ApiService {

    @GET("usuarios/{id}/sugestoes")
    Call<List<MatchSuggestion>> getSugestoes(@Path("id") long id);

    @POST("usuarios/{id}/curtir/{targetId}")
    Call<SwipeResponse> curtir(@Path("id") long id, @Path("targetId") long targetId);

    @GET("usuarios/{id}/matches")
    Call<List<MatchConfirmado>> getMatches(@Path("id") long id);

    @POST("usuarios")
    Call<MatchSuggestion> criarUsuario(@Body NovoUsuario novoUsuario);

    @GET("usuarios/generos")
    Call<List<Genero>> getGeneros();

    @POST("usuarios/{id}/interesses/{generoId}")
    Call<Void> adicionarInteresse(@Path("id") long id, @Path("generoId") long generoId);

    @Multipart
    @POST("usuarios/{id}/foto")
    Call<String> uploadFoto(@Path("id") long id, @Part MultipartBody.Part file);

    @GET("usuarios")
    Call<List<UsuarioSimples>> listarUsuarios();

    @POST("usuarios/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @PUT("usuarios/{id}/bio")
    Call<Void> atualizarBio(@Path("id") long id, @Body AtualizarBio bio);

    @POST("usuarios/{id}/finalizar-onboarding")
    Call<Void> finalizarOnboarding(@Path("id") long id);
}