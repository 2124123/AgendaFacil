package br.com.agendafacil.app.ui.cliente;

import br.com.agendafacil.app.data.Models;

import java.time.LocalDate;

/** Escolhas do cliente nas 4 etapas: 1 Serviço, 2 Profissional, 3 Data/Hora, 4 Confirmação. */
public class BookingState {
    public int etapa = 1;
    public String categoria;
    public Models.Servico servico;
    public Models.Profissional profissional;
    public LocalDate data;
    public String hora;
    public Models.Agendamento resultado;

    public void limpar() {
        etapa = 1; categoria = null; servico = null; profissional = null; data = null; hora = null; resultado = null;
    }

    public void escolherServico(Models.Servico s) {
        servico = s; profissional = null; data = null; hora = null; resultado = null; etapa = 2;
    }

    public void escolherProfissional(Models.Profissional p) {
        profissional = p; data = null; hora = null; etapa = 3;
    }

    public boolean voltar() {
        if (etapa == 3) { data = null; hora = null; etapa = 2; return true; }
        if (etapa == 2) { profissional = null; etapa = 1; return true; }
        return false;
    }
}
