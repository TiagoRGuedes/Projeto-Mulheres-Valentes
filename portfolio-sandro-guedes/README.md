# Portfolio: Sandro Guedes

Site estático de portfolio profissional de **Sandro Guedes P. da Costa**, Global Partner Director na Pipefy.
É feito com HTML, CSS e JavaScript puros, sem build, sem dependências e sem fontes ou scripts externos.

## Como abrir

Abra `index.html` no navegador. Para servir localmente:

```bash
cd portfolio-sandro-guedes
python3 -m http.server 8000
# http://localhost:8000
```

## Estrutura

```
portfolio-sandro-guedes/
├── index.html                      # todo o conteúdo, uma seção por bloco comentado
├── css/styles.css                  # estilos; cores, fontes e espaçamentos nos tokens do :root
├── js/main.js                      # menu mobile, link ativo, duração do cargo atual, ano do rodapé
└── assets/
    └── sandro-guedes-curriculo.pdf # PDF do perfil exportado do LinkedIn (botão "Baixar currículo")
```

Seções: Início (hero) · Números · Sobre · Experiência · Competências e certificações · Formação · Na mídia · Contato.

## De onde veio o conteúdo

| Fonte | O que foi usado |
|---|---|
| PDF do perfil do LinkedIn (exportado em set/2026) | headline, resumo, contato, experiências completas, formação, principais competências e certificações |
| [Comunicado da Pipefy, 23/06/2026](https://www.pipefy.com/press-release/pipefy-announces-new-executives-to-lead-the-global-expansion-of-partnerships-and-strategic-alliances/) | nomeação como Global Director of Partnerships e contexto do time de parcerias |

- Não há dados inventados. Os números da seção "Números" saem do PDF: carreira em tecnologia desde 2000, cerca de 600 clientes no Google Cloud e o tempo em cada empresa.
- Os cargos ficam em inglês, como no LinkedIn. As descrições foram traduzidas do inglês com fidelidade.
- Correções de digitação do original: "Plataform" → "Platform" e "Suport Analyst" → "Support Analyst".
- A duração do cargo atual ("jan 2026 – atual · N meses") é calculada no navegador pelo atributo `data-since`, com a mesma contagem do LinkedIn.

## Pendências (não estão no PDF, só no LinkedIn)

O LinkedIn bloqueia acesso automatizado, então os itens abaixo ficaram marcados com `TODO` no `index.html`:

- [ ] **Foto de perfil**: salvar em `assets/sandro-guedes.jpg` e trocar o placeholder "SG" do hero (instrução no comentário do HTML)
- [ ] **Lista completa de competências**: o PDF traz só as 3 principais (ITIL, Pre-sales, PMP)
- [ ] **Emissor e data das certificações**
- [ ] **Curso e período** na Universidade Católica de Petrópolis
- [ ] **Destaques, atividade, artigos e palestras** para a seção "Na mídia"
- [ ] **Recomendações**, que podem virar uma seção de depoimentos
- [ ] **Idiomas, voluntariado e prêmios**, se houver
- [ ] **Investimentos-anjo e advisory**: startups que ele queira divulgar
- [ ] Opcional: **versão em inglês** do site

## Design

O design é uma base neutra, pensada para ser trocada:

- Paleta, tipografia, raios e espaçamentos são variáveis CSS no topo de `css/styles.css`.
- O modo escuro sai do `prefers-color-scheme` e redefine os mesmos tokens.
- A fonte usa a pilha do sistema (`Inter` se estiver instalada). Para usar uma webfont, importe-a no `<head>` e ajuste `--font-sans`.
- Os quadrados com iniciais nas experiências são placeholders. Troque por logos oficiais só se houver permissão de uso.
- Há estilos de impressão (`@media print`), então o site também pode virar PDF pelo navegador.
