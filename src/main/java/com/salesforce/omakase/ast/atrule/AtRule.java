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

package com.salesforce.omakase.ast.atrule;

import com.google.common.base.Optional;
import com.salesforce.omakase.SupportMatrix;
import com.salesforce.omakase.ast.Named;
import com.salesforce.omakase.ast.RawSyntax;
import com.salesforce.omakase.ast.Refinable;
import com.salesforce.omakase.ast.Rule;
import com.salesforce.omakase.ast.Statement;
import com.salesforce.omakase.ast.StatementIterable;
import com.salesforce.omakase.ast.Syntax;
import com.salesforce.omakase.ast.collection.AbstractGroupable;
import com.salesforce.omakase.broadcast.Broadcaster;
import com.salesforce.omakase.broadcast.annotation.Description;
import com.salesforce.omakase.broadcast.annotation.Subscribable;
import com.salesforce.omakase.data.Prefix;
import com.salesforce.omakase.parser.raw.RawAtRuleParser;
import com.salesforce.omakase.parser.refiner.AtRuleRefiner;
import com.salesforce.omakase.parser.refiner.GenericRefiner;
import com.salesforce.omakase.parser.refiner.Refiner;
import com.salesforce.omakase.writer.StyleAppendable;
import com.salesforce.omakase.writer.StyleWriter;

import java.io.IOException;

import static com.google.common.base.Preconditions.*;
import static com.salesforce.omakase.broadcast.BroadcastRequirement.AUTOMATIC;

/**
 * Represents one of the CSS at-rules, such as {@literal @}media, {@literal @}charset, {@literal @}keyframes, etc...
 * <p/>
 * It's important to note that the raw members may contain grammatically incorrect CSS. Refining the object will perform basic
 * grammar validation. See the notes on {@link Refinable}.
 *
 * @author nmcwilliams
 * @see RawAtRuleParser
 */
@Subscribable
@Description(broadcasted = AUTOMATIC)
public final class AtRule extends AbstractGroupable<StatementIterable, Statement> implements Statement, Refinable<AtRule>,
    Named {
    private final transient GenericRefiner refiner;
    private String name;

    // unrefined
    private final Optional<RawSyntax> rawExpression;
    private final Optional<RawSyntax> rawBlock;

    // refined
    private Optional<AtRuleExpression> expression;
    private Optional<AtRuleBlock> block;

    private boolean shouldWriteName = true;

    /**
     * Constructs a new {@link AtRule} instance.
     *
     * @param line
     *     The line number.
     * @param column
     *     The column number.
     * @param name
     *     Name of the at-rule.
     * @param rawExpression
     *     The raw at-rule expression. If no expression is present pass in null.
     * @param rawBlock
     *     The raw at-rule block. If no block is present pass in null.
     * @param refiner
     *     The {@link GenericRefiner} to be used later during refinement of this object.
     */
    public AtRule(int line, int column, String name, RawSyntax rawExpression, RawSyntax rawBlock, GenericRefiner refiner) {
        super(line, column);
        this.name = name;
        this.rawExpression = Optional.fromNullable(rawExpression);
        this.rawBlock = Optional.fromNullable(rawBlock);
        this.expression = Optional.absent();
        this.block = Optional.absent();
        this.refiner = refiner;
    }

    /**
     * Creates a new instance with no line or number specified (used for dynamically created {@link Syntax} units).
     *
     * @param name
     *     Name of the at-rule.
     * @param expression
     *     The at-rule's expression, or null if not present.
     * @param block
     *     The at-rule's block, or null if not present.
     */
    public AtRule(String name, AtRuleExpression expression, AtRuleBlock block) {
        super(-1, -1);

        checkNotNull(name, "name cannot be  null");
        checkArgument(expression != null || block != null, "either the expression or the block must be present");

        this.name = name;
        this.rawExpression = Optional.absent();
        this.rawBlock = Optional.absent();
        this.expression = Optional.fromNullable(expression);
        this.block = Optional.fromNullable(block);
        this.refiner = null;

        if (block != null) {
            block.parent(this);
        }
    }

    @Override
    public String name() {
        return name;
    }

    /**
     * Specifies whether the name should be written out. This might be specified as false by custom {@link Refiner} objects where
     * the name of the custom at-rule is not applicable in the final CSS source.
     *
     * @param shouldWriteName
     *     Whether the at-rule name (and @ symbol) should be written out.
     *
     * @return this, for chaining.
     */
    public AtRule shouldWriteName(boolean shouldWriteName) {
        this.shouldWriteName = shouldWriteName;
        return this;
    }

    /**
     * Gets whether the name of the atRule should be written out. Default is true, but can be modified via {@link
     * #shouldWriteName(boolean)}.
     *
     * @return True if the name of the atRule should be written out.
     */
    public boolean shouldWriteName() {
        return shouldWriteName;
    }

    /**
     * Gets the original, raw, non-validated expression if present (e.g., "utf-8", or "all and (min-width: 800px)".
     *
     * @return The raw expression, or {@link Optional#absent()} if not present.
     */
    public Optional<RawSyntax> rawExpression() {
        return rawExpression;
    }

    /**
     * Gets the original, raw, non-validated at-rule block, if present.
     *
     * @return The at-rule block, or {@link Optional#absent()} if not present.
     */
    public Optional<RawSyntax> rawBlock() {
        return rawBlock;
    }

    /**
     * Sets the {@link AtRuleExpression}.
     *
     * @param expression
     *     The expression.
     *
     * @return this, for chaining.
     */
    public AtRule expression(AtRuleExpression expression) {
        if (expression == null) {
            checkState(block.isPresent(), "cannot remove / set a null expression when the block is absent");
        }
        this.expression = Optional.fromNullable(expression);
        return this;
    }

    /**
     * Gets the at-rule expression, if present. Note that this attempts refinement on the expression unless a refined expression
     * is already set.
     *
     * @return The expression, or {@link Optional#absent()} if not present.
     */
    public Optional<AtRuleExpression> expression() {
        return refine().expression;
    }

    /**
     * Gets whether a refined expression has been set on this at-rule.
     *
     * @return True if a refined expression has been set.
     */
    public boolean hasRefinedExpression() {
        return expression.isPresent();
    }

    /**
     * Sets the {@link AtRuleBlock}.
     *
     * @param block
     *     The block.
     *
     * @return this, for chaining.
     */
    public AtRule block(AtRuleBlock block) {
        if (block == null) {
            checkState(expression.isPresent(), "cannot remove / set a null block when the expression is absent");
        } else {
            block.parent(this);
        }
        this.block = Optional.fromNullable(block);
        return this;
    }

    /**
     * Gets the at-rule block, if present. Note that this attempts refinement on the block unless a refined block is already set.
     *
     * @return The block, or {@link Optional#absent()} if not present.
     */
    public Optional<AtRuleBlock> block() {
        return refine().block;
    }

    /**
     * Gets whether a refined block has been set on this at-rule.
     *
     * @return True if a refined block has been set.
     */
    public boolean hasRefinedBlock() {
        return block.isPresent();
    }

    /**
     * Used to indicate this at-rule is for metadata purposes only and should not be written out in the output CSS.
     * <p/>
     * This is mainly used for custom syntax that is refined by an {@link AtRuleRefiner}.
     *
     * @return this, for chaining.
     */
    public AtRule markAsMetadataRule() {
        shouldWriteName(false);
        expression(MetadataExpression.instance());
        return this;
    }

    @Override
    public boolean isRefined() {
        return expression.isPresent() || block.isPresent();
    }

    @Override
    public AtRule refine() {
        if (!isRefined() && refiner != null) refiner.refine(this);
        return this;
    }

    @Override
    public Optional<Rule> asRule() {
        return Optional.absent();
    }

    @Override
    public Optional<AtRule> asAtRule() {
        return Optional.of(this);
    }

    @Override
    protected AtRule self() {
        return this;
    }

    @Override
    public void propagateBroadcast(Broadcaster broadcaster) {
        if (expression.isPresent()) {
            expression.get().propagateBroadcast(broadcaster);
        }
        if (block.isPresent()) {
            block.get().propagateBroadcast(broadcaster);
        }
        super.propagateBroadcast(broadcaster);
    }

    @Override
    public boolean isWritable() {
        if (!super.isWritable()) return false;

        if (isRefined()) {
            // this logic is based on the assumed general behavior of not being writable when no contents are writable. For
            // example, if the block is not writable but the expression present and writable, writing out just the expression
            // could result in invalid css.

            // if expression is present, it must be writable
            if (expression.isPresent() && !expression.get().isWritable()) {
                return false;
            }

            // if block is present, it must be writable
            if (block.isPresent() && !block.get().isWritable()) {
                return false;
            }

            // otherwise if we have a writable name, expression or block return true
            return shouldWriteName || expression.isPresent() || block.isPresent();
        }
        return true;
    }

    @Override
    public void write(StyleWriter writer, StyleAppendable appendable) throws IOException {
        // newlines (unless first statement)
        if (!writer.isCompressed() && !isFirst()) {
            appendable.newline().newlineIf(writer.isVerbose());
        }

        if (isRefined()) {
            // name
            if (shouldWriteName) {
                appendable.append('@').append(name);
                appendable.spaceIf(expression.isPresent() && expression.get().isWritable());
            }

            // expression
            if (expression.isPresent()) {
                writer.writeInner(expression.get(), appendable);
            }

            // block
            if (block.isPresent()) {
                writer.writeInner(block.get(), appendable);
            }

        } else {
            // symbol and name
            appendable.append('@').append(name).space();

            // expression
            if (rawExpression.isPresent()) {
                writer.writeInner(rawExpression.get(), appendable);
                appendable.spaceIf(rawBlock.isPresent() && !writer.isCompressed());
            }

            // block
            if (rawBlock.isPresent()) {
                appendable.append('{');
                appendable.indentIf(writer.isVerbose());
                appendable.newlineIf(writer.isVerbose());
                writer.writeInner(rawBlock.get(), appendable);
                appendable.unindentIf(writer.isVerbose());
                appendable.newlineIf(writer.isVerbose());
                appendable.append('}');
            } else {
                appendable.append(';');
            }
        }
    }

    @Override
    public AtRule copy() {
        AtRule copy;

        if (isRefined()) {
            AtRuleExpression expressionCopy = expression.isPresent() ? expression.get().copy() : null;
            AtRuleBlock blockCopy = block.isPresent() ? block.get().copy() : null;
            copy = new AtRule(name, expressionCopy, blockCopy).copiedFrom(this);
        } else {
            RawSyntax expressionCopy = rawExpression.isPresent() ? rawExpression.get().copy() : null;
            RawSyntax blockCopy = rawBlock.isPresent() ? rawBlock.get().copy() : null;
            copy = new AtRule(-1, -1, name, expressionCopy, blockCopy, refiner).copiedFrom(this);
        }
        copy.shouldWriteName(shouldWriteName);
        return copy;
    }

    @Override
    public void prefix(Prefix prefix, SupportMatrix support, boolean deep) {
        if (support.requiresPrefixForAtRule(prefix, name)) {
            name = prefix + name;
        }

        if (!deep) return;

        if (isRefined()) {
            if (expression.isPresent()) {
                expression.get().prefix(prefix, support, deep);
            }
            if (block.isPresent()) {
                block.get().prefix(prefix, support, deep);
            }
        } else {
            if (rawExpression.isPresent()) {
                rawExpression.get().prefix(prefix, support, deep);
            }
            if (rawBlock.isPresent()) {
                rawBlock.get().prefix(prefix, support, deep);
            }
        }
    }
}
