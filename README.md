# Meet & Beat — Módulo de Matching Social

Módulo funcional demonstrativo do Trabalho de Conclusão de Curso **Meet & Beat: Uma Plataforma Digital Para Conexão de Fãs e Apoio à Experiência em Eventos Musicais**, desenvolvido no curso de Sistemas de Informação da Universidade do Estado de Minas Gerais.

Este repositório contempla a implementação do módulo de **matching social por afinidade musical**, responsável por sugerir conexões entre usuários com interesses musicais em comum, registrar curtidas mútuas (matches), e permitir o cadastro completo de novos usuários com upload de fotos reais de shows.

## Sobre o projeto

O Meet & Beat é uma plataforma voltada a amantes de música e eventos, que integra conexão social, caronas colaborativas e revenda segura de ingressos. Este módulo específico implementa e demonstra a viabilidade técnica do sistema de matching, utilizando um banco de dados orientado a grafos para modelar as relações de afinidade entre usuários e seus interesses musicais.

## Arquitetura

O projeto é dividido em dois componentes independentes:

```
meet-beat/
├── matching/              → Backend (Spring Boot + Neo4j)
└── app/                   → Aplicativo Android (Java)
```

### Backend — `matching/`

Responsável por armazenar o grafo de usuários e interesses, gerenciar upload de fotos, e expor a API REST consumida pelo aplicativo.

**Tecnologias:**
- Java 21
- Spring Boot
- Spring Data Neo4j
- Neo4j (banco de dados orientado a grafos)
- Maven

**Modelagem do grafo:**

```
(:Pessoa {nome, bio, fotoShow})
(:Genero {nome})

(Pessoa)-[:GOSTA_DE]->(Genero)
(Pessoa)-[:CURTIU]->(Pessoa)
```

O campo `fotoShow` armazena tanto referências a recursos locais do app (para os usuários de exemplo pré-cadastrados, ex.: `"foto_namjoon"`) quanto URLs de arquivos enviados via upload (ex.: `"/uploads/user_17_1785109747793.jpg"`), permitindo a coexistência dos dois formatos.

### Aplicativo — `app/`

Aplicativo Android nativo que consome a API do backend, exibe as sugestões de match em formato de cards, permite curtir/recusar sugestões, visualizar matches confirmados, e cadastrar novos usuários com foto real.

**Tecnologias:**
- Java
- Android Studio (Views/XML, sem Jetpack Compose)
- Retrofit + Gson (consumo de API REST)
- Glide (carregamento de imagens via URL)
- RecyclerView / CardView
- SharedPreferences (persistência do usuário logado)

## Funcionalidades implementadas

- [x] Listagem de todos os usuários cadastrados
- [x] Sugestão de matches por afinidade musical, com cálculo de compatibilidade via consulta em grafo (Cypher)
- [x] Exibição dos gêneros/fandoms em comum entre dois usuários
- [x] Registro de curtidas (swipe) entre usuários
- [x] Detecção automática de match mútuo
- [x] Tela de matches confirmados
- [x] Tela de detalhe do match (foto ampliada + gêneros em comum)
- [x] Botão de mensagem simulado (funcionalidade futura sinalizada na interface)
- [x] Cadastro completo de novo usuário (nome, bio, gêneros de interesse)
- [x] Upload real de foto de show, armazenada no servidor e servida via URL
- [x] Seletor de usuário logado, com persistência local entre sessões
- [x] Interface Android navegável, consumindo a API em tempo real

## Como executar o projeto

### Pré-requisitos

- [Neo4j Desktop](https://neo4j.com/download/) instalado
- JDK 21 ou superior
- Android Studio (versão recente, com SDK 34 instalado)
- Um emulador Android configurado (ou dispositivo físico na mesma rede Wi-Fi do computador)

### 1. Subir o banco de dados Neo4j

1. Abra o Neo4j Desktop
2. Inicie a instância do projeto (verifique se o status está **RUNNING**)
3. Confirme a URI de conexão: `neo4j://127.0.0.1:7687`

> ⚠️ O backend não conecta automaticamente caso o Neo4j esteja parado, nem se reconecta sozinho após o Neo4j cair e voltar. Sempre confira se o Neo4j está rodando **antes** de iniciar o backend, e reinicie o backend caso o Neo4j tenha caído em algum momento.

### 2. Configurar e rodar o backend

1. Acesse a pasta `matching/`
2. Configure as credenciais do banco em `src/main/resources/application.properties`:

   ```properties
   spring.neo4j.uri=neo4j://127.0.0.1:7687
   spring.neo4j.authentication.username=neo4j
   spring.neo4j.authentication.password=SUA_SENHA_AQUI

   file.upload-dir=./uploads
   spring.servlet.multipart.max-file-size=10MB
   spring.servlet.multipart.max-request-size=10MB
   ```

3. Execute a classe `MatchingApplication.java` (pelo VS Code, IntelliJ, ou `mvn spring-boot:run`)
4. Confirme que o console exibe `Started MatchingApplication...` sem erros
5. Teste no navegador: `http://localhost:8080/usuarios`

### 3. Popular o banco com dados de exemplo

No Neo4j Browser (acessível pelo botão **Connect > Query** no Neo4j Desktop), crie usuários e gêneros de exemplo com comandos Cypher, associando interesses musicais entre eles para testar o motor de sugestões.

### 4. Rodar o aplicativo Android

1. Abra a pasta `app/` no Android Studio
2. Aguarde a sincronização do Gradle
3. Certifique-se de que o backend está rodando **antes** de iniciar o app
4. Execute o app em um emulador Android ou dispositivo físico

> ⚠️ Em **emuladores**, o endereço `localhost` do computador é acessado através de `10.0.2.2`. Em **dispositivos físicos**, é necessário usar o IP real do computador na rede Wi-Fi (ex.: `192.168.0.X`), com o celular e o computador conectados à mesma rede. Essa configuração está em `RetrofitClient.java` e no arquivo `network_security_config.xml`.

## Endpoints da API

| Método | Endpoint                              | Descrição                                                  |
|--------|----------------------------------------|--------------------------------------------------------------|
| GET    | `/usuarios`                            | Lista todos os usuários cadastrados                          |
| POST   | `/usuarios`                            | Cria um novo usuário (nome, bio)                              |
| GET    | `/usuarios/{id}/sugestoes`             | Retorna sugestões de match ordenadas por compatibilidade      |
| POST   | `/usuarios/{id}/curtir/{targetId}`     | Registra uma curtida; retorna se houve match mútuo            |
| GET    | `/usuarios/{id}/matches`               | Lista os matches confirmados (curtida mútua) de um usuário    |
| GET    | `/usuarios/generos`                    | Lista todos os gêneros/fandoms disponíveis                    |
| POST   | `/usuarios/{id}/interesses/{generoId}` | Associa um gênero de interesse a um usuário                   |
| POST   | `/usuarios/{id}/foto`                  | Recebe upload de arquivo de imagem e associa ao usuário        |

Arquivos enviados via upload ficam acessíveis publicamente em `/uploads/{nome-do-arquivo}`.

### Exemplo de resposta — `GET /usuarios/{id}/sugestoes`

```json
[
  {
    "id": 6,
    "nome": "Namjoon",
    "score": 3,
    "generosComuns": ["ARMY (BTS)", "K-pop", "Hip-Hop"],
    "fotoShow": "foto_namjoon"
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

### Exemplo de resposta — `GET /usuarios/{id}/matches`

```json
[
  {
    "id": 6,
    "nome": "Namjoon",
    "fotoShow": "foto_namjoon",
    "generosComuns": ["K-pop", "ARMY (BTS)"]
  }
]
```

### Exemplo de resposta — `POST /usuarios`

```json
{
  "id": 17,
  "nome": "Arirang",
  "bio": "Fã de shows e festivais",
  "fotoShow": null,
  "generos": []
}
```

### Exemplo de resposta — `POST /usuarios/{id}/foto`

```
/uploads/user_17_1785109747793.jpg
```

## Estrutura de pastas

### Backend

```
matching/src/main/java/com/meetbeat/matching/
├── MatchingApplication.java
├── config/
│   └── WebConfig.java              (expõe a pasta de uploads como recurso estático)
├── model/
│   ├── UserNode.java
│   └── MusicInterest.java
├── repository/
│   └── UserRepository.java
├── controller/
│   └── UserController.java
└── dto/
    ├── MatchSuggestionDTO.java
    ├── MatchConfirmadoDTO.java
    ├── SwipeResponseDTO.java
    ├── NovoUsuarioDTO.java
    └── GeneroDTO.java
```

### Android

```
app/src/main/java/com/meetbeat/app/
├── MainActivity.java
├── MatchesActivity.java
├── MatchDetalheActivity.java
├── CadastroActivity.java
├── model/
│   ├── MatchSuggestion.java
│   ├── SwipeResponse.java
│   ├── MatchConfirmado.java
│   ├── NovoUsuario.java
│   ├── Genero.java
│   └── UsuarioSimples.java
├── network/
│   ├── ApiService.java
│   └── RetrofitClient.java
├── adapter/
│   ├── MatchAdapter.java
│   └── MatchConfirmadoAdapter.java
└── util/
    ├── ImageLoader.java             (decide entre carregar foto local ou via URL/Glide)
    └── UserSession.java             (persiste o usuário logado entre sessões)
```

## Fluxo de cadastro de usuário

1. O app coleta nome, bio, gêneros de interesse selecionados e uma foto da galeria do dispositivo
2. `POST /usuarios` cria o registro do usuário e retorna o `id` gerado
3. Para cada gênero selecionado, o app chama `POST /usuarios/{id}/interesses/{generoId}`
4. Se uma foto foi selecionada, o app envia o arquivo via `POST /usuarios/{id}/foto` (multipart/form-data); o backend salva o arquivo em disco e atualiza o campo `fotoShow` do usuário com a URL pública gerada
5. O usuário recém-criado passa a aparecer normalmente nas sugestões de match de outras pessoas

## Justificativa técnica: por que Neo4j?

O problema de matching por afinidade musical é, por natureza, um problema de grafos: usuários e interesses são nós, e as relações de afinidade entre eles são arestas. Consultas como "encontrar usuários com interesses em comum, ordenados por grau de compatibilidade" são naturalmente mais expressivas e performáticas em Cypher do que em SQL relacional tradicional, especialmente à medida que a base de usuários cresce. Essa escolha compõe uma estratégia de persistência poliglota no projeto Meet & Beat, na qual o Neo4j é utilizado especificamente para o módulo de matching social.

## Status do projeto

Este é um módulo funcional demonstrativo desenvolvido para fins de avaliação acadêmica (TCC), não uma versão de produção. A seleção do "usuário logado" atualmente é feita por um seletor simples (sem autenticação), adequada para fins de demonstração.

## Trabalhos futuros

- Tela de login com autenticação real (usuário e senha)
- Edição e exclusão de perfil (completar o CRUD)
- Upload de múltiplas fotos por usuário
- Implementação real do sistema de mensagens entre matches (atualmente simulado na interface)
- Autenticação e controle de sessão de usuários
- Integração com APIs de ticketeiras e módulo de caronas colaborativas (conforme escopo geral do Meet & Beat)

## Autoria

Desenvolvido por Beatriz da Costa Lauro como parte do Trabalho de Conclusão de Curso em Sistemas de Informação — Universidade do Estado de Minas Gerais, 2026.
