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
import pl.pelcra.nkjp.corpusmaker.tagged.model.Text;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAO.NoSuchText;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAO.NoSuchUser;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAOBatched;

import java.beans.PropertyVetoException;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class SingleThreadedTaggedImportWrapper {

    private static SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public static void process() throws SQLException, PropertyVetoException, NoSuchUser, NoSuchText {
        XMLParser parser = null;
        List<File> xmlDirList = null;
        File startDir = null;
        int cnt = 1;
        long t0, t1, t2, t3;

        TextDAOBatched textDAO = new TextDAOBatched();

        // output some initial info
        log.info("[{}] Started in tagged import mode (single-threaded).", dtFmt.format(new Date()));

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
        log.info("[{}] Searching for data directories in {}.",
              dtFmt.format(new Date()), ConfigProvider.PARSING_PROPS.getString("textDir"));
        xmlDirList = new FastTable<File>();
        startDir = new File(ConfigProvider.PARSING_PROPS.getString("textDir"));

        if (startDir.exists() && startDir.isDirectory()) {
            Iterator<File> fileIterator = FileUtils.iterateFiles(startDir, new NameFileFilter(
                  "ann_morphosyntax.xml"), TrueFileFilter.INSTANCE);
            while (fileIterator.hasNext()) {
                xmlDirList.add(fileIterator.next().getParentFile());
            }

            log.info("[{}] Found {} directories to process.", dtFmt.format(new Date()), xmlDirList.size());

            // process texts and update DB

            t0 = System.currentTimeMillis();
            for (File dir : xmlDirList) {
                t1 = System.currentTimeMillis();
                log.info("[{}] Processing directory {} [{}/{}].", dtFmt.format(new Date()), dir.getAbsolutePath(), cnt, xmlDirList.size());
                try {
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
                cnt++;
                t2 = System.currentTimeMillis();
                log.info("[{}] Done processing directory, took {} seconds.", dtFmt.format(new Date()), (t2 - t1) / 1000F);
            }
            t3 = System.currentTimeMillis();
            log.info("[{}] Done importing texts, took {} seconds.", dtFmt.format(new Date()), (t3 - t0) / 1000F);
        }
    }
}
