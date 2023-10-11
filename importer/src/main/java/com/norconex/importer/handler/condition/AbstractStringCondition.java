/* Copyright 2021-2022 Norconex Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.norconex.importer.handler.condition;

import java.io.IOException;
import java.io.Reader;

import com.norconex.commons.lang.io.IOUtil;
import com.norconex.commons.lang.io.TextReader;
import com.norconex.commons.lang.xml.XMLConfigurable;
import com.norconex.importer.handler.HandlerDoc;
import com.norconex.importer.handler.ImporterHandlerException;
import com.norconex.importer.parser.ParseState;

import lombok.Data;

/**
 * <p>Base class to facilitate creating conditions based on text content,
 * loading text into {@link StringBuilder} for memory processing.
 * </p>
 *
 * <p>This class limits the memory used for content
 * filtering by reading one section of text at a time.  Each
 * sections are sent for filtering once they are read until the condition
 * is met.
 * No two sections exists in memory at once.  Sub-classes should
 * respect this approach.  Each section have a maximum number of characters
 * equal to the maximum read size defined using {@link #setMaxReadSize(int)}.
 * When none is set, the default read size is defined by
 * {@link TextReader#DEFAULT_MAX_READ_SIZE}.
 * </p>
 *
 * <p>An attempt is made to break sections nicely after a paragraph, sentence,
 * or word.  When not possible, long text will be cut at a size equal
 * to the maximum read size.
 * </p>
 *
 * <p>
 * The {@link #testDocument(HandlerDoc, String, ParseState, int)}
 * method is invoked at least once, even if there is no content. This gives
 * subclasses a chance to act on metadata even if there is no content.
 * </p>
 *
 * <p>
 * Implementors should be conscious about memory when dealing with the string
 * builder.
 * </p>
 *
 * {@nx.xml.usage #attributes
 *   maxReadSize="(max characters to read at once)"
 *   {@nx.include com.norconex.importer.handler.condition.AbstractCharStreamCondition#attributes}
 * }
 *
 * <p>
 * Subclasses inherit the above {@link XMLConfigurable} attribute(s).
 * </p>
 *
 */
@SuppressWarnings("javadoc")
@Data
public abstract class AbstractStringCondition
        <T extends StringConditionConfig>
                extends AbstractCharStreamCondition<T> {

    @Override
    protected final boolean testDocument(
            HandlerDoc doc, Reader input, ParseState parseState)
                    throws ImporterHandlerException {
        var sectionIndex = 0;
        var b = new StringBuilder();
        String text = null;
        try (var reader = new TextReader(
                IOUtil.toNonNullReader(input),
                getConfiguration().getMaxReadSize())) {
            while ((text = reader.readText()) != null) {
                b.append(text);
                var matched = testDocument(
                        doc, b.toString(), parseState, sectionIndex);
                sectionIndex++;
                b.setLength(0);
                if (matched) {
                    return true;
                }
            }
            // should have been incremented at least once if there is content
            if (sectionIndex == 0) {
                return testDocument(
                        doc, b.toString(), parseState, sectionIndex);
            }
        } catch (IOException e) {
            throw new ImporterHandlerException(
                    "Cannot filter text document.", e);
        }
        b.setLength(0);
        b = null;
        return false;
    }

    protected abstract boolean testDocument(
            HandlerDoc doc,
            String input,
            ParseState parseState,
            int sectionIndex)
                    throws ImporterHandlerException;
}