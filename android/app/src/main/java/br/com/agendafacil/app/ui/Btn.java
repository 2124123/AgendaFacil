package br.com.agendafacil.app.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Botão arredondado do protótipo (primário, suave, perigo ou contorno). */
public class Btn extends TextView {
    private final String rotulo;

    public Btn(Context c, String texto, int tipo) {
        super(c);
        rotulo = texto;
        int fundo, cor, borda = 0;
        switch (tipo) {
            case Ui.SUAVE: fundo = Ui.CYAN_BG; cor = Ui.PRIMARY; break;
            case Ui.PERIGO: fundo = Ui.RED_BG; cor = Ui.RED_FG; break;
            case Ui.CONTORNO: fundo = Ui.WHITE; cor = Ui.PRIMARY; borda = Ui.PRIMARY; break;
            default: fundo = Ui.PRIMARY; cor = Ui.WHITE; break;
        }
        setText(texto);
        setTextColor(cor);
        setTextSize(15);
        setTypeface(Ui.fonte(c, false, 600));
        setGravity(Gravity.CENTER);
        setSingleLine(true);
        setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 12), 0);
        setBackground(Ui.ripple(c, fundo, 14, borda));
        setMinHeight(Ui.dp(c, 52));
        setClickable(true);
        setFocusable(true);
        setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 52)));
    }

    public void setHabilitado(boolean sim) {
        setEnabled(sim);
        setAlpha(sim ? 1f : 0.5f);
    }

    public void setOcupado(boolean ocupado) {
        setEnabled(!ocupado);
        setAlpha(ocupado ? 0.6f : 1f);
        setText(ocupado ? "Aguarde..." : rotulo);
    }
}
