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

import java.io.Writer;
import java.util.List;

import static org.jeesy.classinfo.ClassInfoScanner.classInfo;

/**
 * Write beans to csv writer
 *
 * @author Artem Mironov
 */
//TODO: too many object copy here
public class CsvBeanWriter<T> extends CsvWriter {
    private Class<T> beanType;
    public CsvBeanWriter(Class<T> beanType, Writer writer, CsvModel model) {
        super(writer, model);
        this.beanType = beanType;
    }

    public void writeHeader() {
        ClassInfo<T> ci = classInfo(beanType);
        CsvModel.CsvIndex idx = ci.getIndex(CsvModel.CsvIndex.class);
        write(idx.getHeader().toArray(new String[idx.getHeader().size()]));
    }

    public void writeBean(T bean) {
        write(beanToArray(bean));
    }

    protected String [] beanToArray(T bean) {
        ClassInfo<T> ci = classInfo((Class<T>)bean.getClass());
        CsvModel.CsvIndex idx = ci.getIndex(CsvModel.CsvIndex.class);
        List<String> header = idx.getHeader();
        String [] res = new String[header.size()];
        for(int i = 0; i<res.length; i++) {
            PropertyInfo pi = ci.getPropertyInfo(idx.getFieldNameByColumnName(header.get(i)));
            if(pi == null) throw new RuntimeException("Wrong header");
            Object rawVal = pi.getValue(bean);
            String val = model.getConverter().toString(pi.getTypeInfo(), rawVal);
            if(val == null) val = "";
            res[i] = val;
        }
        return res;
    }
}
