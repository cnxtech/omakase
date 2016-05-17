/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.omakase.plugin.prefixer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.salesforce.omakase.Omakase;
import com.salesforce.omakase.data.Browser;
import com.salesforce.omakase.data.Prefix;
import com.salesforce.omakase.plugin.basic.AutoRefiner;
import com.salesforce.omakase.writer.StyleWriter;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.*;

/**
 * Functional tests for {@link Prefixer} selector replacements.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class PrefixerUnitSelectorTest {
    private String process(String original, Prefixer prefixer) {
        StyleWriter writer = StyleWriter.inline();
        Omakase.source(original).use(AutoRefiner.refineEverything()).use(writer).use(prefixer).process();
        return writer.write();
    }

    private Prefixer setup(Prefix... prefixes) {
        List<Prefix> list = Lists.newArrayList(prefixes);
        boolean moz = Iterables.contains(list, Prefix.MOZ);

        Prefixer prefixer = Prefixer.customBrowserSupport();

        if (moz) prefixer.support().browser(Browser.FIREFOX, 25); // all are currently prefixed
        return prefixer;
    }

    // required, not present (add)
    @Test
    public void selectorTest1() {
        String original = "::selection {color:red}";
        String expected = "::-moz-selection {color:red}\n" +
            "::selection {color:red}";

        assertThat(process(original, setup(Prefix.MOZ, Prefix.WEBKIT))).isEqualTo(expected);
    }

    // required, some present (add) (CAN'T DO THIS ONE YET)
    // @Test
    public void selectorTest2() {
        String original = "";
        String expected = "";

        assertThat(process(original, setup(Prefix.MOZ, Prefix.WEBKIT))).isEqualTo(expected);
        fail("unimplemented");
    }

    // required, some present, rearrange true (move/add) (MOVE)
    @Test
    public void selectorTest3() {
        String original = "::selection {color:red}\n" +
            "::-moz-selection {color:red}";
        String expected = "::-moz-selection {color:red}\n" +
            "::selection {color:red}";

        Prefixer prefixer = setup(Prefix.MOZ, Prefix.WEBKIT).rearrange(true);
        assertThat(process(original, prefixer)).isEqualTo(expected);
    }

    // required, some present, rearrange false (leave/add) (LEAVE/NOOP)
    @Test
    public void selectorTest4() {
        String original = "::selection {color:red}\n" +
            "::-moz-selection {color:red}";
        String expected = "::selection {color:red}\n" +
            "::-moz-selection {color:red}";

        Prefixer prefixer = setup(Prefix.MOZ, Prefix.WEBKIT).rearrange(false);
        assertThat(process(original, prefixer)).isEqualTo(expected);
    }

    // not required, not present (noop)
    @Test
    public void selectorTest5() {
        String original = "::selection {color:red}\n" +
            "::-moz-selection {color:red}";
        String expected = "::selection {color:red}\n" +
            "::-moz-selection {color:red}";

        assertThat(process(original, setup())).isEqualTo(expected);
    }

    // not required, present, remove true (remove)
    @Test
    public void selectorTest6() {
        String original = "::-ms-selection {color:red}\n" +
            "::selection {color:red}\n" +
            "::-moz-selection {color:red}\n" +
            "::-webkit-selection {color:red}";
        String expected = "::selection {color:red}";

        Prefixer prefixer = setup().prune(true);
        assertThat(process(original, prefixer)).isEqualTo(expected);
    }

    // not required, present, remove false, rearrange true (move)
    @Test
    public void selectorTest7() {
        String original = "::-ms-selection {color:red}\n" +
            "::selection {color:red}\n" +
            "::-moz-selection {color:red}\n" +
            "::-webkit-selection {color:red}";
        String expected = "::-ms-selection {color:red}\n" +
            "::-moz-selection {color:red}\n" +
            "::-webkit-selection {color:red}\n" +
            "::selection {color:red}";

        Prefixer prefixer = setup().prune(false).rearrange(true);
        assertThat(process(original, prefixer)).isEqualTo(expected);
    }

    // not required, present, remove false, rearrange false (noop)
    @Test
    public void selectorTest8() {
        String original = "::-ms-selection {color:red}\n" +
            "::selection {color:red}\n" +
            "::-moz-selection {color:red}\n" +
            "::-webkit-selection {color:red}";
        String expected = "::-ms-selection {color:red}\n" +
            "::selection {color:red}\n" +
            "::-moz-selection {color:red}\n" +
            "::-webkit-selection {color:red}";

        Prefixer prefixer = setup().prune(false).rearrange(false);
        assertThat(process(original, prefixer)).isEqualTo(expected);
    }

    // some required, some present, remove true, rearrange true (move/remove)
    @Test
    public void selectorTest9() {
        String original = "::-ms-selection {color:red}\n" +
            "::selection {color:red}\n" +
            "::-moz-selection {color:red}\n" +
            "::-webkit-selection {color:red}";

        String expected = "::-moz-selection {color:red}\n" +
            "::selection {color:red}";

        Prefixer prefixer = setup(Prefix.MOZ).prune(true).rearrange(true);
        assertThat(process(original, prefixer)).isEqualTo(expected);
    }

    // some required, some present, remove true, rearrange true (add/move/remove) (CANT DO THIS ONE YET)
    // @Test
    public void selectorTest10() {
        String original = "";
        String expected = "";

        Prefixer prefixer = setup(Prefix.MOZ, Prefix.WEBKIT).prune(true).rearrange(true);
        assertThat(process(original, prefixer)).isEqualTo(expected);
        fail("unimplemented");
    }

    // required, multiple present, rearrange true (move)
    @Test
    public void selectorTest11() {
        String original = "::selection {color:red}\n" +
            "::-moz-selection {color:red\n}" +
            "::-moz-selection {color:red}" +
            ".test{color:red}";

        String expected = "::-moz-selection {color:red}\n" +
            "::-moz-selection {color:red}\n" +
            "::selection {color:red}\n" +
            ".test {color:red}";

        Prefixer prefixer = setup(Prefix.MOZ).rearrange(true);
        assertThat(process(original, prefixer)).isEqualTo(expected);
    }

    // required, multiple present, remove true (remove)
    @Test
    public void selectorTest12() {
        String original = "::selection {color:red}\n" +
            "::-moz-selection {color:red\n}" +
            "::-moz-selection {color:red}" +
            ".test{color:red}";

        String expected = "::selection {color:red}\n" +
            ".test {color:red}";

        Prefixer prefixer = setup().prune(true);
        assertThat(process(original, prefixer)).isEqualTo(expected);
    }
}
