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
import org.jeesy.classinfo.PropertyInfo;
import org.jeesy.classinfo.TypeInfo;
import org.jeesy.classinfo.converter.api.*;
import org.jeesy.classinfo.selector.PropertyHandle;
import org.jeesy.classinfo.selector.PropertySelector;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Artem Mironov
 */
public class CsvBeanReader<T> extends CsvReader {
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
            header = classInfo.getIndex(CsvIndex.class).getHeader();

    }

    public CsvBeanReader(Class<T> beanType, Reader reader, boolean useHeaderFromFile, CsvModel model) throws CsvException {
        super(reader, model);
        classInfo = ClassInfoScanner.classInfo(beanType);

        if(useHeaderFromFile) header = readRow();
        else header = classInfo.getIndex(CsvIndex.class).getHeader();
    }

    public void readBeans(final RowHandler<T> rowHandler) {
        while(readOne(rowHandler));
    }

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
                        PropertySelector selector = PropertySelector.parse(prop.getPath()).createNullElements();

                        PropertyHandle handle = null;
                        try {
                            handle = selector.resolve(instance);
                        } catch(Exception e) {
                            return rowHandler.onError(new CsvException(rowNum, colNum, e));
                        }


                        Converter<String, Object> converter = getModel().getConverter().converterFor(String.class, handle.getInfo().getType());
                        if(handle.getInfo().hasAnnotation(CsvCol.class) && !StringParser.class.equals(handle.getInfo().getAnnotation(CsvCol.class).parser())) {
                            StringParser<Object> parser = getModel().getConverter().converterByType(handle.getInfo().getAnnotation(CsvCol.class).parser());
                            if(parser != null) converter = parser;
                        }
                        if(converter == null) throw new RuntimeException("Cannot find converter from String to "+handle.getInfo().getType());
                        Object val = converter.convert(value, STRING_TYPE_INFO, handle.getInfo().getTypeInfo());
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
}
