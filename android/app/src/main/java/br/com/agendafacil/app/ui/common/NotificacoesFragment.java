package br.com.agendafacil.app.ui.common;

import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;

import java.util.List;

/** Central de notificações (novo agendamento, lembrete, cancelamento...). */
public class NotificacoesFragment extends PageFragment {
    private final boolean comFechar;

    public NotificacoesFragment() { this(false); }
    public NotificacoesFragment(boolean comFechar) { this.comFechar = comFechar; }

    @Override protected void montarTopo() {
        LinearLayout linha = Ui.row(c);
        Ui.addW(linha, cabecalho("Central de Notificações", null), 0);
        if (comFechar) {
            TextView x = Ui.title(c, "✕", 20);
            x.setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 4), Ui.dp(c, 8));
            x.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
            linha.addView(x);
        }
        top.addView(linha);
        TextView todas = Ui.semi(c, "Marcar todas como lidas", 13, Ui.PRIMARY);
        todas.setPadding(0, Ui.dp(c, 6), Ui.dp(c, 12), Ui.dp(c, 10));
        todas.setOnClickListener(v -> api(Net.api().marcarTodasLidas(), r -> { reload(); shell().atualizarBadge(); }));
        Ui.addWrap(top, todas, 0);
    }

    @Override protected void reload() {
        fetch(Net.api().notificacoes(), this::desenhar);
    }

    private void desenhar(List<Models.Notificacao> lista) {
        if (lista.isEmpty()) { list.addView(Ui.empty(c, "Você não tem notificações.")); return; }
        for (final Models.Notificacao n : lista) {
            String emoji; int bg;
            switch (n.tipo == null ? "" : n.tipo) {
                case "LEMBRETE": emoji = "⏰"; bg = Ui.CORAL_BG; break;
                case "CANCELAMENTO": emoji = "🚫"; bg = Ui.RED_BG; break;
                case "CONFIRMACAO": emoji = "✅"; bg = Ui.GREEN_BG; break;
                case "REMARCACAO": emoji = "🔄"; bg = Ui.AMBER_BG; break;
                default: emoji = "📅"; bg = Ui.CYAN_BG; break;
            }
            LinearLayout card = Ui.clickCard(c, v -> {
                if (n.lida) return;
                api(Net.api().marcarLida(n.id), r -> { reload(); shell().atualizarBadge(); });
            });
            LinearLayout linha = new LinearLayout(c);
            TextView icone = Ui.body(c, emoji, 18, Ui.TEXT);
            icone.setGravity(Gravity.CENTER);
            android.graphics.drawable.GradientDrawable oval = new android.graphics.drawable.GradientDrawable();
            oval.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            oval.setColor(bg);
            icone.setBackground(oval);
            Ui.addSize(linha, 40, 40, 0, icone);
            LinearLayout txt = Ui.column(c);
            txt.setPadding(Ui.dp(c, 12), 0, 0, 0);
            txt.addView(n.lida ? Ui.body(c, n.mensagem, 14, Ui.TEXT) : Ui.bold(c, n.mensagem, 14, Ui.TEXT));
            Ui.addWrap(txt, Ui.body(c, Fmt.tempoRelativo(n.criadaEm), 12, Ui.MUTED), 4);
            Ui.addW(linha, txt, 0);
            if (!n.lida) {
                android.view.View ponto = new android.view.View(c);
                android.graphics.drawable.GradientDrawable p = new android.graphics.drawable.GradientDrawable();
                p.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                p.setColor(Ui.CORAL);
                ponto.setBackground(p);
                Ui.addSize(linha, 8, 8, 8, ponto);
            }
            card.addView(linha);
            Ui.add(list, card, 10);
        }
    }
}
