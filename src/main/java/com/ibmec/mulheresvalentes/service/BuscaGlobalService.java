package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.GrupoResultadoBuscaDTO;
import com.ibmec.mulheresvalentes.dto.ItemResultadoBuscaDTO;
import com.ibmec.mulheresvalentes.dto.ResultadoBuscaGlobalDTO;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.repository.AtividadeRepository;
import com.ibmec.mulheresvalentes.repository.CategoriaFinanceiraRepository;
import com.ibmec.mulheresvalentes.repository.EncontroRepository;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BuscaGlobalService {

    static final int LIMITE_POR_GRUPO = 6;

    private final ParticipanteRepository participanteRepository;
    private final AtividadeRepository atividadeRepository;
    private final EncontroRepository encontroRepository;
    private final LancamentoCaixaRepository lancamentoRepository;
    private final CategoriaFinanceiraRepository categoriaRepository;
    private final FormatadorService formatador;

    public BuscaGlobalService(
            ParticipanteRepository participanteRepository,
            AtividadeRepository atividadeRepository,
            EncontroRepository encontroRepository,
            LancamentoCaixaRepository lancamentoRepository,
            CategoriaFinanceiraRepository categoriaRepository,
            FormatadorService formatador) {
        this.participanteRepository = participanteRepository;
        this.atividadeRepository = atividadeRepository;
        this.encontroRepository = encontroRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository = categoriaRepository;
        this.formatador = formatador;
    }

    @Transactional(readOnly = true)
    public ResultadoBuscaGlobalDTO buscar(String termoInformado) {
        String termo = normalizar(termoInformado);
        if (termo.isBlank()) {
            return new ResultadoBuscaGlobalDTO(termo, List.of(), LIMITE_POR_GRUPO);
        }

        Pageable limite = PageRequest.of(0, LIMITE_POR_GRUPO);
        List<GrupoResultadoBuscaDTO> grupos = new ArrayList<>();

        adicionarGrupo(grupos, "participantes", "Participantes",
                participanteRepository.buscarGlobal(termo, limite).stream()
                        .map(this::mapearParticipante)
                        .toList());
        adicionarGrupo(grupos, "atividades", "Atividades",
                atividadeRepository.buscarGlobal(termo, limite).stream()
                        .map(this::mapearAtividade)
                        .toList());
        adicionarGrupo(grupos, "encontros", "Encontros",
                encontroRepository.buscarGlobal(termo, limite).stream()
                        .map(this::mapearEncontro)
                        .toList());
        adicionarGrupo(grupos, "financeiro", "Livro Caixa",
                lancamentoRepository.buscarGlobal(termo, limite).stream()
                        .map(this::mapearLancamento)
                        .toList());
        adicionarGrupo(grupos, "categorias", "Categorias financeiras",
                categoriaRepository.buscarGlobal(termo, limite).stream()
                        .map(this::mapearCategoria)
                        .toList());

        return new ResultadoBuscaGlobalDTO(termo, grupos, LIMITE_POR_GRUPO);
    }

    private ItemResultadoBuscaDTO mapearParticipante(Participante participante) {
        String situacao = participante.isAtivo() ? "Ativo" : "Desativado";
        String descricao = participante.isAtivo()
                ? "Disponível para novos registros de presença."
                : "O histórico foi preservado, mas o cadastro está desativado.";
        if (participante.getContato() != null) {
            descricao = "Contato: " + participante.getContato() + " · " + descricao;
        }
        return new ItemResultadoBuscaDTO(
                "Participante", participante.getNome(), descricao, situacao,
                "/participantes/" + participante.getId());
    }

    private ItemResultadoBuscaDTO mapearAtividade(Atividade atividade) {
        return new ItemResultadoBuscaDTO(
                "Atividade",
                atividade.getNome(),
                resumir(atividade.getDescricao(), "Atividade cadastrada no controle de presença."),
                atividade.isAtivo() ? "Ativa" : "Desativada",
                "/atividades/" + atividade.getId());
    }

    private ItemResultadoBuscaDTO mapearEncontro(Encontro encontro) {
        return new ItemResultadoBuscaDTO(
                "Encontro",
                encontro.getAtividade().getNome(),
                "Encontro em " + formatador.data(encontro.getData()) + ".",
                encontro.getAtividade().isAtivo() ? "Atividade ativa" : "Atividade desativada",
                "/encontros/" + encontro.getId());
    }

    private ItemResultadoBuscaDTO mapearLancamento(LancamentoCaixa lancamento) {
        return new ItemResultadoBuscaDTO(
                lancamento.getTipo().getDescricao(),
                lancamento.getDescricao(),
                lancamento.getCategoria().getNome()
                        + " · " + formatador.data(lancamento.getData())
                        + " · " + (lancamento.isMonetario()
                                ? formatador.moeda(lancamento.getValor())
                                : lancamento.getNatureza().getDescricao()),
                lancamento.getTipo().getRotuloAcessivel(),
                "/caixa/movimentacoes/" + lancamento.getId());
    }

    private ItemResultadoBuscaDTO mapearCategoria(CategoriaFinanceira categoria) {
        return new ItemResultadoBuscaDTO(
                "Categoria financeira",
                categoria.getNome(),
                "Categoria de " + categoria.getTipo().getDescricao().toLowerCase() + ".",
                categoria.isAtivo() ? "Ativa" : "Desativada",
                "/caixa/categorias");
    }

    private void adicionarGrupo(
            List<GrupoResultadoBuscaDTO> grupos,
            String chave,
            String titulo,
            List<ItemResultadoBuscaDTO> itens) {
        if (!itens.isEmpty()) {
            grupos.add(new GrupoResultadoBuscaDTO(chave, titulo, itens));
        }
    }

    private String normalizar(String termo) {
        return termo == null ? "" : termo.trim().replaceAll("\\s+", " ");
    }

    private String resumir(String texto, String valorPadrao) {
        if (texto == null || texto.isBlank()) {
            return valorPadrao;
        }
        String normalizado = texto.trim().replaceAll("\\s+", " ");
        return normalizado.length() <= 140
                ? normalizado
                : normalizado.substring(0, 137) + "…";
    }
}
