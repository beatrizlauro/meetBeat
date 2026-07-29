package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meetbeat.app.model.LoginRequest;
import com.meetbeat.app.model.LoginResponse;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.UserSession;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editNome, editSenha;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = RetrofitClient.getApiService();

        editNome = findViewById(R.id.editNomeLogin);
        editSenha = findViewById(R.id.editSenhaLogin);

        findViewById(R.id.btnEntrar).setOnClickListener(v -> fazerLogin());

        findViewById(R.id.btnIrParaCadastro).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class)));
    }

    private void fazerLogin() {
        String nome = editNome.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (nome.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha nome e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.login(new LoginRequest(nome, senha)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse resultado = response.body();

                    if (resultado.isSucesso()) {
                        UserSession.salvarUsuarioLogado(LoginActivity.this, resultado.getUsuarioId());
                        Toast.makeText(LoginActivity.this, "Bem-vindo(a), " + resultado.getNome() + "!", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, resultado.getMensagem(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Erro ao fazer login", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}