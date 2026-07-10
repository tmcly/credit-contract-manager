package br.com.creditcontract.domain.valueobject;

import br.com.creditcontract.domain.exception.InvalidCpfException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfTest {

	@Test
	void shouldNormalizeAndValidateFormattedCpf() {
		Cpf cpf = Cpf.from("529.982.247-25");

		assertEquals("52998224725", cpf.value());
		assertEquals(5, cpf.finalDigit());
	}

	@Test
	void shouldRejectInvalidCheckDigits() {
		assertThrows(InvalidCpfException.class,
				() -> Cpf.from("529.982.247-24"));
	}

	@Test
	void shouldRejectRepeatedDigits() {
		assertThrows(InvalidCpfException.class,
				() -> Cpf.from("111.111.111-11"));
	}

	@Test
	void shouldRejectUnsupportedLength() {
		assertThrows(InvalidCpfException.class,
				() -> Cpf.from("123456789"));
	}

	@Test
	void shouldRejectUnsupportedCharacters() {
		assertThrows(InvalidCpfException.class,
				() -> Cpf.from("CPF 529.982.247-25"));
	}

	@Test
	void shouldRejectMissingValue() {
		assertThrows(InvalidCpfException.class, () -> Cpf.from(null));
		assertThrows(InvalidCpfException.class, () -> Cpf.from("   "));
	}
}
