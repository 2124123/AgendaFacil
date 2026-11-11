package br.com.agendafacil.app.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.agendafacil.app.R;

import java.util.HashMap;
import java.util.Map;

/** Componentes visuais do app (cores e fontes do protótipo do Figma). */
public final class Ui {
    public static final int PRIMARY = 0xFF0891B2, TITLE = 0xFF134E5E, CORAL = 0xFFFF7F5C, CORAL_BG = 0xFFFFEDE8;
    public static final int BG = 0xFFF8FAFC, BORDER = 0xFFE2E8F0, TEXT = 0xFF0F172A, MUTED = 0xFF64748B, WHITE = 0xFFFFFFFF;
    public static final int CYAN_BG = 0xFFECFEFF, GREEN_BG = 0xFFDCFCE7, GREEN_FG = 0xFF15803D;
    public static final int AMBER_BG = 0xFFFEF3C7, AMBER_FG = 0xFFB45309, RED_BG = 0xFFFEE2E2, RED_FG = 0xFFB91C1C;

    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT, WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    private static final Map<String, Typeface> FONTES = new HashMap<>();
    private static final Map<String, Integer> FOTOS = new HashMap<>();
    private static final int[] CORES_AVATAR = {0xFF0891B2, 0xFFFF7F5C, 0xFF0E7490, 0xFF7C3AED, 0xFF059669, 0xFFD97706};

    static {
        // Fotos do protótipo (dados de demonstração). Sem foto, o avatar mostra as iniciais.
        FOTOS.put("ana paula", R.drawable.foto_ana);
        FOTOS.put("mariana costa", R.drawable.foto_mariana);
        FOTOS.put("carlos souza", R.drawable.foto_carlos);
        FOTOS.put("beatriz xavier", R.drawable.foto_beatriz);
        FOTOS.put("juliana prado", R.drawable.foto_juliana);
    }

    private Ui() {}

    public static int dp(Context c, float v) { return Math.round(v * c.getResources().getDisplayMetrics().density); }

    // ------------------------------------------------------------------ texto

    /** Fontes variáveis do Figma: Outfit (títulos) e DM Sans (textos). */
    public static Typeface fonte(Context c, boolean outfit, int peso) {
        String chave = (outfit ? "o" : "d") + peso;
        Typeface t = FONTES.get(chave);
        if (t == null) {
            try {
                t = new Typeface.Builder(c.getAssets(), outfit ? "fonts/Outfit.ttf" : "fonts/DMSans.ttf")
                        .setFontVariationSettings("'wght' " + peso).setWeight(peso).build();
            } catch (Exception e) {
                t = null;
            }
            if (t == null) t = peso >= 600 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT;
            FONTES.put(chave, t);
        }
        return t;
    }

    public static TextView text(Context c, CharSequence s, float sp, int cor, int peso, boolean outfit) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(cor);
        t.setTypeface(fonte(c, outfit, peso));
        return t;
    }

    public static TextView title(Context c, CharSequence s, float sp) { return text(c, s, sp, TEXT, 700, true); }
    public static TextView body(Context c, CharSequence s, float sp, int cor) { return text(c, s, sp, cor, 400, false); }
    public static TextView semi(Context c, CharSequence s, float sp, int cor) { return text(c, s, sp, cor, 600, false); }
    public static TextView bold(Context c, CharSequence s, float sp, int cor) { return text(c, s, sp, cor, 700, false); }

    // ------------------------------------------------------------------ formas e layouts

    public static GradientDrawable shape(Context c, int fundo, float raioDp, int borda) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fundo);
        g.setCornerRadius(dp(c, raioDp));
        if (borda != 0) g.setStroke(dp(c, 1), borda);
        return g;
    }

    public static Drawable ripple(Context c, int fundo, float raioDp, int borda) {
        return new RippleDrawable(ColorStateList.valueOf(0x330891B2), shape(c, fundo, raioDp, borda), shape(c, WHITE, raioDp, 0));
    }

    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    /** Mantém a altura fixa já definida na view (ex.: divisor de 1dp); senão, WRAP_CONTENT. */
    private static int altura(View v) {
        ViewGroup.LayoutParams p = v.getLayoutParams();
        return p != null && p.height > 0 ? p.height : WRAP;
    }

    /** Adiciona ocupando toda a largura, com margem superior. */
    public static void add(ViewGroup pai, View v, float topoDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, altura(v));
        lp.topMargin = dp(pai.getContext(), topoDp);
        pai.addView(v, lp);
    }

    public static void addWrap(ViewGroup pai, View v, float topoDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP, altura(v));
        lp.topMargin = dp(pai.getContext(), topoDp);
        pai.addView(v, lp);
    }

    /** Em uma linha horizontal: largura proporcional (peso 1) e margem à esquerda. */
    public static void addW(ViewGroup pai, View v, float esquerdaDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, altura(v), 1f);
        lp.leftMargin = dp(pai.getContext(), esquerdaDp);
        pai.addView(v, lp);
    }

    public static void addSize(ViewGroup pai, float larguraDp, float alturaDp, float esquerdaDp, View v) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(pai.getContext(), larguraDp), dp(pai.getContext(), alturaDp));
        lp.leftMargin = dp(pai.getContext(), esquerdaDp);
        pai.addView(v, lp);
    }

    public static View space(Context c, float alturaDp) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(c, alturaDp)));
        return v;
    }

    public static View divider(Context c) {
        View v = new View(c);
        v.setBackgroundColor(BORDER);
        v.setLayoutParams(new LinearLayout.LayoutParams(MATCH, dp(c, 1)));
        return v;
    }

    public static ScrollView scroll(Context c, View conteudo) {
        ScrollView s = new ScrollView(c);
        s.setFillViewport(true);
        s.addView(conteudo, new ViewGroup.LayoutParams(MATCH, WRAP));
        return s;
    }

    public static LinearLayout card(Context c) {
        LinearLayout l = column(c);
        int p = dp(c, 16);
        l.setPadding(p, p, p, p);
        l.setBackground(shape(c, WHITE, 16, BORDER));
        l.setElevation(dp(c, 1));
        return l;
    }

    public static LinearLayout clickCard(Context c, View.OnClickListener ouvinte) {
        LinearLayout l = card(c);
        l.setBackground(ripple(c, WHITE, 16, BORDER));
        l.setOnClickListener(ouvinte);
        return l;
    }

    public static TextView sectionTitle(Context c, String s) { return title(c, s, 16); }

    // ------------------------------------------------------------------ campos e botões

    public static final int PRIMARIO = 0, SUAVE = 1, PERIGO = 2, CONTORNO = 3;

    public static Btn button(Context c, String texto, int tipo, View.OnClickListener ouvinte) {
        Btn b = new Btn(c, texto, tipo);
        b.setOnClickListener(ouvinte);
        return b;
    }

    public static EditText field(Context c, String dica, int tipoEntrada) {
        EditText e = new EditText(c);
        e.setHint(dica);
        e.setHintTextColor(MUTED);
        e.setTextColor(TEXT);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        e.setTypeface(fonte(c, false, 400));
        e.setInputType(tipoEntrada);
        e.setSingleLine(true);
        e.setGravity(Gravity.CENTER_VERTICAL);
        e.setBackground(shape(c, WHITE, 12, BORDER));
        e.setPadding(dp(c, 16), 0, dp(c, 16), 0);
        e.setMinimumHeight(dp(c, 52));
        return e;
    }

    public static int TEXTO() { return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES; }
    public static int EMAIL() { return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; }
    public static int TELEFONE() { return InputType.TYPE_CLASS_PHONE; }
    public static int NUMERO() { return InputType.TYPE_CLASS_NUMBER; }
    public static int DECIMAL() { return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL; }

    public static LinearLayout labeled(Context c, String rotulo, View entrada) {
        LinearLayout l = column(c);
        if (rotulo != null && !rotulo.isEmpty()) {
            l.addView(text(c, rotulo, 14, TITLE, 500, false));
            add(l, entrada, 6);
        } else {
            add(l, entrada, 0);
        }
        return l;
    }

    // ------------------------------------------------------------------ chips, avatar e estados

    public static TextView pill(Context c, String texto, int fundo, int cor) {
        TextView t = text(c, texto, 11, cor, 700, false);
        t.setBackground(shape(c, fundo, 50, 0));
        t.setPadding(dp(c, 10), dp(c, 4), dp(c, 10), dp(c, 4));
        t.setSingleLine(true);
        return t;
    }

    public static TextView statusChip(Context c, String status) {
        switch (status == null ? "" : status) {
            case "CONFIRMADO": return pill(c, "Confirmado", GREEN_BG, GREEN_FG);
            case "PENDENTE": return pill(c, "Pendente", AMBER_BG, AMBER_FG);
            case "CANCELADO": return pill(c, "Cancelado", RED_BG, RED_FG);
            case "REALIZADO": return pill(c, "Realizado", CYAN_BG, PRIMARY);
            default: return pill(c, String.valueOf(status), BORDER, MUTED);
        }
    }

    public static View avatar(Context c, String nome, float tamanhoDp) {
        String n = nome == null ? "" : nome.trim();
        Integer foto = FOTOS.get(n.toLowerCase(Fmt.PT_BR));
        int px = dp(c, tamanhoDp);
        GradientDrawable oval = new GradientDrawable();
        oval.setShape(GradientDrawable.OVAL);
        if (foto != null) {
            ImageView img = new ImageView(c);
            img.setImageResource(foto);
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            oval.setColor(BORDER);
            img.setBackground(oval);
            img.setClipToOutline(true);
            img.setLayoutParams(new LinearLayout.LayoutParams(px, px));
            return img;
        }
        StringBuilder iniciais = new StringBuilder();
        for (String parte : n.split("\\s+")) {
            if (!parte.isEmpty() && iniciais.length() < 2) iniciais.append(Character.toUpperCase(parte.charAt(0)));
        }
        TextView t = text(c, iniciais, tamanhoDp * 0.36f, WHITE, 700, false);
        t.setGravity(Gravity.CENTER);
        oval.setColor(CORES_AVATAR[Math.abs(n.hashCode()) % CORES_AVATAR.length]);
        t.setBackground(oval);
        t.setLayoutParams(new LinearLayout.LayoutParams(px, px));
        return t;
    }

    public static View loading(Context c) {
        FrameLayout f = new FrameLayout(c);
        f.setTag("loading");
        f.setPadding(0, dp(c, 40), 0, dp(c, 40));
        ProgressBar p = new ProgressBar(c);
        p.setIndeterminateTintList(ColorStateList.valueOf(PRIMARY));
        f.addView(p, new FrameLayout.LayoutParams(dp(c, 36), dp(c, 36), Gravity.CENTER));
        return f;
    }

    public static View errorView(Context c, String mensagem, Runnable tentarDeNovo) {
        LinearLayout l = column(c);
        l.setGravity(Gravity.CENTER_HORIZONTAL);
        l.setPadding(dp(c, 16), dp(c, 32), dp(c, 16), dp(c, 16));
        TextView t = body(c, mensagem, 14, RED_FG);
        t.setGravity(Gravity.CENTER);
        l.addView(t);
        Btn b = button(c, "Tentar novamente", SUAVE, v -> tentarDeNovo.run());
        addWrap(l, b, 12);
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(c, 200), dp(c, 48)));
        return l;
    }

    public static View empty(Context c, String mensagem) {
        TextView t = body(c, mensagem, 14, MUTED);
        t.setPadding(dp(c, 8), dp(c, 32), dp(c, 8), dp(c, 32));
        return t;
    }

    public static void toast(Context c, String mensagem) {
        if (c != null) Toast.makeText(c.getApplicationContext(), mensagem, Toast.LENGTH_LONG).show();
    }
}
