package br.com.agendafacil.app.ui.common;

/** Fragment que trata o botão voltar internamente (ex.: etapas do agendamento). */
public interface BackAware {
    /** @return true se consumiu o "voltar". */
    boolean onBack();
}
