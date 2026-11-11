package br.com.agendafacil.app.ui.cliente;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import br.com.agendafacil.app.R;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.data.Session;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.common.PageFragment;

import java.util.List;

/** Tela inicial do cliente: busca, próximos agendamentos e serviços populares. */
public class HomeFragment extends PageFragment {
    public static final String[] CATEGORIAS = {"Cabelo", "Estética", "Consultoria", "Unhas"};
    public static final String[] EMOJIS = {"✂️", "✨", "💼", "💅"};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable busca;
    private String consulta = "";

    @Override protected void montarTopo() {
        Models.Usuario u = Session.get().usuario;
        LinearLayout linha = Ui.row(c);
        linha.setPadding(0, Ui.dp(c, 8), 0, 0);
        Ui.addW(linha, cabecalho("Olá, " + Fmt.primeiroNome(u.nome) + "!", "Qual serviço você procura hoje?"), 0);
        android.view.View avatar = Ui.avatar(c, u.nome, 44);
        avatar.setOnClickListener(v -> shell().selecionarAba(R.id.nav_perfil));
        linha.addView(avatar);
        top.addView(linha);

        EditText campo = Ui.field(c, "Buscar serviços ou profissionais...", Ui.TEXTO());
        campo.setBackground(Ui.shape(c, Ui.WHITE, 14, Ui.BORDER));
        campo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void afterTextChanged(Editable e) {
                consulta = e.toString().trim();
                if (busca != null) handler.removeCallbacks(busca);
                busca = () -> { if (isAdded()) reload(); };
                handler.postDelayed(busca, 350);
            }
        });
        Ui.add(top, campo, 8);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (busca != null) handler.removeCallbacks(busca);
    }

    @Override protected void reload() {
        if (consulta.isEmpty()) {
            fetch(Net.api().meusAgendamentos("proximos"), this::desenharInicio);
        } else {
            final String q = consulta;
            fetch(Net.api().servicos(q, null), r -> {
                Ui.add(list, Ui.sectionTitle(c, "Resultados da busca"), 20);
                if (r.isEmpty()) list.addView(Ui.empty(c, "Nenhum serviço encontrado para \"" + q + "\"."));
                for (Models.Servico s : r) Ui.add(list, itemServico(s), 10);
            });
        }
    }

    private void desenharInicio(List<Models.Agendamento> proximos) {
        LinearLayout cab = Ui.row(c);
        Ui.addW(cab, Ui.sectionTitle(c, "Próximos agendamentos"), 0);
        TextView todos = Ui.body(c, "Ver todos", 13, Ui.PRIMARY);
        todos.setOnClickListener(v -> shell().selecionarAba(R.id.nav_meus));
        cab.addView(todos);
        Ui.add(list, cab, 20);
        if (proximos.isEmpty()) list.addView(Ui.empty(c, "Você ainda não tem agendamentos próximos."));
        for (int i = 0; i < Math.min(2, proximos.size()); i++) Ui.add(list, cartaoProximo(proximos.get(i)), 10);

        Ui.add(list, Ui.sectionTitle(c, "Serviços populares"), 22);
        LinearLayout grade = Ui.row(c);
        for (int i = 0; i < CATEGORIAS.length; i++) {
            final String cat = CATEGORIAS[i];
            LinearLayout cel = Ui.clickCard(c, v -> {
                BookingState b = ((ClienteActivity) requireActivity()).booking;
                b.limpar();
                b.categoria = cat;
                shell().selecionarAba(R.id.nav_agendar);
            });
            cel.setGravity(Gravity.CENTER_HORIZONTAL);
            cel.setPadding(Ui.dp(c, 4), Ui.dp(c, 14), Ui.dp(c, 4), Ui.dp(c, 12));
            TextView ic = Ui.body(c, EMOJIS[i], 18, Ui.TEXT);
            ic.setGravity(Gravity.CENTER);
            android.graphics.drawable.GradientDrawable oval = new android.graphics.drawable.GradientDrawable();
            oval.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            oval.setColor(i == 1 ? Ui.CORAL_BG : Ui.CYAN_BG);
            ic.setBackground(oval);
            cel.addView(ic, new LinearLayout.LayoutParams(Ui.dp(c, 40), Ui.dp(c, 40)));
            TextView nome = Ui.semi(c, cat, 11, Ui.TEXT);
            nome.setSingleLine(true);
            Ui.addWrap(cel, nome, 8);
            Ui.addW(grade, cel, i == 0 ? 0 : 8);
        }
        list.addView(grade, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
    }

    private android.view.View itemServico(final Models.Servico s) {
        LinearLayout card = Ui.clickCard(c, v -> {
            BookingState b = ((ClienteActivity) requireActivity()).booking;
            b.limpar();
            b.escolherServico(s);
            shell().selecionarAba(R.id.nav_agendar);
        });
        card.addView(linhaServico(c, s));
        return card;
    }

    /** Linha padrão de serviço: ícone, nome, categoria/duração e preço. */
    public static android.view.View linhaServico(android.content.Context c, Models.Servico s) {
        LinearLayout linha = Ui.row(c);
        String emoji = "✂️";
        for (int i = 0; i < CATEGORIAS.length; i++) if (CATEGORIAS[i].equalsIgnoreCase(s.categoria)) emoji = EMOJIS[i];
        TextView ic = Ui.body(c, emoji, 18, Ui.TEXT);
        ic.setGravity(Gravity.CENTER);
        android.graphics.drawable.GradientDrawable oval = new android.graphics.drawable.GradientDrawable();
        oval.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        oval.setColor(Ui.CYAN_BG);
        ic.setBackground(oval);
        Ui.addSize(linha, 44, 44, 0, ic);
        LinearLayout txt = Ui.column(c);
        txt.setPadding(Ui.dp(c, 12), 0, 0, 0);
        txt.addView(Ui.semi(c, s.nome, 15, Ui.TEXT));
        txt.addView(Ui.body(c, s.categoria + " • " + s.duracaoMinutos + " min", 12, Ui.MUTED));
        Ui.addW(linha, txt, 0);
        linha.addView(Ui.bold(c, Fmt.brl(s.preco), 14, Ui.PRIMARY));
        return linha;
    }

    private android.view.View cartaoProximo(Models.Agendamento a) {
        LinearLayout card = Ui.card(c);
        LinearLayout linha = Ui.row(c);
        boolean pend = "PENDENTE".equals(a.status);
        java.time.LocalDate d = Fmt.data(a.data);
        LinearLayout data = Ui.column(c);
        data.setGravity(Gravity.CENTER);
        data.setBackground(Ui.shape(c, pend ? Ui.CORAL_BG : Ui.CYAN_BG, 12, 0));
        int cor = pend ? Ui.CORAL : Ui.PRIMARY;
        data.addView(Ui.text(c, String.valueOf(d.getDayOfMonth()), 16, cor, 700, true));
        data.addView(Ui.bold(c, Fmt.mesCurto(d), 10, cor));
        Ui.addSize(linha, 52, 52, 0, data);
        LinearLayout txt = Ui.column(c);
        txt.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 8), 0);
        txt.addView(Ui.semi(c, a.servico, 15, Ui.TEXT));
        txt.addView(Ui.body(c, "Profissional: " + a.profissional, 12, Ui.MUTED));
        txt.addView(Ui.body(c, "Horário: " + a.horaInicio, 12, Ui.MUTED));
        Ui.addW(linha, txt, 0);
        linha.addView(Ui.statusChip(c, a.status));
        card.addView(linha);
        return card;
    }
}
