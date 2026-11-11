package br.com.agendafacil.app.ui.profissional;

import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.data.Session;
import br.com.agendafacil.app.ui.Btn;
import br.com.agendafacil.app.ui.Dlg;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.Widgets;
import br.com.agendafacil.app.ui.common.PageFragment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** RF006/RF007: agenda do profissional (RN004), bloqueio de horário, confirmar e registrar atendimento (RN006). */
public class AgendaFragment extends PageFragment {
    private LocalDate data = Fmt.hoje();
    private boolean semana = false;
    private TextView subtitulo, rotuloLista, botaoSemana;

    @Override protected void montarTopo() {
        Models.Usuario u = Session.get().usuario;
        LinearLayout cab = Ui.row(c);
        cab.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 8));
        LinearLayout txt = Ui.column(c);
        txt.addView(Ui.title(c, "Agenda de " + Fmt.primeiroNome(u.nome), 24));
        subtitulo = Ui.body(c, "", 13, Ui.MUTED);
        txt.addView(subtitulo);
        Ui.addW(cab, txt, 0);
        cab.addView(Ui.avatar(c, u.nome, 44));
        top.addView(cab);

        LinearLayout botoes = Ui.row(c);
        Ui.addW(botoes, Ui.button(c, "Bloquear Horário", Ui.CONTORNO, v -> bloquear()), 0);
        Btn alternar = Ui.button(c, "Ver Semana", Ui.PRIMARIO, null);
        botaoSemana = alternar;
        alternar.setOnClickListener(v -> { semana = !semana; reload(); });
        Ui.addW(botoes, alternar, 10);
        Ui.add(top, botoes, 8);

        LinearLayout nav = Ui.row(c);
        rotuloLista = Ui.title(c, "Horários do dia", 16);
        Ui.addW(nav, rotuloLista, 0);
        TextView ant = Ui.title(c, "‹", 28);
        ant.setPadding(Ui.dp(c, 14), 0, Ui.dp(c, 14), 0);
        ant.setOnClickListener(v -> { data = data.minusDays(semana ? 7 : 1); reload(); });
        TextView prox = Ui.title(c, "›", 28);
        prox.setPadding(Ui.dp(c, 14), 0, Ui.dp(c, 4), 0);
        prox.setOnClickListener(v -> { data = data.plusDays(semana ? 7 : 1); reload(); });
        nav.addView(ant);
        nav.addView(prox);
        Ui.add(top, nav, 16);
    }

    @Override protected void reload() {
        boolean hoje = data.equals(Fmt.hoje());
        subtitulo.setText(hoje ? "Hoje: " + Fmt.diaSemanaLongo(data) + ", " + data.getDayOfMonth() + " " + cap(Fmt.mesCurto(data))
                : Fmt.diaSemanaLongo(data) + ", " + Fmt.curta(data));
        rotuloLista.setText(semana ? "Próximos 7 dias" : "Horários do dia");
        botaoSemana.setText(semana ? "Ver Dia" : "Ver Semana");
        fetch(Net.api().agendaProfissional(data.toString(), semana ? 7 : 1), this::desenhar);
    }

    private static String cap(String s) { return s.isEmpty() ? s : s.charAt(0) + s.substring(1).toLowerCase(); }

    /** Item da lista: agendamento ou bloqueio, ordenados por data e hora. */
    private static class Entrada {
        final String data, hora; final Models.Agendamento ag; final Models.Bloqueio blq;
        Entrada(Models.Agendamento a) { data = a.data; hora = a.horaInicio; ag = a; blq = null; }
        Entrada(Models.Bloqueio b) { data = b.data; hora = b.horaInicio; ag = null; blq = b; }
    }

    private void desenhar(Models.Agenda agenda) {
        List<Entrada> es = new ArrayList<>();
        for (Models.Agendamento a : agenda.agendamentos) es.add(new Entrada(a));
        for (Models.Bloqueio b : agenda.bloqueios) es.add(new Entrada(b));
        Collections.sort(es, (x, y) -> (x.data + x.hora).compareTo(y.data + y.hora));
        if (es.isEmpty()) { list.addView(Ui.empty(c, "Nenhum atendimento neste período.")); return; }
        String ultimo = "";
        for (final Entrada e : es) {
            if (semana && !e.data.equals(ultimo)) {
                ultimo = e.data;
                LocalDate d = Fmt.data(e.data);
                TextView t = Ui.bold(c, Fmt.diaSemanaLongo(d) + ", " + Fmt.diaMes(d), 14, Ui.TITLE);
                Ui.add(list, t, 16);
            }
            LinearLayout linha = new LinearLayout(c);
            TextView hora = Ui.bold(c, e.hora, 14, Ui.TEXT);
            hora.setPadding(0, Ui.dp(c, 14), 0, 0);
            linha.addView(hora, new LinearLayout.LayoutParams(Ui.dp(c, 56), Ui.WRAP));
            if (e.ag != null) {
                final Models.Agendamento a = e.ag;
                LinearLayout card = Ui.clickCard(c, v -> acoes(a));
                LinearLayout cab = Ui.row(c);
                Ui.addW(cab, Ui.bold(c, a.servico, 15, Ui.TEXT), 0);
                cab.addView(Ui.statusChip(c, a.status));
                card.addView(cab);
                Ui.add(card, Ui.body(c, "Cliente: " + a.cliente, 13, Ui.MUTED), 4);
                Ui.addW(linha, card, 0);
            } else {
                final Models.Bloqueio b = e.blq;
                LinearLayout card = Ui.column(c);
                card.setBackground(Ui.shape(c, Ui.BORDER, 16, 0));
                card.setPadding(Ui.dp(c, 16), Ui.dp(c, 10), Ui.dp(c, 8), Ui.dp(c, 10));
                LinearLayout l = Ui.row(c);
                LinearLayout txt = Ui.column(c);
                txt.addView(Ui.semi(c, "Bloqueado até " + b.horaFim, 14, Ui.TITLE));
                if (b.motivo != null && !b.motivo.isEmpty()) txt.addView(Ui.body(c, b.motivo, 12, Ui.MUTED));
                Ui.addW(l, txt, 0);
                TextView x = Ui.title(c, "✕", 18);
                x.setPadding(Ui.dp(c, 12), Ui.dp(c, 8), Ui.dp(c, 8), Ui.dp(c, 8));
                x.setOnClickListener(v -> api(Net.api().removerBloqueio(b.id), r -> { Ui.toast(c, "Bloqueio removido"); reload(); }));
                l.addView(x);
                card.addView(l);
                Ui.addW(linha, card, 0);
            }
            Ui.add(list, linha, 10);
        }
    }

    private void acoes(final Models.Agendamento a) {
        final Dlg dlg = new Dlg(requireContext(), a.servico);
        LinearLayout chip = Ui.row(c);
        chip.addView(Ui.statusChip(c, a.status));
        dlg.add(chip, 10);
        dlg.add(Ui.body(c, "Cliente: " + a.cliente, 15, Ui.TEXT), 12);
        if (a.clienteTelefone != null && !a.clienteTelefone.isEmpty()) dlg.add(Ui.body(c, "Telefone: " + a.clienteTelefone, 13, Ui.MUTED), 2);
        dlg.add(Ui.body(c, Fmt.curta(Fmt.data(a.data)) + " • " + a.horaInicio + " às " + a.horaFim, 13, Ui.MUTED), 2);
        dlg.add(Ui.semi(c, "Valor: " + Fmt.brl(a.valor), 14, Ui.PRIMARY), 4);

        if ("PENDENTE".equals(a.status)) {
            dlg.add(Ui.button(c, "Confirmar agendamento", Ui.PRIMARIO, v -> executar(Net.api().confirmarAtendimento(a.id), "Agendamento confirmado", dlg)), 16);
        }
        if ("CONFIRMADO".equals(a.status) && !Fmt.data(a.data).isAfter(Fmt.hoje())) {
            dlg.add(Ui.button(c, "Registrar como realizado", Ui.PRIMARIO, v -> executar(Net.api().realizarAtendimento(a.id), "Atendimento registrado como realizado", dlg)), 16);
        }
        if ("PENDENTE".equals(a.status) || "CONFIRMADO".equals(a.status)) {
            dlg.add(Ui.button(c, "Cancelar atendimento", Ui.PERIGO, v -> new AlertDialog.Builder(requireContext())
                    .setTitle("Cancelar atendimento")
                    .setMessage("O cliente " + a.cliente + " será notificado. Continuar?")
                    .setPositiveButton("Cancelar atendimento", (d, w) -> executar(Net.api().cancelarProfissional(a.id), "Atendimento cancelado", dlg))
                    .setNegativeButton("Voltar", null).show()), 10);
        }
        dlg.add(Ui.button(c, "Fechar", Ui.SUAVE, v -> dlg.dismiss()), 10);
        dlg.show();
    }

    private void executar(retrofit2.Call<Models.Agendamento> call, final String msg, final Dlg dlg) {
        api(call, r -> { dlg.dismiss(); Ui.toast(c, msg); reload(); });
    }

    private void bloquear() {
        final Dlg dlg = new Dlg(requireContext(), "Bloquear horário");
        LocalDate inicial = data.isBefore(Fmt.hoje()) ? Fmt.hoje() : data;
        final Widgets.DateField campoData = new Widgets.DateField(c, "Data", inicial, Fmt.hoje());
        dlg.add(campoData, 14);
        LinearLayout horas = Ui.row(c);
        final Widgets.Choice ini = new Widgets.Choice(c, "Início", Widgets.horariosPadrao(), "12:00", null);
        final Widgets.Choice fim = new Widgets.Choice(c, "Fim", Widgets.horariosPadrao(), "13:00", null);
        Ui.addW(horas, ini, 0);
        Ui.addW(horas, fim, 10);
        dlg.add(horas, 12);
        final android.widget.EditText motivo = Ui.field(c, "Ex.: almoço", Ui.TEXTO());
        dlg.add(Ui.labeled(c, "Motivo (opcional)", motivo), 12);

        final Btn salvar = Ui.button(c, "Bloquear", Ui.PRIMARIO, null);
        salvar.setOnClickListener(v -> {
            if (fim.valor().compareTo(ini.valor()) <= 0) { Ui.toast(c, "O horário final deve ser depois do inicial"); return; }
            String m = motivo.getText().toString().trim();
            salvar.setOcupado(true);
            api(Net.api().bloquear(new Models.BloqueioReq(campoData.valor().toString(), ini.valor(), fim.valor(), m.isEmpty() ? null : m)), r -> {
                dlg.dismiss();
                Ui.toast(c, "Horário bloqueado");
                reload();
            }, err -> { salvar.setOcupado(false); Ui.toast(c, err); });
        });
        LinearLayout botoes = Ui.row(c);
        Ui.addW(botoes, Ui.button(c, "Voltar", Ui.SUAVE, v -> dlg.dismiss()), 0);
        Ui.addW(botoes, salvar, 10);
        dlg.add(botoes, 20);
        dlg.show();
    }
}
