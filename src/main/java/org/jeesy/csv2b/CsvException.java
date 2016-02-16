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

import org.jeesy.classinfo.converter.api.ConversionException;

/**
 * @author Artem Mironov
 */
public class CsvException extends RuntimeException {
    private int row = 0;
    private int col = 0;
    private ConversionException conversionException;
    private Exception exception;//any other exception including {@link IOException}
    public CsvException(ConversionException e) {
        exception = conversionException = e;
    }

    public CsvException(Exception e) {
        exception = e;
    }
    public CsvException(int row, int col, ConversionException e) {
        this.row = row;
        this.col = col;
        this.conversionException = e;
        this.exception = e;
    }

    public CsvException(int row, int col, Exception e) {
        this.row = row;
        this.col = col;
        this.exception = e;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public ConversionException getConversionException() {
        return conversionException;
    }

    public Exception getException() {
        return exception;
    }

    /**
     * Assume what only conversion can be reported without stopping iteration.
     */
    public boolean isFatal() {
        return conversionException == null;
    }

    @Override
    public String getMessage() {
        return String.format("Error on row=%d col=%d: %s", row, col, exception.getMessage());
    }
}
