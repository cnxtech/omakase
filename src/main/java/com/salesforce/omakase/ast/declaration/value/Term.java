/**
 * ADD LICENSE
 */
package com.salesforce.omakase.ast.declaration.value;

import static com.salesforce.omakase.emitter.SubscribableRequirement.REFINED_DECLARATION;

import com.salesforce.omakase.ast.Syntax;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.emitter.Description;
import com.salesforce.omakase.emitter.Subscribable;

/**
 * A single segment of a {@link Declaration}'s full property value.
 * 
 * @author nmcwilliams
 */
@Subscribable
@Description(value = "a single segment of a property value", broadcasted = REFINED_DECLARATION)
public interface Term extends TermListMember, Syntax {
}