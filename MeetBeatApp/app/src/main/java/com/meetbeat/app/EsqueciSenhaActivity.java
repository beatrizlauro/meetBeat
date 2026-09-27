package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meetbeat.app.model.EsqueciSenhaRequest;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EsqueciSenhaActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText editEmail;
    private TextView btnEnviarCodigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueci_senha);

        apiService = RetrofitClient.getApiService();
        editEmail = findViewById(R.id.editEmailEsqueciSenha);
        btnEnviarCodigo = findViewById(R.id.btnEnviarCodigo);

        findViewById(R.id.btnVoltarEsqueciSenha).setOnClickListener(v -> finish());
        btnEnviarCodigo.setOnClickListener(v -> enviarCodigo());
    }

    private void enviarCodigo() {
        String email = editEmail.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Digite um e-mail válido", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEnviarCodigo.setEnabled(false);

        apiService.esqueciSenha(new EsqueciSenhaRequest(email)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnEnviarCodigo.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(EsqueciSenhaActivity.this,
                            "Se esse e-mail estiver cadastrado, você vai receber um código em instantes",
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(EsqueciSenhaActivity.this, RedefinirSenhaActivity.class);
                    intent.putExtra(RedefinirSenhaActivity.EXTRA_EMAIL, email);
                    startActivity(intent);
                } else {
                    Toast.makeText(EsqueciSenhaActivity.this, "Erro ao enviar o código. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnEnviarCodigo.setEnabled(true);
                Toast.makeText(EsqueciSenhaActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
