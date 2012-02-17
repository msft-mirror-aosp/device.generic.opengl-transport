/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdkstats;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class to send "ping" usage reports to the server. */
public class SdkStatsService {

    /** Minimum interval between ping, in milliseconds. */
    private static final long PING_INTERVAL_MSEC = 86400 * 1000;  // 1 day

    private DdmsPreferenceStore mStore = new DdmsPreferenceStore();

    public SdkStatsService() {
    }

    /**
     * Send a "ping" to the Google toolbar server, if enough time has
     * elapsed since the last ping, and if the user has not opted out.<br>
     *
     * The ping will not be sent if the user opt out dialog has not been shown yet.
     * Use {@link #getUserPermissionForPing(Shell)} to display the dialog requesting
     * user permissions.<br>
     *
     * Note: The actual ping (if any) is sent in a <i>non-daemon</i> background thread.
     *
     * @param app name to report in the ping
     * @param version to report in the ping
     */
    public void ping(String app, String version) {
        doPing(app, version);
    }

    /**
     * Display a dialog to the user providing information about the ping service,
     * and whether they'd like to opt-out of it.
     *
     * Once the dialog has been shown, it sets a preference internally indicating that the user has
     * viewed this dialog. This setting can be queried using {@link #pingPermissionsSet()}.
     */
    public void checkUserPermissionForPing(Shell parent) {
        if (!mStore.hasPingId()) {
            askUserPermissionForPing(parent);
            mStore.generateNewPingId();
        }
    }

    /**
     * Prompt the user for whether they want to opt out of reporting, and save the user
     * input in preferences.
     */
    private void askUserPermissionForPing(final Shell parent) {
        final Display display = parent.getDisplay();
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                SdkStatsPermissionDialog dialog = new SdkStatsPermissionDialog(parent);
                dialog.open();
                mStore.setPingOptIn(dialog.getPingUserPreference());
            }
        });
    }

    // -------

    /**
     * Pings the usage stats server, as long as the prefs contain the opt-in boolean
     *
     * @param app name to report in the ping
     * @param version to report in the ping
     */
    private void doPing(final String app, String version) {
        // Validate the application and version input.
        final String normalVersion = normalizeVersion(app, version);

        // If the user has not opted in, do nothing and quietly return.
        if (!mStore.isPingOptIn()) {
            // user opted out.
            return;
        }

        // If the last ping *for this app* was too recent, do nothing.
        long now = System.currentTimeMillis();
        long then = mStore.getPingTime(app);
        if (now - then < PING_INTERVAL_MSEC) {
            // too soon after a ping.
            return;
        }

        // Record the time of the attempt, whether or not it succeeds.
        mStore.setPingTime(app, now);

        // Send the ping itself in the background (don't block if the
        // network is down or slow or confused).
        final long id = mStore.getPingId();
        new Thread() {
            @Override
            public void run() {
                try {
                    actuallySendPing(app, normalVersion, id);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * Unconditionally send a "ping" request to the Google toolbar server.
     *
     * @param app name to report in the ping
     * @param version to report in the ping (dotted numbers, no more than four)
     * @param id of the local installation
     * @throws IOException if the ping failed
     */
    private static void actuallySendPing(String app, String version, long id)
                throws IOException {
        String osName  = URLEncoder.encode(getOsName(),  "UTF-8");
        String osArch  = URLEncoder.encode(getOsArch(),  "UTF-8");
        String jvmArch = URLEncoder.encode(getJvmInfo(), "UTF-8");

        // Include the application's name as part of the as= value.
        // Share the user ID for all apps, to allow unified activity reports.

        URL url = new URL(
            "http",                                         //$NON-NLS-1$
            "tools.google.com",                             //$NON-NLS-1$
            "/service/update?as=androidsdk_" + app +        //$NON-NLS-1$
                "&id=" + Long.toHexString(id) +             //$NON-NLS-1$
                "&version=" + version +                     //$NON-NLS-1$
                "&os=" + osName +                           //$NON-NLS-1$
                "&osa=" + osArch +                          //$NON-NLS-1$
                "&vma=" + jvmArch);                         //$NON-NLS-1$

        // Discard the actual response, but make sure it reads OK
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Believe it or not, a 404 response indicates success:
        // the ping was logged, but no update is configured.
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK &&
            conn.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IOException(
                conn.getResponseMessage() + ": " + url);    //$NON-NLS-1$
        }
    }

    /**
     * Detects and reports the host OS: "linux", "win" or "mac".
     * For Windows and Mac also append the version, so for example
     * Win XP will return win-5.1.
     */
    private static String getOsName() {
        String os = System.getProperty("os.name");          //$NON-NLS-1$

        if (os == null || os.length() == 0) {
            return "unknown";                               //$NON-NLS-1$
        }

        if (os.startsWith("Mac OS")) {                      //$NON-NLS-1$
            os = "mac";                                     //$NON-NLS-1$
            String osVers = getOsVersion();
            if (osVers != null) {
                os = os + '-' + osVers;
            }
        } else if (os.startsWith("Windows")) {              //$NON-NLS-1$
            os = "win";                                     //$NON-NLS-1$
            String osVers = getOsVersion();
            if (osVers != null) {
                os = os + '-' + osVers;
            }
        } else if (os.startsWith("Linux")) {                //$NON-NLS-1$
            os = "linux";                                   //$NON-NLS-1$

        } else if (os.length() > 32) {
            // Unknown -- send it verbatim so we can see it
            // but protect against arbitrarily long values
            os = os.substring(0, 32);
        }
        return os;
    }

    /**
     * Detects and returns the OS architecture: x86, x86_64, ppc.
     * This may differ or be equal to the JVM architecture in the sense that
     * a 64-bit OS can run a 32-bit JVM.
     */
    private static String getOsArch() {
        String arch = getJvmArch();

        if ("x86_64".equals(arch)) {                                    //$NON-NLS-1$
            // This is a simple case: the JVM runs in 64-bit so the
            // OS must be a 64-bit one.
            return arch;

        } else if ("x86".equals(arch)) {                                //$NON-NLS-1$
            // This is the misleading case: the JVM is 32-bit but the OS
            // might be either 32 or 64. We can't tell just from this
            // property.
            // Macs are always on 64-bit, so we just need to figure it
            // out for Windows and Linux.

            String os = getOsName();
            if (os.startsWith("win")) {                                 //$NON-NLS-1$
                // When WOW64 emulates a 32-bit environment under a 64-bit OS,
                // it sets PROCESSOR_ARCHITEW6432 to AMD64 or IA64 accordingly.
                // Ref: http://msdn.microsoft.com/en-us/library/aa384274(v=vs.85).aspx

                String w6432 = System.getenv("PROCESSOR_ARCHITEW6432"); //$NON-NLS-1$
                if (w6432 != null && w6432.indexOf("64") != -1) {       //$NON-NLS-1$
                    return "x86_64";                                    //$NON-NLS-1$
                }
            } else if (os.startsWith("linux")) {                        //$NON-NLS-1$
                // Let's try the obvious. This works in Ubuntu and Debian
                String s = System.getenv("HOSTTYPE");                   //$NON-NLS-1$

                s = sanitizeOsArch(s);
                if (s.indexOf("86") != -1) {
                    arch = s;
                }
            }
        }

        return arch;
    }

    /**
     * Returns the version of the OS version if it is defined as X.Y, or null otherwise.
     * <p/>
     * Example of returned versions can be found at http://lopica.sourceforge.net/os.html
     * <p/>
     * This method removes any exiting micro versions.
     */
    private static String getOsVersion() {
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+).*"); //$NON-NLS-1$
        String osVers = System.getProperty("os.version"); //$NON-NLS-1$
        Matcher m = p.matcher(osVers);
        if (m.matches()) {
            return m.group(1) + '.' + m.group(2);
        }

        return null;
    }

    /**
     * Detects and returns the JVM info: version + architecture.
     * Examples: 1.4-ppc, 1.6-x86, 1.7-x86_64
     */
    private static String getJvmInfo() {
        return getJvmVersion() + '-' + getJvmArch();
    }

    /**
     * Returns the major.minor Java version.
     * <p/>
     * The "java.version" property returns something like "1.6.0_20"
     * of which we want to return "1.6".
     */
    private static String getJvmVersion() {
        String version = System.getProperty("java.version");    //$NON-NLS-1$

        if (version == null || version.length() == 0) {
            return "unknown";                                   //$NON-NLS-1$
        }

        Pattern p = Pattern.compile("(\\d+)\\.(\\d+).*");       //$NON-NLS-1$
        Matcher m = p.matcher(version);
        if (m.matches()) {
            return m.group(1) + '.' + m.group(2);
        }

        // Unknown version. Send it as-is within a reasonable size limit.
        if (version.length() > 8) {
            version = version.substring(0, 8);
        }
        return version;
    }

    /**
     * Detects and returns the JVM architecture.
     * <p/>
     * The HotSpot JVM has a private property for this, "sun.arch.data.model",
     * which returns either "32" or "64". However it's not in any kind of spec.
     * <p/>
     * What we want is to know whether the JVM is running in 32-bit or 64-bit and
     * the best indicator is to use the "os.arch" property.
     * - On a 32-bit system, only a 32-bit JVM can run so it will be x86 or ppc.<br/>
     * - On a 64-bit system, a 32-bit JVM will also return x86 since the OS needs
     *   to masquerade as a 32-bit OS for backward compatibility.<br/>
     * - On a 64-bit system, a 64-bit JVM will properly return x86_64.
     * <pre>
     * JVM:       Java 32-bit   Java 64-bit
     * Windows:   x86           x86_64
     * Linux:     x86           x86_64
     * Mac        untested      x86_64
     * </pre>
     */
    private static String getJvmArch() {
        String arch = System.getProperty("os.arch");        //$NON-NLS-1$
        return sanitizeOsArch(arch);
    }

    private static String sanitizeOsArch(String arch) {
        if (arch == null || arch.length() == 0) {
            return "unknown";                               //$NON-NLS-1$
        }

        if (arch.equalsIgnoreCase("x86_64") ||              //$NON-NLS-1$
                arch.equalsIgnoreCase("ia64") ||            //$NON-NLS-1$
                arch.equalsIgnoreCase("amd64")) {           //$NON-NLS-1$
            return "x86_64";                                //$NON-NLS-1$
        }

        if (arch.length() == 4 && arch.charAt(0) == 'i' && arch.lastIndexOf("86") == 2) {
            // Any variation of iX86 counts as x86 (i386, i486, i686).
            return "x86";                                   //$NON-NLS-1$
        }

        if (arch.equalsIgnoreCase("PowerPC")) {             //$NON-NLS-1$
            return "ppc";                                   //$NON-NLS-1$
        }

        // Unknown arch. Send it as-is but protect against arbitrarily long values.
        if (arch.length() > 32) {
            arch = arch.substring(0, 32);
        }
        return arch;
    }

    /**
     * Validate the supplied application version, and normalize the version.
     * @param app to report
     * @param version supplied by caller
     * @return normalized dotted quad version
     */
    private static String normalizeVersion(String app, String version) {
        // Application name must contain only word characters (no punctuation)
        if (!app.matches("\\w+")) {                                             //$NON-NLS-1$
            throw new IllegalArgumentException("Bad app name: " + app);         //$NON-NLS-1$
        }

        // Version must be between 1 and 4 dotted numbers
        String[] numbers = version.split("\\.");                                //$NON-NLS-1$
        if (numbers.length > 4) {
            throw new IllegalArgumentException("Bad version: " + version);      //$NON-NLS-1$
        }
        for (String part: numbers) {
            if (!part.matches("\\d+")) {                                        //$NON-NLS-1$
                throw new IllegalArgumentException("Bad version: " + version);  //$NON-NLS-1$
            }
        }

        // Always output 4 numbers, even if fewer were supplied (pad with .0)
        StringBuffer normal = new StringBuffer(numbers[0]);
        for (int i = 1; i < 4; i++) {
            normal.append('.').append(i < numbers.length ? numbers[i] : "0");   //$NON-NLS-1$
        }
        return normal.toString();
    }
}
