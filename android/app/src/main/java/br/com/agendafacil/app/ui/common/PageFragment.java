package br.com.agendafacil.app.ui.common;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import br.com.agendafacil.app.data.Net;
import br.com.agendafacil.app.ui.Ui;
import retrofit2.Call;

/**
 * Base das telas: um cabeçalho fixo ({@link #top}) e uma área de conteúdo ({@link #list}) que é
 * redesenhada a cada carregamento. Puxar para baixo chama {@link #reload()}.
 */
public abstract class PageFragment extends Fragment {
    protected SwipeRefreshLayout srl;
    protected LinearLayout top, list;
    protected Context c;
    protected boolean atualizavel = true;

    @Nullable @Override
    public android.view.View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        c = requireContext();
        srl = new SwipeRefreshLayout(c);
        srl.setColorSchemeColors(Ui.PRIMARY);
        srl.setEnabled(atualizavel);
        srl.setBackgroundColor(Ui.BG);
        ScrollView sv = new ScrollView(c);
        sv.setFillViewport(true);
        LinearLayout col = Ui.column(c);
        col.setPadding(Ui.dp(c, 20), Ui.dp(c, 8), Ui.dp(c, 20), Ui.dp(c, 24));
        top = Ui.column(c);
        list = Ui.column(c);
        col.addView(top, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(list, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        sv.addView(col, new ViewGroup.LayoutParams(Ui.MATCH, Ui.WRAP));
        srl.addView(sv);
        srl.setOnRefreshListener(this::reload);
        montarTopo();
        return srl;
    }

    /** Cabeçalho que não é apagado ao recarregar (título, busca, abas). */
    protected void montarTopo() {}

    /** Recarrega os dados e redesenha {@link #list}. */
    protected abstract void reload();

    @Override public void onStart() {
        super.onStart();
        reload();
    }

    /** Título grande + subtítulo, como nas telas do Figma. */
    protected android.view.View cabecalho(String titulo, String subtitulo) {
        LinearLayout l = Ui.column(c);
        l.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 8));
        l.addView(Ui.title(c, titulo, 24));
        if (subtitulo != null) l.addView(Ui.body(c, subtitulo, 13, Ui.MUTED));
        return l;
    }

    /** Executa a chamada protegendo contra tela já fechada. */
    protected <T> void api(Call<T> call, Net.Ok<T> ok, Net.Fail falha) {
        Net.enqueue(call, new Net.Resultado<T>() {
            @Override public void onOk(T corpo) { if (isAdded()) ok.run(corpo); }
            @Override public void onFail(String msg) {
                if (!isAdded()) return;
                if (falha != null) falha.run(msg); else Ui.toast(c, msg);
            }
        });
    }

    protected <T> void api(Call<T> call, Net.Ok<T> ok) { api(call, ok, null); }

    /** Carrega a lista: mostra "carregando", depois desenha o resultado ou um erro com "tentar novamente". */
    protected <T> void fetch(Call<T> call, Net.Ok<T> desenhar) {
        final boolean jaTinha = list.getChildCount() > 0 && !"loading".equals(list.getChildAt(0).getTag());
        if (!jaTinha) {
            list.removeAllViews();
            list.addView(Ui.loading(c));
        }
        api(call, r -> {
            list.removeAllViews();
            desenhar.run(r);
            srl.setRefreshing(false);
        }, m -> {
            srl.setRefreshing(false);
            if (jaTinha) Ui.toast(c, m);
            else {
                list.removeAllViews();
                list.addView(Ui.errorView(c, m, this::reload));
            }
        });
    }

    protected ShellActivity shell() { return (ShellActivity) requireActivity(); }
}
