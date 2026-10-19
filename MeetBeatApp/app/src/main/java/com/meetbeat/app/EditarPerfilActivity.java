package com.meetbeat.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.meetbeat.app.model.AtualizarBio;
import com.meetbeat.app.model.AtualizarPerfil;
import com.meetbeat.app.model.UsuarioCompleto;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.ContadorCaracteres;
import com.meetbeat.app.util.ImageLoader;
import com.meetbeat.app.util.MensagensErro;
import com.meetbeat.app.util.MostrarSenha;
import com.meetbeat.app.util.UserSession;
import com.meetbeat.app.util.ValidacaoCampos;
import com.meetbeat.app.util.VisualHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilActivity extends AppCompatActivity {

    private ApiService apiService;
    private long meuId;

    private EditText editNome, editUsername, editEmail, editBio, editMusica, editArtista, editNovaSenha;
    private ImageView imageFotoPerfil, imageFotoFeed;
    private View btnRemoverFotoFeed;

    // Cada foto passa pela tela de recorte no formato em que será exibida antes do envio
    private final ActivityResultLauncher<Intent> recortadorPerfil =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                Uri uri = resultado.getData() != null ? resultado.getData().getData() : null;
                if (resultado.getResultCode() == RESULT_OK && uri != null) {
                    enviarNovaFotoPerfil(uri);
                }
            });

    private final ActivityResultLauncher<Intent> recortadorFeed =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                Uri uri = resultado.getData() != null ? resultado.getData().getData() : null;
                if (resultado.getResultCode() == RESULT_OK && uri != null) {
                    enviarNovaFotoFeed(uri);
                }
            });

    private final ActivityResultLauncher<String> seletorFotoPerfil =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    recortadorPerfil.launch(RecorteFotoActivity.criarIntent(this, uri, RecorteFotoActivity.Formato.PERFIL));
                }
            });

    private final ActivityResultLauncher<String> seletorFotoFeed =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    recortadorFeed.launch(RecorteFotoActivity.criarIntent(this, uri, RecorteFotoActivity.Formato.FEED));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        editNome = findViewById(R.id.editNomeEditar);
        editUsername = findViewById(R.id.editUsernameEditar);
        editEmail = findViewById(R.id.editEmailEditar);
        editBio = findViewById(R.id.editBioEditar);
        editMusica = findViewById(R.id.editMusicaEditar);
        editArtista = findViewById(R.id.editArtistaEditar);
        editNovaSenha = findViewById(R.id.editNovaSenhaEditar);
        MostrarSenha.ligar(editNovaSenha, findViewById(R.id.btnMostrarNovaSenhaEditar));

        ValidacaoCampos.restringir(editNome, ValidacaoCampos.Regra.NOME);
        ValidacaoCampos.restringir(editUsername, ValidacaoCampos.Regra.USERNAME);
        ValidacaoCampos.restringir(editEmail, ValidacaoCampos.Regra.SEM_ESPACOS);
        ValidacaoCampos.validarEmailAoSair(editEmail);
        ValidacaoCampos.restringir(editMusica, ValidacaoCampos.Regra.SEM_EMOJI);
        ValidacaoCampos.restringir(editArtista, ValidacaoCampos.Regra.SEM_EMOJI);
        imageFotoPerfil = findViewById(R.id.imageFotoPerfilEditar);
        imageFotoFeed = findViewById(R.id.imageFotoFeedEditar);
        btnRemoverFotoFeed = findViewById(R.id.btnRemoverFotoFeed);

        ContadorCaracteres.ligar(editBio, findViewById(R.id.textContadorBioEditar),
                getResources().getInteger(R.integer.max_caracteres_bio));

        findViewById(R.id.btnVoltarEditarPerfil).setOnClickListener(v -> finish());
        findViewById(R.id.btnTrocarFotoPerfil).setOnClickListener(v -> seletorFotoPerfil.launch("image/*"));
        imageFotoPerfil.setOnClickListener(v -> seletorFotoPerfil.launch("image/*"));
        findViewById(R.id.btnTrocarFotoFeed).setOnClickListener(v -> seletorFotoFeed.launch("image/*"));
        imageFotoFeed.setOnClickListener(v -> seletorFotoFeed.launch("image/*"));
        btnRemoverFotoFeed.setOnClickListener(v -> confirmarRemocaoFotoFeed());
        findViewById(R.id.btnSalvarEdicaoPerfil).setOnClickListener(v -> salvar());

        carregarDadosAtuais();
    }

    private void carregarDadosAtuais() {
        apiService.buscarUsuario(meuId).enqueue(new Callback<UsuarioCompleto>() {
            @Override
            public void onResponse(Call<UsuarioCompleto> call, Response<UsuarioCompleto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioCompleto usuario = response.body();
                    editNome.setText(usuario.getNome());
                    editUsername.setText(usuario.getUsername());
                    editEmail.setText(usuario.getEmail());
                    editBio.setText(usuario.getBio());
                    editMusica.setText(usuario.getMusicaFavorita());
                    editArtista.setText(usuario.getArtistaFavorito());
                    ImageLoader.carregarCircular(EditarPerfilActivity.this, usuario.getFotoPerfil(), imageFotoPerfil);
                    ImageLoader.carregar(EditarPerfilActivity.this, usuario.getFotoShow(), imageFotoFeed);
                    String fotoFeed = usuario.getFotoShow();
                    btnRemoverFotoFeed.setVisibility(fotoFeed == null || fotoFeed.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<UsuarioCompleto> call, Throwable t) {
                Toast.makeText(EditarPerfilActivity.this, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void salvar() {
        String nome = ValidacaoCampos.normalizarEspacos(editNome.getText().toString());
        String username = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        String musica = editMusica.getText().toString().trim();
        String artista = editArtista.getText().toString().trim();
        String novaSenha = editNovaSenha.getText().toString().trim();
        String bio = editBio.getText().toString().trim();

        if (ValidacaoCampos.mostrarErro(editNome, ValidacaoCampos.erroNome(nome))
                || ValidacaoCampos.mostrarErro(editUsername, ValidacaoCampos.erroUsername(username))
                || ValidacaoCampos.mostrarErro(editEmail, ValidacaoCampos.erroEmail(email, false))
                || (!novaSenha.isEmpty()
                    && ValidacaoCampos.mostrarErro(editNovaSenha, ValidacaoCampos.erroSenha(novaSenha)))) {
            return;
        }

        AtualizarPerfil dados = new AtualizarPerfil(nome, username, email, musica, artista,
                novaSenha.isEmpty() ? null : novaSenha);

        View btnSalvar = findViewById(R.id.btnSalvarEdicaoPerfil);
        VisualHelper.definirOcupado(btnSalvar, true);

        apiService.atualizarPerfil(meuId, dados).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    salvarBio(bio);
                    return;
                }
                VisualHelper.definirOcupado(btnSalvar, false);
                if (response.code() == 409) {
                    Toast.makeText(EditarPerfilActivity.this, "Esse nome de usuário ou e-mail já está em uso", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditarPerfilActivity.this, "Não foi possível salvar. Confira os dados e tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                VisualHelper.definirOcupado(btnSalvar, false);
                Toast.makeText(EditarPerfilActivity.this, MensagensErro.SEM_CONEXAO, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void salvarBio(String bio) {
        apiService.atualizarBio(meuId, new AtualizarBio(bio)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(EditarPerfilActivity.this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // O restante do perfil já foi salvo com sucesso; só a bio falhou
                Toast.makeText(EditarPerfilActivity.this, "Perfil salvo, mas a bio não foi atualizada", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void enviarNovaFotoPerfil(Uri uri) {
        try {
            Bitmap bitmap = carregarBitmapReduzido(uri, 1080);
            if (bitmap == null) return;

            // Usa o mesmo recorte circular da foto já salva, para a pré-visualização não
            // aparecer quadrada antes do envio ao servidor
            Glide.with(this).load(bitmap).circleCrop().into(imageFotoPerfil);

            File arquivoTemp = File.createTempFile("perfil_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(arquivoTemp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.close();

            RequestBody requestFile = RequestBody.create(arquivoTemp, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", arquivoTemp.getName(), requestFile);

            apiService.uploadFotoPerfil(meuId, body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (!response.isSuccessful()) {
                        Toast.makeText(EditarPerfilActivity.this, "Erro ao enviar foto", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(EditarPerfilActivity.this, "Erro ao enviar foto", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao processar a foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void enviarNovaFotoFeed(Uri uri) {
        try {
            Bitmap bitmap = carregarBitmapReduzido(uri, 1080);
            if (bitmap == null) return;

            Glide.with(this).load(bitmap).centerCrop().into(imageFotoFeed);

            File arquivoTemp = File.createTempFile("feed_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(arquivoTemp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.close();

            RequestBody requestFile = RequestBody.create(arquivoTemp, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", arquivoTemp.getName(), requestFile);

            apiService.uploadFoto(meuId, body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        btnRemoverFotoFeed.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(EditarPerfilActivity.this, "Erro ao enviar foto", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(EditarPerfilActivity.this, "Erro ao enviar foto", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao processar a foto", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmarRemocaoFotoFeed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remover foto do feed?")
                .setMessage("Seu card nas sugestões de match ficará sem foto até você escolher outra.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Remover", (dialogo, botao) -> removerFotoFeed())
                .show();
    }

    private void removerFotoFeed() {
        apiService.removerFotoFeed(meuId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ImageLoader.carregar(EditarPerfilActivity.this, null, imageFotoFeed);
                    btnRemoverFotoFeed.setVisibility(View.GONE);
                    Toast.makeText(EditarPerfilActivity.this, "Foto do feed removida", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditarPerfilActivity.this, "Erro ao remover a foto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditarPerfilActivity.this, "Falha de conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap carregarBitmapReduzido(Uri uri, int tamanhoMaximo) throws Exception {
        InputStream inputStreamMedidas = getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStreamMedidas, null, options);
        inputStreamMedidas.close();

        int largura = options.outWidth;
        int altura = options.outHeight;
        int inSampleSize = 1;

        while ((largura / inSampleSize) > tamanhoMaximo || (altura / inSampleSize) > tamanhoMaximo) {
            inSampleSize *= 2;
        }

        BitmapFactory.Options optionsFinal = new BitmapFactory.Options();
        optionsFinal.inSampleSize = inSampleSize;

        InputStream inputStreamFinal = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStreamFinal, null, optionsFinal);
        inputStreamFinal.close();

        return bitmap;
    }
}
