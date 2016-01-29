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
import org.jeesy.classinfo.converter.DefaultConverter;
import org.jeesy.classinfo.converter.api.StringConverter;
import org.jeesy.classinfo.indexes.CommonClassIndex;
import org.jeesy.classinfo.indexes.PropertyIndex;

import java.io.IOException;
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
    private StringConverter converter = defaultConverter();

    public CsvModel(char separatorChar, char quoteChar, String endOfLine, boolean ignoreEmptyLines, StringConverter converter) {
        this.separatorChar = separatorChar;
        this.quoteChar = quoteChar;
        this.endOfLine = endOfLine;
        this.ignoreEmptyLines = ignoreEmptyLines;
        this.converter = converter;
    }

    public CsvReader newReader(Reader reader) throws IOException {
        return new CsvReader(reader, this);
    }

    public <T> CsvBeanReader<T> newBeanReader(Class<T> beanType, Reader reader, boolean readHeader) throws CsvException {
        return new CsvBeanReader<>(beanType, reader, readHeader, this);
    }

    public <T> CsvBeanReader<T> newBeanReader(Class<T> beanType, Reader reader, String [] header) throws CsvException {
        return new CsvBeanReader<>(beanType, reader, header, this);
    }

    public CsvWriter newWriter(Writer writer) throws IOException{
        return new CsvWriter(writer, this);
    }

    public <T> CsvBeanWriter<T> newBeanWriter(Class<T> beanType, Writer writer) throws IOException{
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

    public StringConverter getConverter() {
        return converter;
    }

    public static class CsvIndex implements PropertyIndex, CommonClassIndex {
        private List<String> header = new ArrayList<>();
        private Map<String, String> headerToFieldMap = new HashMap<>();
        private Set<String> required = new HashSet<>();

        @Override
        public void index(ClassInfo container, PropertyInfo propertyInfo) {
            CsvCol csvCol = propertyInfo.getAnnotation(CsvCol.class);
            String headerName = propertyInfo.getName();
            if(csvCol != null) {
                //TODO: support embedded pojos
                /*if(csvCol.embedded() && propertyInfo.getClassInfo().hasAnnotation(CsvRow.class)) {
                    for(Map.Entry<String, PropertyInfo> entry : (Set<Map.Entry>)propertyInfo.getClassInfo().getProperties().entrySet()) {
                        headerToFieldMap.put()
                    }
                }*/
                if(!csvCol.name().isEmpty()) headerName = csvCol.name();
                if(csvCol.required()) required.add(headerName);

            }
            headerToFieldMap.put(headerName, propertyInfo.getName());
        }

        public Set<String> getRequiredColumns() {
            return required;
        }

        public List<String> getHeader() {
            return header;
        }

        public String getFieldNameByColumnName(String name) {
            return headerToFieldMap.get(name);
        }

        public String getHeaderByPos(int pos) {
            return header.get(pos-1);
        }

        @Override
        public void index(ClassInfo<?> classInfo) {
            ClassInfo<?> parent = classInfo.getParent();
            if(parent != null) {
                header.addAll(parent.getIndex(CsvIndex.class).getHeader());
            }
            CsvRow row = classInfo.getAnnotation(CsvRow.class);
            if(row != null && row.names().length > 0) {
                header.addAll(Arrays.asList(row.names()));
            } else {
                header.addAll(headerToFieldMap.keySet());
            }
        }

        public static CsvIndex forClass(Class<?> type) {
            return classInfo(type).getIndex(CsvIndex.class);
        }
    }

    public final static CsvModel STANDARD = new CsvModel(',', '\"',"\r\n",true,defaultConverter());
}
