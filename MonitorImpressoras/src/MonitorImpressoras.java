import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.Desktop;
import java.io.File;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MonitorImpressoras {

    static String comunidadeSNMP = "public";
    static String OID_MODELO = "1.3.6.1.2.1.1.1.0";
    static String OID_DESC = "1.3.6.1.2.1.43.11.1.1.6.1.";
    static String OID_MAX = "1.3.6.1.2.1.43.11.1.1.8.1.";
    static String OID_ATUAL = "1.3.6.1.2.1.43.11.1.1.9.1.";
    static String OID_SERIAL = "1.3.6.1.2.1.43.5.1.1.17.1";

    static String htmlInterface = """
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <title>Monitor de Infraestrutura MUAHAHAHA</title>
            <style>
                body { background-color: #0d1117; color: #c9d1d9; font-family: sans-serif; padding: 20px; }
                h1 { border-bottom: 2px solid #58a6ff; color: #58a6ff; }
                #painel { display: flex; flex-wrap: wrap; gap: 20px; margin-top: 20px; }
                .card { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 15px; width: 300px; }
                .nome { font-size: 18px; font-weight: bold; color: #fff; }
                .modelo { font-size: 12px; color: #58a6ff; margin-bottom: 10px; }
                .status-off { color: #f85149; font-weight: bold; font-size: 14px; margin-top: 10px; }
                .sup { margin-bottom: 10px; font-size: 12px; }
                .barra-f { background: #21262d; height: 10px; border-radius: 5px; overflow: hidden; }
                .barra-p { background: #58a6ff; height: 100%; }
                .detalhes { font-size: 11px; color: #8b949e; margin-top: 10px; border-top: 1px solid #30363d; padding-top: 5px;}
            </style>
        </head>
        <body>
            <h1>MONITORAMENTO DE INFRAESTRUTURA</h1>
            <div id="painel">Carregando dispositivos...</div>
            <script>
                async function carregar() {
                    const res = await fetch('/api/impressoras');
                    const dados = await res.json();
                    const painel = document.getElementById('painel');
                    painel.innerHTML = '';
                    
                    if (dados.length === 0) {
                        painel.innerHTML = '<p>Nenhuma impressora listada no arquivo impressoras.txt</p>';
                        return;
                    }

                    dados.forEach(imp => {
                        let html = `<div class="card">
                            <div class="nome">🏢 ${imp.local}</div>
                            <div class="modelo">🔍 ${imp.modelo}</div>`;
                        
                        if (imp.erro) {
                            html += `<div class="status-off">⚠️ DISPOSITIVO OFFLINE</div>`;
                        } else {
                            imp.suprimentos.forEach(s => {
                                html += `<div class="sup">
                                    <div>${s.nome} (${s.porcentagem}%)</div>
                                    <div class="barra-f"><div class="barra-p" style="width:${s.porcentagem}%"></div></div>
                                </div>`;
                            });
                        }
                        html += `<div class="detalhes">IP: ${imp.ip} | S/N: ${imp.serial}</div></div>`;
                        painel.innerHTML += html;
                    });
                }
                carregar();
                setInterval(carregar, 30000);
            </script>
        </body>
        </html>
        """;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", e -> {
            byte[] b = htmlInterface.getBytes("UTF-8");
            e.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            e.sendResponseHeaders(200, b.length);
            e.getResponseBody().write(b);
            e.getResponseBody().close();
        });

        server.createContext("/api/impressoras", e -> {
            JSONArray array = new JSONArray();
            try {
                File f = new File("impressoras.txt");
                if (f.exists()) {
                    List<String> linhas = Files.readAllLines(f.toPath());
                    for (String l : linhas) {
                        if (!l.contains(",")) continue;
                        String[] p = l.split(",");
                        String ip = p[0].trim();
                        String local = p[1].trim();

                        JSONObject json = new JSONObject();
                        json.put("ip", ip);
                        json.put("local", local);

                        try {
                            JSONObject dados = consultarSNMP(ip);
                            json.put("erro", dados.getBoolean("offline"));
                            json.put("modelo", dados.getString("modelo"));
                            json.put("serial", dados.getString("serial"));
                            json.put("suprimentos", dados.getJSONArray("suprimentos"));
                        } catch (Exception ex) {
                            json.put("erro", true);
                            json.put("modelo", "Não alcançável");
                            json.put("serial", "---");
                        }
                        array.put(json);
                    }
                }
            } catch (Exception ex) {}

            byte[] b = array.toString().getBytes("UTF-8");
            e.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            e.sendResponseHeaders(200, b.length);
            e.getResponseBody().write(b);
            e.getResponseBody().close();
        });

        server.start();
        System.out.println("Servidor rodando...");
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI("http://localhost:8080"));
    }

    public static JSONObject consultarSNMP(String ip) throws Exception {
        JSONObject r = new JSONObject();
        TransportMapping<? extends Address> t = new DefaultUdpTransportMapping();
        t.listen();
        Snmp snmp = new Snmp(t);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(comunidadeSNMP));
        target.setAddress(GenericAddress.parse("udp:" + ip + "/161"));
        target.setRetries(0); // Não tenta de novo se falhar (mais rápido)
        target.setTimeout(1000); // Espera apenas 1 segundo
        target.setVersion(SnmpConstants.version2c);

        String mod = fazerGet(snmp, target, OID_MODELO);

        // SE NÃO CONSEGUIR O MODELO, JÁ PARA AQUI (ESTÁ OFFLINE)
        if (mod == null || mod.equals("Null")) {
            r.put("offline", true);
            r.put("modelo", "Offline / Timeout");
            r.put("serial", "---");
            r.put("suprimentos", new JSONArray());
        } else {
            r.put("offline", false);
            r.put("modelo", mod.split(";")[0]);
            r.put("serial", fazerGet(snmp, target, OID_SERIAL));

            JSONArray sups = new JSONArray();
            for (int i = 1; i <= 6; i++) {
                String nome = fazerGet(snmp, target, OID_DESC + i);
                if (nome == null || nome.equals("Null")) continue;

                String mStr = fazerGet(snmp, target, OID_MAX + i);
                String aStr = fazerGet(snmp, target, OID_ATUAL + i);

                if (!mStr.equals("Null") && !aStr.equals("Null")) {
                    int max = Integer.parseInt(mStr);
                    int atual = Integer.parseInt(aStr);
                    if (max > 0) {
                        JSONObject s = new JSONObject();
                        s.put("nome", nome.replace("\"", ""));
                        s.put("porcentagem", (atual * 100) / max);
                        sups.put(s);
                    }
                }
            }
            r.put("suprimentos", sups);
        }
        snmp.close();
        return r;
    }

    private static String fazerGet(Snmp s, CommunityTarget t, String oid) {
        try {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);
            ResponseEvent ev = s.send(pdu, t, null);
            if (ev != null && ev.getResponse() != null) {
                return ev.getResponse().get(0).getVariable().toString();
            }
        } catch (Exception e) {}
        return "Null";
    }
}