# 🌐 Usar o Agenda Fácil em outros celulares (qualquer rede)

Para o app funcionar em celulares que **não estão no seu Wi-Fi**, o servidor precisa estar acessível pela internet.
Hoje o servidor roda no seu computador, e por isso só celulares na mesma rede conseguem falar com ele.

Existem dois caminhos. Escolha um:

| | Caminho A — Túnel (rápido) | Caminho B — Servidor na nuvem (definitivo) |
|---|---|---|
| Quando usar | Apresentação ou teste por algumas horas | Uso contínuo, 24 horas |
| Seu computador | **Precisa ficar ligado** com o servidor rodando | Pode ficar desligado |
| Custo | Grátis | Depende do plano de hospedagem (confira os planos atuais) |
| Dificuldade | Fácil (10 minutos) | Média (30 a 60 minutos) |
| Endereço | Muda cada vez que reinicia | Fixo |

Nos dois casos, o resto é igual: **gerar o APK** e **instalar nos celulares** (Parte 3).

---

## Caminho A — Túnel (rápido)

Um "túnel" dá ao seu computador um endereço público `https://...` temporário.

1. Suba o servidor normalmente (em `backend/`):
   `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`
2. Instale o **cloudflared** (Cloudflare, gratuito e sem criar conta): https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/downloads/
3. Em **outro terminal**, rode:
   `cloudflared tunnel --url http://localhost:8080`
4. Aparece uma linha parecida com `https://alguma-coisa.trycloudflare.com`. **Esse é o endereço do servidor.** Copie.
5. No app (qualquer celular), na tela de login toque em **⚙ Servidor**, cole o endereço, e toque em **Salvar e testar conexão**. Deve aparecer "✓ Servidor encontrado e salvo".

Atenção:
- Mantenha os dois terminais abertos (servidor e túnel).
- O endereço **muda toda vez** que você reinicia o túnel; será preciso atualizar no app.
- Alternativa equivalente: **ngrok** (`ngrok http 8080`), que exige criar uma conta gratuita.

---

## Caminho B — Servidor na nuvem

Você precisa de **duas coisas** na nuvem: o servidor (o projeto da pasta `backend/`) e um **banco MySQL**.
O projeto já inclui um `Dockerfile`, então qualquer hospedagem que aceite Docker serve (Railway, Render, Fly.io, uma VPS etc.).

O exemplo abaixo usa o **Railway**, porque ele oferece MySQL e publicação direta a partir do GitHub. Os nomes dos botões podem mudar com o tempo.

### 1. Coloque o projeto no GitHub
Crie um repositório e envie a pasta `agenda-facil` inteira (com `backend/` e `android/`).

### 2. Crie o banco e o servidor
1. Entre em https://railway.app e crie um **New Project**.
2. Adicione **MySQL** (Add → Database → MySQL).
3. Adicione o servidor: **New → GitHub Repo** e escolha o seu repositório.
4. Nas configurações do serviço do servidor, defina o **Root Directory** como `backend`.

### 3. Configure as variáveis do servidor
No serviço do servidor, aba **Variables**, crie:

| Variável | Valor |
|---|---|
| `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo` |
| `DB_USER` | `${{MySQL.MYSQLUSER}}` |
| `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
| `JWT_SECRET` | uma frase longa e secreta (**mínimo 32 caracteres**) que só você conhece |

Se o seu provedor usar outros nomes para as variáveis do MySQL, use host, porta, nome do banco, usuário e senha que ele mostrar.
O formato do `DB_URL` é sempre `jdbc:mysql://HOST:PORTA/NOME_DO_BANCO?...`.

### 4. Gere o endereço público
No serviço do servidor, vá em **Settings → Networking → Generate Domain**.
Você recebe algo como `https://agenda-facil-production.up.railway.app`. **Esse é o endereço do servidor.**

### 5. Confira
Abra o endereço no navegador acrescentando `/api/servicos`.
Se aparecer uma página de erro **401**, está tudo certo: o servidor está no ar e pedindo login.
(A primeira inicialização demora, pois o banco é criado e populado com os dados de exemplo.)

### Cuidados com o servidor público
- As contas de demonstração (`admin@agendafacil.com`, senha `12345678` etc.) ficam **abertas para qualquer pessoa**. Para uso real, **troque as senhas** (pelo perfil do app) ou crie os seus próprios usuários.
- Depois da primeira execução, você pode definir `AGENDA_SEED=false` para não recarregar os dados de exemplo.
- Nunca divulgue o valor do `JWT_SECRET`.

---

## Parte 3 — Gerar o APK e instalar nos celulares

### Gerar o APK
**Opção 1 — pelo Android Studio (mais fácil)**
1. Abra a pasta `android/`.
2. Menu **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
3. Quando terminar, clique em **locate** na notificação. O arquivo é `app-debug.apk`
   (caminho: `android/app/build/outputs/apk/debug/app-debug.apk`).

**Opção 2 — já apontando para o seu servidor** (o usuário não precisa configurar nada)
Na pasta `android/`:
```bash
./gradlew assembleDebug -PAPI_URL=https://SEU-SERVIDOR.up.railway.app/      # Windows: .\gradlew.bat assembleDebug "-PAPI_URL=https://SEU-SERVIDOR.up.railway.app/"
```
O endereço precisa terminar com `/`.

### Instalar em outros celulares
1. Envie o `app-debug.apk` por WhatsApp, e-mail ou Google Drive.
2. No celular, abra o arquivo. O Android vai pedir para permitir **"instalar apps de fontes desconhecidas"** para o app que você usou (WhatsApp, Chrome, Drive...). Autorize.
3. Instale e abra o app.
4. Se você usou a Opção 1 (ou o endereço mudou), toque em **⚙ Servidor** na tela de login, informe o endereço público e toque em **Salvar e testar conexão**.
5. Entre com um usuário de teste e use normalmente.

> O APK "debug" serve para testes e apresentações. Para publicar na **Google Play**, é preciso gerar um APK/AAB **assinado** (Build → Generate Signed Bundle / APK) e ter uma conta de desenvolvedor Google.

---

## Problemas comuns

| Sintoma | O que fazer |
|---|---|
| "Não foi possível conectar" ao testar o servidor | Confira o endereço (com `https://`), se o servidor está ligado e se o túnel ainda está aberto |
| O app funcionava e parou (Caminho A) | O endereço do túnel mudou. Gere um novo e atualize em ⚙ Servidor |
| Servidor na nuvem não sobe | Veja os **logs** do serviço. Os erros mais comuns são `DB_URL` errado ou `JWT_SECRET` com menos de 32 caracteres |
| Primeira resposta lenta | Alguns planos gratuitos "dormem" o servidor quando ficam parados; a primeira chamada demora para acordar |
| Celular não instala o APK | Autorize a instalação de fontes desconhecidas nas configurações do celular |
