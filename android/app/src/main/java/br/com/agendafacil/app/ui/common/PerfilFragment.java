package br.com.agendafacil.app.ui.common;

import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import br.com.agendafacil.app.AgendaApp;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.data.Session;
import br.com.agendafacil.app.ui.Btn;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;

/** RF001: manter os dados do próprio cadastro. Usada pelos três perfis. */
public class PerfilFragment extends PageFragment {
    private final boolean comNotificacoes;

    public PerfilFragment() { this(false); }

    public PerfilFragment(boolean comNotificacoes) {
        this.comNotificacoes = comNotificacoes;
        this.atualizavel = false;
    }

    @Override protected void montarTopo() { top.addView(cabecalho("Perfil", null)); }

    @Override protected void reload() {
        list.removeAllViews();
        final Models.Usuario u = Session.get().usuario;

        LinearLayout linha = Ui.row(c);
        linha.addView(Ui.avatar(c, u.nome, 64));
        LinearLayout info = Ui.column(c);
        info.setPadding(Ui.dp(c, 14), 0, 0, 0);
        info.addView(Ui.text(c, u.nome, 18, Ui.TEXT, 700, true));
        info.addView(Ui.body(c, u.email, 13, Ui.MUTED));
        Ui.addWrap(info, Ui.pill(c, Fmt.perfilLabel(u.perfil), Ui.CYAN_BG, Ui.PRIMARY), 6);
        linha.addView(info);
        list.addView(linha);

        final EditText nome = Ui.field(c, "Nome", Ui.TEXTO());
        nome.setText(u.nome);
        Ui.add(list, Ui.labeled(c, "Nome", nome), 24);
        final EditText tel = Ui.field(c, "Telefone", Ui.TELEFONE());
        tel.setText(u.telefone == null ? "" : u.telefone);
        Ui.add(list, Ui.labeled(c, "Telefone", tel), 14);
        EditText mail = Ui.field(c, "", Ui.EMAIL());
        mail.setText(u.email);
        mail.setEnabled(false);
        mail.setAlpha(0.6f);
        Ui.add(list, Ui.labeled(c, "E-mail", mail), 14);

        final Btn salvar = Ui.button(c, "Salvar alterações", Ui.PRIMARIO, null);
        salvar.setOnClickListener(v -> {
            String n = nome.getText().toString().trim();
            if (n.isEmpty()) { Ui.toast(c, "Informe o nome"); return; }
            String t = tel.getText().toString().trim();
            salvar.setOcupado(true);
            api(Net.api().atualizarPerfil(new Models.PerfilUpdate(n, t.isEmpty() ? null : t)), r -> {
                Session.updateUser(r);
                salvar.setOcupado(false);
                Ui.toast(c, "Dados atualizados");
                reload();
            }, m -> { salvar.setOcupado(false); Ui.toast(c, m); });
        });
        Ui.add(list, salvar, 20);

        if (comNotificacoes) {
            Ui.add(list, Ui.button(c, "Notificações", Ui.SUAVE, v -> shell().abrir(new NotificacoesFragment(true))), 12);
        }
        Ui.add(list, Ui.button(c, "Sair da conta", Ui.PERIGO, v -> new AlertDialog.Builder(requireContext())
                .setTitle("Sair da conta").setMessage("Deseja realmente sair?")
                .setPositiveButton("Sair", (d, w) -> { Session.clear(); AgendaApp.irParaLogin(); })
                .setNegativeButton("Voltar", null).show()), 12);
    }
}
