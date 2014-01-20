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

package com.salesforce.omakase.parser.refiner;

import com.google.common.collect.Lists;
import com.salesforce.omakase.ast.Refinable;
import com.salesforce.omakase.ast.atrule.AtRule;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.ast.declaration.GenericFunctionValue;
import com.salesforce.omakase.ast.declaration.RawFunction;
import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.omakase.broadcast.Broadcaster;
import com.salesforce.omakase.broadcast.QueryableBroadcaster;
import com.salesforce.omakase.plugin.SyntaxPlugin;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Top-level manager/registry used for refining {@link Refinable} objects, such as {@link Selector} and {@link Declaration}, and
 * other "refinable" syntax such as custom functions.
 * <p/>
 * This is normally internal-use only.
 *
 * @author nmcwilliams
 * @see SyntaxPlugin
 * @see RefinerRegistry
 */
public final class GenericRefiner implements Refiner, RefinerRegistry {
    private static final StandardRefiner STANDARD = new StandardRefiner();

    private final Broadcaster defaultBroadcaster;
    private final List<AtRuleRefiner> atRuleRefiners = Lists.newArrayList();
    private final List<SelectorRefiner> selectorRefiners = Lists.newArrayList();
    private final List<DeclarationRefiner> declarationRefiners = Lists.newArrayList();
    private final List<FunctionRefiner> functionRefiners = Lists.newArrayList();

    /**
     * Creates a new {@link GenericRefiner} instance without a specific {@link Broadcaster} specified.
     * <p/>
     * Generally not the constructor to use.
     */
    public GenericRefiner() {
        this(new QueryableBroadcaster());
    }

    /**
     * Creates a new {@link GenericRefiner} instance with the given {@link Broadcaster} to use for new refined AST objects.
     *
     * @param broadcaster
     *     The {@link Broadcaster} to use for refined AST objects.
     */
    public GenericRefiner(Broadcaster broadcaster) {
        this.defaultBroadcaster = broadcaster;
    }

    @Override
    public GenericRefiner register(AtRuleRefiner refiner) {
        atRuleRefiners.add(checkNotNull(refiner, "refiner cannot be null"));
        return this;
    }

    @Override
    public GenericRefiner register(SelectorRefiner refiner) {
        selectorRefiners.add(checkNotNull(refiner, "refiner cannot be null"));
        return this;
    }

    @Override
    public GenericRefiner register(DeclarationRefiner refiner) {
        declarationRefiners.add(checkNotNull(refiner, "refiner cannot be null"));
        return this;
    }

    @Override
    public GenericRefiner register(FunctionRefiner refiner) {
        functionRefiners.add(checkNotNull(refiner, "refiner cannot be null"));
        return this;
    }

    @Override
    public GenericRefiner registerMulti(Refiner refiner) {
        if (refiner instanceof AtRuleRefiner) {
            atRuleRefiners.add((AtRuleRefiner)refiner);
        }
        if (refiner instanceof FunctionRefiner) {
            functionRefiners.add((FunctionRefiner)refiner);
        }
        if (refiner instanceof DeclarationRefiner) {
            declarationRefiners.add((DeclarationRefiner)refiner);
        }
        if (refiner instanceof SelectorRefiner) {
            selectorRefiners.add((SelectorRefiner)refiner);
        }
        return this;
    }

    /**
     * Refines an {@link AtRule} object.
     * <p/>
     * {@link Refiner} objects will be consulted in the registered order. If no {@link Refiner} decides to handle the instance, or
     * if none are registered then {@link StandardRefiner#refine(AtRule, Broadcaster, GenericRefiner)} will be used.
     * <p/>
     * <b>Note:</b> Non-library code usually should not call this method directly, but {@link AtRule#refine()} instead.
     *
     * @param atRule
     *     The {@link AtRule} to refine.
     *
     * @return Whether refinement occurred or not.
     */
    public boolean refine(AtRule atRule) {
        return refine(atRule, defaultBroadcaster);
    }

    /**
     * Refines an {@link AtRule} object.
     * <p/>
     * {@link Refiner} objects will be consulted in the registered order. If no {@link Refiner} decides to handle the instance, or
     * if none are registered then {@link StandardRefiner#refine(AtRule, Broadcaster, GenericRefiner)} will be used.
     * <p/>
     * <b>Note:</b> Non-library code usually should not call this method directly, but {@link AtRule#refine()} instead.
     *
     * @param atRule
     *     The {@link AtRule} to refine.
     * @param broadcaster
     *     {@link Broadcaster} to use.
     *
     * @return Whether refinement occurred or not.
     */
    public boolean refine(AtRule atRule, Broadcaster broadcaster) {
        for (AtRuleRefiner strategy : atRuleRefiners) {
            if (strategy.refine(atRule, broadcaster, this)) return true;
        }

        // fallback to the default refiner
        return STANDARD.refine(atRule, broadcaster, this);
    }

    /**
     * Refines a {@link Selector} object.
     * <p/>
     * {@link Refiner} objects will be consulted in the registered order. If no {@link Refiner} decides to handle the instance, or
     * if none are registered then {@link StandardRefiner#refine(Selector, Broadcaster, GenericRefiner)} will be used.
     * <p/>
     * <b>Note:</b> Non-library code usually should not call this method directly, but {@link Selector#refine()} instead.
     *
     * @param selector
     *     The {@link Selector} to refine.
     *
     * @return Whether refinement occurred or not.
     */
    public boolean refine(Selector selector) {
        return refine(selector, defaultBroadcaster);
    }

    /**
     * Refines a {@link Selector} object.
     * <p/>
     * {@link Refiner} objects will be consulted in the registered order. If no {@link Refiner} decides to handle the instance, or
     * if none are registered then {@link StandardRefiner#refine(Selector, Broadcaster, GenericRefiner)} will be used.
     * <p/>
     * <b>Note:</b> Non-library code usually should not call this method directly, but {@link Selector#refine()} instead.
     *
     * @param selector
     *     The {@link Selector} to refine.
     * @param broadcaster
     *     {@link Broadcaster} to use.
     *
     * @return Whether refinement occurred or not.
     */
    public boolean refine(Selector selector, Broadcaster broadcaster) {
        for (SelectorRefiner strategy : selectorRefiners) {
            if (strategy.refine(selector, broadcaster, this)) return true;
        }

        // fallback to the default refiner
        return STANDARD.refine(selector, broadcaster, this);
    }

    /**
     * Refines a {@link Declaration} object.
     * <p/>
     * {@link Refiner} objects will be consulted in the registered order. If no {@link Refiner} decides to handle the instance, or
     * if none are registered then {@link StandardRefiner#refine(Declaration, Broadcaster, GenericRefiner)} will be used.
     * <p/>
     * <b>Note:</b> Non-library code usually should not call this method directly, but {@link Declaration#refine()} instead.
     *
     * @param declaration
     *     The {@link Declaration} to refine.
     *
     * @return Whether refinement occurred or not.
     */
    public boolean refine(Declaration declaration) {
        return refine(declaration, defaultBroadcaster);
    }

    /**
     * Refines a {@link Declaration} object.
     * <p/>
     * {@link Refiner} objects will be consulted in the registered order. If no {@link Refiner} decides to handle the instance, or
     * if none are registered then {@link StandardRefiner#refine(Declaration, Broadcaster, GenericRefiner)} will be used.
     * <p/>
     * <b>Note:</b> Non-library code usually should not call this method directly, but {@link Declaration#refine()} instead.
     *
     * @param declaration
     *     The {@link Declaration} to refine.
     * @param broadcaster
     *     {@link Broadcaster} to use.
     *
     * @return Whether refinement occurred or not.
     */
    public boolean refine(Declaration declaration, Broadcaster broadcaster) {
        for (DeclarationRefiner strategy : declarationRefiners) {
            if (strategy.refine(declaration, broadcaster, this)) return true;
        }

        // fallback to the default refiner
        return STANDARD.refine(declaration, broadcaster, this);
    }

    /**
     * Refines a {@link GenericFunctionValue} object.
     * <p/>
     * {@link Refiner} objects will be consulted in the registered order. If no {@link Refiner} decides to handle the instance, or
     * if none are registered then {@link StandardRefiner#refine(RawFunction, Broadcaster, GenericRefiner)} will be used.
     *
     * @param raw
     *     The {@link RawFunction} to refine.
     *
     * @return Whether refinement occurred or not.
     */
    public boolean refine(RawFunction raw) {
        return refine(raw, defaultBroadcaster);
    }

    /**
     * Refines a {@link GenericFunctionValue} object.
     * <p/>
     * {@link Refiner} objects will be consulted in the registered order. If no {@link Refiner} decides to handle the instance, or
     * if none are registered then {@link StandardRefiner#refine(RawFunction, Broadcaster, GenericRefiner)} will be used.
     *
     * @param raw
     *     The {@link RawFunction} to refine.
     * @param broadcaster
     *     {@link Broadcaster} to use.
     *
     * @return Whether refinement occurred or not.
     */
    public boolean refine(RawFunction raw, Broadcaster broadcaster) {
        for (FunctionRefiner strategy : functionRefiners) {
            if (strategy.refine(raw, broadcaster, this)) return true;
        }

        // fallback to the default refiner
        return STANDARD.refine(raw, broadcaster, this);
    }

    /**
     * Gets the {@link Broadcaster} registered with this {@link GenericRefiner}.
     *
     * @return The {@link Broadcaster}.
     */
    public Broadcaster broadcaster() {
        return defaultBroadcaster;
    }
}
