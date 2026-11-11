package br.com.agendafacil.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;
import br.com.agendafacil.app.BuildConfig;

/** Endereço da API. Pode ser trocado na tela de login ("Servidor"), sem gerar um novo APK. */
public final class Server {
    private static SharedPreferences prefs;

    private Server() {}

    public static void init(Context c) {
        prefs = c.getSharedPreferences("agenda_server", Context.MODE_PRIVATE);
    }

    /** Endereço salvo pelo usuário ou, se não houver, o padrão definido no build. */
    public static String url() {
        String u = prefs == null ? null : prefs.getString("url", null);
        return u == null || u.isEmpty() ? BuildConfig.API_BASE_URL : u;
    }

    public static boolean personalizado() {
        return prefs != null && prefs.getString("url", null) != null;
    }

    /** Ajusta o que o usuário digitou: acrescenta https:// e a barra final. Retorna null se for inválido. */
    public static String normalizar(String entrada) {
        String u = entrada == null ? "" : entrada.trim();
        if (u.isEmpty()) return null;
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://" + u;
        if (!u.endsWith("/")) u = u + "/";
        return Patterns.WEB_URL.matcher(u).matches() ? u : null;
    }

    public static void salvar(String urlNormalizada) {
        prefs.edit().putString("url", urlNormalizada).apply();
        Net.reiniciar();
    }

    public static void restaurar() {
        prefs.edit().remove("url").apply();
        Net.reiniciar();
    }
}
