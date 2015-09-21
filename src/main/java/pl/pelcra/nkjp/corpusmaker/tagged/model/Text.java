package pl.pelcra.nkjp.corpusmaker.tagged.model;

import javolution.util.FastList;

import java.util.List;

public class Text {

    private Integer num;
    private String nkjpId;
    private String xmlId;
    private String file;
    private String path;
    private List<Paragraph> paragraphs;
    //TODO: everything having to do with isNull is a temp solution for multithreading
    private Boolean isNull = Boolean.FALSE;

    public Text() {
        this.paragraphs = new FastList<Paragraph>();
    }

    public Text(Integer num, String nkjpId, String xmlId, String file, String path,
                List<Paragraph> paragraphs) {
        this.num = num;
        this.nkjpId = nkjpId;
        this.xmlId = xmlId;
        this.file = file;
        this.path = path;
        this.paragraphs = paragraphs;
    }

    public Text(Boolean isNull) {
        this.isNull = isNull;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getNkjpId() {
        return nkjpId;
    }

    public void setNkjpId(String nkjpId) {
        this.nkjpId = nkjpId;
    }

    public String getXmlId() {
        return xmlId;
    }

    public void setXmlId(String xmlId) {
        this.xmlId = xmlId;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public void addParagraph(Paragraph paragraph) {
        this.paragraphs.add(paragraph);
    }

    public void setParagraphs(List<Paragraph> paragraphs) {
        this.paragraphs = paragraphs;
    }

    public synchronized Boolean isNull() {
        return isNull;
    }

    public synchronized void setIsNull(Boolean isNull) {
        this.isNull = isNull;
    }

}
