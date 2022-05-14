package org.sjb.core.config;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

import java.util.Properties;


@Data
@Builder(builderMethodName = "hiddenBuilder", access = AccessLevel.PRIVATE)
public class AppConfiguration {

    private String config;
    private String profile;
    private DatabaseConfiguration db;

    public static AppConfiguration build(Properties properties){
        DatabaseConfiguration db = DatabaseConfiguration.build(properties);
        return AppConfiguration.hiddenBuilder()
                .config(properties.getProperty("config"))
                .profile(properties.getProperty("profile"))
                .db(db)
                .build();
    }


}
