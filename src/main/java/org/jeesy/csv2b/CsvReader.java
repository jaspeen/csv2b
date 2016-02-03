/*
 * Copyright 2015 Artem Mironov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jeesy.csv2b;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain csv reader
 * @author Artem Mironov
 */
public class CsvReader implements Closeable {
    private final Reader reader;
    private final CsvModel model;

    protected CsvReader(Reader reader, CsvModel model) {
        this.reader = reader;
        this.model = model;
    }


    protected int rowNum = 0;
    protected int colNum = 0;
    private boolean wasCR = false;

    public interface ColumnProcessor {
        boolean onValue(int rowNum, int colNum, String value);
    }

    public class ArrayColumnProcessor implements ColumnProcessor {
        private List<String> data = new ArrayList<>();

        @Override
        public boolean onValue(int rowNum, int colNum, String value) {
            data.add(value);
            return true;
        }

        public List<String> getData() {
            return data;
        }

        public String [] toArray() {
            return data.toArray(new String[data.size()]);
        }
    }

    public interface RowHandler<T> {
        boolean onError(CsvException e);
        boolean onValue(int rowNum, T value);
    }

    public static class ListRowHandler implements RowHandler<String[]> {
        private List<String[]> rows = new ArrayList<>();
        private int lastRowNum = 0;
        @Override
        public boolean onError(CsvException e) {
            throw e;
        }

        @Override
        public boolean onValue(int rowNum, String[] value) {
            rows.add(value);
            lastRowNum = rowNum;
            return true;
        }

        public List<String[]> getRows() {
            return rows;
        }

        public int getLastRowNum() {
            return lastRowNum;
        }
    }

    protected class SimpleRowHandler<T> implements RowHandler<T> {
        CsvException exception;
        T value;
        @Override
        public boolean onError(CsvException e) {
            this.exception = e;
            return false;
        }

        @Override
        public boolean onValue(int rowNum, T value) {
            this.value = value;
            return true;
        }
    }

    /**
     * Return true if it reported any value
     * @param processor column processor what will be called on each column value
     * @return
     * @throws IOException
     */
    protected boolean realReadRow(ColumnProcessor processor) throws IOException {
        int val = reader.read();
        if(val == -1) return false; else rowNum++;
        StringBuilder out = new StringBuilder();
        boolean inQuote = false;
        colNum = 0;
        boolean delayedQuote = false;
        boolean anyValue = false;
        while(val != -1) {

            char charVal = (char) val;
            if(inQuote) {
                if(model.getQuoteChar() == charVal) {
                    delayedQuote = true;
                    inQuote = false;
                } else {
                    out.append(charVal);
                }
            } else {
                if(charVal == model.getQuoteChar()) {
                  if(delayedQuote) {
                      out.append(charVal);
                      inQuote = true;

                  } else {
                      inQuote = true;
                  }
                } else if(charVal == model.getSeparatorChar()) {
                    if(!processor.onValue(rowNum, ++colNum, out.toString())) return false;
                    out.setLength(0);
                    anyValue = true;
                } else if(charVal == '\n') {
                    if(wasCR) {
                        wasCR = false;
                    } else {
                        if(colNum > 0 || out.length() > 0 || !model.isIgnoreEmptyLines())
                            break;
                    }
                } else if(charVal == '\r') {
                    wasCR = true;
                    if(colNum > 0 || out.length() > 0  || !model.isIgnoreEmptyLines())
                        break ;
                } else {
                    out.append(charVal);
                }
                delayedQuote = false;
            }
            val = reader.read();
        }
        if(inQuote) throw new RuntimeException("Unfinished quote");
        boolean ret = processor.onValue(rowNum, ++colNum, out.toString());
        return ret && (anyValue || out.length() > 0);
    }

    public <T extends RowHandler<String[]>> T read(T rowHandler) throws CsvException {
        while(readRow(rowHandler));
        return rowHandler;
    }

    public String [] readRow() throws CsvException {
        try {
            ArrayColumnProcessor lcp = new ArrayColumnProcessor();
            realReadRow(lcp);
            return lcp.toArray();
        } catch (IOException e) {
            throw new CsvException(rowNum, colNum, e);
        }
    }

    public boolean readRow(RowHandler<String[]> rowHandler) {
        final List<String> res = new ArrayList<>();
        boolean ret;
        try {
            ret = realReadRow(new ColumnProcessor() {
                @Override
                public boolean onValue(int rowNum, int colNum, String value) {
                    res.add(value);
                    return true;
                }
            });
        } catch (IOException e) {
            rowHandler.onError(new CsvException(rowNum, colNum, e));
            return false;
        }
        return ret && rowHandler.onValue(rowNum, res.toArray(new String[res.size()]));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public CsvModel getModel() {
        return model;
    }
}
