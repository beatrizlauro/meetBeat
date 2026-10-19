package com.meetbeat.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.meetbeat.app.model.EsqueciSenhaRequest;
import com.meetbeat.app.model.RedefinirSenhaRequest;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.MensagensErro;
import com.meetbeat.app.util.MostrarSenha;
import com.meetbeat.app.util.ValidacaoCampos;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RedefinirSenhaActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private ApiService apiService;
    private String email;
    private EditText editCodigo, editNovaSenha, editConfirmarSenha;
    private TextView btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        apiService = RetrofitClient.getApiService();
        email = getIntent().getStringExtra(EXTRA_EMAIL);

        TextView textEmail = findViewById(R.id.textEmailRedefinirSenha);
        textEmail.setText("Enviamos um código para " + email);

        editCodigo = findViewById(R.id.editCodigoRedefinirSenha);
        editNovaSenha = findViewById(R.id.editNovaSenhaRedefinir);
        editConfirmarSenha = findViewById(R.id.editConfirmarSenhaRedefinir);
        MostrarSenha.ligar(editNovaSenha, findViewById(R.id.btnMostrarNovaSenhaRedefinir));
        MostrarSenha.ligar(editConfirmarSenha, findViewById(R.id.btnMostrarConfirmarSenhaRedefinir));
        btnConfirmar = findViewById(R.id.btnConfirmarRedefinicao);

        findViewById(R.id.btnVoltarRedefinirSenha).setOnClickListener(v -> finish());
        findViewById(R.id.btnReenviarCodigo).setOnClickListener(v -> reenviarCodigo());
        btnConfirmar.setOnClickListener(v -> confirmarRedefinicao());
    }

    private void reenviarCodigo() {
        apiService.esqueciSenha(new EsqueciSenhaRequest(email)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(RedefinirSenhaActivity.this, "Código reenviado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RedefinirSenhaActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmarRedefinicao() {
        String codigo = editCodigo.getText().toString().trim();
        String novaSenha = editNovaSenha.getText().toString().trim();
        String confirmarSenha = editConfirmarSenha.getText().toString().trim();

        if (codigo.length() != 6) {
            Toast.makeText(this, "Digite o código de 6 dígitos enviado por e-mail", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ValidacaoCampos.mostrarErro(editNovaSenha, ValidacaoCampos.erroSenha(novaSenha))) {
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmar.setEnabled(false);

        apiService.redefinirSenha(new RedefinirSenhaRequest(email, codigo, novaSenha)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnConfirmar.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(RedefinirSenhaActivity.this, "Senha redefinida! Faça login com a nova senha.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RedefinirSenhaActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else if (response.code() == 400) {
                    Toast.makeText(RedefinirSenhaActivity.this, "Código inválido ou expirado", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RedefinirSenhaActivity.this, "Erro ao redefinir a senha", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnConfirmar.setEnabled(true);
                Toast.makeText(RedefinirSenhaActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
            }
        });
    }
}
