package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meetbeat.app.model.GoogleLoginRequest;
import com.meetbeat.app.model.LoginRequest;
import com.meetbeat.app.model.LoginResponse;
import com.meetbeat.app.model.NovoUsuario;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.GoogleAuthHelper;
import com.meetbeat.app.util.UserSession;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText editNome, editUsername, editSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        apiService = RetrofitClient.getApiService();

        editNome = findViewById(R.id.editNome);
        editUsername = findViewById(R.id.editUsername);
        editSenha = findViewById(R.id.editSenhaCadastro);

        findViewById(R.id.btnVoltarCadastro).setOnClickListener(v -> finish());
        findViewById(R.id.btnSalvarCadastro).setOnClickListener(v -> salvarCadastro());
        findViewById(R.id.btnCadastroGoogle).setOnClickListener(v -> cadastrarComGoogle());
    }

    private void cadastrarComGoogle() {
        GoogleAuthHelper.signIn(this, new GoogleAuthHelper.OnGoogleIdTokenListener() {
            @Override
            public void onSuccess(String idToken) {
                apiService.loginGoogle(new GoogleLoginRequest(idToken)).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSucesso()) {
                            LoginResponse resultado = response.body();
                            UserSession.salvarUsuarioLogado(CadastroActivity.this, resultado.getUsuarioId(), resultado.getToken());

                            Intent intent = resultado.isOnboardingCompleto()
                                    ? new Intent(CadastroActivity.this, MainContainerActivity.class)
                                    : new Intent(CadastroActivity.this, OnboardingGenerosActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CadastroActivity.this, "Erro ao continuar com o Google", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(CadastroActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(CadastroActivity.this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void salvarCadastro() {
        String nome = editNome.getText().toString().trim();
        String username = editUsername.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (nome.isEmpty()) {
            Toast.makeText(this, "Digite um nome", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Digite um nome de usuário", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.isEmpty() || senha.length() < 4) {
            Toast.makeText(this, "A senha deve ter pelo menos 4 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        // bio vazia por enquanto — será preenchida no onboarding
        NovoUsuario novoUsuario = new NovoUsuario(nome, username, "", senha);

        apiService.criarUsuario(novoUsuario).enqueue(new Callback<com.meetbeat.app.model.MatchSuggestion>() {
            @Override
            public void onResponse(Call<com.meetbeat.app.model.MatchSuggestion> call, Response<com.meetbeat.app.model.MatchSuggestion> response) {
                if (response.isSuccessful()) {
                    fazerLoginAutomatico(username, senha);
                } else if (response.code() == 409) {
                    Toast.makeText(CadastroActivity.this, "Esse nome de usuário já está em uso", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CadastroActivity.this, "Erro ao criar usuário", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.meetbeat.app.model.MatchSuggestion> call, Throwable t) {
                Toast.makeText(CadastroActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fazerLoginAutomatico(String username, String senha) {
        apiService.login(new LoginRequest(username, senha)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSucesso()) {
                    LoginResponse resultado = response.body();
                    UserSession.salvarUsuarioLogado(CadastroActivity.this, resultado.getUsuarioId(), resultado.getToken());

                    // Usuário recém-criado: onboardingCompleto sempre será false aqui
                    Intent intent = new Intent(CadastroActivity.this, OnboardingGenerosActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CadastroActivity.this, "Erro ao entrar automaticamente. Tente fazer login.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(CadastroActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}