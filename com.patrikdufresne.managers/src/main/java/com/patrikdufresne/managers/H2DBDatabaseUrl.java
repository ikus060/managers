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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represent a database Url
 * 
 * Ref.: http://www.h2database.com/html/features.html
 * 
 * @author Patrik Dufresne
 * 
 */
class H2DBDatabaseUrl {
    /**
     * File's protocol.
     */
    private static final String SCHEME_FILE = "file"; //$NON-NLS-1$

    private static final String SCHEME_JDBC_H2_LOCAL1 = "jdbc:h2"; //$NON-NLS-1$

    private static final String SCHEME_JDBC_H2_LOCAL2 = "jdbc:h2:file"; //$NON-NLS-1$

    private static final String SCHEME_JDBC_H2_MEM = "jdbc:h2:mem"; //$NON-NLS-1$

    private static final String SCHEME_JDBC_H2_TCP = "jdbc:h2:tcp"; //$NON-NLS-1$

    /**
     * The url's path.
     */
    private String path;
    /**
     * the url's sheme.
     */
    private String scheme;

    /**
     * Create a new database url.
     * 
     * @param url
     */
    public H2DBDatabaseUrl(String url) throws MalformedURLException {
        if (url == null) {
            throw new IllegalArgumentException();
        }

        // Try to find the scheme
        Pattern pattern = Pattern.compile("^([a-z0-9:]+):(.*)", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()) {
            this.scheme = matcher.group(1);
            this.path = parseName(matcher.group(2));
        } else {
            this.scheme = SCHEME_FILE;
            this.path = parseName(url);
        }

        // Check if local or remote
        if (isLocal() || isRemote() || isMemory()) {
            return;
        }

        // The current scheme doesn't match, try with the local file
        // representation
        // Check if a file representation may matches
        String filepath = null;
        try {
            filepath = (new File(url)).getCanonicalPath();
        } catch (IOException e) {
            // Nothing to do
        }
        if (filepath != null) {
            this.scheme = SCHEME_FILE;
            this.path = parseName(filepath);
            if (isLocal() || isRemote()) {
                return;
            }
        }

        throw new MalformedURLException(this.scheme + " (unsupported sheme)"); //$NON-NLS-1$
    }

    /**
     * Return a string representation of a local database url.
     * 
     * @return
     */
    public String getAbsolutePath() {
        if (!isLocal()) {
            return null;
        }
        try {
            return new File(this.path.replaceFirst(";.*", "")).getCanonicalPath();
        } catch (IOException e) {
            return new File(this.path.replaceFirst(";.*", "")).getAbsolutePath();
        }
    }

    /**
     * Return the database name.
     * 
     * @return
     */
    public String getName() {
        return new File(this.path.replaceFirst(";.*", "")).getName();
    }

    public boolean isInMemory() {
        return this.scheme.equals(SCHEME_JDBC_H2_MEM);
    }

    /**
     * Return true if the url is matching a local url pattern.
     * 
     * @return
     */
    public boolean isLocal() {
        // Check the protocol type
        return this.scheme.equals(SCHEME_FILE) || this.scheme.equals(SCHEME_JDBC_H2_LOCAL1) || this.scheme.equals(SCHEME_JDBC_H2_LOCAL2);
    }

    /**
     * Return true if the url define a in-memory database.
     * 
     * @return
     */
    public boolean isMemory() {
        return this.scheme.equals(SCHEME_JDBC_H2_MEM);
    }

    /**
     * Return true if the url matches a remote database pattern.
     * 
     * @param url
     * @return
     */
    public boolean isRemote() {
        return this.scheme.equals(SCHEME_JDBC_H2_TCP);
    }

    private String parseName(String path) throws MalformedURLException {
        // Search for the name
        Pattern pattern = Pattern.compile("(.*?)(\\.h2\\.db)?$"); //$NON-NLS-1$
        Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new MalformedURLException(path + " (no database name)"); //$NON-NLS-1$
    }

    /**
     * Return a string representation of the url.
     */
    @Override
    public String toString() {
        return this.scheme + ":" + this.path; //$NON-NLS-1$
    }

}