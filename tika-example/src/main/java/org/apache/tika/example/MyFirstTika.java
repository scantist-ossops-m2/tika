/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tika.example;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.language.LanguageProfile;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

/**
 * Demonstrates how to call the different components within Tika: its
 * {@link Detector} framework (aka MIME identification and repository), its
 * {@link Parser} interface, its {@link LanguageIdentifier} and other goodies.
 * It also shows the "easy way" via {@link AutoDetectParser}
 */

@SuppressWarnings("deprecation")
public class MyFirstTika {
    public static void main(String[] args) throws Exception {
        String filename = args[0];
        TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
        
        Metadata metadata = new Metadata();
        String text = parseUsingComponents(filename, tikaConfig, metadata);
        System.out.println("Parsed Metadata: ");
        System.out.println(metadata);
        System.out.println("Parsed Text: ");
        System.out.println(text);
        
        System.out.println("-------------------------");
        
        metadata = new Metadata();
        text = parseUsingAutoDetect(filename, tikaConfig, metadata);
        System.out.println("Parsed Metadata: ");
        System.out.println(metadata);
        System.out.println("Parsed Text: ");
        System.out.println(text);
    }
    
    public static String parseUsingAutoDetect(String filename, TikaConfig tikaConfig, 
            Metadata metadata) throws Exception {
        System.out.println("Handling using AutoDetectParser: [" + filename + "]");
        
        AutoDetectParser parser = new AutoDetectParser(tikaConfig);
        ContentHandler handler = new BodyContentHandler();
        TikaInputStream stream = TikaInputStream.get(new File(filename));
        parser.parse(stream, handler, metadata, new ParseContext());
        return handler.toString();
    }
    public static String parseUsingComponents(String filename, TikaConfig tikaConfig, 
            Metadata metadata) throws Exception {    
        MimeTypes mimeRegistry = tikaConfig.getMimeRepository();

        System.out.println("Examining: [" + filename + "]");

        System.out.println("The MIME type (based on filename) is: ["
                + mimeRegistry.getMimeType(filename) + "]");

        System.out.println("The MIME type (based on MAGIC) is: ["
                + mimeRegistry.getMimeType(new File(filename)) + "]");

        Detector mimeDetector = (Detector) mimeRegistry;
        System.out.println("The MIME type (based on the Detector interface) is: ["
                + mimeDetector.detect(new File(filename).toURI().toURL()
                        .openStream(), new Metadata()) + "]");

        LanguageIdentifier lang = new LanguageIdentifier(new LanguageProfile(
                FileUtils.readFileToString(new File(filename))));

        System.out.println("The language of this content is: ["
                + lang.getLanguage() + "]");

        Parser parser = tikaConfig.getParser(
                MediaType.parse(mimeRegistry.getMimeType(filename).getName()));
        ContentHandler handler = new BodyContentHandler();
        parser.parse(new File(filename).toURI().toURL().openStream(), handler,
                metadata, new ParseContext());
        
        return handler.toString();
    }
}
