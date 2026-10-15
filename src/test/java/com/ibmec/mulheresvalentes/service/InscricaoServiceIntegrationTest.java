package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.InscricaoForm;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AreaApoio;
import com.ibmec.mulheresvalentes.model.Inscricao;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.PreferenciaContato;
import com.ibmec.mulheresvalentes.model.StatusInscricao;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class InscricaoServiceIntegrationTest {

    @Autowired
    private InscricaoService inscricaoService;

    @Autowired
    private ParticipanteService participanteService;

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private HistoricoOperacaoService historicoService;

    @Test
    void deveReceberInscricaoComoNovaEContar() {
        long antes = inscricaoService.contarNovas();

        Inscricao inscricao = inscricaoService.receber(formulario("  Maria   Teste ", "(11) 98888-1111"));

        assertThat(inscricao.getStatus()).isEqualTo(StatusInscricao.NOVA);
        assertThat(inscricao.getNome()).isEqualTo("Maria Teste");
        assertThat(inscricao.getAreas()).containsExactlyInAnyOrder(AreaApoio.ACOLHIMENTO, AreaApoio.CULTURA);
        assertThat(inscricaoService.contarNovas()).isEqualTo(antes + 1);
        assertThat(historicoService.listarPorEntidade("Inscricao", inscricao.getId())).isNotEmpty();
    }

    @Test
    void deveRecusarInscricaoSemConsentimento() {
        InscricaoForm form = formulario("Maria", "(11) 98888-2222");
        form.setConsentimento(false);

        assertThatThrownBy(() -> inscricaoService.receber(form))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("autoriza");
    }

    @Test
    void aceitarDeveCriarParticipanteComNomeEContato() {
        Inscricao inscricao = inscricaoService.receber(formulario("Joana Nova", "(11) 97777-3333"));
        long participantesAntes = participanteRepository.count();

        Inscricao aceita = inscricaoService.aceitar(inscricao.getId());

        assertThat(aceita.getStatus()).isEqualTo(StatusInscricao.ACEITA);
        assertThat(participanteRepository.count()).isEqualTo(participantesAntes + 1);
        Participante participante = aceita.getParticipante();
        assertThat(participante.getNome()).isEqualTo("Joana Nova");
        assertThat(participante.getContato()).isEqualTo("(11) 97777-3333");
        assertThat(participante.isAtivo()).isTrue();
    }

    @Test
    void aceitarDeveVincularParticipanteExistenteSemDuplicarEReativar() {
        Participante existente = participanteService.cadastrar("Ana Antiga", "11 96666-4444", null);
        participanteService.desativar(existente.getId());
        Inscricao inscricao = inscricaoService.receber(formulario("Ana", "(11) 96666-4444"));
        long participantesAntes = participanteRepository.count();

        Inscricao aceita = inscricaoService.aceitar(inscricao.getId());

        assertThat(participanteRepository.count()).isEqualTo(participantesAntes);
        assertThat(aceita.getParticipante().getId()).isEqualTo(existente.getId());
        assertThat(participanteService.buscar(existente.getId()).isAtivo()).isTrue();
    }

    @Test
    void naoDevePermitirAceitarDuasVezesNemDepoisDeArquivar() {
        Inscricao primeira = inscricaoService.receber(formulario("Bia", "(11) 95555-5555"));
        inscricaoService.aceitar(primeira.getId());
        Inscricao segunda = inscricaoService.receber(formulario("Cida", "(11) 94444-6666"));
        inscricaoService.arquivar(segunda.getId());

        assertThatThrownBy(() -> inscricaoService.aceitar(primeira.getId()))
                .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> inscricaoService.aceitar(segunda.getId()))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void marcarEmContatoSoValeParaInscricaoNova() {
        Inscricao inscricao = inscricaoService.receber(formulario("Dora", "(11) 93333-7777"));

        assertThat(inscricaoService.marcarEmContato(inscricao.getId()).getStatus())
                .isEqualTo(StatusInscricao.EM_CONTATO);
        assertThatThrownBy(() -> inscricaoService.marcarEmContato(inscricao.getId()))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void normalizacaoDeContatoIgnoraFormatacao() {
        assertThat(InscricaoService.normalizarContato("(11) 9 8888-1111"))
                .isEqualTo(InscricaoService.normalizarContato("11988881111"));
        assertThat(InscricaoService.normalizarContato(" Maria@Exemplo.COM "))
                .isEqualTo("maria@exemplo.com");
    }

    @Test
    void deveAceitarTelefonesComDddEmQualquerFormato() {
        assertThat(InscricaoService.problemaNoContato("(11) 91234-5678")).isEmpty();
        assertThat(InscricaoService.problemaNoContato("11912345678")).isEmpty();
        assertThat(InscricaoService.problemaNoContato("+55 11 91234-5678")).isEmpty();
        assertThat(InscricaoService.problemaNoContato("(11) 3456-7890")).isEmpty();
        assertThat(InscricaoService.problemaNoContato("maria@exemplo.com")).isEmpty();
    }

    @Test
    void deveApontarContatosQueAEquipeNaoConseguiriaUsar() {
        assertThat(InscricaoService.problemaNoContato("91234-5678")).get().asString().contains("DDD");
        assertThat(InscricaoService.problemaNoContato("(11) 9123-456")).get().asString().contains("faltar");
        assertThat(InscricaoService.problemaNoContato("(11) 91234-56789")).get().asString().contains("a mais ou a menos");
        assertThat(InscricaoService.problemaNoContato("maria@exemplo")).get().asString().contains("incompleto");
        assertThat(InscricaoService.problemaNoContato("ligar pra vizinha")).get().asString().contains("só com números");
    }

    @Test
    void deveApontarPreferenciaIncompativelComOContato() {
        assertThat(InscricaoService.problemaNaPreferencia("(11) 91234-5678", PreferenciaContato.EMAIL)).isPresent();
        assertThat(InscricaoService.problemaNaPreferencia("maria@exemplo.com", PreferenciaContato.WHATSAPP)).isPresent();
        assertThat(InscricaoService.problemaNaPreferencia("(11) 91234-5678", PreferenciaContato.LIGACAO)).isEmpty();
    }

    @Test
    void deveGuardarOContatoNumFormatoUnico() {
        Inscricao inscricao = inscricaoService.receber(formulario("Elza", "+55 11 912345678"));

        assertThat(inscricao.getContato()).isEqualTo("(11) 91234-5678");
    }

    @Test
    void deveRecusarTelefoneSemDddMesmoForaDaTela() {
        assertThatThrownBy(() -> inscricaoService.receber(formulario("Fátima", "91234-5678")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("DDD");
    }

    private InscricaoForm formulario(String nome, String contato) {
        InscricaoForm form = new InscricaoForm();
        form.setNome(nome);
        form.setContato(contato);
        form.setPreferenciaContato(PreferenciaContato.WHATSAPP);
        form.setAreas(Set.of(AreaApoio.ACOLHIMENTO, AreaApoio.CULTURA));
        form.setConsentimento(true);
        return form;
    }
}
