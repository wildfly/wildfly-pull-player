/**
 * Internal Use Only
 *
 * Copyright 2011 Red Hat, Inc. All rights reserved.
 */
package org.jboss.pull.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Jason T. Greene
 */
public class Jobs {
    private static final File jobDir = new File(Util.BASE_DIR, "completed-jobs");

    public static String getCompletedJob(int pull, String sha1) {
        if (sha1 == null) {
            return null;
        }
        String build = readBuild(new File(jobDir, key(pull, sha1)), pull);
        if (build != null) {
            return build;
        }
        // UPGRADE COMPATIBILITY: jobs completed before the key included the pull number were stored under the sha alone,
        // honor those as well, but only when they were recorded for this very pull
        return readBuild(new File(jobDir, sha1), pull);
    }

    public static void storeCompletedJob(int pull, String sha1, int build) {
        jobDir.mkdir();
        File file = new File(jobDir, key(pull, sha1));
        PrintWriter writer = null;
        try {
            file.createNewFile();
            writer = new PrintWriter(new FileWriter(file));
            writer.println(pull);
            writer.println(build);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            Util.safeClose(writer);
        }
    }

    public static void remove(int pull, String sha1) {
        if (sha1 == null) {
            return;
        }
        new File(jobDir, key(pull, sha1)).delete();
        // only drop a legacy sha-only entry when it belongs to this pull
        File legacy = new File(jobDir, sha1);
        if (readBuild(legacy, pull) != null) {
            legacy.delete();
        }
    }

    private static String key(int pull, String sha1) {
        return pull + "-" + sha1;
    }

    /**
     * Reads the build number out of a completed job file, provided the file was written for
     * {@code pull}. Returns {@code null} if the file is missing, unreadable, incomplete, or
     * records a different pull.
     */
    private static String readBuild(File job, int pull) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(job));
        } catch (FileNotFoundException e) {
            return null;
        }

        try {
            String storedPull = reader.readLine();
            String build = reader.readLine();
            if (storedPull == null || build == null) {
                return null;
            }
            if (Integer.parseInt(storedPull.trim()) != pull) {
                return null;
            }
            return build.trim();
        } catch (IOException | NumberFormatException e) {
            return null;
        } finally {
            Util.safeClose(reader);
        }
    }
}
