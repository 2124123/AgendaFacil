package br.com.agendafacil.app.ui;

import android.content.Context;
import android.graphics.drawable.InsetDrawable;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;

/** Janela de diálogo com título e conteúdo livre (cantos arredondados). */
public class Dlg {
    public final LinearLayout corpo;
    public final AlertDialog dialogo;
    public final Context ctx;

    public Dlg(Context c, String titulo) {
        ctx = c;
        corpo = Ui.column(c);
        int p = Ui.dp(c, 20);
        corpo.setPadding(p, p, p, p);
        corpo.addView(Ui.title(c, titulo, 18));
        dialogo = new AlertDialog.Builder(c).setView(Ui.scroll(c, corpo)).create();
        if (dialogo.getWindow() != null) {
            dialogo.getWindow().setBackgroundDrawable(new InsetDrawable(Ui.shape(c, Ui.WHITE, 20, 0), Ui.dp(c, 16)));
        }
    }

    public Dlg add(android.view.View v, float topoDp) {
        Ui.add(corpo, v, topoDp);
        return this;
    }

    public void show() { dialogo.show(); }
    public void dismiss() { dialogo.dismiss(); }
}
