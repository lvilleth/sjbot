package org.sjb.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;
import static java.util.Objects.*;

import static org.sjb.core.utils.Constants.DEFAULT;
import static org.sjb.core.utils.Constants.PROPERTIES_FILENAME;

public abstract class FileUtils {

    private FileUtils(){}

    public static File readFromInstallDir(String filename) throws FileNotFoundException {
        File f = Paths.get("").toAbsolutePath().resolve(filename).toFile();
        if(!f.exists() || !f.canRead()){
            throw new FileNotFoundException(String.format("File '%s' not found", filename));
        }
        return f;
    }

    public static Properties readProperties(){
        return readProperties(DEFAULT);
    }

    public static Properties readProperties(String profile){
        if(isNull(profile))
            return readProperties(DEFAULT);
        Properties props = new Properties();
        String resourceName = profile.equalsIgnoreCase(DEFAULT) ? PROPERTIES_FILENAME : profile +"."+PROPERTIES_FILENAME;
        try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)){
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public static String getInstallDirAbsolutePath(){
        return Paths.get("").toAbsolutePath().toString();
    }

}
