package pl.pelcra.nkjp.corpusmaker.tagged;

import lombok.extern.slf4j.Slf4j;
import pl.pelcra.nkjp.corpusmaker.common.ConfigProvider;
import pl.pelcra.nkjp.corpusmaker.tagged.importer.MultiThreadedTaggedImportWrapper;
import pl.pelcra.nkjp.corpusmaker.tagged.importer.SingleThreadedTaggedImportWrapper;

import java.text.SimpleDateFormat;
import java.util.Date;

/* XML parsers:
 * - SAX
 * - JAXB
 * - StAX
 * -- Woodstox: http://woodstox.codehaus.org/
 * - RapidXML: http://rapidxml.sourceforge.net/
 * - VTD-XML: http://vtd-xml.sourceforge.net/
 * - Piccolo: http://piccolo.sourceforge.net/
 */

@Slf4j
public class StartUp {

    public static void main(String args[]) throws Exception {

        if (args.length != 1) {
            usage();
        } else {
            log.info("[{}] Initializing app.", new SimpleDateFormat(
                  "yyyy/MM/dd HH:mm:ss").format(new Date()));
            ConfigProvider.loadConfig(args[0]);
            switch (ConfigProvider.MODE) {
                case IMPORT:
                    switch (ConfigProvider.THREADS) {
                        case SINGLE_THREADED:
                            SingleThreadedTaggedImportWrapper.process();
                            break;
                        case MULTI_THREADED:
                            MultiThreadedTaggedImportWrapper.process();
                            break;
                    }
                    break;
                case EXPORT:
                    break;
                default:
                    usage();
                    break;
            }
            log.info("[{}] Done.",
                  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        }
    }

    private static void usage() {
        System.out.println("Usage: java -Xmx2G jCorpusMaker.jar config.xml");
    }
}
