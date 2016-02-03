/*
 *
 *  * Copyright 2016 Artem Mironov
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.jeesy.csv2b;

import org.jeesy.classinfo.ClassInfo;
import org.jeesy.classinfo.PropertyInfo;
import org.jeesy.classinfo.indexes.CommonClassIndex;
import org.jeesy.classinfo.indexes.PropertyIndex;

import java.util.*;

import static org.jeesy.classinfo.ClassInfoScanner.classInfo;

/**
 * @author Artem Mironov
 */
public class CsvIndex implements CommonClassIndex {
    private String[] header;
    private Map<String, CsvProp> headerToFieldMap = new HashMap<>();
    private Map<String, String> fieldToHeaderMap = new HashMap<>();
    private Set<String> required = new HashSet<>();

    public static class CsvProp {
        private CsvCol csvCol;
        private String path;

        public CsvProp(CsvCol csvCol, String path) {
            this.csvCol = csvCol;
            this.path = path;
        }

        public CsvProp(CsvCol csvCol) {
            this.csvCol = csvCol;
        }

        public CsvCol getCsvCol() {
            return csvCol;
        }

        public String getPath() {
            return path;
        }
    }

    private void putMapping(String header, CsvCol csvCol, String fieldPath) {
        headerToFieldMap.put(header, new CsvProp(csvCol, fieldPath));
        fieldToHeaderMap.put(fieldPath, header);
    }

    private String [] getFullOrder(ClassInfo<?> classInfo) {
        String [] parentOrder = new String[0];
        if(classInfo.getParent() != null) {
              parentOrder = ((ClassInfo<?>)classInfo.getParent()).getIndex(CsvIndex.class).getFullOrder(classInfo.getParent());
        }
        if(classInfo.hasAnnotation(CsvRow.class)) {
            CsvRow csvRow = classInfo.getAnnotation(CsvRow.class);
            String [] newOrder;
            if(csvRow.order().length > 0) {
                newOrder = csvRow.order();
                return newOrder;
            } else if(csvRow.declaredOrder().length > 0) {
                newOrder = Arrays.copyOf(parentOrder, csvRow.order().length + parentOrder.length);
                System.arraycopy(csvRow.order(), 0, newOrder, 0, csvRow.order().length);
                return newOrder;
            }
        }
        //create random order
        Set<String> keySet = classInfo.getProperties().keySet();
        return keySet.toArray(new String[keySet.size()]);

    }

    private void collectEmbedded(List<String> headerList, PropertyInfo pi) {
        //if there is an some kind of order
        String [] fullOrder = getFullOrder(pi.getClassInfo());
        CsvEmbed csvEmbed = pi.getAnnotation(CsvEmbed.class);
        String headerPrefix = csvEmbed.headerPrefix();
        indexColumns(headerPrefix, pi.getName()+".", headerList, pi.getClassInfo(), Collections.<String, CsvCol>emptyMap());
    }

    public Set<String> getRequiredColumns() {
        return required;
    }

    public String[] getHeader() {
        return header;
    }

    public CsvProp getFieldNameByColumnName(String name) {
        return headerToFieldMap.get(name);
    }

    public String getHeaderByPos(int pos) {
        return header[pos - 1];
    }

    private void indexColumns(String headerPrefix, String propertyPrefix, List<String> headerList, ClassInfo<?> classInfo, Map<String, CsvCol> columnOverrides) {
        String [] fullOrder = getFullOrder(classInfo);
        for(String fieldName : fullOrder) {
            PropertyInfo pi = classInfo.getPropertyInfo(fieldName);
            CsvCol csvCol = pi.getAnnotation(CsvCol.class);
            if(csvCol != null && !csvCol.name().isEmpty()) {
                headerList.add(headerPrefix+csvCol.name());
                putMapping(headerPrefix+csvCol.name(), csvCol, propertyPrefix+pi.getName());
            } else if(pi.hasAnnotation(CsvEmbed.class)) {
                collectEmbedded(headerList, pi);
            } else {
                headerList.add(headerPrefix+pi.getName());
                putMapping(headerPrefix+pi.getName(), csvCol, propertyPrefix+pi.getName());
            }
        }
    }

    @Override
    public void index(ClassInfo<?> classInfo) {
        List<String> headerList = new ArrayList<>();
        indexColumns("","", headerList, classInfo, Collections.<String, CsvCol>emptyMap());
        header = headerList.toArray(new String[headerList.size()]);
    }

    public static CsvIndex forClass(Class<?> type) {
        return classInfo(type).getIndex(CsvIndex.class);
    }
}
