/* Copyright 2014-2022 Norconex Inc.
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
package com.norconex.crawler.core.checksum.impl;

import com.norconex.commons.lang.map.PropertySetter;
import com.norconex.crawler.core.checksum.MetadataChecksummer;
import com.norconex.crawler.core.doc.CrawlDocMetadata;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>Abstract implementation of {@link MetadataChecksummer} giving the option
 * to keep the generated checksum.  The checksum can be stored
 * in a target field name specified.  If no target field name is specified,
 * it stores it under the
 * metadata field name {@link CrawlDocMetadata#CHECKSUM_METADATA}.
 * </p><p>
 * <b>Implementors do not need to store the checksum themselves, this abstract
 * class does it.</b>
 * </p><p>
 * Implementors should offer this XML configuration usage:
 * </p>
 *
 * {@nx.xml #usage
 * <metadataChecksummer class="(subclass)">
 *    keep="[false|true]"
 *    toField="(optional metadata field to store the checksum)"
 *    onSet="[append|prepend|replace|optional]" />
 * }
 * <p>
 * <code>toField</code> is ignored unless the <code>keep</code>
 * attribute is set to <code>true</code>.
 * </p>
 */
@Data
@Accessors(chain = true)
@SuppressWarnings("javadoc")
public class BaseChecksummerConfig {

    /**
     * Whether to keep the metadata checksum value as a new metadata field.
     * @param keep <code>true</code> to keep the checksum
     * @return <code>true</code> to keep the checksum
     */
    private boolean keep;

    /**
     * The metadata field to use to store the checksum value.
     * Default value is set by checksummer implementations.
     * Only applicable if {@link #isKeep()} returns {@code true}
     * @param toField the metadata field name
     * @return metadata field name
     */
	private String toField;

    /**
     * The property setter to use when a value is set.
     * @param onSet property setter
     * @return property setter
     */
    private PropertySetter onSet;
}
