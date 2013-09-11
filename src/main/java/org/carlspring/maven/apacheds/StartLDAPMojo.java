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

import javax.naming.NamingException;
import java.io.IOException;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.factory.DefaultDirectoryServiceFactory;
import org.apache.directory.server.core.partition.impl.avl.AvlPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.bouncycastle.util.IPAddress;

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
            startServer();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Could not start server",e);
        }


    }

    public void startServer()
            throws Exception, IllegalStateException
    {
        if (ldapServer.isStarted())
        {
            throw new IllegalStateException("Service already running");
        }
        directoryService.startup();
        ldapServer.start();
    }


}
