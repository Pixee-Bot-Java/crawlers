/* Copyright 2010-2022 Norconex Inc.
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
package com.norconex.importer;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.common.i18n.UncheckedException;
import org.apache.tika.io.NullInputStream;

import com.norconex.commons.lang.file.ContentType;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.commons.lang.map.MapUtil;
import com.norconex.commons.lang.map.Properties;
import com.norconex.commons.lang.xml.XML;
import com.norconex.importer.doc.Doc;
import com.norconex.importer.doc.DocMetadata;
import com.norconex.importer.handler.HandlerDoc;
import com.norconex.importer.handler.ImporterHandlerException;
import com.norconex.importer.handler.condition.ImporterCondition;
import com.norconex.importer.handler.filter.DocumentFilter;
import com.norconex.importer.handler.tagger.DocumentTagger;
import com.norconex.importer.parser.ParseState;

public final class TestUtil {

    private static final String BASE_PATH =
         "src/site/resources/examples/books/alice-in-wonderland-book-chapter-1";

    private TestUtil() {
    }

    public static Properties newMetadata() {
        var p = new Properties();
        p.loadFromMap(MapUtil.toMap(
            "field1", "value1",
            "field2", "value2",
            "field3", List.of("value3.1", "value3.2")
        ));
        return p;
    }

    public static String getContentAsString(Doc doc)
            throws IOException {
        return IOUtils.toString(doc.getInputStream(), StandardCharsets.UTF_8);
    }

    public static File getAlicePdfFile() {
        return new File(BASE_PATH + ".pdf");
    }
    public static File getAliceDocxFile() {
        return new File(BASE_PATH + ".docx");
    }
    public static File getAliceZipFile() {
        return new File(BASE_PATH + ".zip");
    }
    public static File getAliceHtmlFile() {
        return new File(BASE_PATH + ".html");
    }
    public static File getAliceTextFile() {
        return new File(BASE_PATH + ".txt");
    }

    public static Doc getAlicePdfDoc() {
        return newDoc(getAlicePdfFile());
    }
    public static Doc getAliceDocxDoc() {
        return newDoc(getAliceDocxFile());
    }
    public static Doc getAliceZipDoc() {
        return newDoc(getAliceZipFile());
    }
    public static Doc getAliceHtmlDoc() {
        return newDoc(getAliceHtmlFile());
    }
    public static Doc getAliceTextDoc() {
        return newDoc(getAliceTextFile());
    }

    public static Importer getTestConfigImporter() throws IOException {
        var config = new ImporterConfig();
        try (var is =
                TestUtil.class.getResourceAsStream("test-config.xml");
                Reader r = new InputStreamReader(is)) {
            new XML(r).populate(config);
        }
        return new Importer(config);
    }

    public static boolean filter(DocumentFilter filter, String ref,
            Properties metadata, ParseState parseState)
                    throws ImporterHandlerException {
        return filter(filter, ref, null, metadata, parseState);
    }
    public static boolean filter(DocumentFilter filter, String ref,
            InputStream is, Properties metadata, ParseState parseState)
                    throws ImporterHandlerException {
        var input = is == null ? new NullInputStream(0) : is;
        return filter.acceptDocument(
                newHandlerDoc(ref, input, metadata), input, parseState);
    }

    public static boolean condition(ImporterCondition cond, String ref,
            Properties metadata, ParseState parseState)
                    throws ImporterHandlerException {
        return condition(cond, ref, null, metadata, parseState);
    }
    public static boolean condition(ImporterCondition cond, String ref,
            InputStream is, Properties metadata, ParseState parseState)
                    throws ImporterHandlerException {
        var input = is == null ? new NullInputStream(0) : is;
        return cond.testDocument(
                newHandlerDoc(ref, input, metadata), input, parseState);
    }

    public static void tag(DocumentTagger tagger, String ref,
            Properties metadata, ParseState parseState)
                    throws ImporterHandlerException {
        tag(tagger, ref, null, metadata, parseState);
    }
    public static void tag(DocumentTagger tagger, String ref,
            InputStream is, Properties metadata, ParseState parseState)
                    throws ImporterHandlerException {
        var input = is == null ? new NullInputStream(0) : is;
        tagger.tagDocument(
                newHandlerDoc(ref, input, metadata), input, parseState);
    }


    public static Doc newDoc(File file) {
        try {
            return new Doc(
                    file.getAbsolutePath(),
                    CachedInputStream.cache(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new UncheckedException(e);
        }
    }


    public static HandlerDoc newHandlerDoc() {
        return newHandlerDoc("N/A", null, new Properties());
    }
    public static HandlerDoc newHandlerDoc(Properties meta) {
        return newHandlerDoc("N/A", null, meta);
    }
    public static HandlerDoc newHandlerDoc(String ref) {
        return newHandlerDoc(ref, null, new Properties());
    }
    public static HandlerDoc newHandlerDoc(String ref, Properties meta) {
        return newHandlerDoc(ref, null, meta);
    }
    public static HandlerDoc newHandlerDoc(String ref, InputStream in) {
        return newHandlerDoc(ref, in, new Properties());
    }
    public static HandlerDoc newHandlerDoc(
            String ref, InputStream in, Properties meta) {
        // Remove document.reference for tests that need the same count
        // as values they entered in metadata. Just keep it if explicitely
        // passed.
        var hasRef = meta != null && meta.containsKey("document.reference");
        var inputStream = in != null ? in : InputStream.nullInputStream();
        var doc = new Doc(ref, CachedInputStream.cache(inputStream), meta);
        if (!hasRef) {
            doc.getMetadata().remove("document.reference");
        }
        var ct = doc.getMetadata().getString(DocMetadata.CONTENT_TYPE);
        if (ct != null) {
            doc.getDocRecord().setContentType(ContentType.valueOf(ct));
        }

        return new HandlerDoc(doc);
    }

    public static String contentAsString(Doc doc) {
        try {
            return IOUtils.toString(
                    doc.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    public static String toString(InputStream is) {
        try {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    public static CachedInputStream toCachedInputStream(String str) {
        return CachedInputStream.cache(toInputStream(str));
    }
    public static InputStream toInputStream(String str) {
        return new ByteArrayInputStream(str.getBytes());
    }

    public static CachedInputStream failingCachedInputStream() {
        return new CachedStreamFactory().newInputStream(failingInputStream());
    }
    public static InputStream failingInputStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Test mock exception.");
            }
        };
    }

    public static String toUtf8UnixLineString(ByteArrayOutputStream os) {
        return os.toString(UTF_8).replace("\r", "");
    }
}
