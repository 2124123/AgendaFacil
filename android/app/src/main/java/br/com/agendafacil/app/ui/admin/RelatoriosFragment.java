package br.com.agendafacil.app.ui.admin;

import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.common.PageFragment;

import java.time.LocalDate;

/** RF002: relatório de agendamentos por período. */
public class RelatoriosFragment extends PageFragment {
    private static final String[] CHAVES = {"hoje", "7", "mes", "30"};
    private static final String[] ROTULOS = {"Hoje", "7 dias", "Mês atual", "30 dias"};
    private String periodo = "mes";
    private LinearLayout filtros;

    @Override protected void montarTopo() {
        LinearLayout cab = Ui.row(c);
        TextView voltar = Ui.title(c, "‹", 30);
        voltar.setPadding(0, 0, Ui.dp(c, 14), 0);
        voltar.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        cab.addView(voltar);
        cab.addView(Ui.title(c, "Relatórios de Desempenho", 20));
        cab.setPadding(0, Ui.dp(c, 8), 0, 0);
        top.addView(cab);
        HorizontalScrollView hs = new HorizontalScrollView(c);
        hs.setHorizontalScrollBarEnabled(false);
        filtros = Ui.row(c);
        hs.addView(filtros);
        Ui.add(top, hs, 12);
        pintar();
    }

    private void pintar() {
        filtros.removeAllViews();
        for (int i = 0; i < CHAVES.length; i++) {
            final String k = CHAVES[i];
            boolean sel = k.equals(periodo);
            TextView t = Ui.semi(c, ROTULOS[i], 13, sel ? Ui.WHITE : Ui.TITLE);
            t.setBackground(sel ? Ui.shape(c, Ui.PRIMARY, 50, 0) : Ui.ripple(c, Ui.WHITE, 50, Ui.BORDER));
            t.setPadding(Ui.dp(c, 14), Ui.dp(c, 8), Ui.dp(c, 14), Ui.dp(c, 8));
            t.setOnClickListener(v -> { periodo = k; pintar(); list.removeAllViews(); reload(); });
            Ui.addWrap(filtros, t, 0);
            if (i < CHAVES.length - 1) filtros.addView(Ui.space(c, 1), new LinearLayout.LayoutParams(Ui.dp(c, 8), 1));
        }
    }

    @Override protected void reload() {
        LocalDate fim = Fmt.hoje();
        LocalDate ini;
        switch (periodo) {
            case "hoje": ini = fim; break;
            case "7": ini = fim.minusDays(6); break;
            case "30": ini = fim.minusDays(29); break;
            default: ini = fim.withDayOfMonth(1);
        }
        fetch(Net.api().adminRelatorio(ini.toString(), fim.toString()), this::desenhar);
    }

    private void desenhar(Models.Relatorio r) {
        list.addView(Ui.body(c, Fmt.curta(Fmt.data(r.inicio)) + " a " + Fmt.curta(Fmt.data(r.fim)), 13, Ui.MUTED));
        LinearLayout l1 = Ui.row(c);
        Ui.addW(l1, resumo("Total", r.total, Ui.PRIMARY), 0);
        Ui.addW(l1, resumo("Realizados", r.realizados, Ui.GREEN_FG), 10);
        Ui.add(list, l1, 10);
        LinearLayout l2 = Ui.row(c);
        Ui.addW(l2, resumo("Confirmados", r.confirmados, Ui.PRIMARY), 0);
        Ui.addW(l2, resumo("Pendentes", r.pendentes, Ui.AMBER_FG), 10);
        Ui.addW(l2, resumo("Cancelados", r.cancelados, Ui.RED_FG), 10);
        Ui.add(list, l2, 10);

        LinearLayout rec = Ui.card(c);
        rec.addView(Ui.body(c, "Receita dos atendimentos realizados", 12, Ui.MUTED));
        rec.addView(Ui.text(c, Fmt.brl(r.receita), 26, Ui.PRIMARY, 700, true));
        Ui.add(list, rec, 10);

        Ui.add(list, Ui.sectionTitle(c, "Agendamentos do período"), 22);
        if (r.itens.isEmpty()) list.addView(Ui.empty(c, "Nenhum agendamento no período."));
        for (Models.Agendamento a : r.itens) {
            LinearLayout card = Ui.card(c);
            LinearLayout cab = Ui.row(c);
            Ui.addW(cab, Ui.bold(c, Fmt.diaMes(Fmt.data(a.data)) + " • " + a.horaInicio, 13, Ui.TEXT), 0);
            cab.addView(Ui.statusChip(c, a.status));
            card.addView(cab);
            Ui.add(card, Ui.semi(c, a.servico, 15, Ui.TEXT), 4);
            card.addView(Ui.body(c, a.cliente + " → " + a.profissional, 12, Ui.MUTED));
            card.addView(Ui.body(c, Fmt.brl(a.valor), 12, Ui.PRIMARY));
            Ui.add(list, card, 8);
        }
    }

    private LinearLayout resumo(String titulo, long valor, int cor) {
        LinearLayout card = Ui.card(c);
        card.addView(Ui.body(c, titulo, 11, Ui.MUTED));
        card.addView(Ui.text(c, String.valueOf(valor), 22, cor, 700, true));
        return card;
    }
}
