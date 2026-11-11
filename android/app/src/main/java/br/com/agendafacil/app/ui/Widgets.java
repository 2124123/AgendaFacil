package br.com.agendafacil.app.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Campos compostos reutilizados nas telas. */
public final class Widgets {
    private Widgets() {}

    /** Horários de 30 em 30 minutos, de 06:00 a 22:00. */
    public static List<String> horariosPadrao() {
        List<String> l = new ArrayList<>();
        for (int i = 12; i <= 44; i++) l.add(String.format(Locale.US, "%02d:%02d", i / 2, (i % 2) * 30));
        return l;
    }

    // ------------------------------------------------------------------ senha com mostrar/ocultar

    public static class PasswordBox extends FrameLayout {
        public final EditText edit;

        public PasswordBox(Context c, String dica) {
            super(c);
            edit = Ui.field(c, dica, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            edit.setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 80), 0);
            addView(edit, new LayoutParams(Ui.MATCH, Ui.WRAP));
            TextView alternar = Ui.semi(c, "Mostrar", 12, Ui.PRIMARY);
            alternar.setPadding(Ui.dp(c, 12), Ui.dp(c, 12), Ui.dp(c, 16), Ui.dp(c, 12));
            LayoutParams lp = new LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.END | Gravity.CENTER_VERTICAL);
            addView(alternar, lp);
            alternar.setOnClickListener(v -> {
                boolean visivel = alternar.getText().toString().equals("Ocultar");
                int pos = edit.getSelectionEnd();
                edit.setInputType(InputType.TYPE_CLASS_TEXT
                        | (visivel ? InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
                edit.setTypeface(Ui.fonte(c, false, 400));
                edit.setSelection(Math.max(pos, 0));
                alternar.setText(visivel ? "Mostrar" : "Ocultar");
            });
        }

        public String texto() { return edit.getText().toString(); }
    }

    // ------------------------------------------------------------------ lista de opções

    public interface AoEscolher { void run(String valor); }

    public static class Choice extends LinearLayout {
        private String valor;
        private final TextView mostra;

        public Choice(Context c, String rotulo, List<String> opcoes, String inicial, AoEscolher ao) {
            super(c);
            setOrientation(VERTICAL);
            valor = inicial;
            if (rotulo != null && !rotulo.isEmpty()) addView(Ui.text(c, rotulo, 14, Ui.TITLE, 500, false));
            mostra = Ui.body(c, inicial + "   ▾", 15, Ui.TEXT);
            mostra.setGravity(Gravity.CENTER_VERTICAL);
            mostra.setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0);
            mostra.setMinHeight(Ui.dp(c, 52));
            mostra.setBackground(Ui.ripple(c, Ui.WHITE, 12, Ui.BORDER));
            Ui.add(this, mostra, rotulo == null || rotulo.isEmpty() ? 0 : 6);
            mostra.setOnClickListener(v -> new AlertDialog.Builder(c)
                    .setItems(opcoes.toArray(new String[0]), (d, i) -> {
                        valor = opcoes.get(i);
                        mostra.setText(valor + "   ▾");
                        if (ao != null) ao.run(valor);
                    }).show());
        }

        public String valor() { return valor; }
    }

    // ------------------------------------------------------------------ data

    public static class DateField extends LinearLayout {
        private LocalDate valor;
        private final TextView mostra;

        public DateField(Context c, String rotulo, LocalDate inicial, LocalDate minima) {
            super(c);
            setOrientation(VERTICAL);
            valor = inicial;
            if (rotulo != null && !rotulo.isEmpty()) addView(Ui.text(c, rotulo, 14, Ui.TITLE, 500, false));
            mostra = Ui.body(c, "📅  " + Fmt.curta(inicial), 15, Ui.TEXT);
            mostra.setGravity(Gravity.CENTER_VERTICAL);
            mostra.setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0);
            mostra.setMinHeight(Ui.dp(c, 52));
            mostra.setBackground(Ui.ripple(c, Ui.WHITE, 12, Ui.BORDER));
            Ui.add(this, mostra, rotulo == null || rotulo.isEmpty() ? 0 : 6);
            mostra.setOnClickListener(v -> {
                DatePickerDialog d = new DatePickerDialog(c, (view, a, m, dia) -> {
                    valor = LocalDate.of(a, m + 1, dia);
                    mostra.setText("📅  " + Fmt.curta(valor));
                }, valor.getYear(), valor.getMonthValue() - 1, valor.getDayOfMonth());
                d.getDatePicker().setMinDate(minima.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
                d.show();
            });
        }

        public LocalDate valor() { return valor; }
    }

    // ------------------------------------------------------------------ calendário de 7 dias + horários

    public interface AoMudar { void run(LocalDate data, String hora); }

    /** Dias sem vaga ficam desabilitados (RN002). Usa os endpoints /api/agenda/dias e /api/agenda/horarios. */
    public static class SlotPicker extends LinearLayout {
        private final long profissionalId, servicoId;
        private final Long ignorarId;
        private final AoMudar ao;
        private LocalDate inicio = Fmt.hoje(), dataSel;
        private String horaSel;
        private List<Models.DiaDisponivel> dias;
        private List<String> horarios;
        private final TextView mes;
        private final LinearLayout linhaDias, caixaHoras;
        private int versao = 0;

        public SlotPicker(Context c, long profissionalId, long servicoId, Long ignorarId, LocalDate dataInicial, String horaInicial, AoMudar ao) {
            super(c);
            setOrientation(VERTICAL);
            this.profissionalId = profissionalId; this.servicoId = servicoId; this.ignorarId = ignorarId; this.ao = ao;
            this.dataSel = dataInicial; this.horaSel = horaInicial;

            LinearLayout topo = Ui.row(c);
            mes = Ui.title(c, "", 16);
            Ui.addW(topo, mes, 0);
            TextView ant = Ui.title(c, "‹", 28);
            ant.setPadding(Ui.dp(c, 14), 0, Ui.dp(c, 14), 0);
            ant.setOnClickListener(v -> { if (inicio.isAfter(Fmt.hoje())) { inicio = inicio.minusDays(7); carregarDias(); } });
            TextView prox = Ui.title(c, "›", 28);
            prox.setPadding(Ui.dp(c, 14), 0, Ui.dp(c, 8), 0);
            prox.setOnClickListener(v -> { inicio = inicio.plusDays(7); carregarDias(); });
            topo.addView(ant);
            topo.addView(prox);
            addView(topo);

            linhaDias = Ui.row(c);
            Ui.add(this, linhaDias, 8);
            addView(Ui.title(c, "Horários disponíveis", 16), params(20));
            caixaHoras = Ui.column(c);
            Ui.add(this, caixaHoras, 10);
            carregarDias();
            if (dataSel != null) carregarHorarios();
            desenharHoras();
        }

        private LayoutParams params(float topoDp) {
            LayoutParams lp = new LayoutParams(Ui.WRAP, Ui.WRAP);
            lp.topMargin = Ui.dp(getContext(), topoDp);
            return lp;
        }

        public LocalDate data() { return dataSel; }
        public String hora() { return horaSel; }

        /** Recarrega as vagas (ex.: após conflito de horário). */
        public void atualizar() {
            horaSel = null;
            carregarDias();
            if (dataSel != null) carregarHorarios();
            if (ao != null) ao.run(dataSel, null);
        }

        private void carregarDias() {
            final int v = ++versao;
            dias = null;
            desenharDias();
            Net.enqueue(Net.api().diasDisponiveis(profissionalId, servicoId, inicio.toString(), 7), new Net.Resultado<List<Models.DiaDisponivel>>() {
                @Override public void onOk(List<Models.DiaDisponivel> r) { if (v == versao) { dias = r; desenharDias(); } }
                @Override public void onFail(String m) { Ui.toast(getContext(), m); }
            });
        }

        private void carregarHorarios() {
            horarios = null;
            desenharHoras();
            final LocalDate alvo = dataSel;
            Net.enqueue(Net.api().horariosLivres(profissionalId, servicoId, alvo.toString(), ignorarId), new Net.Resultado<List<String>>() {
                @Override public void onOk(List<String> r) { if (alvo.equals(dataSel)) { horarios = r; desenharHoras(); } }
                @Override public void onFail(String m) { horarios = new ArrayList<>(); desenharHoras(); Ui.toast(getContext(), m); }
            });
        }

        private void desenharDias() {
            Context c = getContext();
            mes.setText(Fmt.mesAno(inicio));
            linhaDias.removeAllViews();
            for (int i = 0; i < 7; i++) {
                final LocalDate d = inicio.plusDays(i);
                boolean livre = false;
                if (dias != null) for (Models.DiaDisponivel x : dias) if (x.data.equals(d.toString()) && x.horarios > 0) livre = true;
                boolean sel = d.equals(dataSel);
                LinearLayout cel = Ui.column(c);
                cel.setGravity(Gravity.CENTER_HORIZONTAL);
                cel.setPadding(0, Ui.dp(c, 10), 0, Ui.dp(c, 10));
                cel.setBackground(sel ? Ui.shape(c, Ui.PRIMARY, 12, 0) : Ui.ripple(c, Ui.WHITE, 12, Ui.BORDER));
                int corSem = sel ? Ui.WHITE : (livre ? Ui.MUTED : Ui.BORDER);
                int corDia = sel ? Ui.WHITE : (livre ? Ui.TEXT : Ui.BORDER);
                cel.addView(Ui.body(c, Fmt.diaSemanaCurto(d), 11, corSem));
                cel.addView(Ui.text(c, String.valueOf(d.getDayOfMonth()), 16, corDia, 700, true));
                cel.setEnabled(livre);
                if (livre) cel.setOnClickListener(v -> {
                    dataSel = d; horaSel = null;
                    desenharDias(); carregarHorarios();
                    if (ao != null) ao.run(dataSel, null);
                });
                Ui.addW(linhaDias, cel, i == 0 ? 0 : 6);
            }
        }

        private void desenharHoras() {
            Context c = getContext();
            caixaHoras.removeAllViews();
            if (dataSel == null) { caixaHoras.addView(Ui.body(c, "Selecione uma data acima.", 13, Ui.MUTED)); return; }
            if (horarios == null) { caixaHoras.addView(Ui.loading(c)); return; }
            if (horarios.isEmpty()) { caixaHoras.addView(Ui.body(c, "Sem horários disponíveis nesta data.", 13, Ui.MUTED)); return; }
            LinearLayout linha = null;
            for (int i = 0; i < horarios.size(); i++) {
                if (i % 4 == 0) {
                    linha = Ui.row(c);
                    Ui.add(caixaHoras, linha, i == 0 ? 0 : 10);
                }
                final String h = horarios.get(i);
                boolean sel = h.equals(horaSel);
                TextView chip = Ui.text(c, h, 14, sel ? Ui.WHITE : Ui.TEXT, 500, false);
                chip.setGravity(Gravity.CENTER);
                chip.setPadding(0, Ui.dp(c, 12), 0, Ui.dp(c, 12));
                chip.setBackground(sel ? Ui.shape(c, Ui.CORAL, 12, 0) : Ui.ripple(c, Ui.WHITE, 12, Ui.BORDER));
                chip.setOnClickListener(v -> { horaSel = h; desenharHoras(); if (ao != null) ao.run(dataSel, horaSel); });
                Ui.addW(linha, chip, i % 4 == 0 ? 0 : 10);
            }
            // completa a última linha para manter o tamanho dos botões
            int resto = horarios.size() % 4;
            if (resto != 0) for (int k = resto; k < 4; k++) Ui.addW(linha, new View(c), 10);
        }
    }
}
