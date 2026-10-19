package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.meetbeat.app.util.MensagensErro;
import com.meetbeat.app.util.MostrarSenha;
import com.meetbeat.app.util.UserSession;
import com.meetbeat.app.util.ValidacaoCampos;
import com.meetbeat.app.util.VisualHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText editNome, editUsername, editEmail, editSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        apiService = RetrofitClient.getApiService();

        editNome = findViewById(R.id.editNome);
        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmailCadastro);
        editSenha = findViewById(R.id.editSenhaCadastro);
        MostrarSenha.ligar(editSenha, findViewById(R.id.btnMostrarSenhaCadastro));

        ValidacaoCampos.restringir(editNome, ValidacaoCampos.Regra.NOME);
        ValidacaoCampos.restringir(editUsername, ValidacaoCampos.Regra.USERNAME);
        ValidacaoCampos.restringir(editEmail, ValidacaoCampos.Regra.SEM_ESPACOS);
        ValidacaoCampos.validarEmailAoSair(editEmail);

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
                        Toast.makeText(CadastroActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
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
        String nome = ValidacaoCampos.normalizarEspacos(editNome.getText().toString());
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        String senha = editSenha.getText().toString().trim();

        // A senha mínima agora é 6, igual à exigida pela API (antes o app aceitava 4 e a API recusava)
        if (ValidacaoCampos.mostrarErro(editNome, ValidacaoCampos.erroNome(nome))
                || ValidacaoCampos.mostrarErro(editEmail, ValidacaoCampos.erroEmail(email, true))
                || ValidacaoCampos.mostrarErro(editUsername, ValidacaoCampos.erroUsername(username))
                || ValidacaoCampos.mostrarErro(editSenha, ValidacaoCampos.erroSenha(senha))) {
            return;
        }

        // bio vazia por enquanto — será preenchida no onboarding
        NovoUsuario novoUsuario = new NovoUsuario(nome, username, email, "", senha);

        View btnSalvar = findViewById(R.id.btnSalvarCadastro);
        VisualHelper.definirOcupado(btnSalvar, true);

        apiService.criarUsuario(novoUsuario).enqueue(new Callback<com.meetbeat.app.model.MatchSuggestion>() {
            @Override
            public void onResponse(Call<com.meetbeat.app.model.MatchSuggestion> call, Response<com.meetbeat.app.model.MatchSuggestion> response) {
                if (response.isSuccessful()) {
                    fazerLoginAutomatico(username, senha);
                    return;
                }
                VisualHelper.definirOcupado(btnSalvar, false);
                if (response.code() == 409) {
                    Toast.makeText(CadastroActivity.this, "Esse nome de usuário ou e-mail já está em uso", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CadastroActivity.this, "Não foi possível criar a conta. Confira os dados e tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.meetbeat.app.model.MatchSuggestion> call, Throwable t) {
                VisualHelper.definirOcupado(btnSalvar, false);
                Toast.makeText(CadastroActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
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
                // A conta já foi criada; só o login automático falhou, então leva para a tela de login
                Toast.makeText(CadastroActivity.this, "Conta criada! Entre com seu usuário e senha.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}