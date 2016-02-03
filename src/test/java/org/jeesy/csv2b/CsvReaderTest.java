package org.jeesy.csv2b;

import org.jeesy.classinfo.PropertyInfo;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.jeesy.classinfo.ClassInfoScanner.classInfo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Artem Mironov
 */
public class CsvReaderTest {
    public static void assertRow(String [] row, String ... expected) {
        assertArrayEquals(expected, row);
    }

    @Test
    public void testRead() throws IOException {
        try(CsvReader reader = CsvModel.STANDARD.newReader(
                new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("test.csv"), "UTF-8"))) {
            CsvReader.ListRowHandler lrh = reader.read(new CsvReader.ListRowHandler());
            List<String[]> rows = lrh.getRows();
            assertRow(rows.get(0), "Year", "Make", "Model", "Description", "Price");
            assertRow(rows.get(1), "1997", "Ford", "E350", "ac, abs, moon", "3000.00");
            assertRow(rows.get(2), "1999", "Chevy", "Venture \"Extended Edition\"", "", "4900.00");
            assertRow(rows.get(3), "1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00");
            assertRow(rows.get(4), "1999", "Chevy", "Venture \"Extended Edition, Very Large\"", "", "5000.00");
            assertRow(rows.get(5), "", "", "Venture \"Extended Edition\"", "", "4900.00");
            assertEquals(6, lrh.getLastRowNum());
        }
    }
}
