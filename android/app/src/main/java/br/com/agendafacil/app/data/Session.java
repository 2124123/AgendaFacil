package br.com.agendafacil.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

/** Guarda o token JWT e o usuário logado (SharedPreferences privado do app). */
public final class Session {
    private static final Gson GSON = new Gson();
    private static SharedPreferences prefs;
    private static Models.SessionData atual;

    private Session() {}

    public static void init(Context c) {
        prefs = c.getSharedPreferences("agenda_session", Context.MODE_PRIVATE);
        String json = prefs.getString("session", null);
        if (json != null) {
            try { atual = GSON.fromJson(json, Models.SessionData.class); } catch (Exception e) { atual = null; }
        }
    }

    public static Models.SessionData get() { return atual; }

    public static String token() { return atual == null ? null : atual.token; }

    public static void save(Models.AuthRes r) {
        Models.SessionData d = new Models.SessionData();
        d.token = r.token; d.usuario = r.usuario;
        d.estabelecimento = r.estabelecimento == null ? "Agenda Fácil" : r.estabelecimento;
        persistir(d);
    }

    public static void updateUser(Models.Usuario u) {
        if (atual == null) return;
        atual.usuario = u;
        persistir(atual);
    }

    public static void clear() { persistir(null); }

    private static void persistir(Models.SessionData d) {
        atual = d;
        SharedPreferences.Editor e = prefs.edit();
        if (d == null) e.remove("session"); else e.putString("session", GSON.toJson(d));
        e.apply();
    }
}
