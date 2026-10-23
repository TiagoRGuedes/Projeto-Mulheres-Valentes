# Mulheres Valentes — Sistema de Presença e Livro Caixa

Projeto acadêmico da disciplina **IBM4024 — Projeto em Ciência de Dados II — IBMEC — 2026.2**.

**Integrantes**

- Bruno Cappellette
- Tiago Guedes

Este repositório contém uma aplicação web independente para apoiar o controle de presença e o livro caixa da ONG Mulheres Valentes. Ele não compartilha banco de dados, configuração, dependências, código ou documentação com o projeto da ONG Remo Meu Rumo.

> **Importante:** a entrevista com a ONG foi realizada e está registrada no documento de escopo (AC1). O protótipo foi ajustado ao que ela confirmou — ver **Requisitos do escopo** logo abaixo. O que a ONG não confirmou continua marcado como premissa em `docs/PREMISSAS_E_DUVIDAS.md`. A autenticação (RNF04) existe em versão simples: uma única conta de administrador, ainda sem perfis nem identificação de quem fez cada ação.

## Requisitos do escopo (AC1)

| Código | Requisito | Onde está no sistema |
|---|---|---|
| RF01 | Cadastrar uma participante com nome e contato | `/participantes/novo` · `POST /api/participantes` |
| RF02 | Registrar a presença de uma participante em uma data | `/presencas/registrar` · `POST /api/presencas` |
| RF03 | Consultar a listagem de presença por período | `/presencas` · `/frequencia` · `GET /api/participantes/{id}/presencas` |
| RF04 | Registrar um lançamento no caixa, com valor obrigatório apenas nos monetários | `/caixa/entrada` · `/caixa/saida` · `POST /api/caixa` |
| RF05 | Categorizar o lançamento como aluguel/patrocínio ou doação de material | campo **Natureza** no formulário; enum `NaturezaLancamento` |
| RF06 | Registrar o comprovante de uma doação de material | campo **Comprovante**, obrigatório quando é doação de material |
| RF07 | Consultar o saldo atual do caixa | `/caixa` · `GET /api/caixa/saldo` |
| RF08 | Gerar um extrato do caixa por período | `/caixa/movimentacoes` · `/caixa/relatorio.csv` · `GET /api/caixa/extrato` |
| RF09 | Editar ou excluir um lançamento feito por engano | `/caixa/movimentacoes/{id}/editar` e cancelamento rastreável |
| RNF01 | Valores em decimal, nunca em ponto flutuante | `BigDecimal` no domínio, `DECIMAL(10,2)` no banco |
| RNF02 | Interface simples para quem tem familiaridade digital média | textos de ajuda, fluxo Preencher → Revisar → Confirmar, Central de Ajuda |
| RNF03 | Funcionar em celular | CSS próprio responsivo, tabelas adaptáveis |
| RNF04 | Acesso com login | `/admin` · sessão única de administrador — ver "Acesso da equipe" |

### O que a entrevista mudou no protótipo

- **A presença é um controle único por data.** A ONG não separa a chamada por atividade. A tela de registro abre na data de hoje, sem exigir atividade nem encontro; o vínculo com uma atividade continua disponível, mas é opcional.
- **Nem todo lançamento do caixa é dinheiro.** O único valor monetário recorrente é o aluguel/patrocínio de R$ 1.200 por mês. Doações de material chegam fisicamente, não têm valor e são controladas por comprovante — então elas são registradas sem valor e não entram no saldo.
- **A participante tem contato.** O cadastro passou a guardar telefone, WhatsApp ou e-mail, opcionalmente e com consentimento; a busca também encontra pela pessoa pelo contato.
- **A prioridade é o caixa, não a presença.** Isso não muda o código, mas orienta o que demonstrar e o que aprimorar primeiro.

## O que o projeto oferece

- cadastro, pesquisa por nome ou contato, edição, ativação e desativação de participantes;
- exclusão segura de cadastros ainda sem histórico relacionado;
- registro de presença por data, no controle único descrito pela ONG;
- cadastro opcional de atividades e encontros, para quem quiser separar a chamada;
- registro conjunto de presença, ausência ou justificativa;
- histórico e cálculo de frequência em Java;
- registro de entradas e saídas com valores em BigDecimal;
- registro de doações de material, sem valor monetário e com comprovante obrigatório;
- consulta de movimentações, filtros por período e resumo de saldo, que soma apenas o que é dinheiro;
- Dashboard administrativo com indicadores de presença, caixa e atividade recente;
- Histórico do sistema, somente leitura, para criações, alterações, desativações e cancelamentos;
- cancelamento lógico de lançamentos financeiros, preservando a rastreabilidade sem afetar o saldo;
- relatórios de presença e livro caixa, com versão para impressão e exportação quando disponível;
- Central de Ajuda e explicações dentro das telas;
- opções de tamanho de fonte, alto contraste, modo escuro e redução de animações;
- páginas web Thymeleaf e API REST;
- dados exclusivamente fictícios para demonstração.

As categorias, pessoas, atividades e movimentações carregadas para demonstração não representam dados oficiais da ONG.

## Identidade institucional

A interface administrativa utiliza como referência principal o [site oficial da ONG Mulheres Valentes](https://mulheresvalentes.org.br/), sem copiar a estrutura do site público. O resultado adapta a mesma personalidade visual a tarefas internas de presença, caixa e relatórios.

- **logo oficial local:** `src/main/resources/static/img/brand/logo-mulheres-valentes.png`;
- **fonte local:** Manrope variável em `src/main/resources/static/fonts/manrope-variable.woff2`;
- **base institucional:** verde-petróleo `#144047` e verde profundo `#002C33`;
- **destaques da marca:** magenta `#E41A81`, rosa `#F76BC8` e rosa suave `#FFECF9`;
- **apoio informativo:** azul-petróleo `#138999`;
- **ícones:** SVGs leves incluídos diretamente nos fragments e cards, sempre acompanhados de texto;
- **modos:** claro, escuro, alto contraste, fonte ampliada e redução de animações possuem paletas e estados próprios;
- **impressão:** relatórios removem a navegação e exibem discretamente a logo e a identificação da ONG.

Os arquivos são servidos localmente: a operação do sistema não depende do site público. Não foi criado favicon porque não foi localizado um asset oficial especificamente apropriado para esse uso; nenhum símbolo foi inventado ou recortado da marca. Consulte `docs/IDENTIDADE_VISUAL.md` para fontes, decisões e regras de uso.

## Tecnologias e arquitetura

- Java 25;
- Maven e Maven Wrapper;
- Spring Boot 4.1.1;
- Spring Web MVC;
- Spring Data JPA e Hibernate;
- H2 persistente para desenvolvimento;
- Thymeleaf, HTML semântico e CSS próprio;
- Jakarta Bean Validation;
- JUnit e suporte de testes do Spring Boot.

A aplicação segue MVC e separa responsabilidades:

- **Controller:** recebe requisições, valida formulários e escolhe a resposta;
- **Service:** aplica regras de presença, frequência e finanças;
- **Repository:** acessa o banco;
- **Model:** representa o domínio;
- **DTO:** transporta dados de formulários e da API;
- **DashboardService:** consolida indicadores sem transferir cálculos importantes para os templates;
- **HistoricoOperacaoService:** registra a operação na mesma transação da alteração de negócio;
- **Templates:** apresentam páginas orientadas para pessoas com pouca experiência digital.

## Estrutura principal

~~~text
.
├── .mvn/wrapper/
├── docs/
├── src/
│   ├── main/
│   │   ├── java/com/ibmec/mulheresvalentes/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── fonts/
│   │       │   └── img/brand/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
~~~

O banco de desenvolvimento é criado em dados/, pasta ignorada pelo Git.

## 1. Instalar o Java 25 no Windows

1. Baixe um JDK 25 para Windows em uma distribuição confiável, como Eclipse Temurin, Microsoft Build of OpenJDK ou Oracle JDK.
2. Escolha a versão de 64 bits e conclua a instalação.
3. Quando o instalador oferecer as opções, habilite a configuração de JAVA_HOME e a inclusão do Java no PATH.
4. Feche e abra novamente o PowerShell e o VS Code.
5. Verifique:

~~~powershell
java -version
javac -version
~~~

Os dois comandos devem informar a versão 25. Se mostrarem outra versão, consulte “Problemas comuns”.

## 2. Instalar o Visual Studio Code

1. Baixe e instale o Visual Studio Code.
2. Abra o programa.
3. No painel **Extensões**, instale:
   - **Extension Pack for Java**, da Microsoft;
   - **Spring Boot Extension Pack**, da VMware, como apoio opcional.
4. Reinicie o VS Code se ele solicitar.

## 3. Abrir o projeto

1. No VS Code, selecione **Arquivo > Abrir Pasta**.
2. Escolha a pasta deste projeto, a que contém pom.xml e mvnw.cmd.
3. Confirme que confia nos arquivos somente se recebeu o projeto de uma fonte conhecida.
4. Aguarde a importação do Maven terminar. Na primeira vez, dependências serão baixadas da internet.
5. Abra **Terminal > Novo Terminal**. O prompt deve estar na raiz do projeto.

## 4. Iniciar a aplicação

No PowerShell integrado do VS Code, execute:

~~~powershell
./mvnw.cmd spring-boot:run
~~~

No Windows PowerShell, também funciona:

~~~powershell
.\mvnw.cmd spring-boot:run
~~~

Na primeira execução, o Maven Wrapper baixa o Maven 3.9.11. Aguarde a mensagem de que a aplicação foi iniciada. Depois, abra:

- Página pública da ONG: http://localhost:8080/
- Painel da equipe: http://localhost:8080/painel
- Console H2: http://localhost:8080/h2-console

Nunca é necessário instalar o Maven para usar o Wrapper.

## 5. Executar com Maven instalado

Se Maven já estiver instalado e o comando mvn -version funcionar:

~~~powershell
mvn spring-boot:run
~~~

O Maven do computador é opcional; o caminho recomendado é o Maven Wrapper.

## 6. Acessar o banco H2

Com a aplicação em execução:

1. Abra http://localhost:8080/h2-console.
2. Preencha:
   - **Driver Class:** org.h2.Driver
   - **JDBC URL:** jdbc:h2:file:./dados/mulheresvalentes
   - **User Name:** sa
   - **Password:** deixe vazio
3. Clique em **Connect**.

O console existe apenas para desenvolvimento e demonstração. Usuários comuns não precisam acessá-lo. Não exponha o console em uma implantação pública.

Os arquivos do banco ficam em dados/ e não são versionados. Faça cópia dessa pasta antes de qualquer manutenção. Nunca coloque no repositório dados reais de pessoas atendidas.

## 7. Executar os testes

Pare a aplicação se necessário e execute:

~~~powershell
.\mvnw.cmd clean test
~~~

Ou, com Maven instalado:

~~~powershell
mvn clean test
~~~

O Maven mostra BUILD SUCCESS quando todos os testes passam. Relatórios detalhados ficam em target/surefire-reports/.

## 8. Parar o servidor

Volte ao terminal em que a aplicação está rodando e pressione:

~~~text
Ctrl + C
~~~

Se o PowerShell perguntar se deseja finalizar o trabalho, confirme.

## Endereços principais

| Área | Endereço |
|---|---|
| Página pública da ONG | http://localhost:8080/ |
| Inscrição pública | http://localhost:8080/inscricao |
| Painel da equipe | http://localhost:8080/painel |
| Inscrições recebidas | http://localhost:8080/inscricoes |
| Participantes | http://localhost:8080/participantes |
| Atividades | http://localhost:8080/atividades |
| Registro de presença | http://localhost:8080/presencas/registrar |
| Saldo do caixa (API) | http://localhost:8080/api/caixa/saldo |
| Extrato do caixa (API) | http://localhost:8080/api/caixa/extrato |
| Frequência | http://localhost:8080/frequencia |
| Livro Caixa | http://localhost:8080/caixa |
| Movimentações | http://localhost:8080/caixa/movimentacoes |
| Histórico do sistema | http://localhost:8080/historico |
| Relatórios | http://localhost:8080/relatorios |
| Ajuda | http://localhost:8080/ajuda |
| Acessibilidade | http://localhost:8080/acessibilidade |
| Console H2 | http://localhost:8080/h2-console |

Consulte docs/ENDPOINTS.md para o contrato preliminar completo das páginas e APIs.

## Página pública e inscrições

A raiz do sistema (`/`) é a página de apresentação da ONG, pensada para as
mulheres que podem participar dela. O conteúdo vem de fontes da própria ONG: a
seção **Nossa história** reproduz na íntegra o texto enviado pela Karine, e
endereço, telefone, e-mail e projetos vêm do site oficial.

Pelo botão **Quero participar**, a mulher preenche uma inscrição curta: nome,
contato, como prefere ser contatada, como e quando é seguro falar com ela,
áreas de interesse e uma mensagem opcional. O envio exige consentimento.

Antes de enviar, ela passa por uma tela de **conferência** com tudo o que
escreveu e o contato em destaque, e escolhe entre **Corrigir meus dados** e
**Está tudo certo, enviar inscrição**. Nada é gravado antes disso. O contato
também é verificado: telefone precisa ter DDD e número completos, e-mail
precisa estar no formato nome@exemplo.com, e a forma preferida precisa combinar
com o contato (e-mail com e-mail, ligação ou WhatsApp com telefone). Telefones
são guardados sempre no mesmo formato, por exemplo (11) 91234-5678, e um clique
duplo no botão de envio não cria inscrição repetida.

Como a ONG atua no enfrentamento à violência doméstica, as páginas públicas
têm um botão **Sair rápido** no topo e mostram os telefones 190 e 180.

Do lado da equipe:

- o menu **Inscrições** mostra quantas inscrições novas estão esperando;
- o painel exibe um aviso quando há inscrições novas;
- em cada inscrição, a equipe pode marcar que está em contato, aceitar ou arquivar,
  sempre passando pela tela de revisão;
- ao aceitar, a inscrita vira participante com nome e contato. Se já existir uma
  participante com o mesmo contato, a inscrição é vinculada a ela, sem criar
  duplicado, e o cadastro é reativado se estiver desativado.

### Fotos da página

Coloque fotos em `src/main/resources/static/img/publico/galeria/` (jpg, png ou
webp) e reinicie a aplicação: elas aparecem na seção **Nossos encontros**. O
texto alternativo vem do nome do arquivo, então `roda-de-conversa.jpg` vira
"Roda de conversa". Sem fotos na pasta, a seção não aparece. Use apenas fotos
com autorização das pessoas retratadas, principalmente quando houver crianças.

As duas imagens atuais (`acolhimento.jpg` e `rede-de-mulheres.jpg`) vieram do
site oficial da ONG e são ilustrações, não fotos das atividades.

## Painel da equipe

O menu lateral mostra só o que é usado no dia a dia: **Visão geral**,
**Inscrições**, **Participantes**, **Registrar presença**, **Livro Caixa**,
**Relatórios** e **Ajuda e acessibilidade**. As demais páginas (atividades,
encontros, frequência, histórico de presença, movimentações, categorias,
histórico do sistema e acessibilidade) ficam em **Mais opções**, que já abre
sozinho quando uma delas está aberta.

A **Visão geral** começa pelas três ações do dia (presença, entrada e saída),
mostra o aviso de inscrições novas, quatro indicadores (saldo, frequência do
mês, participantes ativas e entradas do mês) e as últimas movimentações.

No **Livro Caixa**, os indicadores mostram o caixa todo (ou o período escolhido
em **Filtrar por período**), e os botões **Todas**, **Entradas**, **Saídas** e
**Doações** filtram só a tabela, que mostra até 15 movimentações. A lista
completa fica em **Movimentações**.

## Uso rápido

### Cadastrar uma pessoa

1. Na Home, escolha **Cadastrar participante**.
2. Informe o nome e, se a pessoa autorizar, um contato. A observação é opcional.
3. Selecione **Revisar cadastro**.
4. Confira o resumo e selecione **Confirmar cadastro**.
5. Confira a mensagem de sucesso.

### Registrar presença

1. Na Home, escolha **Registrar presença**. A tela já abre com a data de hoje.
2. Se a chamada for de outro dia, troque a data e selecione **Atualizar lista**.
3. Marque a situação de cada participante.
4. Selecione **Salvar presença** e revise os totais.
5. Se tudo estiver correto, selecione **Confirmar presença**.

Vincular a chamada a uma atividade é opcional: escolha a atividade e o encontro
somente se quiser separar o registro por programa.

### Registrar entrada ou saída

1. Na Home, escolha **Registrar entrada** ou **Registrar saída**.
2. Informe a data e responda se o lançamento tem valor em dinheiro.
3. Se tiver, informe categoria, descrição e valor.
4. Se for uma doação de material, informe categoria, descrição e o **comprovante**: não há valor a preencher.
5. Selecione o botão para revisar e confira o tipo, o valor e o saldo previsto.
6. Confirme a operação e confira a mensagem de sucesso.

### Consultar alterações administrativas

1. No menu lateral, abra **Mais opções** e escolha **Histórico do sistema**.
2. Escolha período, módulo ou tipo de ação se quiser reduzir a lista.
3. Use a busca para localizar um nome ou descrição.
4. O histórico é somente leitura: ele não pode ser editado ou apagado pela interface comum.

O guia em linguagem simples está em docs/GUIA_USUARIO.md.

## Dados de demonstração

O carregador de dados pode criar registros fictícios para facilitar a apresentação. Eles servem apenas para demonstrar os fluxos. Nenhum nome, atividade, categoria ou valor deve ser interpretado como informação oficial da ONG.

A configuração app.dados-demonstracao=true controla o carregamento no ambiente de desenvolvimento. Antes de usar com dados reais, é necessário revisar privacidade, acesso, backup, segurança e consentimento com a ONG.

Em uma base vazia, a versão atual cria uma demonstração que segue a realidade
descrita na entrevista: 36 participantes, 6 atividades, 24 encontros, 972
registros de presença (864 por encontro e 108 do controle único por data), 13
categorias e 30 movimentações financeiras — 12 aluguéis mensais de R$ 1.200, 8
doações de material com comprovante e sem valor, e 10 saídas. A carga acontece
somente uma vez e jamais apaga ou completa automaticamente um banco já
preenchido.

## Revisar antes de confirmar

Toda alteração feita pelas telas segue o fluxo **Preencher → Revisar →
Confirmar**. A página de revisão mostra os dados, diferenças de antes/depois e,
no financeiro, o saldo previsto. Nada é gravado ao abrir essa página ou ao usar
**Voltar e corrigir**. O botão final usa uma confirmação de uso único, impedindo
que dois cliques criem duas operações.

## Busca em todo o sistema

Use o campo **Buscar no sistema...** no topo de qualquer página. Os resultados
de participantes, atividades, encontros, movimentações e categorias são
mostrados em grupos, sempre com um caminho direto para o registro relacionado.

## Problemas comuns

### “java” não é reconhecido

- feche e reabra o VS Code depois de instalar o JDK;
- execute where.exe java para descobrir qual instalação está sendo usada;
- confira se JAVA_HOME aponta para a pasta do JDK 25, não para a pasta bin;
- confirme que %JAVA_HOME%\bin está no PATH.

### O projeto usa uma versão de Java diferente da 25

Execute java -version e javac -version. No VS Code, abra **Java: Configure Java Runtime** pela Paleta de Comandos e selecione o JDK 25 para o projeto.

### O Maven Wrapper não baixa dependências

Confirme a conexão com a internet e se a rede permite acesso a repo.maven.apache.org. Em rede institucional, pode ser necessário solicitar configuração de proxy ao responsável técnico.

### A porta 8080 já está em uso

Pare a outra aplicação que está usando a porta. Para localizar o processo:

~~~powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
~~~

Evite encerrar processos que você não reconhece. Como alternativa temporária de desenvolvimento:

~~~powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
~~~

Nesse caso, acesse http://localhost:8081/.

### O banco não abre

Confira se a aplicação tem permissão para criar a pasta dados/ e se outra execução não está prendendo o arquivo. Não apague o banco que contém informação importante. Para um ambiente exclusivamente demonstrativo, faça primeiro uma cópia de dados/ e somente então recrie a base se isso for realmente necessário.

### Alterações visuais não aparecem

Atualize a página com Ctrl + F5. Em desenvolvimento, o cache do Thymeleaf está desativado, mas o navegador ainda pode manter CSS em cache.

### Um teste falhou

Leia o primeiro erro em target/surefire-reports/. Confirme que Java 25 está ativo e que nenhum teste depende do banco de desenvolvimento. Não ignore falhas antes de apresentar ou entregar o projeto.

## Documentação

- docs/ESCOPO_PRELIMINAR.md — limites e objetivos provisórios;
- docs/SABEMOS_SUPOMOS_NAO_SABEMOS.md — separação do conhecimento atual;
- docs/ROTEIRO_ENTREVISTA.md — perguntas abertas para a ONG;
- docs/PREMISSAS_E_DUVIDAS.md — registro vivo de hipóteses e decisões pendentes;
- docs/BACKLOG.md — histórias preliminares e critérios de aceitação;
- docs/MODELO_DADOS.md — entidades, relações e regras de integridade;
- docs/ENDPOINTS.md — páginas web e contrato REST;
- docs/GUIA_USUARIO.md — instruções para pessoas com pouca experiência digital;
- docs/DECISOES_TECNICAS.md — escolhas de arquitetura e seus motivos.
- docs/IDENTIDADE_VISUAL.md — origem dos assets, paleta, tipografia, componentes e validação do redesign.

## Acesso da equipe

O painel não aparece mais em nenhum link da página pública: para chegar nele é
preciso digitar `/admin` no endereço. Essa tela pede usuário e senha e, só
depois disso, libera `/painel` e as demais telas administrativas. Quem tentar
abrir qualquer endereço interno sem ter entrado volta para `/admin`, e as rotas
`/api/**` respondem `401`. O botão **Sair**, no alto do painel, encerra a sessão.

O console do banco (`/h2-console`) também exige login. Ele roda fora do Spring
MVC, então é protegido por um filtro próprio — sem isso, qualquer pessoa leria e
apagaria o banco inteiro sem passar pela tela de entrada.

As credenciais valem para uma única conta compartilhada e vêm das variáveis de
ambiente `MV_ADMIN_USUARIO` e `MV_ADMIN_SENHA`. Não há valor padrão: sem as duas
definidas a aplicação não sobe, e reclama de qual faltou. É proposital — assim
nenhuma senha fica guardada no repositório.

Para rodar na sua máquina, defina as duas antes de subir:

```bash
MV_ADMIN_USUARIO=equipe MV_ADMIN_SENHA=escolha-uma ./mvnw spring-boot:run
```

No IntelliJ, o mesmo vai em *Run > Edit Configurations > Environment variables*.

## Limites antes do uso real

**O login atual é um protótipo e não basta para dados reais.** A senha fica em
texto no arquivo de configuração, existe uma conta só e o sistema não registra
quem fez cada ação. A PR03 indica que pelo menos duas contas serão necessárias
(Karine e Isabelle), com perfis diferentes — isso ainda não existe.

O que já foi feito contra os riscos mais básicos: a sessão é trocada no login
(evita fixação de sessão), o cookie é `HttpOnly` e `SameSite=Lax`, o
identificador da sessão não aparece na URL, as telas do painel não ficam no
cache do navegador e a comparação da senha não vaza tempo. Ainda faltam, antes
de qualquer uso real: senhas com hash, contas individuais por pessoa, HTTPS
com cookie `Secure`, token anti-CSRF nos formulários e limite de tentativas de
senha.

Perfis de acesso, identificação do responsável por cada ação, política organizacional de correção/estorno, backup, hospedagem, privacidade, volume de dados e regras de prestação de contas também ainda precisam ser discutidos. A trilha administrativa já existe no protótipo, mas não substitui uma política formal de auditoria. Este projeto não deve ser colocado em produção nem receber dados pessoais reais antes dessas definições e de uma avaliação de segurança.

## Licença e uso

O uso acadêmico deste código não autoriza divulgar informações reais da ONG ou das pessoas atendidas. A licença de distribuição do projeto ainda não foi definida.
