package org.carlspring.maven.apacheds;

/**
 * Copyright 2013 Carlspring Consulting & Development Ltd.
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

import java.io.File;

import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author mtodorov
 */
@Mojo(name = "import", requiresProject = false)
public class ImportLDIFMojo
        extends AbstractLDAPMojo
{

    @Parameter(property = "apacheds.importLdif")
    private String ldifFile;


    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException
    {
        try
        {
            // Initialize server configuration
            initialize();
            // Start server
            startServer();
            // Import
            importLDIF();

            // TODO: Remove this
            getLog().info("Sleeping for a minute...");
            Thread.sleep(60000);

            // Start server
            stopServer();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Import an LDIF file
     */
    public void importLDIF()
            throws MojoExecutionException
    {
        if (!ldifFile.isEmpty())
        {
            File file = new File(ldifFile);
            if (file.exists())
            {
                getLog().info("Importing LDIF file " + file.toString() + "...");

                LdifFileLoader ldifLoader = new LdifFileLoader(getDirectoryService().getAdminSession(), file.toString());
                ldifLoader.execute();

                getLog().info("Imported LDIF file " + file.toString() + "...");
            }
            else
            {
                throw new MojoExecutionException("Importing LDIF file failed because the specified file could not be located!");
            }
        }
    }

    public String getLdifFile()
    {
        return ldifFile;
    }

    public void setLdifFile(String ldifFile)
    {
        this.ldifFile = ldifFile;
    }

}
