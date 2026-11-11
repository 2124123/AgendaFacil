package br.com.agendafacil.app.data;

import java.util.List;

/** Objetos trocados com a API (campos públicos para o Gson). */
public final class Models {
    private Models() {}

    public static class LoginReq {
        public String email, senha, perfil;
        public LoginReq(String email, String senha, String perfil) { this.email = email; this.senha = senha; this.perfil = perfil; }
    }

    public static class RegisterReq {
        public String nome, email, telefone, senha;
        public RegisterReq(String nome, String email, String telefone, String senha) {
            this.nome = nome; this.email = email; this.telefone = telefone; this.senha = senha;
        }
    }

    public static class Usuario { public long id; public String nome, email, telefone, perfil; }

    public static class AuthRes { public String token; public Usuario usuario; public String estabelecimento; }

    public static class PerfilUpdate {
        public String nome, telefone;
        public PerfilUpdate(String nome, String telefone) { this.nome = nome; this.telefone = telefone; }
    }

    public static class Servico {
        public long id; public String nome, descricao, categoria; public int duracaoMinutos; public double preco; public boolean ativo;
    }

    public static class ServicoForm {
        public String nome, descricao, categoria; public int duracaoMinutos; public double preco;
        public ServicoForm(String nome, String descricao, String categoria, int duracaoMinutos, double preco) {
            this.nome = nome; this.descricao = descricao; this.categoria = categoria; this.duracaoMinutos = duracaoMinutos; this.preco = preco;
        }
    }

    public static class Profissional {
        public long id; public String nome, email, telefone, especialidade; public boolean ativo;
    }

    public static class ProfissionalForm {
        public String nome, email, telefone, especialidade, senha;
        public ProfissionalForm(String nome, String email, String telefone, String especialidade, String senha) {
            this.nome = nome; this.email = email; this.telefone = telefone; this.especialidade = especialidade; this.senha = senha;
        }
    }

    public static class Disponibilidade {
        public String diaSemana, horaInicio, horaFim;
        public Disponibilidade(String diaSemana, String horaInicio, String horaFim) {
            this.diaSemana = diaSemana; this.horaInicio = horaInicio; this.horaFim = horaFim;
        }
    }

    public static class AgendamentoReq {
        public long profissionalId, servicoId; public String data, horaInicio;
        public AgendamentoReq(long profissionalId, long servicoId, String data, String horaInicio) {
            this.profissionalId = profissionalId; this.servicoId = servicoId; this.data = data; this.horaInicio = horaInicio;
        }
    }

    public static class RemarcarReq {
        public String data, horaInicio;
        public RemarcarReq(String data, String horaInicio) { this.data = data; this.horaInicio = horaInicio; }
    }

    public static class Agendamento {
        public long id, servicoId, profissionalId, clienteId;
        public String servico, categoria, profissional, cliente, clienteTelefone;
        public String data, horaInicio, horaFim, status, local, endereco;
        public double valor;
    }

    public static class DiaDisponivel { public String data; public int horarios; }

    public static class BloqueioReq {
        public String data, horaInicio, horaFim, motivo;
        public BloqueioReq(String data, String horaInicio, String horaFim, String motivo) {
            this.data = data; this.horaInicio = horaInicio; this.horaFim = horaFim; this.motivo = motivo;
        }
    }

    public static class Bloqueio { public long id; public String data, horaInicio, horaFim, motivo; }

    public static class Agenda { public List<Agendamento> agendamentos; public List<Bloqueio> bloqueios; }

    public static class ClienteResumo {
        public long id, atendimentos; public String nome, telefone, email, ultimoAtendimento;
    }

    public static class Notificacao { public long id; public String tipo, mensagem, criadaEm; public boolean lida; }

    public static class ProfissionalResumo { public long id, agendamentosHoje; public String nome, especialidade; }

    public static class Dashboard {
        public String estabelecimento; public long agendamentosHoje; public Integer variacaoPercentual;
        public int taxaOcupacao; public List<Integer> semana; public List<ProfissionalResumo> profissionais;
    }

    public static class Relatorio {
        public String inicio, fim; public long total, pendentes, confirmados, realizados, cancelados;
        public double receita; public List<Agendamento> itens;
    }

    /** Sessão guardada no aparelho. */
    public static class SessionData { public String token; public Usuario usuario; public String estabelecimento; }
}
