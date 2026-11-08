# Agenda Fácil

Aplicativo Android (Kotlin + Jetpack Compose) + API REST (Spring Boot + MySQL), baseado na documentação
"Agenda Fácil" e no protótipo do Figma (8 telas, perfis Cliente / Profissional / Administrador).

```
agenda-facil/
├── backend/   Spring Boot 3.3 · Java 17 · JPA · MySQL · JWT   (+ database/01_schema.sql)
├── android/   Kotlin · Compose Material 3 · Retrofit
└── docs/img/  telas do protótipo do Figma
```

<p align="center">
  <img src="docs/img/figma-01-login.png" width="130"> <img src="docs/img/figma-02-inicio.png" width="130">
  <img src="docs/img/figma-03-data-hora.png" width="130"> <img src="docs/img/figma-04-confirmacao.png" width="130">
  <img src="docs/img/figma-05-meus-agendamentos.png" width="130"> <img src="docs/img/figma-06-agenda-profissional.png" width="130">
  <img src="docs/img/figma-07-painel-admin.png" width="130"> <img src="docs/img/figma-08-notificacoes.png" width="130">
</p>

> Visual do app: fontes **Outfit** (títulos) e **DM Sans** (textos) e as fotos de perfil retiradas do arquivo Figma.

## 1. Rodar o backend

Requisitos: JDK 17+ e Maven. Para MySQL: Docker **ou** MySQL 8 instalado.

```bash
cd backend
docker compose up -d                 # MySQL (root/root, banco agenda_facil)
./mvnw spring-boot:run               # Windows: mvnw.cmd spring-boot:run  (não precisa ter Maven instalado)
```
Sem MySQL (teste rápido, banco em memória): `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`

O modelo físico está em `backend/database/01_schema.sql` (opcional: a API cria as tabelas sozinha).

Variáveis opcionais: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (mín. 32 caracteres).
Na primeira execução as tabelas são criadas (Hibernate `ddl-auto: update`) e populadas com os dados do protótipo.

### Usuários de teste (senha de todos: `12345678`)
| Perfil | E-mail |
|---|---|
| Cliente | beatriz@email.com (também caue@, stefany@, gabriel@email.com) |
| Profissional | ana@agendafacil.com (mariana@, carlos@, juliana@agendafacil.com) |
| Administrador | admin@agendafacil.com |

## 2. Rodar o app Android

1. Android Studio (Koala ou mais novo, JDK 17) → **Open** → pasta `android/`. Aguarde o Gradle sync (o `gradlew` já está incluído).
2. Emulador: o app já aponta para `http://10.0.2.2:8080/` (localhost do seu PC).
   Celular físico: em `android/app/build.gradle.kts` troque `API_BASE_URL` pelo IP do PC na rede (ex.: `http://192.168.0.10:8080/`).
3. Run ▶.

## 3. Requisitos da documentação → onde estão

| Item | Implementação |
|---|---|
| RF001 cadastro do cliente | `POST /api/auth/register`, `PUT /api/me` · telas Criar conta / Perfil |
| RF002 relatórios (admin) | `GET /api/admin/relatorios/agendamentos` · Painel → Relatórios |
| RF003 consultar serviços | `GET /api/servicos` · Início e etapa 1 do agendamento |
| RF004 agendar | `POST /api/cliente/agendamentos` · fluxo 4 etapas + tela de confirmação |
| RF005 remarcar/cancelar | `PUT .../remarcar`, `PATCH .../cancelar` · tela Meus |
| RF006 agenda do profissional | `GET /api/profissional/agenda` (dia ou semana) · tela Agenda, Bloquear Horário |
| RF007 confirmar/registrar atendimento | `PATCH /api/profissional/agendamentos/{id}/confirmar\|realizar` |
| RN001 | só cliente cadastrado agenda (`/api/cliente/**`) |
| RN002 sem conflito | validação + trava pessimista no profissional (`findByIdForUpdate`) |
| RN003 | cliente só altera os próprios agendamentos |
| RN004 | profissional só vê/altera a própria agenda |
| RN005 | `/api/admin/**` só para ADMINISTRADOR |
| RN006 | "realizado" só se existir agendamento CONFIRMADO e a data já chegou |
| RNF002/005 | JWT, senhas com BCrypt, rotas por perfil |
| Notificações | criadas em novo agendamento/cancelamento/remarcação/confirmação + lembrete 30 min antes (scheduler) |

`agenda.auto-confirmar` (application.yml): `true` = agendamento já nasce Confirmado (como no Figma);
`false` = nasce Pendente e o profissional confirma.

## 4. Limitações conhecidas (fora do que a documentação especifica ou não validado)
- Integrações externas do documento (WhatsApp, pagamento, IA) **não** foram implementadas; notificações são internas ao app (sem push).
- "Esqueci minha senha" apenas informa para procurar o administrador (não há envio de e-mail).
- Avaliações (aparecem como exemplo na central de notificações do Figma) não existem no modelo.
- As fotos do Figma são só para os usuários de demonstração (associadas pelo nome em `Components.kt`); os demais usuários mostram iniciais. Para fotos reais seria preciso upload no backend.
- HTTP em texto puro habilitado só para desenvolvimento; em produção use HTTPS e remova `usesCleartextTraffic`.
