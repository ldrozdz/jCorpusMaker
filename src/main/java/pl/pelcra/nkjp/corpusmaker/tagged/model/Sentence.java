package pl.pelcra.nkjp.corpusmaker.tagged.model;

import javolution.util.FastList;

import java.util.List;

public class Sentence {
    private Integer num;
    private String xmlId;
    private List<Word> words;

    public Sentence() {
        this.words = new FastList<Word>();
    }

    public Sentence(Integer num, String xmlId, List<Word> words) {
        this.num = num;
        this.xmlId = xmlId;
        this.words = words;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getXmlId() {
        return xmlId;
    }

    public void setXmlId(String xmlId) {
        this.xmlId = xmlId;
    }

    public List<Word> getWords() {
        return words;
    }

    public void addWord(Word word) {
        this.words.add(word);
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    // Special methods for dealing with "aglt"
    public Word getLastWord() {

        if (!this.words.isEmpty()) { return this.words.get(this.words.size() - 1); }
        return null;
    }

    public void appendToLastWord(Word currentWord) {
        if (!this.words.isEmpty()) {
            Word lastWord = getLastWord();
            lastWord.setWord(lastWord.getWord() + currentWord.getWord());
            lastWord.setMerged(true);
            lastWord.setPos(lastWord.getPos() + "+" + currentWord.getPos());
            lastWord.setPosClass(lastWord.getPosClass() + "+" + currentWord.getPosClass());
            this.words.set(this.words.size() - 1, lastWord);
        }
    }

}
