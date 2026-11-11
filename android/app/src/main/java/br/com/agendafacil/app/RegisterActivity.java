package br.com.agendafacil.app;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.data.Session;
import br.com.agendafacil.app.ui.Btn;
import br.com.agendafacil.app.ui.Ui;
import br.com.agendafacil.app.ui.Widgets;
import br.com.agendafacil.app.ui.common.Nav;

/** RF001 / UC001: autocadastro do cliente. */
public class RegisterActivity extends AppCompatActivity {
    private EditText nome, email, telefone;
    private Widgets.PasswordBox senha, confirmacao;
    private TextView erro;
    private Btn criar;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout col = Ui.column(this);
        col.setPadding(Ui.dp(this, 24), Ui.dp(this, 16), Ui.dp(this, 24), Ui.dp(this, 24));

        TextView voltar = Ui.semi(this, "‹  Voltar", 15, Ui.TITLE);
        voltar.setPadding(0, Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12));
        voltar.setOnClickListener(v -> finish());
        Ui.addWrap(col, voltar, 0);
        Ui.add(col, Ui.title(this, "Criar conta", 26), 8);
        Ui.add(col, Ui.body(this, "Cadastre-se para agendar seus serviços", 14, Ui.MUTED), 2);

        nome = Ui.field(this, "Seu nome", Ui.TEXTO());
        Ui.add(col, Ui.labeled(this, "Nome completo", nome), 24);
        email = Ui.field(this, "seu@email.com", Ui.EMAIL());
        Ui.add(col, Ui.labeled(this, "E-mail", email), 14);
        telefone = Ui.field(this, "(11) 99999-0000", Ui.TELEFONE());
        Ui.add(col, Ui.labeled(this, "Telefone (opcional)", telefone), 14);
        senha = new Widgets.PasswordBox(this, "Mínimo 6 caracteres");
        Ui.add(col, Ui.labeled(this, "Senha", senha), 14);
        confirmacao = new Widgets.PasswordBox(this, "Repita a senha");
        Ui.add(col, Ui.labeled(this, "Confirmar senha", confirmacao), 14);

        erro = Ui.body(this, "", 13, Ui.RED_FG);
        Ui.add(col, erro, 10);
        criar = Ui.button(this, "Criar conta", Ui.PRIMARIO, v -> cadastrar());
        Ui.add(col, criar, 20);
        setContentView(Ui.scroll(this, col));
    }

    private void cadastrar() {
        String n = nome.getText().toString().trim(), e = email.getText().toString().trim();
        String t = telefone.getText().toString().trim(), s = senha.texto(), c = confirmacao.texto();
        String problema = null;
        if (n.isEmpty() || e.isEmpty() || s.isEmpty()) problema = "Preencha os campos obrigatórios";
        else if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) problema = "E-mail inválido";
        else if (s.length() < 6) problema = "A senha deve ter no mínimo 6 caracteres";
        else if (!s.equals(c)) problema = "As senhas não conferem";
        erro.setText(problema == null ? "" : problema);
        if (problema != null) return;
        criar.setOcupado(true);
        Net.enqueue(Net.api().registrar(new Models.RegisterReq(n, e, t.isEmpty() ? null : t, s)), new Net.Resultado<Models.AuthRes>() {
            @Override public void onOk(Models.AuthRes r) {
                Session.save(r);
                Nav.irParaHome(RegisterActivity.this);
            }
            @Override public void onFail(String m) {
                criar.setOcupado(false);
                erro.setText(m);
            }
        });
    }
}
