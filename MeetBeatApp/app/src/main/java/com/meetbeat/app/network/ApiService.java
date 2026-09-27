package com.meetbeat.app.network;

import com.meetbeat.app.model.AdicionarInteresses;
import com.meetbeat.app.model.AtualizarMostrarCurtidas;
import com.meetbeat.app.model.CurtidaRecebida;
import com.meetbeat.app.model.EsqueciSenhaRequest;
import com.meetbeat.app.model.Genero;
import com.meetbeat.app.model.GoogleLoginRequest;
import com.meetbeat.app.model.LoginRequest;
import com.meetbeat.app.model.LoginResponse;
import com.meetbeat.app.model.MatchConfirmado;
import com.meetbeat.app.model.MatchSuggestion;
import com.meetbeat.app.model.NovoUsuario;
import com.meetbeat.app.model.RedefinirSenhaRequest;
import com.meetbeat.app.model.SwipeResponse;
import com.meetbeat.app.model.UsuarioCompleto;
import com.meetbeat.app.model.UsuarioSimples;
import com.meetbeat.app.model.AtualizarMusicaArtista;
import com.meetbeat.app.model.AtualizarPerfil;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @GET("usuarios/{id}/sugestoes")
    Call<List<MatchSuggestion>> getSugestoes(@Path("id") long id);

    @POST("usuarios/{id}/curtir/{targetId}")
    Call<SwipeResponse> curtir(@Path("id") long id, @Path("targetId") long targetId);

    @POST("usuarios/{id}/rejeitar/{targetId}")
    Call<Void> rejeitar(@Path("id") long id, @Path("targetId") long targetId);

    @GET("usuarios/{id}/matches")
    Call<List<MatchConfirmado>> getMatches(@Path("id") long id);

    @POST("usuarios")
    Call<MatchSuggestion> criarUsuario(@Body NovoUsuario novoUsuario);

    @GET("usuarios/generos")
    Call<List<Genero>> getGeneros();

    @POST("usuarios/{id}/interesses")
    Call<Void> adicionarInteresses(@Path("id") long id, @Body AdicionarInteresses dados);

    @Multipart
    @POST("usuarios/{id}/foto")
    Call<String> uploadFoto(@Path("id") long id, @Part MultipartBody.Part file);

    @GET("usuarios")
    Call<List<UsuarioSimples>> listarUsuarios();

    @POST("usuarios/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("usuarios/login-google")
    Call<LoginResponse> loginGoogle(@Body GoogleLoginRequest googleLoginRequest);

    @POST("usuarios/esqueci-senha")
    Call<Void> esqueciSenha(@Body EsqueciSenhaRequest dados);

    @POST("usuarios/redefinir-senha")
    Call<Void> redefinirSenha(@Body RedefinirSenhaRequest dados);

    @PUT("usuarios/{id}/bio")
    Call<Void> atualizarBio(@Path("id") long id, @Body com.meetbeat.app.model.AtualizarBio bio);

    @POST("usuarios/{id}/finalizar-onboarding")
    Call<Void> finalizarOnboarding(@Path("id") long id);

    @GET("usuarios/{id}")
    Call<UsuarioCompleto> buscarUsuario(@Path("id") long id);

    @GET("usuarios/{id}/posts")
    Call<List<String>> listarPosts(@Path("id") long id);

    @Multipart
    @POST("usuarios/{id}/posts")
    Call<String> uploadPost(@Path("id") long id, @Part MultipartBody.Part file);

    @Multipart
    @POST("usuarios/{id}/foto-perfil")
    Call<String> uploadFotoPerfil(@Path("id") long id, @Part MultipartBody.Part file);

    @PUT("usuarios/{id}/musica-artista")
    Call<Void> atualizarMusicaArtista(@Path("id") long id, @Body AtualizarMusicaArtista dados);

    @PUT("usuarios/{id}")
    Call<Void> atualizarPerfil(@Path("id") long id, @Body AtualizarPerfil dados);

    @GET("usuarios/{id}/curtidas")
    Call<List<CurtidaRecebida>> getCurtidas(@Path("id") long id);

    @GET("usuarios/{id}/curtidas-enviadas")
    Call<List<CurtidaRecebida>> getCurtidasEnviadas(@Path("id") long id);

    @PUT("usuarios/{id}/mostrar-curtidas")
    Call<Void> atualizarMostrarCurtidas(@Path("id") long id, @Body AtualizarMostrarCurtidas dados);
}