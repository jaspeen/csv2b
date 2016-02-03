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

import org.jeesy.classinfo.converter.api.StringConverter;
import org.jeesy.classinfo.converter.api.StringParser;
import org.jeesy.classinfo.converter.api.StringSerializer;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Define csv column metadata
 * @author Artem Mironov
 */
@Documented
@Target({ ANNOTATION_TYPE, FIELD, METHOD})
@Retention(RUNTIME)
public @interface CsvCol {
    /**
     * Name of the csv column as it appears in header
     * If empty property name will be used instead.
     */
    String name() default "";

    /**
     * Converter from string for this column.
     */
    Class<StringParser> parser() default StringParser.class;

    /**
     * Converter to string
     */
    Class<StringSerializer> serializer() default StringSerializer.class;

    /**
     * Reader should generate exception on columns which required by not in csv file
     * Writer should generate exception if this property is null when required is true
     */
    boolean required() default false;

}
