/**
 * Copyright(C) 2013 Patrik Dufresne Service Logiciel <info@patrikdufresne.com>
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
package com.patrikdufresne.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Transient;

/**
 * This class represent a database Url
 * 
 * @author patapouf
 * 
 */
public class DatabaseUrl {
    /**
     * File's protocol.
     */
    private static final String SHEME_FILE = "file"; //$NON-NLS-1$

    private static final String SHEME_JDBC_H2_LOCAL = "jdbc:h2"; //$NON-NLS-1$

    private static final String SHEME_JDBC_H2_TCP = "jdbc:h2:tcp"; //$NON-NLS-1$

    /**
     * The database name (included in path)
     */
    @Transient
    private String name;
    /**
     * The url's path.
     */
    private String path;
    /**
     * the url's sheme.
     */
    private String sheme;

    /**
     * Create a new database url.
     * 
     * @param url
     */
    public DatabaseUrl(String url) throws MalformedURLException {
        if (url == null) {
            throw new NullPointerException();
        }

        // Check if a file representation may matches
        String filepath = null;
        try {
            filepath = (new File(url)).getCanonicalPath();
        } catch (IOException e) {
            // Nothing to do
        }

        // Try to find the sheme
        Pattern pattern = Pattern.compile("^([a-z0-9:]+):(.+)", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            this.sheme = matcher.group(1);
            parseName(matcher.group(2));
        } else {
            this.sheme = SHEME_FILE;
            parseName(url);
        }

        // Check if local or remote
        if (isLocal() || isRemote()) {
            return;
        }

        // The current sheme doesn't match, try with the local file
        // representation
        if (filepath != null) {
            this.sheme = SHEME_FILE;
            parseName(filepath);
            if (isLocal() || isRemote()) {
                return;
            }
        }

        throw new MalformedURLException(this.sheme + " (unsupported sheme)"); //$NON-NLS-1$
    }

    /**
     * Return a string representation of a local database url.
     * @return
     */
    public String getAbsolutePath() {
        if (!isLocal()) {
            return null;
        }
        return new File(path).getAbsolutePath();
    }

    /**
     * Return the database name.
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Return true if the url is matching a local url pattern.
     * 
     * @return
     */
    public boolean isLocal() {
        // Check the protocol type
        return sheme.equals(SHEME_FILE) || sheme.equals(SHEME_JDBC_H2_LOCAL);
    }

    /**
     * Return true if the url matches a remote database pattern.
     * 
     * @param url
     * @return
     */
    public boolean isRemote() {
        return sheme.equals(SHEME_JDBC_H2_TCP);
    }

    /**
     * This function return a file object based on the url path. If the url is
     * not local, this function return null.
     * 
     * @return
     */
    public File localfile() {
        if (!isLocal()) {
            return null;
        }
        return new File(path + ".h2.db"); //$NON-NLS-1$
    }

    private void parseName(String path) throws MalformedURLException {
        // Search for the name
        Pattern pattern = Pattern.compile("(.*[/\\\\]([^/\\\\]+?))(\\.h2\\.db)?"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
            this.path = matcher.group(1);
            this.name = matcher.group(2);
        } else {
            throw new MalformedURLException(path + " (no database name)"); //$NON-NLS-1$
        }
    }

    /**
     * Check if the given string is a valid URL pointing to a jdbc database
     * 
     * @param url
     *            the url to validate
     * @return True if the URL is valid
     * @throws IOException
     *             if the file is not readable or writable
     * @throws FileNotFoundException
     *             if the parent directory doesn't exists
     * @throws MalformedURLException
     *             if the url is not valid.
     */
    public void test() throws IOException {
        // Determine the url type, and test it
        if (isRemote()) {
            testRemote();
        } else if (isLocal()) {
            testLocal();
        } else {
            // Should never get their, since the constructor should have test
            // it.
            throw new MalformedURLException();
        }
    }

    /**
     * Check if the file exists.
     * 
     * @param file
     *            the file
     * @return True if the file exists and can be read
     */
    private void testLocal() throws IOException {
        File file = localfile();

        // Test if it's possible to create a canonical name
        file = file.getCanonicalFile();

        if (file.exists() && !file.canRead()) {
            throw new IOException(file.getPath() + " (Permission denied)"); //$NON-NLS-1$
        } else if (file.exists() && !file.canWrite()) {
            throw new IOException(file.getPath() + " (Permission denied)"); //$NON-NLS-1$
        } else if (file.getParentFile() == null || !file.getParentFile().exists()) {
            throw new FileNotFoundException(file.getParent() + " (No such file or directory)"); //$NON-NLS-1$
        } else if (!file.getParentFile().canRead()) {
            throw new IOException(file.getParent() + " (Permission denied)"); //$NON-NLS-1$
        }

    }

    /**
     * This function test if the remote url is valid. If not, it throw an
     * exception.
     */
    private void testRemote() {
        // TODO complete this
    }

    /**
     * Return a string representation of the url.
     */
    @Override
    public String toString() {
        return sheme + ":" + path; //$NON-NLS-1$
    }

}
