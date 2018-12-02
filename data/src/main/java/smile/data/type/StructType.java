/*******************************************************************************
 * Copyright (c) 2010 Haifeng Li
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
 *******************************************************************************/
package smile.data.type;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import smile.data.Tuple;

/**
 * Struct data type is determined by the fixed order of the fields
 * of primitive data types in the struct. An instance of a struct type
 * will be a tuple.
 *
 * @author Haifeng Li
 */
public class StructType implements DataType {

    /** Struct fields. */
    private final StructField[] fields;
    /** Field name to index map. */
    private final Map<String, Integer> index = new HashMap<>();

    /**
     * Constructor.
     */
    StructType(StructField... fields) {
        this.fields = fields;
        initFieldIndex();
    }

    /**
     * Constructor.
     */
    StructType(List<StructField> fields) {
        this.fields = fields.toArray(new StructField[fields.size()]);
        initFieldIndex();
    }

    /** Initialize the field index mapping. */
    private void initFieldIndex() {
        for (int i = 0; i < this.fields.length; i++) {
            StructField field = this.fields[i];
            index.put(field.name, i);
        }
    }

    /** Returns the fields. */
    public StructField[] fields() {
        return fields;
    }

    /** Return the field of given name. */
    public StructField field(String name) {
        return fields[fieldIndex(name)];
    }

    /** Returns the index of a field. */
    public int fieldIndex(String field) {
        return index.get(field);
    }

    @Override
    public String name() {
        return Arrays.stream(fields)
                .map(field -> String.format("%s: %s", field.name, field.type.name()))
                .collect(Collectors.joining(", ", "Struct[", "]"));
    }

    @Override
    public ID id() {
        return ID.Struct;
    }

    @Override
    public String toString() {
        return Arrays.toString(fields);
    }

    @Override
    public String toString(Object o) {
        Tuple t = (Tuple) o;
        return Arrays.stream(fields)
                .map(field -> String.format("  %s: %s", field.name, field.type.toString(t.get(field.name))))
                .collect(Collectors.joining(",\n", "{\n", "\n}"));
    }

    @Override
    public Tuple valueOf(String s) throws ParseException {
        // strip surrounding []
        String[] elements = s.substring(1, s.length() - 1).split(",");
        final Object[] row = new Object[fields.length];
        for (String element : elements) {
            String[] field = element.split(":");
            DataType type = fields[index.get(field[0])].type;
            Object value = type.valueOf(field[1]);
            int i = index.get(field[0]);
            row[i] = value;
        }

        return Tuple.of(row, this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StructType) {
            StructType t = (StructType) o;
            return Arrays.equals(fields, t.fields);
        }

        return false;
    }
}