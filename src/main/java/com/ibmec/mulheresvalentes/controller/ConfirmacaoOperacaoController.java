package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.AtividadeForm;
import com.ibmec.mulheresvalentes.dto.CategoriaFinanceiraForm;
import com.ibmec.mulheresvalentes.dto.CorrecaoPresencaForm;
import com.ibmec.mulheresvalentes.dto.EncontroForm;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.ParticipanteForm;
import com.ibmec.mulheresvalentes.dto.RegistroPresencaForm;
import com.ibmec.mulheresvalentes.dto.ResultadoOperacaoDTO;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.service.AtividadeService;
import com.ibmec.mulheresvalentes.service.CategoriaFinanceiraService;
import com.ibmec.mulheresvalentes.service.EncontroService;
import com.ibmec.mulheresvalentes.service.LancamentoCaixaService;
import com.ibmec.mulheresvalentes.service.ParticipanteService;
import com.ibmec.mulheresvalentes.service.PresencaService;
import com.ibmec.mulheresvalentes.service.RevisaoOperacaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/confirmacoes")
public class ConfirmacaoOperacaoController {

    private final RevisaoOperacaoService revisaoService;
    private final ParticipanteService participanteService;
    private final AtividadeService atividadeService;
    private final EncontroService encontroService;
    private final PresencaService presencaService;
    private final CategoriaFinanceiraService categoriaService;
    private final LancamentoCaixaService lancamentoService;

    public ConfirmacaoOperacaoController(
            RevisaoOperacaoService revisaoService,
            ParticipanteService participanteService,
            AtividadeService atividadeService,
            EncontroService encontroService,
            PresencaService presencaService,
            CategoriaFinanceiraService categoriaService,
            LancamentoCaixaService lancamentoService) {
        this.revisaoService = revisaoService;
        this.participanteService = participanteService;
        this.atividadeService = atividadeService;
        this.encontroService = encontroService;
        this.presencaService = presencaService;
        this.categoriaService = categoriaService;
        this.lancamentoService = lancamentoService;
    }

    @PostMapping("/confirmar")
    public String confirmar(
            @RequestParam String token,
            @RequestParam String chave,
            HttpSession sessao,
            RedirectAttributes redirectAttributes) {
        ResultadoOperacaoDTO resultado = revisaoService.confirmar(sessao, token, chave);
        redirectAttributes.addFlashAttribute("mensagemSucesso", resultado.mensagem());
        resultado.atributosFlash().forEach(redirectAttributes::addFlashAttribute);
        return "redirect:" + resultado.destino();
    }

    @PostMapping("/voltar")
    public String voltar(
            @RequestParam String token,
            @RequestParam String chave,
            HttpSession sessao,
            Model model) {
        OperacaoRevisaoDTO operacao = revisaoService.cancelarRevisao(sessao, token, chave);
        return devolverFormulario(operacao, model);
    }

    private String devolverFormulario(OperacaoRevisaoDTO operacao, Model model) {
        Long id = operacao.entidadeId();
        return switch (operacao.tipo()) {
            case CRIAR_PARTICIPANTE -> formularioParticipante(
                    dados(operacao, ParticipanteForm.class), null, false, model);
            case EDITAR_PARTICIPANTE -> formularioParticipante(
                    dados(operacao, ParticipanteForm.class), id, true, model);
            case CRIAR_ATIVIDADE -> formularioAtividade(
                    dados(operacao, AtividadeForm.class), null, false, model);
            case EDITAR_ATIVIDADE -> formularioAtividade(
                    dados(operacao, AtividadeForm.class), id, true, model);
            case CRIAR_ENCONTRO -> formularioEncontro(
                    dados(operacao, EncontroForm.class), null, false, model);
            case EDITAR_ENCONTRO -> formularioEncontro(
                    dados(operacao, EncontroForm.class), id, true, model);
            case REGISTRAR_PRESENCAS -> formularioPresencas(
                    dados(operacao, RegistroPresencaForm.class), model);
            case CORRIGIR_PRESENCA -> formularioCorrecao(
                    id, dados(operacao, CorrecaoPresencaForm.class), model);
            case CRIAR_CATEGORIA -> formularioCategoria(
                    dados(operacao, CategoriaFinanceiraForm.class), null, model);
            case EDITAR_CATEGORIA -> formularioCategoria(
                    dados(operacao, CategoriaFinanceiraForm.class), id, model);
            case REGISTRAR_ENTRADA -> formularioLancamento(
                    dados(operacao, LancamentoCaixaForm.class), null, TipoLancamento.ENTRADA, model);
            case REGISTRAR_SAIDA -> formularioLancamento(
                    dados(operacao, LancamentoCaixaForm.class), null, TipoLancamento.SAIDA, model);
            case EDITAR_LANCAMENTO -> {
                LancamentoCaixa lancamento = lancamentoService.buscarPorId(id);
                yield formularioLancamento(
                        dados(operacao, LancamentoCaixaForm.class), id, lancamento.getTipo(), model);
            }
            case ATIVAR_PARTICIPANTE, DESATIVAR_PARTICIPANTE, EXCLUIR_PARTICIPANTE ->
                    "redirect:/participantes/" + id;
            case ATIVAR_ATIVIDADE, DESATIVAR_ATIVIDADE, EXCLUIR_ATIVIDADE ->
                    "redirect:/atividades/" + id;
            case EXCLUIR_ENCONTRO -> "redirect:/encontros/" + id;
            case ALTERNAR_CATEGORIA -> "redirect:/caixa/categorias";
            case CANCELAR_LANCAMENTO -> "redirect:/caixa/movimentacoes/" + id;
            case MARCAR_INSCRICAO_EM_CONTATO, ACEITAR_INSCRICAO, ARQUIVAR_INSCRICAO ->
                    "redirect:/inscricoes/" + id;
        };
    }

    private String formularioParticipante(
            ParticipanteForm form, Long id, boolean edicao, Model model) {
        model.addAttribute("participanteForm", form);
        model.addAttribute("participanteId", id);
        model.addAttribute("modoEdicao", edicao);
        if (edicao) {
            model.addAttribute("participante", participanteService.buscar(id));
        }
        return "participantes/formulario";
    }

    private String formularioAtividade(AtividadeForm form, Long id, boolean edicao, Model model) {
        model.addAttribute("atividadeForm", form);
        model.addAttribute("atividadeId", id);
        model.addAttribute("modoEdicao", edicao);
        if (edicao) {
            model.addAttribute("atividade", atividadeService.buscar(id));
        }
        return "atividades/formulario";
    }

    private String formularioEncontro(EncontroForm form, Long id, boolean edicao, Model model) {
        model.addAttribute("encontroForm", form);
        model.addAttribute("encontroId", id);
        model.addAttribute("modoEdicao", edicao);
        model.addAttribute("atividades", atividadeService.listarAtivas());
        return "atividades/encontro-formulario";
    }

    private String formularioPresencas(RegistroPresencaForm form, Model model) {
        Encontro encontro = encontroService.buscar(form.getEncontroId());
        model.addAttribute("registroPresencaForm", form);
        model.addAttribute("atividades", atividadeService.listarAtivas());
        model.addAttribute("encontros", encontroService.listarDaAtividade(encontro.getAtividade().getId()));
        model.addAttribute("atividadeId", encontro.getAtividade().getId());
        model.addAttribute("encontroSelecionado", encontro);
        model.addAttribute("statusPresenca", StatusPresenca.values());
        model.addAttribute("resumoRegistro", presencaService.resumirFormulario(form));
        model.addAttribute("busca", null);
        return "presencas/registrar";
    }

    private String formularioCorrecao(Long id, CorrecaoPresencaForm form, Model model) {
        Presenca presenca = presencaService.buscar(id);
        model.addAttribute("presenca", presenca);
        model.addAttribute("correcaoPresencaForm", form);
        model.addAttribute("statusPresenca", StatusPresenca.values());
        return "presencas/corrigir";
    }

    private String formularioCategoria(CategoriaFinanceiraForm form, Long id, Model model) {
        model.addAttribute("categoriaForm", form);
        model.addAttribute("editandoId", id);
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("usosCategorias", categoriaService.resumirUsos());
        model.addAttribute("paginaAtual", "caixa");
        return "caixa/categorias";
    }

    private String formularioLancamento(
            LancamentoCaixaForm form, Long id, TipoLancamento tipo, Model model) {
        form.setTipo(tipo);
        CategoriaFinanceira categoriaAtual = id == null
                ? null : lancamentoService.buscarPorId(id).getCategoria();
        List<CategoriaFinanceira> categorias = new ArrayList<>(categoriaService.listarAtivas(tipo));
        if (categoriaAtual != null
                && categorias.stream().noneMatch(categoria -> categoria.getId().equals(categoriaAtual.getId()))) {
            categorias.add(categoriaAtual);
        }
        model.addAttribute("lancamentoForm", form);
        model.addAttribute("lancamentoId", id);
        model.addAttribute("tipoLancamento", tipo);
        model.addAttribute("categorias", categorias);
        model.addAttribute("edicao", id != null);
        model.addAttribute("paginaAtual", "caixa");
        if (id != null) {
            return "caixa/editar";
        }
        return tipo == TipoLancamento.ENTRADA ? "caixa/entrada" : "caixa/saida";
    }

    private <T> T dados(OperacaoRevisaoDTO operacao, Class<T> tipo) {
        return tipo.cast(operacao.dados());
    }
}
