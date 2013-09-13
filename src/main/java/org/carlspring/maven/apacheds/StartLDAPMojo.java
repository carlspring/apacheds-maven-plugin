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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author mtodorov
 * @author stodorov
 */
@Mojo(name = "start", requiresProject = false)
public class StartLDAPMojo
        extends AbstractLDAPMojo
{

    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException
    {
        try
        {
            initialize();
            startServer();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public void startServer()
            throws Exception
    {
        if (getLdapServer() != null && getLdapServer().isStarted())
        {
            throw new IllegalStateException("The ApacheDS service is already running!");
        }

        getDirectoryService().startup();
        getLdapServer().start();
    }

}
