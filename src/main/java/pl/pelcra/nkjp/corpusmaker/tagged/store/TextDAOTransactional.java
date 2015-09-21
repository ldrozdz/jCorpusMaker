package pl.pelcra.nkjp.corpusmaker.tagged.store;

import pl.pelcra.nkjp.corpusmaker.common.ConnectionManager;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Paragraph;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Sentence;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Text;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Word;

import java.sql.*;

@SuppressWarnings("JpaQueryApiInspection")
public class TextDAOTransactional implements TextDAO {

    public void putText(Text t) throws SQLException {
        final String putTextSQL = "INSERT INTO `text` "
              + "(`nkjp_id`,`xml_id`,`file`,`path`) "
              + "VALUES  (?,?,?,?);";
        final String putParagraphSQL = "INSERT INTO `paragraph` "
              + "(`text_num`,`xml_id`,`sequence`) "
              + "VALUES (?,?,?);";
        final String putSentenceSQL = "INSERT INTO `sentence` "
              + "(`text_num`,`paragraph_num`,`xml_id`,`sequence`) "
              + "VALUES (?,?,?,?);";
        final String putWordSQL = "INSERT INTO `word` "
              + "(`text_num`,`paragraph_num`,`sentence_num`,`xml_id`,"
              + "`sequence`,`word`,`pos`,`pos_class`,`pos_alias`,"
              + "`lemma`, `merged`) "
              + "VALUES (?,?,?,?,?,?,?,?,?,?,?);";
        // Set up the scene for inserting
        try (Connection db = ConnectionManager.INSTANCE.getConnection();
             PreparedStatement putTextPS = db.prepareStatement(putTextSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement putParagraphPS = db.prepareStatement(putParagraphSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement putSentencePS = db.prepareStatement(putSentenceSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement putWordPS = db.prepareStatement(putWordSQL, Statement.RETURN_GENERATED_KEYS);) {
            Integer pSeq, wSeq, sSeq;

            // insert texts/paragraphs/sentences/words

            try {
                putTextPS.setString(1, t.getNkjpId());
                putTextPS.setString(2, t.getXmlId());
                putTextPS.setString(3, t.getFile());
                putTextPS.setString(4, t.getPath());
                putTextPS.executeUpdate();
                try (ResultSet textNumRS = putTextPS.getGeneratedKeys()) {
                    textNumRS.next();
                    t.setNum(textNumRS.getInt(1));
                    pSeq = 1;
                    for (Paragraph p : t.getParagraphs()) {
                        try {
                            putParagraphPS.setInt(1, t.getNum());
                            putParagraphPS.setString(2, p.getXmlId());
                            putParagraphPS.setInt(3, pSeq);
                            putParagraphPS.executeUpdate();
                            try (ResultSet paragraphNumRS = putParagraphPS.getGeneratedKeys()) {
                                paragraphNumRS.next();
                                p.setNum(paragraphNumRS.getInt(1));
                                pSeq++;
                                sSeq = 1;
                                for (Sentence s : p.getSentences()) {
                                    try {
                                        putSentencePS.setInt(1, t.getNum());
                                        putSentencePS.setInt(2, p.getNum());
                                        putSentencePS.setString(3, s.getXmlId());
                                        putSentencePS.setInt(4, sSeq);
                                        putSentencePS.executeUpdate();
                                        try (ResultSet sentenceNumRS = putSentencePS.getGeneratedKeys()) {
                                            sentenceNumRS.next();
                                            s.setNum(sentenceNumRS.getInt(1));
                                            sSeq++;
                                            wSeq = 1;
                                            for (Word w : s.getWords()) {
                                                try {
                                                    putWordPS.setInt(1, t.getNum());
                                                    putWordPS.setInt(2, p.getNum());
                                                    putWordPS.setInt(3, s.getNum());
                                                    putWordPS.setString(4, w.getXmlId());
                                                    putWordPS.setInt(5, wSeq);
                                                    putWordPS.setString(6, w.getWord());
                                                    putWordPS.setString(7, w.getPos());
                                                    putWordPS.setString(8, w.getPosClass());
                                                    putWordPS.setString(9, w.getPosAlias());
                                                    putWordPS.setString(10, w.getLemma());
                                                    putWordPS.setBoolean(11, w.isMerged());
                                                    putWordPS.executeUpdate();
                                                    wSeq++;
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
