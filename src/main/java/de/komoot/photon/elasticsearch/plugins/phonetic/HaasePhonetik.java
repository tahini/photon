package de.komoot.photon.elasticsearch.plugins.phonetic;

public class HaasePhonetik extends KoelnerPhonetik {

    private static final String[] HAASE_VARIATIONS_PATTERNS = {
        "OWN",
        "RB",
        "WSK",
        "A$",
        "O$",
        "SCH",
        "GLI",
        "EAU$",
        "^CH",
        "AUX",
        "EUX",
        "ILLE" };
    private static final String[] HAASE_VARIATIONS_REPLACEMENTS = { "AUN", "RW", "RSK", "AR", "OW", "CH", "LI", "O", "SCH", "O", "O", "I" };

    @Override
    protected String[] getPatterns() {
        return HAASE_VARIATIONS_PATTERNS;
    }

    @Override
    protected String[] getReplacements() {
        return HAASE_VARIATIONS_REPLACEMENTS;
    }

    @Override
    protected char getCode() {
        return '9';
    }
}