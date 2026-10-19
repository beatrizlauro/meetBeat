package com.meetbeat.matching.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cria, ao iniciar a API, as restrições de unicidade do banco. Sem elas, a verificação "o nome de
 * usuário já existe?" feita antes de cadastrar não basta: duas requisições simultâneas (por exemplo,
 * toques repetidos no botão de cadastro) passavam juntas pela verificação e criavam contas duplicadas.
 * Com a restrição, o próprio Neo4j recusa a segunda gravação.
 */
@Component
public class RestricoesBanco implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RestricoesBanco.class);

    private static final List<String> RESTRICOES = List.of(
            "CREATE CONSTRAINT pessoa_username_unico IF NOT EXISTS FOR (p:Pessoa) REQUIRE p.username IS UNIQUE",
            "CREATE CONSTRAINT pessoa_email_unico IF NOT EXISTS FOR (p:Pessoa) REQUIRE p.email IS UNIQUE",
            "CREATE CONSTRAINT pessoa_google_id_unico IF NOT EXISTS FOR (p:Pessoa) REQUIRE p.googleId IS UNIQUE",
            "CREATE CONSTRAINT genero_nome_unico IF NOT EXISTS FOR (g:Genero) REQUIRE g.nome IS UNIQUE"
    );

    private final Neo4jClient neo4jClient;

    public RestricoesBanco(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String restricao : RESTRICOES) {
            try {
                neo4jClient.query(restricao).run();
            } catch (Exception e) {
                // Acontece se o banco já tiver dados duplicados: a API continua funcionando, mas
                // os duplicados precisam ser resolvidos para a restrição passar a valer
                log.warn("Não foi possível criar a restrição [{}]: {}", restricao, e.getMessage());
            }
        }
    }
}
