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
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

/**
 * Plain csv writer
 * @author Artem Mironov
 */
public class CsvWriter implements Closeable, Flushable{
    protected CsvModel model;
    protected Writer writer;
    protected int rowNum = 1;
    protected int colNum = 1;
    private boolean alwaysEscape = false;

    public CsvWriter(Writer writer, CsvModel model) {
        this.writer = writer;
        this.model = model;
    }

    private static boolean containsAny(String val, char ... chars) {
        for(int i = 0; i<val.length(); i++) {
            for(char c : chars) {
                if(val.charAt(i) == c) return true;
            }
        }
        return false;
    }

    private void checkEscapeAndWriteCol(String val) throws IOException {
        if (alwaysEscape || containsAny(val, model.getQuoteChar(), model.getSeparatorChar(), '\n', '\r'))
            escapeAndWriteCol(val);
        else writer.write(val);
    }

    private void escapeAndWriteCol(String val) throws IOException {
        writer.append(model.getQuoteChar());
        writer.append(val.replace("\"", "\"\""));
        writer.append(model.getQuoteChar());
    }

    /**
     * Write row
     * @param row arrays with unescaped column values
     */
    public void write(String [] row) throws CsvException {
        colNum = 1;
        for(String col : row) {
            try {
                if(colNum != 1) writer.append(model.getSeparatorChar());
                checkEscapeAndWriteCol(col);
                colNum++;
            } catch (IOException e) {
                throw new CsvException(rowNum, colNum, e);
            }
        }
        try {
            writer.append(model.getEndOfLine());
        } catch (IOException e) {
            throw new CsvException(rowNum, colNum, e);
        }
    }

    public void writeComment(String comment) throws CsvException {
        try {
            writer.write(model.getCommentChar());
            writer.write(comment);
            writer.write(model.getEndOfLine());
        } catch (IOException e) {
            throw new CsvException(rowNum, colNum, e);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}
