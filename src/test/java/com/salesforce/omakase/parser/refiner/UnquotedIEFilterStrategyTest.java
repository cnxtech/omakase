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
import com.salesforce.omakase.ast.RawSyntax;
import com.salesforce.omakase.ast.atrule.AtRule;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.ast.declaration.value.UnquotedIEFilter;
import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.omakase.broadcast.QueryableBroadcaster;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link UnquotedIEFilterStrategy}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class UnquotedIEFilterStrategyTest {
    private Refiner refiner;
    private QueryableBroadcaster broadcaster;
    private UnquotedIEFilterStrategy strategy;

    @Before
    public void setup() {
        strategy = new UnquotedIEFilterStrategy();
        broadcaster = new QueryableBroadcaster();
        refiner = new Refiner(broadcaster, Lists.<RefinableStrategy>newArrayList(strategy));
    }

    @Test
    public void refineSelectorReturnsFalse() {
        Selector s = new Selector(new RawSyntax(5, 5, "p"), refiner);
        assertThat(strategy.refineSelector(s, broadcaster)).isFalse();
        assertThat(s.isRefined()).isFalse();
    }

    @Test
    public void refineAtRuleReturnsFalse() {
        AtRule ar = new AtRule(1, 1, "media", new RawSyntax(1, 1, "all"), new RawSyntax(2, 2, "{}"), refiner);
        assertThat(strategy.refineAtRule(ar, broadcaster)).isFalse();
        assertThat(ar.isRefined()).isFalse();
    }

    @Test
    public void refineDeclarationNoMatchReturnsFalse() {
        Declaration d = new Declaration(new RawSyntax(2, 3, "display"), new RawSyntax(2, 5, "none"), refiner);
        assertThat(strategy.refineDeclaration(d, broadcaster)).isFalse();
        assertThat(d.isRefined()).isFalse();
    }

    @Test
    public void refineDeclarationMatches() {
        Declaration d = new Declaration(new RawSyntax(2, 3, "filter"), new RawSyntax(2, 5,
            "progid:DXImageTransform.Microsoft.Shadow(color='#969696', Direction=145, Strength=3)"), refiner);

        assertThat(strategy.refineDeclaration(d, broadcaster)).isTrue();
        assertThat(d.propertyValue()).isInstanceOf(UnquotedIEFilter.class);

        UnquotedIEFilter ief = (UnquotedIEFilter)d.propertyValue();
        assertThat(ief.line()).isEqualTo(2);
        assertThat(ief.column()).isEqualTo(5);
        assertThat(ief.content()).isEqualTo("progid:DXImageTransform.Microsoft.Shadow(color='#969696', Direction=145, " +
            "Strength=3)");
    }

}
