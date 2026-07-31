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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

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

public class OnboardingFotoPerfilActivity extends AppCompatActivity {

    private ApiService apiService;
    private ImageView imageFoto;
    private Uri fotoUriSelecionada;
    private long meuId;

    private final ActivityResultLauncher<String> seletorDeImagem =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoUriSelecionada = uri;
                    Glide.with(this)
                            .load(uri)
                            .override(500, 500)
                            .transform(new CircleCrop())
                            .into(imageFoto);
                    findViewById(R.id.textPlaceholderIconePerfil).setVisibility(android.view.View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_foto_perfil);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(this);

        imageFoto = findViewById(R.id.imageFotoPerfilOnboarding);

        findViewById(R.id.btnEscolherFotoPerfilOnboarding).setOnClickListener(v ->
                seletorDeImagem.launch("image/*"));

        findViewById(R.id.btnContinuarFotoPerfil).setOnClickListener(v -> continuar());
        findViewById(R.id.btnPularFotoPerfil).setOnClickListener(v -> irParaProximaTela());
        findViewById(R.id.btnVoltarOnboarding).setOnClickListener(v -> finish());
    }

    private void continuar() {
        if (fotoUriSelecionada != null) {
            enviarFoto();
        } else {
            irParaProximaTela();
        }
    }

    private void enviarFoto() {
        try {
            Bitmap bitmap = carregarBitmapReduzido(fotoUriSelecionada, 1080);
            if (bitmap == null) {
                irParaProximaTela();
                return;
            }

            File arquivoTemp = File.createTempFile("perfil_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(arquivoTemp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.close();
            bitmap.recycle();

            RequestBody requestFile = RequestBody.create(arquivoTemp, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", arquivoTemp.getName(), requestFile);

            apiService.uploadFotoPerfil(meuId, body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    irParaProximaTela();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(OnboardingFotoPerfilActivity.this, "Erro ao enviar foto, mas você pode continuar", Toast.LENGTH_SHORT).show();
                    irParaProximaTela();
                }
            });

        } catch (Exception e) {
            irParaProximaTela();
        }
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

    private void irParaProximaTela() {
        startActivity(new Intent(this, OnboardingFotoActivity.class));
    }
}