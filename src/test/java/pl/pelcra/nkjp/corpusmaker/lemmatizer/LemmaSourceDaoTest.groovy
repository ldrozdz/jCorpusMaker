package pl.pelcra.nkjp.lemmatizer

import groovy.sql.Sql
import pl.pelcra.nkjp.corpusmaker.lemmatizer.LemmaSourceDao
import pl.pelcra.nkjp.corpusmaker.lemmatizer.LemmaWriter


/**
 * Created with IntelliJ IDEA.
 * User: MJB
 * Date: 20.08.13
 * Time: 00:49
 * To change this template use File | Settings | File Templates.
 */

class LemmaSourceDaoTest {
    LemmaSourceDao lem
    LemmaWriter lw
    int step = 100000

    void setUp() {


        def db = new ConfigSlurper().parse(new File('db.conf').toURI().toURL()).get("million")

        Sql sql = Sql.newInstance(db.get("database"), db.get("user"), db.get("password"), db.get("driver"))
        lem = new LemmaSourceDao(sql, "\t")

    }


    void tearDown() {
        lem.closeSql()
    }


    void testGetLemmas() {
        int count = lem.getMax()

        for (int i = 1; i < count; i = i + step) {

            lem.getLemmas(i, i + step)
        }
        int multiples = 0
        lem.lemmaCounts.keySet().each {
            if (lem.lemmaCounts.get(it.toString()).counts.size() > 1) {
                multiples++
                //  log.info(it.toString()+"  "+lem.lemmaCounts.get(it.toString()).counts.toString())
            }

        }

        log.info(multiples.toString())
        lw = new LemmaWriter("d:/test.txt", lem.lemmaCounts)

        lw.writeToFile()
        lem.closeSql()

    }

}


