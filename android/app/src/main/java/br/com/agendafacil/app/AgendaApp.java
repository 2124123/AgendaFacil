package br.com.agendafacil.app;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;
import br.com.agendafacil.app.data.Server;
import br.com.agendafacil.app.data.Session;

public class AgendaApp extends Application {
    private static AgendaApp instancia;

    @Override public void onCreate() {
        super.onCreate();
        instancia = this;
        Server.init(this);
        Session.init(this);
    }

    /** Chamado quando o token expira: limpa a pilha e abre o login. */
    public static void irParaLogin() {
        if (instancia == null) return;
        Intent i = new Intent(instancia, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        instancia.startActivity(i);
        Toast.makeText(instancia, "Sessão expirada. Entre novamente.", Toast.LENGTH_LONG).show();
    }
}
