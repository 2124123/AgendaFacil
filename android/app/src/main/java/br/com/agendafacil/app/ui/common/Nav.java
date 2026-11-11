package br.com.agendafacil.app.ui.common;

import android.app.Activity;
import android.content.Intent;
import br.com.agendafacil.app.data.Session;
import br.com.agendafacil.app.ui.admin.AdminActivity;
import br.com.agendafacil.app.ui.cliente.ClienteActivity;
import br.com.agendafacil.app.ui.profissional.ProfissionalActivity;

public final class Nav {
    private Nav() {}

    /** Abre a tela principal conforme o perfil da sessão e fecha a atual. */
    public static void irParaHome(Activity de) {
        if (Session.get() == null) return;
        String perfil = Session.get().usuario.perfil;
        Class<?> alvo = "PROFISSIONAL".equals(perfil) ? ProfissionalActivity.class
                : "ADMINISTRADOR".equals(perfil) ? AdminActivity.class : ClienteActivity.class;
        Intent i = new Intent(de, alvo);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        de.startActivity(i);
        de.finish();
    }
}
