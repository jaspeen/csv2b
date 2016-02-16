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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mark property as embedded bean declaration.
 * @author Artem Mironov
 */
@Documented
@Target({ ANNOTATION_TYPE, FIELD, METHOD})
@Retention(RUNTIME)
public @interface CsvEmbed {
    /**
     * Prefix for all embedded columns
     */
    String headerPrefix() default "";

    /**
     * Metadata overrides for embedded columns
     */
    CsvColOverride [] overrides() default {};

    @Target({ ANNOTATION_TYPE, FIELD, METHOD})
    @Retention(RUNTIME)
    public @interface CsvColOverride {
        String name();
        CsvCol col();
    }
}
