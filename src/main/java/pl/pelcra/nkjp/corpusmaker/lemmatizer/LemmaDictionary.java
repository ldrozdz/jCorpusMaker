package pl.pelcra.nkjp.corpusmaker.lemmatizer;

import lombok.extern.java.Log;
import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;

import java.net.URL;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: MJB
 * Date: 21.08.13
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */
@Log
enum LemmaDictionary {
    INSTANCE;
    public static DictionaryLookup dl;

    public void initialize(URL url) {
        System.err.println("Loading the NKJP lemma dictionary...");
        try {
            dl =
                  new DictionaryLookup(
                        Dictionary.read(url));
            System.err.println("Dictionary loaded...");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static String lookupCombination(String word, String pos) throws Exception {

        String key = word + "_" + pos;
        List<WordData> wd = dl.lookup(key);
        if (wd.size() > 0) {
            return wd.get(0).getStem().toString();
        } else {
            return word;
        }
    }

}
