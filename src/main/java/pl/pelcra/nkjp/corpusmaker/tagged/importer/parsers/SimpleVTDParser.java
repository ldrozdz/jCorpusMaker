package pl.pelcra.nkjp.corpusmaker.tagged.importer.parsers;

import com.ximpleware.*;
import com.ximpleware.NavException;
import com.ximpleware.extended.*;
import javolution.util.FastTable;
import lombok.extern.slf4j.Slf4j;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Paragraph;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Sentence;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Text;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Word;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//This is a simplified version for quickly importing the manually-annotated version to the database
//It ignores paragraphs and always puts the whole text into one paragraph
//It also sets the text's XMLid to NKJPid

@Slf4j
public class SimpleVTDParser implements XMLParser {
    private final SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    public List<Text> parseXMLDir(File dir) throws
          ParseException, XPathParseException, XPathEvalException, NavException, IOException,
          ParseExceptionHuge, XPathParseExceptionHuge, XPathEvalExceptionHuge, NavExceptionHuge {
        // sets nkjpId
        String nkjpId = parseHeader(new File(dir.getAbsolutePath() + "/header.xml"));
        // sets npsList
        List<String> npsList = null;//parseSegmentation(new File(dir.getAbsolutePath() + "/ann_segmentation.xml"));
        // sets texts
        List<Text> texts = parseMorphoSyntax(new File(dir.getAbsolutePath() + "/ann_morphosyntax.xml"), nkjpId, npsList);
        return texts;
    }


    private String parseHeader(File f) throws IOException, ParseException, XPathParseException, XPathEvalException, NavException {
        log.info("[{}] Parsing header.xml ({} bytes).", dtFmt.format(new Date()), f.length());
        String nkjpId = new String();
        VTDGen vg = new VTDGen();
        vg.parseFile(f.getAbsolutePath(), true);
        VTDNav vn = vg.getNav();
        AutoPilot idAP = new AutoPilot(vn);
        idAP.declareXPathNameSpace("xml", "http://www.w3.org/XML/1998/namespace");
        idAP.declareXPathNameSpace("nkjp", "http://www.nkjp.pl/ns/1.0");
        // idAP.selectXPath("/teiHeader/@xml:lang");
        idAP.selectXPath("/teiHeader");
        while (idAP.evalXPath() != -1) {
            nkjpId = vn.toString(vn.getAttrVal("xml:id"));
        }
        idAP.resetXPath();
        // Log warning if ID not found
        if (nkjpId.isEmpty()) {
            log.warn(new String(f.getAbsolutePath() + ": nkjp_id not found."));
        }
        return nkjpId;
    }

    private List<String> parseSegmentation(File f) throws XPathParseException, XPathEvalException,
          NavException {
        log.info("[{}] Parsing ann_segmentation.xml ({} bytes).", dtFmt.format(new Date()), f.length());
        FastTable<String> npsList = new FastTable<>();
        VTDGen vg = new VTDGen();
        vg.parseFile(f.getAbsolutePath(), true);
        VTDNav vn = vg.getNav();
        AutoPilot npsAP = new AutoPilot(vn);
        npsAP.declareXPathNameSpace("xml", "http://www.w3.org/XML/1998/namespace");
        npsAP.declareXPathNameSpace("nkjp", "http://www.nkjp.pl/ns/1.0");
        npsAP.selectXPath("//seg[@nkjp:nps='true']");
        while (npsAP.evalXPath() != -1) {
            npsList.add(vn.toString(vn.getAttrVal("xml:id")));
        }
        npsAP.resetXPath();
        return npsList;
    }

    private List<Text> parseMorphoSyntax(File f, String nkjpId, List<String> npsList) throws IOException, ParseException, XPathParseException, XPathEvalException, NavException,
          ParseExceptionHuge, XPathParseExceptionHuge, XPathEvalExceptionHuge, NavExceptionHuge {
        log.info("[{}] Parsing ann_morphosyntax.xml ({} bytes).", dtFmt.format(new Date()), f.length());
        FastTable<Text> texts = new FastTable<>();
        VTDGenHuge vg = new VTDGenHuge();
        vg.parseFile(f.getAbsolutePath(), true);
        VTDNavHuge vn = vg.getNav();
        AutoPilotHuge sentAP = new AutoPilotHuge(vn);
        AutoPilotHuge wordAP = new AutoPilotHuge(vn);
        AutoPilotHuge orthAP = new AutoPilotHuge(vn);
        AutoPilotHuge disambAP = new AutoPilotHuge(vn);
        sentAP.selectXPath("//text/body//s");
        wordAP.selectXPath("./seg");
        orthAP.selectXPath("./fs[@type='morph']/f[@name='orth']/string/text()");
        disambAP
              .selectXPath("./fs[@type='morph']/f[@name='disamb']//f[@name='interpretation']/string/text()");
        int orthInd = -1;
        int disambInd = -1;
        String curTextId = new String();
        Text text = null;
        Paragraph para = null;
        Sentence sent = null;
        Word word = null;
        Word lastWord = null;
        String orth = null;
        String disamb = null;

        try {
            text = new Text();
            texts.add(text);
            text.setFile(f.getName());
            text.setPath(f.getParent());
            text.setNkjpId(nkjpId);
            text.setXmlId(nkjpId);
            para = new Paragraph();
            para.setXmlId(nkjpId + "_1");
            while ((sentAP.evalXPath()) != -1) {
                sent = new Sentence();
                lastWord = null;
                sent.setXmlId((vn.toString(vn.getAttrVal("xml:id"))));
                while ((wordAP.evalXPath()) != -1) {
                    while ((orthInd = orthAP.evalXPath()) != -1) {
                        orth = vn.toString(orthInd);
                    }
                    orthAP.resetXPath();
                    while ((disambInd = disambAP.evalXPath()) != -1) {
                        disamb = vn.toString(disambInd);
                    }
                    disambAP.resetXPath();

                    // <seg corresp="ann_segmentation.xml#segm_p-1.99-seg"
                    // xml:id="morph_p-1.99-seg">
                    String segId = vn.toString(vn.getAttrVal("corresp")).substring(21);
                    lastWord = word;
                    word = new Word(vn.toString(vn.getAttrVal("xml:id")), orth, disamb);
//          if (npsList.contains(segId)
//                  && lastWord != null
//                  && (!word.getPosAlias().equals("punct") || word.getLemma().equals("-"))
//                  && (!lastWord.getPosAlias().equals("interp") || (!word.getLemma().equals("-") && lastWord.getLemma().equals("-")))
//                  )
//          {
//            sent.appendToLastWord(orth);
//          }
                    //"aglt"
//          if (npsList.contains(segId)
//                  && lastWord != null
//                  && word.getPosAlias().equals("aglt")
//                  )
//          {
//            sent.appendToLastWord(word);
//          } else {
//            sent.addWord(word);
//          }
                    sent.addWord(word);
                }
                wordAP.resetXPath();
                para.addSentence(sent);
            }
            sentAP.resetXPath();
            text.addParagraph(para);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return texts;
    }

}
