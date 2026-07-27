package com.meetbeat.matching.controller;

import com.meetbeat.matching.dto.GeneroDTO;
import com.meetbeat.matching.dto.MatchConfirmadoDTO;
import com.meetbeat.matching.dto.MatchSuggestionDTO;
import com.meetbeat.matching.dto.NovoUsuarioDTO;
import com.meetbeat.matching.dto.SwipeResponseDTO;
import com.meetbeat.matching.model.UserNode;
import com.meetbeat.matching.repository.UserRepository;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.nio.file.Path;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserRepository userRepository;
    private final Neo4jClient neo4jClient;

    public UserController(UserRepository userRepository, Neo4jClient neo4jClient) {
        this.userRepository = userRepository;
        this.neo4jClient = neo4jClient;
    }

    @GetMapping
    public List<UserNode> listarTodos() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}/sugestoes")
    public List<MatchSuggestionDTO> sugestoesDeMatch(@PathVariable Long id) {

        String cypher = """
            MATCH (me:Pessoa)-[:GOSTA_DE]->(g:Genero)<-[:GOSTA_DE]-(other:Pessoa)
            WHERE id(me) = $userId AND me <> other
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
    public SwipeResponseDTO curtir(@PathVariable Long id, @PathVariable Long targetId) {

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
            return new SwipeResponseDTO(true, "É um match! 🎉");
        } else {
            return new SwipeResponseDTO(false, "Curtida registrada. Aguardando o outro usuário.");
        }
    }

    @GetMapping("/{id}/matches")
    public List<MatchConfirmadoDTO> meusMatches(@PathVariable Long id) {

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

    @PostMapping("/{id}/foto")
    public String uploadFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {

        String uploadDir = "./uploads";
        Files.createDirectories(Paths.get(uploadDir));

        String nomeArquivo = "user_" + id + "_" + System.currentTimeMillis() + ".jpg";
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

    @PostMapping
public UserNode criarUsuario(@RequestBody NovoUsuarioDTO dto) {
    UserNode novoUsuario = new UserNode(dto.getNome(), dto.getBio());
    return userRepository.save(novoUsuario);
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

@PostMapping("/{id}/interesses/{generoId}")
    public void adicionarInteresse(@PathVariable Long id, @PathVariable Long generoId) {

        String cypher = """
            MATCH (p:Pessoa), (g:Genero)
            WHERE id(p) = $userId AND id(g) = $generoId
            MERGE (p)-[:GOSTA_DE]->(g)
            """;

        neo4jClient.query(cypher)
                .bind(id).to("userId")
                .bind(generoId).to("generoId")
                .run();
    }
}