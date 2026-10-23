# Alinhamento do projeto com as aulas da disciplina

**Data:** 2026-10-23
**Disciplina:** IBM4024 — Projeto em Ciência de Dados II
**Projeto:** Sistema de Presença e Livro Caixa Digital — ONG Mulheres Valentes
**Natureza:** reestruturação interna, sem mudança de escopo funcional

---

## 1. Objetivo

Reescrever o código do projeto para que use apenas os conceitos e a sintaxe
ensinados nas aulas da disciplina, preservando integralmente a aparência, o
comportamento e a segurança do sistema atual.

A motivação é a prova de projeto: o aluno assumiu o repositório já em
andamento e precisará explicar qualquer arquivo diante do professor, além de
fazer edições ao vivo. Hoje isso é inviável em várias partes do código, que
usam mecanismos nunca apresentados em aula.

### Critérios de sucesso

1. Nenhum arquivo em `src/main/java` usa sintaxe ou padrão ausente das aulas,
   com exceção da camada de segurança, que é documentada explicitamente.
2. Nenhum arquivo passa de 300 linhas; a média fica em torno de 67.
3. Toda rota existente hoje continua existindo, com o mesmo verbo HTTP e o
   mesmo resultado visível.
4. Nenhuma mudança visual. O `app.css` e as imagens não são tocados. Nos
   templates, a única alteração permitida é o **nome** de um atributo em
   `th:object`/`th:field`/`th:text` quando a classe que o alimenta muda de
   forma. Nenhuma tag, classe CSS, estrutura ou texto literal pode mudar — o
   que se verifica lendo o `git diff` dos templates, que não deve conter
   alteração fora de expressões `th:`.
5. A suíte de 17 testes continua passando, sem testes removidos.
6. Para cada peça do código existe uma aula que a justifica, registrada em
   comentário no próprio arquivo.

### Fora do escopo

- Remover ou adicionar funcionalidade.
- Alterar templates, CSS, fontes ou imagens.
- Adotar Spring Security.
- Mudar o banco (continua H2 em arquivo) ou o modelo de dados.

---

## 2. O teto de sintaxe

A referência é o "Mapa da aplicação" do `roteiro_aula11.md`, que lista aula por
aula o que foi ensinado e onde cada conceito vive no projeto `todolist` do
professor. O `todolist-aula10-final` é a implementação canônica a espelhar.

| Aula | Conceitos liberados |
|---|---|
| 3 | `@RestController`, `@GetMapping`, `@PathVariable`, `pom.xml`, starter `webmvc` |
| 4 | `@Entity`, `@Id`, `@GeneratedValue(IDENTITY)`, `@Column`, `JpaRepository`, consultas derivadas, `@Service` com injeção por construtor, CRUD REST com `ResponseEntity` e status 201/200/404/204, `application.properties`, `DataLoader` com `CommandLineRunner`, `@RestControllerAdvice`, JDBC/`PreparedStatement`, injeção de SQL, padrão DAO |
| 6 | `@Controller` + Thymeleaf, `th:text`/`th:each`/`th:if`/`th:unless`/`th:href`/`th:action`/`th:object`/`th:field`, `@RequestParam`, `@ModelAttribute`, POST-Redirect-Get, `RedirectAttributes.addFlashAttribute`, `HttpSession`, escopos request/session/application |
| 9 | `@ManyToOne`, `@JoinColumn`, `optional`/`nullable`, consultas derivadas por relacionamento, verificação de existência no Service, `select` com `*{entidade.id}` |
| 10 | `@NotBlank`, `@Size`, `@Email`, `@NotNull`, `@Valid`, `BindingResult`, `th:errors`, `th:errorclass`, `#fields.hasErrors`, `ManipuladorDeErros` |
| 11 | Git, `.gitignore`, `modelagem.sql`, estrutura de pastas |

### O que o projeto usa hoje e não está no teto

| Padrão | Arquivos | Decisão |
|---|---|---|
| 49 classes em `dto/` (`...Form`, `Resumo...DTO`, `Dashboard...DTO`) | `dto/`, `dto/api/` | **Remover as de entrada** (ver 2.1). As que alimentam template sobrevivem, porque o critério 4 proíbe mexer na estrutura dos templates |
| Motor genérico de revisão com token em `ConcurrentMap` | `RevisaoOperacaoService` (645), `ConfirmacaoOperacaoService` (261), `ConfirmacaoOperacaoController` (215) | **Substituir** por revisão explícita por fluxo, guardando em `HttpSession` (aula 6, §8) |
| Exceções próprias e dois tratadores | `exception/` (5 arquivos, 142 linhas) | **Unificar** em `IllegalArgumentException` + um `ManipuladorDeErros` (aula 10) |
| `@ControllerAdvice` para popular o Model | `ModeloGlobalAdvice` | **Manter.** Alternativa seria repetir dois `addAttribute` em 16 controllers; documentar como extensão |
| `implements ErrorController` | `ErroController` | **Manter.** É o que entrega a página de erro amigável exigida pelo escopo; documentar |
| `HandlerInterceptor`, `Filter`, `WebMvcConfigurer` | `security/`, `WebConfig` | **Manter.** Ver seção 5 |
| `@Enumerated`, enums de domínio | `model/` (8 enums) | **Manter.** RF05 exige o enum `NaturezaLancamento` pelo nome; alternativa seria texto solto |
| `@ElementCollection` | `Inscricao` (áreas de apoio) | **Manter.** Documentar; é um conjunto de valores, não entidade |
| `@Table(indexes=...)`, chave única composta | `Presenca`, `LancamentoCaixa`, `HistoricoOperacao`, `Inscricao` | **Manter.** A unicidade participante+encontro é regra de domínio do escopo |
| `FetchType` explícito | entidades com `@ManyToOne` | **Remover.** Aula 9 usa `@ManyToOne` puro |
| Fragmentos Thymeleaf (`th:replace`) | 9 arquivos em `templates/fragments/` | **Manter.** Remover duplicaria cabeçalho e menu em 40 templates e quebraria o critério 4 |

### 2.1 Quais DTOs saem e quais ficam

A regra é a direção do dado. **DTO de entrada sai**: o que existe só para
receber um formulário é substituído pela própria entidade em
`@ModelAttribute`/`@RequestBody`, como nas aulas 6, 10 e 4. **Classe que
alimenta template fica**: o critério 4 proíbe reestruturar template, e o
template faz bind na forma da classe.

Saem (~640 linhas, 14 arquivos): `ParticipanteForm`, `AtividadeForm`,
`EncontroForm`, `InscricaoForm`, `LancamentoCaixaForm`,
`LancamentoCaixaRequest`, `CategoriaFinanceiraForm`, `RegistroPresencaForm`,
`ItemRegistroPresencaForm`, `CorrecaoPresencaForm` e os 4 de `dto/api/`.

Ficam (~450 linhas, ~18 arquivos), porque há template lendo cada uma:
`PreferenciasAcessibilidade` (classe do `<body>` em todos os templates),
`ResultadoBuscaGlobalDTO` + `GrupoResultadoBuscaDTO` + `ItemResultadoBuscaDTO`
(`busca/resultados.html`), `GrupoHistoricoDTO` (`fragments/timeline-historico.html`),
`FotoGaleriaDTO` (`publico/inicio.html`), `ExtratoCaixaDTO` e
`LancamentoCaixaFiltro` (`caixa/movimentacoes.html`), os `Dashboard...DTO` e
`Resumo...DTO` do painel (`index.html`), `ResumoFrequenciaDTO` e
`FrequenciaParticipanteDto` (`presencas/frequencia.html`),
`ParticipanteConectadoDTO` e irmãos (telas de detalhes).

Os sobreviventes são todos pequenos — 15 a 26 linhas, apenas getters — e
explicáveis em uma frase: "é o objeto que a tela lê". Os `...Response` da API
saem porque lá a aula 4 devolve a entidade direto.

**Consequência assumida:** trocar um `...Form` pela entidade muda o nome do
campo em alguns `th:field` — por exemplo `*{categoriaId}` passa a
`*{categoria.id}`, que é exatamente a forma que a aula 9 ensina para
relacionamento em formulário. São edições de uma expressão por linha, sem
efeito visual.

### O que as aulas ensinam e falta no projeto

| Item | Onde deveria estar | Situação |
|---|---|---|
| `modelagem.sql` | raiz, ao lado do `pom.xml` | **ausente** — todo projeto das aulas tem, e a Aula 11 o lista no mapa de estrutura |
| `ManipuladorDeErros` com `@RestControllerAdvice` | `src/main/java/.../ManipuladorDeErros.java` | existe como `TratadorErrosApi` + `TratadorErrosWeb`, com nome e formato diferentes |
| `spring.jpa.show-sql=true` | `application.properties` | **ausente** — a Aula 9 usa o console para conferir o `alter table ... foreign key` |

---

## 3. Arquitetura alvo

Uma entidade, um repositório, um service, um controller — a estrutura do
`todolist`. Nenhum service de infraestrutura atravessando os demais.

```
src/main/java/com/ibmec/mulheresvalentes/
├── MulheresValentesApplication.java
├── DataLoader.java                 dados de demonstração (aula 4)
├── ManipuladorDeErros.java         exceção → status HTTP (aulas 4 e 10)
├── model/                          entidades e enums
├── repository/                     interfaces JpaRepository
├── service/                        regras de negócio
├── controller/                     páginas Thymeleaf
├── controller/api/                 API REST
├── security/                       login do painel (além das aulas)
└── config/                         WebConfig, ModeloGlobalAdvice
```

### Services que permanecem

Um por entidade, mais dois que alimentam telas existentes:

`ParticipanteService`, `AtividadeService`, `EncontroService`, `PresencaService`,
`LancamentoCaixaService`, `CategoriaFinanceiraService`, `InscricaoService`,
`HistoricoService`, `DashboardService`, `BuscaGlobalService`,
`FormatadorService`, `GaleriaService`.

### Services que desaparecem

| Service | Linhas | Para onde vai |
|---|---|---|
| `RevisaoOperacaoService` | 645 | revisão explícita em cada controller (seção 4) |
| `ConfirmacaoOperacaoService` | 261 | `HttpSession` (seção 4) |
| `ConexoesAdministrativasService` | 89 | consultas derivadas chamadas direto nos services de domínio |
| `HistoricoLinkService` | 68 | o histórico passa a gravar o rótulo pronto |
| `ConferenciaInscricaoService` | 65 | token de conferência da inscrição pública vai para `HttpSession` |

`HistoricoOperacaoService` deixa de comparar estado antes/depois e passa a
expor um único método `registrar(modulo, acao, descricao)`. É essa mudança que
permite enxugar os services de domínio, hoje inflados pela montagem do diff.

---

## 4. A substituição do motor de revisão

### Como funciona hoje

```
POST /caixa/entrada
  → CaixaController monta um OperacaoRevisaoDTO
  → RevisaoOperacaoService descobre genericamente os campos da entidade
  → ConfirmacaoOperacaoService guarda a operação pendente num
    ConcurrentMap com token e validade de 10 minutos
  → renderiza confirmacoes/revisao.html
POST /confirmacoes/confirmar
  → ConfirmacaoOperacaoController valida o token
  → RevisaoOperacaoService executa a operação, qualquer que seja
```

Três arquivos, 1.121 linhas, uma camada de abstração a entender antes de
entender qualquer fluxo concreto.

### Como passa a funcionar

O template `confirmacoes/revisao.html` **não é alterado**. Ele já é dirigido
por um objeto de view com `titulo`, `descricao`, `campos`, `comparacoes`,
`impactos` e `textoConfirmar`; o que muda é apenas quem o preenche.

```
POST /caixa/entrada
  → CaixaController valida com @Valid + BindingResult (aula 10)
  → guarda o lançamento preenchido na HttpSession (aula 6, §8)
  → monta os rótulos da revisão ali mesmo, explicitamente
  → renderiza confirmacoes/revisao.html
POST /caixa/entrada/confirmar
  → lê da sessão, remove o atributo, chama o service, redirect + flash
```

Cada fluxo confirma no seu próprio endpoint, então não há operação genérica e
não há token: a sessão já é por usuário e o atributo é removido ao ser usado,
o que preserva a semântica de "vale uma única vez".

As classes que o `revisao.html` lê hoje — `RevisaoOperacaoViewDTO`,
`LinhaRevisaoDTO` e `ComparacaoRevisaoDTO` — permanecem, pela regra da
seção 2.1: o template faz bind nelas. Saem apenas `OperacaoRevisaoDTO` e
`RevisaoPreparadaDTO`, que existiam para descrever genericamente "uma operação
qualquer" ao motor, conceito que deixa de existir.

### Quais fluxos mantêm revisão

Dez, e o critério não é arbitrário: são exatamente os fluxos em que o
`ESCOPO_PRELIMINAR.md` descreve um passo de revisão. O fluxo de presença tem
"5. revisar totais"; entrada e saída têm "4. revisar". Participante, atividade
e categoria não têm passo de revisão documentado em nenhum lugar do escopo.

| Fluxo | Por quê |
|---|---|
| Registrar entrada | escopo, passo 4; mexe em dinheiro |
| Registrar saída | escopo, passo 4; mexe em dinheiro |
| Editar lançamento | altera saldo já consultado |
| Cancelar lançamento | RF09, exclusão rastreável |
| Registrar presenças (lote) | escopo, passo 5: "revisar totais" antes de salvar o conjunto |
| Corrigir presença | altera dado histórico já relatado |
| Excluir participante | irreversível |
| Excluir atividade | irreversível |
| Excluir encontro | irreversível |
| Aceitar inscrição | cria participante; irreversível na prática |

### Quais fluxos passam a ser diretos

Quinze operações reversíveis e triviais passam a POST + `redirect:` +
`addFlashAttribute`, que é o padrão canônico da aula 6:

criar/editar/ativar/desativar participante, criar/editar/ativar/desativar
atividade, criar/editar encontro, criar/editar/alternar categoria, marcar
inscrição em contato, arquivar inscrição.

**Esta é a única mudança de comportamento visível de toda a reestruturação:**
quinze confirmações a menos. Aprovada explicitamente em 2026-10-23.

---

## 5. Segurança

### Decisão: a camada de login permanece como está

O `AutenticacaoAdminInterceptor` é hoje o código mais bem escrito do projeto.
Ele nega por padrão, usa comparação exata nas rotas livres (`equals("/inscricao")`
mais `startsWith("/inscricao/")`, de modo que `/inscricoes` do painel **não** é
liberada por engano), devolve 401 em `/api/`, redireciona o resto para `/admin`
e envia `Cache-Control: no-store`. O `SessaoAdministrador` chama
`changeSessionId()` após autenticar, contra fixação de sessão, e compara a
senha com `MessageDigest.isEqual`, contra ataque de tempo.

Trocar isso por verificações de `HttpSession` dentro de cada controller — o que
seria a forma "da aula 6" — espalharia a decisão de autorização por 14
controllers do painel e criaria 14 oportunidades de esquecer uma. Seria perder
segurança real para ganhar aderência didática. **Mantém-se, e o README passa a
documentar a camada como extensão consciente além das aulas**, com a tabela de
rotas da seção 6.

O `ConsoleH2Filter` também permanece: o console do H2 é um servlet próprio e
não passa por interceptor de Spring MVC, então o filtro é o único ponto onde
dá para protegê-lo.

### Credencial

O `application.properties` versionado trazia a senha do painel como valor
padrão da variável de ambiente, o que a deixava legível para qualquer pessoa
com acesso ao código. A Aula 11 alerta sobre credencial em commit de forma
explícita.

Já resolvido: `app.admin.usuario` e `app.admin.senha` não têm mais valor
padrão. Sem `MV_ADMIN_USUARIO` e `MV_ADMIN_SENHA` definidas a aplicação não
sobe, e o erro aponta qual faltou. Nenhuma credencial fica no repositório, e na
hora da prova basta definir as duas na configuração de Run do IntelliJ.

### Limitação registrada, não corrigida

Os POSTs do painel não têm proteção CSRF. Hoje a mitigação é o cookie
`SameSite=lax`, que já bloqueia o vetor clássico de POST entre sites. Corrigir
de verdade exigiria Spring Security ou tokens manuais em ~30 formulários —
muito além das aulas e contra o objetivo desta reestruturação. Fica registrado
no README como limitação conhecida de protótipo acadêmico, coerente com o
escopo, que lista "autenticação e autorização em produção" como fora do escopo.

---

## 6. Verificação

A reestruturação é feita módulo a módulo. Entre cada módulo:

1. `./mvnw test` — os 17 testes existentes continuam passando.
2. `git diff src/main/resources/` — o `app.css` e as imagens precisam vir
   intactos. Nos templates, toda linha alterada tem de ser uma expressão `th:`;
   se o diff mostrar mudança de tag, de classe CSS ou de texto visível, a
   estética foi afetada e o módulo volta atrás.
3. Conferência da tabela rota → protegida/pública, comparando a lista de rotas
   antes e depois com `grep` nos controllers.

Ao final:

4. Inventário rota por rota das 25 rotas do painel e das 5 públicas, com o
   resultado documentado no README.
5. `./mvnw spring-boot:run` e navegação manual dos 10 fluxos que mantêm
   revisão, mais os 15 que passaram a ser diretos.
6. Teste de clone limpo, como manda o passo 13 da Aula 11: clonar em outra
   pasta e confirmar que sobe.

### Ordem dos módulos

Do mais isolado ao mais acoplado, para que um erro apareça cedo e barato:

1. `exception/` → `ManipuladorDeErros` (nenhuma dependência de domínio)
2. `modelagem.sql`, `show-sql`, credencial, `DataLoader`
3. Participante (entidade mais simples, nenhum relacionamento de saída)
4. Atividade e Encontro (relacionamento `@ManyToOne` da aula 9)
5. Categoria e Caixa (os 4 fluxos de revisão de dinheiro)
6. Presença (o lote, o maior service)
7. Inscrição (`@ElementCollection`, token da inscrição pública)
8. Histórico, Dashboard, Busca (dependem de todos os anteriores)
9. API REST (entidades no lugar dos DTOs)
10. README: tabela de rotas, mapa aula → arquivo, limitações

---

## 7. Resultado esperado

| Camada | Hoje | Alvo |
|---|---|---|
| `dto/` | 1.193 linhas, 49 arquivos | ~450 linhas, ~18 arquivos |
| `service/` | 3.532 linhas, 17 arquivos | ~1.800 linhas, 12 arquivos |
| `controller/` | 2.079 linhas, 20 arquivos | ~1.850 linhas, 19 arquivos |
| `model/` | 1.355 linhas, 24 arquivos | ~1.000 linhas, 24 arquivos |
| `repository/` | 376 linhas, 8 arquivos | ~376 linhas, 8 arquivos |
| `config/` | 566 linhas, 3 arquivos | ~260 linhas, 2 arquivos |
| `exception/` | 142 linhas, 5 arquivos | 0 (vira `ManipuladorDeErros`, ~45 linhas) |
| `security/` | 155 linhas, 3 arquivos | 155 linhas, inalterado |
| **Total Java** | **9.410 linhas, 120 arquivos** | **~5.950 linhas, ~89 arquivos** |
| CSS e imagens | 7.840 linhas | inalterados |
| Templates | 1.873 linhas, 49 arquivos | 49 arquivos, só expressões `th:` ajustadas |

A redução de tamanho é consequência, não objetivo. O objetivo é que o maior
arquivo do projeto caia de 645 para cerca de 300 linhas, que a média fique
perto de 67, e que cada arquivo tenha no topo um comentário dizendo de qual
aula vem o conceito que ele usa — no mesmo estilo dos "NOVO NA AULA 10" que o
professor escreve no `todolist`.

---

## 8. Decisões tomadas em 2026-10-23

| Decisão | Escolha |
|---|---|
| Abordagem | espelhar o `todolist`, módulo a módulo |
| Comentários | mapear cada peça à aula de origem |
| Credencial de admin | exigir variável de ambiente, senha aleatória no console como alternativa |
| Telas de revisão | manter 10, tornar 15 diretas |
| API REST | manter e alinhar à aula 4, trocando DTOs por entidades |
| Camada de segurança | manter e documentar como extensão além das aulas |

### Premissa a confirmar

A aula de autenticação prevista para 22/10/2026 não aconteceu e não há material
dela em `PastaAulasProjeeto`. Toda esta spec assume que autenticação **não é
conteúdo cobrável**, e é por isso que a camada de segurança é tratada como
extensão documentada em vez de ser reescrita no estilo de uma aula. Se a aula
acontecer antes da prova, a seção 5 deve ser revisada.
