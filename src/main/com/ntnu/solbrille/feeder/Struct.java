package com.ntnu.solbrille.feeder;

import org.htmlparser.util.Translate;

import java.util.*;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class Struct {

    Object value;
    private Map<String, List<Struct>> structs;

    public Struct() {
        this.value = null;
    }
    
    public Struct(Object value) {
        this.value = value;
    }

    public void addField(String key,Struct value) {
        if(!getStructs().containsKey(key)) {
            getStructs().put(key, new ArrayList<Struct>());
        }
        getStructs().get(key).add(value);
    }


    public void addField(String key,Object value) {
        addField(key, new Struct(value));
    }


    public void setField(String key,Object value) {
        setField(key,new Struct(value));
    }

    public void setField(String key,Struct struct) {
        //TODO: Should clone
        ArrayList<Struct> list = new ArrayList<Struct>();
        list.add(struct);
        getStructs().put(key,list);
    }

    public void setFields(String key,List<Struct> list) {
        //TODO: Should clone
        getStructs().put(key,list);
    }

    public void clearField(String key) {
        getStructs().remove(key);
    }

    public Struct getField(String key) {
        List<Struct> list = getStructs().get(key);
        if(list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }

    }

    public Set<String> getKeys() {
        return getStructs().keySet();
    }

    public List<Struct> getFields(String key) {
        List<Struct> list = getStructs().get(key);
        return list;
    }

    public Object getValue() {
        return value;
    }

    public void encodeXml(StringBuilder sb) {
        if(value != null) {
            sb.append(Translate.encode(value.toString()));
        }
        
        if(getStructs() != null) {
            for(Map.Entry<String,List<Struct>> entry: getStructs().entrySet() ) {
                if(entry.getValue() != null) {
                    for(Struct s:entry.getValue()) {
                        sb.append("<" + entry.getKey() + ">\n");
                        s.encodeXml(sb);
                        sb.append("\n</" + entry.getKey() + ">\n");
                    }
                }
            }
        }


    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        encodeXml(sb);
        return sb.toString();
    }

    public Map<String, List<Struct>> getStructs() {
        if(structs == null)
            structs = new HashMap<String,List<Struct>>();
        return structs;
    }
}
