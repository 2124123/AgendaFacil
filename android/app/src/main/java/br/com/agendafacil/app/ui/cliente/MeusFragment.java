package br.com.agendafacil.app.ui.cliente;

import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.ui.Btn;
import br.com.agendafacil.app.ui.Dlg;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.Widgets;
import br.com.agendafacil.app.ui.common.PageFragment;

import java.time.LocalDate;
import java.util.List;

/** RF005: acompanhar, remarcar e cancelar os próprios agendamentos (RN003). */
public class MeusFragment extends PageFragment {
    private String tipo = "proximos";
    private TextView abaProximos, abaHistorico;

    @Override protected void montarTopo() {
        LinearLayout cab = Ui.row(c);
        Ui.addW(cab, cabecalho("Seus Agendamentos", "Puxe para atualizar dados"), 0);
        TextView atualizar = Ui.title(c, "⟳", 24);
        atualizar.setTextColor(Ui.PRIMARY);
        atualizar.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 4), 0);
        atualizar.setOnClickListener(v -> reload());
        cab.addView(atualizar);
        top.addView(cab);

        LinearLayout abas = Ui.row(c);
        abaProximos = aba("Próximos", "proximos");
        abaHistorico = aba("Histórico", "historico");
        Ui.addW(abas, abaProximos, 0);
        Ui.addW(abas, abaHistorico, 0);
        Ui.add(top, abas, 8);
        Ui.add(top, Ui.divider(c), 0);
        pintarAbas();
    }

    private TextView aba(String texto, final String valor) {
        TextView t = Ui.semi(c, texto, 15, Ui.MUTED);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 12));
        t.setOnClickListener(v -> {
            if (tipo.equals(valor)) return;
            tipo = valor;
            pintarAbas();
            list.removeAllViews();
            reload();
        });
        return t;
    }

    private void pintarAbas() {
        boolean p = "proximos".equals(tipo);
        abaProximos.setTextColor(p ? Ui.PRIMARY : Ui.MUTED);
        abaHistorico.setTextColor(p ? Ui.MUTED : Ui.PRIMARY);
        abaProximos.setBackground(p ? sublinhado() : null);
        abaHistorico.setBackground(p ? null : sublinhado());
    }

    private android.graphics.drawable.Drawable sublinhado() {
        android.graphics.drawable.GradientDrawable fundo = new android.graphics.drawable.GradientDrawable();
        fundo.setColor(0);
        android.graphics.drawable.GradientDrawable linha = Ui.shape(c, Ui.PRIMARY, 0, 0);
        android.graphics.drawable.LayerDrawable l = new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{fundo, linha});
        l.setLayerGravity(1, Gravity.BOTTOM);
        l.setLayerHeight(1, Ui.dp(c, 2));
        return l;
    }

    @Override protected void reload() {
        fetch(Net.api().meusAgendamentos(tipo), this::desenhar);
    }

    private void desenhar(List<Models.Agendamento> lista) {
        if (lista.isEmpty()) {
            list.addView(Ui.empty(c, "proximos".equals(tipo) ? "Você não possui agendamentos próximos." : "Nenhum atendimento no histórico."));
            return;
        }
        for (final Models.Agendamento a : lista) {
            LinearLayout card = Ui.card(c);
            LinearLayout cab = Ui.row(c);
            cab.addView(Ui.pill(c, a.servico, Ui.CYAN_BG, Ui.PRIMARY));
            Ui.addW(cab, new android.view.View(c), 0);
            cab.addView(Ui.statusChip(c, a.status));
            card.addView(cab);
            card.addView(Ui.text(c, "Profissional: " + a.profissional, 16, Ui.TEXT, 700, true), params(10));
            card.addView(Ui.body(c, "📅 " + Fmt.longa(Fmt.data(a.data)) + " - " + a.horaInicio, 13, Ui.MUTED), params(6));
            card.addView(Ui.semi(c, Fmt.brl(a.valor), 13, Ui.PRIMARY), params(4));
            boolean aberto = "PENDENTE".equals(a.status) || "CONFIRMADO".equals(a.status);
            if ("proximos".equals(tipo) && aberto) {
                Ui.add(card, Ui.divider(c), 12);
                LinearLayout botoes = Ui.row(c);
                Ui.addW(botoes, Ui.button(c, "Remarcar", Ui.SUAVE, v -> remarcar(a)), 0);
                Ui.addW(botoes, Ui.button(c, "Cancelar", Ui.PERIGO, v -> confirmarCancelamento(a)), 10);
                Ui.add(card, botoes, 12);
            }
            Ui.add(list, card, 12);
        }
    }

    private LinearLayout.LayoutParams params(float topoDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP);
        lp.topMargin = Ui.dp(c, topoDp);
        return lp;
    }

    private void confirmarCancelamento(final Models.Agendamento a) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar agendamento")
                .setMessage("Deseja cancelar " + a.servico + " em " + Fmt.curta(Fmt.data(a.data)) + " às " + a.horaInicio + "?")
                .setPositiveButton("Cancelar agendamento", (d, w) -> api(Net.api().cancelarCliente(a.id), r -> {
                    Ui.toast(c, "Agendamento cancelado");
                    reload();
                }))
                .setNegativeButton("Voltar", null).show();
    }

    private void remarcar(final Models.Agendamento a) {
        final Dlg dlg = new Dlg(requireContext(), "Remarcar " + a.servico);
        dlg.add(Ui.body(c, "Profissional: " + a.profissional, 13, Ui.MUTED), 8);
        final Widgets.SlotPicker picker = new Widgets.SlotPicker(c, a.profissionalId, a.servicoId, a.id, null, null, null);
        dlg.add(picker, 12);
        final Btn confirmar = Ui.button(c, "Confirmar", Ui.PRIMARIO, null);
        confirmar.setOnClickListener(v -> {
            LocalDate d = picker.data();
            String h = picker.hora();
            if (d == null || h == null) { Ui.toast(c, "Escolha a data e o horário"); return; }
            confirmar.setOcupado(true);
            api(Net.api().remarcarCliente(a.id, new Models.RemarcarReq(d.toString(), h)), r -> {
                dlg.dismiss();
                Ui.toast(c, "Agendamento remarcado");
                reload();
            }, m -> { confirmar.setOcupado(false); Ui.toast(c, m); picker.atualizar(); });
        });
        LinearLayout botoes = Ui.row(c);
        Ui.addW(botoes, Ui.button(c, "Voltar", Ui.SUAVE, v -> dlg.dismiss()), 0);
        Ui.addW(botoes, confirmar, 10);
        dlg.add(botoes, 20);
        dlg.show();
    }
}
