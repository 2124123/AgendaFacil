package br.com.agendafacil.app.data;

import android.os.Handler;
import android.os.Looper;
import br.com.agendafacil.app.AgendaApp;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class Net {
    private static ApiService api;

    private Net() {}

    public interface Ok<T> { void run(T valor); }
    public interface Fail { void run(String mensagem); }
    public interface Resultado<T> { void onOk(T corpo); void onFail(String mensagem); }

    /** Descarta o cliente atual (usado quando o endereço do servidor muda). */
    public static synchronized void reiniciar() { api = null; }

    public static synchronized ApiService api() {
        if (api == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request.Builder b = chain.request().newBuilder();
                        String token = Session.token();
                        if (token != null) b.header("Authorization", "Bearer " + token);
                        Request req = b.build();
                        okhttp3.Response res = chain.proceed(req);
                        // Token expirado/inválido fora das telas de login: volta para o login.
                        if (res.code() == 401 && !req.url().encodedPath().startsWith("/api/auth")) {
                            Session.clear();
                            new Handler(Looper.getMainLooper()).post(AgendaApp::irParaLogin);
                        }
                        return res;
                    })
                    .build();
            api = new Retrofit.Builder()
                    .baseUrl(Server.url())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
        return api;
    }

    /** Executa a chamada em segundo plano e devolve o resultado na thread principal. */
    public static <T> void enqueue(Call<T> call, Resultado<T> cb) {
        call.enqueue(new Callback<T>() {
            @Override public void onResponse(Call<T> c, Response<T> r) {
                if (r.isSuccessful()) cb.onOk(r.body()); else cb.onFail(mensagem(r));
            }

            @Override public void onFailure(Call<T> c, Throwable t) {
                if (c.isCanceled()) return;
                cb.onFail(t instanceof IOException
                        ? "Sem conexão com o servidor. Verifique a internet e o endereço da API."
                        : (t.getMessage() == null ? "Erro inesperado" : t.getMessage()));
            }
        });
    }

    private static String mensagem(Response<?> r) {
        try {
            String corpo = r.errorBody() == null ? null : r.errorBody().string();
            if (corpo != null) {
                com.google.gson.JsonElement m = JsonParser.parseString(corpo).getAsJsonObject().get("message");
                if (m != null && !m.isJsonNull()) return m.getAsString();
            }
        } catch (Exception ignorar) {
            // corpo sem JSON: usa mensagem padrão
        }
        switch (r.code()) {
            case 401: return "Sessão expirada. Entre novamente.";
            case 403: return "Você não tem permissão para esta ação.";
            case 404: return "Registro não encontrado.";
            default: return "Erro " + r.code() + " no servidor.";
        }
    }
}
