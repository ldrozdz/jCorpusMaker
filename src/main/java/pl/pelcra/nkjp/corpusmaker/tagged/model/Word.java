package pl.pelcra.nkjp.corpusmaker.tagged.model;

import lombok.extern.slf4j.Slf4j;
import pl.pelcra.nkjp.corpusmaker.tagged.enums.POSAliases;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Word {
    private String xmlId;
    private String word;
    private String wordMerged;
    private String lemma;
    private String pos;
    private String posAlias;
    private String posClass;
    private boolean merged = false;
    private final Pattern pat = Pattern.compile("(:adj|:adja|:adjc|:adjp|:adv|:aglt|:bedzie|:brev|:burk|:comp|:conj|:depr|:fin|:ger|:ign|:imps|:impt|:inf|:interj|:interp|:num|:numcol|:pact|:pant|:pcon|:ppas|:ppron12|:ppron3|:praet|:pred|:prep|:qub|:siebie|:subst|:winien|:xxx)((?=$)|(?=:))");

    public Word(String xmlId, String word, String posRaw) throws Exception {
        this.xmlId = xmlId;
        this.word = word;

        Matcher matcher = pat.matcher(posRaw);
        matcher.find();
        int posStart = matcher.start();
        this.lemma = posRaw.substring(0, posStart);
        String pos = posRaw.substring(posStart + 1, posRaw.length());
        String[] posSplit = pos.split(":");
        this.posClass = posSplit[0];
        this.posAlias = POSAliases.ALIAS_MAP.get(this.posClass);
        this.pos = pos;

        if (this.posAlias == null) {
            throw new Exception();
        }
    }

    public String getXmlId() {
        return xmlId;
    }

    public void setXmlId(String xmlId) {
        this.xmlId = xmlId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getPosAlias() {
        return posAlias;
    }

    public void setPosAlias(String posAlias) {
        this.posAlias = posAlias;
    }

    public synchronized String getLemma() {
        return lemma;
    }

    public synchronized void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getPosClass() {
        return posClass;
    }

    public void setPosClass(String posClass) {
        this.posClass = posClass;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

}
