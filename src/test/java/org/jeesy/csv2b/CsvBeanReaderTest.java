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

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.*;

/**
 * @author Artem Mironov
 */
public class CsvBeanReaderTest {

    public static class ParentClass {
        public String stringProp;
        public String stringProp2;

        @Override
        public String toString() {
            return "ParentClass{" +
                    "stringProp='" + stringProp + '\'' +
                    ", stringProp2='" + stringProp2 + '\'' +
                    '}';
        }
    }

    @CsvRow(order = {"nestedProp", "intProp", "stringProp", "stringProp2"})
    public static class ChildClass extends ParentClass {
        public Integer intProp;
        @CsvEmbed
        public NestedClass nestedProp;

        @Override
        public String toString() {
            return "ChildClass{" +
                    "intProp=" + intProp +
                    ", nestedProp=" + nestedProp +
                    "} " + super.toString();
        }
    }

    @CsvRow(order = {"someField1", "someField2"})
    public static class NestedClass {
        public String someField1;
        public String someField2;

        @Override
        public String toString() {
            return "NestedClass{" +
                    "someField1='" + someField1 + '\'' +
                    ", someField2='" + someField2 + '\'' +
                    '}';
        }
    }

    private Reader resourceReader(String resourceName) {
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName));
    }

    private Writer osWriter() {
        return new OutputStreamWriter(System.out);
    }

    @Test
    public void testReadBean() throws IOException {
        try (CsvBeanReader<ChildClass> reader = CsvModel.STANDARD.newBeanReader(ChildClass.class, resourceReader("testbean.csv"), true)) {
            ChildClass c = reader.readBean();
            assertEquals((Integer)42, c.intProp);
            assertEquals("stringVal", c.stringProp);
            assertEquals("stringVal2", c.stringProp2);
            assertNotNull(c.nestedProp);
            assertEquals("someVal1", c.nestedProp.someField1);
            assertEquals("someVal2", c.nestedProp.someField2);
            StringWriter sw = new StringWriter();
            CsvBeanWriter csvBeanWriter = CsvModel.STANDARD.newBeanWriter(ChildClass.class, sw);
            csvBeanWriter.writeHeader();
            csvBeanWriter.writeBean(c);
            csvBeanWriter.flush();
            assertEquals("someField1,someField2,intProp,stringProp,stringProp2\r\nsomeVal1,someVal2,42,stringVal,stringVal2\r\n", sw.toString());
        }
    }
}
