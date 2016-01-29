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
    protected String[] header;
    protected CsvModel model;
    protected Writer writer;
    protected int rowNum = 1;
    protected int colNum = 1;

    public CsvWriter(Writer writer, CsvModel model) {
        this.writer = writer;
        this.model = model;
    }


    public CsvWriter(String [] header, Writer writer, CsvModel model) {
        this.writer = writer;
        this.model = model;
        this.header = header;
    }

    private void escapeAndWrite(String val) throws IOException {
        //if(val.indexOf(model.getQuoteChar()) >= 0 || val.indexOf(model.get))
        writer.append(model.getQuoteChar());
        writer.append(val.replace("\"","\"\""));
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
                escapeAndWrite(col);
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

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}
