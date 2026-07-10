package br.com.creditcontract.domain.valueobject;

import br.com.creditcontract.domain.exception.InvalidDocumentNumberException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentNumberTest {

	@Test
	void shouldNormalizeAndValidateFormattedCpf() {
		DocumentNumber documentNumber = DocumentNumber.from("529.982.247-25");

		assertEquals("52998224725", documentNumber.value());
		assertEquals(5, documentNumber.finalDigit());
	}

	@Test
	void shouldRejectInvalidCheckDigits() {
		assertThrows(InvalidDocumentNumberException.class,
				() -> DocumentNumber.from("529.982.247-24"));
	}

	@Test
	void shouldRejectRepeatedDigits() {
		assertThrows(InvalidDocumentNumberException.class,
				() -> DocumentNumber.from("111.111.111-11"));
	}

	@Test
	void shouldRejectUnsupportedLength() {
		assertThrows(InvalidDocumentNumberException.class,
				() -> DocumentNumber.from("123456789"));
	}

	@Test
	void shouldRejectUnsupportedCharacters() {
		assertThrows(InvalidDocumentNumberException.class,
				() -> DocumentNumber.from("CPF 529.982.247-25"));
	}

	@Test
	void shouldRejectMissingValue() {
		assertThrows(InvalidDocumentNumberException.class, () -> DocumentNumber.from(null));
		assertThrows(InvalidDocumentNumberException.class, () -> DocumentNumber.from("   "));
	}
}
