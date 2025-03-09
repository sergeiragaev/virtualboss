package net.virtualboss.migration.processor;

import com.linuxense.javadbf.DBFRow;

public interface EntityProcessor {
    void process(DBFRow row);
}
