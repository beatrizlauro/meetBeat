package com.meetbeat.matching.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.meetbeat.matching.dto.AdicionarInteressesDTO;
import com.meetbeat.matching.dto.AtualizarBioDTO;
import com.meetbeat.matching.dto.AtualizarMostrarCurtidasDTO;
import com.meetbeat.matching.dto.AtualizarPerfilDTO;
import com.meetbeat.matching.dto.CurtidaRecebidaDTO;
import com.meetbeat.matching.dto.GeneroDTO;
import com.meetbeat.matching.dto.GoogleLoginRequestDTO;
import com.meetbeat.matching.dto.LoginRequestDTO;
import com.meetbeat.matching.dto.LoginResponseDTO;
import com.meetbeat.matching.dto.MatchConfirmadoDTO;
import com.meetbeat.matching.dto.MatchSuggestionDTO;
import com.meetbeat.matching.dto.NovoUsuarioDTO;
import com.meetbeat.matching.dto.SwipeResponseDTO;
import com.meetbeat.matching.dto.UserResponseDTO;
import com.meetbeat.matching.model.UserNode;
import com.meetbeat.matching.repository.UserRepository;
import com.meetbeat.matching.dto.AtualizarMusicaArtistaDTO;
import com.meetbeat.matching.security.JwtAuthFilter;
import com.meetbeat.matching.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserRepository userRepository;
    private final Neo4jClient neo4jClient;
    private final PasswordEncoder passwordEncoder;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final JwtService jwtService;

    public UserController(UserRepository userRepository, Neo4jClient neo4jClient, PasswordEncoder passwordEncoder,
                           GoogleIdTokenVerifier googleIdTokenVerifier, JwtService jwtService) {
        this.userRepository = userRepository;
        this.neo4jClient = neo4jClient;
        this.passwordEncoder = passwordEncoder;
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.jwtService = jwtService;
    }

    // Garante que o usuário autenticado pelo token só acesse/modifique os próprios dados (OWASP API1:2019)
    private void verificarPropriedade(HttpServletRequest request, Long id) {
        Object usuarioAutenticadoId = request.getAttribute(JwtAuthFilter.ATRIBUTO_USUARIO_ID);
        if (!id.equals(usuarioAutenticadoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso");
        }
    }

    @GetMapping
    public List<UserResponseDTO> listarTodos() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> criarUsuario(@Valid @RequestBody NovoUsuarioDTO dto) {

        String verificarUsername = """
            MATCH (p:Pessoa {username: $username})
            RETURN count(p) AS total
            """;

        Long existente = neo4jClient.query(verificarUsername)
                .bind(dto.getUsername()).to("username")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("total").asLong())
                .one()
                .orElse(0L);

        if (existente > 0) {
            return ResponseEntity.status(409).body("Nome de usuário já está em uso");
        }

        UserNode novoUsuario = new UserNode(dto.getNome(), dto.getBio());
        novoUsuario.setUsername(dto.getUsername());
        novoUsuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        novoUsuario.setOnboardingCompleto(false);

        return ResponseEntity.ok(userRepository.save(novoUsuario));
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {

        String cypher = """
            MATCH (p:Pessoa {username: $username})
            RETURN id(p) AS userId, p.nome AS userNome, p.senhaHash AS senhaHash, p.onboardingCompleto AS onboarding
            """;

        Optional<Object[]> resultado = neo4jClient.query(cypher)
                .bind(dto.getUsername()).to("username")
                .fetchAs(Object[].class)
                .mappedBy((typeSystem, record) -> new Object[]{
                        record.get("userId").asLong(),
                        record.get("userNome").asString(),
                        record.get("senhaHash").asString(null),
                        record.get("onboarding").asBoolean(false)
                })
                .one();

        if (resultado.isEmpty()) {
            return new LoginResponseDTO(false, "Usuário não encontrado", null, null, false);
        }

        Object[] dados = resultado.get();
        Long userId = (Long) dados[0];
        String userNome = (String) dados[1];
        String senhaHash = (String) dados[2];
        Boolean onboardingCompleto = (Boolean) dados[3];

        if (senhaHash == null || !passwordEncoder.matches(dto.getSenha(), senhaHash)) {
            return new LoginResponseDTO(false, "Senha incorreta", null, null, false);
        }

        LoginResponseDTO resposta = new LoginResponseDTO(true, "Login realizado com sucesso", userId, userNome, onboardingCompleto);
        resposta.setToken(jwtService.gerarToken(userId));
        return resposta;
    }

    @PostMapping("/login-google")
    public ResponseEntity<LoginResponseDTO> loginGoogle(@Valid @RequestBody GoogleLoginRequestDTO dto) {

        GoogleIdToken idToken;
        try {
            idToken = googleIdTokenVerifier.verify(dto.getIdToken());
        } catch (Exception e) {
            idToken = null;
        }

        if (idToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponseDTO(false, "Token do Google inválido", null, null, false));
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String nomeGoogle = (String) payload.get("name");

        UserNode usuario = userRepository.findByGoogleId(googleId).orElseGet(() -> {
            String username = gerarUsernameDisponivel(email);
            UserNode novo = new UserNode(nomeGoogle != null && !nomeGoogle.isBlank() ? nomeGoogle : username, "");
            novo.setUsername(username);
            novo.setGoogleId(googleId);
            novo.setOnboardingCompleto(false);
            return userRepository.save(novo);
        });

        LoginResponseDTO resposta = new LoginResponseDTO(true, "Login realizado com sucesso", usuario.getId(),
                usuario.getNome(), Boolean.TRUE.equals(usuario.getOnboardingCompleto()));
        resposta.setToken(jwtService.gerarToken(usuario.getId()));
        return ResponseEntity.ok(resposta);
    }

    // Deriva um nome de usuário a partir do e-mail do Google, garantindo unicidade
    private String gerarUsernameDisponivel(String email) {
        String base = (email != null ? email.split("@")[0] : "usuario")
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        if (base.isEmpty()) {
            base = "usuario";
        }

        String candidato = base;
        int sufixo = 1;
        while (userRepository.findByUsername(candidato).isPresent()) {
            candidato = base + sufixo;
            sufixo++;
        }
        return candidato;
    }

    @PutMapping("/{id}/bio")
    public void atualizarBio(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody AtualizarBioDTO dto) {
        verificarPropriedade(request, id);

        String cypher = """
            MATCH (p:Pessoa) WHERE id(p) = $userId
            SET p.bio = $bio
            """;

        neo4jClient.query(cypher)
                .bind(id).to("userId")
                .bind(dto.getBio()).to("bio")
                .run();
    }

    @PostMapping("/{id}/finalizar-onboarding")
    public void finalizarOnboarding(HttpServletRequest request, @PathVariable Long id) {
        verificarPropriedade(request, id);

        String cypher = """
            MATCH (p:Pessoa) WHERE id(p) = $userId
            SET p.onboardingCompleto = true
            """;

        neo4jClient.query(cypher)
                .bind(id).to("userId")
                .run();
    }

    @GetMapping("/{id}/sugestoes")
    public List<MatchSuggestionDTO> sugestoesDeMatch(HttpServletRequest request, @PathVariable Long id) {
        verificarPropriedade(request, id);

        String cypher = """
            MATCH (me:Pessoa)-[:GOSTA_DE]->(g:Genero)<-[:GOSTA_DE]-(other:Pessoa)
            WHERE id(me) = $userId AND me <> other
              AND NOT (me)-[:CURTIU]->(other)
              AND NOT (me)-[:REJEITOU]->(other)
            WITH other, collect(g.nome) AS generosComuns
            RETURN id(other) AS userId, other.nome AS userNome, other.fotoShow AS userFoto,
                   generosComuns, size(generosComuns) AS matchScore
            ORDER BY matchScore DESC
            """;

        Collection<MatchSuggestionDTO> resultados = neo4jClient.query(cypher)
                .bind(id).to("userId")
                .fetchAs(MatchSuggestionDTO.class)
                .mappedBy((typeSystem, record) -> new MatchSuggestionDTO(
                        record.get("userId").asLong(),
                        record.get("userNome").asString(),
                        record.get("matchScore").asLong(),
                        record.get("generosComuns").asList(value -> value.asString()),
                        record.get("userFoto").asString("")
                ))
                .all();

        return new ArrayList<>(resultados);
    }

    @PostMapping("/{id}/curtir/{targetId}")
    public SwipeResponseDTO curtir(HttpServletRequest request, @PathVariable Long id, @PathVariable Long targetId) {
        verificarPropriedade(request, id);

        String criarCurtida = """
            MATCH (me:Pessoa), (other:Pessoa)
            WHERE id(me) = $meId AND id(other) = $otherId
            MERGE (me)-[:CURTIU]->(other)
            """;

        neo4jClient.query(criarCurtida)
                .bind(id).to("meId")
                .bind(targetId).to("otherId")
                .run();

        String verificarMatch = """
            MATCH (me:Pessoa)-[:CURTIU]->(other:Pessoa)-[:CURTIU]->(me)
            WHERE id(me) = $meId AND id(other) = $otherId
            RETURN count(*) AS total
            """;

        Long total = neo4jClient.query(verificarMatch)
                .bind(id).to("meId")
                .bind(targetId).to("otherId")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("total").asLong())
                .one()
                .orElse(0L);

        if (total > 0) {
            return new SwipeResponseDTO(true, "É um match!");
        } else {
            return new SwipeResponseDTO(false, "Curtida registrada. Aguardando o outro usuário.");
        }
    }

    @PostMapping("/{id}/rejeitar/{targetId}")
    public void rejeitar(HttpServletRequest request, @PathVariable Long id, @PathVariable Long targetId) {
        verificarPropriedade(request, id);

        String criarRejeicao = """
            MATCH (me:Pessoa), (other:Pessoa)
            WHERE id(me) = $meId AND id(other) = $otherId
            MERGE (me)-[:REJEITOU]->(other)
            """;

        neo4jClient.query(criarRejeicao)
                .bind(id).to("meId")
                .bind(targetId).to("otherId")
                .run();
    }

    @GetMapping("/{id}/matches")
    public List<MatchConfirmadoDTO> meusMatches(HttpServletRequest request, @PathVariable Long id) {
        verificarPropriedade(request, id);

        String cypher = """
            MATCH (me:Pessoa)-[:CURTIU]->(other:Pessoa)-[:CURTIU]->(me)
            WHERE id(me) = $userId
            OPTIONAL MATCH (me)-[:GOSTA_DE]->(g:Genero)<-[:GOSTA_DE]-(other)
            WITH DISTINCT other, collect(DISTINCT g.nome) AS generosComuns
            RETURN id(other) AS userId, other.nome AS userNome, other.fotoShow AS userFoto, generosComuns
            """;

        Collection<MatchConfirmadoDTO> resultados = neo4jClient.query(cypher)
                .bind(id).to("userId")
                .fetchAs(MatchConfirmadoDTO.class)
                .mappedBy((typeSystem, record) -> new MatchConfirmadoDTO(
                        record.get("userId").asLong(),
                        record.get("userNome").asString(),
                        record.get("userFoto").asString(""),
                        record.get("generosComuns").asList(value -> value.asString())
                ))
                .all();

        return new ArrayList<>(resultados);
    }

    @GetMapping("/{id}/curtidas")
    public List<CurtidaRecebidaDTO> curtidasRecebidas(HttpServletRequest request, @PathVariable Long id) {
        verificarPropriedade(request, id);

        UserNode eu = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        // Mesma lógica do "visualizado" do WhatsApp: só posso ver quem me curtiu se eu também permito ser visto
        if (!Boolean.TRUE.equals(eu.getMostrarCurtidas())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Ative a opção \"Mostrar minhas curtidas\" no seu perfil para ver quem curtiu você");
        }

        String cypher = """
            MATCH (other:Pessoa)-[:CURTIU]->(me:Pessoa)
            WHERE id(me) = $userId AND me <> other
              AND NOT (me)-[:CURTIU]->(other)
              AND NOT (me)-[:REJEITOU]->(other)
              AND coalesce(other.mostrarCurtidas, true) = true
            OPTIONAL MATCH (me)-[:GOSTA_DE]->(g:Genero)<-[:GOSTA_DE]-(other)
            WITH DISTINCT other, collect(DISTINCT g.nome) AS generosComuns
            RETURN id(other) AS userId, other.nome AS userNome, other.fotoShow AS userFoto, generosComuns
            """;

        Collection<CurtidaRecebidaDTO> resultados = neo4jClient.query(cypher)
                .bind(id).to("userId")
                .fetchAs(CurtidaRecebidaDTO.class)
                .mappedBy((typeSystem, record) -> new CurtidaRecebidaDTO(
                        record.get("userId").asLong(),
                        record.get("userNome").asString(),
                        record.get("userFoto").asString(""),
                        record.get("generosComuns").asList(value -> value.asString())
                ))
                .all();

        return new ArrayList<>(resultados);
    }

    @PutMapping("/{id}/mostrar-curtidas")
    public void atualizarMostrarCurtidas(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody AtualizarMostrarCurtidasDTO dto) {
        verificarPropriedade(request, id);

        String cypher = """
            MATCH (p:Pessoa) WHERE id(p) = $userId
            SET p.mostrarCurtidas = $valor
            """;

        neo4jClient.query(cypher)
                .bind(id).to("userId")
                .bind(dto.getMostrarCurtidas()).to("valor")
                .run();
    }

    @GetMapping("/generos")
    public List<GeneroDTO> listarGeneros() {

        String cypher = """
            MATCH (g:Genero)
            RETURN id(g) AS generoId, g.nome AS generoNome
            ORDER BY g.nome
            """;

        Collection<GeneroDTO> resultados = neo4jClient.query(cypher)
                .fetchAs(GeneroDTO.class)
                .mappedBy((typeSystem, record) -> new GeneroDTO(
                        record.get("generoId").asLong(),
                        record.get("generoNome").asString()
                ))
                .all();

        return new ArrayList<>(resultados);
    }

    @PostMapping("/{id}/interesses")
    public void adicionarInteresses(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody AdicionarInteressesDTO dto) {
        verificarPropriedade(request, id);

        List<Long> generoIds = dto.getGeneroIds() != null ? dto.getGeneroIds() : List.of();

        // Uma única query (em vez de uma chamada por gênero) garante que a seleção inteira do
        // onboarding seja salva de forma atômica, sem risco de ficar pela metade
        String cypher = """
            UNWIND $generoIds AS generoId
            MATCH (p:Pessoa), (g:Genero)
            WHERE id(p) = $userId AND id(g) = generoId
            MERGE (p)-[:GOSTA_DE]->(g)
            """;

        neo4jClient.query(cypher)
                .bind(id).to("userId")
                .bind(generoIds).to("generoIds")
                .run();
    }

    @PostMapping("/{id}/foto")
    public String uploadFoto(HttpServletRequest request, @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        verificarPropriedade(request, id);

        String extensao = validarArquivoDeImagem(file);
        String uploadDir = "./uploads";
        Files.createDirectories(Paths.get(uploadDir));

        String nomeArquivo = "user_" + id + "_" + System.currentTimeMillis() + extensao;
        Path caminho = Paths.get(uploadDir, nomeArquivo);
        Files.write(caminho, file.getBytes());

        String urlPublica = "/uploads/" + nomeArquivo;

        String cypher = """
            MATCH (p:Pessoa) WHERE id(p) = $userId
            SET p.fotoShow = $url
            """;

        neo4jClient.query(cypher)
                .bind(id).to("userId")
                .bind(urlPublica).to("url")
                .run();

        return urlPublica;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarPerfil(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody AtualizarPerfilDTO dto) {
        verificarPropriedade(request, id);

        // Verifica se o novo username já pertence a OUTRA pessoa
        String verificarUsername = """
            MATCH (p:Pessoa {username: $username})
            WHERE id(p) <> $userId
            RETURN count(p) AS total
            """;

        Long existente = neo4jClient.query(verificarUsername)
                .bind(dto.getUsername()).to("username")
                .bind(id).to("userId")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("total").asLong())
                .one()
                .orElse(0L);

        if (existente > 0) {
            return ResponseEntity.status(409).body("Nome de usuário já está em uso");
        }

        String cypherBase = """
            MATCH (p:Pessoa) WHERE id(p) = $userId
            SET p.nome = $nome, p.username = $username, p.musicaFavorita = $musica, p.artistaFavorito = $artista
            """;

        neo4jClient.query(cypherBase)
                .bind(id).to("userId")
                .bind(dto.getNome()).to("nome")
                .bind(dto.getUsername()).to("username")
                .bind(dto.getMusicaFavorita()).to("musica")
                .bind(dto.getArtistaFavorito()).to("artista")
                .run();

        if (dto.getNovaSenha() != null && !dto.getNovaSenha().isEmpty()) {
            String hash = passwordEncoder.encode(dto.getNovaSenha());
            String cypherSenha = """
                MATCH (p:Pessoa) WHERE id(p) = $userId
                SET p.senhaHash = $hash
                """;
            neo4jClient.query(cypherSenha)
                    .bind(id).to("userId")
                    .bind(hash).to("hash")
                    .run();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/posts")
    public String uploadPost(HttpServletRequest request, @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        verificarPropriedade(request, id);

        String extensao = validarArquivoDeImagem(file);
        String uploadDir = "./uploads";
        Files.createDirectories(Paths.get(uploadDir));

        String nomeArquivo = "post_" + id + "_" + System.currentTimeMillis() + extensao;
        Path caminho = Paths.get(uploadDir, nomeArquivo);
        Files.write(caminho, file.getBytes());

        String urlPublica = "/uploads/" + nomeArquivo;

        String cypher = """
            MATCH (p:Pessoa) WHERE id(p) = $userId
            SET p.fotosPosts = coalesce(p.fotosPosts, []) + $url
            """;

        neo4jClient.query(cypher)
                .bind(id).to("userId")
                .bind(urlPublica).to("url")
                .run();

        return urlPublica;
    }

    @GetMapping("/{id}/posts")
    public List<String> listarPosts(HttpServletRequest request, @PathVariable Long id) {
        verificarPropriedade(request, id);

        String cypher = """
            MATCH (p:Pessoa) WHERE id(p) = $userId
            RETURN p.fotosPosts AS posts
            """;

        return neo4jClient.query(cypher)
                .bind(id).to("userId")
                .fetchAs(List.class)
                .mappedBy((typeSystem, record) -> {
                    var value = record.get("posts");
                    return value.isNull() ? new ArrayList<String>() : value.asList(v -> v.asString());
                })
                .one()
                .orElse(new ArrayList<>());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> buscarPorId(HttpServletRequest request, @PathVariable Long id) {
        verificarPropriedade(request, id);
        return userRepository.findById(id)
                .map(UserResponseDTO::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/foto-perfil")
    public String uploadFotoPerfil(HttpServletRequest request, @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        verificarPropriedade(request, id);

        String extensao = validarArquivoDeImagem(file);
        String uploadDir = "./uploads";
        Files.createDirectories(Paths.get(uploadDir));

        String nomeArquivo = "perfil_" + id + "_" + System.currentTimeMillis() + extensao;
        Path caminho = Paths.get(uploadDir, nomeArquivo);
        Files.write(caminho, file.getBytes());

        String urlPublica = "/uploads/" + nomeArquivo;

        String cypher = """
            MATCH (p:Pessoa) WHERE id(p) = $userId
            SET p.fotoPerfil = $url
            """;

        neo4jClient.query(cypher)
                .bind(id).to("userId")
                .bind(urlPublica).to("url")
                .run();

        return urlPublica;
    }

    @PutMapping("/{id}/musica-artista")
    public void atualizarMusicaArtista(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody AtualizarMusicaArtistaDTO dto) {
        verificarPropriedade(request, id);

        String cypher = """
            MATCH (p:Pessoa) WHERE id(p) = $userId
            SET p.musicaFavorita = $musica, p.artistaFavorito = $artista
            """;

        neo4jClient.query(cypher)
                .bind(id).to("userId")
                .bind(dto.getMusicaFavorita()).to("musica")
                .bind(dto.getArtistaFavorito()).to("artista")
                .run();
    }

    // Confere o content-type real do arquivo antes de gravá-lo em disco (Seção 2.4 do TCC)
    private String validarArquivoDeImagem(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum arquivo foi enviado");
        }

        String contentType = file.getContentType();
        if ("image/jpeg".equals(contentType)) {
            return ".jpg";
        }
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Formato de arquivo não suportado. Envie uma imagem JPEG ou PNG");
    }
}