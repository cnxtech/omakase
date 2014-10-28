/*
 * Copyright (C) 2014 salesforce.com, inc.
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

import com.google.common.collect.Lists;
import com.salesforce.omakase.ast.Status;
import com.salesforce.omakase.ast.declaration.KeywordValue;
import com.salesforce.omakase.ast.declaration.PropertyName;
import com.salesforce.omakase.ast.declaration.PropertyValue;
import com.salesforce.omakase.ast.declaration.QuotationMode;
import com.salesforce.omakase.ast.declaration.StringValue;
import com.salesforce.omakase.data.Property;
import com.salesforce.omakase.test.functional.StatusChangingBroadcaster;
import com.salesforce.omakase.util.Values;
import com.salesforce.omakase.writer.StyleWriter;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.*;

/**
 * Unit tests for {@link FontDescriptor}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class FontDescriptorTest {
    private PropertyName samplePropertyName;
    private PropertyValue samplePropertyValue;
    private FontDescriptor descriptor;

    @Before
    public void setup() {
        samplePropertyName = PropertyName.using(Property.FONT_FAMILY);
        samplePropertyValue = PropertyValue.of(StringValue.of(QuotationMode.DOUBLE, "My Font"));
        descriptor = new FontDescriptor(samplePropertyName, samplePropertyValue);
    }

    @Test
    public void getPropertyName() {
        assertThat(descriptor.propertyName()).isSameAs(samplePropertyName);
    }

    @Test
    public void isStringPropertyTrue() {
        assertThat(descriptor.isProperty("font-family")).isTrue();
    }

    @Test
    public void isStringPropertyFalse() {
        assertThat(descriptor.isProperty("border")).isFalse();
    }

    @Test
    public void isPropertyTrue() {
        assertThat(descriptor.isProperty(Property.FONT_FAMILY)).isTrue();
    }

    @Test
    public void isPropertyFalse() {
        descriptor = new FontDescriptor(samplePropertyName, samplePropertyValue);
        assertThat(descriptor.isProperty(Property.BORDER)).isFalse();
    }

    @Test
    public void getName() {
        assertThat(descriptor.name()).isEqualTo("font-family");
    }

    @Test
    public void setPropertyValueTerm() {
        KeywordValue newValue = KeywordValue.of("MyFont");
        descriptor.propertyValue(newValue);
        assertThat(Values.asKeyword(descriptor.propertyValue()).isPresent()).isTrue();
    }

    @Test
    public void setPropertyValueFull() {
        PropertyValue newValue = PropertyValue.of(KeywordValue.of("MyFont"));
        descriptor.propertyValue(newValue);
        assertThat(descriptor.propertyValue()).isSameAs(newValue);
    }

    @Test
    public void newPropertyValueIsBroadcasted() {
        FontFaceBlock block = new FontFaceBlock(1, 1, new StatusChangingBroadcaster());

        PropertyValue newValue = PropertyValue.of(KeywordValue.of("MyFont"));
        descriptor.propertyValue(newValue);

        assertThat(newValue.status()).isSameAs(Status.UNBROADCASTED);
        block.fontDescriptors().append(descriptor);
        assertThat(newValue.status()).isNotSameAs(Status.UNBROADCASTED);
    }

    @Test
    public void changedPropertyValueIsBroadcasted() {
        FontFaceBlock block = new FontFaceBlock(1, 1, new StatusChangingBroadcaster());
        block.fontDescriptors().append(descriptor);

        PropertyValue newValue = PropertyValue.of(KeywordValue.of("MyFont"));
        assertThat(newValue.status()).isSameAs(Status.UNBROADCASTED);

        descriptor.propertyValue(newValue);
        assertThat(newValue.status()).isNotSameAs(Status.UNBROADCASTED);
    }

    @Test
    public void setPropertyValueDoesntBroadcastAlreadyBroadcasted() {
        StatusChangingBroadcaster broadcaster = new StatusChangingBroadcaster();
        FontFaceBlock block = new FontFaceBlock(1, 1, broadcaster);
        descriptor.status(Status.PROCESSED);

        PropertyValue newValue = PropertyValue.of(KeywordValue.of("MyFont"));
        newValue.status(Status.PROCESSED);
        descriptor.propertyValue(newValue);

        block.fontDescriptors().append(descriptor);
        assertThat(broadcaster.all).isEmpty();
    }

    @Test
    public void writeVerbose() {
        StyleWriter writer = StyleWriter.verbose();
        assertThat(writer.writeSnippet(descriptor)).isEqualTo("font-family: \"My Font\"");
    }

    @Test
    public void writeInline() {
        StyleWriter writer = StyleWriter.inline();
        assertThat(writer.writeSnippet(descriptor)).isEqualTo("font-family:\"My Font\"");
    }

    @Test
    public void writeCompressed() {
        StyleWriter writer = StyleWriter.compressed();
        assertThat(writer.writeSnippet(descriptor)).isEqualTo("font-family:\"My Font\"");
    }

    @Test
    public void isWritableTrue() {
        assertThat(descriptor.isWritable()).isTrue();
    }

    @Test
    public void isNotWritableWhenPropertyValueNotWritable() {
        samplePropertyValue.members().clear();
        assertThat(descriptor.isWritable()).isFalse();
    }

    @Test
    public void copy() {
        descriptor.comments(Lists.newArrayList("test"));

        FontDescriptor copy = descriptor.copy();
        assertThat(copy.isProperty(samplePropertyName.asProperty().get()));
        assertThat(Values.asString(copy.propertyValue()).isPresent()).isTrue();
        assertThat(copy.comments()).hasSameSizeAs(descriptor.comments());
    }
}
