package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.HistoricoOperacao;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.model.NaturezaLancamento;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.repository.AtividadeRepository;
import com.ibmec.mulheresvalentes.repository.CategoriaFinanceiraRepository;
import com.ibmec.mulheresvalentes.repository.EncontroRepository;
import com.ibmec.mulheresvalentes.repository.HistoricoOperacaoRepository;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import com.ibmec.mulheresvalentes.repository.PresencaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.dados-demonstracao", havingValue = "true")
public class DadosDemonstracaoConfig implements ApplicationRunner {

    private static final int QUANTIDADE_PARTICIPANTES = 36;
    private static final int QUANTIDADE_ENCONTROS = 24;

    private static final int MESES_DE_ALUGUEL = 12;
    private static final BigDecimal VALOR_ALUGUEL = new BigDecimal("1200.00");
    private static final int QUANTIDADE_DOACOES_MATERIAL = 8;
    private static final int QUANTIDADE_SAIDAS = 10;

    private static final int DIAS_DE_CHAMADA_AVULSA = 3;

    private static final String[] NOMES_FICTICIOS = {
        "Ana Exemplo", "Beatriz Modelo", "Carla Demonstração", "Daniela Exemplo",
        "Elisa Modelo", "Fernanda Demonstração", "Gabriela Exemplo", "Helena Modelo",
        "Isabela Demonstração", "Janaína Exemplo", "Karen Modelo", "Larissa Demonstração",
        "Marina Exemplo", "Natália Modelo", "Olívia Demonstração", "Paula Exemplo",
        "Quézia Modelo", "Renata Demonstração", "Sabrina Exemplo", "Talita Modelo",
        "Úrsula Demonstração", "Valéria Exemplo", "Wanda Modelo", "Yara Demonstração",
        "Alice Modelo", "Bruna Demonstração", "Cecília Exemplo", "Débora Modelo",
        "Ester Demonstração", "Flávia Exemplo", "Giovana Modelo", "Heloísa Demonstração",
        "Irene Exemplo", "Joana Modelo", "Lívia Demonstração", "Mônica Exemplo"
    };

    private static final int[] PRESENCAS_POR_FAIXA = {23, 21, 19, 17, 14, 10};

    private static final String[] DESCRICOES_DOACAO_MATERIAL = {
        "Doação de cestas básicas",
        "Doação de material de papelaria",
        "Doação de roupas",
        "Doação de produtos de higiene",
        "Doação de livros",
        "Doação de brinquedos",
        "Doação de material de limpeza",
        "Doação de tecidos para oficina"
    };

    private static final String[] COMPROVANTES_DOACAO = {
        "Recibo fictício 001/2026 — entrega na sede",
        "Recibo fictício 002/2026 — 4 caixas conferidas",
        "Recibo fictício 003/2026 — lista de itens assinada",
        "Recibo fictício 004/2026 — entrega registrada em foto",
        "Recibo fictício 005/2026 — 2 caixas de livros",
        "Recibo fictício 006/2026 — doação de pessoa física",
        "Recibo fictício 007/2026 — entrega conferida por duas pessoas",
        "Recibo fictício 008/2026 — 5 metros de tecido"
    };

    private static final String[] DESCRICOES_SAIDA = {
        "Materiais para atividade",
        "Transporte para encontro",
        "Lanche para oficina",
        "Manutenção do espaço",
        "Serviço de apoio demonstrativo",
        "Itens de papelaria",
        "Passagens para atividade",
        "Água e alimentos para encontro",
        "Pequeno reparo no espaço",
        "Impressão de materiais"
    };

    private static final String[] VALORES_SAIDA = {
        "146.75", "82.40", "195.30", "310.00", "225.00",
        "68.90", "124.50", "178.25", "245.80", "96.00"
    };

    private static final String[] CONTATOS_FICTICIOS = {
        "(11) 90000-0001", "(11) 90000-0002", "exemplo1@demonstracao.invalid",
        "(11) 90000-0004", "(11) 90000-0005", "exemplo2@demonstracao.invalid"
    };

    private final ParticipanteRepository participanteRepository;
    private final AtividadeRepository atividadeRepository;
    private final EncontroRepository encontroRepository;
    private final PresencaRepository presencaRepository;
    private final CategoriaFinanceiraRepository categoriaRepository;
    private final LancamentoCaixaRepository lancamentoRepository;
    private final HistoricoOperacaoRepository historicoRepository;

    public DadosDemonstracaoConfig(
            ParticipanteRepository participanteRepository,
            AtividadeRepository atividadeRepository,
            EncontroRepository encontroRepository,
            PresencaRepository presencaRepository,
            CategoriaFinanceiraRepository categoriaRepository,
            LancamentoCaixaRepository lancamentoRepository,
            HistoricoOperacaoRepository historicoRepository) {
        this.participanteRepository = participanteRepository;
        this.atividadeRepository = atividadeRepository;
        this.encontroRepository = encontroRepository;
        this.presencaRepository = presencaRepository;
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.historicoRepository = historicoRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        carregarPresencasSeBancoNovo();
        carregarCaixaSeBancoNovo();
    }

    private void carregarPresencasSeBancoNovo() {
        if (participanteRepository.count() != 0
                || atividadeRepository.count() != 0
                || encontroRepository.count() != 0
                || presencaRepository.count() != 0) {
            return;
        }

        List<Participante> participantes = criarParticipantes();
        List<Atividade> atividades = criarAtividades();
        List<Encontro> encontros = criarEncontros(atividades);
        List<Presenca> presencas = criarPresencas(participantes, encontros);
        criarPresencasAvulsas(participantes);
        criarHistoricoDePresenca(participantes, atividades, encontros, presencas);
    }

    private List<Participante> criarParticipantes() {
        List<Participante> participantes = new ArrayList<>(QUANTIDADE_PARTICIPANTES);
        for (int indice = 0; indice < QUANTIDADE_PARTICIPANTES; indice++) {
            Participante participante = new Participante(
                    NOMES_FICTICIOS[indice],
                    indice % 7 == 0 ? null : CONTATOS_FICTICIOS[indice % CONTATOS_FICTICIOS.length],
                    indice % 9 == 0 ? "Cadastro inteiramente fictício para apresentação." : null);
            if (indice >= QUANTIDADE_PARTICIPANTES - 4) {
                participante.desativar();
            }
            participantes.add(participante);
        }
        return participanteRepository.saveAll(participantes);
    }

    private List<Atividade> criarAtividades() {
        List<Atividade> atividades = new ArrayList<>(List.of(
                new Atividade(
                        "Roda de conversa demonstrativa",
                        "Atividade fictícia para apresentar o registro de encontros."),
                new Atividade(
                        "Oficina de autonomia — exemplo",
                        "Nome ilustrativo e editável; não representa programação oficial."),
                new Atividade(
                        "Encontro de acolhimento fictício",
                        "Exemplo criado somente para uso acadêmico do sistema."),
                new Atividade(
                        "Oficina criativa demonstrativa",
                        "Atividade inteiramente fictícia para exercitar os relatórios."),
                new Atividade(
                        "Formação comunitária — modelo",
                        "Exemplo genérico, sujeito à confirmação e substituição pela ONG."),
                new Atividade(
                        "Bem-estar em grupo — exemplo",
                        "Atividade ilustrativa sem vínculo com a agenda real da ONG.")));
        atividades.getLast().desativar();
        return atividadeRepository.saveAll(atividades);
    }

    private List<Encontro> criarEncontros(List<Atividade> atividades) {
        LocalDate hoje = LocalDate.now();
        List<Encontro> encontros = new ArrayList<>(QUANTIDADE_ENCONTROS);
        for (int indice = 0; indice < QUANTIDADE_ENCONTROS; indice++) {
            LocalDate data = hoje.minusDays((QUANTIDADE_ENCONTROS - 1L - indice) * 5L + 2L);
            Atividade atividade = atividades.get(indice % atividades.size());
            encontros.add(new Encontro(
                    atividade,
                    data,
                    indice % 6 == 0 ? "Encontro fictício usado na demonstração." : null));
        }
        return encontroRepository.saveAll(encontros);
    }

    private List<Presenca> criarPresencas(
            List<Participante> participantes,
            List<Encontro> encontros) {
        List<Presenca> presencas = new ArrayList<>(participantes.size() * encontros.size());
        for (int indiceParticipante = 0; indiceParticipante < participantes.size(); indiceParticipante++) {
            int quantidadePresente = PRESENCAS_POR_FAIXA[indiceParticipante % PRESENCAS_POR_FAIXA.length];
            for (int indiceEncontro = 0; indiceEncontro < encontros.size(); indiceEncontro++) {
                int posicaoNoCiclo = Math.floorMod(indiceEncontro + indiceParticipante * 5, QUANTIDADE_ENCONTROS);
                StatusPresenca status;
                String observacoes = null;
                if (posicaoNoCiclo < quantidadePresente) {
                    status = StatusPresenca.PRESENTE;
                } else if ((indiceEncontro + indiceParticipante) % 3 == 0) {
                    status = StatusPresenca.JUSTIFICADA;
                    observacoes = "Justificativa fictícia para demonstração.";
                } else {
                    status = StatusPresenca.AUSENTE;
                }
                presencas.add(new Presenca(
                        participantes.get(indiceParticipante),
                        encontros.get(indiceEncontro),
                        status,
                        observacoes));
            }
        }
        return presencaRepository.saveAll(presencas);
    }

    private void criarPresencasAvulsas(List<Participante> participantes) {
        LocalDate hoje = LocalDate.now();
        List<Presenca> avulsas = new ArrayList<>(participantes.size() * DIAS_DE_CHAMADA_AVULSA);
        for (int dia = 0; dia < DIAS_DE_CHAMADA_AVULSA; dia++) {
            LocalDate data = hoje.minusDays(dia);
            for (Participante participante : participantes) {
                avulsas.add(new Presenca(participante, data, StatusPresenca.PRESENTE, null));
            }
        }
        presencaRepository.saveAll(avulsas);
    }

    private void criarHistoricoDePresenca(
            List<Participante> participantes,
            List<Atividade> atividades,
            List<Encontro> encontros,
            List<Presenca> presencas) {
        Participante primeira = participantes.getFirst();
        Participante participanteInativa = participantes.getLast();
        Atividade primeiraAtividade = atividades.getFirst();
        Atividade atividadeInativa = atividades.getLast();
        Encontro recente = encontros.getLast();
        Presenca presencaExemplo = presencas.getLast();

        historicoRepository.saveAll(List.of(
                historico(
                        ModuloHistorico.PARTICIPANTES, "Participante", primeira.getId(), primeira.getNome(),
                        AcaoHistorico.CRIACAO, "Participante fictícia cadastrada para demonstração."),
                historico(
                        ModuloHistorico.PARTICIPANTES, "Participante", participanteInativa.getId(),
                        participanteInativa.getNome(),
                        AcaoHistorico.DESATIVACAO,
                        "Participante fictícia desativada; seus registros anteriores foram mantidos."),
                historico(
                        ModuloHistorico.ATIVIDADES, "Atividade", primeiraAtividade.getId(),
                        primeiraAtividade.getNome(), AcaoHistorico.CRIACAO,
                        "Atividade demonstrativa cadastrada."),
                historico(
                        ModuloHistorico.ATIVIDADES, "Atividade", atividadeInativa.getId(),
                        atividadeInativa.getNome(),
                        AcaoHistorico.DESATIVACAO,
                        "Atividade demonstrativa desativada; seu histórico foi mantido."),
                historico(
                        ModuloHistorico.ENCONTROS, "Encontro", recente.getId(), nomeEncontro(recente),
                        AcaoHistorico.CRIACAO, "Encontro demonstrativo cadastrado."),
                historico(
                        ModuloHistorico.PRESENCAS, "Encontro", recente.getId(), nomeEncontro(recente),
                        AcaoHistorico.REGISTRO,
                        "Presenças demonstrativas registradas para 36 participantes."),
                historico(
                        ModuloHistorico.PRESENCAS, "Chamada", null,
                        "Chamada de " + LocalDate.now(),
                        AcaoHistorico.REGISTRO,
                        "Chamada demonstrativa do controle único, sem vínculo com atividade."),
                new HistoricoOperacao(
                        ModuloHistorico.PRESENCAS,
                        "Presenca",
                        presencaExemplo.getId(),
                        presencaExemplo.getParticipante().getNome(),
                        AcaoHistorico.CORRECAO,
                        "Exemplo de correção de presença no cenário demonstrativo.",
                        "Situação: Ausente",
                        "Situação: " + presencaExemplo.getStatus().getDescricao())));
    }

    private void carregarCaixaSeBancoNovo() {
        if (categoriaRepository.count() != 0 || lancamentoRepository.count() != 0) {
            return;
        }

        List<CategoriaFinanceira> entradas = criarCategoriasEntrada();
        List<CategoriaFinanceira> saidas = criarCategoriasSaida();
        List<LancamentoCaixa> lancamentos = criarLancamentos(entradas, saidas);
        criarHistoricoFinanceiro(entradas, saidas, lancamentos);
    }

    private List<CategoriaFinanceira> criarCategoriasEntrada() {
        List<CategoriaFinanceira> categorias = new ArrayList<>(List.of(
                new CategoriaFinanceira("Aluguel/Patrocínio", TipoLancamento.ENTRADA),
                new CategoriaFinanceira("Doação de material", TipoLancamento.ENTRADA),
                new CategoriaFinanceira("Doações", TipoLancamento.ENTRADA),
                new CategoriaFinanceira("Contribuições", TipoLancamento.ENTRADA),
                new CategoriaFinanceira("Eventos solidários", TipoLancamento.ENTRADA),
                new CategoriaFinanceira("Parcerias", TipoLancamento.ENTRADA),
                new CategoriaFinanceira("Outras entradas", TipoLancamento.ENTRADA)));
        categorias.getLast().desativar();
        return categoriaRepository.saveAll(categorias);
    }

    private List<CategoriaFinanceira> criarCategoriasSaida() {
        List<CategoriaFinanceira> categorias = new ArrayList<>(List.of(
                new CategoriaFinanceira("Materiais de atividades", TipoLancamento.SAIDA),
                new CategoriaFinanceira("Transporte", TipoLancamento.SAIDA),
                new CategoriaFinanceira("Alimentação", TipoLancamento.SAIDA),
                new CategoriaFinanceira("Manutenção", TipoLancamento.SAIDA),
                new CategoriaFinanceira("Serviços", TipoLancamento.SAIDA),
                new CategoriaFinanceira("Outras saídas", TipoLancamento.SAIDA)));
        categorias.getLast().desativar();
        return categoriaRepository.saveAll(categorias);
    }

    private List<LancamentoCaixa> criarLancamentos(
            List<CategoriaFinanceira> categoriasEntrada,
            List<CategoriaFinanceira> categoriasSaida) {
        LocalDate hoje = LocalDate.now();
        CategoriaFinanceira categoriaAluguel = categoriasEntrada.getFirst();
        CategoriaFinanceira categoriaMaterial = categoriasEntrada.get(1);
        List<LancamentoCaixa> lancamentos = new ArrayList<>(
                MESES_DE_ALUGUEL + QUANTIDADE_DOACOES_MATERIAL + QUANTIDADE_SAIDAS);

        for (int mes = MESES_DE_ALUGUEL - 1; mes >= 0; mes--) {
            lancamentos.add(new LancamentoCaixa(
                    hoje.minusMonths(mes).withDayOfMonth(5),
                    TipoLancamento.ENTRADA,
                    NaturezaLancamento.MONETARIA,
                    categoriaAluguel,
                    "Aluguel/patrocínio mensal",
                    VALOR_ALUGUEL,
                    null,
                    mes == 0 ? "Valor fixo informado pela ONG; cenário fictício." : null));
        }

        for (int indice = 0; indice < QUANTIDADE_DOACOES_MATERIAL; indice++) {
            lancamentos.add(new LancamentoCaixa(
                    hoje.minusDays(indice * 11L + 3L),
                    TipoLancamento.ENTRADA,
                    NaturezaLancamento.DOACAO_MATERIAL,
                    categoriaMaterial,
                    DESCRICOES_DOACAO_MATERIAL[indice],
                    null,
                    COMPROVANTES_DOACAO[indice],
                    null));
        }

        for (int indice = 0; indice < QUANTIDADE_SAIDAS; indice++) {
            CategoriaFinanceira categoria = categoriasSaida.get(
                    indice % (categoriasSaida.size() - 1));
            lancamentos.add(new LancamentoCaixa(
                    hoje.minusDays(indice * 9L + 5L),
                    TipoLancamento.SAIDA,
                    NaturezaLancamento.MONETARIA,
                    categoria,
                    DESCRICOES_SAIDA[indice],
                    new BigDecimal(VALORES_SAIDA[indice]),
                    null,
                    indice % 8 == 0 ? "Despesa fictícia usada na apresentação." : null));
        }
        return lancamentoRepository.saveAll(lancamentos);
    }

    private void criarHistoricoFinanceiro(
            List<CategoriaFinanceira> categoriasEntrada,
            List<CategoriaFinanceira> categoriasSaida,
            List<LancamentoCaixa> lancamentos) {
        CategoriaFinanceira primeiraCategoria = categoriasEntrada.getFirst();
        CategoriaFinanceira categoriaInativa = categoriasSaida.getLast();
        LancamentoCaixa primeiraEntrada = lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == TipoLancamento.ENTRADA)
                .findFirst()
                .orElseThrow();
        LancamentoCaixa primeiraSaida = lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == TipoLancamento.SAIDA)
                .findFirst()
                .orElseThrow();
        LancamentoCaixa primeiraDoacao = lancamentos.stream()
                .filter(lancamento -> !lancamento.isMonetario())
                .findFirst()
                .orElseThrow();
        LancamentoCaixa lancamentoEditado = lancamentos.getLast();

        historicoRepository.saveAll(List.of(
                historico(
                        ModuloHistorico.CATEGORIAS, "CategoriaFinanceira", primeiraCategoria.getId(),
                        primeiraCategoria.getNome(), AcaoHistorico.CRIACAO,
                        "Categoria financeira demonstrativa cadastrada."),
                historico(
                        ModuloHistorico.CATEGORIAS, "CategoriaFinanceira", categoriaInativa.getId(),
                        categoriaInativa.getNome(), AcaoHistorico.DESATIVACAO,
                        "Categoria demonstrativa desativada; lançamentos anteriores foram mantidos."),
                historico(
                        ModuloHistorico.LIVRO_CAIXA, "LancamentoCaixa", primeiraEntrada.getId(),
                        primeiraEntrada.getDescricao(), AcaoHistorico.REGISTRO,
                        "Entrada demonstrativa registrada no Livro Caixa."),
                historico(
                        ModuloHistorico.LIVRO_CAIXA, "LancamentoCaixa", primeiraSaida.getId(),
                        primeiraSaida.getDescricao(), AcaoHistorico.REGISTRO,
                        "Saída demonstrativa registrada no Livro Caixa."),
                historico(
                        ModuloHistorico.LIVRO_CAIXA, "LancamentoCaixa", primeiraDoacao.getId(),
                        primeiraDoacao.getDescricao(), AcaoHistorico.REGISTRO,
                        "Doação de material registrada sem valor monetário, apenas com comprovante."),
                new HistoricoOperacao(
                        ModuloHistorico.LIVRO_CAIXA,
                        "LancamentoCaixa",
                        lancamentoEditado.getId(),
                        lancamentoEditado.getDescricao(),
                        AcaoHistorico.EDICAO,
                        "Exemplo de lançamento demonstrativo atualizado.",
                        "Valor anterior fictício: R$ 90,00",
                        "Valor atualizado: R$ " + lancamentoEditado.getValor().toPlainString().replace('.', ','))));
    }

    private HistoricoOperacao historico(
            ModuloHistorico modulo,
            String tipoEntidade,
            Long entidadeId,
            String nomeEntidade,
            AcaoHistorico acao,
            String descricao) {
        return new HistoricoOperacao(
                modulo, tipoEntidade, entidadeId, nomeEntidade, acao, descricao, null, null);
    }

    private String nomeEncontro(Encontro encontro) {
        return encontro.getAtividade().getNome() + " — " + encontro.getData();
    }
}
