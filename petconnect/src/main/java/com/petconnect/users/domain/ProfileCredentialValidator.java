package com.petconnect.users.domain;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Valida los números acreditativos exigidos para los perfiles profesionales.
 *
 * <p>
 * Ni el nº de colegiado veterinario ni el nº de registro de protectora tienen
 * un dígito de control verificable matemáticamente (como el DNI), porque los
 * asignan colegios provinciales y registros autonómicos sin formato nacional
 * único ni API pública de verificación. Esta validación es, por tanto,
 * <strong>estricta de formato</strong>: rechaza basura evidente pero no puede
 * certificar que el número exista en el registro oficial. La verificación
 * definitiva sigue siendo responsabilidad del Colegio / registro autonómico
 * correspondiente.
 * </p>
 */
public final class ProfileCredentialValidator {

    private ProfileCredentialValidator() {
    }

    /** Prefijos de colegios provinciales de veterinarios reconocidos (COLVET-XX). */
    private static final Set<String> VET_COLLEGE_PREFIXES = Set.of(
            "AL", "AB", "A", "AV", "B", "BA", "BI", "BU", "C", "CC", "CA", "CS",
            "CR", "CO", "CU", "GI", "GR", "GU", "HU", "J", "LC", "LE", "L", "LO",
            "LU", "M", "MA", "MU", "NA", "OU", "O", "P", "PO", "S", "SA", "SG",
            "SE", "SO", "SS", "T", "TE", "TO", "V", "VA", "VI", "ZA", "Z", "H",
            "PM", "TF", "GC", "CE", "ML");

    /**
     * Colegiado: {@code COLVET-<provincia>-<3..6 dígitos>} o
     * {@code <provincia>-<3..6 dígitos>}, p. ej. {@code M-12345} o
     * {@code COLVET-M-12345}. Provoca rechazo cualquier otro formato.
     */
    private static final Pattern LICENSE_PATTERN = Pattern.compile(
            "^(?:COLVET-)?([A-Z]{1,3})-(\\d{3,6})$");

    /**
     * Registro de protectora: CIF válido (letra + 7 dígitos + control) o
     * referencia de registro autonómico {@code <letras>-<año>-<dígitos>}, p. ej.
     * {@code G-12345674} o {@code ASOC-2021-4582}.
     */
    private static final Pattern REGIONAL_REGISTER_PATTERN = Pattern.compile(
            "^[A-ZÁÉÍÓÚÑ]{2,10}-\\d{4}-\\d{3,8}$");

    private static final Pattern CIF_PATTERN = Pattern.compile(
            "^[ABCDEFGHJNPQRSUVW]\\d{7}[0-9A-J]$");

    /** Versión tolerante con el guion tras la letra (p. ej. G-12345674). */
    private static final Pattern CIF_DASHED_PATTERN = Pattern.compile(
            "^[ABCDEFGHJNPQRSUVW]-\\d{7}[0-9A-J]$");

    /** Letras de control del CIF según el resto módulo 10. */
    private static final String CIF_CONTROL_LETTERS = "JABCDEFGHI";

    /** Tipos de CIF cuyo control debe ser obligatoriamente numérico. */
    private static final Set<Character> CIF_NUMERIC_CONTROL = Set.of('A', 'B', 'E', 'H');

    /** Tipos de CIF cuyo control debe ser obligatoriamente letra. */
    private static final Set<Character> CIF_LETTER_CONTROL = Set.of('K', 'P', 'Q', 'S');

    /** ¿Nº de colegiado con formato estricto válido? Normaliza a mayúsculas y espacios. */
    public static boolean isValidLicenseNumber(String licenseNumber) {
        String normalized = normalize(licenseNumber);
        if (normalized == null) {
            return false;
        }
        var matcher = LICENSE_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return false;
        }
        return VET_COLLEGE_PREFIXES.contains(matcher.group(1));
    }

    /** ¿Nº de registro de protectora válido? CIF con control o referencia autonómica. */
    public static boolean isValidCharityNumber(String charityNumber) {
        String normalized = normalize(charityNumber);
        if (normalized == null) {
            return false;
        }
        if (CIF_PATTERN.matcher(normalized).matches()) {
            return isValidCif(normalized);
        }
        if (CIF_DASHED_PATTERN.matcher(normalized).matches()) {
            return isValidCif(normalized.replace("-", ""));
        }
        return REGIONAL_REGISTER_PATTERN.matcher(normalized).matches();
    }

    /** Normaliza la entrada (trim + mayúsculas); null si vacía. */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        return normalized.isEmpty() ? null : normalized;
    }

    /** Verifica el dígito/letra de control del CIF (algoritmo módulo 10). */
    private static boolean isValidCif(String cif) {
        char type = cif.charAt(0);
        String digits = cif.substring(1, 8);
        char control = cif.charAt(8);

        int sumEven = 0;
        int sumOdd = 0;
        for (int i = 0; i < 7; i++) {
            int digit = digits.charAt(i) - '0';
            if (i % 2 == 0) {
                int doubled = digit * 2;
                sumOdd += doubled / 10 + doubled % 10;
            } else {
                sumEven += digit;
            }
        }
        int total = sumEven + sumOdd;
        int controlDigit = (10 - (total % 10)) % 10;
        char expectedLetter = CIF_CONTROL_LETTERS.charAt(controlDigit);

        if (CIF_NUMERIC_CONTROL.contains(type)) {
            return control == ('0' + controlDigit);
        }
        if (CIF_LETTER_CONTROL.contains(type)) {
            return control == expectedLetter;
        }
        return control == ('0' + controlDigit) || control == expectedLetter;
    }
}
