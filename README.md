# mineiradorLead-backend

API Spring Boot do projeto (extração de leads, histórico de buscas, Stripe, autenticação JWT).

## Requisitos

- Java 17+
- Maven
- PostgreSQL

## Executar (local)

```bash
mvn spring-boot:run
```

Configure o banco em `application.yml` ou variáveis de ambiente (`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`).

## Deploy no Render (Docker)

1. **PostgreSQL** no Render (Free) → crie o banco.
2. **Web Service** (Docker) apontando para este repositório e `Dockerfile` na raiz.
3. No menu do Postgres, **Connect** a esse Web Service (injeta `DATABASE_URL` no formato `postgresql://...` — o app converte para JDBC automaticamente).
4. **Environment** no Web Service (obrigatório em produção):
   - `JWT_SECRET` — string longa e aleatória (mínimo 32 caracteres).
   - `CORS_ORIGINS` — URL do front (ex.: `https://seu-front.onrender.com`). Pode listar várias separadas por vírgula.
5. Opcional: Stripe (`application.yml`).
6. **Google Maps no Render:** o IP do datacenter costuma ser pior que o da sua casa — timeout ou bloqueio. Use **proxy residencial** (Bright Data, Oxylabs, etc.) nas variáveis do Web Service:
   - `PROXY_SERVER` — ex.: `http://host:porta`
   - `PROXY_USERNAME` / `PROXY_PASSWORD` — se o provedor exigir.

**Porta:** o Render define `PORT`; o app usa `server.port=${PORT}`.

**Extração Playwright:** use a imagem oficial no `Dockerfile` (Chromium incluso). Se ainda falhar só em produção, é quase sempre **IP** → proxy acima ou **Places API** (oficial, paga).

## Build local do JAR

```bash
mvn clean package -DskipTests
java -jar target/lead-backend-0.1.0-SNAPSHOT.jar
```
