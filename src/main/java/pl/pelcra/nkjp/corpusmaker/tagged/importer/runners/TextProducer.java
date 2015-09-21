package pl.pelcra.nkjp.corpusmaker.tagged.importer.runners;

import lombok.extern.slf4j.Slf4j;
import pl.pelcra.nkjp.corpusmaker.tagged.importer.parsers.XMLParser;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class TextProducer implements Runnable {
    private BlockingQueue<Text> textQ;
    private XMLParser parser;
    private List<File> dirL;
    // TODO: temp solution, this will not work with multiple producers
    private int textCnt, textTotalCnt = 1, dirCnt = 1;
    private SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private long t1, t2;
    private String curNkjpId = new String();

    public TextProducer(BlockingQueue<Text> textQ, List<File> dirL, XMLParser parser) {
        this.textQ = textQ;
        this.dirL = dirL;
        this.parser = parser;
    }

    public void run() {
        for (File dir : dirL) {
            t1 = System.currentTimeMillis();
            log.info("[{}] Producer: Processing directory {} [{}/{} dirs processed].", dtFmt.format(new Date()), dir.getAbsolutePath(), dirCnt, dirL.size());
            try {
                textCnt = 1;
                try {
                    for (Text t : parser.parseXMLDir(dir)) {
                        if (textCnt == 1) {
                            curNkjpId = t.getNkjpId();
                        }
                        textQ.put(t);
                        textCnt++;
                        textTotalCnt++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dirCnt++;
                t2 = System.currentTimeMillis();
                log.info("[{}] Producer: Got {} texts, {} total ({}/*). Queue length: {}.",
                      dtFmt.format(new Date()), textCnt, textTotalCnt, curNkjpId, textQ.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
            log.info("[{}] Producer: Done processing directory, took {} seconds.", dtFmt.format(new Date()), (t2 - t1) / 1000F);
        }
        // TODO: temp solution, this should be a full-fledged semaphor
        try {
            log.info("[{}] Producer: Producer finished working.", dtFmt.format(new Date()));
            textQ.put(new Text(Boolean.TRUE));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
