package pl.pelcra.nkjp.corpusmaker.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import pl.pelcra.nkjp.corpusmaker.common.enums.ProcessingModes;
import pl.pelcra.nkjp.corpusmaker.common.enums.XMLParsers;

@Slf4j
public enum ConfigProvider {
    INSTANCE;
    public static Configuration CONNECTION_PROPS;
    public static Configuration PARSING_PROPS;
    public static ProcessingModes MODE, THREADS;
    public static XMLParsers XML_PARSER;
    public static Integer POOL_SIZE = 1;

    public static void loadConfig(String configFile) {
        try {
            XMLConfiguration xmlConfig = new XMLConfiguration(configFile);
            CONNECTION_PROPS = new BaseConfiguration();
            PARSING_PROPS = new BaseConfiguration();
            CONNECTION_PROPS.addProperty("jdbcURL", xmlConfig.getString("jdbc.url"));
            CONNECTION_PROPS.addProperty("jdbcDriver", xmlConfig.getString("jdbc.driver"));
            CONNECTION_PROPS.addProperty("jdbcUser", xmlConfig.getString("jdbc.user"));
            CONNECTION_PROPS.addProperty("jdbcPassword", xmlConfig.getString("jdbc.password"));
            PARSING_PROPS.addProperty("textDir", xmlConfig.getString("parse.dir"));
            try {
                MODE = ProcessingModes.getEnum(xmlConfig.getString("mode"));
                XML_PARSER = XMLParsers.getEnum(xmlConfig.getString("parse.parser"));
                THREADS = ProcessingModes.getEnum(xmlConfig.getString("threading"));
                POOL_SIZE = xmlConfig.getInt("pool_size");
            } catch (IllegalArgumentException e) {
                log.warn("{} thrown.", e);
            }
        } catch (ConfigurationException cex) {
            log.warn("{} thrown.", cex);
        }
    }

}
