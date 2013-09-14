package org.carlspring.maven.apacheds;

/**
 * Copyright 2012 Martin Todorov.
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

/**
 * @author mtodorov
 */
public class ImportLDIFMojoTest
        extends AbstractLDAPMojoTest
{

    StartLDAPMojo startMojo;
    ImportLDIFMojo importMojo;
    StopLDAPMojo stopMojo;


    protected void setUp()
            throws Exception
    {
        super.setUp();

        startMojo = (StartLDAPMojo) lookupMojo("start", POM_PLUGIN);
        configureMojo(startMojo);

        stopMojo = (StopLDAPMojo) lookupMojo("stop", POM_PLUGIN);
        configureMojo(stopMojo);

        importMojo = (ImportLDIFMojo) lookupMojo("import", POM_PLUGIN);
        configureMojo(importMojo);
        importMojo.setLdifFile(System.getProperty("basedir") + "/src/test/resources/ldap/apacheds.ldif");
    }

    public void testMojo()
            throws Exception
    {
        startMojo.execute();
        importMojo.execute();
        stopMojo.execute();
    }

}
