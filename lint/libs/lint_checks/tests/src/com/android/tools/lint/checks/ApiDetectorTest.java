/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class ApiDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ApiDetector();
    }

    public void testXmlApi1() throws Exception {
        assertEquals(
                "layout.xml:15: Error: View requires API level 11 (current min is 1): <CalendarView>\n" +
                "layout.xml:21: Error: View requires API level 14 (current min is 1): <GridLayout>\n" +
                "layout.xml:9: Error: View requires API level 5 (current min is 1): <QuickContactBadge>",

                lintProject(
                    "apicheck/minsdk1.xml=>AndroidManifest.xml",
                    "apicheck/layout.xml=>res/layout/layout.xml"
                    ));
    }

    public void testXmlApi14() throws Exception {
        assertEquals(
                "No warnings.",

                lintProject(
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "apicheck/layout.xml=>res/layout/layout.xml"
                    ));
    }

    public void testApi1() throws Exception {
        assertEquals(
            "ApiCallTest.java:20: Error: Call requires API level 11 (current min is 1): android.app.Activity#getActionBar\n" +
            "ApiCallTest.java:20: Error: Class requires API level 8 (current min is 1): org.w3c.dom.DOMLocator\n" +
            "ApiCallTest.java:23: Error: Class requires API level 8 (current min is 1): org.w3c.dom.DOMError\n" +
            "ApiCallTest.java:24: Error: Class requires API level 8 (current min is 1): org.w3c.dom.DOMErrorHandler\n" +
            "ApiCallTest.java:27: Error: Call requires API level 3 (current min is 1): android.widget.Chronometer#getOnChronometerTickListener\n" +
            "ApiCallTest.java:30: Error: Call requires API level 11 (current min is 1): android.widget.Chronometer#setTextIsSelectable\n" +
            "ApiCallTest.java:33: Error: Field requires API level 11 (current min is 1): dalvik.bytecode.OpcodeInfo#MAXIMUM_VALUE\n" +
            "ApiCallTest.java:38: Error: Class requires API level 14 (current min is 1): android.app.ApplicationErrorReport.BatteryInfo\n" +
            "ApiCallTest.java:38: Error: Field requires API level 14 (current min is 1): android.app.ApplicationErrorReport#batteryInfo\n" +
            "ApiCallTest.java:41: Error: Field requires API level 11 (current min is 1): android.graphics.PorterDuff$Mode#OVERLAY\n" +
            "ApiCallTest.java:46: Error: Class requires API level 14 (current min is 1): android.widget.GridLayout\n" +
            "ApiCallTest.java:50: Error: Class requires API level 14 (current min is 1): android.app.ApplicationErrorReport",

            lintProject(
                "apicheck/classpath=>.classpath",
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "apicheck/ApiCallTest.java.txt=>src/foo/bar/ApiCallTest.java",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class"
                ));
    }

    public void testApi2() throws Exception {
        assertEquals(
            "ApiCallTest.java:20: Error: Call requires API level 11 (current min is 2): android.app.Activity#getActionBar\n" +
            "ApiCallTest.java:20: Error: Class requires API level 8 (current min is 2): org.w3c.dom.DOMLocator\n" +
            "ApiCallTest.java:23: Error: Class requires API level 8 (current min is 2): org.w3c.dom.DOMError\n" +
            "ApiCallTest.java:24: Error: Class requires API level 8 (current min is 2): org.w3c.dom.DOMErrorHandler\n" +
            "ApiCallTest.java:27: Error: Call requires API level 3 (current min is 2): android.widget.Chronometer#getOnChronometerTickListener\n" +
            "ApiCallTest.java:30: Error: Call requires API level 11 (current min is 2): android.widget.Chronometer#setTextIsSelectable\n" +
            "ApiCallTest.java:33: Error: Field requires API level 11 (current min is 2): dalvik.bytecode.OpcodeInfo#MAXIMUM_VALUE\n" +
            "ApiCallTest.java:38: Error: Class requires API level 14 (current min is 2): android.app.ApplicationErrorReport.BatteryInfo\n" +
            "ApiCallTest.java:38: Error: Field requires API level 14 (current min is 2): android.app.ApplicationErrorReport#batteryInfo\n" +
            "ApiCallTest.java:41: Error: Field requires API level 11 (current min is 2): android.graphics.PorterDuff$Mode#OVERLAY\n" +
            "ApiCallTest.java:46: Error: Class requires API level 14 (current min is 2): android.widget.GridLayout\n" +
            "ApiCallTest.java:50: Error: Class requires API level 14 (current min is 2): android.app.ApplicationErrorReport",

            lintProject(
                "apicheck/classpath=>.classpath",
                "apicheck/minsdk2.xml=>AndroidManifest.xml",
                "apicheck/ApiCallTest.java.txt=>src/foo/bar/ApiCallTest.java",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class"
                ));
    }

    public void testApi4() throws Exception {
        assertEquals(
            "ApiCallTest.java:20: Error: Call requires API level 11 (current min is 4): android.app.Activity#getActionBar\n" +
            "ApiCallTest.java:20: Error: Class requires API level 8 (current min is 4): org.w3c.dom.DOMLocator\n" +
            "ApiCallTest.java:23: Error: Class requires API level 8 (current min is 4): org.w3c.dom.DOMError\n" +
            "ApiCallTest.java:24: Error: Class requires API level 8 (current min is 4): org.w3c.dom.DOMErrorHandler\n" +
            "ApiCallTest.java:30: Error: Call requires API level 11 (current min is 4): android.widget.Chronometer#setTextIsSelectable\n" +
            "ApiCallTest.java:33: Error: Field requires API level 11 (current min is 4): dalvik.bytecode.OpcodeInfo#MAXIMUM_VALUE\n" +
            "ApiCallTest.java:38: Error: Class requires API level 14 (current min is 4): android.app.ApplicationErrorReport.BatteryInfo\n" +
            "ApiCallTest.java:38: Error: Field requires API level 14 (current min is 4): android.app.ApplicationErrorReport#batteryInfo\n" +
            "ApiCallTest.java:41: Error: Field requires API level 11 (current min is 4): android.graphics.PorterDuff$Mode#OVERLAY\n" +
            "ApiCallTest.java:46: Error: Class requires API level 14 (current min is 4): android.widget.GridLayout\n" +
            "ApiCallTest.java:50: Error: Class requires API level 14 (current min is 4): android.app.ApplicationErrorReport",

            lintProject(
                "apicheck/classpath=>.classpath",
                "apicheck/minsdk4.xml=>AndroidManifest.xml",
                "apicheck/ApiCallTest.java.txt=>src/foo/bar/ApiCallTest.java",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class"
                ));
    }

    public void testApi10() throws Exception {
        assertEquals(
            "ApiCallTest.java:20: Error: Call requires API level 11 (current min is 10): android.app.Activity#getActionBar\n" +
            "ApiCallTest.java:30: Error: Call requires API level 11 (current min is 10): android.widget.Chronometer#setTextIsSelectable\n" +
            "ApiCallTest.java:33: Error: Field requires API level 11 (current min is 10): dalvik.bytecode.OpcodeInfo#MAXIMUM_VALUE\n" +
            "ApiCallTest.java:38: Error: Class requires API level 14 (current min is 10): android.app.ApplicationErrorReport.BatteryInfo\n" +
            "ApiCallTest.java:38: Error: Field requires API level 14 (current min is 10): android.app.ApplicationErrorReport#batteryInfo\n" +
            "ApiCallTest.java:41: Error: Field requires API level 11 (current min is 10): android.graphics.PorterDuff$Mode#OVERLAY\n" +
            "ApiCallTest.java:46: Error: Class requires API level 14 (current min is 10): android.widget.GridLayout\n" +
            "ApiCallTest.java:50: Error: Class requires API level 14 (current min is 10): android.app.ApplicationErrorReport",

            lintProject(
                "apicheck/classpath=>.classpath",
                "apicheck/minsdk10.xml=>AndroidManifest.xml",
                "apicheck/ApiCallTest.java.txt=>src/foo/bar/ApiCallTest.java",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class"
                ));
        }

    public void testApi14() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                "apicheck/classpath=>.classpath",
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "apicheck/ApiCallTest.java.txt=>src/foo/bar/ApiCallTest.java",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class"
                ));
    }

    // Test suppressing errors -- on classes, methods etc.

    public void testSuppress() throws Exception {
        assertEquals(
            // These errors are correctly -not- suppressed because they
            // appear in method3 (line 74-98) which is annotated with a
            // @SuppressLint annotation specifying only an unrelated issue id
            "SuppressTest1.java:76: Error: Call requires API level 11 (current min is 1): android.app.Activity#getActionBar\n" +
            "SuppressTest1.java:76: Error: Class requires API level 8 (current min is 1): org.w3c.dom.DOMLocator\n" +
            "SuppressTest1.java:79: Error: Class requires API level 8 (current min is 1): org.w3c.dom.DOMError\n" +
            "SuppressTest1.java:80: Error: Class requires API level 8 (current min is 1): org.w3c.dom.DOMErrorHandler\n" +
            "SuppressTest1.java:83: Error: Call requires API level 3 (current min is 1): android.widget.Chronometer#getOnChronometerTickListener\n" +
            "SuppressTest1.java:86: Error: Call requires API level 11 (current min is 1): android.widget.Chronometer#setTextIsSelectable\n" +
            "SuppressTest1.java:89: Error: Field requires API level 11 (current min is 1): dalvik.bytecode.OpcodeInfo#MAXIMUM_VALUE\n" +
            "SuppressTest1.java:94: Error: Class requires API level 14 (current min is 1): android.app.ApplicationErrorReport.BatteryInfo\n" +
            "SuppressTest1.java:94: Error: Field requires API level 14 (current min is 1): android.app.ApplicationErrorReport#batteryInfo\n" +
            "SuppressTest1.java:97: Error: Field requires API level 11 (current min is 1): android.graphics.PorterDuff$Mode#OVERLAY\n" +

            // Note: These annotations are within the methods, not ON the methods, so they have
            // no effect (because they don't end up in the bytecode)
            "SuppressTest4.java:16: Error: Class requires API level 14 (current min is 1): android.app.ApplicationErrorReport\n" +
            "SuppressTest4.java:19: Error: Class requires API level 14 (current min is 1): android.app.ApplicationErrorReport.BatteryInfo\n" +
            "SuppressTest4.java:19: Error: Field requires API level 14 (current min is 1): android.app.ApplicationErrorReport#batteryInfo",

            lintProject(
                "apicheck/classpath=>.classpath",
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "apicheck/SuppressTest1.java.txt=>src/foo/bar/SuppressTest1.java",
                "apicheck/SuppressTest1.class.data=>bin/classes/foo/bar/SuppressTest1.class",
                "apicheck/SuppressTest2.java.txt=>src/foo/bar/SuppressTest2.java",
                "apicheck/SuppressTest2.class.data=>bin/classes/foo/bar/SuppressTest2.class",
                "apicheck/SuppressTest3.java.txt=>src/foo/bar/SuppressTest3.java",
                "apicheck/SuppressTest3.class.data=>bin/classes/foo/bar/SuppressTest3.class",
                "apicheck/SuppressTest4.java.txt=>src/foo/bar/SuppressTest4.java",
                "apicheck/SuppressTest4.class.data=>bin/classes/foo/bar/SuppressTest4.class"
                ));
    }
}
