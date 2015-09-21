package pl.pelcra.nkjp.corpusmaker.lemmatizer

import groovy.util.logging.Log

/**
 * Created with IntelliJ IDEA.
 * User: MJB
 * Date: 14.10.13
 * Time: 20:57
 * To change this template use File | Settings | File Templates.
 */
@Log
class LemmaWriter {
    File file
    Map<String, WordForm> lemmaCounts

    public LemmaWriter(String target, Map<String, WordForm> lemmaCounts_) {
        this.file = new File(target)
        this.lemmaCounts = lemmaCounts_

    }

    public void writeToFile() {
        file.withWriter { out ->
            lemmaCounts?.keySet().each { String key ->
                String lemmaline
                int count = 0
                lemmaCounts.get(key).counts.keySet().each {
                    int temp = lemmaCounts?.get(key).counts[it]
                    if (temp > count) {
                        count = temp
                        lemmaline = it
                    }
                }
                out.write(lemmaline)
                out.write(System.getProperty("line.separator"))
            }
        }
    }

}
