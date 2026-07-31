package com.meetbeat.app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.meetbeat.app.LoginActivity;
import com.meetbeat.app.R;
import com.meetbeat.app.adapter.PostAdapter;
import com.meetbeat.app.model.UsuarioCompleto;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.ImageLoader;
import com.meetbeat.app.util.UserSession;

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

public class ProfileFragment extends Fragment {

    private ApiService apiService;
    private long meuId;
    private RecyclerView recyclerPosts;
    private PostAdapter postAdapter;
    private final List<String> posts = new ArrayList<>();

    private final ActivityResultLauncher<String> seletorDeImagem =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    enviarNovoPost(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        apiService = RetrofitClient.getApiService();
        meuId = UserSession.getUsuarioLogado(requireContext());

        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        recyclerPosts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        postAdapter = new PostAdapter(posts);
        recyclerPosts.setAdapter(postAdapter);

        carregarPerfil(view);
        carregarPosts();

        view.findViewById(R.id.btnAdicionarPost).setOnClickListener(v ->
                seletorDeImagem.launch("image/*"));

        view.findViewById(R.id.btnMenuPerfil).setOnClickListener(this::abrirMenuPerfil);

        return view;
    }

    private void carregarPerfil(View view) {
        TextView textNome = view.findViewById(R.id.textPerfilNome);
        TextView textUsername = view.findViewById(R.id.textPerfilUsername);
        TextView textMusica = view.findViewById(R.id.textMusicaFavorita);
        TextView textArtistaSub = view.findViewById(R.id.textArtistaFavoritoSub);
        TextView textArtista = view.findViewById(R.id.textArtistaFavorito);
        ImageView imageFoto = view.findViewById(R.id.imagePerfilFoto);

        apiService.buscarUsuario(meuId).enqueue(new Callback<UsuarioCompleto>() {
            @Override
            public void onResponse(Call<UsuarioCompleto> call, Response<UsuarioCompleto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioCompleto usuario = response.body();

                    textNome.setText(usuario.getNome());
                    textUsername.setText("@" + usuario.getUsername());

                    String musica = usuario.getMusicaFavorita();
                    String artista = usuario.getArtistaFavorito();

                    textMusica.setText((musica == null || musica.isEmpty()) ? "Sem música favorita" : musica);
                    textArtistaSub.setText((artista == null || artista.isEmpty()) ? "-" : artista);
                    textArtista.setText((artista == null || artista.isEmpty()) ? "-" : artista);

                    // Usa a foto de PERFIL (não a foto do show, que é usada no feed de matching)
                    ImageLoader.carregarCircular(requireContext(), usuario.getFotoPerfil(), imageFoto);
                }
            }

            @Override
            public void onFailure(Call<UsuarioCompleto> call, Throwable t) {
                Toast.makeText(requireContext(), "Erro ao carregar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarPosts() {
        apiService.listarPosts(meuId).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    posts.clear();
                    posts.addAll(response.body());
                    postAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                // silencioso — grade só fica vazia
            }
        });
    }

    private void enviarNovoPost(Uri uri) {
        try {
            Bitmap bitmap = carregarBitmapReduzido(uri, 1080);
            if (bitmap == null) return;

            File arquivoTemp = File.createTempFile("post_", ".jpg", requireContext().getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(arquivoTemp);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.close();
            bitmap.recycle();

            RequestBody requestFile = RequestBody.create(arquivoTemp, MediaType.parse("image/jpeg"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", arquivoTemp.getName(), requestFile);

            apiService.uploadPost(meuId, body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    carregarPosts();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(requireContext(), "Erro ao enviar foto", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Erro ao processar a foto", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap carregarBitmapReduzido(Uri uri, int tamanhoMaximo) throws Exception {
        InputStream inputStreamMedidas = requireContext().getContentResolver().openInputStream(uri);
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

        InputStream inputStreamFinal = requireContext().getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStreamFinal, null, optionsFinal);
        inputStreamFinal.close();

        return bitmap;
    }

    private void abrirMenuPerfil(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add("Editar perfil");
        popup.getMenu().add("Sair");

        popup.setOnMenuItemClickListener(item -> {
            String titulo = item.getTitle().toString();

            if (titulo.equals("Sair")) {
                UserSession.logout(requireContext());
                startActivity(new Intent(requireContext(), LoginActivity.class));
                requireActivity().finish();
            } else if (titulo.equals("Editar perfil")) {
                Toast.makeText(requireContext(), "Tela de edição em construção", Toast.LENGTH_SHORT).show();
                // Na Fase C, vamos abrir a EditarPerfilActivity aqui
            }

            return true;
        });

        popup.show();
    }
}