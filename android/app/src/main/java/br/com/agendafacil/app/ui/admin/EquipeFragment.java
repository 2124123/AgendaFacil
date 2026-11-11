package br.com.agendafacil.app.ui.admin;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RN005: apenas o administrador cadastra profissionais e define seus horários de atendimento. */
public class EquipeFragment extends PageFragment {
    private static final String[] DIAS = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
    private static final String[] DIAS_ROTULO = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo"};

    @Override protected void montarTopo() {
        LinearLayout cab = Ui.row(c);
        Ui.addW(cab, cabecalho("Profissionais", "Gerencie a equipe e os horários"), 0);
        TextView novo = Ui.semi(c, "+ Adicionar", 14, Ui.WHITE);
        novo.setBackground(Ui.ripple(c, Ui.PRIMARY, 12, 0));
        novo.setPadding(Ui.dp(c, 14), Ui.dp(c, 10), Ui.dp(c, 14), Ui.dp(c, 10));
        novo.setOnClickListener(v -> formulario(null));
        cab.addView(novo);
        top.addView(cab);
    }

    @Override protected void reload() {
        fetch(Net.api().adminProfissionais(), (List<Models.Profissional> r) -> {
            if (r.isEmpty()) list.addView(Ui.empty(c, "Nenhum profissional cadastrado."));
            for (final Models.Profissional p : r) {
                LinearLayout card = Ui.clickCard(c, v -> formulario(p));
                LinearLayout linha = Ui.row(c);
                linha.addView(Ui.avatar(c, p.nome, 44));
                LinearLayout txt = Ui.column(c);
                txt.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 8), 0);
                txt.addView(p.ativo ? Ui.bold(c, p.nome, 15, Ui.TEXT) : Ui.bold(c, p.nome, 15, Ui.MUTED));
                TextView sub = Ui.body(c, p.especialidade + " • " + p.email, 12, Ui.MUTED);
                sub.setSingleLine(true);
                sub.setEllipsize(android.text.TextUtils.TruncateAt.END);
                txt.addView(sub);
                Ui.addW(linha, txt, 0);
                Switch sw = new Switch(c);
                sw.setChecked(p.ativo);
                sw.setOnCheckedChangeListener((b, marcado) -> {
                    if (marcado == p.ativo) return;
                    api(Net.api().adminAtivoProfissional(p.id, marcado), res -> {
                        Ui.toast(c, marcado ? "Profissional ativado" : "Profissional desativado");
                        reload();
                    }, m -> { Ui.toast(c, m); reload(); });
                });
                linha.addView(sw);
                card.addView(linha);
                TextView horarios = Ui.semi(c, "🕒  Horários de atendimento", 13, Ui.PRIMARY);
                horarios.setPadding(0, Ui.dp(c, 12), 0, 0);
                horarios.setOnClickListener(v -> horarios(p));
                card.addView(horarios);
                Ui.add(list, card, 10);
            }
        });
    }

    private void formulario(final Models.Profissional p) {
        final Dlg dlg = new Dlg(requireContext(), p == null ? "Novo profissional" : "Editar profissional");
        final EditText nome = Ui.field(c, "Nome", Ui.TEXTO());
        final EditText email = Ui.field(c, "E-mail", Ui.EMAIL());
        final EditText tel = Ui.field(c, "Telefone", Ui.TELEFONE());
        final Widgets.PasswordBox senha = new Widgets.PasswordBox(c, p == null ? "Mínimo 6 caracteres" : "Deixe em branco para manter");
        if (p != null) {
            nome.setText(p.nome);
            email.setText(p.email);
            email.setEnabled(false);
            email.setAlpha(0.6f);
            tel.setText(p.telefone == null ? "" : p.telefone);
        }
        dlg.add(Ui.labeled(c, "Nome", nome), 14);
        dlg.add(Ui.labeled(c, "E-mail", email), 12);
        dlg.add(Ui.labeled(c, "Telefone (opcional)", tel), 12);
        final Widgets.Choice esp = new Widgets.Choice(c, "Especialidade (categoria de serviços)", Arrays.asList(HomeFragment.CATEGORIAS),
                p == null ? HomeFragment.CATEGORIAS[0] : p.especialidade, null);
        dlg.add(esp, 12);
        dlg.add(Ui.labeled(c, p == null ? "Senha inicial" : "Nova senha (opcional)", senha), 12);

        final Btn salvar = Ui.button(c, "Salvar", Ui.PRIMARIO, null);
        salvar.setOnClickListener(v -> {
            String n = nome.getText().toString().trim(), e = email.getText().toString().trim();
            String t = tel.getText().toString().trim(), s = senha.texto();
            if (n.isEmpty() || e.isEmpty()) { Ui.toast(c, "Informe nome e e-mail"); return; }
            if ((p == null || !s.isEmpty()) && s.length() < 6) { Ui.toast(c, "A senha deve ter no mínimo 6 caracteres"); return; }
            Models.ProfissionalForm f = new Models.ProfissionalForm(n, e, t.isEmpty() ? null : t, esp.valor(), s.isEmpty() ? null : s);
            salvar.setOcupado(true);
            retrofit2.Call<Models.Profissional> call = p == null ? Net.api().adminCriarProfissional(f) : Net.api().adminAtualizarProfissional(p.id, f);
            api(call, r -> { dlg.dismiss(); Ui.toast(c, p == null ? "Profissional cadastrado" : "Profissional atualizado"); reload(); },
                    m -> { salvar.setOcupado(false); Ui.toast(c, m); });
        });
        LinearLayout botoes = Ui.row(c);
        Ui.addW(botoes, Ui.button(c, "Cancelar", Ui.SUAVE, v -> dlg.dismiss()), 0);
        Ui.addW(botoes, salvar, 10);
        dlg.add(botoes, 20);
        dlg.show();
    }

    /** Define os dias e horários de atendimento (usados no cálculo das vagas). */
    private void horarios(final Models.Profissional p) {
        final Dlg dlg = new Dlg(requireContext(), "Horários de " + Fmt.primeiroNome(p.nome));
        final LinearLayout conteudo = Ui.column(c);
        conteudo.addView(Ui.loading(c));
        dlg.add(conteudo, 8);
        dlg.show();

        api(Net.api().adminDisponibilidade(p.id), (List<Models.Disponibilidade> atuais) -> {
            conteudo.removeAllViews();
            final Map<String, Models.Disponibilidade> porDia = new HashMap<>();
            for (Models.Disponibilidade d : atuais) if (!porDia.containsKey(d.diaSemana)) porDia.put(d.diaSemana, d);
            final Switch[] ativos = new Switch[7];
            final Widgets.Choice[] inicios = new Widgets.Choice[7], fins = new Widgets.Choice[7];
            for (int i = 0; i < 7; i++) {
                Models.Disponibilidade d = porDia.get(DIAS[i]);
                LinearLayout linha = Ui.row(c);
                ativos[i] = new Switch(c);
                ativos[i].setChecked(d != null);
                linha.addView(ativos[i]);
                TextView rot = Ui.body(c, DIAS_ROTULO[i], 13, Ui.TEXT);
                rot.setPadding(Ui.dp(c, 8), 0, 0, 0);
                linha.addView(rot, new LinearLayout.LayoutParams(Ui.dp(c, 76), Ui.WRAP));
                inicios[i] = new Widgets.Choice(c, "", Widgets.horariosPadrao(), d != null ? d.horaInicio : "09:00", null);
                fins[i] = new Widgets.Choice(c, "", Widgets.horariosPadrao(), d != null ? d.horaFim : "18:00", null);
                Ui.addW(linha, inicios[i], 0);
                Ui.addW(linha, fins[i], 6);
                Ui.add(conteudo, linha, 8);
            }
            final Btn salvar = Ui.button(c, "Salvar", Ui.PRIMARIO, null);
            salvar.setOnClickListener(v -> {
                List<Models.Disponibilidade> nova = new java.util.ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    if (!ativos[i].isChecked()) continue;
                    if (fins[i].valor().compareTo(inicios[i].valor()) <= 0) { Ui.toast(c, "O horário final deve ser depois do inicial"); return; }
                    nova.add(new Models.Disponibilidade(DIAS[i], inicios[i].valor(), fins[i].valor()));
                }
                salvar.setOcupado(true);
                api(Net.api().adminDefinirDisponibilidade(p.id, nova), r -> { dlg.dismiss(); Ui.toast(c, "Horários salvos"); },
                        m -> { salvar.setOcupado(false); Ui.toast(c, m); });
            });
            LinearLayout botoes = Ui.row(c);
            Ui.addW(botoes, Ui.button(c, "Cancelar", Ui.SUAVE, v -> dlg.dismiss()), 0);
            Ui.addW(botoes, salvar, 10);
            Ui.add(conteudo, botoes, 16);
        }, m -> { dlg.dismiss(); Ui.toast(c, m); });
    }
}
