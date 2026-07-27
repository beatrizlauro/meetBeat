package com.meetbeat.app;

import android.net.Uri;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.meetbeat.app.model.Genero;
import com.meetbeat.app.model.MatchSuggestion;
import com.meetbeat.app.model.NovoUsuario;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroActivity extends AppCompatActivity {

    private ApiService apiService;
    private ImageView imageFotoSelecionada;
    private EditText editNome, editBio;
    private LinearLayout containerGeneros;

    private Uri fotoUriSelecionada;
    private final List<CheckBox> checkboxesGeneros = new ArrayList<>();

    private final ActivityResultLauncher<String> seletorDeImagem =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoUriSelecionada = uri;
                    imageFotoSelecionada.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        apiService = RetrofitClient.getApiService();

        imageFotoSelecionada = findViewById(R.id.imageFotoSelecionada);
        editNome = findViewById(R.id.editNome);
        editBio = findViewById(R.id.editBio);
        containerGeneros = findViewById(R.id.containerGeneros);

        findViewById(R.id.btnVoltarCadastro).setOnClickListener(v -> finish());

        findViewById(R.id.btnEscolherFoto).setOnClickListener(v ->
                seletorDeImagem.launch("image/*"));

        findViewById(R.id.btnSalvarCadastro).setOnClickListener(v -> salvarCadastro());

        carregarGeneros();
    }

    private void carregarGeneros() {
        apiService.getGeneros().enqueue(new Callback<List<Genero>>() {
            @Override
            public void onResponse(Call<List<Genero>> call, Response<List<Genero>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Genero genero : response.body()) {
                        CheckBox checkBox = new CheckBox(CadastroActivity.this);
                        checkBox.setText(genero.getNome());
                        checkBox.setTag(genero.getId());
                        checkboxesGeneros.add(checkBox);
                        containerGeneros.addView(checkBox);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Genero>> call, Throwable t) {
                Toast.makeText(CadastroActivity.this, "Erro ao carregar gêneros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void salvarCadastro() {
        String nome = editNome.getText().toString().trim();
        String bio = editBio.getText().toString().trim();

        if (nome.isEmpty()) {
            Toast.makeText(this, "Digite um nome", Toast.LENGTH_SHORT).show();
            return;
        }

        NovoUsuario novoUsuario = new NovoUsuario(nome, bio);

        apiService.criarUsuario(novoUsuario).enqueue(new Callback<MatchSuggestion>() {
            @Override
            public void onResponse(Call<MatchSuggestion> call, Response<MatchSuggestion> response) {
                if (response.isSuccessful() && response.body() != null) {
                    long novoId = response.body().getId();
                    associarGenerosSelecionados(novoId);
                } else {
                    Toast.makeText(CadastroActivity.this, "Erro ao criar usuário", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MatchSuggestion> call, Throwable t) {
                Toast.makeText(CadastroActivity.this, "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void associarGenerosSelecionados(long usuarioId) {
        for (CheckBox checkBox : checkboxesGeneros) {
            if (checkBox.isChecked()) {
                long generoId = (long) checkBox.getTag();
                apiService.adicionarInteresse(usuarioId, generoId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        // Silencioso: cada interesse é associado em segundo plano
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Silencioso, não bloqueia o fluxo principal
                    }
                });
            }
        }

        if (fotoUriSelecionada != null) {
            enviarFoto(usuarioId);
        } else {
            finalizarCadastro();
        }
    }

    private void enviarFoto(long usuarioId) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fotoUriSelecionada);
            File arquivoTemp = File.createTempFile("upload_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(arquivoTemp);

            byte[] buffer = new byte[4096];
            int bytesLidos;
            while ((bytesLidos = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesLidos);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(arquivoTemp, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", arquivoTemp.getName(), requestFile);

            apiService.uploadFoto(usuarioId, body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    finalizarCadastro();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CadastroActivity.this, "Perfil criado, mas falhou o envio da foto", Toast.LENGTH_LONG).show();
                    finalizarCadastro();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao processar a foto", Toast.LENGTH_SHORT).show();
            finalizarCadastro();
        }
    }

    private void finalizarCadastro() {
        Toast.makeText(this, "Perfil criado com sucesso! 🎉", Toast.LENGTH_LONG).show();
        finish();
    }
}