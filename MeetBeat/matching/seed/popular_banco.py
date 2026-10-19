"""
Popula o banco Neo4j do módulo de matching com dados fictícios para testes e apresentação.

O que o script faz, nesta ordem:
  1. Salva um backup de TODO o conteúdo atual do banco em seed/backups/ (JSON).
  2. Gera as imagens fictícias (avatares com iniciais e fotos de show estilizadas)
     em uploads/seed/, servidas pela API em /uploads/seed/...
  3. Apaga todos os nós e relações do banco.
  4. Cria os gêneros musicais, os usuários fictícios e as relações GOSTA_DE, CURTIU e REJEITOU.

Uso (com o Neo4j ligado; a API Spring não precisa estar rodando):
    python seed/popular_banco.py              -> pede confirmação antes de apagar
    python seed/popular_banco.py --confirmar  -> apaga e popula sem perguntar

Todos os usuários fictícios usam a mesma senha de demonstração: meetbeat123
Conta de demonstração com matches, curtidas recebidas e enviadas já prontas: marina.costa

A geração é determinística (semente fixa): rodar de novo produz exatamente a mesma base,
o que permite reproduzir os números citados no TCC.
"""

import base64
import json
import math
import random
import shutil
import sys
import unicodedata
import urllib.error
import urllib.request
from datetime import datetime
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

SEMENTE = 2026
TOTAL_USUARIOS = 120

# BCrypt de "meetbeat123", gerado com o mesmo BCryptPasswordEncoder usado pela API
SENHA_HASH_DEMO = "$2a$10$D9sPrgPyOKhIMRbzwWP9t.b7Tz2l6roq8z4QxOhkg.qzdDMzjgZfW"

RAIZ = Path(__file__).resolve().parent.parent          # MeetBeat/matching
PASTA_IMAGENS = RAIZ / "uploads" / "seed"
PASTA_BACKUPS = Path(__file__).resolve().parent / "backups"
ARQUIVO_SEGREDOS = RAIZ / "src" / "main" / "resources" / "application-secrets.properties"
ARQUIVO_PROPRIEDADES = RAIZ / "src" / "main" / "resources" / "application.properties"

GENEROS = [
    "Pop", "K-pop", "J-pop", "R&B", "Soul", "Hip-Hop", "Rap", "Trap", "Funk", "Reggaeton",
    "Rock", "Rock Alternativo", "Indie", "Grunge", "Punk", "Metal", "Hard Rock", "Emo",
    "Lo-fi", "Folk", "Sertanejo", "Forró", "Piseiro", "Arrocha", "Pagode", "Samba", "MPB",
    "Bossa Nova", "Axé", "Brega", "Reggae", "Jazz", "Blues", "Gospel", "Clássica",
    "Country", "Eletrônica", "House", "Techno", "Drum and Bass",
]

# Cada perfil musical reúne gêneros que costumam andar juntos, artistas e músicas favoritas,
# trechos de biografia e a paleta de cores usada nas fotos de show geradas para ele.
PERFIS = {
    "kpop": {
        "peso": 14,
        "generos": ["K-pop", "Pop", "J-pop", "R&B", "Hip-Hop"],
        "favoritos": [("BTS", "Spring Day"), ("BLACKPINK", "Pink Venom"), ("Stray Kids", "S-Class"),
                      ("NewJeans", "Super Shy"), ("TWICE", "Fancy"), ("SEVENTEEN", "Very Nice")],
        "bios": ["Lightstick sempre carregado e pronto para o próximo show.",
                 "Coleciono photocards e setlists de turnê.",
                 "Fã de K-pop desde a primeira geração que chegou ao Brasil.",
                 "Já acampei na fila do show e faria tudo de novo."],
        "paleta": [(255, 92, 168), (170, 90, 255), (90, 200, 255)],
    },
    "pop": {
        "peso": 14,
        "generos": ["Pop", "R&B", "Indie", "Eletrônica", "Soul"],
        "favoritos": [("Taylor Swift", "Cruel Summer"), ("Dua Lipa", "Levitating"),
                      ("Olivia Rodrigo", "vampire"), ("The Weeknd", "Blinding Lights"),
                      ("Sabrina Carpenter", "Espresso"), ("Lady Gaga", "Bad Romance")],
        "bios": ["Sei todas as coreografias e canto até o último refrão.",
                 "Playlist de pop para cada humor do dia.",
                 "Viajo de cidade em cidade atrás de turnê internacional.",
                 "Pulseira da amizade feita para trocar no próximo show."],
        "paleta": [(255, 120, 200), (255, 200, 90), (140, 120, 255)],
    },
    "sertanejo": {
        "peso": 12,
        "generos": ["Sertanejo", "Forró", "Piseiro", "Arrocha", "Pagode", "Brega"],
        "favoritos": [("Marília Mendonça", "Infiel"), ("Jorge & Mateus", "Propaganda"),
                      ("Henrique & Juliano", "Arranhão"), ("Ana Castela", "Pipoco"),
                      ("Wesley Safadão", "Ar Condicionado no 15"), ("Maiara & Maraisa", "Medo Bobo")],
        "bios": ["Rodeio, festa de peão e modão até o sol nascer.",
                 "Se tem sanfona, eu estou dentro.",
                 "Agenda de shows lotada nos fins de semana.",
                 "Sofrência boa se canta em grupo."],
        "paleta": [(255, 160, 60), (255, 90, 60), (255, 220, 120)],
    },
    "urbano": {
        "peso": 12,
        "generos": ["Funk", "Trap", "Rap", "Hip-Hop", "Reggaeton"],
        "favoritos": [("Anitta", "Envolver"), ("Matuê", "Máquina do Tempo"),
                      ("Racionais MC's", "Vida Loka, Parte II"), ("Ludmilla", "Maldivas"),
                      ("Bad Bunny", "Tití Me Preguntó"), ("Veigh", "Novo Balanço")],
        "bios": ["Baile, batalha de rima e festival de trap no calendário.",
                 "Grave no talo e fone sempre no bolso.",
                 "Conheço cada verso dos clássicos do rap nacional.",
                 "Se tocar funk, a pista é minha."],
        "paleta": [(80, 255, 170), (255, 60, 120), (120, 90, 255)],
    },
    "mpb": {
        "peso": 10,
        "generos": ["MPB", "Samba", "Bossa Nova", "Pagode", "Jazz"],
        "favoritos": [("Caetano Veloso", "Sozinho"), ("Gilberto Gil", "Andar com Fé"),
                      ("Marisa Monte", "Ainda Bem"), ("Djavan", "Oceano"),
                      ("Elis Regina", "Como Nossos Pais"), ("Zeca Pagodinho", "Deixa a Vida Me Levar")],
        "bios": ["Roda de samba no domingo é sagrada.",
                 "Coleção de vinis de MPB herdada da família.",
                 "Show intimista, voz e violão, é o meu lugar preferido.",
                 "Carnaval de rua e samba de raiz o ano inteiro."],
        "paleta": [(255, 190, 90), (230, 110, 70), (120, 200, 160)],
    },
    "rock": {
        "peso": 10,
        "generos": ["Rock", "Rock Alternativo", "Indie", "Grunge", "Punk"],
        "favoritos": [("Arctic Monkeys", "Do I Wanna Know?"), ("Foo Fighters", "Everlong"),
                      ("Nirvana", "Smells Like Teen Spirit"), ("Pitty", "Me Adora"),
                      ("Legião Urbana", "Tempo Perdido"), ("Paramore", "Misery Business")],
        "bios": ["Camiseta de banda e tênis surrado, pronto para a roda.",
                 "Guitarra distorcida é terapia.",
                 "Festival de rock com chuva e lama é o melhor tipo.",
                 "Toco baixo numa banda de garagem nas horas vagas."],
        "paleta": [(255, 60, 60), (255, 150, 50), (230, 230, 255)],
    },
    "eletronica": {
        "peso": 8,
        "generos": ["Eletrônica", "House", "Techno", "Drum and Bass", "Pop"],
        "favoritos": [("Alok", "Hear Me Now"), ("Vintage Culture", "Free"),
                      ("Calvin Harris", "Summer"), ("David Guetta", "Titanium"),
                      ("Daft Punk", "One More Time"), ("Fred again..", "Delilah (pull me out of this)")],
        "bios": ["Pista até o amanhecer e óculos escuros no after.",
                 "Festival de eletrônica é minha viagem favorita.",
                 "Coleciono pulseiras de festival desde 2015.",
                 "Bpm alto e luz estroboscópica, só assim."],
        "paleta": [(0, 230, 255), (255, 0, 200), (120, 80, 255)],
    },
    "indie": {
        "peso": 8,
        "generos": ["Indie", "Lo-fi", "Folk", "Rock Alternativo", "Emo"],
        "favoritos": [("Tame Impala", "The Less I Know the Better"), ("Lana Del Rey", "Summertime Sadness"),
                      ("Boogarins", "Lucifernandis"), ("Lagum", "Deixa"),
                      ("Phoebe Bridgers", "Motion Sickness"), ("Terno Rei", "Dia Lindo")],
        "bios": ["Sempre descobrindo bandas novas antes de todo mundo.",
                 "Show pequeno em casa de show pequena é o que eu mais gosto.",
                 "Playlists lo-fi para estudar e indie para viver.",
                 "Câmera analógica na bolsa em todo show."],
        "paleta": [(255, 170, 120), (150, 120, 255), (90, 210, 200)],
    },
    "soul": {
        "peso": 6,
        "generos": ["R&B", "Soul", "Blues", "Jazz", "Gospel"],
        "favoritos": [("Beyoncé", "Halo"), ("Tim Maia", "Gostava Tanto de Você"),
                      ("Amy Winehouse", "Back to Black"), ("SZA", "Kill Bill"),
                      ("Iza", "Pesadão"), ("Stevie Wonder", "Isn't She Lovely")],
        "bios": ["Voz potente ao vivo me arrepia todas as vezes.",
                 "Black music das antigas e das novas.",
                 "Jazz bar na sexta e show de soul no sábado.",
                 "Canto no coral e vivo de música ao vivo."],
        "paleta": [(255, 180, 60), (200, 80, 160), (255, 120, 80)],
    },
    "metal": {
        "peso": 4,
        "generos": ["Metal", "Hard Rock", "Rock", "Punk"],
        "favoritos": [("Metallica", "Master of Puppets"), ("Sepultura", "Roots Bloody Roots"),
                      ("Iron Maiden", "The Trooper"), ("System of a Down", "Chop Suey!"),
                      ("Angra", "Carry On")],
        "bios": ["Headbanging garantido em qualquer show.",
                 "Mosh pit é onde faço amizades.",
                 "Já perdi a conta de quantos shows de metal fui.",
                 "Peso, velocidade e solo de guitarra."],
        "paleta": [(255, 40, 30), (255, 120, 20), (200, 200, 200)],
    },
    "reggae": {
        "peso": 4,
        "generos": ["Reggae", "Axé", "Samba", "MPB"],
        "favoritos": [("Bob Marley", "Is This Love"), ("Natiruts", "Quero Ser Feliz Também"),
                      ("Ivete Sangalo", "Sorte Grande"), ("Armandinho", "Sol Loiro"),
                      ("Daniela Mercury", "O Canto da Cidade")],
        "bios": ["Show na praia com pé na areia é perfeito.",
                 "Trio elétrico no carnaval e reggae no resto do ano.",
                 "Good vibes e muita música boa.",
                 "Festival de verão é minha estação favorita."],
        "paleta": [(255, 210, 60), (60, 200, 90), (255, 90, 60)],
    },
}

FRASES_FINAIS = [
    "Procurando companhia para o próximo show!",
    "Bora dividir a ida para o festival?",
    "Me chama para curtir um show junto.",
    "Quero conhecer gente com o mesmo gosto musical.",
    "Topo show em qualquer cidade.",
    "",
]

PRIMEIROS_NOMES = [
    "Ana", "Bruno", "Camila", "Diego", "Eduarda", "Felipe", "Gabriela", "Henrique", "Isabela",
    "João", "Larissa", "Lucas", "Mariana", "Mateus", "Natália", "Otávio", "Paula", "Rafael",
    "Sofia", "Thiago", "Valentina", "Vinícius", "Yasmin", "Leonardo", "Beatriz", "Gustavo",
    "Juliana", "Pedro", "Letícia", "André", "Carolina", "Caio", "Fernanda", "Daniel", "Helena",
    "Igor", "Lívia", "Murilo", "Bianca", "Renan", "Clara", "Samuel", "Alice", "Enzo", "Lorena",
    "Arthur", "Manuela", "Davi", "Luana", "Heitor", "Giovanna", "Nicolas", "Rebeca", "Miguel",
    "Lara", "Bernardo", "Cecília", "Theo", "Melissa", "Kauã", "Vitória", "Ícaro", "Elisa", "Iago",
]

SOBRENOMES = [
    "Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira", "Alves", "Pereira", "Lima",
    "Gomes", "Ribeiro", "Carvalho", "Almeida", "Lopes", "Martins", "Araújo", "Rocha", "Barbosa",
    "Cardoso", "Teixeira", "Moreira", "Mendes", "Nunes", "Freitas", "Vieira", "Monteiro",
    "Campos", "Duarte", "Pinto", "Cavalcanti", "Nogueira", "Moura", "Batista", "Farias",
    "Correia", "Castro", "Machado", "Rezende", "Andrade", "Fonseca",
]

USERNAME_DEMO = "marina.costa"

RESTRICOES = [
    "CREATE CONSTRAINT pessoa_username_unico IF NOT EXISTS FOR (p:Pessoa) REQUIRE p.username IS UNIQUE",
    "CREATE CONSTRAINT pessoa_email_unico IF NOT EXISTS FOR (p:Pessoa) REQUIRE p.email IS UNIQUE",
    "CREATE CONSTRAINT pessoa_google_id_unico IF NOT EXISTS FOR (p:Pessoa) REQUIRE p.googleId IS UNIQUE",
    "CREATE CONSTRAINT genero_nome_unico IF NOT EXISTS FOR (g:Genero) REQUIRE g.nome IS UNIQUE",
]


# ---------------------------------------------------------------------------------------
# Conexão com o Neo4j (API HTTP, sem dependências externas)
# ---------------------------------------------------------------------------------------

def ler_propriedade(arquivo, chave):
    if not arquivo.exists():
        return None
    for linha in arquivo.read_text(encoding="utf-8").splitlines():
        linha = linha.strip()
        if linha.startswith(chave + "="):
            return linha.split("=", 1)[1].strip()
    return None


class Neo4jHttp:
    def __init__(self):
        uri = ler_propriedade(ARQUIVO_PROPRIEDADES, "spring.neo4j.uri") or "neo4j://127.0.0.1:7687"
        host = uri.split("://", 1)[1].split(":", 1)[0]
        usuario = ler_propriedade(ARQUIVO_PROPRIEDADES, "spring.neo4j.authentication.username") or "neo4j"
        senha = ler_propriedade(ARQUIVO_SEGREDOS, "spring.neo4j.authentication.password")
        if not senha:
            sys.exit(f"Senha do Neo4j não encontrada em {ARQUIVO_SEGREDOS}")
        self.base = f"http://{host}:7474/db/neo4j"
        self.usar_query_api = False  # muda para a Query API se o endpoint HTTP antigo não existir
        token = base64.b64encode(f"{usuario}:{senha}".encode()).decode()
        self.cabecalhos = {"Content-Type": "application/json", "Accept": "application/json",
                           "Authorization": f"Basic {token}"}

    def _post(self, url, corpo):
        requisicao = urllib.request.Request(url, data=json.dumps(corpo).encode(), headers=self.cabecalhos,
                                            method="POST")
        try:
            with urllib.request.urlopen(requisicao, timeout=120) as resposta:
                return json.loads(resposta.read())
        except urllib.error.HTTPError as erro:
            if erro.code == 404:
                return None
            if erro.code == 401:
                sys.exit("O Neo4j recusou o usuário/senha de application-secrets.properties.")
            raise RuntimeError(erro.read().decode(errors="replace"))
        except urllib.error.URLError as erro:
            sys.exit(f"Não foi possível conectar ao Neo4j em {self.base}. Ele está ligado? ({erro})")

    def executar(self, cypher, **parametros):
        if not self.usar_query_api:
            dados = self._post(f"{self.base}/tx/commit",
                               {"statements": [{"statement": cypher, "parameters": parametros}]})
            if dados is not None:
                if dados.get("errors"):
                    raise RuntimeError(dados["errors"])
                resultado = dados["results"][0]
                return [dict(zip(resultado["columns"], linha["row"])) for linha in resultado["data"]]
            self.usar_query_api = True

        dados = self._post(f"{self.base}/query/v2", {"statement": cypher, "parameters": parametros})
        if dados is None:
            sys.exit(f"Nenhuma API HTTP do Neo4j respondeu em {self.base}.")
        if dados.get("errors"):
            raise RuntimeError(dados["errors"])
        campos = dados["data"]["fields"]
        return [dict(zip(campos, linha)) for linha in dados["data"]["values"]]


# ---------------------------------------------------------------------------------------
# Backup
# ---------------------------------------------------------------------------------------

def fazer_backup(banco):
    nos = banco.executar("MATCH (n) RETURN id(n) AS id, labels(n) AS rotulos, properties(n) AS propriedades")
    relacoes = banco.executar(
        "MATCH (a)-[r]->(b) RETURN id(r) AS id, type(r) AS tipo, id(a) AS origem, id(b) AS destino, "
        "properties(r) AS propriedades")
    PASTA_BACKUPS.mkdir(parents=True, exist_ok=True)
    arquivo = PASTA_BACKUPS / f"backup_{datetime.now():%Y%m%d_%H%M%S}.json"
    arquivo.write_text(json.dumps({"nos": nos, "relacoes": relacoes}, ensure_ascii=False, indent=1),
                       encoding="utf-8")
    print(f"Backup salvo: {arquivo} ({len(nos)} nós, {len(relacoes)} relações)")


# ---------------------------------------------------------------------------------------
# Geração das imagens
# ---------------------------------------------------------------------------------------

def carregar_fonte(tamanho):
    for caminho in ("C:/Windows/Fonts/segoeuib.ttf", "C:/Windows/Fonts/arialbd.ttf",
                    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"):
        if Path(caminho).exists():
            return ImageFont.truetype(caminho, tamanho)
    return ImageFont.load_default()


def misturar(cor_a, cor_b, t):
    return tuple(int(a + (b - a) * t) for a, b in zip(cor_a, cor_b))


def gerar_foto_show(rng, paleta, caminho, tamanho=1080):
    """Foto de show estilizada: palco com feixes de luz coloridos, bokeh e plateia em silhueta."""
    w = h = tamanho
    fundo_topo = misturar((8, 6, 20), paleta[0], 0.12)
    fundo_base = misturar((20, 10, 35), paleta[1], 0.25)
    imagem = Image.new("RGBA", (w, h))
    desenho = ImageDraw.Draw(imagem)
    for y in range(h):
        desenho.line([(0, y), (w, y)], fill=misturar(fundo_topo, fundo_base, y / h) + (255,))

    # Névoa colorida ao fundo
    nevoa = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(nevoa)
    for _ in range(5):
        cor = rng.choice(paleta)
        cx, cy, r = rng.randint(0, w), rng.randint(int(h * 0.2), int(h * 0.7)), rng.randint(200, 420)
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=cor + (70,))
    imagem = Image.alpha_composite(imagem, nevoa.filter(ImageFilter.GaussianBlur(120)))

    # Feixes de luz partindo do topo do palco
    feixes = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(feixes)
    fontes_luz = rng.randint(4, 7)
    for i in range(fontes_luz):
        x0 = int(w * (i + 0.5) / fontes_luz + rng.randint(-40, 40))
        cor = rng.choice(paleta)
        for _ in range(rng.randint(1, 2)):
            angulo = math.radians(rng.uniform(-35, 35))
            abertura = math.radians(rng.uniform(4, 9))
            comprimento = h * 1.1
            p1 = (x0 + math.sin(angulo - abertura) * comprimento, math.cos(angulo - abertura) * comprimento)
            p2 = (x0 + math.sin(angulo + abertura) * comprimento, math.cos(angulo + abertura) * comprimento)
            d.polygon([(x0, -10), p1, p2], fill=cor + (rng.randint(55, 95),))
        d.ellipse([x0 - 14, -14, x0 + 14, 14], fill=(255, 255, 255, 230))
    imagem = Image.alpha_composite(imagem, feixes.filter(ImageFilter.GaussianBlur(10)))

    # Brilho da borda do palco
    palco = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(palco)
    y_palco = int(h * rng.uniform(0.56, 0.64))
    d.rectangle([0, y_palco - 12, w, y_palco + 12], fill=misturar(paleta[0], (255, 255, 255), 0.5) + (170,))
    imagem = Image.alpha_composite(imagem, palco.filter(ImageFilter.GaussianBlur(28)))

    # Bokeh
    bokeh = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(bokeh)
    for _ in range(rng.randint(25, 45)):
        cor = misturar(rng.choice(paleta), (255, 255, 255), 0.4)
        cx, cy, r = rng.randint(0, w), rng.randint(0, int(h * 0.65)), rng.randint(6, 26)
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=cor + (rng.randint(60, 150),))
    imagem = Image.alpha_composite(imagem, bokeh.filter(ImageFilter.GaussianBlur(4)))

    # Plateia em silhueta, em duas fileiras (a da frente maior, para dar profundidade)
    plateia = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(plateia)
    cor_silhueta = (6, 4, 12, 255)
    for fileira, (escala, y_base) in enumerate([(0.7, h * 0.80), (1.0, h * 0.92)]):
        x = rng.randint(-40, 0)
        while x < w + 40:
            r = int(rng.uniform(30, 40) * escala)
            y = int(y_base + rng.uniform(-22, 22) * escala)
            # Cabeça, pescoço e ombros arredondados, que se sobrepõem aos vizinhos
            d.ellipse([x - r * 0.85, y - r, x + r * 0.85, y + r], fill=cor_silhueta)
            d.rectangle([x - r * 0.4, y + r * 0.5, x + r * 0.4, y + r * 1.4], fill=cor_silhueta)
            d.ellipse([x - r * 2.3, y + r * 1.1, x + r * 2.3, y + r * 4.2], fill=cor_silhueta)
            d.rectangle([x - r * 2.0, y + r * 2.6, x + r * 2.0, h + 50], fill=cor_silhueta)
            if rng.random() < 0.3:
                lado = rng.choice([-1, 1])
                ombro = (x + lado * r * 1.6, y + r * 2.0)
                cotovelo = (x + lado * r * rng.uniform(1.9, 2.6), y - r * rng.uniform(0.2, 0.9))
                mao = (x + lado * r * rng.uniform(1.0, 2.2), y - r * rng.uniform(2.4, 3.4))
                largura = max(10, int(r * 0.6))
                d.line([ombro, cotovelo, mao], fill=cor_silhueta, width=largura, joint="curve")
                d.ellipse([mao[0] - largura * 0.7, mao[1] - largura * 0.9, mao[0] + largura * 0.7,
                           mao[1] + largura * 0.5], fill=cor_silhueta)
                if rng.random() < 0.5:  # celular erguido com a lanterna ou a tela acesa
                    d.rounded_rectangle([mao[0] - r * 0.32, mao[1] - r * 1.0, mao[0] + r * 0.32, mao[1] - r * 0.2],
                                        radius=4, fill=(235, 235, 255, 255))
            x += int(rng.uniform(1.4, 2.0) * r * 2)
    imagem = Image.alpha_composite(imagem, plateia)

    # Vinheta
    vinheta = Image.new("L", (w, h), 0)
    ImageDraw.Draw(vinheta).ellipse([-w * 0.25, -h * 0.25, w * 1.25, h * 1.25], fill=255)
    vinheta = vinheta.filter(ImageFilter.GaussianBlur(160))
    escuro = Image.new("RGBA", (w, h), (0, 0, 0, 255))
    imagem = Image.composite(imagem, escuro, vinheta)

    imagem.convert("RGB").save(caminho, "JPEG", quality=85, optimize=True)


def gerar_avatar(nome, paleta, caminho, tamanho=512):
    """Avatar com as iniciais do nome sobre um degradê com as cores do perfil musical."""
    cor_a, cor_b = paleta[0], paleta[1]
    imagem = Image.new("RGB", (tamanho, tamanho))
    desenho = ImageDraw.Draw(imagem)
    for i in range(tamanho * 2):
        cor = misturar(cor_a, cor_b, i / (tamanho * 2))
        desenho.line([(i, 0), (0, i)], fill=misturar(cor, (40, 20, 70), 0.35))
    partes = nome.split()
    iniciais = (partes[0][0] + partes[-1][0]).upper()
    fonte = carregar_fonte(int(tamanho * 0.38))
    caixa = desenho.textbbox((0, 0), iniciais, font=fonte)
    x = (tamanho - (caixa[2] - caixa[0])) / 2 - caixa[0]
    y = (tamanho - (caixa[3] - caixa[1])) / 2 - caixa[1]
    desenho.text((x, y), iniciais, font=fonte, fill=(255, 255, 255))
    imagem.save(caminho, "JPEG", quality=88, optimize=True)


# ---------------------------------------------------------------------------------------
# Geração dos dados
# ---------------------------------------------------------------------------------------

def sem_acento(texto):
    return "".join(c for c in unicodedata.normalize("NFD", texto) if unicodedata.category(c) != "Mn")


def escolher_ponderado(rng, opcoes_com_peso):
    total = sum(p for _, p in opcoes_com_peso)
    alvo = rng.uniform(0, total)
    acumulado = 0
    for opcao, peso in opcoes_com_peso:
        acumulado += peso
        if alvo <= acumulado:
            return opcao
    return opcoes_com_peso[-1][0]


def gerar_usuarios(rng):
    nomes_usados = set()
    usuarios = []
    perfis_ponderados = [(chave, p["peso"]) for chave, p in PERFIS.items()]

    while len(usuarios) < TOTAL_USUARIOS:
        if not usuarios:
            nome, perfil = "Marina Costa", "kpop"  # conta de demonstração
        else:
            nome = f"{rng.choice(PRIMEIROS_NOMES)} {rng.choice(SOBRENOMES)}"
            perfil = escolher_ponderado(rng, perfis_ponderados)
        if nome in nomes_usados:
            continue
        nomes_usados.add(nome)

        dados_perfil = PERFIS[perfil]
        username = sem_acento(nome).lower().replace(" ", ".")

        if username == USERNAME_DEMO:
            generos = ["K-pop", "Pop", "R&B"]
        else:
            principais = dados_perfil["generos"]
            generos = [principais[0]] if rng.random() < 0.85 else []
            generos += rng.sample(principais[1:], rng.randint(1, 3))
            if rng.random() < 0.35:
                outro = PERFIS[escolher_ponderado(rng, perfis_ponderados)]
                generos += rng.sample(outro["generos"], rng.randint(1, 2))
            if rng.random() < 0.15:
                generos.append(rng.choice(GENEROS))
            generos = list(dict.fromkeys(generos))  # remove repetidos mantendo a ordem

        artista, musica = rng.choice(dados_perfil["favoritos"])
        bio = " ".join(filter(None, [rng.choice(dados_perfil["bios"]), rng.choice(FRASES_FINAIS)]))

        usuarios.append({
            "nome": nome,
            "username": username,
            "email": f"{username}@example.com",  # domínio reservado, nenhum e-mail real é enviado
            "perfil": perfil,
            "generos": generos,
            "bio": bio,
            "artistaFavorito": artista,
            "musicaFavorita": musica,
            "mostrarCurtidas": username == USERNAME_DEMO or rng.random() > 0.1,
        })
    return usuarios


def gerar_imagens(rng, usuarios):
    if PASTA_IMAGENS.exists():
        shutil.rmtree(PASTA_IMAGENS)
    PASTA_IMAGENS.mkdir(parents=True)

    fotos_por_perfil = {}
    for perfil, dados in PERFIS.items():
        fotos_por_perfil[perfil] = []
        for i in range(1, 7):
            arquivo = f"show_{perfil}_{i}.jpg"
            gerar_foto_show(rng, dados["paleta"], PASTA_IMAGENS / arquivo)
            fotos_por_perfil[perfil].append(f"/uploads/seed/{arquivo}")

    for usuario in usuarios:
        arquivo = f"avatar_{usuario['username'].replace('.', '_')}.jpg"
        gerar_avatar(usuario["nome"], PERFIS[usuario["perfil"]]["paleta"], PASTA_IMAGENS / arquivo)
        usuario["fotoPerfil"] = f"/uploads/seed/{arquivo}"
        posts = rng.sample(fotos_por_perfil[usuario["perfil"]], rng.randint(1, 4))
        usuario["fotoShow"] = posts[0]
        usuario["fotosPosts"] = posts

    print(f"Imagens geradas em {PASTA_IMAGENS}")


def gerar_interacoes(rng, usuarios):
    """Curtidas e rejeições, com preferência por quem tem gêneros em comum."""
    curtidas, rejeicoes = set(), set()
    nomes = [u["username"] for u in usuarios]
    generos = {u["username"]: set(u["generos"]) for u in usuarios}

    for origem in nomes:
        if origem == USERNAME_DEMO:
            continue
        candidatos = [(d, 1 + 3 * len(generos[origem] & generos[d])) for d in nomes
                      if d != origem and d != USERNAME_DEMO]
        for _ in range(rng.randint(2, 8)):
            curtidas.add((origem, escolher_ponderado(rng, candidatos)))
        for _ in range(rng.randint(0, 3)):
            destino = rng.choice(nomes)
            if destino not in (origem, USERNAME_DEMO) and (origem, destino) not in curtidas:
                rejeicoes.add((origem, destino))

    # Parte das curtidas é retribuída, formando matches. Percorre em ordem fixa (sorted): a ordem
    # de um set muda a cada execução do Python, e isso tornaria a base diferente a cada carga
    for origem, destino in sorted(curtidas):
        if rng.random() < 0.35 and (destino, origem) not in rejeicoes:
            curtidas.add((destino, origem))

    # Conta de demonstração com cada tela do app preenchida
    demo = USERNAME_DEMO
    afins = sorted((d for d in nomes if d != demo and generos[demo] & generos[d]),
                   key=lambda d: -len(generos[demo] & generos[d]))
    visiveis = [d for d in afins if next(u for u in usuarios if u["username"] == d)["mostrarCurtidas"]]
    rng.shuffle(visiveis)
    matches, recebidas = visiveis[:6], visiveis[6:13]
    restantes = [d for d in afins if d not in matches and d not in recebidas]
    rng.shuffle(restantes)
    enviadas, rejeitadas = restantes[:5], restantes[5:8]

    for outro in matches:
        curtidas.update({(demo, outro), (outro, demo)})
    for outro in recebidas:
        curtidas.add((outro, demo))
    for outro in enviadas:
        curtidas.add((demo, outro))
    for outro in rejeitadas:
        rejeicoes.add((demo, outro))
    # Quem a demo curtiu e ainda não retribuiu não pode tê-la rejeitado nem curtido
    for outro in enviadas:
        curtidas.discard((outro, demo))

    rejeicoes -= curtidas
    return curtidas, rejeicoes


# ---------------------------------------------------------------------------------------
# Execução
# ---------------------------------------------------------------------------------------

def popular(banco, usuarios, curtidas, rejeicoes):
    banco.executar("MATCH (n) DETACH DELETE n")
    print("Banco apagado.")

    # Mesmas restrições de unicidade criadas pela API ao iniciar (config/RestricoesBanco.java)
    for restricao in RESTRICOES:
        banco.executar(restricao)

    banco.executar("UNWIND $generos AS nome CREATE (:Genero {nome: nome})", generos=GENEROS)

    campos = ["nome", "username", "email", "bio", "artistaFavorito", "musicaFavorita",
              "mostrarCurtidas", "fotoPerfil", "fotoShow", "fotosPosts"]
    registros = [{c: u[c] for c in campos} for u in usuarios]
    banco.executar("""
        UNWIND $usuarios AS u
        CREATE (p:Pessoa)
        SET p = u, p.senhaHash = $senhaHash, p.onboardingCompleto = true
        """, usuarios=registros, senhaHash=SENHA_HASH_DEMO)

    pares_genero = [{"u": u["username"], "g": g} for u in usuarios for g in u["generos"]]
    banco.executar("""
        UNWIND $pares AS par
        MATCH (p:Pessoa {username: par.u}), (g:Genero {nome: par.g})
        CREATE (p)-[:GOSTA_DE]->(g)
        """, pares=pares_genero)

    for tipo, pares in (("CURTIU", curtidas), ("REJEITOU", rejeicoes)):
        banco.executar(f"""
            UNWIND $pares AS par
            MATCH (a:Pessoa {{username: par[0]}}), (b:Pessoa {{username: par[1]}})
            CREATE (a)-[:{tipo}]->(b)
            """, pares=[list(p) for p in sorted(pares)])


def resumo(banco):
    print("\nResumo da base:")
    for linha in banco.executar("MATCH (n) RETURN labels(n)[0] AS rotulo, count(*) AS total ORDER BY rotulo"):
        print(f"  {linha['rotulo']}: {linha['total']} nós")
    for linha in banco.executar("MATCH ()-[r]->() RETURN type(r) AS tipo, count(*) AS total ORDER BY tipo"):
        print(f"  {linha['tipo']}: {linha['total']} relações")
    matches = banco.executar(
        "MATCH (a:Pessoa)-[:CURTIU]->(b:Pessoa)-[:CURTIU]->(a) WHERE id(a) < id(b) RETURN count(*) AS total")
    print(f"  Matches (curtidas recíprocas): {matches[0]['total']}")

    print("\nGêneros mais populares:")
    for linha in banco.executar("""
            MATCH (g:Genero)<-[:GOSTA_DE]-(p) RETURN g.nome AS genero, count(p) AS usuarios
            ORDER BY usuarios DESC, genero LIMIT 8"""):
        print(f"  {linha['genero']}: {linha['usuarios']} usuários")

    demo = banco.executar("""
        MATCH (me:Pessoa {username: $u})
        RETURN id(me) AS id,
          [(me)-[:GOSTA_DE]->(g) | g.nome] AS generos,
          COUNT { (me)-[:CURTIU]->(o)-[:CURTIU]->(me) } AS matches,
          COUNT { (o)-[:CURTIU]->(me) WHERE NOT (me)-[:CURTIU]->(o) } AS recebidas,
          COUNT { (me)-[:CURTIU]->(o) WHERE NOT (o)-[:CURTIU]->(me) } AS enviadas,
          COUNT { (me)-[:REJEITOU]->() } AS rejeitadas
        """, u=USERNAME_DEMO)[0]
    print(f"\nConta de demonstração: {USERNAME_DEMO} (id {demo['id']}), gêneros {', '.join(demo['generos'])}")
    print(f"  {demo['matches']} matches, {demo['recebidas']} curtidas recebidas, "
          f"{demo['enviadas']} curtidas enviadas, {demo['rejeitadas']} rejeitados")
    print("  Senha de todos os usuários fictícios: meetbeat123")


def main():
    banco = Neo4jHttp()
    banco.executar("RETURN 1")  # falha cedo se o Neo4j estiver desligado

    total = banco.executar("MATCH (n) RETURN count(n) AS total")[0]["total"]
    if "--confirmar" not in sys.argv:
        print(f"O banco atual tem {total} nós. Eles serão APAGADOS (um backup será salvo antes).")
        if input("Digite APAGAR para continuar: ").strip() != "APAGAR":
            sys.exit("Cancelado. Nada foi alterado.")

    rng = random.Random(SEMENTE)
    usuarios = gerar_usuarios(rng)
    curtidas, rejeicoes = gerar_interacoes(rng, usuarios)

    fazer_backup(banco)
    gerar_imagens(random.Random(SEMENTE), usuarios)
    popular(banco, usuarios, curtidas, rejeicoes)
    resumo(banco)


if __name__ == "__main__":
    main()
