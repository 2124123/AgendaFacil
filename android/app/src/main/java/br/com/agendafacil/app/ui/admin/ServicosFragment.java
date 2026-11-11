package br.com.agendafacil.app.ui.admin;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.ui.Btn;
import br.com.agendafacil.app.ui.Dlg;
import br.com.agendafacil.app.ui.Fmt;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.Widgets;
import br.com.agendafacil.app.ui.cliente.HomeFragment;
import br.com.agendafacil.app.ui.common.PageFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** RN005: apenas o administrador cadastra e altera serviços. */
public class ServicosFragment extends PageFragment {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable busca;
    private String consulta = "";

    @Override protected void montarTopo() {
        LinearLayout cab = Ui.row(c);
        Ui.addW(cab, cabecalho("Serviços", "Gerencie os serviços oferecidos"), 0);
        TextView novo = Ui.semi(c, "+ Adicionar", 14, Ui.WHITE);
        novo.setBackground(Ui.ripple(c, Ui.PRIMARY, 12, 0));
        novo.setPadding(Ui.dp(c, 14), Ui.dp(c, 10), Ui.dp(c, 14), Ui.dp(c, 10));
        novo.setOnClickListener(v -> formulario(null));
        cab.addView(novo);
        top.addView(cab);

        EditText campo = Ui.field(c, "Buscar serviços...", Ui.TEXTO());
        campo.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void afterTextChanged(Editable e) {
                consulta = e.toString().trim();
                if (busca != null) handler.removeCallbacks(busca);
                busca = () -> { if (isAdded()) reload(); };
                handler.postDelayed(busca, 300);
            }
        });
        Ui.add(top, campo, 8);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (busca != null) handler.removeCallbacks(busca);
    }

    @Override protected void reload() {
        fetch(Net.api().adminServicos(consulta.isEmpty() ? null : consulta), (List<Models.Servico> r) -> {
            if (r.isEmpty()) list.addView(Ui.empty(c, "Nenhum serviço encontrado."));
            for (final Models.Servico s : r) {
                LinearLayout card = Ui.clickCard(c, v -> formulario(s));
                LinearLayout linha = Ui.row(c);
                LinearLayout txt = Ui.column(c);
                txt.addView(s.ativo ? Ui.bold(c, s.nome, 15, Ui.TEXT) : Ui.bold(c, s.nome, 15, Ui.MUTED));
                txt.addView(Ui.body(c, s.duracaoMinutos + " min • " + Fmt.brl(s.preco), 13, Ui.MUTED));
                Ui.addWrap(txt, Ui.pill(c, s.categoria, Ui.CYAN_BG, Ui.PRIMARY), 6);
                Ui.addW(linha, txt, 0);
                Switch sw = new Switch(c);
                sw.setChecked(s.ativo);
                sw.setOnCheckedChangeListener((b, marcado) -> {
                    if (marcado == s.ativo) return;
                    api(Net.api().adminAtivoServico(s.id, marcado), res -> {
                        Ui.toast(c, marcado ? "Serviço ativado" : "Serviço desativado");
                        reload();
                    }, m -> { Ui.toast(c, m); reload(); });
                });
                linha.addView(sw);
                card.addView(linha);
                Ui.add(list, card, 10);
            }
        });
    }

    private void formulario(final Models.Servico s) {
        final Dlg dlg = new Dlg(requireContext(), s == null ? "Novo serviço" : "Editar serviço");
        final EditText nome = Ui.field(c, "Nome do serviço", Ui.TEXTO());
        final EditText desc = Ui.field(c, "Descrição", Ui.TEXTO());
        final EditText dur = Ui.field(c, "60", Ui.NUMERO());
        final EditText preco = Ui.field(c, "85,00", Ui.DECIMAL());
        if (s != null) {
            nome.setText(s.nome);
            desc.setText(s.descricao == null ? "" : s.descricao);
            dur.setText(String.valueOf(s.duracaoMinutos));
            preco.setText(String.format(Fmt.PT_BR, "%.2f", s.preco));
        } else {
            dur.setText("60");
        }
        dlg.add(Ui.labeled(c, "Nome", nome), 14);
        dlg.add(Ui.labeled(c, "Descrição (opcional)", desc), 12);
        final Widgets.Choice cat = new Widgets.Choice(c, "Categoria", Arrays.asList(HomeFragment.CATEGORIAS),
                s == null ? HomeFragment.CATEGORIAS[0] : s.categoria, null);
        dlg.add(cat, 12);
        LinearLayout linha = Ui.row(c);
        Ui.addW(linha, Ui.labeled(c, "Duração (min)", dur), 0);
        Ui.addW(linha, Ui.labeled(c, "Preço (R$)", preco), 10);
        dlg.add(linha, 12);

        final Btn salvar = Ui.button(c, "Salvar", Ui.PRIMARIO, null);
        salvar.setOnClickListener(v -> {
            String n = nome.getText().toString().trim();
            int d;
            double p;
            try { d = Integer.parseInt(dur.getText().toString().trim()); } catch (NumberFormatException e) { d = 0; }
            try { p = Double.parseDouble(preco.getText().toString().trim().replace(',', '.')); } catch (NumberFormatException e) { p = -1; }
            if (n.isEmpty()) { Ui.toast(c, "Informe o nome do serviço"); return; }
            if (d < 5) { Ui.toast(c, "Duração mínima de 5 minutos"); return; }
            if (p < 0) { Ui.toast(c, "Informe um preço válido"); return; }
            String ds = desc.getText().toString().trim();
            Models.ServicoForm f = new Models.ServicoForm(n, ds.isEmpty() ? null : ds, cat.valor(), d, p);
            salvar.setOcupado(true);
            retrofit2.Call<Models.Servico> call = s == null ? Net.api().adminCriarServico(f) : Net.api().adminAtualizarServico(s.id, f);
            api(call, r -> { dlg.dismiss(); Ui.toast(c, s == null ? "Serviço criado" : "Serviço atualizado"); reload(); },
                    m -> { salvar.setOcupado(false); Ui.toast(c, m); });
        });
        LinearLayout botoes = Ui.row(c);
        Ui.addW(botoes, Ui.button(c, "Cancelar", Ui.SUAVE, v -> dlg.dismiss()), 0);
        Ui.addW(botoes, salvar, 10);
        dlg.add(botoes, 20);
        dlg.show();
    }
}
