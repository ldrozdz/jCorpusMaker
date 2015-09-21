package pl.pelcra.nkjp.corpusmaker.tagged.store;

import pl.pelcra.nkjp.corpusmaker.tagged.model.Text;

import java.sql.SQLException;

public interface TextDAO {

    public abstract void putText(Text text) throws NoSuchUser, NoSuchText, SQLException;

    public static class NoSuchUser extends Exception {

        private static final long serialVersionUID = -4921593614912117288L;

        public NoSuchUser(String msg) {
            super(msg);
        }
    }

    public static class NoSuchText extends Exception {

        private static final long serialVersionUID = 1918476189974916715L;

        public NoSuchText(String msg) {
            super(msg);
        }
    }

}
