package br.com.agendafacil.app.data;

import br.com.agendafacil.app.data.Models.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {
    // ---- autenticação / perfil
    @POST("api/auth/login") Call<AuthRes> login(@Body LoginReq b);
    @POST("api/auth/register") Call<AuthRes> registrar(@Body RegisterReq b);
    @PUT("api/me") Call<Usuario> atualizarPerfil(@Body PerfilUpdate b);

    // ---- consultas (cliente)
    @GET("api/servicos") Call<List<Servico>> servicos(@Query("busca") String busca, @Query("categoria") String categoria);
    @GET("api/profissionais") Call<List<Profissional>> profissionais(@Query("servicoId") Long servicoId);
    @GET("api/agenda/dias") Call<List<DiaDisponivel>> diasDisponiveis(
            @Query("profissionalId") long profissionalId, @Query("servicoId") long servicoId,
            @Query("inicio") String inicio, @Query("dias") int dias);
    @GET("api/agenda/horarios") Call<List<String>> horariosLivres(
            @Query("profissionalId") long profissionalId, @Query("servicoId") long servicoId,
            @Query("data") String data, @Query("ignorarId") Long ignorarId);

    // ---- agendamentos do cliente
    @POST("api/cliente/agendamentos") Call<Agendamento> criarAgendamento(@Body AgendamentoReq b);
    @GET("api/cliente/agendamentos") Call<List<Agendamento>> meusAgendamentos(@Query("tipo") String tipo);
    @PUT("api/cliente/agendamentos/{id}/remarcar") Call<Agendamento> remarcarCliente(@Path("id") long id, @Body RemarcarReq b);
    @PATCH("api/cliente/agendamentos/{id}/cancelar") Call<Agendamento> cancelarCliente(@Path("id") long id);

    // ---- profissional
    @GET("api/profissional/agenda") Call<Agenda> agendaProfissional(@Query("data") String data, @Query("dias") int dias);
    @PATCH("api/profissional/agendamentos/{id}/confirmar") Call<Agendamento> confirmarAtendimento(@Path("id") long id);
    @PATCH("api/profissional/agendamentos/{id}/realizar") Call<Agendamento> realizarAtendimento(@Path("id") long id);
    @PATCH("api/profissional/agendamentos/{id}/cancelar") Call<Agendamento> cancelarProfissional(@Path("id") long id);
    @POST("api/profissional/bloqueios") Call<Bloqueio> bloquear(@Body BloqueioReq b);
    @DELETE("api/profissional/bloqueios/{id}") Call<Void> removerBloqueio(@Path("id") long id);
    @GET("api/profissional/clientes") Call<List<ClienteResumo>> clientesAtendidos();

    // ---- notificações
    @GET("api/notificacoes") Call<List<Notificacao>> notificacoes();
    @PATCH("api/notificacoes/{id}/lida") Call<Void> marcarLida(@Path("id") long id);
    @POST("api/notificacoes/lidas") Call<Void> marcarTodasLidas();

    // ---- administrador
    @GET("api/admin/servicos") Call<List<Servico>> adminServicos(@Query("busca") String busca);
    @POST("api/admin/servicos") Call<Servico> adminCriarServico(@Body ServicoForm f);
    @PUT("api/admin/servicos/{id}") Call<Servico> adminAtualizarServico(@Path("id") long id, @Body ServicoForm f);
    @PATCH("api/admin/servicos/{id}/ativo") Call<Servico> adminAtivoServico(@Path("id") long id, @Query("valor") boolean valor);
    @GET("api/admin/profissionais") Call<List<Profissional>> adminProfissionais();
    @POST("api/admin/profissionais") Call<Profissional> adminCriarProfissional(@Body ProfissionalForm f);
    @PUT("api/admin/profissionais/{id}") Call<Profissional> adminAtualizarProfissional(@Path("id") long id, @Body ProfissionalForm f);
    @PATCH("api/admin/profissionais/{id}/ativo") Call<Profissional> adminAtivoProfissional(@Path("id") long id, @Query("valor") boolean valor);
    @GET("api/admin/profissionais/{id}/disponibilidade") Call<List<Disponibilidade>> adminDisponibilidade(@Path("id") long id);
    @PUT("api/admin/profissionais/{id}/disponibilidade") Call<List<Disponibilidade>> adminDefinirDisponibilidade(@Path("id") long id, @Body List<Disponibilidade> l);
    @GET("api/admin/dashboard") Call<Dashboard> adminDashboard();
    @GET("api/admin/relatorios/agendamentos") Call<Relatorio> adminRelatorio(@Query("inicio") String inicio, @Query("fim") String fim);
}
