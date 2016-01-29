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
import org.jeesy.classinfo.converter.api.ConversionException;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Artem Mironov
 */
public class CsvBeanReader<T> extends CsvReader {
    private ClassInfo<T> classInfo;
    private boolean ignoreUnknownColumns = true;
    public CsvBeanReader(Class<T> beanType, Reader reader, String [] header, CsvModel model) throws CsvException {
        super(reader, model);
        classInfo = ClassInfoScanner.classInfo(beanType);
        if(header == null)
            this.header = classInfo.getIndex(CsvModel.CsvIndex.class).getHeader().toArray(new String[0]);

    }

    public CsvBeanReader(Class<T> beanType, Reader reader, boolean readHeader, CsvModel model) throws CsvException {
        super(reader, model);
        classInfo = ClassInfoScanner.classInfo(beanType);
        if(readHeader) header = readHeader();
        else header = classInfo.getIndex(CsvModel.CsvIndex.class).getHeader().toArray(new String[0]);

    }

    public void readBeans(final RowHandler<T> rowHandler) {
        while(readOne(rowHandler));
    }

    public T readBean() throws CsvException {
        SimpleRowHandler<T> t = new SimpleRowHandler<T>() {
            @Override
            public boolean onValue(int rowNum, T value) {
                return false;
            }
        };
        if(t.exception != null) throw t.exception;
        else return t.value;
    }

    public boolean readOne(final RowHandler<T> rowHandler) {
        final CsvModel.CsvIndex csvIndex = classInfo.getIndex(CsvModel.CsvIndex.class);
        final T instance;
        try {
            instance = classInfo.getType().newInstance();
        } catch (IllegalAccessException|InstantiationException e) {
            throw new RuntimeException(e);
        }
        boolean ret;
        try {
            ret = readRow(new ColumnProcessor() {
                @Override
                public boolean onValue(int rowNum, int colNum, String value) {
                    try {

                        String propertyName = csvIndex.getFieldNameByColumnName(header[colNum-1]);
                        if(propertyName == null) {
                            if(ignoreUnknownColumns) return true;
                            else return rowHandler.onError(new CsvException(rowNum, colNum, new RuntimeException("Property not found by header "+header[colNum-1])));
                        }
                        PropertyInfo pi = classInfo.getPropertyInfo(propertyName);
                        if(pi == null) {
                            return rowHandler.onError(new CsvException(rowNum, colNum, new RuntimeException("Property not found by name "+propertyName)));
                        }
                        Object val = model.getConverter().fromString(pi.getTypeInfo(), value);
                        pi.setValue(instance, val);

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
