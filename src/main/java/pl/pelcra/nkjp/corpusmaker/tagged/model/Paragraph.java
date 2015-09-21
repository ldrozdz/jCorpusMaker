package pl.pelcra.nkjp.corpusmaker.tagged.model;

import javolution.util.FastList;

import java.util.List;

public class Paragraph {
    private Integer num;
    private String xmlId;
    private List<Sentence> sentences;

    public Paragraph() {
        this.sentences = new FastList<Sentence>();
    }

    public Paragraph(Integer num, String xmlId, List<Sentence> sentences) {
        this.num = num;
        this.xmlId = xmlId;
        this.sentences = sentences;
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

    public List<Sentence> getSentences() {
        return sentences;
    }

    public void addSentence(Sentence sentence) {
        this.sentences.add(sentence);
    }


    public void setSentences(List<Sentence> sentences) {
        this.sentences = sentences;
    }

}
