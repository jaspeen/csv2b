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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Mark bean classes what should be mapped to/from csv
 * @author Artem Mironov
 */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface CsvRow {
    /**
     * Fields names in order how it should be used to write.
     * Should only list columns declared in annotated class since they will be concatenated with parent order
     */
    String [] declaredOrder() default {};

    /**
     * Full order including all parent properties
     * If order is not empty the mapper will ignore declaredOrder attribute
     */
    String [] order() default {};
}
