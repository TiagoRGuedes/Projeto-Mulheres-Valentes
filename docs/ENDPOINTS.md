# Endpoints preliminares

## Status do contrato

Esta documentação descreve o contrato planejado para o protótipo. Rotas ligadas a fluxos da ONG continuam sujeitas à entrevista. Antes de integrar outro sistema, conferir a implementação e criar versionamento da API.

Base local:

~~~text
http://localhost:8080
~~~

## Convenções

- páginas web retornam HTML Thymeleaf;
- formulários alteram dados por POST e depois redirecionam;
- API recebe e devolve JSON;
- datas usam ISO 8601: AAAA-MM-DD;
- data e hora técnica usam ISO 8601;
- valores monetários no JSON são números decimais positivos, por exemplo 1250.50;
- moeda é formatada como R$ 1.250,50 somente na apresentação;
- filtros de data usam limites inclusivos;
- texto enviado é aparado no servidor;
- validações nunca dependem somente do navegador;
- nenhuma exclusão é realizada por GET.

## Rotas web

### Geral

| Método | Rota | Finalidade |
|---|---|---|
| GET | / | Dashboard com ações, visão geral, presença, caixa e atividade recente |
| GET | /historico | Histórico administrativo paginado, com filtros |
| GET | /ajuda | Central de Ajuda e primeiros passos |
| GET | /acessibilidade | Preferências de leitura |
| POST | /acessibilidade | Salvar preferências na sessão |
| POST | /acessibilidade/restaurar | Restaurar preferências padrão |
| GET | /relatorios | Entrada dos relatórios |

### Participantes

| Método | Rota | Finalidade |
|---|---|---|
| GET | /participantes | Listar e pesquisar |
| GET | /participantes/novo | Exibir formulário de cadastro |
| POST | /participantes | Cadastrar |
| GET | /participantes/{id} | Visualizar |
| GET | /participantes/{id}/editar | Exibir formulário de edição |
| POST | /participantes/{id} | Atualizar |
| POST | /participantes/{id}/ativar | Ativar |
| POST | /participantes/{id}/desativar | Desativar |
| POST | /participantes/{id}/excluir | Excluir somente quando não há presença relacionada |

### Atividades e encontros

| Método | Rota | Finalidade |
|---|---|---|
| GET | /atividades | Listar atividades |
| GET | /atividades/nova | Exibir formulário |
| POST | /atividades | Cadastrar |
| GET | /atividades/{id} | Visualizar |
| GET | /atividades/{id}/editar | Exibir edição |
| POST | /atividades/{id} | Atualizar |
| POST | /atividades/{id}/ativar | Ativar |
| POST | /atividades/{id}/desativar | Desativar |
| POST | /atividades/{id}/excluir | Excluir somente quando não há encontro relacionado |
| GET | /encontros | Listar encontros |
| GET | /encontros/novo | Exibir formulário |
| POST | /encontros | Cadastrar |
| GET | /encontros/{id} | Visualizar |
| GET | /encontros/{id}/editar | Exibir edição |
| POST | /encontros/{id} | Atualizar |
| POST | /encontros/{id}/excluir | Excluir somente quando não há presença relacionada |

### Presença

| Método | Rota | Finalidade |
|---|---|---|
| GET | /presencas | Histórico de registros |
| GET | /presencas/registrar | Escolher atividade e encontro ou mostrar lista |
| POST | /presencas/registrar | Salvar uma lista de presença |
| GET | /presencas/{id}/corrigir | Exibir a presença e o formulário de correção |
| POST | /presencas/{id}/corrigir | Corrigir situação/observação e registrar histórico |
| GET | /frequencia | Consultar totais e percentual |
| GET | /relatorios/presenca | Relatório de presença para impressão |
| GET | /relatorios/frequencia | Relatório de frequência para impressão |

### Livro Caixa

| Método | Rota | Finalidade |
|---|---|---|
| GET | /caixa | Dashboard financeiro |
| GET | /caixa/entrada | Formulário de entrada |
| POST | /caixa/entrada | Salvar entrada |
| GET | /caixa/saida | Formulário de saída |
| POST | /caixa/saida | Salvar saída |
| GET | /caixa/movimentacoes | Extrato com filtros |
| GET | /caixa/movimentacoes/{id} | Detalhes e histórico do lançamento |
| GET | /caixa/movimentacoes/{id}/editar | Exibir edição |
| POST | /caixa/movimentacoes/{id}/editar | Atualizar lançamento |
| GET | /caixa/movimentacoes/{id}/excluir | Exibir confirmação completa de cancelamento |
| POST | /caixa/movimentacoes/{id}/excluir | Cancelar logicamente e registrar histórico |
| GET | /caixa/categorias | Listar categorias e exibir formulário |
| POST | /caixa/categorias | Cadastrar categoria |
| GET | /caixa/categorias/{id}/editar | Exibir categoria no formulário de edição |
| POST | /caixa/categorias/{id}/editar | Atualizar categoria |
| POST | /caixa/categorias/{id}/alternar-status | Ativar ou desativar |
| GET | /caixa/relatorio | Relatório de livro caixa para impressão |
| GET | /caixa/relatorio.csv | Exportação CSV |

Os nomes de rotas auxiliares podem ser refinados. As rotas mínimas especificadas pelo projeto são preservadas.

## API REST — Participantes

### GET /api/participantes

Lista participantes. Filtros preliminares:

| Parâmetro | Tipo | Exemplo | Uso |
|---|---|---|---|
| busca | texto | ana | trecho do nome |

Resposta esperada: 200 OK.

### POST /api/participantes

Cria participante.

~~~json
{
  "nome": "Pessoa Fictícia 04",
  "observacoes": "Registro somente para demonstração"
}
~~~

Respostas:

- 201 Created, com Location e representação criada;
- 400 Bad Request para campos inválidos.

### GET /api/participantes/{id}

- 200 OK quando encontrado;
- 404 Not Found com mensagem humana quando não existe.

### PUT /api/participantes/{id}

Atualiza os campos permitidos.

~~~json
{
  "nome": "Pessoa Fictícia 04",
  "observacoes": "Cadastro demonstrativo desativado"
}
~~~

- 200 OK quando atualizado;
- 400 Bad Request para dados inválidos;
- 404 Not Found quando não existe.

### DELETE /api/participantes/{id}

Semântica segura preliminar: desativar o cadastro, preservando histórico.

- 204 No Content quando desativado;
- 404 Not Found quando não existe.

Essa semântica deve ser confirmada antes de uma integração externa.

## API REST — Presenças

### GET /api/presencas

Lista registros. Filtros que podem ser oferecidos:

- participanteId;
- encontroId;
- atividadeId;
- dataInicial;
- dataFinal;
- status.

Resposta esperada: 200 OK.

### POST /api/presencas

Cria um registro individual:

~~~json
{
  "participanteId": 1,
  "encontroId": 2,
  "status": "PRESENTE",
  "observacoes": ""
}
~~~

Respostas:

- 201 Created quando criado;
- 400 Bad Request para formato inválido;
- 422 Unprocessable Entity para referência ou regra de negócio inválida;
- 404 Not Found se participante ou encontro não existir;
- 409 Conflict quando já existe presença para participante + encontro.

O formulário web pode enviar vários itens juntos a um DTO próprio, preservando validação transacional. Uma rota REST de lote só deverá ser publicada se for necessária e documentada.

## API REST — Lançamentos

### GET /api/lancamentos

Lista lançamentos, com filtros preliminares:

| Parâmetro | Tipo | Exemplo |
|---|---|---|
| dataInicial | data | 2026-08-01 |
| dataFinal | data | 2026-08-31 |
| tipo | enum | ENTRADA |
| categoriaId | número | 1 |
| descricao | texto | evento |

Um intervalo com data inicial posterior à final retorna 400 Bad Request.

### POST /api/lancamentos

Cria entrada ou saída:

~~~json
{
  "data": "2026-08-27",
  "tipo": "ENTRADA",
  "categoriaId": 1,
  "descricao": "Entrada fictícia para demonstração",
  "valor": 125.50,
  "observacoes": "Sem relação com movimentação real"
}
~~~

Regras:

- valor maior que zero;
- categoria existente, ativa e do mesmo tipo;
- descrição obrigatória;
- valor armazenado positivo.

Respostas:

- 201 Created;
- 400 Bad Request para formato inválido;
- 422 Unprocessable Entity para regra de negócio inválida;
- 404 Not Found para categoria inexistente.

### GET /api/lancamentos/{id}

- 200 OK;
- 404 Not Found.

### PUT /api/lancamentos/{id}

Atualiza lançamento com o mesmo formato do POST.

- 200 OK;
- 400 Bad Request;
- 404 Not Found.

### DELETE /api/lancamentos/{id}

Cancela logicamente o lançamento. O registro deixa de compor saldo, extrato, relatórios, CSV, API e dashboards, mas permanece no banco e sua exclusão fica descrita no Histórico do sistema.

- 204 No Content;
- 404 Not Found.

**Atenção:** cancelar ou estornar é uma regra organizacional ainda não confirmada. O comportamento atual prioriza rastreabilidade, mas não deve ser usado com informação real até a ONG validar a política.

## API REST — Resumo financeiro

### GET /api/financeiro/resumo

Parâmetros opcionais:

- dataInicial;
- dataFinal.

Exemplo de resposta:

~~~json
{
  "dataInicial": "2026-08-01",
  "dataFinal": "2026-08-31",
  "totalEntradas": 1500.00,
  "totalSaidas": 420.75,
  "saldoPeriodo": 1079.25,
  "saldoAtual": 1079.25,
  "quantidadeMovimentacoes": 8
}
~~~

Sem datas, o resumo considera todos os lançamentos disponíveis. A interface deve deixar isso claro.

## Erros de validação

Formato conceitual sugerido:

~~~json
{
  "status": 400,
  "mensagem": "Não foi possível salvar. Confira os campos destacados.",
  "campos": {
    "valor": "Informe um valor maior que zero."
  }
}
~~~

Regras:

- não retornar SQL, stack trace, caminho interno ou nome de classe;
- associar erros de campo ao campo correspondente;
- usar 409 para duplicidade de presença;
- usar 404 para recurso ausente;
- usar 400 para conteúdo ou filtro inválido;
- usar 500 somente para erro inesperado, com mensagem genérica e registro técnico interno.

## Exemplos no PowerShell

Listar participantes:

~~~powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/participantes"
~~~

Criar participante fictício:

~~~powershell
$corpoParticipante = @{ nome = "Pessoa Fictícia da API"; observacoes = "Somente demonstração" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/participantes" -ContentType "application/json" -Body $corpoParticipante
~~~

Consultar resumo:

~~~powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/financeiro/resumo?dataInicial=2026-08-01&dataFinal=2026-08-31"
~~~

## Segurança

O protótipo não define autenticação final. Consequentemente:

- a API não deve ser exposta à internet;
- não devem ser usados dados pessoais ou financeiros reais;
- CORS não deve ser aberto sem necessidade;
- perfis ADMIN e OPERADOR são sugestões futuras;
- a trilha administrativa existe, mas identificação de usuário, retenção, proteção e política formal de auditoria ainda precisam ser definidas;
- rate limiting e política de tokens ainda precisam ser definidos.

## Console H2

GET /h2-console é uma ferramenta de desenvolvimento, não um endpoint de negócio. Deve permanecer desabilitado em produção.
