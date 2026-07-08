package br.com.contratocredito.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContratoCreditoTest {

	private ContratoCredito novo() {
		return ContratoCredito.criar(
				ContratoId.generate(),
				"CT-2026-000001",
				new Cliente("Maria Silva",
						new Endereco("PR", "Curitiba", "Rua das Flores", "123", new Cep("80010-000"))),
				ValorMonetario.reais(new BigDecimal("5000.00")),
				LocalDateTime.now().minusDays(1)
		);
	}

	@Test
	void criar_contrato_em_rascunho_com_versao_zero() {
		ContratoCredito c = novo();
		assertEquals(StatusContrato.RASCUNHO, c.getStatus());
		assertEquals(0L, c.getVersao());
		assertNotNull(c.getDataCriacao());
		assertNotNull(c.getDataUltimaAtualizacao());
	}

	@Test
	void bloquear_atualiza_status_motivo_e_versao() {
		ContratoCredito c = novo();
		c.bloquear("Suspeita de fraude");
		assertEquals(StatusContrato.BLOQUEADO, c.getStatus());
		assertEquals("Suspeita de fraude", c.getMotivoBloqueio());
		assertEquals(1L, c.getVersao());
	}

	@Test
	void bloquear_sem_motivo_lanca_excecao() {
		ContratoCredito c = novo();
		assertThrows(NullPointerException.class, () -> c.bloquear(null));
	}

	@Test
	void cancelar_apos_bloqueio_funciona_e_incrementa_versao() {
		ContratoCredito c = novo();
		c.bloquear("Fraude");
		c.cancelar("Confirmada fraude");
		assertEquals(StatusContrato.CANCELADO, c.getStatus());
		assertEquals("Confirmada fraude", c.getMotivoCancelamento());
		assertEquals(2L, c.getVersao());
	}

	@Test
	void nao_bloqueia_contrato_ja_cancelado() {
		ContratoCredito c = novo();
		c.cancelar("Solicitacao do cliente");
		assertThrows(IllegalStateException.class, () -> c.bloquear("Tarde demais"));
	}

	@Test
	void nao_cancela_contrato_ja_cancelado() {
		ContratoCredito c = novo();
		c.cancelar("Solicitacao do cliente");
		assertThrows(IllegalStateException.class, () -> c.cancelar("De novo"));
	}

	@Test
	void valor_monetario_negativo_lanca_excecao() {
		assertThrows(IllegalArgumentException.class,
				() -> ValorMonetario.reais(new BigDecimal("-1.00")));
	}

	@Test
	void cep_invalido_lanca_excecao() {
		assertThrows(IllegalArgumentException.class, () -> new Cep("123"));
	}
}
