package br.com.agendafacil.app.ui;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Formatação de datas, horas e valores em português. */
public final class Fmt {
    public static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    public static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    private Fmt() {}

    public static LocalDate hoje() { return LocalDate.now(ZONA); }
    public static LocalDate data(String iso) { return LocalDate.parse(iso); }

    private static String cap(String s) { return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(PT_BR) + s.substring(1); }

    public static String curta(LocalDate d) { return d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
    public static String diaMes(LocalDate d) { return d.format(DateTimeFormatter.ofPattern("dd/MM")); }
    public static String mesNome(LocalDate d) { return cap(d.getMonth().getDisplayName(TextStyle.FULL, PT_BR)); }
    public static String mesAno(LocalDate d) { return mesNome(d) + " " + d.getYear(); }
    public static String longa(LocalDate d) { return d.getDayOfMonth() + " de " + mesNome(d) + ", " + d.getYear(); }

    public static String mesCurto(LocalDate d) {
        String s = d.getMonth().getDisplayName(TextStyle.SHORT, PT_BR).replace(".", "").toUpperCase(PT_BR);
        return s.length() > 3 ? s.substring(0, 3) : s;
    }

    public static String diaSemanaCurto(LocalDate d) {
        String s = cap(d.getDayOfWeek().getDisplayName(TextStyle.SHORT, PT_BR).replace(".", ""));
        return s.length() > 3 ? s.substring(0, 3) : s;
    }

    public static String diaSemanaLongo(LocalDate d) { return cap(d.getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR)); }

    public static String brl(double v) { return NumberFormat.getCurrencyInstance(PT_BR).format(v); }

    public static String primeiroNome(String nome) {
        String n = nome == null ? "" : nome.trim();
        int i = n.indexOf(' ');
        return i < 0 ? n : n.substring(0, i);
    }

    public static String perfilLabel(String p) {
        if ("PROFISSIONAL".equals(p)) return "Profissional";
        if ("ADMINISTRADOR".equals(p)) return "Administrador";
        return "Cliente";
    }

    /** "2min atrás", "1h atrás", "ontem", "dd/MM". */
    public static String tempoRelativo(String iso) {
        try {
            LocalDateTime t = LocalDateTime.parse(iso);
            long min = Duration.between(t, LocalDateTime.now(ZONA)).toMinutes();
            if (min < 1) return "agora";
            if (min < 60) return min + "min atrás";
            if (min < 60 * 24) return (min / 60) + "h atrás";
            if (min < 60 * 48) return "ontem";
            return diaMes(t.toLocalDate());
        } catch (Exception e) {
            return "";
        }
    }
}
