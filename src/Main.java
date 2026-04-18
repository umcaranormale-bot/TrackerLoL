import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.awt.Desktop;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
    static HttpClient clientNormal = HttpClient.newHttpClient();
    static HttpClient clientLCU;

    // Tabela de correções manuais (Nome do Campeão -> [Q, W, E, R])
    static HashMap<String, String[]> excecoesCooldowns = new HashMap<>() {{
        // put("Sejuani", new String[]{"11/10/9/8/7", "12/9/6/3/0", "16/15.5/15/14.5/14", "120/105/90"});
    }};

    static HashMap<Integer, String> dicionarioCampeoes = new HashMap<>();
    static HashMap<Integer, String> dicionarioRunas = new HashMap<>();
    static String riotApiKey = "";

    // Variável global para manter o Tracker sempre no patch atual do LoL
    static String versaoAtual = "16.8.1";

    static HashMap<String, JSONArray> cacheRunasNuvem = new HashMap<>();
    static long proximaTentativaNuvem = 0;
    static boolean sucessoNaNuvem = false;

    // Colocamos VERSAO_DDRAGON aqui para substituir automaticamente depois
    static String htmlInterface = """
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>LoL Cooldown Tracker</title>
            <style>
                body { background-color: #010A13; color: #C8AA6E; font-family: 'Segoe UI', Tahoma, sans-serif; margin: 0; padding: 20px; display: flex; flex-direction: column; align-items: center; }
                h1 { border-bottom: 2px solid #C8AA6E; padding-bottom: 10px; text-transform: uppercase; letter-spacing: 2px; }
                #container-inimigos { display: flex; flex-wrap: wrap; gap: 20px; justify-content: center; margin-top: 20px; width: 100%; align-items: flex-start; }
                .campeao-card { background: linear-gradient(180deg, #1E2328 0%, #0A1428 100%); border: 1px solid #785A28; border-radius: 8px; padding: 15px; width: 220px; box-shadow: 0 4px 8px rgba(0,0,0,0.5); text-align: center; transition: 0.2s; }
                .campeao-card:hover { border-color: #C8AA6E; transform: translateY(-3px); }
                .campeao-foto { width: 80px; height: 80px; border-radius: 50%; border: 2px solid #C8AA6E; margin-bottom: 10px; }
                .habilidade { display: flex; justify-content: space-between; background-color: rgba(0, 0, 0, 0.4); margin: 5px 0; padding: 5px 10px; border-radius: 4px; font-size: 14px; color: #F0E6D2; }
                .tecla { font-weight: bold; color: #0AC8B9; }
                .runas-box { margin-top: 15px; padding-top: 10px; border-top: 1px dashed #785A28; font-size: 12px; color: #A09B8C; text-align: left; line-height: 1.4; }
                .runa-essencial { color: #C8AA6E; font-weight: bold; font-size: 14px; margin-bottom: 5px; display: block; text-align: center; }
                .status-bar { margin-top: 20px; font-size: 12px; color: #888; }
            </style>
        </head>
        <body>
            <h1>Cooldown Tracker</h1>
            <div id="container-inimigos"><p style="color: #888;">Aguardando início da partida ou seleção de campeões...</p></div>
            <div class="status-bar" id="status">Buscando dados no servidor local e na nuvem...</div>
            <script>
                async function atualizarDados() {
                    try {
                        const resposta = await fetch('/api/cooldowns');
                        const inimigos = await resposta.json();
                        const container = document.getElementById('container-inimigos');
                        const status = document.getElementById('status');

                        if (inimigos.length === 0) {
                            container.innerHTML = '<p style="color: #888;">Nenhuma seleção ou partida detectada no momento.</p>';
                            status.innerText = "Monitorando...";
                            return;
                        }
                        container.innerHTML = '';
                        status.innerText = "Sincronizado via Live Client e Riot Spectator API!";
                        
                        inimigos.forEach(inimigo => {
                            const card = document.createElement('div');
                            card.className = 'campeao-card';
                            const imgUrl = `https://ddragon.leagueoflegends.com/cdn/VERSAO_DDRAGON/img/champion/${inimigo.nome}.png`;
                            
                            let htmlHabilidades = '';
                            inimigo.habilidades.forEach(hab => {
                                htmlHabilidades += `<div class="habilidade"><span class="tecla">[${hab.tecla}]</span><span>${hab.tempo}s</span></div>`;
                            });

                            let htmlRunas = `<div class="runas-box">`;
                            if (inimigo.runas_completas && inimigo.runas_completas.length > 0) {
                                htmlRunas += `<span class="runa-essencial">🎯 ${inimigo.runas_completas[0]}</span>`;
                                for(let i = 1; i < inimigo.runas_completas.length; i++) {
                                    htmlRunas += `🔹 ${inimigo.runas_completas[i]}<br>`;
                                }
                            } else {
                                htmlRunas += `<span class="runa-essencial">⚠️ Oculto</span><br>Runas escondidas na Seleção de Campeões.`;
                            }
                            htmlRunas += `</div>`;

                            card.innerHTML = `<img src="${imgUrl}" alt="${inimigo.nome}" class="campeao-foto"> <h3 style="margin: 5px 0;">${inimigo.nome}</h3> <div>${htmlHabilidades}</div> ${htmlRunas}`;
                            container.appendChild(card);
                        });
                    } catch (erro) {
                        document.getElementById('status').innerText = "Erro. Certifique-se que o programa está rodando.";
                    }
                }
                setInterval(atualizarDados, 3000);
                atualizarDados();
            </script>
        </body>
        </html>
        """;

    public static void main(String[] args) throws Exception {
        configurarSSL();
        carregarDicionarios();
        carregarChaveRiot();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try {
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    byte[] bytes = htmlInterface.getBytes("UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } catch (Exception e) {}
            }
        });

        server.createContext("/api/cooldowns", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try {
                    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                    JSONArray arrayInimigos = new JSONArray();
                    if (!tentarLerPartida(arrayInimigos)) tentarLerSelecao(arrayInimigos);
                    byte[] bytes = arrayInimigos.toString().getBytes("UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } catch (Exception e) {}
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor iniciado na porta 8000.");

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI("http://localhost:8000"));
        }
    }

    public static void carregarChaveRiot() {
        try {
            File arquivoChave = new File("chave_riot.txt");
            if (arquivoChave.exists()) {
                String conteudo = Files.readString(arquivoChave.toPath());
                riotApiKey = conteudo.replaceAll("[^a-zA-Z0-9-]", "");
                System.out.println("-> Chave Riot (" + riotApiKey.substring(0, Math.min(8, riotApiKey.length())) + "...) carregada e filtrada!");
            } else {
                arquivoChave.createNewFile();
                System.out.println("⚠️ AVISO: Arquivo 'chave_riot.txt' criado. Cole sua chave lá!");
            }
        } catch (Exception e) {}
    }

    public static HashMap<String, JSONArray> buscarRunasDaNuvem() {
        if (sucessoNaNuvem) {
            return cacheRunasNuvem;
        }

        if (System.currentTimeMillis() < proximaTentativaNuvem) {
            return cacheRunasNuvem;
        }

        HashMap<String, JSONArray> mapaRunasNuvem = new HashMap<>();
        if (riotApiKey.isEmpty()) {
            System.out.println("⚠️ Nenhuma chave encontrada no arquivo 'chave_riot.txt'. Usando Plano B local.");
            return mapaRunasNuvem;
        }

        try {
            File arquivoLockfile = new File("C:\\Riot Games\\League of Legends\\lockfile");
            if (!arquivoLockfile.exists()) return mapaRunasNuvem;
            String[] partes = Files.readString(arquivoLockfile.toPath()).split(":");
            String tokenBase64 = Base64.getEncoder().encodeToString(("riot:" + partes[3]).getBytes());

            HttpRequest reqSummoner = HttpRequest.newBuilder()
                    .uri(URI.create("https://127.0.0.1:" + partes[2] + "/lol-summoner/v1/current-summoner"))
                    .header("Authorization", "Basic " + tokenBase64).GET().build();
            JSONObject summonerJson = new JSONObject(clientLCU.send(reqSummoner, HttpResponse.BodyHandlers.ofString()).body());

            String gameName = summonerJson.has("gameName") ? summonerJson.getString("gameName") : summonerJson.getString("displayName").split("#")[0];
            String tagLine = summonerJson.has("tagLine") ? summonerJson.getString("tagLine") : "BR1";
            if (summonerJson.has("displayName") && summonerJson.getString("displayName").contains("#")) {
                tagLine = summonerJson.getString("displayName").split("#")[1];
            }

            gameName = java.net.URLEncoder.encode(gameName, "UTF-8").replace("+", "%20");
            tagLine = java.net.URLEncoder.encode(tagLine, "UTF-8").replace("+", "%20");

            HttpRequest reqAccount = HttpRequest.newBuilder()
                    .uri(URI.create("https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/" + gameName + "/" + tagLine))
                    .header("X-Riot-Token", riotApiKey).GET().build();
            HttpResponse<String> resAccount = clientNormal.send(reqAccount, HttpResponse.BodyHandlers.ofString());

            if (resAccount.statusCode() != 200) {
                System.out.println("⚠️ Erro ao converter Riot ID. A Nuvem respondeu: " + resAccount.statusCode());
                proximaTentativaNuvem = System.currentTimeMillis() + 60000;
                return mapaRunasNuvem;
            }
            String puuidPublico = new JSONObject(resAccount.body()).getString("puuid");

            HttpRequest reqSpectator = HttpRequest.newBuilder()
                    .uri(URI.create("https://br1.api.riotgames.com/lol/spectator/v5/active-games/by-summoner/" + puuidPublico))
                    .header("X-Riot-Token", riotApiKey).GET().build();
            HttpResponse<String> resSpectator = clientNormal.send(reqSpectator, HttpResponse.BodyHandlers.ofString());

            if (resSpectator.statusCode() == 200) {
                System.out.println("✅ Nuvem da Riot conectada com sucesso! Runas baixadas e salvas na memória.");
                JSONArray participants = new JSONObject(resSpectator.body()).getJSONArray("participants");
                for (int i = 0; i < participants.length(); i++) {
                    JSONObject jogadorNuvem = participants.getJSONObject(i);
                    int champId = jogadorNuvem.getInt("championId");
                    String nomeCampeao = dicionarioCampeoes.getOrDefault(champId, "Desconhecido");

                    JSONArray idsDasRunas = jogadorNuvem.getJSONObject("perks").getJSONArray("perkIds");
                    JSONArray runasTraduzidas = new JSONArray();
                    for (int j = 0; j < idsDasRunas.length(); j++) {
                        int idRuna = idsDasRunas.getInt(j);
                        runasTraduzidas.put(dicionarioRunas.getOrDefault(idRuna, "Fragmento de Status"));
                    }
                    mapaRunasNuvem.put(nomeCampeao, runasTraduzidas);
                }

                cacheRunasNuvem = mapaRunasNuvem;
                sucessoNaNuvem = true;

            } else if (resSpectator.statusCode() == 403) {
                System.out.println("\n🔴 ERRO CRÍTICO: Chave da Riot VENCEU ou é inválida!");
                System.out.println("Cole uma nova no chave_riot.txt. O Tracker tentará lê-la de novo em 1 minuto.");
                proximaTentativaNuvem = System.currentTimeMillis() + 60000;
            } else if (resSpectator.statusCode() == 404) {
                System.out.println("⏳ Partida ranqueada com delay (Erro 404). O Tracker tentará de novo sozinho em 1 minuto...");
                proximaTentativaNuvem = System.currentTimeMillis() + 60000;
            } else {
                System.out.println("⚠️ Erro desconhecido: " + resSpectator.statusCode());
                proximaTentativaNuvem = System.currentTimeMillis() + 60000;
            }
        } catch (Exception e) {}

        return mapaRunasNuvem;
    }

    public static boolean tentarLerPartida(JSONArray arrayInimigos) {
        try {
            HttpRequest reqActive = HttpRequest.newBuilder().uri(URI.create("https://127.0.0.1:2999/liveclientdata/activeplayer")).GET().build();
            HttpResponse<String> resActive = clientLCU.send(reqActive, HttpResponse.BodyHandlers.ofString());

            if (resActive.statusCode() == 200) {
                String meuNome = new JSONObject(resActive.body()).getString("summonerName");
                HttpRequest reqList = HttpRequest.newBuilder().uri(URI.create("https://127.0.0.1:2999/liveclientdata/playerlist")).GET().build();
                JSONArray listaJogadores = new JSONArray(clientLCU.send(reqList, HttpResponse.BodyHandlers.ofString()).body());

                String meuTime = "";
                for (int i = 0; i < listaJogadores.length(); i++) {
                    if (listaJogadores.getJSONObject(i).getString("summonerName").equals(meuNome)) {
                        meuTime = listaJogadores.getJSONObject(i).getString("team");
                        break;
                    }
                }

                HashMap<String, JSONArray> mapaRunasNuvem = buscarRunasDaNuvem();

                for (int i = 0; i < listaJogadores.length(); i++) {
                    JSONObject jogador = listaJogadores.getJSONObject(i);
                    if (!jogador.getString("team").equals(meuTime)) {
                        String nomeCampeao = jogador.getString("championName");
                        JSONObject inimigoData = buscarSpellsDoCampeao(nomeCampeao);

                        if (mapaRunasNuvem.containsKey(nomeCampeao)) {
                            inimigoData.put("runas_completas", mapaRunasNuvem.get(nomeCampeao));
                        } else {
                            JSONArray runasLocais = new JSONArray();
                            try {
                                JSONObject runasAPI = jogador.getJSONObject("runes");
                                runasLocais.put(runasAPI.getJSONObject("keystone").getString("displayName"));
                                runasLocais.put(runasAPI.getJSONObject("secondaryRuneTree").getString("displayName"));
                                runasLocais.put("(Detalhes Ocultos em Partidas Contra Bots)");
                            } catch (Exception e) {}

                            if(runasLocais.length() > 0){
                                inimigoData.put("runas_completas", runasLocais);
                            }
                        }
                        arrayInimigos.put(inimigoData);
                    }
                }
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    public static void tentarLerSelecao(JSONArray arrayInimigos) {
        try {
            File arquivoLockfile = new File("C:\\Riot Games\\League of Legends\\lockfile");
            if (!arquivoLockfile.exists()) return;
            String[] partes = Files.readString(arquivoLockfile.toPath()).split(":");
            String tokenBase64 = Base64.getEncoder().encodeToString(("riot:" + partes[3]).getBytes());

            HttpRequest requestLCU = HttpRequest.newBuilder()
                    .uri(URI.create("https://127.0.0.1:" + partes[2] + "/lol-champ-select/v4/session"))
                    .header("Authorization", "Basic " + tokenBase64).GET().build();

            HttpResponse<String> responseLCU = clientLCU.send(requestLCU, HttpResponse.BodyHandlers.ofString());

            if (responseLCU.statusCode() == 200) {
                JSONArray timeInimigo = new JSONObject(responseLCU.body()).getJSONArray("theirTeam");
                for (int i = 0; i < timeInimigo.length(); i++) {
                    int idDoCampeao = timeInimigo.getJSONObject(i).getInt("championId");
                    if (idDoCampeao != 0) {
                        String nomeDoCampeao = dicionarioCampeoes.getOrDefault(idDoCampeao, "Desconhecido");
                        JSONObject inimigoData = buscarSpellsDoCampeao(nomeDoCampeao);
                        arrayInimigos.put(inimigoData);
                    }
                }
            }
        } catch (Exception e) {}
    }

    public static JSONObject buscarSpellsDoCampeao(String nomeCampeao) {
        JSONObject objInimigo = new JSONObject();
        objInimigo.put("nome", nomeCampeao);
        JSONArray cooldowns = new JSONArray();
        String[] teclas = {"Q", "W", "E", "R"};

        if (excecoesCooldowns.containsKey(nomeCampeao)) {
            String[] temposManuais = excecoesCooldowns.get(nomeCampeao);
            for (int i = 0; i < 4; i++) {
                JSONObject skill = new JSONObject();
                skill.put("tecla", teclas[i]);
                skill.put("tempo", temposManuais[i]);
                cooldowns.put(skill);
            }
            System.out.println("-> Usando valores CORRIGIDOS para: " + nomeCampeao);
        } else {
            try {
                // AQUI usamos a versão dinâmica para nunca quebrar em atualizações futuras
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://ddragon.leagueoflegends.com/cdn/" + versaoAtual + "/data/pt_BR/champion/" + nomeCampeao + ".json"))
                        .GET().build();
                HttpResponse<String> response = clientNormal.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONArray spells = new JSONObject(response.body()).getJSONObject("data").getJSONObject(nomeCampeao).getJSONArray("spells");
                    for (int i = 0; i < 4; i++) {
                        JSONObject skill = new JSONObject();
                        skill.put("tecla", teclas[i]);
                        skill.put("tempo", spells.getJSONObject(i).getString("cooldownBurn"));
                        cooldowns.put(skill);
                    }
                }
            } catch (Exception e) {}
        }

        objInimigo.put("habilidades", cooldowns);
        return objInimigo;
    }

    public static void carregarDicionarios() {
        try {
            HttpRequest reqVersion = HttpRequest.newBuilder()
                    .uri(URI.create("https://ddragon.leagueoflegends.com/api/versions.json")).GET().build();
            JSONArray versoes = new JSONArray(clientNormal.send(reqVersion, HttpResponse.BodyHandlers.ofString()).body());

            // AQUI salvamos a versão atual na variável global
            versaoAtual = versoes.getString(0);
            System.out.println("-> Versão detectada: " + versaoAtual);

            // Injetamos a versão correta no nosso código HTML para as fotos aparecerem
            htmlInterface = htmlInterface.replace("VERSAO_DDRAGON", versaoAtual);

            HttpRequest reqCamp = HttpRequest.newBuilder()
                    .uri(URI.create("https://ddragon.leagueoflegends.com/cdn/" + versaoAtual + "/data/pt_BR/champion.json")).GET().build();
            JSONObject dados = new JSONObject(clientNormal.send(reqCamp, HttpResponse.BodyHandlers.ofString()).body()).getJSONObject("data");

            for (String key : dados.keySet()) {
                dicionarioCampeoes.put(Integer.parseInt(dados.getJSONObject(key).getString("key")), key);
            }

            HttpRequest reqRunas = HttpRequest.newBuilder()
                    .uri(URI.create("https://ddragon.leagueoflegends.com/cdn/" + versaoAtual + "/data/pt_BR/runesReforged.json")).GET().build();
            JSONArray arvores = new JSONArray(clientNormal.send(reqRunas, HttpResponse.BodyHandlers.ofString()).body());

            for (int i = 0; i < arvores.length(); i++) {
                JSONArray slots = arvores.getJSONObject(i).getJSONArray("slots");
                for (int j = 0; j < slots.length(); j++) {
                    JSONArray runes = slots.getJSONObject(j).getJSONArray("runes");
                    for (int k = 0; k < runes.length(); k++) {
                        JSONObject rune = runes.getJSONObject(k);
                        dicionarioRunas.put(rune.getInt("id"), rune.getString("name"));
                    }
                }
            }
            System.out.println("-> Dicionários de Campeões e Runas carregados com sucesso!");

        } catch (Exception e) {
            System.out.println("Erro crítico ao carregar dicionários: " + e.getMessage());
        }
    }

    public static void configurarSSL() {
        try {
            TrustManager[] tm = new TrustManager[]{ new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tm, new java.security.SecureRandom());
            clientLCU = HttpClient.newBuilder().sslContext(sc).build();
        } catch (Exception e) {}
    }
}