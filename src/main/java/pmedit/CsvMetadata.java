package pmedit;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CsvMetadata {

    public static List<MetadataInfo> readFile(File filename) throws Exception {
        ArrayList<MetadataInfo> parsed = new ArrayList<MetadataInfo>();
        CSVReader reader = new CSVReaderBuilder(new FileReader(filename))
                .withCSVParser(new CSVParserBuilder()
                        .withEscapeChar(ICSVParser.NULL_CHARACTER)
                        .build())
                .build();
        List<String[]> entries = reader.readAll();
        reader.close();
        String[] header = entries.remove(0);
        for (int i = 0; i < header.length; ++i) {
            header[i] = header[i].trim();
        }
        if (!Arrays.asList(header).contains("file.fullPath")) {
            throw new Exception("The header must specify a 'file.fullPath' column");
        }
        for (String[] row : entries) {
            MetadataInfo metadata = new MetadataInfo();
            for (int idx = 0; idx < row.length; ++idx) {
                String id = header[idx];
                if (CommandLine.validMdNames.contains(id)) {
                    String value = row[idx].trim();
                    metadata.setAppendFromString(id, value);
                    metadata.setEnabled(id, true);
                }
            }
            parsed.add(metadata);
        }
        return parsed;
    }
}
