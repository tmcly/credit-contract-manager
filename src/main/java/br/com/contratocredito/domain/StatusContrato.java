package br.com.contratocredito.domain;

/**
 * Lifecycle states of a credit contract (state machine).
 *
 * The enum only lists the states. Which transitions are legal is enforced
 * by {@link ContratoCredito} (e.g. you cannot block a cancelled contract).
 * Adjust this set as the real business rules are confirmed.
 */
public enum StatusContrato {
	RASCUNHO,
	EM_ANALISE,
	APROVADO,
	ATIVO,
	BLOQUEADO,
	CANCELADO
}
