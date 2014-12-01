/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.salesforce.omakase.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.salesforce.omakase.ast.Syntax;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Helper for constructing toString() methods...cuz guava's helper just doesn't get the job done.
 * <p/>
 * Example: <code><pre>As.string(this).indent().add("abc", abc).toString();</pre></code>
 *
 * @author nmcwilliams
 */
public final class As {
    private final List<Entry> entries = Lists.newArrayList();
    private final Object instance;

    private String name;
    private boolean indent;

    /** use a constructor method instead */
    private As(Object object) {
        named(object.getClass().getSimpleName());
        this.instance = object;
    }

    /**
     * Creates a new string representation helper for the given object. Usually used inside of toString methods.
     *
     * @param object
     *     Create a string representation of this object.
     *
     * @return The helper instance.
     */
    public static As string(Object object) {
        return new As(object);
    }

    /**
     * Creates a new string representation helper described by the given name. Usually used inside of toString methods.
     *
     * @param name
     *     Name of the object being represented.
     *
     * @return The helper instance.
     */
    public As named(String name) {
        this.name = name;
        return this;
    }

    /**
     * Specifies that this toString representation should indent and write each member on a separate line.
     *
     * @return this, for chaining.
     */
    public As indent() {
        indent = true;
        return this;
    }

    /**
     * Adds a member to this toString representation.
     *
     * @param name
     *     Name of the member.
     * @param value
     *     The member.
     *
     * @return this, for chaining.
     */
    public As add(String name, Object value) {
        return entry(name, value, false);
    }

    /**
     * Same as {@link #add(String, Object)}, except it will only add the member if the given condition is true.
     *
     * @param condition
     *     Only add if this condition is true.
     * @param name
     *     Name of the member.
     * @param value
     *     The member.
     *
     * @return this, for chaining.
     */
    public As addIf(boolean condition, String name, Object value) {
        if (condition) add(name, value);
        return this;
    }

    /**
     * Adds a member to this toString representation. This is for iterables, which will automatically have their indentation level
     * increased (if indent is turned on).
     *
     * @param name
     *     Name of the member.
     * @param iterable
     *     The member.
     *
     * @return this, for chaining.
     */
    public As add(String name, Iterable<?> iterable) {
        return entry(name, iterable, true);
    }

    /**
     * Same as {@link #add(String, Iterable)}, except it will only add the member if the iterable is not empty.
     *
     * @param name
     *     Name of the member.
     * @param iterable
     *     The member.
     *
     * @return this, for chaining.
     */
    public As addUnlessEmpty(String name, Iterable<?> iterable) {
        if (!Iterables.isEmpty(iterable)) {
            add(name, iterable);
        }
        return this;
    }

    /**
     * Specifies that all fields should be added.
     * <p/>
     * Fields with a value of null will not be output. Iterables will be added with {@link #add(String, Iterable)} and {@link
     * #indent()} will automatically be turned on upon the first addition of an iterable. If the object being printed is a {@link
     * Syntax} unit, line, column, and comments information will automatically be added.
     *
     * @return this, for chaining.
     */
    @SuppressWarnings("AutoBoxing")
    public As fields() {
        if (instance instanceof Syntax) {
            Syntax syntax = (Syntax)instance;
            addIf(syntax.line() > -1, "line", syntax.line());
            addIf(syntax.column() > -1, "col", syntax.line());
            addUnlessEmpty("comments", syntax.comments());
            addUnlessEmpty("orphaned comments", syntax.orphanedComments());
        }

        List<Field> fields = Lists.newArrayList();

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!Modifier.isTransient(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                fields.add(field);
            }
        }

        Collections.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        try {
            for (Field field : fields) {
                Object value = null;
                value = field.get(instance);
                if (value instanceof Iterable) {
                    indent();
                    Iterable<?> iterable = (Iterable<?>)value;
                    if (!Iterables.isEmpty(iterable)) {
                        add(field.getName(), iterable);
                    }
                } else if (value != null) {
                    add(field.getName(), value);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(entries.size() * 32);

        // name of the object being printed
        builder.append(name);
        if (indent) builder.append(" ");
        builder.append("{");

        // entries for the object being printed
        String separator = "";
        for (Entry entry : entries) {
            if (indent) {
                // use a new line separator between entries
                builder.append("\n  ");
            } else {
                // use a comma separator between entries
                builder.append(separator);
                separator = ", ";
            }

            // entry name
            builder.append(entry.name);
            builder.append(indent ? ": " : "=");

            // entry value. For indentation, increase the space before each newline for iterables
            String value = String.valueOf(entry.value);
            if (entry.isIterable) {
                value = value.replaceAll("\n", "\n  ");
            }
            builder.append(value);
        }

        // closing bracket
        if (indent) builder.append("\n");
        builder.append("}");

        return builder.toString();
    }

    /** utility method to create an {@link Entry} */
    private As entry(String name, Object value, boolean isIterable) {
        Entry entry = new Entry();
        entry.name = name;
        entry.value = value;
        entry.isIterable = isIterable;
        entries.add(entry);
        return this;
    }

    /** information on an item to include in toString */
    private static final class Entry {
        String name;
        Object value;
        boolean isIterable;
    }
}
