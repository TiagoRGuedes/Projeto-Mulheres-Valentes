# Identidade visual do sistema administrativo

## Objetivo

Este documento registra a transformação visual do sistema de presença e livro caixa. A interface foi desenhada como uma aplicação administrativa interna da ONG Mulheres Valentes, não como cópia de seu site público.

O backend, o banco, as regras, as APIs e os fluxos existentes foram preservados. O trabalho concentrou-se em marca, navegação, hierarquia, componentes, linguagem, acessibilidade, responsividade e impressão.

## Fontes de referência

### Fonte principal

- Site oficial: `https://mulheresvalentes.org.br/`
- Logo publicada pelo site: `https://mulheresvalentes.org.br/wp-content/uploads/2026/04/logo-mulheres-valentes.png`
- Fonte Manrope publicada pelo tema do site: `https://mulheresvalentes.org.br/wp-content/themes/twentytwentyfive/assets/fonts/manrope/Manrope-VariableFont_wght.woff2`

O site oficial apresenta verde-petróleo escuro em títulos e áreas de confiança, magenta e rosa em chamadas e navegação, fundos claros e tipografia Manrope com pesos fortes. Esses elementos foram convertidos em um Design System administrativo.

### Fonte complementar

- Instagram oficial informado: `https://www.instagram.com/valentesmulheres/`

O perfil foi mantido como link oficial no rodapé. A plataforma bloqueou a leitura automatizada durante a pesquisa; por isso, nenhuma imagem, texto, pessoa ou dado do Instagram foi copiado para o projeto. A definição visual usa o site oficial como referência principal, conforme solicitado.

## Assets locais

| Asset | Caminho no projeto | Uso |
|---|---|---|
| Logo oficial PNG | `src/main/resources/static/img/brand/logo-mulheres-valentes.png` | header, Home, footer e impressão |
| Manrope variável WOFF2 | `src/main/resources/static/fonts/manrope-variable.woff2` | tipografia de toda a interface |

A logo não foi redesenhada, recolorida, recortada ou distorcida. Containers usam `object-fit: contain` e dimensões proporcionais. Os assets são locais para que a aplicação continue funcionando sem conexão com o site da ONG.

Não foi criado favicon. O arquivo oficial encontrado é uma composição vertical completa e não foi considerado adequado para um ícone pequeno; inventar ou recortar um símbolo contrariaria a preservação da marca.

## Design System

### Cores institucionais

| Token conceitual | Cor | Uso principal |
|---|---:|---|
| `--mv-primary` | `#144047` | navegação, botões principais e confiança institucional |
| `--mv-primary-dark` | `#002C33` | header, footer e áreas de alta hierarquia |
| `--mv-secondary` | `#E41A81` | seleção, linha de marca e destaques curtos |
| `--mv-accent` | `#F76BC8` | grafismos e estados brandizados discretos |
| `--mv-accent-soft` | `#FFECF9` | superfícies suaves e orientação |
| `--mv-teal` | `#138999` | foco informativo, filtros e ajuda contextual |

Cores de entrada, saída, erro, aviso e sucesso são semânticas. Setas, sinais, rótulos e texto garantem que cor nunca seja a única informação.

### Tipografia

Manrope é carregada por `@font-face` local com `font-display: swap`. A pilha de fallback usa Segoe UI e fontes do sistema. Títulos têm peso forte e espaçamento compacto; textos de apoio priorizam leitura e tamanho confortável.

### Componentes

- header com logo oficial, identificação administrativa, Ajuda e Acessibilidade;
- sidebar organizada por Presença, Livro Caixa, Consulta e Suporte;
- SVGs consistentes, leves e sempre acompanhados de texto;
- hero operacional sem fotografia ou conteúdo sensível;
- cards com hierarquia, descrição, foco e ação explícita;
- botões primário, secundário, discreto, entrada, saída e perigoso;
- formulários com campos grandes, labels, ajuda e erro próximo;
- tabelas com cabeçalho, hover, badges e adaptação para cards no celular;
- estados vazios com explicação e próximo passo;
- relatórios com marca discreta em impressão preto e branco.

## Decisões de UX

1. A Home prioriza Registrar presença, Registrar entrada e Registrar saída.
2. Cadastros, consultas e suporte aparecem em grupos com nomes diretos.
3. O menu móvel continua visível como faixa horizontal rolável, evitando esconder funções essenciais atrás de um ícone sem texto.
4. Ajuda oferece oito guias em linguagem cotidiana: presença, participante, atividade, entrada, saída, saldo, movimentações e relatório.
5. Participantes continuam com dados mínimos; nenhum campo sensível foi adicionado.
6. Entrada e saída usam seta, verbo, sinal e cor para reduzir enganos.
7. A logo é protagonista no ambiente, enquanto a atribuição acadêmica fica secundária no rodapé.

## Acessibilidade e responsividade

Foram preservados link de pular conteúdo, HTML semântico, labels, foco visível, ARIA, navegação por teclado e preferências de sessão. O redesign inclui paleta própria para modo escuro e regras específicas de alto contraste; não usa inversão por filtro.

As revisões visuais foram realizadas em larguras representativas de 1366 px, 768 px e 390 px, com captura de página completa. Também foram verificados modo escuro, alto contraste, fonte normal, grande, muito grande e redução de animações.

## Validação realizada

- 33 testes automatizados aprovados após a evolução administrativa;
- 62 fontes Java compiladas com Java 25;
- rotas web principais renderizadas com HTTP 200;
- logo, CSS e fonte local servidos com HTTP 200;
- cadastro, edição e detalhe de participante verificados;
- presença fictícia registrada e localizada no histórico;
- entrada fictícia de R$ 123,45 e saída fictícia de R$ 23,45 registradas;
- variação de saldo conferida em R$ 100,00;
- filtros e relatório financeiro com identidade de impressão verificados.

Todos os registros usados nessa validação são explicitamente fictícios.
