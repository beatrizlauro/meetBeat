package com.meetbeat.app.fragment;

import android.app.Activity;
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
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.meetbeat.app.EditarGenerosActivity;
import com.meetbeat.app.EditarPerfilActivity;
import com.meetbeat.app.LoginActivity;
import com.meetbeat.app.R;
import com.meetbeat.app.RecorteFotoActivity;
import com.meetbeat.app.adapter.PostAdapter;
import com.meetbeat.app.model.AtualizarMostrarCurtidas;
import com.meetbeat.app.model.Genero;
import com.meetbeat.app.model.UsuarioCompleto;
import com.meetbeat.app.network.ApiService;
import com.meetbeat.app.network.RetrofitClient;
import com.meetbeat.app.util.Ajuda;
import com.meetbeat.app.util.AvisoDesenvolvimento;
import com.meetbeat.app.util.IconeGenero;
import com.meetbeat.app.util.ImageLoader;
import com.meetbeat.app.util.MensagensErro;
import com.meetbeat.app.util.ThemePreference;
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

    // Gêneros exibidos no perfil: até 5, e um chip "+N" para ver o restante
    private static final int LIMITE_GENEROS_VISIVEIS = 5;
    private final List<Genero> meusGeneros = new ArrayList<>();
    private boolean generosExpandidos = false;

    private final ActivityResultLauncher<Intent> recortador =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                Uri uri = resultado.getData() != null ? resultado.getData().getData() : null;
                if (resultado.getResultCode() == Activity.RESULT_OK && uri != null) {
                    enviarNovoPost(uri);
                }
            });

    private final ActivityResultLauncher<String> seletorDeImagem =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    recortador.launch(RecorteFotoActivity.criarIntent(requireContext(), uri,
                            RecorteFotoActivity.Formato.POST));
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
        postAdapter = new PostAdapter(posts, this::confirmarExclusaoPost);
        recyclerPosts.setAdapter(postAdapter);

        view.findViewById(R.id.btnAdicionarPost).setOnClickListener(v ->
                seletorDeImagem.launch("image/*"));

        view.findViewById(R.id.btnMenuPerfil).setOnClickListener(this::abrirMenuPerfil);

        // Atalhos para os módulos de ingressos e caronas, previstos para as próximas etapas
        view.findViewById(R.id.btnCarteiraPerfil).setOnClickListener(v ->
                AvisoDesenvolvimento.mostrar(requireContext(), "A carteira de ingressos"));
        view.findViewById(R.id.btnCaronasPerfil).setOnClickListener(v ->
                AvisoDesenvolvimento.mostrar(requireContext(), "O módulo de caronas"));

        ImageView btnAlternarTema = view.findViewById(R.id.btnAlternarTema);
        atualizarIconeTema(btnAlternarTema);
        btnAlternarTema.setOnClickListener(v -> {
            ThemePreference.alternar(requireContext());
            requireActivity().recreate();
        });

        return view;
    }

    private void atualizarIconeTema(ImageView botao) {
        boolean escuroAtivado = ThemePreference.isModoEscuroAtivado(requireContext());
        botao.setImageResource(escuroAtivado ? R.drawable.ic_sun : R.drawable.ic_moon);
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            carregarPerfil(view);
            carregarGeneros();
            carregarPosts();
        }
    }

    private void carregarPerfil(View view) {
        TextView textNome = view.findViewById(R.id.textPerfilNome);
        TextView textUsername = view.findViewById(R.id.textPerfilUsername);
        TextView textBio = view.findViewById(R.id.textPerfilBio);
        TextView textMusica = view.findViewById(R.id.textMusicaFavorita);
        TextView textArtistaSub = view.findViewById(R.id.textArtistaFavoritoSub);
        TextView textArtista = view.findViewById(R.id.textArtistaFavorito);
        ImageView imageFoto = view.findViewById(R.id.imagePerfilFoto);
        SwitchCompat switchMostrarCurtidas = view.findViewById(R.id.switchMostrarCurtidas);

        apiService.buscarUsuario(meuId).enqueue(new Callback<UsuarioCompleto>() {
            @Override
            public void onResponse(Call<UsuarioCompleto> call, Response<UsuarioCompleto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioCompleto usuario = response.body();

                    textNome.setText(usuario.getNome());
                    textUsername.setText("@" + usuario.getUsername());
                    exibirBio(textBio, usuario.getBio());

                    String musica = usuario.getMusicaFavorita();
                    String artista = usuario.getArtistaFavorito();

                    textMusica.setText((musica == null || musica.isEmpty()) ? "Sem música favorita" : musica);
                    textArtistaSub.setText((artista == null || artista.isEmpty()) ? "-" : artista);
                    textArtista.setText((artista == null || artista.isEmpty()) ? "-" : artista);

                    // Usa a foto de PERFIL (não a foto do show, que é usada no feed de matching)
                    ImageLoader.carregarCircular(requireContext(), usuario.getFotoPerfil(), imageFoto);

                    // Evita disparar uma chamada à API ao só refletir o estado atual vindo do servidor
                    switchMostrarCurtidas.setOnCheckedChangeListener(null);
                    switchMostrarCurtidas.setChecked(usuario.isMostrarCurtidas());
                    switchMostrarCurtidas.setOnCheckedChangeListener((buttonView, marcado) ->
                            atualizarPreferenciaCurtidas(marcado));
                }
            }

            @Override
            public void onFailure(Call<UsuarioCompleto> call, Throwable t) {
                Toast.makeText(requireContext(), "Erro ao carregar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarPreferenciaCurtidas(boolean mostrarCurtidas) {
        apiService.atualizarMostrarCurtidas(meuId, new AtualizarMostrarCurtidas(mostrarCurtidas)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Erro ao salvar preferência", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), MensagensErro.SEM_CONEXAO, Toast.LENGTH_SHORT).show();
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
                    atualizarDicaExcluirPost();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                // silencioso — grade só fica vazia
            }
        });
    }

    // Mostra a bio; se estiver vazia, exibe um convite que leva à edição do perfil
    private void exibirBio(TextView textBio, String bio) {
        if (bio != null && !bio.trim().isEmpty()) {
            textBio.setText(bio.trim());
            textBio.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_dark));
            textBio.setOnClickListener(null);
            textBio.setClickable(false);
        } else {
            textBio.setText("+ Adicionar uma bio para os outros fãs te conhecerem");
            textBio.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary));
            textBio.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditarPerfilActivity.class)));
        }
    }

    private void carregarGeneros() {
        apiService.getInteresses(meuId).enqueue(new Callback<List<Genero>>() {
            @Override
            public void onResponse(Call<List<Genero>> call, Response<List<Genero>> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                meusGeneros.clear();
                meusGeneros.addAll(response.body());
                exibirGeneros();
            }

            @Override
            public void onFailure(Call<List<Genero>> call, Throwable t) {
                // silencioso: a área de gêneros só fica vazia
            }
        });
    }

    private void exibirGeneros() {
        View view = getView();
        if (view == null) return;

        ChipGroup grupo = view.findViewById(R.id.chipGroupGenerosPerfil);
        grupo.removeAllViews();
        grupo.setVisibility(meusGeneros.isEmpty() ? View.GONE : View.VISIBLE);

        int total = meusGeneros.size();
        boolean cabemTodos = total <= LIMITE_GENEROS_VISIVEIS;
        int visiveis = (cabemTodos || generosExpandidos) ? total : LIMITE_GENEROS_VISIVEIS;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < visiveis; i++) {
            String nome = meusGeneros.get(i).getNome();
            Chip chip = (Chip) inflater.inflate(R.layout.item_chip_genero, grupo, false);
            chip.setText(nome);
            chip.setChipIconResource(IconeGenero.para(nome));
            grupo.addView(chip);
        }

        if (!cabemTodos) {
            int ocultos = total - LIMITE_GENEROS_VISIVEIS;
            Chip alternar = (Chip) inflater.inflate(R.layout.item_chip_genero, grupo, false);
            alternar.setChipIconVisible(false);
            alternar.setText(generosExpandidos ? "Ver menos" : "+" + ocultos);
            alternar.setContentDescription(generosExpandidos
                    ? "Mostrar menos gêneros" : "Mostrar mais " + ocultos + " gêneros");
            alternar.setTypeface(alternar.getTypeface(), android.graphics.Typeface.BOLD);
            alternar.setChipStrokeColorResource(R.color.purple_primary);
            alternar.setChipStrokeWidth(getResources().getDisplayMetrics().density);
            alternar.setClickable(true);
            alternar.setFocusable(true);
            alternar.setOnClickListener(v -> {
                generosExpandidos = !generosExpandidos;
                exibirGeneros();
            });
            grupo.addView(alternar);
        }
    }

    private void atualizarDicaExcluirPost() {
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.textDicaExcluirPost).setVisibility(posts.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void confirmarExclusaoPost(String urlFoto) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir foto?")
                .setMessage("A foto será removida do seu perfil.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialogo, botao) -> excluirPost(urlFoto))
                .show();
    }

    private void excluirPost(String urlFoto) {
        apiService.removerPost(meuId, urlFoto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    int posicao = posts.indexOf(urlFoto);
                    if (posicao >= 0) {
                        posts.remove(posicao);
                        postAdapter.notifyItemRemoved(posicao);
                    }
                    atualizarDicaExcluirPost();
                } else {
                    Toast.makeText(requireContext(), "Erro ao excluir a foto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Falha de conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
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

    private void confirmarLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sair da conta?")
                .setMessage("Você precisará entrar novamente com seu usuário e senha ou com o Google.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sair", (dialogo, botao) -> {
                    UserSession.logout(requireContext());
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                })
                .show();
    }

    private void abrirMenuPerfil(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add("Editar perfil");
        popup.getMenu().add("Editar gêneros musicais");
        popup.getMenu().add("Como funciona");
        popup.getMenu().add("Sair");

        popup.setOnMenuItemClickListener(item -> {
            String titulo = item.getTitle().toString();

            if (titulo.equals("Sair")) {
                confirmarLogout();
            } else if (titulo.equals("Editar perfil")) {
                startActivity(new Intent(requireContext(), EditarPerfilActivity.class));
            } else if (titulo.equals("Editar gêneros musicais")) {
                startActivity(new Intent(requireContext(), EditarGenerosActivity.class));
            } else if (titulo.equals("Como funciona")) {
                Ajuda.mostrarComoFunciona(requireContext());
            }

            return true;
        });

        popup.show();
    }
}