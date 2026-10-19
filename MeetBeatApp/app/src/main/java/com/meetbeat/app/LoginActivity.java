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

public class LoginActivity extends AppCompatActivity {

    private EditText editNome, editSenha;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = RetrofitClient.getApiService();

        editNome = findViewById(R.id.editNomeLogin);
        editSenha = findViewById(R.id.editSenhaLogin);
        MostrarSenha.ligar(editSenha, findViewById(R.id.btnMostrarSenhaLogin));
        ValidacaoCampos.restringir(editNome, ValidacaoCampos.Regra.USERNAME);

        findViewById(R.id.btnEntrar).setOnClickListener(v -> fazerLogin());

        findViewById(R.id.btnIrParaCadastro).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class)));

        findViewById(R.id.btnEsqueciSenha).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, EsqueciSenhaActivity.class)));

        findViewById(R.id.btnLoginGoogle).setOnClickListener(v -> fazerLoginGoogle());
    }

    private void fazerLogin() {
        String username = editNome.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (username.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha usuário e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        View btnEntrar = findViewById(R.id.btnEntrar);
        VisualHelper.definirOcupado(btnEntrar, true);

        apiService.login(new LoginRequest(username, senha)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                VisualHelper.definirOcupado(btnEntrar, false);
                processarRespostaLogin(response);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                VisualHelper.definirOcupado(btnEntrar, false);
                Toast.makeText(LoginActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fazerLoginGoogle() {
        GoogleAuthHelper.signIn(this, new GoogleAuthHelper.OnGoogleIdTokenListener() {
            @Override
            public void onSuccess(String idToken) {
                apiService.loginGoogle(new GoogleLoginRequest(idToken)).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        processarRespostaLogin(response);
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(LoginActivity.this, mensagem, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processarRespostaLogin(Response<LoginResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            LoginResponse resultado = response.body();

            if (resultado.isSucesso()) {
                UserSession.salvarUsuarioLogado(LoginActivity.this, resultado.getUsuarioId(), resultado.getToken());
                Toast.makeText(LoginActivity.this, "Bem-vindo(a), " + resultado.getNome() + "!", Toast.LENGTH_SHORT).show();

                Intent intent;
                if (resultado.isOnboardingCompleto()) {
                    intent = new Intent(LoginActivity.this, MainContainerActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, OnboardingGenerosActivity.class);
                }
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, resultado.getMensagem(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(LoginActivity.this, "Erro ao fazer login", Toast.LENGTH_SHORT).show();
        }
    }
}