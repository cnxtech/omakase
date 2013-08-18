Omakase
=======




Subscribable Syntax Units
-------------------------

<pre>
    Name                        Description                                               Enablement / Dependency     Type
    -------------------------   -------------------------------------------------------   -------------------------   ---------------
01: Refinable                   raw syntax that can be further refined                    Automatic                   interface
02: Statement                   rule or at-rule                                           SyntaxTree                  interface
03: Syntax                      parent interface of all subscribable units                Automatic                   interface
04: Rule                        (no description)                                          SyntaxTree                  class
05: Stylesheet                  (no description)                                          SyntaxTree                  class
06: Declaration                 (no description)                                          Automatic                   class
07: PropertyValue               interface for all property values                         Declaration#refine          interface
08: Term                        a single segment of a property value                      Declaration#refine          interface
09: FunctionValue               individual function value                                 Declaration#refine          class
10: HexColorValue               individual hex color value                                Declaration#refine          class
11: KeywordValue                individual keyword value                                  Declaration#refine          class
12: NumericalValue              individual numerical value                                Declaration#refine          class
13: StringValue                 individual string value                                   Declaration#refine          class
14: TermList                    default, generic property value                           Declaration#refine          class
15: SelectorPart                parent interface for all selector segments                Selector#refine             interface
16: SimpleSelector              parent interface for non-combinator selector parts        Selector#refine             interface
17: AttributeSelector           attribute selector segment                                Selector#refine             class
18: ClassSelector               class selector segment                                    Selector#refine             class
19: Combinator                  combinator segment                                        Selector#refine             class
20: IdSelector                  id selector segment                                       Selector#refine             class
21: PseudoClassSelector         pseudo class selector segment                             Selector#refine             class
22: PseudoElementSelector       pseudo element selector segment                           Selector#refine             class
23: Selector                    (no description)                                          Automatic                   class
24: SelectorGroup               group of comma-separated selectors                        Selector#refine             class
25: TypeSelector                type/element selector segment                             Selector#refine             class
26: UniversalSelector           universal selector segment                                Selector#refine             class

Generated by SubscribableSyntaxTable.java
</pre>
