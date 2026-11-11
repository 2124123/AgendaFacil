package br.com.agendafacil.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.data.Server;
import br.com.agendafacil.app.data.Session;
import br.com.agendafacil.app.ui.Btn;
import br.com.agendafacil.app.ui.Dlg;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.Widgets;
import br.com.agendafacil.app.ui.common.Nav;

public class LoginActivity extends AppCompatActivity {
    private static final String[] PERFIS = {"CLIENTE", "PROFISSIONAL", "ADMINISTRADOR"};
    private static final String[] ROTULOS = {"Cliente", "Profissional", "Administrador"};

    private int perfilSel = 0;
    private TextView[] abas;
    private TextView criarConta, erro;
    private EditText email;
    private Widgets.PasswordBox senha;
    private Btn entrar;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Session.get() != null) { Nav.irParaHome(this); return; }

        LinearLayout col = Ui.column(this);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(Ui.dp(this, 24), Ui.dp(this, 56), Ui.dp(this, 24), Ui.dp(this, 24));

        LinearLayout logo = Ui.row(this);
        ImageView icone = new ImageView(this);
        icone.setImageResource(R.drawable.ic_launcher);
        icone.setScaleType(ImageView.ScaleType.CENTER_CROP);
        icone.setBackground(Ui.shape(this, Ui.PRIMARY, 16, 0));
        icone.setClipToOutline(true);
        logo.addView(icone, new LinearLayout.LayoutParams(Ui.dp(this, 56), Ui.dp(this, 56)));
        TextView nome = Ui.title(this, "AgendaFácil", 28);
        nome.setTextColor(Ui.TITLE);
        nome.setPadding(Ui.dp(this, 12), 0, 0, 0);
        logo.addView(nome);
        col.addView(logo, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));
        Ui.addWrap(col, Ui.body(this, "Agende seus serviços com facilidade", 14, Ui.MUTED), 10);

        LinearLayout seg = Ui.row(this);
        seg.setBackground(Ui.shape(this, Ui.WHITE, 14, Ui.BORDER));
        seg.setPadding(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
        abas = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView t = Ui.semi(this, ROTULOS[i], 12, Ui.TITLE);
            t.setGravity(Gravity.CENTER);
            t.setMinHeight(Ui.dp(this, 40));
            t.setSingleLine(true);
            t.setOnClickListener(v -> escolherPerfil(idx));
            abas[i] = t;
            Ui.addW(seg, t, 0);
        }
        Ui.add(col, seg, 32);

        email = Ui.field(this, "seu@email.com", Ui.EMAIL());
        Ui.add(col, Ui.labeled(this, "E-mail", email), 20);
        senha = new Widgets.PasswordBox(this, "Sua senha");
        Ui.add(col, Ui.labeled(this, "Senha", senha), 14);

        erro = Ui.body(this, "", 13, Ui.RED_FG);
        Ui.add(col, erro, 10);
        entrar = Ui.button(this, "Entrar", Ui.PRIMARIO, v -> entrar());
        Ui.add(col, entrar, 20);

        criarConta = Ui.semi(this, "Criar conta", 14, Ui.PRIMARY);
        criarConta.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 8));
        criarConta.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        Ui.addWrap(col, criarConta, 6);
        TextView esqueci = Ui.body(this, "Esqueci minha senha", 13, Ui.MUTED);
        esqueci.setPadding(Ui.dp(this, 16), Ui.dp(this, 8), Ui.dp(this, 16), Ui.dp(this, 8));
        esqueci.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Recuperar senha")
                .setMessage("A recuperação automática de senha ainda não está disponível. Procure o administrador do estabelecimento para redefinir o seu acesso.")
                .setPositiveButton("OK", null).show());
        Ui.addWrap(col, esqueci, 0);
        TextView servidor = Ui.body(this, "⚙  Servidor", 12, Ui.MUTED);
        servidor.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 8));
        servidor.setOnClickListener(v -> configurarServidor());
        Ui.addWrap(col, servidor, 12);

        setContentView(Ui.scroll(this, col));
        escolherPerfil(0);
    }

    private void escolherPerfil(int idx) {
        perfilSel = idx;
        for (int i = 0; i < 3; i++) {
            boolean sel = i == idx;
            abas[i].setBackground(sel ? Ui.shape(this, Ui.PRIMARY, 10, 0) : null);
            abas[i].setTextColor(sel ? Ui.WHITE : Ui.TITLE);
        }
        criarConta.setVisibility(idx == 0 ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void entrar() {
        String e = email.getText().toString().trim();
        String s = senha.texto();
        if (e.isEmpty() || s.isEmpty()) { erro.setText("Informe e-mail e senha"); return; }
        erro.setText("");
        entrar.setOcupado(true);
        Net.enqueue(Net.api().login(new Models.LoginReq(e, s, PERFIS[perfilSel])), new Net.Resultado<Models.AuthRes>() {
            @Override public void onOk(Models.AuthRes r) {
                Session.save(r);
                Nav.irParaHome(LoginActivity.this);
            }
            @Override public void onFail(String m) {
                entrar.setOcupado(false);
                erro.setText(m);
            }
        });
    }

    /** Permite apontar o app para o servidor na internet (ex.: https://meu-servidor.up.railway.app). */
    private void configurarServidor() {
        final Dlg dlg = new Dlg(this, "Endereço do servidor");
        dlg.add(Ui.body(this, "Informe o endereço da API. Em celulares fora da sua rede, use o endereço público (https://...).", 13, Ui.MUTED), 8);
        final EditText campo = Ui.field(this, "https://meu-servidor.com", Ui.EMAIL());
        campo.setText(Server.url());
        dlg.add(Ui.labeled(this, "Endereço", campo), 14);
        final TextView resultado = Ui.body(this, "", 13, Ui.MUTED);
        dlg.add(resultado, 10);
        final Btn salvar = Ui.button(this, "Salvar e testar conexão", Ui.PRIMARIO, null);
        salvar.setOnClickListener(v -> {
            String u = Server.normalizar(campo.getText().toString());
            if (u == null) { resultado.setTextColor(Ui.RED_FG); resultado.setText("Endereço inválido."); return; }
            Server.salvar(u);
            campo.setText(u);
            resultado.setTextColor(Ui.MUTED);
            resultado.setText("Testando...");
            salvar.setOcupado(true);
            // Login de mentira: se o servidor responder (mesmo com "senha inválida"), a conexão está ok.
            Net.enqueue(Net.api().login(new Models.LoginReq("teste@teste.com", "x", "CLIENTE")), new Net.Resultado<Models.AuthRes>() {
                @Override public void onOk(Models.AuthRes r) { fim(true, ""); }
                @Override public void onFail(String m) { fim(!m.startsWith("Sem conexão"), m); }
                private void fim(boolean ok, String m) {
                    salvar.setOcupado(false);
                    resultado.setTextColor(ok ? Ui.GREEN_FG : Ui.RED_FG);
                    resultado.setText(ok ? "✓ Servidor encontrado e salvo." : "Não foi possível conectar. Confira o endereço e se o servidor está ligado.");
                }
            });
        });
        dlg.add(salvar, 12);
        dlg.add(Ui.button(this, "Restaurar padrão", Ui.SUAVE, v -> {
            Server.restaurar();
            campo.setText(Server.url());
            resultado.setTextColor(Ui.MUTED);
            resultado.setText("Endereço padrão restaurado.");
        }), 10);
        dlg.add(Ui.button(this, "Fechar", Ui.SUAVE, v -> dlg.dismiss()), 10);
        dlg.show();
    }
}
