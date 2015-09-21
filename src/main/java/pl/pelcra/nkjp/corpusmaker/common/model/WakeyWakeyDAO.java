package pl.pelcra.nkjp.corpusmaker.common.model;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class WakeyWakeyDAO {

    Connection c;

    public WakeyWakeyDAO(Connection c) {
        this.c = c;
    }

    private PreparedStatement select1PS;

    public int select1(Connection c) {
        int one = 0;
        if (select1PS == null) {
            try {
                select1PS = c.prepareStatement("SELECT 1;");
            } catch (SQLException ex) {
                log.warn("{} thrown.", ex);
            }
        }
        try {
            ResultSet rs = select1PS.executeQuery();
            if (rs.next()) {
                one = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException ex) {
            log.warn("{} thrown.", ex);
        }

        return one;
    }

}
