package pl.pelcra.nkjp.corpusmaker.tagged.importer.parsers;

import com.ximpleware.*;
import com.ximpleware.NavException;
import com.ximpleware.extended.*;
import javolution.util.FastMap;
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
import java.util.Map;

@Slf4j
public class VTDParser implements XMLParser {
    private final SimpleDateFormat dtFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    @Override
    public FastTable<Text> parseXMLDir(File dir) throws
          ParseException, XPathParseException, XPathEvalException, NavException, IOException,
          ParseExceptionHuge, XPathParseExceptionHuge, XPathEvalExceptionHuge, NavExceptionHuge {
        // sets nkjpId
        String nkjpId = parseHeader(new File(dir.getAbsolutePath() + "/header.xml"));
        // sets textParaMap
        Map<String, String> textParaMap = parseTextStructure(new File(dir.getAbsolutePath() + "/text_structure.xml"));
        // sets npsList
        List<String> npsList = parseSegmentation(new File(dir.getAbsolutePath() + "/ann_segmentation.xml"));
        // sets texts
        FastTable<Text> texts = parseMorphoSyntax(new File(dir.getAbsolutePath() + "/ann_morphosyntax.xml"), nkjpId, textParaMap, npsList);
        return texts;
    }


    private String parseHeader(File f) throws IOException, ParseException, XPathParseException, XPathEvalException, NavException {
        log.info("[{}] Parsing header.xml (%,d bytes).", dtFmt.format(new Date()), f.length());
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

    private Map<String, String> parseTextStructure(File f) throws IOException, ParseException, XPathParseException, XPathEvalException, NavException {
        log.info("[{}] Parsing text_structure.xml ({} bytes).", dtFmt.format(new Date()), f.length());
        FastMap<String, String> textParaMap = new FastMap<>();
        VTDGen vg = new VTDGen();
        vg.parseFile(f.getAbsolutePath(), true);
        VTDNav vn = vg.getNav();
        AutoPilot textAP = new AutoPilot(vn);
        AutoPilot paraAP = new AutoPilot(vn);
        textAP.declareXPathNameSpace("xml", "http://www.w3.org/XML/1998/namespace");
        // idAP.selectXPath("/teiHeader/@xml:lang");
        textAP.selectXPath("/teiCorpus/TEI/text/group/text");
        if (textAP.evalXPath() == -1) {
            textAP.selectXPath("/teiCorpus/TEI/text");
        } else {
            textAP.resetXPath();
        }
        paraAP.selectXPath(".//*");
        int textInd = -1;
        int paraInd = -1;
        // Iterate over texts
        while (textAP.evalXPath() != -1) {
            // Get text ID
            textInd = vn.getAttrVal("xml:id");

            // Get para ids for the current text
            while (paraAP.evalXPath() != -1) {
                paraInd = vn.getAttrVal("xml:id");

                if ((paraInd != -1)) {
                    if (textInd != -1) {
                        textParaMap.put(vn.toString(paraInd), vn.toString(textInd));
                    } else {
                        textParaMap.put(vn.toString(paraInd), "text");
                    }
                }
            }
            paraAP.resetXPath();
        }
        textAP.resetXPath();
        return textParaMap;
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

    private FastTable<Text> parseMorphoSyntax(File f, String nkjpId, Map<String, String> textParaMap, List<String> npsList) throws IOException, ParseException, XPathParseException, XPathEvalException, NavException,
          ParseExceptionHuge, XPathParseExceptionHuge, XPathEvalExceptionHuge, NavExceptionHuge {
        log.info("[{}] Parsing ann_morphosyntax.xml ({} bytes).", dtFmt.format(new Date()), f.length());
        FastTable<Text> texts = new FastTable<>();
        VTDGenHuge vg = new VTDGenHuge();
        vg.parseFile(f.getAbsolutePath(), true);
        VTDNavHuge vn = vg.getNav();
        AutoPilotHuge paraAP = new AutoPilotHuge(vn);
        AutoPilotHuge sentAP = new AutoPilotHuge(vn);
        AutoPilotHuge wordAP = new AutoPilotHuge(vn);
        AutoPilotHuge orthAP = new AutoPilotHuge(vn);
        AutoPilotHuge disambAP = new AutoPilotHuge(vn);
        paraAP.selectXPath("//text/body/p");
        sentAP.selectXPath("./s");
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

        while ((paraAP.evalXPath()) != -1) {
            para = new Paragraph();
            para.setXmlId(vn.toString(vn.getAttrVal("xml:id")));
            try {
                if (!curTextId.equals(textParaMap.get(para.getXmlId()))) {
                    curTextId = textParaMap.get(para.getXmlId());
                    text = new Text();
                    texts.add(text);
                    text.setFile(f.getName());
                    text.setPath(f.getParent());
                    text.setNkjpId(nkjpId);
                    text.setXmlId(curTextId);
                }
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
                        if (npsList.contains(segId)
                              && (!word.getPosAlias().equals("interp") || word.getLemma().equals("-"))
                              && lastWord != null
                              && (!lastWord.getPosAlias().equals("interp") || (!word.getLemma().equals("-") && lastWord
                              .getLemma().equals("-")))) {
                            sent.appendToLastWord(word);
                        } else {
                            sent.addWord(word);
                        }
                    }
                    wordAP.resetXPath();
                    para.addSentence(sent);
                }
                sentAP.resetXPath();
                text.addParagraph(para);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return texts;
    }

}
