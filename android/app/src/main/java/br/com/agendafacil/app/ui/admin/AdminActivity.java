package br.com.agendafacil.app.ui.admin;

import androidx.fragment.app.Fragment;
import br.com.agendafacil.app.R;
import br.com.agendafacil.app.ui.common.NotificacoesFragment;
import br.com.agendafacil.app.ui.common.PerfilFragment;
import br.com.agendafacil.app.ui.common.ShellActivity;

public class AdminActivity extends ShellActivity {
    @Override protected int menuRes() { return R.menu.menu_admin; }
    @Override protected int primeiroItem() { return R.id.nav_painel; }
    @Override protected int itemNotificacoes() { return R.id.nav_notif; }

    @Override protected Fragment fragmentoDe(int itemId) {
        if (itemId == R.id.nav_painel) return new PainelFragment();
        if (itemId == R.id.nav_servicos) return new ServicosFragment();
        if (itemId == R.id.nav_equipe) return new EquipeFragment();
        if (itemId == R.id.nav_notif) return new NotificacoesFragment();
        return new PerfilFragment();
    }
}
