package br.com.agendafacil.app.ui.profissional;

import androidx.fragment.app.Fragment;
import br.com.agendafacil.app.R;
import br.com.agendafacil.app.ui.common.NotificacoesFragment;
import br.com.agendafacil.app.ui.common.PerfilFragment;
import br.com.agendafacil.app.ui.common.ShellActivity;

public class ProfissionalActivity extends ShellActivity {
    @Override protected int menuRes() { return R.menu.menu_profissional; }
    @Override protected int primeiroItem() { return R.id.nav_agenda; }
    @Override protected int itemNotificacoes() { return R.id.nav_notif; }

    @Override protected Fragment fragmentoDe(int itemId) {
        if (itemId == R.id.nav_agenda) return new AgendaFragment();
        if (itemId == R.id.nav_clientes) return new ClientesFragment();
        if (itemId == R.id.nav_notif) return new NotificacoesFragment();
        return new PerfilFragment();
    }
}
