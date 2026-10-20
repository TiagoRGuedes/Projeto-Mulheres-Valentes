package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.ComprovanteArquivo;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.repository.ComprovanteArquivoRepository;
import com.ibmec.mulheresvalentes.security.UsuarioAutenticado;
import com.ibmec.mulheresvalentes.security.UsuarioLogado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Foto do comprovante do Livro Caixa (opcional).
 *
 * <ul>
 *   <li>Aceita JPG, PNG, WEBP e PDF de até 5 MB.</li>
 *   <li>O tipo é conferido pelo conteúdo do arquivo, não só pela extensão.</li>
 *   <li>O arquivo é salvo com nome aleatório numa pasta fora do site público.</li>
 * </ul>
 */
@Service
public class ComprovanteArquivoService {

    public static final long TAMANHO_MAXIMO = 5L * 1024 * 1024;
    private static final Logger LOG = LoggerFactory.getLogger(ComprovanteArquivoService.class);

    private final ComprovanteArquivoRepository repository;
    private final LancamentoCaixaService lancamentoService;
    private final HistoricoOperacaoService historicoService;
    private final Path diretorio;

    public ComprovanteArquivoService(
            ComprovanteArquivoRepository repository,
            LancamentoCaixaService lancamentoService,
            HistoricoOperacaoService historicoService,
            @Value("${app.comprovantes.diretorio:dados/comprovantes}") String diretorio) {
        this.repository = repository;
        this.lancamentoService = lancamentoService;
        this.historicoService = historicoService;
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    /** Arquivo pronto para ser enviado ao navegador. */
    public record ArquivoParaEnvio(Path caminho, String tipoConteudo, String nomeOriginal, long tamanho) {
    }

    @Transactional(readOnly = true)
    public Optional<ComprovanteArquivo> buscar(Long lancamentoId) {
        return repository.findByLancamentoId(lancamentoId);
    }

    @Transactional
    public ComprovanteArquivo anexar(Long lancamentoId, MultipartFile arquivo) {
        LancamentoCaixa lancamento = lancamentoService.buscarPorIdIncluindoCancelados(lancamentoId);
        if (lancamento.isCancelado()) {
            throw new RegraNegocioException("Este lançamento está cancelado. Não é possível anexar comprovante.");
        }
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("Escolha uma foto ou um PDF do comprovante.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("O arquivo é maior que 5 MB. Tire a foto com qualidade menor ou envie um PDF menor.");
        }
        byte[] conteudo;
        try {
            conteudo = arquivo.getBytes();
        } catch (IOException ex) {
            throw new RegraNegocioException("Não foi possível ler o arquivo. Tente de novo.");
        }
        TipoArquivo tipo = TipoArquivo.detectar(conteudo)
                .orElseThrow(() -> new RegraNegocioException(
                        "Formato não aceito. Envie uma foto JPG, PNG ou WEBP, ou um arquivo PDF."));

        String nomeGuardado = UUID.randomUUID() + "." + tipo.extensao;
        Path destino = resolver(nomeGuardado);
        try {
            Files.createDirectories(diretorio);
            try (InputStream entrada = new java.io.ByteArrayInputStream(conteudo)) {
                Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            LOG.error("Falha ao gravar comprovante em {}", destino, ex);
            throw new RegraNegocioException("Não foi possível guardar o arquivo. Tente de novo.");
        }
        // Se a gravação no banco falhar, o arquivo novo é apagado.
        apagarAoDesfazer(destino);

        ComprovanteArquivo comprovante = repository.findByLancamentoId(lancamentoId)
                .orElseGet(() -> new ComprovanteArquivo(lancamento));
        String anterior = comprovante.getNomeGuardado();
        String nomeOriginal = nomeOriginal(arquivo.getOriginalFilename(), tipo);
        comprovante.substituir(nomeGuardado, nomeOriginal, tipo.tipoConteudo, conteudo.length,
                UsuarioLogado.atual().map(UsuarioAutenticado::getNome).orElse(null));
        ComprovanteArquivo salvo = repository.save(comprovante);
        if (anterior != null) {
            apagarDepoisDeConfirmar(resolver(anterior));
        }

        historicoService.registrar(
                ModuloHistorico.LIVRO_CAIXA, "LancamentoCaixa", lancamento.getId(), lancamento.getDescricao(),
                AcaoHistorico.EDICAO,
                (anterior == null ? "Foto do comprovante anexada" : "Foto do comprovante substituída")
                        + " (" + nomeOriginal + ", " + salvo.getTamanhoLegivel() + ").");
        return salvo;
    }

    @Transactional
    public void remover(Long lancamentoId) {
        ComprovanteArquivo comprovante = repository.findByLancamentoId(lancamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Este lançamento não tem comprovante anexado."));
        LancamentoCaixa lancamento = comprovante.getLancamento();
        Path arquivo = resolver(comprovante.getNomeGuardado());
        repository.delete(comprovante);
        apagarDepoisDeConfirmar(arquivo);
        historicoService.registrar(
                ModuloHistorico.LIVRO_CAIXA, "LancamentoCaixa", lancamento.getId(), lancamento.getDescricao(),
                AcaoHistorico.EXCLUSAO, "Foto do comprovante removida (" + comprovante.getNomeOriginal() + ").");
    }

    @Transactional(readOnly = true)
    public ArquivoParaEnvio abrir(Long lancamentoId) {
        ComprovanteArquivo comprovante = repository.findByLancamentoId(lancamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Este lançamento não tem comprovante anexado."));
        Path caminho = resolver(comprovante.getNomeGuardado());
        if (!Files.isRegularFile(caminho)) {
            throw new RecursoNaoEncontradoException(
                    "O arquivo do comprovante não foi encontrado na pasta do sistema. Anexe de novo.");
        }
        return new ArquivoParaEnvio(caminho, comprovante.getTipoConteudo(), comprovante.getNomeOriginal(),
                comprovante.getTamanhoBytes());
    }

    private Path resolver(String nomeGuardado) {
        Path caminho = diretorio.resolve(nomeGuardado).normalize();
        if (!caminho.startsWith(diretorio)) {
            throw new RegraNegocioException("Arquivo inválido.");
        }
        return caminho;
    }

    private static void apagarAoDesfazer(Path arquivo) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        apagarSilenciosamente(arquivo);
                    }
                }
            });
        }
    }

    private static void apagarDepoisDeConfirmar(Path arquivo) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    apagarSilenciosamente(arquivo);
                }
            });
        } else {
            apagarSilenciosamente(arquivo);
        }
    }

    private static void apagarSilenciosamente(Path arquivo) {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException ex) {
            LOG.warn("Não foi possível apagar o arquivo {}", arquivo, ex);
        }
    }

    private static String nomeOriginal(String informado, TipoArquivo tipo) {
        String nome = informado == null ? "" : Path.of(informado.replace('\\', '/')).getFileName().toString();
        nome = nome.replaceAll("[\\p{Cntrl}\"<>|]", "").trim();
        if (nome.isBlank()) {
            nome = "comprovante." + tipo.extensao;
        }
        return nome.length() > 200 ? nome.substring(nome.length() - 200) : nome;
    }

    /** Tipos aceitos, reconhecidos pelos primeiros bytes do arquivo. */
    enum TipoArquivo {
        JPG("image/jpeg", "jpg"),
        PNG("image/png", "png"),
        WEBP("image/webp", "webp"),
        PDF("application/pdf", "pdf");

        final String tipoConteudo;
        final String extensao;

        TipoArquivo(String tipoConteudo, String extensao) {
            this.tipoConteudo = tipoConteudo;
            this.extensao = extensao;
        }

        static Optional<TipoArquivo> detectar(byte[] b) {
            if (comeca(b, 0xFF, 0xD8, 0xFF)) {
                return Optional.of(JPG);
            }
            if (comeca(b, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)) {
                return Optional.of(PNG);
            }
            if (b.length >= 12 && comeca(b, 'R', 'I', 'F', 'F')
                    && Arrays.equals(Arrays.copyOfRange(b, 8, 12), new byte[] {'W', 'E', 'B', 'P'})) {
                return Optional.of(WEBP);
            }
            if (comeca(b, '%', 'P', 'D', 'F', '-')) {
                return Optional.of(PDF);
            }
            return Optional.empty();
        }

        private static boolean comeca(byte[] b, int... prefixo) {
            if (b.length < prefixo.length) {
                return false;
            }
            for (int i = 0; i < prefixo.length; i++) {
                if ((b[i] & 0xFF) != prefixo[i]) {
                    return false;
                }
            }
            return true;
        }
    }
}
