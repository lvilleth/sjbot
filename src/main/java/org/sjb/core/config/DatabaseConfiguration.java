package org.sjb.core.config;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import org.sjb.core.utils.FileUtils;

import java.util.Properties;

@Data
@Builder(builderMethodName = "hiddenBuilder", access = AccessLevel.PRIVATE)
public class DatabaseConfiguration {

    private String name;
    private String username;
    private String password;
    private String url;
    private String action;
    private String persistenceUnit;

    public static DatabaseConfiguration build(Properties properties){
        String name = properties.getProperty("db.name", "sjbot");
        String pUnit = properties.getProperty("db.persistence-unit");
        String urlAdditionalConfig = ";database_to_upper=false;IFEXISTS=TRUE;AUTO_SERVER=TRUE";
        if(!pUnit.equals("h2")){
            urlAdditionalConfig = "";
        }
        String url = "jdbc:" + pUnit + ":" + FileUtils.getInstallDirAbsolutePath() + "\\" + name + urlAdditionalConfig;

        return DatabaseConfiguration.hiddenBuilder()
                .action(properties.getProperty("db.action", "none"))
                .name(name)
                .username(properties.getProperty("db.username"))
                .password(properties.getProperty("db.password"))
                .persistenceUnit(pUnit)
                .url(url)
                .build();
    }

}
