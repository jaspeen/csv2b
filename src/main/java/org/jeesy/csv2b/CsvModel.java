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

import org.jeesy.classinfo.ClassInfo;
import org.jeesy.classinfo.PropertyInfo;
import org.jeesy.classinfo.converter.api.ConversionService;
import org.jeesy.classinfo.indexes.CommonClassIndex;
import org.jeesy.classinfo.indexes.PropertyIndex;

import java.io.Reader;
import java.io.Writer;
import java.util.*;

import static org.jeesy.classinfo.ClassInfoScanner.classInfo;
import static org.jeesy.classinfo.converter.DefaultConverter.defaultConverter;

/**
 * @author Artem Mironov
 */
public class CsvModel {
    private char separatorChar = ',';
    private char quoteChar = '"';
    private String endOfLine = "\r\n";
    private boolean ignoreEmptyLines = true;
    private char commentChar = '#';
    private ConversionService converter = defaultConverter();

    public CsvModel(char separatorChar, char quoteChar, String endOfLine, boolean ignoreEmptyLines, char commentChar, ConversionService converter) {
        this.separatorChar = separatorChar;
        this.quoteChar = quoteChar;
        this.endOfLine = endOfLine;
        this.ignoreEmptyLines = ignoreEmptyLines;
        this.commentChar = commentChar;
        this.converter = converter;
    }

    public CsvReader newReader(Reader reader) {
        return new CsvReader(reader, this);
    }

    public <T> CsvBeanReader<T> newBeanReader(Class<T> beanType, Reader reader, boolean readHeader) throws CsvException {
        return new CsvBeanReader<>(beanType, reader, readHeader, this);
    }

    public <T> CsvBeanReader<T> newBeanReader(Class<T> beanType, Reader reader, String [] header) throws CsvException {
        return new CsvBeanReader<>(beanType, reader, header, this);
    }

    public CsvWriter newWriter(Writer writer) {
        return new CsvWriter(writer, this);
    }

    public <T> CsvBeanWriter<T> newBeanWriter(Class<T> beanType, Writer writer) {
        return new CsvBeanWriter<>(beanType, writer, this);
    }

    public char getSeparatorChar() {
        return separatorChar;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public String getEndOfLine() {
        return endOfLine;
    }

    public boolean isIgnoreEmptyLines() {
        return ignoreEmptyLines;
    }

    public char getCommentChar() {
        return commentChar;
    }

    public ConversionService getConverter() {
        return converter;
    }

    public final static CsvModel STANDARD = new CsvModel(',','\"',"\r\n",true,'#', defaultConverter());
}
