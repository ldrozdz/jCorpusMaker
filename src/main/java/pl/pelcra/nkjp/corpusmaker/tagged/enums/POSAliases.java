package pl.pelcra.nkjp.corpusmaker.tagged.enums;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum POSAliases {
    INSTANCE;
    public static final Map<String, String> ALIAS_MAP = Collections
          .unmodifiableMap(new HashMap<String, String>() {
              {
                  put("comp", "conj");
                  put("depr", "noun");
                  put("ger", "noun");
                  put("subst", "noun");
                  put("adja", "adj");
                  put("adjc", "adj");
                  put("adjp", "adj");
                  put("numcol", "num");
                  put("ppron12", "pron");
                  put("ppron3", "pron");
                  put("siebie", "pron");
                  put("bedzie", "verb");
                  put("fin", "verb");
                  put("imps", "verb");
                  put("impt", "verb");
                  put("inf", "verb");
                  put("pact", "verb");
                  put("pant", "verb");
                  put("pcon", "verb");
                  put("ppas", "verb");
                  put("praet", "verb");
                  put("winien", "verb");
                  put("adj", "adj");
                  put("adv", "adv");
                  put("aglt", "aglt");
                  put("brev", "brev");
                  put("burk", "burk");
                  put("conj", "conj");
                  put("ign", "ign");
                  put("interj", "interj");
                  put("interp", "punct");
                  put("num", "num");
                  put("pred", "pred");
                  put("prep", "prep");
                  put("qub", "qub");
                  put("xxx", "xxx");
              }
          });
}
