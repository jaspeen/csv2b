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

package org.jeesy.csv2b.convert;

import org.jeesy.classinfo.TypeInfo;
import org.jeesy.classinfo.converter.api.ConversionException;
import org.jeesy.classinfo.converter.api.ConversionService;
import org.jeesy.classinfo.converter.api.StringParser;

/**
 * Converter what turns empty strings to null values.
 * @author Artem Mironov
 */
public class NullForEmptyStringParser<T> implements StringParser<T> {
    private final ConversionService conversionService;
    public NullForEmptyStringParser(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }


    @Override
    public T convert(String src, TypeInfo<String> srcType, TypeInfo<T> dstType) throws ConversionException {
        if(src != null && src.isEmpty())
            src = null;
        return conversionService.convertType(src, srcType, dstType);
    }
}
