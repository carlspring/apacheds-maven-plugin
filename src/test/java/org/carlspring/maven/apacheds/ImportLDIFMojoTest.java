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

    ImportLDIFMojo importMojo;

    StopLDAPMojo stopMojo;


    protected void setUp()
            throws Exception
    {
        super.setUp();

        importMojo = (ImportLDIFMojo) lookupMojo("import", POM_PLUGIN);
        configureMojo(importMojo);
        stopMojo = (StopLDAPMojo) lookupMojo("stop", POM_PLUGIN);
        configureMojo(stopMojo);
    }

    public void testMojo()
            throws Exception
    {

        System.out.println("Stopping the server ...");
        stopMojo.execute();
    }

}
