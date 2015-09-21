package pl.pelcra.nkjp.corpusmaker.lemmatizer

import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: MJB
 * Date: 15.10.13
 * Time: 00:39
 * To change this template use File | Settings | File Templates.
 */

class LemmaDictionaryTest {

    @Test
    void testInitialize() {

        LemmaDictionary.INSTANCE.initialize(this.getClass().getResource("/lemmatizer/nkjpM.dict"))
        BufferedReader br = new BufferedReader(new FileReader(this.getClass().getResource("/lemmatizer/nkjpM.txt").file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] query = line.split("\\t")
            String[] params = query[0].split("_")
            String lemma = LemmaDictionary.INSTANCE.lookupCombination(params[0], params[1])

            log.info("Input word: " + params[0] + "_" + params[1] + ", lemma: " + query[1] + ", result: " + lemma)
            assert (query[1].equals(lemma))


        }
        br.close();

    }


}
