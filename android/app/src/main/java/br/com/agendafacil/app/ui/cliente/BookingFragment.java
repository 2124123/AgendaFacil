package br.com.agendafacil.app.ui.cliente;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.provider.CalendarContract;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.com.agendafacil.app.R;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.ui.Btn;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.Widgets;
import br.com.agendafacil.app.ui.common.BackAware;
import br.com.agendafacil.app.ui.common.PageFragment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** RF004: agendamento em 4 etapas (Serviço → Profissional → Data/Hora → Confirmação). */
public class BookingFragment extends PageFragment implements BackAware {
    private BookingState b;

    @Override protected void montarTopo() {
        atualizavel = false;
        srl.setEnabled(false);
    }

    @Override public boolean onBack() {
        if (b != null && b.etapa < 4 && b.voltar()) { reload(); return true; }
        return false;
    }

    @Override protected void reload() {
        b = ((ClienteActivity) requireActivity()).booking;
        top.removeAllViews();
        list.removeAllViews();
        list.setGravity(Gravity.START);
        if (b.etapa == 4 && b.resultado != null) { desenharConfirmacao(b.resultado); return; }

        LinearLayout cab = Ui.row(c);
        if (b.etapa > 1) {
            TextView voltar = Ui.title(c, "‹", 30);
            voltar.setPadding(0, 0, Ui.dp(c, 14), 0);
            voltar.setOnClickListener(v -> onBack());
            cab.addView(voltar);
        }
        cab.addView(Ui.title(c, "Novo Agendamento", 20));
        cab.setPadding(0, Ui.dp(c, 8), 0, 0);
        top.addView(cab);
        Ui.add(top, estagios(), 14);

        LinearLayout chips = Ui.row(c);
        if (b.servico != null) chips.addView(chip("✂️ " + b.servico.nome));
        if (b.profissional != null) { android.view.View esp = new android.view.View(c); Ui.addSize(chips, 8, 1, 0, esp); chips.addView(chip("👤 " + b.profissional.nome)); }
        if (chips.getChildCount() > 0) Ui.add(top, chips, 12);

        if (b.etapa == 1) etapaServico();
        else if (b.etapa == 2) etapaProfissional();
        else etapaDataHora();
    }

    private LinearLayout estagios() {
        LinearLayout l = Ui.row(c);
        for (int n = 1; n <= 4; n++) {
            int fundo = n < b.etapa ? Ui.PRIMARY : n == b.etapa ? Ui.CORAL : Ui.WHITE;
            int cor = n <= b.etapa ? Ui.WHITE : Ui.MUTED;
            TextView t = Ui.bold(c, String.valueOf(n), 12, cor);
            t.setGravity(Gravity.CENTER);
            android.graphics.drawable.GradientDrawable oval = new android.graphics.drawable.GradientDrawable();
            oval.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            oval.setColor(fundo);
            if (n > b.etapa) oval.setStroke(Ui.dp(c, 1), Ui.BORDER);
            t.setBackground(oval);
            Ui.addSize(l, 24, 24, 0, t);
            if (n < 4) {
                android.view.View barra = new android.view.View(c);
                barra.setBackgroundColor(n < b.etapa ? Ui.PRIMARY : Ui.BORDER);
                Ui.addSize(l, 24, 2, 0, barra);
            }
        }
        return l;
    }

    private TextView chip(String texto) {
        TextView t = Ui.semi(c, texto, 12, Ui.TITLE);
        t.setBackground(Ui.shape(c, Ui.WHITE, 50, 0));
        t.setPadding(Ui.dp(c, 12), Ui.dp(c, 6), Ui.dp(c, 12), Ui.dp(c, 6));
        return t;
    }

    // ------------------------------------------------------------------ etapa 1

    private void etapaServico() {
        Ui.add(top, Ui.sectionTitle(c, "Escolha o serviço"), 16);
        HorizontalScrollView hs = new HorizontalScrollView(c);
        hs.setHorizontalScrollBarEnabled(false);
        LinearLayout filtros = Ui.row(c);
        filtros.addView(filtro("Todos", null));
        for (String cat : HomeFragment.CATEGORIAS) { Ui.addSize(filtros, 8, 1, 0, new android.view.View(c)); filtros.addView(filtro(cat, cat)); }
        hs.addView(filtros);
        Ui.add(top, hs, 10);

        fetch(Net.api().servicos(null, b.categoria), (List<Models.Servico> r) -> {
            if (r.isEmpty()) list.addView(Ui.empty(c, "Nenhum serviço disponível nesta categoria."));
            for (final Models.Servico s : r) {
                LinearLayout card = Ui.clickCard(c, v -> { b.escolherServico(s); reload(); });
                card.addView(HomeFragment.linhaServico(c, s));
                Ui.add(list, card, 10);
            }
        });
    }

    private TextView filtro(String texto, final String categoria) {
        boolean sel = (categoria == null && b.categoria == null) || (categoria != null && categoria.equals(b.categoria));
        TextView t = Ui.semi(c, texto, 13, sel ? Ui.WHITE : Ui.TITLE);
        t.setBackground(sel ? Ui.shape(c, Ui.PRIMARY, 50, 0) : Ui.ripple(c, Ui.WHITE, 50, Ui.BORDER));
        t.setPadding(Ui.dp(c, 14), Ui.dp(c, 8), Ui.dp(c, 14), Ui.dp(c, 8));
        t.setOnClickListener(v -> { b.categoria = categoria; reload(); });
        return t;
    }

    // ------------------------------------------------------------------ etapa 2

    private void etapaProfissional() {
        Ui.add(top, Ui.sectionTitle(c, "Escolha o profissional"), 16);
        fetch(Net.api().profissionais(b.servico.id), (List<Models.Profissional> r) -> {
            if (r.isEmpty()) list.addView(Ui.empty(c, "Nenhum profissional disponível para este serviço."));
            for (final Models.Profissional p : r) {
                LinearLayout card = Ui.clickCard(c, v -> { b.escolherProfissional(p); reload(); });
                LinearLayout linha = Ui.row(c);
                linha.addView(Ui.avatar(c, p.nome, 48));
                LinearLayout txt = Ui.column(c);
                txt.setPadding(Ui.dp(c, 12), 0, 0, 0);
                txt.addView(Ui.semi(c, p.nome, 15, Ui.TEXT));
                txt.addView(Ui.body(c, p.especialidade, 12, Ui.MUTED));
                Ui.addW(linha, txt, 0);
                linha.addView(Ui.title(c, "›", 24));
                card.addView(linha);
                Ui.add(list, card, 10);
            }
        });
    }

    // ------------------------------------------------------------------ etapa 3

    private void etapaDataHora() {
        final Btn continuar = Ui.button(c, "Continuar", Ui.PRIMARIO, null);
        continuar.setHabilitado(b.data != null && b.hora != null);
        final Widgets.SlotPicker picker = new Widgets.SlotPicker(c, b.profissional.id, b.servico.id, null, b.data, b.hora,
                (data, hora) -> { b.data = data; b.hora = hora; continuar.setHabilitado(data != null && hora != null); });
        Ui.add(list, picker, 12);
        continuar.setOnClickListener(v -> {
            if (picker.data() == null || picker.hora() == null) return;
            continuar.setOcupado(true);
            api(Net.api().criarAgendamento(new Models.AgendamentoReq(b.profissional.id, b.servico.id, picker.data().toString(), picker.hora())), r -> {
                b.resultado = r;
                b.etapa = 4;
                reload();
            }, m -> {
                continuar.setOcupado(false);
                continuar.setHabilitado(false);
                Ui.toast(c, m);
                picker.atualizar();   // horário pode ter sido ocupado por outro cliente (RN002)
            });
        });
        Ui.add(list, continuar, 24);
    }

    // ------------------------------------------------------------------ etapa 4

    private void desenharConfirmacao(final Models.Agendamento a) {
        list.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView ok = Ui.body(c, "✓", 36, Ui.GREEN_FG);
        ok.setGravity(Gravity.CENTER);
        android.graphics.drawable.GradientDrawable oval = new android.graphics.drawable.GradientDrawable();
        oval.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        oval.setColor(Ui.GREEN_BG);
        ok.setBackground(oval);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(c, 72), Ui.dp(c, 72));
        lp.topMargin = Ui.dp(c, 32);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        list.addView(ok, lp);
        boolean confirmado = "CONFIRMADO".equals(a.status);
        TextView t = Ui.title(c, confirmado ? "Agendamento Confirmado!" : "Agendamento Solicitado!", 24);
        t.setTextColor(Ui.TITLE);
        t.setGravity(Gravity.CENTER);
        Ui.add(list, t, 16);
        if (!confirmado) {
            TextView s = Ui.body(c, "Aguardando a confirmação do profissional.", 13, Ui.MUTED);
            s.setGravity(Gravity.CENTER);
            Ui.add(list, s, 4);
        }

        LinearLayout card = Ui.card(c);
        card.addView(Ui.body(c, "SERVIÇO", 11, Ui.MUTED));
        card.addView(Ui.text(c, a.servico, 18, Ui.TEXT, 700, true));
        Ui.add(card, Ui.divider(c), 12);
        card.addView(linha("Profissional", a.profissional), margem(12));
        card.addView(linha("Data", Fmt.curta(Fmt.data(a.data))), margem(6));
        card.addView(linha("Horário", a.horaInicio), margem(6));
        card.addView(Ui.body(c, "Local", 14, Ui.MUTED), margem(6));
        card.addView(Ui.bold(c, a.local == null ? "" : a.local, 14, Ui.TEXT));
        if (a.endereco != null && !a.endereco.isEmpty()) card.addView(Ui.body(c, a.endereco, 12, Ui.MUTED));
        Ui.add(card, Ui.divider(c), 12);
        LinearLayout valor = Ui.row(c);
        Ui.addW(valor, Ui.bold(c, "Valor", 15, Ui.TEXT), 0);
        valor.addView(Ui.text(c, Fmt.brl(a.valor), 20, Ui.PRIMARY, 700, true));
        Ui.add(card, valor, 12);
        Ui.add(list, card, 24);

        Ui.add(list, Ui.button(c, "Adicionar ao Calendário", Ui.CONTORNO, v -> adicionarAoCalendario(a)), 24);
        Ui.add(list, Ui.button(c, "Ver Meus Agendamentos", Ui.PRIMARIO, v -> {
            b.limpar();
            shell().selecionarAba(R.id.nav_meus);
        }), 12);
    }

    private LinearLayout linha(String rotulo, String valor) {
        LinearLayout l = Ui.row(c);
        Ui.addW(l, Ui.body(c, rotulo, 14, Ui.MUTED), 0);
        l.addView(Ui.bold(c, valor, 14, Ui.TEXT));
        return l;
    }

    private LinearLayout.LayoutParams margem(float topoDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        lp.topMargin = Ui.dp(c, topoDp);
        return lp;
    }

    private void adicionarAoCalendario(Models.Agendamento a) {
        LocalDate d = Fmt.data(a.data);
        long ini = LocalDateTime.of(d, LocalTime.parse(a.horaInicio)).atZone(Fmt.ZONA).toInstant().toEpochMilli();
        long fim = LocalDateTime.of(d, LocalTime.parse(a.horaFim)).atZone(Fmt.ZONA).toInstant().toEpochMilli();
        Intent i = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, ini)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fim)
                .putExtra(CalendarContract.Events.TITLE, a.servico + " - " + (a.local == null ? "Agenda Fácil" : a.local))
                .putExtra(CalendarContract.Events.DESCRIPTION, "Profissional: " + a.profissional)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, a.endereco == null ? "" : a.endereco);
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Ui.toast(c, "Nenhum aplicativo de calendário encontrado");
        }
    }
}
