package pl.pelcra.nkjp.corpusmaker.tagged.store;

import pl.pelcra.nkjp.corpusmaker.common.ConnectionManager;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Paragraph;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Sentence;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Text;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Word;

import java.sql.*;

// Alternative implementation without automatic transactions and with batch updates
// Any speedups yet to be observed
//TODO: doesn't work with multithreaded import
@SuppressWarnings("JpaQueryApiInspection")
public class TextDAOBatched implements TextDAO {

    public void putText(Text t) throws SQLException {
        final String putTextSQL = "INSERT INTO `text` "
              + "(`num`,`nkjp_id`,`xml_id`,`file`,`path`) "
              + "VALUES  (?,?,?,?,?)";
        final String putParagraphSQL = "INSERT INTO `paragraph` "
              + "(`num`,`text_num`,`xml_id`,`sequence`) "
              + "VALUES (?,?,?,?)";
        final String putSentenceSQL = "INSERT INTO `sentence` "
              + "(`num`,`text_num`,`paragraph_num`,`xml_id`,`sequence`) "
              + "VALUES (?,?,?,?,?)";
        final String putWordSQL = "INSERT INTO `word` "
              + "(`text_num`,`paragraph_num`,`sentence_num`,`xml_id`,"
              + "`sequence`,`word`,`pos`,`pos_class`,`pos_alias`,"
              + "`lemma`,`merged`) "
              + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        // Set up the scene for inserting
        try (Connection db = ConnectionManager.INSTANCE.getConnection()) {
            try (PreparedStatement putTextPS = db.prepareStatement(putTextSQL, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement putParagraphPS = db.prepareStatement(putParagraphSQL, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement putSentencePS = db.prepareStatement(putSentenceSQL, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement putWordPS = db.prepareStatement(putWordSQL, Statement.RETURN_GENERATED_KEYS);) {
                db.setAutoCommit(false);
                Integer pSeq, wSeq, sSeq;
                Integer tNum = 0, pNum = 0, sNum = 0;

                // get autoincrement values
                try (ResultSet rs = db.createStatement().executeQuery("SHOW TABLE STATUS;")) {
                    while (rs.next()) {
                        if (rs.getString("Name").equals("text")) {
                            tNum = rs.getInt("Auto_increment");
                        } else if (rs.getString("Name").equals("paragraph")) {
                            pNum = rs.getInt("Auto_increment");
                        } else if (rs.getString("Name").equals("sentence")) {
                            sNum = rs.getInt("Auto_increment");
                        }
                    }
                }

                // lock tables
                db.createStatement().executeUpdate("LOCK TABLES `text` WRITE, `paragraph` WRITE, `sentence` WRITE, `word` WRITE;");

                // insert texts/paragraphs/sentences/words
                t.setNum(tNum);
                putTextPS.setInt(1, t.getNum());
                putTextPS.setString(2, t.getNkjpId());
                putTextPS.setString(3, t.getXmlId());
                putTextPS.setString(4, t.getFile());
                putTextPS.setString(5, t.getPath());
                putTextPS.addBatch();
                tNum++;
                pSeq = 1;
                for (Paragraph p : t.getParagraphs()) {
                    p.setNum(pNum);
                    putParagraphPS.setInt(1, p.getNum());
                    putParagraphPS.setInt(2, t.getNum());
                    putParagraphPS.setString(3, p.getXmlId());
                    putParagraphPS.setInt(4, pSeq);
                    putParagraphPS.addBatch();
                    pNum++;
                    pSeq++;
                    sSeq = 1;
                    for (Sentence s : p.getSentences()) {
                        s.setNum(sNum);
                        putSentencePS.setInt(1, s.getNum());
                        putSentencePS.setInt(2, t.getNum());
                        putSentencePS.setInt(3, p.getNum());
                        putSentencePS.setString(4, s.getXmlId());
                        putSentencePS.setInt(5, sSeq);
                        putSentencePS.addBatch();
                        sNum++;
                        sSeq++;
                        wSeq = 1;
                        for (Word w : s.getWords()) {
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
                        }
                    }
                }
                putTextPS.executeBatch();
                putParagraphPS.executeBatch();
                putSentencePS.executeBatch();
                putWordPS.executeBatch();
                db.commit();
            } catch (Exception e) {
                if (db != null) {
                    db.rollback();
                }
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
