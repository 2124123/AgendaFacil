package br.com.agendafacil.app.ui.profissional;

import android.widget.LinearLayout;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.common.PageFragment;

import java.util.List;

public class ClientesFragment extends PageFragment {
    @Override protected void montarTopo() { top.addView(cabecalho("Clientes", "Clientes com atendimentos na sua agenda")); }

    @Override protected void reload() {
        fetch(Net.api().clientesAtendidos(), (List<Models.ClienteResumo> r) -> {
            if (r.isEmpty()) list.addView(Ui.empty(c, "Você ainda não possui clientes."));
            for (Models.ClienteResumo cli : r) {
                LinearLayout card = Ui.card(c);
                LinearLayout linha = Ui.row(c);
                linha.addView(Ui.avatar(c, cli.nome, 46));
                LinearLayout txt = Ui.column(c);
                txt.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 8), 0);
                txt.addView(Ui.bold(c, cli.nome, 15, Ui.TEXT));
                txt.addView(Ui.body(c, cli.telefone != null ? cli.telefone : cli.email, 12, Ui.MUTED));
                txt.addView(Ui.body(c, cli.ultimoAtendimento != null
                        ? "Último atendimento: " + Fmt.curta(Fmt.data(cli.ultimoAtendimento)) : "Sem atendimento realizado", 12, Ui.MUTED));
                Ui.addW(linha, txt, 0);
                linha.addView(Ui.pill(c, cli.atendimentos + " agend.", Ui.CYAN_BG, Ui.PRIMARY));
                card.addView(linha);
                Ui.add(list, card, 10);
            }
        });
    }
}
