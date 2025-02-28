package net.virtualboss.migration.service;

import com.linuxense.javadbf.DBFRow;

public interface EntityProcessor {
    void process(DBFRow row);
}
