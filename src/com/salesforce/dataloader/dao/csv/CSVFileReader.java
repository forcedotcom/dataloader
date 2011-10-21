/*
 * Copyright (c) 2011, salesforce.com, inc.
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

package com.salesforce.dataloader.dao.csv;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.salesforce.dataloader.config.Config;
import com.salesforce.dataloader.config.Messages;
import com.salesforce.dataloader.dao.DataReader;
import com.salesforce.dataloader.exception.*;
import com.salesforce.dataloader.util.DAORowUtil;

/**
 * Parse a CSV or tab delimmited file into lines of fields. One line is returned in each call to getNextLine. Each line
 * is returned as an ArrayList of String fields. This parser auto-detects comma or tab delimmiters based on the first
 * line. This file correctly handles embedded quotes, delimmiters, and newlines, based on the way MS Excel and other
 * apps do CSV format. Note that this is different that the way StreamTokenizer handles things itself, which is why I
 * needed to add the getNextToken () method that wraps the StreamTokenizer.nextToken () method. StreamTokenizer doesn't
 * handle embedded newlines in a quoted string. Because CSV format allows embedded newlines in quoted strings, record
 * index values in the array won't necessarily agree with line numbers in a text editor, although they will agree with
 * row numbers when the file is viewed in Excel. We should probably give some accessor to ask the text-editor
 * appropriate line number for the current record.
 */

public class CSVFileReader implements DataReader {

    // logger
    private static Logger logger = Logger.getLogger(CSVFileReader.class);

    // the file we will be loading
    protected File file;

    // the current Buffered Reader
    protected BufferedReader input;

    // this should store the total rows in the file,
    // must be set externally, since this class streams the file
    protected int totalRows = 0;

    /**
     * Should the file always be read as UTF8
     * @return true if UTF8 format is forced
     */

    protected boolean isForceUTF8() {
        return forceUTF8;
    }

    /**
     * Sets if the file should always be read as UTF8
     * @param utf8Encoded
     */
    public void setForceUTF8(boolean utf8Encoded) {
        this.forceUTF8 = utf8Encoded;
    }
    protected StreamTokenizer mParser;
    protected char mSeparator;
    protected boolean mInitializing;
    protected boolean ignoreBlankRecords;
    protected Map<String, String> mUniqueStrings;
    protected boolean mTrimStrings;
    protected int maxSizeOfIndividualCell = 0;
    protected int maxColumnsPerRow = 4000;
    protected int maxRowSizeInCharacters = 0;
    // 400K of characters in a row..

    // by default, giving a 10m character limit. Note that this limit is in charachters. if you want to limit
    // by bytes, do it seperately.
    // Call the mutator to set the limit for following.
    protected int maxFileSizeInCharacters = 0;
    private int maxRowsInFile = 0;

    private int fileSizeInCharacters = 0;
    private int rowsInFile = 0;
    protected int mNumHeaders;
    protected int mNumFields;

    boolean mAtEOF;
    protected boolean mHaveLastToken;
    protected int mLastToken;
    protected String mLastWord;
    protected int currentRowNumber = 0;

    private boolean forceUTF8 = false;

    private List<String> headerRow = null;

    private boolean isOpen = false;

    public CSVFileReader() {
    }

    public CSVFileReader(Config config) {
        this(config.getString(Config.DAO_NAME));
        setForceUTF8(config.getBoolean(Config.READ_UTF8));
    }

    public CSVFileReader(String filePath) {
        file = new File(filePath);
    }

    public CSVFileReader(File f) {
        file = f;
    }

    public void checkConnection() throws DataAccessObjectInitializationException {
        open();
        close();
    }

    public void open() throws DataAccessObjectInitializationException {
        if(isOpen()) {
            close();
        }
        // can't use a file reader because we may want UTF8
        currentRowNumber = 0;
        if (forceUTF8 || isUTF8File(file)) {
            try {
                input = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error(Messages.getString("CSVFileDAO.errorUnsupportedEncoding"), e); //$NON-NLS-1$
                throw new DataAccessObjectInitializationException(Messages.getString("CSVFileDAO.errorUnsupportedEncoding"), e); //$NON-NLS-1$
            } catch (FileNotFoundException e) {
                String errMsg = Messages.getFormattedString("CSVFileDAO.errorOpen", file.getAbsolutePath()); //$NON-NLS-1$
                logger.error(errMsg, e);
                throw new DataAccessObjectInitializationException(errMsg, e);
            }
        } else {
            try {
                input = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                String errMsg = Messages.getFormattedString("CSVFileDAO.errorOpen", file.getAbsolutePath());
                logger.error(errMsg, e);
                throw new DataAccessObjectInitializationException(errMsg, e);
            }
        }

        mParser = new StreamTokenizer(input);
        mParser.ordinaryChars(0, 255);
        mParser.wordChars(0, 255);
        mParser.ordinaryChar('\"');
        // Need to do set EOL significance after setting ordinary and word
        // chars, and need to explicitely set \n and \r as whitespace chars
        // for EOL detection to work
        mParser.eolIsSignificant(true);
        mParser.whitespaceChars('\n', '\n');
        mParser.whitespaceChars('\r', '\r');
        mInitializing = true;
        mAtEOF = false;
        mUniqueStrings = null;

        // mTrimStrings = false; //LEXI: this was the orig
        mTrimStrings = true;
        try {
            headerRow = getHeaderLine();

            if (headerRow == null) {
                logger.error(Messages.getString("CSVFileDAO.errorHeaderRow")); //$NON-NLS-1$
                throw new DataAccessObjectInitializationException(Messages.getString("CSVFileDAO.errorHeaderRow")); //$NON-NLS-1$
            }

            // file is open and initialized at this point
            setOpen(true);
        } catch (IOException e) {
            logger.error(Messages.getString("CSVFileDAO.errorHeaderRow")); //$NON-NLS-1$
            throw new DataAccessObjectInitializationException(Messages.getString("CSVFileDAO.errorHeaderRow")); //$NON-NLS-1$
        } finally {
            // if there's a problem getting header row, the stream needs to be closed
            if(!isOpen()) {
                try {
                    input.close();
                } catch (IOException ignore) {
                    // ignore exception as this is ok at this point
                }
            }
        }
    }

    /**
     * Close the file stream when we are finishe with a load
     */
    public void close() {
        try {
            if (isOpen() && input != null) {
                try {
                    input.close();
                } catch (IOException ioe) {
                    logger.error("Error closing file stream.", ioe);
                }
            }
        } finally {
            input = null;
            setOpen(false);
        }
    }

    /**
     * Checks the Bytes for the UTF-8 BOM if found, returns true, else false
     */
    private boolean isUTF8File(File f) {

        FileInputStream stream = null;

        // UTF-8 BOM is 0xEE 0xBB OxBf
        // or 239 187 191

        try {
            stream = new FileInputStream(f);

            if (stream.read() == 239) {
                if (stream.read() == 187) {
                    if (stream.read() == 191) { return true; }
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("Error in file when testing CSV");
        } catch (IOException io) {
            logger.error("IO error when testing file");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {

                }
            }
        }
        return false;
    }

    public void setTrimStrings(boolean s) {
        mTrimStrings = s;
    }

    public void setUniquifyStrings(boolean s) {
        if (s) {
            if (mUniqueStrings == null) {
                mUniqueStrings = new HashMap<String, String>();
            }
        } else {
            mUniqueStrings = null;
        }
    }

    public void setIgnoreBlankRecords(boolean f) {
        ignoreBlankRecords = f;
    }

    public void setMaxRowsInFile(int i) {
        maxRowsInFile = i;
    }

    public void setMaxFileSizeInCharacters(int i) {
        maxFileSizeInCharacters = i;
    }

    public void setMaxColumnsPerRow(int i) {
        maxColumnsPerRow = i;
    }

    public int getMaxColumnsPerRow() {
        return maxColumnsPerRow;
    }

    public void setMaxSizeOfIndividualCell(int i) {
        maxSizeOfIndividualCell = i;
    }

    public int getMaxSizeOfIndividualCell() {
        return maxSizeOfIndividualCell;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataReader#readRowList(int)
     */
    public List<Map<String, Object>> readRowList(int maxRows) throws DataAccessObjectException {
        List<Map<String, Object>> outputRows = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < maxRows; i++) {
            Map<String, Object> outputRow = readRow();
            if (outputRow != null) {
                // if row has been returned, add it to the output
                outputRows.add(outputRow);
            } else {
                // if encountered null, the reading is over
                break;
            }
        }
        return outputRows;
    }

    /*
     * Gets the next row from the current data access object data source. <i>Side effect:</i> Updates the current record
     * number
     * @see com.salesforce.dataloader.dao.DataAccessObject#getNextRow()
     */
    public synchronized Map<String, Object> readRow() throws DataAccessObjectException {
        // make sure file is open
        if(!isOpen()) {
            open();
        }

        if (mParser == null) {
            throw new DataAccessObjectException(new FileNotOpenException());
        }

        if (mAtEOF) return null;
        ArrayList<String> line = null;
        Map<String, Object> map = null;
        try {
            ++currentRowNumber;
            line = getRegularLine();
            if (ignoreBlankRecords) {
                while (line != null) {
                    // repeat as necessary until we get a non-blank line
                    boolean found_something = false;
                    for (int i = 0; i < line.size(); ++i) {
                        String value = line.get(i);
                        if ((value != null) && (value.length() > 0)) {
                            found_something = true;
                            break;
                        }
                    }
                    if (found_something) break;
                    ++currentRowNumber;
                    line = getRegularLine();
                }
            }
            if (!DAORowUtil.isValidRow(line)) {
                --currentRowNumber;
                return null;
            } else {
                if(line.size() > headerRow.size()) {
                    String errMsg = Messages.getFormattedString("CSVFileDAO.errorRowTooLarge", new String[] { //$NON-NLS-1$
                            String.valueOf(currentRowNumber), String.valueOf(line.size()), String.valueOf(headerRow.size()) });
                    throw new DataAccessRowException(errMsg);
                }
                map = new HashMap<String, Object>();
                for (int i = 0; i < line.size(); i++) {
                    map.put(headerRow.get(i), line.get(i));
                }
            }
            checkLineExceptions(line);
        } catch (IOException ioe) {
            throw new DataAccessObjectException(ioe);
        }

        return map;
    }

    private synchronized void checkLineExceptions(ArrayList line) throws IOException {
        int rowSizeInCharacters = 0;
        if (line != null) {
            for (int j = 0; j < line.size(); ++j) {
                String value = (String)line.get(j);
                if (value != null) {
                    rowSizeInCharacters += value.length();
                }
            }
            checkRowSize(rowSizeInCharacters);
            fileSizeInCharacters += rowSizeInCharacters;
            checkFileSize(fileSizeInCharacters);
            rowsInFile++;
            checkRowsInFile(rowsInFile);
        }
    }

    protected ArrayList<String> getHeaderLine() throws IOException {
        mParser.ordinaryChar(',');
        mParser.ordinaryChar('\t');

        ArrayList<String> comma_fields = new ArrayList<String>();
        ArrayList<String> tab_fields = new ArrayList<String>();

        StringBuffer comma_field = new StringBuffer();
        StringBuffer tab_field = new StringBuffer();
        int token = 0;

        do {
            token = getNextToken();
            if (token == ',') {
                comma_fields.add(comma_field.toString());
                comma_field = new StringBuffer();
                checkNumCells(comma_fields.size());
                appendToCell(tab_field, ',');
            } else if (token == '\t') {
                tab_fields.add(tab_field.toString());
                checkNumCells(tab_fields.size());
                tab_field = new StringBuffer();
                appendToCell(comma_field, '\t');
            } else if ((token == StreamTokenizer.TT_EOF) || (token == StreamTokenizer.TT_EOL)) {
                if (comma_field.length() != 0 || tab_field.length() != 0) {
                    comma_fields.add(comma_field.toString());
                    checkNumCells(comma_fields.size());
                    tab_fields.add(tab_field.toString());
                    checkNumCells(tab_fields.size());
                }
            } else {
                appendToCell(comma_field, mParser.sval);
                appendToCell(tab_field, mParser.sval);
            }

            if (token == StreamTokenizer.TT_EOF) mAtEOF = true;
        } while ((token != StreamTokenizer.TT_EOF) && (token != StreamTokenizer.TT_EOL));

        ArrayList<String> fields = null;
        if (tab_fields.size() == 0 && comma_fields.size() == 0) {
            fields = null;
        } else if (tab_fields.size() > comma_fields.size()) {
            mSeparator = '\t';
            mParser.wordChars(',', ',');
            fields = tab_fields;
        } else {
            mSeparator = ',';
            mParser.wordChars('\t', '\t');
            fields = comma_fields;
        }

        if (mAtEOF && (fields == null || fields.size() == 0)) {
            return null;
        }

        mNumHeaders = fields == null ? 0 : fields.size();
        for (int i = 0; i < mNumHeaders; ++i)
            fields.set(i, intern(fields.get(i)));
        mNumFields = mNumHeaders;
        return fields;
    }

    protected void appendToCell(StringBuffer cell, char c) throws CellSizeTooBigException {
        checkStringBufferSize(cell, 1);
        cell.append(c);
    }

    protected void appendToCell(StringBuffer cell, String s) throws CellSizeTooBigException {
        checkStringBufferSize(cell, s.length());
        cell.append(s);
    }

    protected void checkStringBufferSize(StringBuffer cell, int additional) throws CellSizeTooBigException {
        if (maxSizeOfIndividualCell <= 0) return;
        if (cell.length() + additional <= maxSizeOfIndividualCell) return;
        throw new CellSizeTooBigException(currentRowNumber);
    }

    @SuppressWarnings("serial")
    public static class CSVParseException extends IOException {
        final int recordNumber;

        CSVParseException(int i) {
            recordNumber = i;
        }

        public int getRecordNumber() {
            return recordNumber;
        }
    }

    @SuppressWarnings("serial")
    public static class FileNotOpenException extends IOException {
        FileNotOpenException() {
            super("File not Open.  Open the file before accessing it.");
        }
    }

    @SuppressWarnings("serial")
    public static class CellSizeTooBigException extends CSVParseException {
        CellSizeTooBigException(int i) {
            super(i);
        }
    }

    @SuppressWarnings("serial")
    public static class TooManyCellsInRowException extends CSVParseException {
        TooManyCellsInRowException(int i) {
            super(i);
        }
    }

    @SuppressWarnings("serial")
    public static class TooManyRowsException extends CSVParseException {
        TooManyRowsException(int i) {
            super(i);
        }
    }

    @SuppressWarnings("serial")
    public static class RowSizeExceededException extends CSVParseException {
        RowSizeExceededException(int i) {
            super(i);
        }
    }

    @SuppressWarnings("serial")
    public static class FileSizeExceededException extends CSVParseException {
        public FileSizeExceededException(int i) {
            super(i);
        }
    }

    protected void checkNumCells(int curSize) throws TooManyCellsInRowException {
        if (maxColumnsPerRow <= 0) return;
        if (curSize <= maxColumnsPerRow) return;
        throw new TooManyCellsInRowException(currentRowNumber);
    }

    protected void checkRowsInFile(int rowsInFile) throws TooManyRowsException {
        if (maxRowsInFile <= 0) { return; }
        if (rowsInFile <= maxRowsInFile) { return; }
        throw new TooManyRowsException(currentRowNumber);
    }

    protected void checkRowSize(int rowSizeInCharacters) throws RowSizeExceededException {
        if (this.maxRowSizeInCharacters <= 0) { return; }
        if (rowSizeInCharacters <= this.maxRowSizeInCharacters) { return; }
        throw new RowSizeExceededException(this.currentRowNumber);
    }

    protected void checkFileSize(int fileSizeInCharacters) throws FileSizeExceededException {
        if (this.maxFileSizeInCharacters <= 0) { return; }
        if (fileSizeInCharacters <= this.maxFileSizeInCharacters) { return; }
        throw new FileSizeExceededException(this.currentRowNumber);
    }

    public int getHeaderCount() {
        return mNumHeaders;
    }

    public String intern(String s) {
        if (s == null) return null;

        if (mTrimStrings) s = s.trim();
        if (mUniqueStrings != null) {
            String new_s = mUniqueStrings.get(s);
            if (new_s == null)
                mUniqueStrings.put(s, s);
            else
                s = new_s;
        }
        return s;
    }

    public void removeInternedString(String s) {
        if (mUniqueStrings == null) return;
        mUniqueStrings.remove(s);
    }

    protected ArrayList<String> getRegularLine() throws IOException {
        if (mAtEOF) return null;
        ArrayList<String> columnValues = new ArrayList<String>(mNumFields);
        try {
            int token = getNextToken();
            StringBuffer field = new StringBuffer();
            while ((token != StreamTokenizer.TT_EOF) && (token != StreamTokenizer.TT_EOL)) {
                if (token == mSeparator) {
                    columnValues.add(intern(field.toString()));
                    checkNumCells(columnValues.size());
                    field = new StringBuffer();
                } else {
                    appendToCell(field, mParser.sval);
                }
                token = getNextToken();
            }
            columnValues.add(intern(field.toString()));
            checkNumCells(columnValues.size());
            if (token == StreamTokenizer.TT_EOF) mAtEOF = true;
        } catch (IOException e) {
            if (e instanceof EOFException)
                mAtEOF = true;
            else
                throw e;
        }

        if (mAtEOF && (columnValues.size() == 0)) return null;

        if (columnValues.size() > mNumFields) mNumFields = columnValues.size();
        return columnValues;
    }

    protected int getNextToken() throws IOException {
        int token;
        if (mHaveLastToken) {
            token = mLastToken;
            mParser.sval = mLastWord;
            mHaveLastToken = false;
        } else
            token = mParser.nextToken();
        if (token != '"')
            // If it's any normal token, just return it
            return token;

        // Now handle quoted strings
        token = mParser.nextToken();
        if (token == '"') {
            // A double quote immediately following an opening double quote means
            // any empty field. This is different than an embedded pair of
            // double quotes, handled below
            mParser.sval = "";
            return StreamTokenizer.TT_WORD;
        }
        if (token == StreamTokenizer.TT_EOF) return token;
        StringBuffer field = new StringBuffer();
        do {
            if (token == '"') {
                // Note that we could only reach this case if it was an embedded
                // double quote, inside a quoted string. The above case handles
                // a double quote immediately after an opening double quote,
                // which indicates an empty value. This case is inside a quoted
                // string. Here, a pair of double quotes indicates a single
                // embedded double quote.
                token = mParser.nextToken();
                if (token == '"') {
                    appendToCell(field, '"');
                } else {
                    mLastToken = token;
                    mLastWord = mParser.sval;
                    mHaveLastToken = true;
                    mParser.sval = field.toString();
                    return StreamTokenizer.TT_WORD;
                }
            } else if (token == StreamTokenizer.TT_EOF) {
                mParser.sval = field.toString();
                return token;
            } else if (token == StreamTokenizer.TT_EOL) {
                appendToCell(field, '\n');
            } else if (token == ',') {
                appendToCell(field, ',');
            } else if (token == '\t') {
                appendToCell(field, '\t');
            } else {
                if (mParser.sval != null)
                    appendToCell(field, mParser.sval);
                else
                    logger.warn("null token value in CSVfile, token is " + token);
            }
            token = mParser.nextToken();
        } while (true);
    }

    /**
     * @return Names of output columns being read during each readRow call
     */
    public List<String> getColumnNames() {
        return headerRow;
    }

    /*
     * (non-Javadoc)
     * @see com.salesforce.dataloader.dao.DataReader#getTotalRows()
     */
    public int getTotalRows() throws DataAccessObjectException {
        if (totalRows == 0) {
            assert isOpen();
            totalRows = DAORowUtil.calculateTotalRows(this);
        }
        return totalRows;
    }

    /**
     * @return Current record number that has been read
     */
    public int getCurrentRowNumber() {
        return currentRowNumber;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
}
