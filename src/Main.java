import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

public class Main extends JFrame {
    static HttpClient clientNormal = HttpClient.newHttpClient();
    static HttpClient clientLCU;

    static HashMap<String, String[]> excecoesCooldowns = new HashMap<>();
    static HashMap<Integer, String> dicionarioCampeoes = new HashMap<>();
    static HashMap<Integer, String> dicionarioRunas = new HashMap<>();
    static String riotApiKey = "";
    static String versaoAtual = "14.8.1";

    static HashMap<String, JSONArray> cacheRunasNuvem = new HashMap<>();
    static long proximaTentativaNuvem = 0;
    static boolean sucessoNaNuvem = false;

    static JPanel painelInimigos;
    static boolean overlayConstruido = false;

    // --- HTML ATUALIZADO PARA MOSTRAR A ÁRVORE SECUNDÁRIA ---
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
                .campeao-foto { width: 80px; height: 80px; border-radius: 50%; border: 2px solid #C8AA6E; margin-bottom: 10px; }
                .habilidade { display: flex; justify-content: space-between; background-color: rgba(0, 0, 0, 0.4); margin: 5px 0; padding: 5px 10px; border-radius: 4px; font-size: 14px; color: #F0E6D2; }
                .tecla { font-weight: bold; color: #0AC8B9; }
                .runas-box { margin-top: 15px; padding-top: 10px; border-top: 1px dashed #785A28; font-size: 12px; color: #A09B8C; text-align: left; line-height: 1.4; }
                .runa-essencial { color: #C8AA6E; font-weight: bold; font-size: 14px; margin-bottom: 2px; display: block; text-align: center; }
                .arvore-secundaria { color: #888888; font-weight: bold; font-size: 11px; margin-bottom: 6px; display: block; text-align: center; }
                .status-bar { margin-top: 20px; font-size: 12px; color: #888; }
            </style>
        </head>
        <body>
            <h1>Cooldown Tracker Web</h1>
            <div id="container-inimigos"><p style="color: #888;">Aguardando partida...</p></div>
            <div class="status-bar" id="status">Sincronizado via LCU e Nuvem</div>
            <script>
                async function atualizarDados() {
                    try {
                        const resposta = await fetch('/api/cooldowns');
                        const inimigos = await resposta.json();
                        const container = document.getElementById('container-inimigos');

                        if (inimigos.length === 0) {
                            container.innerHTML = '<p style="color: #888;">Nenhuma partida detectada.</p>';
                            return;
                        }
                        container.innerHTML = '';
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
                                // 1. Runa Principal
                                htmlRunas += `<span class="runa-essencial">🎯 ${inimigo.runas_completas[0]}</span>`;
                                
                                // 2. Árvore Secundária (Se existir)
                                if (inimigo.runas_completas.length > 1) {
                                    htmlRunas += `<span class="arvore-secundaria">🌳 Secundária: ${inimigo.runas_completas[1]}</span>`;
                                }
                                
                                // 3. O resto das runas menores e fragmentos
                                for(let i = 2; i < inimigo.runas_completas.length; i++) {
                                    htmlRunas += `🔹 ${inimigo.runas_completas[i]}<br>`;
                                }
                            }
                            htmlRunas += `</div>`;
                            
                            card.innerHTML = `<img src="${imgUrl}" class="campeao-foto"> <h3 style="margin: 5px 0;">${inimigo.nome}</h3> <div>${htmlHabilidades}</div> ${htmlRunas}`;
                            container.appendChild(card);
                        });
                    } catch (erro) {}
                }
                setInterval(atualizarDados, 3000);
                atualizarDados();
            </script>
        </body>
        </html>
        """;

    public Main() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setLayout(new BorderLayout());

        JPanel barraControle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        barraControle.setOpaque(false);

        JButton btnToggle = new JButton("✖ Ocultar Spells");
        btnToggle.setFont(new Font("Arial", Font.BOLD, 9));
        btnToggle.setBackground(new Color(20, 20, 20, 220));
        btnToggle.setForeground(new Color(200, 170, 110));
        btnToggle.setFocusPainted(false);
        btnToggle.setMargin(new Insets(2, 5, 2, 5));

        btnToggle.addActionListener(e -> {
            boolean visivel = painelInimigos.isVisible();
            painelInimigos.setVisible(!visivel);
            btnToggle.setText(!visivel ? "✖ Ocultar Spells" : "👁 Mostrar Spells");
            pack();
        });

        barraControle.add(btnToggle);
        add(barraControle, BorderLayout.NORTH);

        painelInimigos = new JPanel();
        painelInimigos.setLayout(new BoxLayout(painelInimigos, BoxLayout.Y_AXIS));
        painelInimigos.setOpaque(false);
        add(painelInimigos, BorderLayout.CENTER);

        setLocation(0, 0);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Timer atualizador = new Timer(3000, e -> {
            JSONArray inimigos = new JSONArray();
            if (!tentarLerPartida(inimigos)) tentarLerSelecao(inimigos);

            if (inimigos.length() == 0) {
                if (overlayConstruido) {
                    painelInimigos.removeAll();
                    painelInimigos.revalidate();
                    painelInimigos.repaint();
                    pack();
                    overlayConstruido = false;
                }
            }
            else if (!overlayConstruido) {
                painelInimigos.removeAll();
                for (int i = 0; i < inimigos.length(); i++) {
                    JSONObject in = inimigos.getJSONObject(i);
                    String nome = in.getString("nome");

                    String spell1 = in.has("spell1") ? in.getString("spell1") : "Flash";
                    String spell2 = in.has("spell2") ? in.getString("spell2") : "Incendiar";

                    int cd1 = getCooldownFeitico(spell1);
                    int cd2 = getCooldownFeitico(spell2);

                    int cdUlt = 120;
                    try {
                        int level = in.optInt("level", 1);
                        String[] ultCds = in.optString("ultCooldown", "120/100/80").split("/");
                        int rankIndex = 0;
                        if (level >= 16) rankIndex = 2;
                        else if (level >= 11) rankIndex = 1;
                        if (rankIndex >= ultCds.length) rankIndex = ultCds.length - 1;
                        cdUlt = (int) Math.round(Double.parseDouble(ultCds[rankIndex].trim()));
                    } catch (Exception ex) {}

                    String principal = "Desconhecida";
                    String secundaria = "";
                    String status = "";

                    if (in.has("runas_completas")) {
                        JSONArray runas = in.getJSONArray("runas_completas");
                        if (runas.length() > 0) principal = runas.getString(0);
                        if (runas.length() > 1) secundaria = runas.getString(1); // AGORA AQUI É A ÁRVORE!
                        if (runas.length() > 2) status = runas.getString(2);
                        for(int r = 3; r < runas.length(); r++) status += ", " + runas.getString(r);
                    }

                    painelInimigos.add(new CartaoInimigo(nome, spell1, cd1, spell2, cd2, cdUlt, principal, secundaria, status));
                }
                painelInimigos.revalidate();
                painelInimigos.repaint();
                pack();
                overlayConstruido = true;
            }
        });
        atualizador.start();
        pack();
    }

    public static void main(String[] args) throws Exception {
        configurarSSL();
        carregarDicionarios();
        carregarChaveRiot();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            byte[] bytes = htmlInterface.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        });
        server.createContext("/api/cooldowns", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            JSONArray arrayInimigos = new JSONArray();
            if (!tentarLerPartida(arrayInimigos)) tentarLerSelecao(arrayInimigos);
            byte[] bytes = arrayInimigos.toString().getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        });
        server.setExecutor(null);
        server.start();

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try { Desktop.getDesktop().browse(new URI("http://localhost:8000")); } catch (Exception e) {}
        }

        SwingUtilities.invokeLater(() -> {
            Main overlay = new Main();
            overlay.setVisible(true);
        });
    }

    public static int getCooldownFeitico(String nome) {
        if (nome.contains("Flash")) return 300;
        if (nome.contains("Ignite") || nome.contains("Incendiar")) return 180;
        if (nome.contains("Heal") || nome.contains("Curar")) return 240;
        if (nome.contains("Teleport") || nome.contains("Teleporte")) return 360;
        if (nome.contains("Exhaust") || nome.contains("Exaustão")) return 210;
        if (nome.contains("Cleanse") || nome.contains("Purificar")) return 210;
        if (nome.contains("Barrier") || nome.contains("Barreira")) return 180;
        if (nome.contains("Ghost") || nome.contains("Fantasma")) return 210;
        if (nome.contains("Smite") || nome.contains("Golpear")) return 15;
        return 300;
    }

    public static String getNomeIconeFeitico(String nome) {
        if (nome.equals("Ult")) return null;
        if (nome.contains("Flash")) return "SummonerFlash";
        if (nome.contains("Ignite") || nome.contains("Incendiar")) return "SummonerDot";
        if (nome.contains("Heal") || nome.contains("Curar")) return "SummonerHeal";
        if (nome.contains("Teleport") || nome.contains("Teleporte")) return "SummonerTeleport";
        if (nome.contains("Exhaust") || nome.contains("Exaustão")) return "SummonerExhaust";
        if (nome.contains("Cleanse") || nome.contains("Purificar")) return "SummonerBoost";
        if (nome.contains("Barrier") || nome.contains("Barreira")) return "SummonerBarrier";
        if (nome.contains("Ghost") || nome.contains("Fantasma")) return "SummonerHaste";
        if (nome.contains("Smite") || nome.contains("Golpear")) return "SummonerSmite";
        return "SummonerFlash";
    }

    public static void carregarChaveRiot() {
        try {
            File arquivoChave = new File("chave_riot.txt");
            if (arquivoChave.exists()) {
                String conteudo = Files.readString(arquivoChave.toPath());
                riotApiKey = conteudo.replaceAll("[^a-zA-Z0-9-]", "");
            }
        } catch (Exception e) {}
    }

    public static HashMap<String, JSONArray> buscarRunasDaNuvem() {
        if (sucessoNaNuvem) return cacheRunasNuvem;
        if (System.currentTimeMillis() < proximaTentativaNuvem) return cacheRunasNuvem;

        HashMap<String, JSONArray> mapaRunasNuvem = new HashMap<>();
        if (riotApiKey.isEmpty()) return mapaRunasNuvem;

        try {
            File arquivoLockfile = new File("C:\\Riot Games\\League of Legends\\lockfile");
            if (!arquivoLockfile.exists()) return mapaRunasNuvem;
            String[] partes = Files.readString(arquivoLockfile.toPath()).split(":");
            String tokenBase64 = Base64.getEncoder().encodeToString(("riot:" + partes[3]).getBytes());

            HttpRequest reqSummoner = HttpRequest.newBuilder().uri(URI.create("https://127.0.0.1:" + partes[2] + "/lol-summoner/v1/current-summoner")).header("Authorization", "Basic " + tokenBase64).GET().build();
            JSONObject summonerJson = new JSONObject(clientLCU.send(reqSummoner, HttpResponse.BodyHandlers.ofString()).body());
            String gameName = java.net.URLEncoder.encode(summonerJson.getString("gameName"), "UTF-8").replace("+", "%20");
            String tagLine = java.net.URLEncoder.encode(summonerJson.getString("tagLine"), "UTF-8").replace("+", "%20");

            HttpRequest reqAccount = HttpRequest.newBuilder().uri(URI.create("https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/" + gameName + "/" + tagLine)).header("X-Riot-Token", riotApiKey).GET().build();
            HttpResponse<String> resAccount = clientNormal.send(reqAccount, HttpResponse.BodyHandlers.ofString());

            if (resAccount.statusCode() == 200) {
                String puuidPublico = new JSONObject(resAccount.body()).getString("puuid");
                HttpRequest reqSpectator = HttpRequest.newBuilder().uri(URI.create("https://br1.api.riotgames.com/lol/spectator/v5/active-games/by-summoner/" + puuidPublico)).header("X-Riot-Token", riotApiKey).GET().build();
                HttpResponse<String> resSpectator = clientNormal.send(reqSpectator, HttpResponse.BodyHandlers.ofString());

                if (resSpectator.statusCode() == 200) {
                    JSONArray participants = new JSONObject(resSpectator.body()).getJSONArray("participants");
                    for (int i = 0; i < participants.length(); i++) {
                        JSONObject jogadorNuvem = participants.getJSONObject(i);
                        String nomeCampeao = dicionarioCampeoes.getOrDefault(jogadorNuvem.getInt("championId"), "Desconhecido");
                        JSONArray traduzidas = new JSONArray();

                        // --- NOVA LÓGICA DE RUNAS DA NUVEM COM ÁRVORE ---
                        if (jogadorNuvem.has("perks")) {
                            JSONObject perks = jogadorNuvem.getJSONObject("perks");
                            JSONArray idsRunas = perks.getJSONArray("perkIds");

                            // 1. Runa Principal
                            if (idsRunas.length() > 0) traduzidas.put(dicionarioRunas.getOrDefault(idsRunas.getInt(0), "Runa Principal"));

                            // 2. Árvore Secundária (Traduzida pelo ID do perkSubStyle)
                            int subStyle = perks.optInt("perkSubStyle", 0);
                            if (subStyle != 0) traduzidas.put(dicionarioRunas.getOrDefault(subStyle, "Secundária Oculta"));

                            // 3. O resto das runas
                            for (int j = 1; j < idsRunas.length(); j++) {
                                traduzidas.put(dicionarioRunas.getOrDefault(idsRunas.getInt(j), "Runa Menor"));
                            }
                        }
                        mapaRunasNuvem.put(nomeCampeao, traduzidas);
                    }
                    cacheRunasNuvem = mapaRunasNuvem;
                    sucessoNaNuvem = true;
                } else { proximaTentativaNuvem = System.currentTimeMillis() + 60000; }
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

                        inimigoData.put("level", jogador.optInt("level", 1));

                        try {
                            JSONObject spellsAPI = jogador.getJSONObject("summonerSpells");
                            inimigoData.put("spell1", spellsAPI.getJSONObject("summonerSpellOne").getString("displayName"));
                            inimigoData.put("spell2", spellsAPI.getJSONObject("summonerSpellTwo").getString("displayName"));
                        } catch (Exception e) {}

                        if (mapaRunasNuvem.containsKey(nomeCampeao)) {
                            inimigoData.put("runas_completas", mapaRunasNuvem.get(nomeCampeao));
                        } else {
                            // --- NOVA LÓGICA DE RUNAS LOCAIS COM ÁRVORE ---
                            JSONArray runasLocais = new JSONArray();
                            try {
                                JSONObject runasAPI = jogador.getJSONObject("runes");

                                // 1. Runa Principal
                                if (runasAPI.has("keystone")) runasLocais.put(runasAPI.getJSONObject("keystone").getString("displayName"));

                                // 2. Árvore Secundária
                                if (runasAPI.has("secondaryRuneTree")) {
                                    runasLocais.put(runasAPI.getJSONObject("secondaryRuneTree").getString("displayName"));
                                } else {
                                    runasLocais.put("Secundária Oculta");
                                }

                                // 3. Runas Gerais e Status
                                if (runasAPI.has("generalRunes")) {
                                    JSONArray general = runasAPI.getJSONArray("generalRunes");
                                    for (int r = 1; r < general.length(); r++) runasLocais.put(general.getJSONObject(r).getString("displayName"));
                                }
                                if (runasAPI.has("statRunes")) {
                                    JSONArray stats = runasAPI.getJSONArray("statRunes");
                                    for (int r = 0; r < stats.length(); r++) runasLocais.put(dicionarioRunas.getOrDefault(stats.getJSONObject(r).getInt("id"), "Status"));
                                }
                            } catch (Exception e) {}
                            inimigoData.put("runas_completas", runasLocais);
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

            HttpRequest requestLCU = HttpRequest.newBuilder().uri(URI.create("https://127.0.0.1:" + partes[2] + "/lol-champ-select/v4/session")).header("Authorization", "Basic " + tokenBase64).GET().build();
            HttpResponse<String> responseLCU = clientLCU.send(requestLCU, HttpResponse.BodyHandlers.ofString());

            if (responseLCU.statusCode() == 200) {
                JSONArray timeInimigo = new JSONObject(responseLCU.body()).getJSONArray("theirTeam");
                for (int i = 0; i < timeInimigo.length(); i++) {
                    int idDoCampeao = timeInimigo.getJSONObject(i).getInt("championId");
                    if (idDoCampeao != 0) {
                        JSONObject inimigoData = buscarSpellsDoCampeao(dicionarioCampeoes.getOrDefault(idDoCampeao, "Desconhecido"));
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
        String ultCooldownStr = "120/100/80";

        if (excecoesCooldowns.containsKey(nomeCampeao)) {
            String[] tempos = excecoesCooldowns.get(nomeCampeao);
            for (int i = 0; i < 4; i++) {
                JSONObject skill = new JSONObject();
                skill.put("tecla", teclas[i]);
                skill.put("tempo", tempos[i]);
                cooldowns.put(skill);
            }
            ultCooldownStr = tempos[3];
        } else {
            try {
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://ddragon.leagueoflegends.com/cdn/" + versaoAtual + "/data/pt_BR/champion/" + nomeCampeao + ".json")).GET().build();
                HttpResponse<String> response = clientNormal.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JSONArray spells = new JSONObject(response.body()).getJSONObject("data").getJSONObject(nomeCampeao).getJSONArray("spells");
                    for (int i = 0; i < 4; i++) {
                        JSONObject skill = new JSONObject();
                        skill.put("tecla", teclas[i]);
                        skill.put("tempo", spells.getJSONObject(i).getString("cooldownBurn"));
                        cooldowns.put(skill);
                    }
                    ultCooldownStr = spells.getJSONObject(3).getString("cooldownBurn");
                }
            } catch (Exception e) {}
        }
        objInimigo.put("habilidades", cooldowns);
        objInimigo.put("ultCooldown", ultCooldownStr);
        return objInimigo;
    }

    public static void carregarDicionarios() {
        try {
            HttpRequest reqVersion = HttpRequest.newBuilder().uri(URI.create("https://ddragon.leagueoflegends.com/api/versions.json")).GET().build();
            versaoAtual = new JSONArray(clientNormal.send(reqVersion, HttpResponse.BodyHandlers.ofString()).body()).getString(0);
            htmlInterface = htmlInterface.replace("VERSAO_DDRAGON", versaoAtual);

            HttpRequest reqCamp = HttpRequest.newBuilder().uri(URI.create("https://ddragon.leagueoflegends.com/cdn/" + versaoAtual + "/data/pt_BR/champion.json")).GET().build();
            JSONObject dados = new JSONObject(clientNormal.send(reqCamp, HttpResponse.BodyHandlers.ofString()).body()).getJSONObject("data");
            for (String key : dados.keySet()) dicionarioCampeoes.put(Integer.parseInt(dados.getJSONObject(key).getString("key")), key);

            HttpRequest reqRunas = HttpRequest.newBuilder().uri(URI.create("https://ddragon.leagueoflegends.com/cdn/" + versaoAtual + "/data/pt_BR/runesReforged.json")).GET().build();
            JSONArray arvores = new JSONArray(clientNormal.send(reqRunas, HttpResponse.BodyHandlers.ofString()).body());
            for (int i = 0; i < arvores.length(); i++) {
                JSONObject arvoreObj = arvores.getJSONObject(i);

                // ENSINANDO O NOME DAS ÁRVORES AO DICIONÁRIO!
                dicionarioRunas.put(arvoreObj.getInt("id"), arvoreObj.getString("name"));

                JSONArray slots = arvoreObj.getJSONArray("slots");
                for (int j = 0; j < slots.length(); j++) {
                    JSONArray runes = slots.getJSONObject(j).getJSONArray("runes");
                    for (int k = 0; k < runes.length(); k++) dicionarioRunas.put(runes.getJSONObject(k).getInt("id"), runes.getJSONObject(k).getString("name"));
                }
            }
        } catch (Exception e) {}
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

// ====================================================================================
// CLASSES VISUAIS DO OVERLAY FLUTUANTE
// ====================================================================================

class CartaoInimigo extends JPanel {
    public CartaoInimigo(String champ, String spell1, int cd1, String spell2, int cd2, int cdUlt,
                         String principal, String secundaria, String status) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
        setOpaque(false);

        String champImg = champ.replace(" ", "").replace("'", "");
        if (champImg.equals("Wukong")) champImg = "MonkeyKing";
        if (champImg.equals("RenataGlasc")) champImg = "Renata";

        String urlFotoChamp = "https://ddragon.leagueoflegends.com/cdn/" + Main.versaoAtual + "/img/champion/" + champImg + ".png";
        String htmlNome = "<html><table cellpadding='0'><tr><td><img src='" + urlFotoChamp + "' width='20' height='20'></td><td style='padding-left:4px;'>" + champ + "</td></tr></table></html>";

        JLabel lblChamp = new JLabel(htmlNome);
        lblChamp.setForeground(new Color(240, 230, 210));
        lblChamp.setFont(new Font("Arial", Font.BOLD, 11));
        lblChamp.setPreferredSize(new Dimension(85, 25));

        BotaoFeitico btnSpell1 = new BotaoFeitico(spell1, cd1);
        BotaoFeitico btnSpell2 = new BotaoFeitico(spell2, cd2);
        BotaoFeitico btnUlt = new BotaoFeitico("Ult", cdUlt);

        String textoRunas = "<html>" +
                "<span style='color:#C8AA6E; font-size:9px;'>🎯 <b>" + principal + "</b></span><br>" +
                "<span style='color:#A09B8C; font-size:8px;'>🔹 " + secundaria + "</span><br>" +
                "<span style='color:#888888; font-size:8px;'>🔸 Status: " + status + "</span>" +
                "</html>";

        JLabel lblRunas = new JLabel(textoRunas);

        add(lblChamp);
        add(btnSpell1);
        add(btnSpell2);
        add(btnUlt);
        add(lblRunas);
    }
}

class BotaoFeitico extends JButton {
    private int tempoTotalRestante;
    private int cooldownInicial;
    private Timer cronometro;
    private String nomeFeitico;
    private Image iconeDdragon;

    public BotaoFeitico(String nomeFeitico, int cooldownSegundos) {
        this.nomeFeitico = nomeFeitico;
        this.cooldownInicial = cooldownSegundos;
        this.tempoTotalRestante = 0;

        setContentAreaFilled(false);
        setText(nomeFeitico);
        setFont(new Font("Arial", Font.BOLD, 10));
        setForeground(new Color(200, 170, 110));
        setFocusPainted(false);
        setMargin(new Insets(0, 0, 0, 0));
        setPreferredSize(new Dimension(65, 25));

        try {
            String imgName = Main.getNomeIconeFeitico(nomeFeitico);
            if (imgName != null) {
                iconeDdragon = new ImageIcon(new java.net.URL("https://ddragon.leagueoflegends.com/cdn/" + Main.versaoAtual + "/img/spell/" + imgName + ".png")).getImage();
            }
        } catch(Exception e){}

        addActionListener(e -> iniciarCooldown());

        cronometro = new Timer(1000, e -> {
            tempoTotalRestante--;
            if (tempoTotalRestante <= 0) {
                cronometro.stop();
                setText(this.nomeFeitico);
                setEnabled(true);
            } else {
                setText(tempoTotalRestante + "s");
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setColor(new Color(10, 20, 40, 220));
        g2.fillRect(0, 0, getWidth(), getHeight());

        if (iconeDdragon != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2.drawImage(iconeDdragon, 0, 0, getWidth(), getHeight(), this);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    private void iniciarCooldown() {
        if (!cronometro.isRunning()) {
            tempoTotalRestante = cooldownInicial;
            setText(tempoTotalRestante + "s");
            setEnabled(false);
            cronometro.start();
        }
    }
}