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

package com.salesforce.omakase.ast.declaration.value;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.salesforce.omakase.As;
import com.salesforce.omakase.ast.AbstractSyntax;
import com.salesforce.omakase.ast.Syntax;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.broadcaster.Broadcaster;
import com.salesforce.omakase.emitter.Description;
import com.salesforce.omakase.emitter.Subscribable;
import com.salesforce.omakase.parser.declaration.TermListParser;
import com.salesforce.omakase.writer.StyleAppendable;
import com.salesforce.omakase.writer.StyleWriter;

import java.io.IOException;
import java.util.List;

import static com.salesforce.omakase.emitter.SubscribableRequirement.REFINED_DECLARATION;

/**
 * TESTME
 * <p/>
 * The generic and default {@link Declaration}'s {@link PropertyValue}. This contains a list of {@link Term}s, for example
 * numbers, keywords, functions, hex colors, etc...
 * <p/>
 * If you need to change the contents of the {@link TermList}, change the contents of the actual {@link Term} itself. If you need
 * to remove or add {@link Term}s from the {@link TermList}, create a new {@link TermList} to replace this one with instead.
 * (ACTUALLY I'm not sure why this comment is here. maybe it can be ignored).
 * <p/>
 * In the CSS 2.1 spec this is called "expr", which is obviously shorthand for "expression", however "expression" is name now
 * given to multiple syntax units within different CSS3 modules! So that's why this is not called expression.
 * <p/>
 * XXX This setup is perhaps inconsistent with the rest of the project, with respect to the term members being directly added
 * instead of broadcasted. Also, as noted above, this doesn't allow for additions/removals from the list, which would be nice to
 * support.
 *
 * @author nmcwilliams
 * @see Term
 * @see TermListParser
 * @see TermListMember
 */
@Subscribable
@Description(value = "default, generic property value", broadcasted = REFINED_DECLARATION)
public class TermList extends AbstractSyntax implements PropertyValue {
    private final List<TermListMember> members = Lists.newArrayListWithCapacity(4);

    /** Creates a new instance with no line or number specified (used for dynamically created {@link Syntax} units). */
    public TermList() {}

    /**
     * Constructs a new {@link TermList} instance.
     *
     * @param line
     *     The line number.
     * @param column
     *     The column number.
     */
    public TermList(int line, int column) {
        super(line, column);
    }

    /**
     * Adds a {@link TermListMember}.
     *
     * @param member
     *     The member to add.
     *
     * @return this, for chaining.
     */
    public TermList add(TermListMember member) {
        this.members.add(member);
        return this;
    }

    /**
     * Gets a list of all {@link TermListMember}s in this list.
     *
     * @return All {@link TermListMember}s.
     */
    public ImmutableList<TermListMember> members() {
        return ImmutableList.copyOf(members);
    }

    /**
     * Gets only the list of {@link Term}s in this list (as opposed to {@link #members()} which returns both terms and
     * operators).
     *
     * @return All {@link Term}s.
     */
    public ImmutableList<Term> terms() {
        return ImmutableList.copyOf(Iterables.filter(members, Term.class));
    }

    @Override
    public void propagateBroadcast(Broadcaster broadcaster) {
        super.propagateBroadcast(broadcaster);
        for (TermListMember member : members) {
            // FIXME
            if (member instanceof Term) {
                ((Term)member).propagateBroadcast(broadcaster);
            }
        }
    }

    @Override
    public void write(StyleWriter writer, StyleAppendable appendable) throws IOException {
        for (TermListMember member : members) {
            writer.write(member, appendable);
        }
    }

    @Override
    public String toString() {
        return As.string(this)
            .add("abstract", super.toString())
            .add("members", members)
            .toString();
    }

    /**
     * Creates a new {@link TermList} with the given {@link Term} as the only member.
     * <p/>
     * Example:
     * <pre>
     * <code>TermList.singleValue(NumericalValue.of(10, "px"));</code>
     * </pre>
     *
     * @param term
     *     The value.
     *
     * @return The new {@link TermList} instance.
     */
    public static TermList singleValue(Term term) {
        return new TermList(-1, -1).add(term);
    }

    /**
     * Creates a new {@link TermList} with multiple values separated by the given {@link TermOperator}.
     * <p/>
     * Example:
     * <pre>
     * <code>NumericalValue px10 = NumericalValue.of(10, "px");
     * NumericalValue em5 = NumericalValue.of(5, "em");
     * PropertyValue value = TermList.ofValues(TermOperator.SPACE, px10, em5);
     * </code>
     * </pre>
     *
     * @param separator
     *     The {@link TermOperator} to place in between each {@link Term}.
     * @param values
     *     List of member {@link Term}s.
     *
     * @return The new {@link TermList} instance.
     */
    public static PropertyValue ofValues(TermOperator separator, Term... values) {
        TermList termList = new TermList();
        for (int i = 0; i < values.length; i++) {
            if (i != 0) termList.add(separator);
            termList.add(values[i]);
        }
        return termList;
    }
}
