package br.com.creditcontract.domain.valueobject;

import br.com.creditcontract.domain.exception.InvalidDocumentNumberException;

import java.util.regex.Pattern;

/**
 * Brazilian CPF represented by digits only.
 *
 * <p>The constructor accepts formatted or unformatted values, normalizes the
 * representation, rejects unsupported characters and validates check digits.
 */
public record DocumentNumber(String value) {

	private static final Pattern ALLOWED_FORMAT = Pattern.compile("[0-9./\\-\\s]+");
	private static final Pattern NON_DIGITS = Pattern.compile("\\D");

	private static final int[] CPF_FIRST_DIGIT_WEIGHTS = {10, 9, 8, 7, 6, 5, 4, 3, 2};
	private static final int[] CPF_SECOND_DIGIT_WEIGHTS = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

	public DocumentNumber {
		if (value == null || value.isBlank()) {
			throw new InvalidDocumentNumberException("documentNumber is required");
		}
		if (!ALLOWED_FORMAT.matcher(value).matches()) {
			throw new InvalidDocumentNumberException("documentNumber contains invalid characters");
		}

		String digits = NON_DIGITS.matcher(value).replaceAll("");
		if (!isValidCpf(digits)) {
			throw new InvalidDocumentNumberException("documentNumber must be valid");
		}

		value = digits;
	}

	public static DocumentNumber from(String value) {
		return new DocumentNumber(value);
	}

	public int finalDigit() {
		return Character.digit(value.charAt(value.length() - 1), 10);
	}

	private static boolean isValidCpf(String digits) {
		return digits.length() == 11
				&& !hasRepeatedDigits(digits)
				&& matchesCheckDigits(digits, CPF_FIRST_DIGIT_WEIGHTS, CPF_SECOND_DIGIT_WEIGHTS);
	}

	private static boolean matchesCheckDigits(String digits, int[] firstWeights, int[] secondWeights) {
		int firstPosition = digits.length() - 2;
		int secondPosition = digits.length() - 1;
		return digitAt(digits, firstPosition) == calculateCheckDigit(digits, firstWeights)
				&& digitAt(digits, secondPosition) == calculateCheckDigit(digits, secondWeights);
	}

	private static int calculateCheckDigit(String digits, int[] weights) {
		int sum = 0;
		for (int index = 0; index < weights.length; index++) {
			sum += digitAt(digits, index) * weights[index];
		}
		int digit = 11 - (sum % 11);
		return digit >= 10 ? 0 : digit;
	}

	private static boolean hasRepeatedDigits(String digits) {
		return digits.chars().allMatch(character -> character == digits.charAt(0));
	}

	private static int digitAt(String digits, int index) {
		return Character.digit(digits.charAt(index), 10);
	}
}
