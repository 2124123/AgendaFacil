package br.com.agendafacil.app.ui.common;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import br.com.agendafacil.app.AgendaApp;
import br.com.agendafacil.app.R;
import br.com.agendafacil.app.data.Models;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.data.Session;
import br.com.agendafacil.app.ui.Ui;

import java.util.List;

/** Tela principal de cada perfil: conteúdo (fragments) + barra de navegação inferior. */
public abstract class ShellActivity extends AppCompatActivity {
    protected BottomNavigationView nav;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable verificarAvisos = new Runnable() {
        @Override public void run() {
            atualizarBadge();
            handler.postDelayed(this, 30_000);
        }
    };

    protected abstract int menuRes();
    protected abstract int primeiroItem();
    protected abstract Fragment fragmentoDe(int itemId);

    /** Id do item "notificações" (para exibir o contador de não lidas) ou 0. */
    protected int itemNotificacoes() { return 0; }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Session.get() == null) { AgendaApp.irParaLogin(); finish(); return; }

        LinearLayout raiz = Ui.column(this);
        raiz.setBackgroundColor(Ui.BG);
        FrameLayout container = new FrameLayout(this);
        container.setId(R.id.container);
        raiz.addView(container, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));
        View linha = Ui.divider(this);
        raiz.addView(linha);

        nav = new BottomNavigationView(this);
        nav.inflateMenu(menuRes());
        nav.setBackgroundColor(Ui.WHITE);
        ColorStateList cores = new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {}}, new int[]{Ui.PRIMARY, Ui.MUTED});
        nav.setItemIconTintList(cores);
        nav.setItemTextColor(cores);
        nav.setItemActiveIndicatorColor(ColorStateList.valueOf(Ui.CYAN_BG));
        nav.setOnItemSelectedListener(item -> {
            getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentoDe(item.getItemId())).commit();
            return true;
        });
        raiz.addView(nav, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        setContentView(raiz);

        if (savedInstanceState == null) nav.setSelectedItemId(primeiroItem());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                Fragment atual = getSupportFragmentManager().findFragmentById(R.id.container);
                if (atual instanceof BackAware && ((BackAware) atual).onBack()) return;
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else if (nav.getSelectedItemId() != primeiroItem()) {
                    nav.setSelectedItemId(primeiroItem());
                } else {
                    finish();
                }
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (itemNotificacoes() != 0) handler.post(verificarAvisos);
    }

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(verificarAvisos);
    }

    public void selecionarAba(int itemId) { nav.setSelectedItemId(itemId); }

    /** Abre uma tela por cima da aba atual (volta com o botão voltar). */
    public void abrir(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, f).addToBackStack(null).commit();
    }

    public void atualizarBadge() {
        final int id = itemNotificacoes();
        if (id == 0 || Session.get() == null) return;
        Net.enqueue(Net.api().notificacoes(), new Net.Resultado<List<Models.Notificacao>>() {
            @Override public void onOk(List<Models.Notificacao> r) {
                int n = 0;
                for (Models.Notificacao x : r) if (!x.lida) n++;
                if (n > 0) {
                    BadgeDrawable b = nav.getOrCreateBadge(id);
                    b.setNumber(n);
                    b.setBackgroundColor(Ui.CORAL);
                } else {
                    nav.removeBadge(id);
                }
            }
            @Override public void onFail(String m) { /* silencioso: tenta de novo no próximo ciclo */ }
        });
    }
}
