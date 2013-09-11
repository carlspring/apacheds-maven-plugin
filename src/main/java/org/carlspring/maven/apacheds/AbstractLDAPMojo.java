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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * @author mtodorov
 */
public abstract class AbstractLDAPMojo
        extends AbstractMojo
{

    @Parameter(readonly = true, property = "project", required = true)
    private MavenProject project;

    /**
     * ApacheDS Instance name.
     */
    @Parameter( property = "apacheds.instanceName", defaultValue = "exampleInstance" )
    private String instanceName;

    /**
     * ApacheDS Instance path (where to create the server's database)
     */
    @Parameter( property = "apacheds.instancePath", defaultValue = "${project.build.directory}/apacheds-server" )
    private String instancePath;

    /**
     * The base DN
     */
    @Parameter( property = "apacheds.baseDN", defaultValue = "o=exampleOrganization")
    private String baseDN;

    /**
     * Server IP/host
     */
    @Parameter( property = "apacheds.host", defaultValue = "localhost")
    private String host;

    /**
     * Server port
     */
    @Parameter( property = "apacheds.port", defaultValue = "389")
    private int port;

    DirectoryService directoryService;
    LdapServer ldapServer;


    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException
    {
        try
        {
            setupApacheDS();
        }
        catch (IOException e)
        {
            throw new MojoFailureException("IOException while initializing ApacheDS", e);
        }
        catch (LdapException e)
        {
            throw new MojoExecutionException("LdapException while initializing ApacheDS", e);
        }
        catch (NamingException e)
        {
            throw new MojoExecutionException("NamingException while initializing ApacheDS", e);
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Exception while initializing ApacheDS", e);
        }
    }

    protected void setupApacheDS()
            throws Exception, IOException, LdapException, NamingException
    {

        DefaultDirectoryServiceFactory factory = new DefaultDirectoryServiceFactory();
        factory.init(getInstanceName());

        directoryService = factory.getDirectoryService();
        directoryService.getChangeLog().setEnabled(false);
        directoryService.setShutdownHookEnabled(true);

        InstanceLayout il = new InstanceLayout(getInstancePath());
        directoryService.setInstanceLayout(il);

        AvlPartition partition = new AvlPartition(directoryService.getSchemaManager());
        partition.setId(getInstanceName());
        partition.setSuffixDn(new Dn(directoryService.getSchemaManager(), getBaseDN()));
        partition.initialize();
        directoryService.addPartition(partition);

        ldapServer = new LdapServer();
        ldapServer.setTransports(new TcpTransport(getHost(), getPort()));
        ldapServer.setDirectoryService(directoryService);

    }

    public MavenProject getProject()
    {
        return project;
    }

    public void setProject(MavenProject project)
    {
        this.project = project;
    }

    public String getInstanceName()
    {
        return instanceName;
    }

    public void setInstanceName(String instanceName)
    {
        this.instanceName = instanceName;
    }

    public String getInstancePath()
    {
        return instancePath;
    }

    public void setInstancePath(String instancePath)
    {
        this.instancePath = instancePath;
    }

    public String getBaseDN()
    {
        return baseDN;
    }

    public void setBaseDN(String baseDN)
    {
        this.baseDN = baseDN;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}
