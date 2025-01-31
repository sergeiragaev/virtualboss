package net.virtualboss.service.migration;

import com.linuxense.javadbf.DBFReader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Component
public class DBFReaderFactory {
    public DBFReader createReader(String dbfPath, String memoPath) throws FileNotFoundException {
        DBFReader reader = new DBFReader(new FileInputStream(dbfPath));
        if (memoPath != null) reader.setMemoFile(new File(memoPath));
        return reader;
    }
}
