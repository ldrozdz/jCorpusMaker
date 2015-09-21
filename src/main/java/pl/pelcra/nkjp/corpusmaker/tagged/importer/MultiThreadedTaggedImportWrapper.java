package pl.pelcra.nkjp.corpusmaker.tagged.importer;

import javolution.util.FastTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import pl.pelcra.nkjp.corpusmaker.common.ConfigProvider;
import pl.pelcra.nkjp.corpusmaker.tagged.importer.parsers.SimpleVTDParser;
import pl.pelcra.nkjp.corpusmaker.tagged.importer.parsers.VTDParser;
import pl.pelcra.nkjp.corpusmaker.tagged.importer.parsers.XMLParser;
import pl.pelcra.nkjp.corpusmaker.tagged.importer.runners.TextConsumer;
import pl.pelcra.nkjp.corpusmaker.tagged.importer.runners.TextProducer;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Text;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAO;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAO.NoSuchText;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAO.NoSuchUser;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAOBatched;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAOTransactional;

import java.beans.PropertyVetoException;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class MultiThreadedTaggedImportWrapper {

    private static SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    // set up the chosen parser
    private static XMLParser getParser() {
        switch (ConfigProvider.XML_PARSER) {
            case VTD_XML:
                log.info("[{}] Using the VTD_XML parser.", dtFmt.format(new Date()));
                return new VTDParser();
            case SIMPLE_VTD_XML:
                log.info("[{}] Using the SIMPLE_VTD_XML parser.", dtFmt.format(new Date()));
                return new SimpleVTDParser();
            default:
                return null;
        }
    }

    public static void process() throws SQLException, PropertyVetoException, NoSuchUser, NoSuchText {
        final List<File> xmlDirList=new FastTable<>();
        File startDir = new File(ConfigProvider.PARSING_PROPS.getString("textDir"));;
        final LongAdder cnt = new LongAdder();

        // output some initial info
        log.info("[{}] Started in tagged import mode (multi-threaded).", dtFmt.format(new Date()));

        // iterate through directories
        log.info("[{}] Searching for data directories in {}.",
              dtFmt.format(new Date()), ConfigProvider.PARSING_PROPS.getString("textDir"));

        if (startDir.exists() && startDir.isDirectory()) {
            Iterator<File> fileIterator = FileUtils.iterateFiles(startDir, new NameFileFilter(
                  "ann_morphosyntax.xml"), TrueFileFilter.INSTANCE);
            while (fileIterator.hasNext()) {
                xmlDirList.add(fileIterator.next().getParentFile());
            }

            log.info("[{}] Found {} directories to process.", dtFmt.format(new Date()), xmlDirList.size());

            long t0 = System.currentTimeMillis();
            ForkJoinPool forkJoinPool = new ForkJoinPool(ConfigProvider.POOL_SIZE);
            // process texts and update DB
            try {
                forkJoinPool.submit(() ->
                            xmlDirList.parallelStream().forEach(dir -> {
                                      long t1 = System.currentTimeMillis();
                                      log.info("[{}] Processing directory {} [{}/{}].", dtFmt.format(new Date()), dir.getAbsolutePath(), cnt.longValue() + 1, xmlDirList.size());
                                      try {
                                          XMLParser parser = getParser();
                                          TextDAO textDAO = new TextDAOTransactional();
                                          try {
                                              for (Text t : parser.parseXMLDir(dir)) {
                                                  textDAO.putText(t);
                                              }
                                          } catch (Exception e) {
                                              e.printStackTrace();
                                          }
                                      } catch (Exception e) {
                                          e.printStackTrace();
                                      }
                                      cnt.increment();
                                      long t2 = System.currentTimeMillis();
                                      log.info("[{}] Done processing directory, took {} seconds.", dtFmt.format(new Date()), (t2 - t1) / 1000F);
                                  }
                            )
                ).get();
            } catch (InterruptedException e) {
                log.error("{}", e);
            } catch (ExecutionException e) {
                log.error("{}", e);
            }

            long t3 = System.currentTimeMillis();
            log.info("[{}] Done importing texts, took {} seconds.", dtFmt.format(new Date()), (t3 - t0) / 1000F);
        }
    }

    /**
     * @throws SQLException
     * @throws PropertyVetoException
     * @throws NoSuchUser
     * @throws NoSuchText
     * @deprecated This will kill your machine for large texts.
     */
    @Deprecated
    public static void processWithProducerConsumer() throws SQLException, PropertyVetoException, NoSuchUser, NoSuchText {
        XMLParser parser = null;
        List<File> xmlDirList = null;
        File startDir = null;
        BlockingQueue<Text> textQ = null;
        long t0, t3;

        // output some initial info
        log.info("[{}] Started in tagged import mode.", dtFmt.format(new Date()));

        // set up the chosen parser
        switch (ConfigProvider.XML_PARSER) {
            case VTD_XML:
                log.info("[{}] Using the VTD_XML parser.", dtFmt.format(new Date()));
                parser = new VTDParser();
                break;
            case SIMPLE_VTD_XML:
                log.info("[{}] Using the SIMPLE_VTD_XML parser.", dtFmt.format(new Date()));
                parser = new SimpleVTDParser();
                break;
            default:
                break;
        }

        // iterate through directories
        log.info("[{}] Searching for data directories in {}.", dtFmt.format(new Date()),
              ConfigProvider.PARSING_PROPS.getString("textDir"));
        xmlDirList = new FastTable<File>();
        startDir = new File(ConfigProvider.PARSING_PROPS.getString("textDir"));

        if (startDir.exists() && startDir.isDirectory()) {
            Iterator<File> fileIterator = FileUtils.iterateFiles(startDir, new NameFileFilter(
                  "ann_morphosyntax.xml"), TrueFileFilter.INSTANCE);
            while (fileIterator.hasNext()) {
                xmlDirList.add(fileIterator.next().getParentFile());
            }

            log.info("[{}] Found {} directories to process.", dtFmt.format(new Date()),
                  xmlDirList.size());

            // process texts and update DB

            t0 = System.currentTimeMillis();
            textQ = new ArrayBlockingQueue<Text>(10000, true);

            ExecutorService threadPool = Executors.newFixedThreadPool(ConfigProvider.POOL_SIZE);
            Future producerStatus = threadPool.submit(new TextProducer(textQ, xmlDirList, parser));
            Future consumerStatus = threadPool.submit(new TextConsumer(textQ));
            // this will wait for the producer to finish its execution.
            try {
                producerStatus.get();
                consumerStatus.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            threadPool.shutdown();
            t3 = System.currentTimeMillis();
            log.info("[{}] Done processing texts, took {} seconds.", dtFmt.format(new Date()),
                  (t3 - t0) / 1000F);

        }
    }
}
