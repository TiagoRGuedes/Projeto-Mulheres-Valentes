package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.HistoricoOperacao;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
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

/**
 * Cria um cenário amplo, inteiramente fictício, somente quando cada domínio
 * ainda está vazio. A gravação direta pelos repositórios evita que a carga
 * inicial gere uma operação administrativa para cada uma das centenas de
 * presenças.
 */
@Component
@ConditionalOnProperty(name = "app.dados-demonstracao", havingValue = "true")
public class DadosDemonstracaoConfig implements ApplicationRunner {

    private static final int QUANTIDADE_PARTICIPANTES = 36;
    private static final int QUANTIDADE_ENCONTROS = 24;
    private static final int QUANTIDADE_LANCAMENTOS = 50;

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

    /** Quantidade exata de presenças em 24 encontros para cada faixa. */
    private static final int[] PRESENCAS_POR_FAIXA = {23, 21, 19, 17, 14, 10};

    private static final String[] DESCRICOES_ENTRADA = {
        "Doação espontânea demonstrativa",
        "Contribuição para atividade",
        "Arrecadação de encontro solidário",
        "Apoio de parceria fictícia",
        "Doação para materiais",
        "Contribuição mensal demonstrativa",
        "Arrecadação de ação comunitária",
        "Apoio para oficina demonstrativa"
    };

    private static final String[] VALORES_ENTRADA = {
        "850.00", "420.00", "1260.50", "600.00", "315.75",
        "980.00", "540.00", "1475.00", "275.50", "720.00"
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
        criarHistoricoDePresenca(participantes, atividades, encontros, presencas);
    }

    private List<Participante> criarParticipantes() {
        List<Participante> participantes = new ArrayList<>(QUANTIDADE_PARTICIPANTES);
        for (int indice = 0; indice < QUANTIDADE_PARTICIPANTES; indice++) {
            Participante participante = new Participante(
                    NOMES_FICTICIOS[indice],
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
        List<LancamentoCaixa> lancamentos = new ArrayList<>(QUANTIDADE_LANCAMENTOS);
        int indiceEntrada = 0;
        int indiceSaida = 0;

        for (int indice = 0; indice < QUANTIDADE_LANCAMENTOS; indice++) {
            LocalDate data = hoje.minusDays((QUANTIDADE_LANCAMENTOS - 1L - indice) * 2L);
            boolean entrada = indice % 5 < 2;
            if (entrada) {
                CategoriaFinanceira categoria = categoriasEntrada.get(
                        indiceEntrada % (categoriasEntrada.size() - 1));
                lancamentos.add(new LancamentoCaixa(
                        data,
                        TipoLancamento.ENTRADA,
                        categoria,
                        DESCRICOES_ENTRADA[indiceEntrada % DESCRICOES_ENTRADA.length],
                        new BigDecimal(VALORES_ENTRADA[indiceEntrada % VALORES_ENTRADA.length]),
                        indiceEntrada % 7 == 0 ? "Valor inteiramente fictício para demonstração." : null));
                indiceEntrada++;
            } else {
                CategoriaFinanceira categoria = categoriasSaida.get(
                        indiceSaida % (categoriasSaida.size() - 1));
                lancamentos.add(new LancamentoCaixa(
                        data,
                        TipoLancamento.SAIDA,
                        categoria,
                        DESCRICOES_SAIDA[indiceSaida % DESCRICOES_SAIDA.length],
                        new BigDecimal(VALORES_SAIDA[indiceSaida % VALORES_SAIDA.length]),
                        indiceSaida % 8 == 0 ? "Despesa fictícia usada na apresentação." : null));
                indiceSaida++;
            }
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
