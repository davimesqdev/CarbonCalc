# CarbonCalc

Sistema de pegada de carbono empresarial: calcula quanto a empresa emite (Escopos 1, 2 e 3, em
toneladas de CO₂e), quanto precisa compensar e sugere como fazer isso (certificados de energia
renovável, créditos de carbono e reflorestamento).

Os diagramas UML que guiaram o projeto estão em [docs/diagramas](docs/diagramas).

## Estrutura

| Pasta | Conteúdo |
|---|---|
| `backend/` | API REST em Java 21 + Spring Boot 4 (auth, cadastro, cálculo, compensação, relatório, consulta) |
| `frontend/` | Interface web em React + TypeScript (Vite) |
| `docker-compose.yml` | PostgreSQL, Redis e MinIO (S3 local) |
| `docs/` | Diagramas e logo |

## Como rodar

### Opção A — sem Docker (perfil `local`)

Usa banco H2 em arquivo, cache em memória e grava os relatórios em `backend/data/`.

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

```bash
cd frontend
npm install
npm run dev
```

Abra http://localhost:5173.

### Opção B — infraestrutura completa (Docker)

Requer o Docker Desktop funcionando (no Windows, ele depende do WSL: `wsl --install` como
administrador e reiniciar).

```bash
docker compose up -d
cd backend
./mvnw spring-boot:run
```

O frontend sobe da mesma forma que na opção A.

### Portas

| Serviço | Porta |
|---|---|
| Frontend (Vite) | 5173 |
| Backend | 8085 (mude com a variável `SERVER_PORT`; se mudar, ajuste o proxy em `frontend/vite.config.ts`) |
| PostgreSQL / Redis / MinIO | 5432 / 6379 / 9000 (console do MinIO em 9001) |

## Usuários de demonstração

Na primeira execução são criados três usuários, um por perfil. A senha é a mesma para os três e
está em `carboncalc.seed.senha`, no arquivo `backend/src/main/resources/application.yml`.

| E-mail | Perfil | Pode |
|---|---|---|
| `admin@carboncalc.local` | Administrador | Tudo, incluindo usuários, empresas, plantas e fontes |
| `gestor@carboncalc.local` | Gestor ambiental | Registrar consumo, calcular, compensar, gerar e enviar relatórios |
| `auditor@carboncalc.local` | Auditor | Consultar o histórico e gerar relatórios |

## Fluxo de uso

1. **Administrador**: cadastra a empresa, as plantas e as fontes de emissão, e ativa a planta
   (com a documentação conferida).
2. **Gestor**: registra o consumo mensal de cada fonte e calcula a pegada do período.
3. **Gestor**: compara cenários, gera o plano de compensação e emite o relatório (PDF ou Excel).
4. **Gestor**: envia o relatório ao órgão regulador e recebe o protocolo.
5. **Auditor**: consulta o histórico de cálculos e relatórios.

## Decisões e simplificações

- **Fatores de emissão**: o GHG Protocol não tem API pública. A integração externa é simulada por
  `GhgProtocolSimulado`; se ela falhar ou passar de 5 s, o cálculo usa o cache (Redis) e, na falta
  dele, a tabela `fator_emissao`. Para testar a queda, defina `carboncalc.ghg.disponivel: false`.
  Os fatores são valores de referência ilustrativos.
- **Compensação**: 1 crédito = 1 t CO₂e. O Escopo 2 é coberto por I-RECs; o restante é dividido em
  60% créditos de carbono e 40% reflorestamento. Preços e rendimentos estão em
  `CompensacaoService` e são premissas ilustrativas, não cotações de mercado.
- **Envio ao órgão regulador**: simulado. Apenas gera e registra o protocolo.
- **Planta**: segue o diagrama de estados. Só plantas ativas recebem consumo; plantas existentes
  não aceitam consumo de meses futuros (projeções são para plantas novas); uma planta ativa sem
  consumo nos últimos 3 meses aparece como ociosa; plantas inativas há 5 anos são arquivadas por
  uma rotina diária.
- **Acréscimos em relação ao diagrama de classes**: `Calculo` ganhou período e cenário, o escopo
  vem do tipo da fonte (`TipoFonte`) e o fator de emissão é ligado ao tipo de fonte, com vigência.

## Testes

```bash
cd backend
./mvnw test
```
