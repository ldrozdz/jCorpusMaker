package pl.pelcra.nkjp.corpusmaker.common.enums;

public enum ProcessingModes {
    IMPORT, EXPORT, SINGLE_THREADED, MULTI_THREADED;

    public static ProcessingModes getEnum(String s) {
        if (IMPORT.name().equalsIgnoreCase(s)) {
            return IMPORT;
        } else if (EXPORT.name().equalsIgnoreCase(s)) { return EXPORT; }
        if (SINGLE_THREADED.name().equalsIgnoreCase(s)) {
            return SINGLE_THREADED;
        } else if (MULTI_THREADED.name().equalsIgnoreCase(s)) { return MULTI_THREADED; }
        throw new IllegalArgumentException("Illegal processing mode \"" + s + "\" specified.");
    }
}
