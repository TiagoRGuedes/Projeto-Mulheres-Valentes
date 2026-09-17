package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.dto.ResumoAtividadesDTO;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.repository.AtividadeRepository;
import com.ibmec.mulheresvalentes.repository.EncontroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final EncontroRepository encontroRepository;
    private final HistoricoOperacaoService historicoService;

    public AtividadeService(
            AtividadeRepository atividadeRepository,
            EncontroRepository encontroRepository,
            HistoricoOperacaoService historicoService) {
        this.atividadeRepository = atividadeRepository;
        this.encontroRepository = encontroRepository;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public List<Atividade> listar(String busca) {
        if (busca == null || busca.isBlank()) {
            return atividadeRepository.findAllByOrderByNomeAsc();
        }
        return atividadeRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(busca.trim());
    }

    @Transactional(readOnly = true)
    public List<Atividade> listarAtivas() {
        return atividadeRepository.findByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Atividade buscar(Long id) {
        if (id == null) {
            throw new RecursoNaoEncontradoException("Atividade não encontrada.");
        }
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada."));
    }

    @Transactional
    public Atividade cadastrar(String nome, String descricao) {
        Atividade atividade = new Atividade(validarNome(nome), limparTextoOpcional(descricao));
        Atividade salva = atividadeRepository.save(atividade);
        historicoService.registrar(
                ModuloHistorico.ATIVIDADES, "Atividade", salva.getId(), salva.getNome(),
                AcaoHistorico.CRIACAO, "Atividade cadastrada.");
        return salva;
    }

    @Transactional
    public Atividade atualizar(Long id, String nome, String descricao) {
        Atividade atividade = buscar(id);
        String anterior = "Nome: " + atividade.getNome();
        atividade.atualizarDados(validarNome(nome), limparTextoOpcional(descricao));
        Atividade salva = atividadeRepository.save(atividade);
        historicoService.registrar(
                ModuloHistorico.ATIVIDADES, "Atividade", salva.getId(), salva.getNome(),
                AcaoHistorico.EDICAO, "Atividade atualizada.", anterior, "Nome: " + salva.getNome());
        return salva;
    }

    @Transactional
    public Atividade ativar(Long id) {
        Atividade atividade = buscar(id);
        if (atividade.isAtivo()) {
            return atividade;
        }
        atividade.ativar();
        Atividade salva = atividadeRepository.save(atividade);
        historicoService.registrar(
                ModuloHistorico.ATIVIDADES, "Atividade", salva.getId(), salva.getNome(),
                AcaoHistorico.ATIVACAO, "Atividade ativada para novos encontros.");
        return salva;
    }

    @Transactional
    public Atividade desativar(Long id) {
        Atividade atividade = buscar(id);
        if (!atividade.isAtivo()) {
            return atividade;
        }
        atividade.desativar();
        Atividade salva = atividadeRepository.save(atividade);
        historicoService.registrar(
                ModuloHistorico.ATIVIDADES, "Atividade", salva.getId(), salva.getNome(),
                AcaoHistorico.DESATIVACAO,
                "Atividade desativada. Encontros e presenças anteriores foram mantidos.");
        return salva;
    }

    @Transactional
    public void excluir(Long id) {
        Atividade atividade = buscar(id);
        if (encontroRepository.existsByAtividadeId(id)) {
            throw new RegraNegocioException(
                    "Esta atividade possui encontros e não pode ser excluída. Você pode desativá-la.");
        }
        historicoService.registrar(
                ModuloHistorico.ATIVIDADES, "Atividade", atividade.getId(), atividade.getNome(),
                AcaoHistorico.EXCLUSAO, "Atividade excluída por não possuir encontros.");
        atividadeRepository.delete(atividade);
    }

    @Transactional(readOnly = true)
    public boolean podeExcluir(Long id) {
        buscar(id);
        return !encontroRepository.existsByAtividadeId(id);
    }

    @Transactional(readOnly = true)
    public ResumoAtividadesDTO resumir() {
        return new ResumoAtividadesDTO(
                atividadeRepository.count(),
                atividadeRepository.countByAtivoTrue(),
                atividadeRepository.countByAtivoFalse(),
                encontroRepository.count());
    }

    private String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Informe o nome da atividade.");
        }
        String nomeLimpo = nome.trim();
        if (nomeLimpo.length() > 150) {
            throw new RegraNegocioException("O nome deve ter no máximo 150 caracteres.");
        }
        return nomeLimpo;
    }

    private String limparTextoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
