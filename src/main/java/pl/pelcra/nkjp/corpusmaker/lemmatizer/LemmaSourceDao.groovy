package pl.pelcra.nkjp.corpusmaker.lemmatizer

import groovy.sql.Sql
import groovy.util.logging.Log

/**
 * Created with IntelliJ IDEA.
 * User: MJB
 * Date: 14.10.13
 * Time: 20:30
 * To change this template use File | Settings | File Templates.
 */
@Log
class LemmaSourceDao {

    String sep
    Sql sql
    Map<String, WordForm> lemmaCounts


    LemmaSourceDao(Sql sql_, String separator) {
        this.sql = sql_
        this.sep = separator.toString()
        lemmaCounts = new HashMap<>()


    }

    final String getLemmasSQL = "SELECT word, lemma, pos FROM word where num between ? and ?;"

    public void getLemmas(int from, int to) {

        sql.rows(getLemmasSQL, [from, to]).each {
            String word = it.getProperty("word").toString().toLowerCase()
            String lemma = it.getProperty("lemma").toString().toLowerCase()
            String pos = it.getProperty("pos").toString()
            Set<String> lemmaLines = processLemmaData(word, lemma, pos)

            if (lemmaLines != null) {
                lemmaLines.each { String lemmaLine ->

                    String[] parts = lemmaLine.split("\t")
                    String key = parts[0] + "_" + parts[2]
                    WordForm wf = lemmaCounts.get(key, new WordForm())
                    Map temp = wf.getCounts()

                    temp[lemmaLine] = wf.getCounts().get(lemmaLine, 0) + 1
                }
            }

        }


    }

    Set<String> processLemmaData(String word, String lemma, String pos) {
        Set<String> lemmaLines = new HashSet<>()
        if (word.length() > 0 && lemma.length() > 0) {
            if (word.contains(sep) || lemma.contains(sep) || word.contains("+") || lemma.contains("+") || word.contains("_") || lemma.contains("_")) {
                log.info("Wrong word and/or lemma: " + word + " " + lemma)
                return null
            } else {

                if (pos.contains("+")) {

                    String[] poss = pos.split("\\+")

                    poss.each { String ps ->

                        def lemmaLine = word + "_" + ps + sep + lemma + sep + ps
                        lemmaLines << lemmaLine
                    }

                } else {
                    def lemmaLine = word + "_" + pos + sep + lemma + sep + pos
                    lemmaLines << lemmaLine
                }

                return lemmaLines
            }
        }
    }

    final String getMaxSQL = "SELECT MAX(num) as maxnum from word;"

    public int getMax() {

        return sql.firstRow(getMaxSQL).get("maxnum")
    }

    public void closeSql() {

        sql.close()
    }

}