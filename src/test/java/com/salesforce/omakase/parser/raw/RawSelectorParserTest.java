/**
 * ADD LICENSE
 */
package com.salesforce.omakase.parser.raw;

import static com.salesforce.omakase.util.Templates.withExpectedResult;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.salesforce.omakase.ast.Syntax;
import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.omakase.parser.AbstractParserTest;
import com.salesforce.omakase.util.Templates.SourceWithExpectedResult;

/**
 * Unit tests for {@link RawSelectorParser}.
 * 
 * @author nmcwilliams
 */
@SuppressWarnings("javadoc")
public class RawSelectorParserTest extends AbstractParserTest<RawSelectorParser> {

    @Override
    public List<String> invalidSources() {
        return ImmutableList.of(
            "   ",
            "\n",
            "{color: red}",
            "1234",
            "$123",
            "$class");
    }

    @Override
    public List<String> validSources() {
        return ImmutableList.of(
            ".class1",
            ".class1.class2",
            ".class1 .class2",
            ".class1 + .class2",
            "#id #id",
            "p p",
            "A",
            ":before",
            "*",
            "::after",
            "  .class .class",
            "  p#id",
            " a[href]",
            "div[class]",
            "E[foo=\"bar\"]",
            "E[foo~=\"bar\"]",
            "E[foo^=\"bar\"]",
            "E[foo$=\"bar\"]  ",
            "E[foo*=\"bar\"]",
            "E:nth-child(n)",
            "E::first-letter",
            "p+p",
            "\t  p~p",
            "\n  p~p",
            "p\n.class\n*#id",
            "p~ a",
            ".aclajsclkajsclajsca .ahcjashjkchas___ ._afaafa_fafa #afa-afa-afaf-afa",
            ".aclajsclkajsclajsca>.ahcjashjkchas___>._afaafa_fafa#afa-afa-afaf-afa",
            "/*comment*/.class",
            "/*comment\n\n aaffa1*//*comment*/.class",
            "/*comment\n\n aaffa1*/\n /*comment*/.class");
    }

    @Override
    public List<SourceWithExpectedResult<Integer>> validSourcesWithExpectedEndIndex() {
        return ImmutableList.of(
            withExpectedResult(".class1, .class2", 7),
            withExpectedResult(".class1 + .class2 { color: red }", 18),
            withExpectedResult("div[class]", 10),
            withExpectedResult("p\n.class\n*#id", 13),
            withExpectedResult(".aclajsclkajsclajsca .ahcjashjkchas___ ._afaafa_fafa #afa-afa-afaf-afa", 70),
            withExpectedResult("E:nth-child(n), .class", 14),
            withExpectedResult("E[foo=\"b,ar\"], .class", 13),
            withExpectedResult("E[foo=\"b{a r\"]#id, #id", 17));
    }

    @Override
    public boolean allowedToTrimLeadingWhitespace() {
        return true;
    }

    @Test
    @Override
    public void matchesExpectedBroadcastContent() {
        List<ParseResult<String>> results = parseWithExpected(
            withExpectedResult(".class1, .class2", ".class1"),
            withExpectedResult(".class1 + .class2 { color: red }", ".class1 + .class2"),
            withExpectedResult("div[class]", "div[class]"),
            withExpectedResult("p\n.class\n*#id", "p\n.class\n*#id"),
            withExpectedResult(
                ".aclajsclkajsclajsca .ahcjashjkchas___ ._afaafa_fafa #afa-afa-afaf-afa",
                ".aclajsclkajsclajsca .ahcjashjkchas___ ._afaafa_fafa #afa-afa-afaf-afa"),
            withExpectedResult("E:nth-child(n), .class", "E:nth-child(n)"),
            withExpectedResult("afafjafasfkasfkjsa", "afafjafasfkasfkjsa"),
            withExpectedResult("E[foo=\"b,ar\"]", "E[foo=\"b,ar\"]"),
            withExpectedResult("E[foo=\"b{a r\"]#id", "E[foo=\"b{a r\"]#id"),
            withExpectedResult("/*comment*/.class.class2", ".class.class2"),
            withExpectedResult("/*comment*//*comment*/#id-abc_ac", "#id-abc_ac"));

        for (ParseResult<String> result : results) {
            Selector s = result.broadcaster.findOnly(Selector.class).get();
            assertThat(s.rawContent().content()).isEqualTo(result.expected);
        }
    }

    @Test
    @Override
    public void correctLineAndColumnNumber() {
        Syntax syntax = parse("\n  .class1").get(0).broadcasted.get(0);
        assertThat(syntax.line()).isEqualTo(2);
        assertThat(syntax.column()).isEqualTo(3);
    }

    @Test
    public void storesComments() {
        Selector s = parse("/*comment1*/.class.class").get(0).broadcaster.findOnly(Selector.class).get();
        assertThat(s.comments().get(0)).isEqualTo("comment1");

        s = parse("/*comment1\n * new line *//*comment2*/.class.class").get(0).broadcaster.findOnly(Selector.class).get();
        assertThat(s.comments()).hasSize(2);
    }
}