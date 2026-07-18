# Meet & Beat — Módulo de Matching Social

Módulo funcional demonstrativo do Trabalho de Conclusão de Curso **Meet & Beat: Uma Plataforma Digital Para Conexão de Fãs e Apoio à Experiência em Eventos Musicais**, desenvolvido no curso de Sistemas de Informação da Universidade do Estado de Minas Gerais.

Este repositório contempla a implementação do módulo de **matching social por afinidade musical**, responsável por sugerir conexões entre usuários com interesses musicais em comum e registrar curtidas mútuas (matches).

## Sobre o projeto

O Meet & Beat é uma plataforma voltada a amantes de música e eventos, que integra conexão social, caronas colaborativas e revenda segura de ingressos. Este módulo específico implementa e demonstra a viabilidade técnica do sistema de matching, utilizando um banco de dados orientado a grafos para modelar as relações de afinidade entre usuários e seus interesses musicais.

## Arquitetura

O projeto é dividido em dois componentes independentes:

```
TCC/
├── MeetBeat/                      → Backend (Spring Boot + Neo4j)
└── MeetBeatApp/                   → Aplicativo Android (Java)
```

### Backend — `MeetBeat/`

Responsável por armazenar o grafo de usuários e interesses e expor a API REST consumida pelo aplicativo.

**Tecnologias:**
- Java 21
- Spring Boot
- Spring Data Neo4j
- Neo4j (banco de dados orientado a grafos)
- Maven

**Modelagem do grafo:**

```
(:Pessoa {nome})
(:Genero {nome})

(Pessoa)-[:GOSTA_DE]->(Genero)
(Pessoa)-[:CURTIU]->(Pessoa)
```

### Aplicativo — `MeetBeatApp/`

Aplicativo Android nativo que consome a API do backend e exibe as sugestões de match em formato de cards, com opção de curtir ou recusar cada sugestão.

**Tecnologias:**
- Java
- Android Studio (Views/XML, sem Jetpack Compose)
- Retrofit + Gson (consumo de API REST)
- RecyclerView / CardView

## Funcionalidades implementadas

- Listagem de todos os usuários cadastrados
- Sugestão de matches por afinidade musical, com cálculo de compatibilidade via consulta em grafo (Cypher)
- Exibição dos gêneros/fandoms em comum entre dois usuários
- Registro de curtidas (swipe) entre usuários
- Detecção automática de match mútuo
- Interface Android navegável, consumindo a API em tempo real

## Como executar o projeto

### Pré-requisitos

- [Neo4j Desktop](https://neo4j.com/download/) instalado
- JDK 21 ou superior
- Android Studio (versão recente, com SDK 34 instalado)
- Um emulador Android configurado (ou dispositivo físico)

### 1. Subir o banco de dados Neo4j

1. Abra o Neo4j Desktop
2. Inicie a instância do projeto (verifique se o status está **RUNNING**)
3. Confirme a URI de conexão: `neo4j://127.0.0.1:7687`

> ⚠️ O backend não conecta automaticamente caso o Neo4j esteja parado. Sempre inicie o banco **antes** de rodar o backend.

### 2. Configurar e rodar o backend

1. Acesse a pasta `matching/`
2. Configure as credenciais do banco em `src/main/resources/application.properties`:

   ```properties
   spring.neo4j.uri=neo4j://127.0.0.1:7687
   spring.neo4j.authentication.username=neo4j
   spring.neo4j.authentication.password=SUA_SENHA_AQUI
   ```

3. Execute a classe `MatchingApplication.java` (pelo VS Code, IntelliJ, ou `mvn spring-boot:run`)
4. Confirme que o console exibe `Started MatchingApplication...` sem erros
5. Teste no navegador: `http://localhost:8080/usuarios`

### 3. Popular o banco com dados de exemplo

No Neo4j Browser (acessível pelo botão **Connect > Query** no Neo4j Desktop), execute o script de carga localizado em `matching/scripts/seed-data.cypher` (ou o script fornecido na documentação do projeto) para criar usuários, gêneros e relações de exemplo.

### 4. Rodar o aplicativo Android

1. Abra a pasta `app/` no Android Studio
2. Aguarde a sincronização do Gradle
3. Certifique-se de que o backend está rodando **antes** de iniciar o app
4. Execute o app em um emulador Android

> ⚠️ Em emuladores, o endereço `localhost` do seu computador é acessado através de `10.0.2.2`. Essa configuração já está definida em `RetrofitClient.java`.

## Endpoints da API

| Método | Endpoint                              | Descrição                                              |
|--------|----------------------------------------|---------------------------------------------------------|
| GET    | `/usuarios`                            | Lista todos os usuários cadastrados                     |
| GET    | `/usuarios/{id}/sugestoes`             | Retorna sugestões de match ordenadas por compatibilidade |
| POST   | `/usuarios/{id}/curtir/{targetId}`     | Registra uma curtida; retorna se houve match mútuo       |

### Exemplo de resposta — `GET /usuarios/{id}/sugestoes`

```json
[
  {
    "id": 6,
    "nome": "Namjoon",
    "score": 3,
    "generosComuns": ["ARMY (BTS)", "K-pop", "Hip-Hop"]
  }
]
```

### Exemplo de resposta — `POST /usuarios/{id}/curtir/{targetId}`

```json
{
  "match": true,
  "mensagem": "É um match! 🎉"
}
```

## Estrutura de pastas

### Backend

```
matching/src/main/java/com/meetbeat/matching/
├── MatchingApplication.java
├── model/
│   ├── UserNode.java
│   └── MusicInterest.java
├── repository/
│   └── UserRepository.java
├── controller/
│   └── UserController.java
└── dto/
    ├── MatchSuggestionDTO.java
    └── SwipeResponseDTO.java
```

### Android

```
app/src/main/java/com/meetbeat/app/
├── MainActivity.java
├── model/
│   ├── MatchSuggestion.java
│   └── SwipeResponse.java
├── network/
│   ├── ApiService.java
│   └── RetrofitClient.java
└── adapter/
    └── MatchAdapter.java
```

## Justificativa técnica: por que Neo4j?

O problema de matching por afinidade musical é, por natureza, um problema de grafos: usuários e interesses são nós, e as relações de afinidade entre eles são arestas. Consultas como "encontrar usuários com interesses em comum, ordenados por grau de compatibilidade" são naturalmente mais expressivas e performáticas em Cypher do que em SQL relacional tradicional, especialmente à medida que a base de usuários cresce. Essa escolha compõe uma estratégia de persistência poliglota no projeto Meet & Beat, na qual o Neo4j é utilizado especificamente para o módulo de matching social.

## Status do projeto

Este é um módulo funcional demonstrativo desenvolvido para fins de avaliação acadêmica (TCC), não uma versão de produção. Funcionalidades como autenticação, cadastro completo de usuários (CRUD) e persistência de fotos estão previstas como evoluções futuras do projeto.

## Trabalhos futuros

- Tela de cadastro de usuário e seleção de interesses musicais (CRUD completo)
- Tela de listagem de matches confirmados
- Upload e exibição de fotos reais de perfil
- Autenticação de usuários
- Integração com APIs de ticketeiras e módulo de caronas colaborativas (conforme escopo geral do Meet & Beat)

## Autoria

Desenvolvido por Beatriz da Costa Lauro como parte do Trabalho de Conclusão de Curso em Sistemas de Informação — Universidade do Estado de Minas Gerais, 2026.