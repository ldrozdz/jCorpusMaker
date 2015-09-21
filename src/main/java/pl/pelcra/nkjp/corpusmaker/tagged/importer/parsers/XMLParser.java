package pl.pelcra.nkjp.corpusmaker.tagged.importer.parsers;

import pl.pelcra.nkjp.corpusmaker.tagged.model.Text;

import java.io.File;
import java.util.List;

public interface XMLParser {
    public abstract List<Text> parseXMLDir(File dir) throws Exception;

}
