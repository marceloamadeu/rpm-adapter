/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 artipie.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.rpm.meta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Alter xml file.
 * @since 0.8
 */
public final class XmlAlter {

    /**
     * File to update.
     */
    private final Path file;

    /**
     * Ctor.
     * @param file File to update
     */
    public XmlAlter(final Path file) {
        this.file = file;
    }

    /**
     * Updates packages attribute of the given file.
     * @param tag Tag to change
     * @param value Value for the attribute
     * @throws IOException When error occurs
     */
    public void pkgAttr(final String tag, final String value) throws IOException {
        final Path trf = Files.createTempFile("", ".xml");
        try (
            InputStream input = Files.newInputStream(this.file);
            OutputStream out = Files.newOutputStream(trf)) {
            final XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
            final XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(out);
            XMLEvent event;
            while (reader.hasNext()) {
                event = reader.nextEvent();
                if (event.isStartElement()
                    && event.asStartElement().getName().getLocalPart().equals(tag)) {
                    writer.add(alterEvent(event, value));
                } else {
                    writer.add(event);
                }
            }
            reader.close();
            writer.close();
        } catch (final XMLStreamException err) {
            throw new IOException("Failed to alter file", err);
        }
        Files.move(trf, this.file, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Alters event by changing packages attribute value.
     * @param original Original event
     * @param value New value
     * @return Altered event
     */
    private static XMLEvent alterEvent(final XMLEvent original, final String value) {
        final StartElement element = original.asStartElement();
        final List<Attribute> newattrs = new ArrayList<>(0);
        final XMLEventFactory events = XMLEventFactory.newFactory();
        boolean replaced = false;
        final Iterator<?> origattrs = element.getAttributes();
        final XMLEvent res;
        while (origattrs.hasNext()) {
            final Attribute attr = (Attribute) origattrs.next();
            if (attr.getName().getLocalPart().equals("packages")) {
                newattrs.add(events.createAttribute(attr.getName(), value));
                replaced = true;
            } else {
                newattrs.add(attr);
            }
        }
        if (replaced) {
            final QName name = element.getName();
            res = events.createStartElement(
                name.getPrefix(),
                name.getNamespaceURI(),
                name.getLocalPart(),
                newattrs.iterator(),
                element.getNamespaces(),
                element.getNamespaceContext()
            );
        } else {
            res = original;
        }
        return res;
    }
}
