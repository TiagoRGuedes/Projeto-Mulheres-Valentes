package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.AtividadeForm;
import com.ibmec.mulheresvalentes.dto.CategoriaFinanceiraForm;
import com.ibmec.mulheresvalentes.dto.ComparacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.CorrecaoPresencaForm;
import com.ibmec.mulheresvalentes.dto.EncontroForm;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.dto.LinhaRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.ParticipanteForm;
import com.ibmec.mulheresvalentes.dto.RegistroPresencaForm;
import com.ibmec.mulheresvalentes.dto.ResultadoOperacaoDTO;
import com.ibmec.mulheresvalentes.dto.RevisaoOperacaoViewDTO;
import com.ibmec.mulheresvalentes.dto.RevisaoPreparadaDTO;
import com.ibmec.mulheresvalentes.dto.ResumoRegistroPresencaDto;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.NaturezaLancamento;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RevisaoOperacaoService {

    private final ConfirmacaoOperacaoService confirmacaoService;
    private final ParticipanteService participanteService;
    private final AtividadeService atividadeService;
    private final EncontroService encontroService;
    private final PresencaService presencaService;
    private final CategoriaFinanceiraService categoriaService;
    private final LancamentoCaixaService lancamentoService;
    private final FormatadorService formatador;

    public RevisaoOperacaoService(
            ConfirmacaoOperacaoService confirmacaoService,
            ParticipanteService participanteService,
            AtividadeService atividadeService,
            EncontroService encontroService,
            PresencaService presencaService,
            CategoriaFinanceiraService categoriaService,
            LancamentoCaixaService lancamentoService,
            FormatadorService formatador) {
        this.confirmacaoService = confirmacaoService;
        this.participanteService = participanteService;
        this.atividadeService = atividadeService;
        this.encontroService = encontroService;
        this.presencaService = presencaService;
        this.categoriaService = categoriaService;
        this.lancamentoService = lancamentoService;
        this.formatador = formatador;
    }

    public RevisaoPreparadaDTO preparar(HttpSession sessao, OperacaoRevisaoDTO operacao) {
        validarOperacao(operacao);
        RevisaoOperacaoViewDTO visual = montarVisual(operacao);
        String token = confirmacaoService.gerarToken(
                sessao, chave(operacao), operacao);
        return new RevisaoPreparadaDTO(token, visual);
    }

    public OperacaoRevisaoDTO obterSemConsumir(HttpSession sessao, String token, String chave) {
        return confirmacaoService.obterPayloadSemConsumir(
                token, sessao, chave, OperacaoRevisaoDTO.class);
    }

    public OperacaoRevisaoDTO cancelarRevisao(HttpSession sessao, String token, String chave) {
        return confirmacaoService.consumirToken(
                token, sessao, chave, OperacaoRevisaoDTO.class);
    }

    public ResultadoOperacaoDTO confirmar(HttpSession sessao, String token, String chave) {
        OperacaoRevisaoDTO operacao = confirmacaoService.consumirToken(
                token, sessao, chave, OperacaoRevisaoDTO.class);
        return executar(operacao);
    }

    public String chave(OperacaoRevisaoDTO operacao) {
        validarOperacao(operacao);
        return operacao.tipo().name() + (operacao.entidadeId() == null ? "" : ":" + operacao.entidadeId());
    }

    private ResultadoOperacaoDTO executar(OperacaoRevisaoDTO operacao) {
        Long id = operacao.entidadeId();
        return switch (operacao.tipo()) {
            case CRIAR_PARTICIPANTE -> criarParticipante(dados(operacao, ParticipanteForm.class));
            case EDITAR_PARTICIPANTE -> editarParticipante(id, dados(operacao, ParticipanteForm.class));
            case ATIVAR_PARTICIPANTE -> {
                participanteService.ativar(id);
                yield new ResultadoOperacaoDTO("/participantes/" + id, "Participante ativado com sucesso.");
            }
            case DESATIVAR_PARTICIPANTE -> {
                participanteService.desativar(id);
                yield new ResultadoOperacaoDTO("/participantes/" + id,
                        "Participante desativado. O histórico de presença foi mantido.");
            }
            case EXCLUIR_PARTICIPANTE -> {
                participanteService.excluir(id);
                yield new ResultadoOperacaoDTO("/participantes",
                        "Participante excluído porque não possuía registros de presença.");
            }
            case CRIAR_ATIVIDADE -> criarAtividade(dados(operacao, AtividadeForm.class));
            case EDITAR_ATIVIDADE -> editarAtividade(id, dados(operacao, AtividadeForm.class));
            case ATIVAR_ATIVIDADE -> {
                atividadeService.ativar(id);
                yield new ResultadoOperacaoDTO("/atividades/" + id, "Atividade ativada com sucesso.");
            }
            case DESATIVAR_ATIVIDADE -> {
                atividadeService.desativar(id);
                yield new ResultadoOperacaoDTO("/atividades/" + id,
                        "Atividade desativada. Encontros e presenças anteriores foram mantidos.");
            }
            case EXCLUIR_ATIVIDADE -> {
                atividadeService.excluir(id);
                yield new ResultadoOperacaoDTO("/atividades",
                        "Atividade excluída porque não possuía encontros.");
            }
            case CRIAR_ENCONTRO -> criarEncontro(dados(operacao, EncontroForm.class));
            case EDITAR_ENCONTRO -> editarEncontro(id, dados(operacao, EncontroForm.class));
            case EXCLUIR_ENCONTRO -> {
                encontroService.excluir(id);
                yield new ResultadoOperacaoDTO("/encontros",
                        "Encontro excluído porque não possuía registros de presença.");
            }
            case REGISTRAR_PRESENCAS -> registrarPresencas(dados(operacao, RegistroPresencaForm.class));
            case CORRIGIR_PRESENCA -> corrigirPresenca(id, dados(operacao, CorrecaoPresencaForm.class));
            case CRIAR_CATEGORIA -> {
                categoriaService.cadastrar(dados(operacao, CategoriaFinanceiraForm.class));
                yield new ResultadoOperacaoDTO("/caixa/categorias", "Categoria criada com sucesso.");
            }
            case EDITAR_CATEGORIA -> {
                categoriaService.editar(id, dados(operacao, CategoriaFinanceiraForm.class));
                yield new ResultadoOperacaoDTO("/caixa/categorias", "Categoria atualizada com sucesso.");
            }
            case ALTERNAR_CATEGORIA -> {
                CategoriaFinanceira categoria = categoriaService.alternarStatus(id);
                yield new ResultadoOperacaoDTO("/caixa/categorias",
                        "Categoria " + (categoria.isAtivo() ? "ativada" : "desativada") + " com sucesso.");
            }
            case REGISTRAR_ENTRADA -> registrarLancamento(dados(operacao, LancamentoCaixaForm.class), true);
            case REGISTRAR_SAIDA -> registrarLancamento(dados(operacao, LancamentoCaixaForm.class), false);
            case EDITAR_LANCAMENTO -> {
                lancamentoService.atualizar(id, dados(operacao, LancamentoCaixaForm.class));
                yield new ResultadoOperacaoDTO("/caixa/movimentacoes/" + id,
                        "Lançamento atualizado com sucesso.");
            }
            case CANCELAR_LANCAMENTO -> {
                lancamentoService.excluir(id);
                yield new ResultadoOperacaoDTO("/caixa/movimentacoes",
                        "Lançamento cancelado. O saldo e os relatórios foram atualizados; a rastreabilidade foi preservada.");
            }
        };
    }

    private ResultadoOperacaoDTO criarParticipante(ParticipanteForm form) {
        Participante participante = participanteService.cadastrar(
                form.getNome(), form.getContato(), form.getObservacoes());
        return new ResultadoOperacaoDTO(
                "/participantes/" + participante.getId(),
                "Participante cadastrado com sucesso.",
                Map.of("participanteNovo", true));
    }

    private ResultadoOperacaoDTO editarParticipante(Long id, ParticipanteForm form) {
        participanteService.atualizar(id, form.getNome(), form.getContato(), form.getObservacoes());
        return new ResultadoOperacaoDTO("/participantes/" + id,
                "Dados do participante atualizados com sucesso.");
    }

    private ResultadoOperacaoDTO criarAtividade(AtividadeForm form) {
        Atividade atividade = atividadeService.cadastrar(form.getNome(), form.getDescricao());
        return new ResultadoOperacaoDTO("/atividades/" + atividade.getId(),
                "Atividade cadastrada com sucesso.");
    }

    private ResultadoOperacaoDTO editarAtividade(Long id, AtividadeForm form) {
        atividadeService.atualizar(id, form.getNome(), form.getDescricao());
        return new ResultadoOperacaoDTO("/atividades/" + id,
                "Atividade atualizada com sucesso.");
    }

    private ResultadoOperacaoDTO criarEncontro(EncontroForm form) {
        Encontro encontro = encontroService.cadastrar(
                form.getAtividadeId(), form.getData(), form.getObservacoes());
        return new ResultadoOperacaoDTO("/encontros/" + encontro.getId(),
                "Encontro cadastrado com sucesso.");
    }

    private ResultadoOperacaoDTO editarEncontro(Long id, EncontroForm form) {
        encontroService.atualizar(id, form.getAtividadeId(), form.getData(), form.getObservacoes());
        return new ResultadoOperacaoDTO("/encontros/" + id,
                "Encontro atualizado com sucesso.");
    }

    private ResultadoOperacaoDTO registrarPresencas(RegistroPresencaForm form) {
        ResumoRegistroPresencaDto resumo = presencaService.salvarRegistroColetivo(form);
        String mensagem = "Presenças confirmadas: " + resumo.presentes() + " presente(s), "
                + resumo.ausentes() + " ausente(s) e " + resumo.justificadas() + " justificada(s).";
        if (form.getEncontroId() == null) {
            return new ResultadoOperacaoDTO(
                    "/presencas?dataInicial=" + form.getData() + "&dataFinal=" + form.getData(),
                    mensagem);
        }
        Encontro encontro = encontroService.buscar(form.getEncontroId());
        return new ResultadoOperacaoDTO("/encontros/" + encontro.getId(), mensagem);
    }

    private ResultadoOperacaoDTO corrigirPresenca(Long id, CorrecaoPresencaForm form) {
        Presenca presenca = presencaService.buscar(id);
        Long participanteId = presenca.getParticipante().getId();
        presencaService.corrigir(id, form.getStatus(), form.getObservacoes());
        return new ResultadoOperacaoDTO("/presencas?participanteId=" + participanteId,
                "Presença corrigida. A alteração ficou registrada no Histórico do sistema.");
    }

    private ResultadoOperacaoDTO registrarLancamento(LancamentoCaixaForm form, boolean entrada) {
        LancamentoCaixa lancamento = entrada
                ? lancamentoService.registrarEntrada(form)
                : lancamentoService.registrarSaida(form);
        return new ResultadoOperacaoDTO(
                "/caixa/movimentacoes/" + lancamento.getId(),
                (entrada ? "Entrada" : "Saída") + " registrada com sucesso.",
                Map.of("lancamentoNovo", true));
    }

    private RevisaoOperacaoViewDTO montarVisual(OperacaoRevisaoDTO operacao) {
        return switch (operacao.tipo()) {
            case CRIAR_PARTICIPANTE -> visualParticipante(operacao, false);
            case EDITAR_PARTICIPANTE -> visualParticipante(operacao, true);
            case ATIVAR_PARTICIPANTE -> visualEstadoParticipante(operacao, true, false);
            case DESATIVAR_PARTICIPANTE -> visualEstadoParticipante(operacao, false, false);
            case EXCLUIR_PARTICIPANTE -> visualEstadoParticipante(operacao, false, true);
            case CRIAR_ATIVIDADE -> visualAtividade(operacao, false);
            case EDITAR_ATIVIDADE -> visualAtividade(operacao, true);
            case ATIVAR_ATIVIDADE -> visualEstadoAtividade(operacao, true, false);
            case DESATIVAR_ATIVIDADE -> visualEstadoAtividade(operacao, false, false);
            case EXCLUIR_ATIVIDADE -> visualEstadoAtividade(operacao, false, true);
            case CRIAR_ENCONTRO -> visualEncontro(operacao, false);
            case EDITAR_ENCONTRO -> visualEncontro(operacao, true);
            case EXCLUIR_ENCONTRO -> visualExcluirEncontro(operacao);
            case REGISTRAR_PRESENCAS -> visualPresencas(operacao);
            case CORRIGIR_PRESENCA -> visualCorrecaoPresenca(operacao);
            case CRIAR_CATEGORIA -> visualCategoria(operacao, false);
            case EDITAR_CATEGORIA -> visualCategoria(operacao, true);
            case ALTERNAR_CATEGORIA -> visualEstadoCategoria(operacao);
            case REGISTRAR_ENTRADA -> visualLancamento(operacao, false, TipoLancamento.ENTRADA);
            case REGISTRAR_SAIDA -> visualLancamento(operacao, false, TipoLancamento.SAIDA);
            case EDITAR_LANCAMENTO -> visualLancamento(operacao, true, null);
            case CANCELAR_LANCAMENTO -> visualCancelarLancamento(operacao);
        };
    }

    private RevisaoOperacaoViewDTO visualParticipante(OperacaoRevisaoDTO operacao, boolean edicao) {
        ParticipanteForm form = dados(operacao, ParticipanteForm.class);
        List<ComparacaoRevisaoDTO> comparacoes = new ArrayList<>();
        if (edicao) {
            Participante atual = participanteService.buscar(operacao.entidadeId());
            comparar(comparacoes, "Nome", atual.getNome(), form.getNome());
            comparar(comparacoes, "Contato", atual.getContato(), form.getContato());
            comparar(comparacoes, "Observações", atual.getObservacoes(), form.getObservacoes());
        }
        return view("Pessoas · Review Layer", edicao ? "Confirmar alteração do participante" : "Confirmar novo participante",
                edicao ? "Confira o que será alterado antes de atualizar o cadastro." : "Confira os dados antes de criar o cadastro.",
                "P", "neutro", edicao ? "Confirmar alteração" : "Confirmar cadastro",
                List.of(new LinhaRevisaoDTO("Nome", seguro(form.getNome()), true),
                        new LinhaRevisaoDTO("Contato", opcional(form.getContato())),
                        new LinhaRevisaoDTO("Observações", opcional(form.getObservacoes()))),
                comparacoes,
                List.of("O cadastro, a busca e as listas serão atualizados.",
                        "A operação aparecerá no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualEstadoParticipante(
            OperacaoRevisaoDTO operacao, boolean ativar, boolean excluir) {
        Participante participante = participanteService.buscar(operacao.entidadeId());
        if (excluir) {
            return view("Pessoas · ação protegida", "Excluir este participante?",
                    "A exclusão só será concluída se o cadastro nunca tiver sido usado em presenças.",
                    "!", "perigo", "Excluir participante",
                    List.of(new LinhaRevisaoDTO("Participante", participante.getNome(), true)), List.of(),
                    List.of("O cadastro será removido definitivamente.",
                            "A exclusão ficará registrada no Histórico do sistema."));
        }
        return view("Pessoas · situação", ativar ? "Ativar este participante?" : "Desativar este participante?",
                ativar ? "A pessoa voltará a aparecer nas novas listas de presença."
                        : "A pessoa deixará de aparecer nas novas listas, sem perder o histórico.",
                ativar ? "✓" : "—", ativar ? "positivo" : "atencao",
                ativar ? "Confirmar ativação" : "Confirmar desativação",
                List.of(new LinhaRevisaoDTO("Participante", participante.getNome(), true),
                        new LinhaRevisaoDTO("Situação atual", participante.isAtivo() ? "Ativo" : "Desativado")),
                List.of(), List.of("Presenças e relatórios anteriores serão preservados.",
                        "A mudança ficará registrada no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualAtividade(OperacaoRevisaoDTO operacao, boolean edicao) {
        AtividadeForm form = dados(operacao, AtividadeForm.class);
        List<ComparacaoRevisaoDTO> comparacoes = new ArrayList<>();
        if (edicao) {
            Atividade atual = atividadeService.buscar(operacao.entidadeId());
            comparar(comparacoes, "Nome", atual.getNome(), form.getNome());
            comparar(comparacoes, "Descrição", atual.getDescricao(), form.getDescricao());
        }
        return view("Atividades · Review Layer", edicao ? "Confirmar alteração da atividade" : "Confirmar nova atividade",
                "Revise as informações que conectarão atividade, encontros e presenças.",
                "A", "neutro", edicao ? "Confirmar alteração" : "Confirmar atividade",
                List.of(new LinhaRevisaoDTO("Nome", seguro(form.getNome()), true),
                        new LinhaRevisaoDTO("Descrição", opcional(form.getDescricao()))), comparacoes,
                List.of("A lista de atividades e a busca serão atualizadas.",
                        "A operação ficará registrada no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualEstadoAtividade(
            OperacaoRevisaoDTO operacao, boolean ativar, boolean excluir) {
        Atividade atividade = atividadeService.buscar(operacao.entidadeId());
        if (excluir) {
            return view("Atividades · ação protegida", "Excluir esta atividade?",
                    "A exclusão só é permitida quando ainda não existem encontros ligados a ela.",
                    "!", "perigo", "Excluir atividade",
                    List.of(new LinhaRevisaoDTO("Atividade", atividade.getNome(), true)), List.of(),
                    List.of("A atividade será removida definitivamente.",
                            "A exclusão ficará registrada no Histórico do sistema."));
        }
        return view("Atividades · situação", ativar ? "Ativar esta atividade?" : "Desativar esta atividade?",
                ativar ? "A atividade voltará a aceitar novos encontros."
                        : "Novos encontros deixarão de usar esta atividade.",
                ativar ? "✓" : "—", ativar ? "positivo" : "atencao",
                ativar ? "Confirmar ativação" : "Confirmar desativação",
                List.of(new LinhaRevisaoDTO("Atividade", atividade.getNome(), true)), List.of(),
                List.of("Encontros, presenças e relatórios anteriores serão preservados.",
                        "A mudança ficará registrada no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualEncontro(OperacaoRevisaoDTO operacao, boolean edicao) {
        EncontroForm form = dados(operacao, EncontroForm.class);
        Atividade atividade = atividadeService.buscar(form.getAtividadeId());
        List<ComparacaoRevisaoDTO> comparacoes = new ArrayList<>();
        if (edicao) {
            Encontro atual = encontroService.buscar(operacao.entidadeId());
            comparar(comparacoes, "Atividade", atual.getAtividade().getNome(), atividade.getNome());
            comparar(comparacoes, "Data", formatador.data(atual.getData()), formatador.data(form.getData()));
            comparar(comparacoes, "Observações", atual.getObservacoes(), form.getObservacoes());
        }
        return view("Encontros · Review Layer", edicao ? "Confirmar alteração do encontro" : "Confirmar novo encontro",
                "Confira a atividade e a data antes de disponibilizar a chamada.",
                "E", "neutro", edicao ? "Confirmar alteração" : "Confirmar encontro",
                List.of(new LinhaRevisaoDTO("Atividade", atividade.getNome(), true),
                        new LinhaRevisaoDTO("Data", formatador.data(form.getData()), true),
                        new LinhaRevisaoDTO("Observações", opcional(form.getObservacoes()))), comparacoes,
                List.of("O encontro aparecerá no dashboard e no registro de presença.",
                        "A operação ficará registrada no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualExcluirEncontro(OperacaoRevisaoDTO operacao) {
        Encontro encontro = encontroService.buscar(operacao.entidadeId());
        return view("Encontros · ação protegida", "Excluir este encontro?",
                "A exclusão só é permitida quando não existem presenças registradas.",
                "!", "perigo", "Excluir encontro",
                List.of(new LinhaRevisaoDTO("Atividade", encontro.getAtividade().getNome(), true),
                        new LinhaRevisaoDTO("Data", formatador.data(encontro.getData()), true)), List.of(),
                List.of("O encontro será removido definitivamente.",
                        "A exclusão ficará registrada no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualPresencas(OperacaoRevisaoDTO operacao) {
        RegistroPresencaForm form = dados(operacao, RegistroPresencaForm.class);
        Encontro encontro = form.getEncontroId() == null
                ? null
                : encontroService.buscar(form.getEncontroId());
        ResumoRegistroPresencaDto resumo = presencaService.resumirFormulario(form);
        return view("Presença · chamada coletiva", "Revisão da presença",
                "Confira os totais. Nada foi gravado ainda.", "✓", "positivo", "Confirmar presença",
                List.of(new LinhaRevisaoDTO("Atividade",
                                encontro == null ? "Presença geral (sem atividade)"
                                        : encontro.getAtividade().getNome(), true),
                        new LinhaRevisaoDTO("Data", formatador.data(
                                encontro == null ? form.getData() : encontro.getData())),
                        new LinhaRevisaoDTO("Participantes", Long.toString(resumo.total()), true),
                        new LinhaRevisaoDTO("Presentes", Long.toString(resumo.presentes())),
                        new LinhaRevisaoDTO("Ausentes", Long.toString(resumo.ausentes())),
                        new LinhaRevisaoDTO("Justificadas", Long.toString(resumo.justificadas()))), List.of(),
                List.of("A frequência, o encontro, o dashboard e os relatórios serão atualizados.",
                        "O registro aparecerá no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualCorrecaoPresenca(OperacaoRevisaoDTO operacao) {
        Presenca atual = presencaService.buscar(operacao.entidadeId());
        CorrecaoPresencaForm form = dados(operacao, CorrecaoPresencaForm.class);
        List<ComparacaoRevisaoDTO> comparacoes = new ArrayList<>();
        comparar(comparacoes, "Situação", atual.getStatus().getDescricao(),
                form.getStatus() == null ? null : form.getStatus().getDescricao());
        comparar(comparacoes, "Observações", atual.getObservacoes(), form.getObservacoes());
        return view("Presença · correção", "Confirmar correção da presença",
                "A frequência e os relatórios serão recalculados após a confirmação.",
                "✎", "atencao", "Confirmar correção",
                List.of(new LinhaRevisaoDTO("Participante", atual.getParticipante().getNome(), true),
                        new LinhaRevisaoDTO("Atividade", atual.getContexto()),
                        new LinhaRevisaoDTO("Data", formatador.data(atual.getData()))), comparacoes,
                List.of("A situação atual será substituída pela situação revisada.",
                        "O antes e depois ficarão registrados no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualCategoria(OperacaoRevisaoDTO operacao, boolean edicao) {
        CategoriaFinanceiraForm form = dados(operacao, CategoriaFinanceiraForm.class);
        List<ComparacaoRevisaoDTO> comparacoes = new ArrayList<>();
        if (edicao) {
            CategoriaFinanceira atual = categoriaService.buscarPorId(operacao.entidadeId());
            comparar(comparacoes, "Nome", atual.getNome(), form.getNome());
            comparar(comparacoes, "Tipo", atual.getTipo().getDescricao(),
                    form.getTipo() == null ? null : form.getTipo().getDescricao());
        }
        return view("Livro Caixa · categorias", edicao ? "Confirmar alteração da categoria" : "Confirmar nova categoria",
                "A categoria organiza de onde veio ou para onde foi o dinheiro.",
                "C", "neutro", edicao ? "Confirmar alteração" : "Confirmar categoria",
                List.of(new LinhaRevisaoDTO("Nome", seguro(form.getNome()), true),
                        new LinhaRevisaoDTO("Tipo", form.getTipo() == null ? "Não informado" : form.getTipo().getDescricao())),
                comparacoes, List.of("Categorias ativas aparecem imediatamente nos formulários compatíveis.",
                        "A operação ficará registrada no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualEstadoCategoria(OperacaoRevisaoDTO operacao) {
        CategoriaFinanceira categoria = categoriaService.buscarPorId(operacao.entidadeId());
        boolean ativar = !categoria.isAtivo();
        return view("Livro Caixa · categoria", ativar ? "Ativar esta categoria?" : "Desativar esta categoria?",
                ativar ? "Ela voltará a aparecer em novos lançamentos."
                        : "Ela deixará de aparecer em novos lançamentos, sem alterar os antigos.",
                ativar ? "✓" : "—", ativar ? "positivo" : "atencao",
                ativar ? "Confirmar ativação" : "Confirmar desativação",
                List.of(new LinhaRevisaoDTO("Categoria", categoria.getNome(), true),
                        new LinhaRevisaoDTO("Tipo", categoria.getTipo().getDescricao())), List.of(),
                List.of("Movimentações históricas continuarão exibindo esta categoria.",
                        "A mudança ficará registrada no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualLancamento(
            OperacaoRevisaoDTO operacao, boolean edicao, TipoLancamento tipoNovo) {
        LancamentoCaixaForm form = dados(operacao, LancamentoCaixaForm.class);
        LancamentoCaixa atual = edicao ? lancamentoService.buscarPorId(operacao.entidadeId()) : null;
        TipoLancamento tipo = edicao ? atual.getTipo() : tipoNovo;
        NaturezaLancamento natureza = form.getNatureza() == null
                ? NaturezaLancamento.MONETARIA : form.getNatureza();
        CategoriaFinanceira categoria = categoriaService.buscarPorId(form.getCategoriaId());
        BigDecimal saldoAtual = lancamentoService.calcularSaldoAtual();
        BigDecimal valorNovo = natureza.isMonetaria() ? form.getValor() : null;
        BigDecimal saldoDepois = saldoAtual;
        if (edicao) {
            saldoDepois = saldoDepois.subtract(efeito(atual.getTipo(), atual));
        }
        saldoDepois = saldoDepois.add(efeito(tipo, natureza, valorNovo));

        List<ComparacaoRevisaoDTO> comparacoes = new ArrayList<>();
        if (edicao) {
            comparar(comparacoes, "Data", formatador.data(atual.getData()), formatador.data(form.getData()));
            comparar(comparacoes, "Natureza", atual.getNatureza().getDescricao(), natureza.getDescricao());
            comparar(comparacoes, "Categoria", atual.getCategoria().getNome(), categoria.getNome());
            comparar(comparacoes, "Descrição", atual.getDescricao(), form.getDescricao());
            comparar(comparacoes, "Valor", descreverValor(atual.getNatureza(), atual.getValor()),
                    descreverValor(natureza, valorNovo));
            comparar(comparacoes, "Comprovante", atual.getComprovante(), form.getComprovante());
            comparar(comparacoes, "Observações", atual.getObservacoes(), form.getObservacoes());
        }
        String titulo = edicao ? "Confirmar alteração do lançamento"
                : tipo == TipoLancamento.ENTRADA ? "Confirmar entrada" : "Confirmar saída";
        List<LinhaRevisaoDTO> campos = new ArrayList<>(List.of(
                new LinhaRevisaoDTO("Tipo", tipo.getRotuloAcessivel()),
                new LinhaRevisaoDTO("Natureza", natureza.getDescricao()),
                new LinhaRevisaoDTO("Data", formatador.data(form.getData())),
                new LinhaRevisaoDTO("Categoria", categoria.getNome()),
                new LinhaRevisaoDTO("Descrição", seguro(form.getDescricao())),
                new LinhaRevisaoDTO("Valor", descreverValor(natureza, valorNovo), true)));
        if (!natureza.isMonetaria()) {
            campos.add(new LinhaRevisaoDTO("Comprovante", seguro(form.getComprovante()), true));
        }
        campos.add(new LinhaRevisaoDTO("Saldo atual", formatador.moeda(saldoAtual)));
        campos.add(new LinhaRevisaoDTO("Saldo após confirmação", formatador.moeda(saldoDepois), true));
        return view("Livro Caixa · Review Layer", titulo,
                natureza.isMonetaria()
                        ? "Confira os dados e entenda o impacto no saldo antes de confirmar."
                        : "Doações de material não têm valor e não alteram o saldo; o comprovante é o controle.",
                tipo == TipoLancamento.ENTRADA ? "↑" : "↓",
                tipo == TipoLancamento.ENTRADA ? "positivo" : "atencao",
                edicao ? "Confirmar alteração" : tipo == TipoLancamento.ENTRADA ? "Confirmar entrada" : "Confirmar saída",
                List.copyOf(campos), comparacoes,
                List.of("O saldo, o dashboard, as movimentações e o relatório serão atualizados.",
                        "A operação ficará registrada no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO visualCancelarLancamento(OperacaoRevisaoDTO operacao) {
        LancamentoCaixa lancamento = lancamentoService.buscarPorId(operacao.entidadeId());
        BigDecimal saldoAtual = lancamentoService.calcularSaldoAtual();
        BigDecimal saldoDepois = saldoAtual.subtract(efeito(lancamento.getTipo(), lancamento));
        return view("Livro Caixa · ação rastreável", "Cancelar este lançamento?",
                "O lançamento será retirado dos cálculos, mas continuará preservado para rastreabilidade.",
                "!", "perigo", "Confirmar cancelamento",
                List.of(new LinhaRevisaoDTO("Tipo", lancamento.getTipo().getRotuloAcessivel()),
                        new LinhaRevisaoDTO("Descrição", lancamento.getDescricao(), true),
                        new LinhaRevisaoDTO("Categoria", lancamento.getCategoria().getNome()),
                        new LinhaRevisaoDTO("Data", formatador.data(lancamento.getData())),
                        new LinhaRevisaoDTO("Valor",
                                descreverValor(lancamento.getNatureza(), lancamento.getValor()), true),
                        new LinhaRevisaoDTO("Saldo atual", formatador.moeda(saldoAtual)),
                        new LinhaRevisaoDTO("Saldo após cancelamento", formatador.moeda(saldoDepois), true)), List.of(),
                List.of("Deixará de compor o saldo e os relatórios ativos.",
                        "Continuará preservado no banco para rastreabilidade.",
                        "O cancelamento ficará registrado no Histórico do sistema."));
    }

    private RevisaoOperacaoViewDTO view(
            String contexto, String titulo, String descricao, String simbolo, String tom,
            String textoConfirmar, List<LinhaRevisaoDTO> campos,
            List<ComparacaoRevisaoDTO> comparacoes, List<String> impactos) {
        return new RevisaoOperacaoViewDTO(
                contexto, titulo, descricao, simbolo, tom, textoConfirmar,
                campos, comparacoes, impactos);
    }

    private void comparar(List<ComparacaoRevisaoDTO> comparacoes, String rotulo, String antes, String depois) {
        String anterior = opcional(antes);
        String novo = opcional(depois);
        if (!anterior.equals(novo)) {
            comparacoes.add(new ComparacaoRevisaoDTO(rotulo, anterior, novo));
        }
    }

    private BigDecimal efeito(TipoLancamento tipo, LancamentoCaixa lancamento) {
        return efeito(tipo, lancamento.getNatureza(), lancamento.getValor());
    }

    private BigDecimal efeito(TipoLancamento tipo, NaturezaLancamento natureza, BigDecimal valor) {
        if (!natureza.isMonetaria()) {
            return BigDecimal.ZERO;
        }
        if (valor == null) {
            throw new RegraNegocioException("Informe um valor maior que zero.");
        }
        return tipo == TipoLancamento.ENTRADA ? valor : valor.negate();
    }

    private String descreverValor(NaturezaLancamento natureza, BigDecimal valor) {
        return natureza.isMonetaria()
                ? formatador.moeda(valor)
                : "Sem valor monetário (doação de material)";
    }

    private <T> T dados(OperacaoRevisaoDTO operacao, Class<T> tipo) {
        if (!tipo.isInstance(operacao.dados())) {
            throw new RegraNegocioException("Os dados desta confirmação não são válidos. Volte e tente novamente.");
        }
        return tipo.cast(operacao.dados());
    }

    private void validarOperacao(OperacaoRevisaoDTO operacao) {
        if (operacao == null || operacao.tipo() == null) {
            throw new RegraNegocioException("Informe qual operação deseja revisar.");
        }
    }

    private String seguro(String valor) {
        return valor == null || valor.isBlank() ? "Não informado" : valor.trim();
    }

    private String opcional(String valor) {
        return valor == null || valor.isBlank() ? "Não informado" : valor.trim();
    }
}
