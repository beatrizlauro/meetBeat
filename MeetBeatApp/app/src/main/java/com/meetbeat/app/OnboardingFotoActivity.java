package com.meetbeat.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.UserSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingFotoActivity extends AppCompatActivity {

    private ApiService apiService;
    private ImageView imageFotoSelecionada;
    private Uri fotoUriSelecionada;
    private long meuId;

    private final ActivityResultLauncher<String> seletorDeImagem =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoUriSelecionada = uri;
                    imageFotoSelecionada.setImageURI(uri);
                    findViewById(R.id.textPlaceholderIcone).setVisibility(android.view.View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_foto);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        imageFotoSelecionada = findViewById(R.id.imageFotoSelecionadaOnboarding);

        findViewById(R.id.btnEscolherFotoOnboarding).setOnClickListener(v ->
                seletorDeImagem.launch("image/*"));

        findViewById(R.id.btnConcluirOnboarding).setOnClickListener(v -> concluir());
        findViewById(R.id.btnPularFoto).setOnClickListener(v -> finalizarOnboarding());
        findViewById(R.id.btnVoltarOnboarding).setOnClickListener(v -> finish());
    }

    private void concluir() {
        if (fotoUriSelecionada != null) {
            enviarFoto();
        } else {
            finalizarOnboarding();
        }
    }

    private void enviarFoto() {
        try {
            Bitmap bitmapRedimensionado = carregarBitmapReduzido(fotoUriSelecionada, 1080);

            if (bitmapRedimensionado == null) {
                Toast.makeText(this, "Erro ao processar a foto", Toast.LENGTH_SHORT).show();
                finalizarOnboarding();
                return;
            }

            File arquivoTemp = File.createTempFile("upload_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(arquivoTemp);
            bitmapRedimensionado.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.close();
            bitmapRedimensionado.recycle();

            RequestBody requestFile = RequestBody.create(arquivoTemp, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", arquivoTemp.getName(), requestFile);

            apiService.uploadFoto(meuId, body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    finalizarOnboarding();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(OnboardingFotoActivity.this, "Perfil criado, mas falhou o envio da foto", Toast.LENGTH_LONG).show();
                    finalizarOnboarding();
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao processar a foto", Toast.LENGTH_SHORT).show();
            finalizarOnboarding();
        }
    }

    private Bitmap carregarBitmapReduzido(Uri uri, int tamanhoMaximo) throws Exception {
        InputStream inputStreamMedidas = getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStreamMedidas, null, options);
        inputStreamMedidas.close();

        int larguraOriginal = options.outWidth;
        int alturaOriginal = options.outHeight;
        int inSampleSize = 1;

        while ((larguraOriginal / inSampleSize) > tamanhoMaximo || (alturaOriginal / inSampleSize) > tamanhoMaximo) {
            inSampleSize *= 2;
        }

        BitmapFactory.Options optionsFinal = new BitmapFactory.Options();
        optionsFinal.inSampleSize = inSampleSize;

        InputStream inputStreamFinal = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStreamFinal, null, optionsFinal);
        inputStreamFinal.close();

        return bitmap;
    }

    private void finalizarOnboarding() {
        apiService.finalizarOnboarding(meuId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                irParaApp();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // mesmo se falhar em marcar, deixamos a pessoa entrar no app
                irParaApp();
            }
        });
    }

    private void irParaApp() {
        Toast.makeText(this, "Perfil completo! Bem-vindo(a) ao Meet & Beat", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainContainerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}