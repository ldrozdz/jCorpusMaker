package pl.pelcra.nkjp.corpusmaker.common.enums;

public enum XMLParsers {
    VTD_XML, SIMPLE_VTD_XML, WOODSTOX;

    public static XMLParsers getEnum(String s) {
        if (VTD_XML.name().equalsIgnoreCase(s)) {
            return VTD_XML;
        } else if (SIMPLE_VTD_XML.name().equalsIgnoreCase(s)) {
            return SIMPLE_VTD_XML;
        } else if (WOODSTOX.name().equalsIgnoreCase(s)) {
            return WOODSTOX;
        }
        throw new IllegalArgumentException("Illegal parser \"" + s + "\" specified.");
    }
}
