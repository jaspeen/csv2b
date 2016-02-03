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
import org.jeesy.classinfo.converter.api.Converter;
import org.jeesy.classinfo.converter.api.StringSerializer;
import org.jeesy.classinfo.selector.PropertyHandle;
import org.jeesy.classinfo.selector.PropertySelector;

import java.io.Writer;

import static org.jeesy.classinfo.ClassInfoScanner.classInfo;

/**
 * Write beans to csv writer
 *
 * @author Artem Mironov
 */
public class CsvBeanWriter<T> extends CsvWriter {
    private Class<T> beanType;
    public CsvBeanWriter(Class<T> beanType, Writer writer, CsvModel model) {
        super(writer, model);
        this.beanType = beanType;
    }

    public void writeHeader() {
        ClassInfo<T> ci = classInfo(beanType);
        CsvIndex idx = ci.getIndex(CsvIndex.class);
        write(idx.getHeader());
    }

    public void writeBean(T bean) {
        write(beanToArray(bean));
    }

    public String [] beanToArray(T bean) {
        ClassInfo<T> ci = classInfo((Class<T>)bean.getClass());
        CsvIndex idx = ci.getIndex(CsvIndex.class);
        String [] header = idx.getHeader();
        String [] res = new String[header.length];
        for(int i = 0; i<res.length; i++) {
            PropertySelector selector = PropertySelector.parse(idx.getFieldNameByColumnName(header[i]).getPath());
            PropertyHandle handle = selector.resolve(bean);

            Object rawVal = handle.getValue();
            String val = toString(handle.getInfo(), rawVal);
            if(val == null) val = "";
            res[i] = val;
        }
        return res;
    }

    private static final TypeInfo<String> STRING_TYPE_INFO = TypeInfo.forClass(String.class);

    @SuppressWarnings("unchecked")
    private String toString(PropertyInfo pi, Object value) {
        Converter<Object, String> converter = model.getConverter().converterFor(pi.getType(), String.class);
        CsvCol csvCol = pi.getAnnotation(CsvCol.class);
        if(csvCol != null && !StringSerializer.class.equals(csvCol.serializer())) {
            converter = model.getConverter().converterByType(csvCol.serializer());
        }
        return converter.convert(value, pi.getTypeInfo(), STRING_TYPE_INFO);
    }
}
