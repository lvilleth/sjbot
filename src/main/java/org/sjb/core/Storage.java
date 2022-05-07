package org.sjb.core;

import java.util.HashMap;
import java.util.Map;

public class Storage {

    private final Map<String, Map<String, Object>> memoryStorage;

    private Storage(){
        this.memoryStorage = new HashMap<>();
    }

    /**
     * @param id key
     * @return the element or null if doesnt exist
     */
    public Map<String, Object> get(String id){
        return this.memoryStorage.get(id);
    }

    public void set(String id,  Map<String, Object> value){
        this.memoryStorage.put(id, value);
    }

    public static Storage getInstance(){
        return InstanceHolder.instance;
    }

    private static class InstanceHolder {
        private static final Storage instance = new Storage();
    }

}
