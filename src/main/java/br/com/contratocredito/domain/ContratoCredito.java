package br.com.contratocredito.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate root representing a credit contract.
 *
 * <p>Domain invariants:
 * <ul>
 *   <li>A contract is born via {@link #criar(...)} in an initial state.</li>
 *   <li>Blocking / cancellation only make sense from specific states
 *       (enforced here, not by the enum).</li>
 *   <li>Every meaningful change bumps {@code versao} and refreshes
 *       {@code dataUltimaAtualizacao} — this is what gave the original
 *       system contract traceability (versioned contracts).</li>
 * </ul>
 *
 * <p>This class is persistence-agnostic: no JPA, no Spring, no database
 * annotations. The infrastructure layer will map it to a DB model later
 * (the {@code versao} field maps naturally to JPA {@code @Version}).
 */
public class ContratoCredito {

	private final ContratoId id;
	private final String numeroContrato;
	private final Cliente cliente;
	private StatusContrato status;
	private final ValorMonetario limiteCredito;
	private final LocalDateTime dataAnaliseCredito;
	private final LocalDateTime dataCriacao;

	private String motivoBloqueio;
	private String motivoCancelamento;
	private LocalDateTime dataUltimaAtualizacao;
	private Long versao;

	private ContratoCredito(Builder b) {
		this.id = b.id;
		this.numeroContrato = b.numeroContrato;
		this.cliente = b.cliente;
		this.status = Objects.requireNonNull(b.status, "status inicial não pode ser nulo");
		this.limiteCredito = b.limiteCredito;
		this.dataAnaliseCredito = b.dataAnaliseCredito;
		this.dataCriacao = b.dataCriacao;
		this.versao = 0L;
		this.dataUltimaAtualizacao = b.dataCriacao;
	}

	/** Factory: creates a brand new contract in its initial state. */
	public static ContratoCredito criar(ContratoId id,
	                                     String numeroContrato,
	                                     Cliente cliente,
	                                     ValorMonetario limiteCredito,
	                                     LocalDateTime dataAnaliseCredito) {
		return builder()
				.id(id)
				.numeroContrato(numeroContrato)
				.cliente(cliente)
				.limiteCredito(limiteCredito)
				.dataAnaliseCredito(dataAnaliseCredito)
				.dataCriacao(LocalDateTime.now())
				.status(StatusContrato.RASCUNHO)
				.build();
	}

	// ---- State transitions (the contract "state machine") ----

	public void bloquear(String motivo) {
		if (this.status == StatusContrato.CANCELADO) {
			throw new IllegalStateException("Não é possível bloquear um contrato cancelado");
		}
		if (this.status == StatusContrato.BLOQUEADO) {
			throw new IllegalStateException("Contrato já está bloqueado");
		}
		this.motivoBloqueio = Objects.requireNonNull(motivo, "motivo do bloqueio é obrigatório");
		this.status = StatusContrato.BLOQUEADO;
		this.toque();
	}

	public void cancelar(String motivo) {
		if (this.status == StatusContrato.CANCELADO) {
			throw new IllegalStateException("Contrato já está cancelado");
		}
		this.motivoCancelamento = Objects.requireNonNull(motivo, "motivo do cancelamento é obrigatório");
		this.status = StatusContrato.CANCELADO;
		this.toque();
	}

	/** Bumps version + audit timestamp on every meaningful change. */
	private void toque() {
		this.versao += 1;
		this.dataUltimaAtualizacao = LocalDateTime.now();
	}

	// ---- Accessors ----

	public ContratoId getId() { return id; }
	public String getNumeroContrato() { return numeroContrato; }
	public Cliente getCliente() { return cliente; }
	public StatusContrato getStatus() { return status; }
	public ValorMonetario getLimiteCredito() { return limiteCredito; }
	public LocalDateTime getDataAnaliseCredito() { return dataAnaliseCredito; }
	public LocalDateTime getDataCriacao() { return dataCriacao; }
	public String getMotivoBloqueio() { return motivoBloqueio; }
	public String getMotivoCancelamento() { return motivoCancelamento; }
	public LocalDateTime getDataUltimaAtualizacao() { return dataUltimaAtualizacao; }
	public Long getVersao() { return versao; }

	// ---- Builder ----

	public static Builder builder() { return new Builder(); }

	public static final class Builder {
		private ContratoId id;
		private String numeroContrato;
		private Cliente cliente;
		private StatusContrato status;
		private ValorMonetario limiteCredito;
		private LocalDateTime dataAnaliseCredito;
		private LocalDateTime dataCriacao;

		public Builder id(ContratoId id) { this.id = id; return this; }
		public Builder numeroContrato(String n) { this.numeroContrato = n; return this; }
		public Builder cliente(Cliente c) { this.cliente = c; return this; }
		public Builder status(StatusContrato s) { this.status = s; return this; }
		public Builder limiteCredito(ValorMonetario v) { this.limiteCredito = v; return this; }
		public Builder dataAnaliseCredito(LocalDateTime d) { this.dataAnaliseCredito = d; return this; }
		public Builder dataCriacao(LocalDateTime d) { this.dataCriacao = d; return this; }

		public ContratoCredito build() {
			Objects.requireNonNull(id, "id é obrigatório");
			Objects.requireNonNull(numeroContrato, "numeroContrato é obrigatório");
			Objects.requireNonNull(cliente, "cliente é obrigatório");
			Objects.requireNonNull(limiteCredito, "limiteCredito é obrigatório");
			Objects.requireNonNull(dataAnaliseCredito, "dataAnaliseCredito é obrigatório");
			Objects.requireNonNull(dataCriacao, "dataCriacao é obrigatório");
			return new ContratoCredito(this);
		}
	}
}
