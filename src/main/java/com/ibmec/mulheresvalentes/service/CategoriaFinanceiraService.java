package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.CategoriaFinanceiraForm;
import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.repository.CategoriaFinanceiraRepository;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CategoriaFinanceiraService {

    private final CategoriaFinanceiraRepository categoriaRepository;
    private final LancamentoCaixaRepository lancamentoRepository;
    private final HistoricoOperacaoService historicoService;

    public CategoriaFinanceiraService(
            CategoriaFinanceiraRepository categoriaRepository,
            LancamentoCaixaRepository lancamentoRepository,
            HistoricoOperacaoService historicoService) {
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public List<CategoriaFinanceira> listarTodas() {
        return categoriaRepository.findAllByOrderByTipoAscNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<CategoriaFinanceira> listarAtivas() {
        return categoriaRepository.findByAtivoTrueOrderByTipoAscNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<CategoriaFinanceira> listarAtivas(TipoLancamento tipo) {
        if (tipo == null) {
            return listarAtivas();
        }
        return categoriaRepository.findByTipoAndAtivoTrueOrderByNomeAsc(tipo);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> resumirUsos() {
        Map<Long, Long> usos = new LinkedHashMap<>();
        for (CategoriaFinanceira categoria : listarTodas()) {
            usos.put(categoria.getId(),
                    lancamentoRepository.countByCategoriaIdAndCanceladoFalse(categoria.getId()));
        }
        return usos;
    }

    @Transactional(readOnly = true)
    public CategoriaFinanceira buscarPorId(Long id) {
        if (id == null) {
            throw new RegraNegocioException("Escolha uma categoria.");
        }
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria financeira não encontrada."));
    }

    @Transactional(readOnly = true)
    public CategoriaFinanceira buscarAtivaDoTipo(Long id, TipoLancamento tipo) {
        CategoriaFinanceira categoria = buscarPorId(id);
        if (!categoria.isAtivo()) {
            throw new RegraNegocioException("A categoria escolhida está desativada. Escolha outra categoria.");
        }
        validarTipo(categoria, tipo);
        return categoria;
    }

    @Transactional
    public CategoriaFinanceira cadastrar(CategoriaFinanceiraForm form) {
        if (form == null) {
            throw new RegraNegocioException("Informe os dados da categoria.");
        }
        return cadastrar(form.getNome(), form.getTipo());
    }

    @Transactional
    public CategoriaFinanceira cadastrar(String nome, TipoLancamento tipo) {
        String nomeNormalizado = validarENormalizarNome(nome);
        validarTipoInformado(tipo);
        categoriaRepository.findByNomeIgnoreCaseAndTipo(nomeNormalizado, tipo).ifPresent(existente -> {
            throw new RegraNegocioException("Já existe uma categoria com esse nome para "
                    + tipo.getDescricao().toLowerCase() + ".");
        });
        CategoriaFinanceira salva = categoriaRepository.save(new CategoriaFinanceira(nomeNormalizado, tipo));
        historicoService.registrar(
                ModuloHistorico.CATEGORIAS, "CategoriaFinanceira", salva.getId(), salva.getNome(),
                AcaoHistorico.CRIACAO,
                "Categoria de " + salva.getTipo().getDescricao().toLowerCase() + " criada.");
        return salva;
    }

    @Transactional
    public CategoriaFinanceira editar(Long id, CategoriaFinanceiraForm form) {
        if (form == null) {
            throw new RegraNegocioException("Informe os dados da categoria.");
        }
        CategoriaFinanceira categoria = buscarPorId(id);
        String anterior = resumo(categoria);
        String nomeNormalizado = validarENormalizarNome(form.getNome());
        validarTipoInformado(form.getTipo());

        if (categoriaRepository.existsByNomeIgnoreCaseAndTipoAndIdNot(
                nomeNormalizado, form.getTipo(), categoria.getId())) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome para "
                    + form.getTipo().getDescricao().toLowerCase() + ".");
        }
        if (categoria.getTipo() != form.getTipo()
                && lancamentoRepository.existsByCategoriaId(categoria.getId())) {
            throw new RegraNegocioException(
                    "Essa categoria já possui movimentações. Para preservar o histórico, "
                            + "o tipo não pode ser alterado.");
        }

        categoria.setNome(nomeNormalizado);
        categoria.setTipo(form.getTipo());
        CategoriaFinanceira salva = categoriaRepository.save(categoria);
        historicoService.registrar(
                ModuloHistorico.CATEGORIAS, "CategoriaFinanceira", salva.getId(), salva.getNome(),
                AcaoHistorico.EDICAO, "Categoria financeira atualizada.", anterior, resumo(salva));
        return salva;
    }

    @Transactional
    public CategoriaFinanceira alternarStatus(Long id) {
        CategoriaFinanceira categoria = buscarPorId(id);
        categoria.setAtivo(!categoria.isAtivo());
        CategoriaFinanceira salva = categoriaRepository.save(categoria);
        AcaoHistorico acao = salva.isAtivo() ? AcaoHistorico.ATIVACAO : AcaoHistorico.DESATIVACAO;
        String descricao = salva.isAtivo()
                ? "Categoria ativada para novos lançamentos."
                : "Categoria desativada. As movimentações anteriores foram mantidas.";
        historicoService.registrar(
                ModuloHistorico.CATEGORIAS, "CategoriaFinanceira", salva.getId(), salva.getNome(),
                acao, descricao);
        return salva;
    }

    private String resumo(CategoriaFinanceira categoria) {
        return "Nome: " + categoria.getNome() + "; tipo: " + categoria.getTipo().getDescricao();
    }

    private void validarTipo(CategoriaFinanceira categoria, TipoLancamento tipo) {
        validarTipoInformado(tipo);
        if (categoria.getTipo() != tipo) {
            throw new RegraNegocioException("Escolha uma categoria de "
                    + tipo.getDescricao().toLowerCase() + ".");
        }
    }

    private void validarTipoInformado(TipoLancamento tipo) {
        if (tipo == null) {
            throw new RegraNegocioException("Informe se a categoria é de entrada ou de saída.");
        }
    }

    private String validarENormalizarNome(String nome) {
        if (!StringUtils.hasText(nome)) {
            throw new RegraNegocioException("Informe o nome da categoria.");
        }
        String normalizado = nome.trim().replaceAll("\\s+", " ");
        if (normalizado.length() > 80) {
            throw new RegraNegocioException("O nome da categoria deve ter no máximo 80 caracteres.");
        }
        return normalizado;
    }
}
