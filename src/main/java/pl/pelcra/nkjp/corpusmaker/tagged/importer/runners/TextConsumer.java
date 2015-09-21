package pl.pelcra.nkjp.corpusmaker.tagged.importer.runners;

import lombok.extern.slf4j.Slf4j;
import pl.pelcra.nkjp.corpusmaker.tagged.model.Text;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAO;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAO.NoSuchText;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAO.NoSuchUser;
import pl.pelcra.nkjp.corpusmaker.tagged.store.TextDAOTransactional;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class TextConsumer implements Runnable {
    private BlockingQueue<Text> textQ;
    private TextDAO textDAO;
    private String textNkjpId = new String();
    private int textTotalCnt = 1;

    public TextConsumer(BlockingQueue<Text> textQ) throws SQLException, PropertyVetoException {
        this.textQ = textQ;
        textDAO = new TextDAOTransactional();
    }

    public void run() {
        try {
            Text text;
            while (!(text = textQ.take()).isNull()) {
                if (!textNkjpId.equals(text.getNkjpId())) {
                    textNkjpId = text.getNkjpId();
                    log.info(
                          "[{}] Consumer: Writing texts {}/*. Queue length: {}. [{} total texts written].",
                          new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()), textNkjpId,
                          textQ.size(), textTotalCnt);
                }
                textDAO.putText(text);
                textTotalCnt++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoSuchUser e) {
            e.printStackTrace();
        } catch (NoSuchText e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        log.info("[{}] Consumer: Consumer finished working.", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
    }
}
