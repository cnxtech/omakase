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

package com.salesforce.omakase.data;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Utilities for working with the generated data in {@link PrefixTables}.
 *
 * @author nmcwilliams
 */
public final class PrefixUtil {
    // static caches, based on prefix tables data being immutable
    private static final Table<Property, Browser, Double> PROPERTY_CACHE = HashBasedTable.create();
    private static final Table<String, Browser, Double> FUNCTION_CACHE = HashBasedTable.create();
    private static final Table<String, Browser, Double> AT_RULE_CACHE = HashBasedTable.create();
    private static final Table<String, Browser, Double> SELECTOR_CACHE = HashBasedTable.create();

    private PrefixUtil() {}

    /**
     * Gets whether prefix info exists for the given {@link Property}.
     *
     * @param property
     *     Check if prefix info exists for this property.
     *
     * @return True of prefix info exists for the given property.
     */
    public static boolean isPrefixableProperty(Property property) {
        return PrefixTables.PROPERTIES.containsRow(property);
    }

    /**
     * Gets whether prefix info exists for the given function name.
     *
     * @param function
     *     Check if prefix info exists for this function name.
     *
     * @return True of prefix info exists for the given function name.
     */
    public static boolean isPrefixableFunction(String function) {
        return PrefixTables.FUNCTIONS.containsRow(function);
    }

    /**
     * Gets whether prefix info exists for the given at-rule.
     *
     * @param name
     *     Check if prefix info exists for this at-rule.
     *
     * @return True of prefix info exists for the given at-rule.
     */
    public static boolean isPrefixableAtRule(String name) {
        return PrefixTables.AT_RULES.containsRow(name);
    }

    /**
     * Gets whether prefix info exists for the given selector name.
     *
     * @param name
     *     Check if prefix info exists for this selector name.
     *
     * @return True of prefix info exists for the given selector name.
     */
    public static boolean isPrefixableSelector(String name) {
        return PrefixTables.SELECTORS.containsRow(name);
    }

    /**
     * Gets the last version of the given browser that requires a prefix for the given property.
     *
     * @param property
     *     The property.
     * @param browser
     *     The browser.
     *
     * @return The last version, or -1 if all known versions of the browser supports the property unprefixed.
     */
    public static Double lastVersionPropertyIsPrefixed(Property property, Browser browser) {
        Double cached = PROPERTY_CACHE.get(property, browser);

        if (cached == null) {
            cached = PrefixTables.PROPERTIES.get(property, browser);
            if (cached == null) cached = -1d;
            PROPERTY_CACHE.put(property, browser, cached);
        }

        return cached;
    }

    /**
     * Gets the last version of the given browser that requires a prefix for the given function name.
     *
     * @param name
     *     The function name.
     * @param browser
     *     The browser.
     *
     * @return The last version, or -1 if all known versions of the browser supports the function name unprefixed.
     */
    public static Double lastVersionFunctionIsPrefixed(String name, Browser browser) {
        Double cached = FUNCTION_CACHE.get(name, browser);

        if (cached == null) {
            cached = PrefixTables.FUNCTIONS.get(name, browser);
            if (cached == null) cached = -1d;
            FUNCTION_CACHE.put(name, browser, cached);
        }

        return cached;
    }

    /**
     * Gets the last version of the given browser that requires a prefix for the given at-rule.
     *
     * @param name
     *     The at-rule name.
     * @param browser
     *     The browser.
     *
     * @return The last version, or -1 if all known versions of the browser supports the at-rule unprefixed.
     */
    public static Double lastVersionAtRuleIsPrefixed(String name, Browser browser) {
        Double cached = AT_RULE_CACHE.get(name, browser);

        if (cached == null) {
            cached = PrefixTables.AT_RULES.get(name, browser);
            if (cached == null) cached = -1d;
            AT_RULE_CACHE.put(name, browser, cached);
        }

        return cached;
    }

    /**
     * Gets the last version of the given browser that requires a prefix for the given selector name.
     *
     * @param name
     *     The selector name.
     * @param browser
     *     The browser.
     *
     * @return The last version, or -1 if all known versions of the browser supports the selector unprefixed.
     */
    public static Double lastVersionSelectorIsPrefixed(String name, Browser browser) {
        Double cached = SELECTOR_CACHE.get(name, browser);

        if (cached == null) {
            cached = PrefixTables.SELECTORS.get(name, browser);
            if (cached == null) cached = -1d;
            SELECTOR_CACHE.put(name, browser, cached);
        }

        return cached;
    }
}