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
import org.jeesy.classinfo.TypeInfo;
import org.jeesy.classinfo.converter.api.ConversionException;
import org.jeesy.classinfo.converter.api.Converter;
import org.jeesy.classinfo.converter.api.StringSerializer;
import org.jeesy.classinfo.selector.PropertyHandle;
import org.jeesy.classinfo.selector.PropertySelector;

import java.io.Writer;

import static org.jeesy.classinfo.ClassInfoScanner.classInfo;

/**
 * Writer to serialize beans to csv.
 * Bean type passed to the constructor used only for type safety and to write a header.
 * Actual bean info gathered from passed object runtime types and should extend type T
 *
 * @author Artem Mironov
 */
public class CsvBeanWriter<T> extends CsvWriter {
    private Class<T> beanType;
    public CsvBeanWriter(Class<T> beanType, Writer writer, CsvModel model) {
        super(writer, model);
        this.beanType = beanType;
    }

    /**
     * Write header for bean type.
     * Use it before other write methods to produce csv header
     * @throws CsvException for any exception occurred
     */
    public void writeHeader() throws CsvException {
        ClassInfo<T> ci = classInfo(beanType);
        CsvIndex idx = ci.getIndex(CsvIndex.class);
        write(idx.getHeader());
    }

    /**
     * Write single bean to csv
     * @param bean bean to serialize
     * @throws CsvException for any exception occurred
     */
    public <B extends T> void writeBean(B bean) throws CsvException {
        write(beanToArray(bean));
    }

    /**
     * Write sequence of beans, each on own row
     * @param beans sequence of beans
     * @throws CsvException for any exception occurred
     */
    public <B extends T> void writeBeans(Iterable<B> beans) throws CsvException {
        for(B bean : beans) {
            writeBean(bean);
        }
    }

    /**
     * Serialize bean to string array
     */
    public <B extends T> String [] beanToArray(B bean) {
        ClassInfo<B> ci = classInfo((Class<B>)bean.getClass());
        CsvIndex idx = ci.getIndex(CsvIndex.class);
        String [] header = idx.getHeader();
        String [] res = new String[header.length];
        for(int i = 0; i<res.length; i++) {
            PropertySelector selector = PropertySelector.parse(idx.getFieldNameByColumnName(header[i]).getPath());
            PropertyHandle handle = selector.resolve(bean);
            if(handle == null) throw new IllegalStateException("Cannot find property by selector "+selector);
            Object rawVal = handle.getValue();
            String val = toString(handle.getInfo(), rawVal);
            if(val == null) val = "";
            res[i] = val;
        }
        return res;
    }

    private static final TypeInfo<String> STRING_TYPE_INFO = TypeInfo.forClass(String.class);

    @SuppressWarnings("unchecked")
    private String toString(PropertyInfo pi, Object value) throws CsvException {
        Converter<Object, String> converter = model.getConverter().converterFor(pi.getType(), String.class);
        CsvCol csvCol = pi.getAnnotation(CsvCol.class);
        if(csvCol != null && !StringSerializer.class.equals(csvCol.serializer())) {
            converter = model.getConverter().converterByType(csvCol.serializer());
        }
        if(converter == null) throw new IllegalStateException("Converter is null");
        try {
            return converter.convert(value, pi.getTypeInfo(), STRING_TYPE_INFO);
        } catch(ConversionException e) {
            throw new CsvException(rowNum, colNum, e);
        } catch(Exception e) {
            throw new CsvException(rowNum, colNum, e);
        }
    }
}
