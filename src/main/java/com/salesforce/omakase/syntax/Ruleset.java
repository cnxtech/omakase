package com.salesforce.omakase.syntax;

import java.util.List;


/**
 * TODO Description
 * 
 * @author nmcwilliams
 * @since 0.1
 */
public interface Ruleset extends Syntax<RefinedRuleset> {
	/**
	 * TODO Description
	 * 
	 * @return TODO
	 */
	SelectorGroup selectorGroup();

	/**
	 * TODO Description
	 * 
	 * @return TODO
	 */
	List<Declaration> declarations();
}
