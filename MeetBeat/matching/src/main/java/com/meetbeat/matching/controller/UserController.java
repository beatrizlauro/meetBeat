package com.meetbeat.matching.controller;

import com.meetbeat.matching.dto.MatchSuggestionDTO;
import com.meetbeat.matching.dto.SwipeResponseDTO;
import com.meetbeat.matching.model.UserNode;
import com.meetbeat.matching.repository.UserRepository;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
            RETURN id(other) AS userId, other.nome AS userNome, count(g) AS matchScore
            ORDER BY matchScore DESC
            """;

        Collection<MatchSuggestionDTO> resultados = neo4jClient.query(cypher)
                .bind(id).to("userId")
                .fetchAs(MatchSuggestionDTO.class)
                .mappedBy((typeSystem, record) -> new MatchSuggestionDTO(
                        record.get("userId").asLong(),
                        record.get("userNome").asString(),
                        record.get("matchScore").asLong()
                ))
                .all();

        return new ArrayList<>(resultados);
    }

    @PostMapping("/{id}/curtir/{targetId}")
    public SwipeResponseDTO curtir(@PathVariable Long id, @PathVariable Long targetId) {

        // 1. Registra a curtida de "id" para "targetId"
        String criarCurtida = """
            MATCH (me:Pessoa), (other:Pessoa)
            WHERE id(me) = $meId AND id(other) = $otherId
            MERGE (me)-[:CURTIU]->(other)
            """;

        neo4jClient.query(criarCurtida)
                .bind(id).to("meId")
                .bind(targetId).to("otherId")
                .run();

        // 2. Verifica se já existe curtida no sentido inverso (match mútuo)
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
}