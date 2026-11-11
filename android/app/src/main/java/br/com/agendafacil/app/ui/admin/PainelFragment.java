package br.com.agendafacil.app.ui.admin;

import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.com.agendafacil.app.R;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.common.PageFragment;

import java.time.LocalDate;

/** Painel do administrador: resumo do dia, gráfico da semana, equipe e ações rápidas. */
public class PainelFragment extends PageFragment {
    private TextView nomeEstab;

    @Override protected void montarTopo() {
        LocalDate h = Fmt.hoje();
        LinearLayout cab = Ui.row(c);
        cab.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 8));
        LinearLayout txt = Ui.column(c);
        nomeEstab = Ui.title(c, "Painel", 24);
        txt.addView(nomeEstab);
        String dia = Fmt.diaSemanaLongo(h);
        int traco = dia.indexOf('-');
        if (traco > 0) dia = dia.substring(0, traco);
        String mes = Fmt.mesCurto(h);
        txt.addView(Ui.body(c, "Hoje: " + dia + ", " + h.getDayOfMonth() + " " + mes.charAt(0) + mes.substring(1).toLowerCase(), 13, Ui.MUTED));
        Ui.addW(cab, txt, 0);
        cab.addView(Ui.pill(c, "ADMIN", Ui.CYAN_BG, Ui.PRIMARY));
        top.addView(cab);
    }

    @Override protected void reload() {
        fetch(Net.api().adminDashboard(), this::desenhar);
    }

    private void desenhar(Models.Dashboard d) {
        nomeEstab.setText(d.estabelecimento);
        Ui.add(list, Ui.sectionTitle(c, "Resumo do Dia"), 4);

        LinearLayout resumo = Ui.row(c);
        resumo.setGravity(Gravity.TOP);
        LinearLayout a = Ui.card(c);
        a.addView(Ui.body(c, "Agendamentos Hoje", 12, Ui.MUTED));
        a.addView(Ui.text(c, String.valueOf(d.agendamentosHoje), 28, Ui.PRIMARY, 700, true));
        Integer v = d.variacaoPercentual;
        a.addView(Ui.body(c, v == null ? "Sem dados de ontem" : (v >= 0 ? "+" : "") + v + "% vs ontem", 12,
                v == null ? Ui.MUTED : v >= 0 ? Ui.GREEN_FG : Ui.RED_FG));
        Ui.addW(resumo, a, 0);
        LinearLayout o = Ui.card(c);
        o.addView(Ui.body(c, "Taxa de Ocupação", 12, Ui.MUTED));
        o.addView(Ui.text(c, d.taxaOcupacao + "%", 28, Ui.PRIMARY, 700, true));
        boolean bom = d.taxaOcupacao >= 70;
        o.addView(Ui.body(c, bom ? "Bom desempenho" : "Pode melhorar", 12, bom ? Ui.GREEN_FG : Ui.AMBER_FG));
        Ui.addW(resumo, o, 12);
        Ui.add(list, resumo, 10);

        Ui.add(list, Ui.sectionTitle(c, "Agendamentos na semana"), 22);
        LinearLayout grafico = Ui.card(c);
        LinearLayout barras = new LinearLayout(c);
        barras.setGravity(Gravity.BOTTOM);
        int max = 1;
        for (int x : d.semana) max = Math.max(max, x);
        String[] rotulos = {"S", "T", "Q", "Q", "S", "S", "D"};
        for (int i = 0; i < d.semana.size(); i++) {
            int val = d.semana.get(i);
            LinearLayout col = Ui.column(c);
            col.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            col.addView(Ui.body(c, String.valueOf(val), 10, Ui.MUTED));
            android.view.View barra = new android.view.View(c);
            float alt = Math.max(4f, val * 80f / max);
            android.graphics.drawable.GradientDrawable g = new android.graphics.drawable.GradientDrawable();
            g.setColor(val == max && val > 0 ? Ui.CORAL : Ui.PRIMARY);
            float r = Ui.dp(c, 6);
            g.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
            barra.setBackground(g);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(c, 24), Ui.dp(c, alt));
            lp.topMargin = Ui.dp(c, 2);
            col.addView(barra, lp);
            Ui.addWrap(col, Ui.body(c, rotulos[i], 11, Ui.MUTED), 4);
            Ui.addW(barras, col, 0);
        }
        grafico.addView(barras, new LinearLayout.LayoutParams(Ui.MATCH, Ui.dp(c, 130)));
        Ui.add(list, grafico, 10);

        Ui.add(list, Ui.sectionTitle(c, "Profissionais Ativos"), 22);
        if (d.profissionais.isEmpty()) list.addView(Ui.empty(c, "Nenhum profissional ativo."));
        for (Models.ProfissionalResumo p : d.profissionais) {
            LinearLayout card = Ui.card(c);
            LinearLayout linha = Ui.row(c);
            linha.addView(Ui.avatar(c, p.nome, 40));
            LinearLayout txt = Ui.column(c);
            txt.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 8), 0);
            txt.addView(Ui.semi(c, p.nome, 15, Ui.TEXT));
            txt.addView(Ui.body(c, p.especialidade, 12, Ui.MUTED));
            Ui.addW(linha, txt, 0);
            linha.addView(Ui.pill(c, p.agendamentosHoje + " agend.", Ui.CYAN_BG, Ui.PRIMARY));
            card.addView(linha);
            Ui.add(list, card, 8);
        }

        Ui.add(list, Ui.sectionTitle(c, "Ações Rápidas"), 22);
        acao("Gerenciar Serviços", v -> shell().selecionarAba(R.id.nav_servicos));
        acao("Gerenciar Profissionais", v -> shell().selecionarAba(R.id.nav_equipe));
        acao("Relatórios de Desempenho", v -> shell().abrir(new RelatoriosFragment()));
    }

    private void acao(String texto, android.view.View.OnClickListener ao) {
        LinearLayout card = Ui.clickCard(c, ao);
        LinearLayout linha = Ui.row(c);
        Ui.addW(linha, Ui.semi(c, texto, 15, Ui.TEXT), 0);
        linha.addView(Ui.title(c, "›", 22));
        card.addView(linha);
        Ui.add(list, card, 8);
    }
}
