/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.dataloader.dyna;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import com.salesforce.dataloader.util.DLLogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.action.visitor.DAOLoadVisitor;
import com.salesforce.dataloader.config.AppConfig;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.util.AppUtil;
import com.sforce.ws.util.FileUtil;

/**
 * 
 * @author Lexi Viripaeff
 * @since 6.0
 */

public final class FileByteArrayConverter implements Converter {
    private static Logger logger;

    // ----------------------------------------------------------- Constructors

    public FileByteArrayConverter() {
        logger = DLLogManager.getLogger(this.getClass());
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Convert the specified input object into an output object of the specified type.
     * 
     * @param type
     *            Data type to which this value should be converted
     * @param value
     *            The input value to be converted
     * @exception ConversionException
     *                if conversion cannot be performed successfully
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object convert(Class type, Object value) {

        if (value == null || String.valueOf(value).length() == 0) { return null; }
        final String absolutePath = new File(String.valueOf(value.toString())).getAbsolutePath();
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            // just in case the file is not found we want to display the absolute file name to the user
            File file = new File(absolutePath);
            if (!file.canRead()) {
                logger.debug("Attempting to enable readable flag on file " + absolutePath);
                file.setReadable(true);
            }
            FileUtil.copy(new FileInputStream(absolutePath), byteStream);
            Path pathToValueFile = Path.of(absolutePath);
            String mimeType = Files.probeContentType(pathToValueFile);
            AppConfig appConfig = AppConfig.getCurrentConfig();
            if (mimeType != null
                    && mimeType.equalsIgnoreCase("text/plain")
                    && appConfig != null
                    && appConfig.getBoolean(AppConfig.PROP_LOAD_PRESERVE_WHITESPACE_IN_RICH_TEXT)
                    && AppUtil.isContentSObject(appConfig.getString(AppConfig.PROP_ENTITY))) {
                // Preserve the formatting only if the content is of type plain text
                // AND the flag to preserve whitespace characters in RichText fields is enabled
                // AND the content is for ContentNote sobject. 
                //     See https://help.salesforce.com/s/articleView?id=000387816&type=1 for how
                //     data loader processes ContentNote.
                String content = byteStream.toString();
                String formattedContent = DAOLoadVisitor.preserveWhitespaceInRichText(content, AppConfig.DEFAULT_RICHTEXT_REGEX);
                return formattedContent.getBytes();
            } else {
                return byteStream.toByteArray();
            }
        } catch (Exception e) {
            if (e instanceof java.io.FileNotFoundException) {
                if (AppUtil.getOSType() == AppUtil.OSType.MACOSX 
                        && (absolutePath.contains("/Desktop/") || absolutePath.contains("/Downloads/"))) {
                    logger.error(Messages.getMessage(this.getClass(), "insufficientAccessToContentOnMacMsg1", absolutePath));
                    logger.error(Messages.getMessage(this.getClass(), "insufficientAccessToContentOnMacMsg2"));
                } else {
                    logger.error(Messages.getMessage(this.getClass(), "insufficientAccessToContentGenericMsg", absolutePath));
                }
            }
            throw new ConversionException(e);
        } finally {
            try {
                byteStream.close();
            } catch (Exception ex) {
                // do nothing
            }
        }
    }

}
