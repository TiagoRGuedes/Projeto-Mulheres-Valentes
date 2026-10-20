package com.ibmec.mulheresvalentes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Foto ou PDF do comprovante de um lançamento do Livro Caixa (opcional).
 * O arquivo fica em disco (app.comprovantes.diretorio); aqui ficam só os dados dele.
 */
@Entity
@Table(name = "comprovantes_arquivo")
public class ComprovanteArquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lancamento_id", nullable = false, unique = true)
    private LancamentoCaixa lancamento;

    /** Nome gerado pelo sistema (aleatório), usado no disco. */
    @Column(name = "nome_guardado", nullable = false, length = 80)
    private String nomeGuardado;

    @Column(name = "nome_original", nullable = false, length = 200)
    private String nomeOriginal;

    @Column(name = "tipo_conteudo", nullable = false, length = 50)
    private String tipoConteudo;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    @Column(name = "enviado_por_nome", length = 150)
    private String enviadoPorNome;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm;

    protected ComprovanteArquivo() {
    }

    public ComprovanteArquivo(LancamentoCaixa lancamento) {
        this.lancamento = lancamento;
    }

    public void substituir(String nomeGuardado, String nomeOriginal, String tipoConteudo, long tamanhoBytes, String enviadoPorNome) {
        this.nomeGuardado = nomeGuardado;
        this.nomeOriginal = nomeOriginal;
        this.tipoConteudo = tipoConteudo;
        this.tamanhoBytes = tamanhoBytes;
        this.enviadoPorNome = enviadoPorNome;
        this.enviadoEm = LocalDateTime.now();
    }

    @PrePersist
    void aoCriar() {
        if (enviadoEm == null) {
            enviadoEm = LocalDateTime.now();
        }
    }

    public boolean isImagem() {
        return tipoConteudo != null && tipoConteudo.startsWith("image/");
    }

    public String getTamanhoLegivel() {
        if (tamanhoBytes < 1024 * 1024) {
            return Math.max(1, Math.round(tamanhoBytes / 1024.0)) + " KB";
        }
        return String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.1f MB", tamanhoBytes / (1024.0 * 1024.0));
    }

    public Long getId() {
        return id;
    }

    public LancamentoCaixa getLancamento() {
        return lancamento;
    }

    public String getNomeGuardado() {
        return nomeGuardado;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public String getTipoConteudo() {
        return tipoConteudo;
    }

    public long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public String getEnviadoPorNome() {
        return enviadoPorNome;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }
}
