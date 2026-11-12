# Publicar o Meet & Beat na nuvem

Objetivo: usar o app no celular sem abrir VS Code, Neo4j Desktop ou Android Studio.

- **Banco:** Neo4j AuraDB Free (gratuito)
- **API Spring Boot:** Render, plano Free (gratuito, roda via Docker)
- **E-mail de recuperação de senha:** Brevo (gratuito, 300 e-mails/dia). O Render gratuito bloqueia SMTP, então o envio vai pela API HTTPS da Brevo.
- **App:** APK `nuvem`, instalado uma vez no celular

Tempo estimado: uns 30 a 40 minutos na primeira vez.

---

## 1. Criar o banco no Neo4j Aura

1. Acesse https://console.neo4j.io e crie uma conta.
2. Clique em **New Instance > AuraDB Free**.
3. Na tela que aparece, **baixe o arquivo de credenciais** (`.txt`). A senha só é mostrada uma vez.
4. Espere o status ficar **Running** (uns 2 a 5 minutos).
5. Anote do arquivo:
   - `NEO4J_URI` (algo como `neo4j+s://abcd1234.databases.neo4j.io`)
   - `NEO4J_USERNAME` (normalmente `neo4j`, em instâncias novas pode ser o ID da instância)
   - `NEO4J_PASSWORD`
   - `NEO4J_DATABASE` (em instâncias novas é o ID da instância, não `neo4j`)

> A instância gratuita é pausada após 3 dias sem uso. Se isso acontecer, entre no console e clique em **Resume** antes de usar o app.

## 2. Popular o banco da nuvem com a base fictícia

No Git Bash, dentro de `MeetBeat/matching`, com os valores do passo 1:

```bash
export NEO4J_URI="neo4j+s://SEU_ID.databases.neo4j.io"
export NEO4J_USERNAME="neo4j"
export NEO4J_PASSWORD="SUA_SENHA_DO_AURA"
export NEO4J_DATABASE="SEU_ID"
python seed/popular_banco.py --confirmar
```

O script mostra `Conectando ao Neo4j em https://...` e cria os 120 usuários fictícios (senha `meetbeat123`, conta de demonstração `marina.costa`). As imagens em `uploads/seed/` são regeneradas iguais (a geração é determinística).

> Sem essas variáveis, o script continua usando o Neo4j local, como antes.

## 3. Configurar o envio de e-mail (Brevo)

1. Crie uma conta em https://www.brevo.com.
2. Em **Senders, Domains & Dedicated IPs > Senders**, adicione e confirme o e-mail remetente (o mesmo Gmail que você já usa no `spring.mail.username`).
3. Em **SMTP & API > API Keys**, gere uma chave e copie.

Se pular este passo, todo o resto funciona, só o "Esqueci minha senha" não envia o código.

## 4. Enviar o código para o GitHub

O Render publica a partir do repositório `beatrizlauro/meetBeat`. Faça commit e push das alterações (incluindo a pasta `MeetBeat/matching/uploads/seed/`, que agora é versionada porque a imagem Docker precisa das fotos fictícias).

## 5. Publicar a API no Render

1. Acesse https://render.com e entre com a conta do GitHub.
2. **New > Web Service**, escolha o repositório `meetBeat`.
3. Preencha:
   - **Name:** `meetbeat-api` (o nome vira a URL; se já existir, o Render sugere outro)
   - **Root Directory:** `MeetBeat/matching`
   - **Runtime / Language:** Docker
   - **Instance Type:** Free
4. Em **Environment Variables**, adicione:

   | Variável | Valor |
   |---|---|
   | `SPRING_NEO4J_URI` | o `NEO4J_URI` do Aura |
   | `SPRING_NEO4J_AUTHENTICATION_USERNAME` | o `NEO4J_USERNAME` do Aura |
   | `SPRING_NEO4J_AUTHENTICATION_PASSWORD` | o `NEO4J_PASSWORD` do Aura |
   | `SPRING_DATA_NEO4J_DATABASE` | o `NEO4J_DATABASE` do Aura |
   | `JWT_SECRET` | o mesmo `jwt.secret` do seu `application-secrets.properties` (ou um novo: `openssl rand -base64 32`) |
   | `SPRING_MAIL_USERNAME` | o e-mail remetente confirmado na Brevo |
   | `BREVO_API_KEY` | a chave da Brevo |

5. Em **Advanced > Health Check Path**, coloque `/actuator/health`.
6. Clique em **Deploy**. O primeiro build leva uns 5 a 10 minutos. Quando aparecer `Started MatchingApplication`, copie a URL do serviço (ex.: `https://meetbeat-api.onrender.com`).
7. Teste no navegador: `https://SUA-URL.onrender.com/actuator/health` deve responder `{"status":"UP"}`.

> **Plano gratuito:** o serviço "dorme" após 15 minutos sem uso. A primeira abertura do app depois disso demora uns 50 segundos. No dia da apresentação, abra a URL de health no navegador uns minutos antes para acordar o servidor.
>
> **Fotos enviadas pelo app** ficam no disco do servidor, que é apagado a cada novo deploy ou reinício. As fotos da base fictícia ficam sempre disponíveis (estão dentro da imagem). Para a demonstração isso basta; para guardar fotos de verdade, seria preciso um disco pago no Render ou um armazenamento como Cloudinary/S3.

## 6. Gerar e instalar o APK

1. Em `MeetBeatApp/gradle.properties`, troque `meetbeat.cloudUrl` pela URL do Render, **terminando com `/`**:

   ```properties
   meetbeat.cloudUrl=https://SUA-URL.onrender.com/
   ```

2. No Git Bash, dentro de `MeetBeatApp`:

   ```bash
   JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew.bat assembleNuvem
   ```

3. O APK fica em `MeetBeatApp/app/build/outputs/apk/nuvem/app-nuvem.apk`.
4. Passe o arquivo para o celular (cabo, Google Drive, WhatsApp para você mesma etc.), abra e permita "instalar apps de fontes desconhecidas" quando o Android pedir.
   - Com o celular no cabo, também dá para instalar direto: `adb install -r app/build/outputs/apk/nuvem/app-nuvem.apk`

O APK `nuvem` usa a mesma assinatura do debug, então o login com Google continua funcionando. Ele substitui a versão instalada pelo Android Studio (é o mesmo app).

## Desenvolvimento local continua igual

O build `debug` (o que o Android Studio roda) ainda aponta para `http://127.0.0.1:8080/` com `adb reverse`, e a API local continua lendo `application-secrets.properties`.
