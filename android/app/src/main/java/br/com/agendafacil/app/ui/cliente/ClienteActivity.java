package br.com.agendafacil.app.ui.cliente;

import androidx.fragment.app.Fragment;
import br.com.agendafacil.app.R;
import br.com.agendafacil.app.ui.common.PerfilFragment;
import br.com.agendafacil.app.ui.common.ShellActivity;

public class ClienteActivity extends ShellActivity {
    /** Estado do fluxo de agendamento (sobrevive à troca de abas). */
    public final BookingState booking = new BookingState();

    @Override protected int menuRes() { return R.menu.menu_cliente; }
    @Override protected int primeiroItem() { return R.id.nav_home; }

    @Override protected Fragment fragmentoDe(int itemId) {
        if (itemId == R.id.nav_home) return new HomeFragment();
        if (itemId == R.id.nav_agendar) return new BookingFragment();
        if (itemId == R.id.nav_meus) return new MeusFragment();
        return new PerfilFragment(true);
    }
}
