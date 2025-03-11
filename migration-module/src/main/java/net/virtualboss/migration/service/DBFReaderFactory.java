package net.virtualboss.migration.service;

import com.linuxense.javadbf.DBFReader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Component
public class DBFReaderFactory {
    public DBFReader createReader(String dbfPath, String memoPath) throws FileNotFoundException {
        try {
            DBFReader reader = new DBFReader(new FileInputStream(dbfPath));
            if (memoPath != null) reader.setMemoFile(new File(memoPath));
            return reader;
        } catch (FileNotFoundException e) {
            DBFReader reader = new DBFReader(new FileInputStream(dbfPath.toUpperCase()));
            if (memoPath != null) reader.setMemoFile(new File(memoPath.toUpperCase()));
            return reader;
        }
    }
}
