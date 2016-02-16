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
import org.jeesy.classinfo.ClassInfoScanner;
import org.jeesy.classinfo.TypeInfo;
import org.jeesy.classinfo.converter.api.ConversionException;
import org.jeesy.classinfo.converter.api.Converter;
import org.jeesy.classinfo.converter.api.StringParser;
import org.jeesy.classinfo.selector.PropertyHandle;
import org.jeesy.classinfo.selector.PropertySelector;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

/**
 * Read beans from csv stream
 * @author Artem Mironov
 */
public class CsvBeanReader<T> extends CsvReader implements Iterable<T> {
    private ClassInfo<T> classInfo;
    private String [] header;
    private boolean ignoreUnknownColumns = true;
    //when converter returns null for specified field it will be untouched
    //this allow to hold defaults in bean even when empty column specified in csv
    private boolean skipSettingNullValues = true;
    private boolean strictSize = false;
    public CsvBeanReader(Class<T> beanType, Reader reader, String [] header, CsvModel model) throws CsvException {
        super(reader, model);
        classInfo = ClassInfoScanner.classInfo(beanType);

        if(header == null)
            this.header = classInfo.getIndex(CsvIndex.class).getHeader();

    }

    public CsvBeanReader(Class<T> beanType, Reader reader, boolean useHeaderFromFile, CsvModel model) throws CsvException {
        super(reader, model);
        classInfo = ClassInfoScanner.classInfo(beanType);

        if(useHeaderFromFile) header = readRow();
        else header = classInfo.getIndex(CsvIndex.class).getHeader();
    }


    /**
     * Read beans till row handler returns true in onValue and onError or IOException occurred
     * @param rowHandler handler to accept values and errors
     * @return rowHandler parameter
     */
    public RowHandler<T> readBeans(final RowHandler<T> rowHandler) {
        while(readOne(rowHandler));
        return rowHandler;
    }

    /**
     * Read single bean from csv stream
     * @return new instance of type T filled with values from csv row
     * @throws CsvException if any error occurred
     */
    public T readBean() throws CsvException {
        SimpleRowHandler<T> t = new SimpleRowHandler<T>() {
            @Override
            public boolean onValue(int rowNum, T value) {
                super.onValue(rowNum, value);
                return false;
            }
        };
        readOne(t);
        if(t.exception != null) throw t.exception;
        else return t.value;
    }

    private static final TypeInfo<String> STRING_TYPE_INFO = TypeInfo.forClass(String.class);

    /**
     * Read bean from csv stream passing values and errors to rowHandler
     * @param rowHandler to accept errors and constructed bean instance
     * @return true if some data filled without errors
     */
    public boolean readOne(final RowHandler<T> rowHandler) {
        final CsvIndex csvIndex = classInfo.getIndex(CsvIndex.class);
        final T instance;
        try {
            instance = classInfo.getType().newInstance();
        } catch (IllegalAccessException|InstantiationException e) {
            throw new RuntimeException(e);
        }
        boolean ret;
        try {
            ret = realReadRow(new ColumnProcessor() {
                @SuppressWarnings("unchecked")
                @Override
                public boolean onValue(int rowNum, int colNum, String value) {
                    try {
                        if(colNum > header.length) throw new RuntimeException("Too much columns");
                        String columnName = header[colNum-1];
                        CsvIndex.CsvProp prop = csvIndex.getFieldNameByColumnName(columnName);
                        if(prop == null) {
                            return ignoreUnknownColumns || rowHandler.onError(new CsvException(rowNum, colNum, new RuntimeException("Property not found by header " + columnName)));
                        }
                        String propertyName = prop.getPath();
                        PropertySelector selector = PropertySelector.parse(propertyName).createNullElements();

                        PropertyHandle handle = null;
                        try {
                            handle = selector.resolve(instance);
                        } catch(Exception e) {
                            return rowHandler.onError(new CsvException(rowNum, colNum, e));
                        }


                        Converter<String, Object> converter = getModel().getConverter().converterFor(String.class, handle.getInfo().getType());
                        CsvCol csvCol = handle.getInfo().getAnnotation(CsvCol.class);
                        if(csvCol != null && !StringParser.class.equals(csvCol.parser())) {
                            StringParser<Object> parser = getModel().getConverter().converterByType(csvCol.parser());
                            if(parser != null) converter = parser;
                        }
                        if(converter == null) throw new ConversionException(value, STRING_TYPE_INFO, handle.getInfo().getTypeInfo(), "Cannot find converter from String to ");
                        if(value != null && value.isEmpty() && (csvCol == null || csvCol.nullIfEmpty())) {
                            value = null;
                        }
                        Object val = converter.convert(value, STRING_TYPE_INFO, handle.getInfo().getTypeInfo());
                        if(val != null || !skipSettingNullValues)
                            handle.setValue(val);

                        return true;
                    } catch(ConversionException e) {
                        return rowHandler.onError(new CsvException(rowNum, colNum, e));
                    } catch(Exception e) {
                        return rowHandler.onError(new CsvException(rowNum, colNum, e));
                    }
                }
            });
        } catch (IOException e) {
            rowHandler.onError(new CsvException(rowNum-1, colNum, e));
            return false;
        }
        return ret && rowHandler.onValue(rowNum-1, instance);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                return readBean();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported here");
            }
        };
    }
}
