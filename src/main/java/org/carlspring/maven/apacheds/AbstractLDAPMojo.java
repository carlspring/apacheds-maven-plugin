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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.api.ldap.model.schema.registries.SchemaLoader;
import org.apache.directory.api.ldap.schemaextractor.SchemaLdifExtractor;
import org.apache.directory.api.ldap.schemaextractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.api.ldap.schemaloader.LdifSchemaLoader;
import org.apache.directory.api.ldap.schemamanager.impl.DefaultSchemaManager;
import org.apache.directory.api.util.exception.Exceptions;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.api.schema.SchemaPartition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmIndex;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.i18n.I18n;
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
    @Parameter(property = "apacheds.instanceName", defaultValue = "exampleInstance")
    private String instanceName;

    /**
     * ApacheDS Instance path (where to create the server's database)
     */
    @Parameter(property = "apacheds.instancePath", defaultValue = "${project.build.directory}/apacheds-server")
    private String instancePath;

    /**
     * ApacheDS partition name
     */
    @Parameter(property = "apacheds.partitionName", defaultValue = "carlspring", required = true)
    private String partitionName;

    @Parameter(property = "apacheds.partitionDN", defaultValue = "o=carlspring", required = true)
    private String partitionDN;

    /**
     * Server IP/host
     */
    @Parameter(property = "apacheds.host", defaultValue = "localhost")
    private String host;

    /**
     * Server port
     */
    @Parameter(property = "apacheds.port", defaultValue = "10389")
    private int port;

    /**
     * The directory service
     */
    private DirectoryService directoryService;

    /**
     * The LDAP server
     */
    private LdapServer ldapServer;


    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException
    {
        try
        {
            initialize();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public void initialize()
            throws Exception
    {
        final File partitionDir = new File(instancePath);
        if (!partitionDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            partitionDir.mkdirs();
        }

        initialiazeDirectoryService(partitionDir);
    }

    /**
     * Add a new partition to the server
     *
     * @param partitionId The partition Id
     * @param partitionDn The partition DN
     * @return The newly added partition
     * @throws Exception If the partition can't be added
     */
    private Partition addPartition(String partitionId,
                                   String partitionDn)
            throws Exception
    {
        // Create a new partition with the given partition id
        JdbmPartition partition = new JdbmPartition(directoryService.getSchemaManager());
        partition.setId(partitionId);
        partition.setPartitionPath(new File(directoryService.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI());
        partition.setSuffixDn(new Dn(partitionDn));
        directoryService.addPartition(partition);

        return partition;
    }

    /**
     * Add a new set of index on the given attributes
     *
     * @param partition The partition on which we want to add index
     * @param attrs     The list of attributes to index
     */
    private void addIndex(Partition partition,
                          String... attrs)
    {
        // Index some attributes on the apache partition
        Set indexedAttributes = new HashSet();

        for (String attribute : attrs)
        {
            //noinspection unchecked
            indexedAttributes.add(new JdbmIndex<String, Entry>(attribute, false));
        }

        //noinspection unchecked
        ((JdbmPartition) partition).setIndexedAttributes(indexedAttributes);
    }

    /**
     * Initialize the schema manager and add the schema partition to directory service.
     *
     * @throws Exception if the schema LDIF files are not found on the classpath
     */
    private void initSchemaPartition()
            throws Exception
    {
        InstanceLayout instanceLayout = directoryService.getInstanceLayout();

        File schemaPartitionDirectory = new File(instanceLayout.getPartitionsDirectory(), "schema");

        // Extract the schema on disk (a brand new one) and load the registries
        if (schemaPartitionDirectory.exists())
        {
            System.out.println("schema partition already exists, skipping schema extraction");
        }
        else
        {
            SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(instanceLayout.getPartitionsDirectory());
            extractor.extractOrCopy();
        }

        SchemaLoader loader = new LdifSchemaLoader(schemaPartitionDirectory);
        SchemaManager schemaManager = new DefaultSchemaManager(loader);

        // We have to load the schema now, otherwise we won't be able
        // to initialize the Partitions, as we won't be able to parse
        // and normalize their suffix Dn
        schemaManager.loadAllEnabled();

        List<Throwable> errors = schemaManager.getErrors();

        if (errors.size() != 0)
        {
            throw new Exception(I18n.err(I18n.ERR_317, Exceptions.printErrors(errors)));
        }

        directoryService.setSchemaManager(schemaManager);

        // Init the LdifPartition with schema
        LdifPartition schemaLdifPartition = new LdifPartition(schemaManager);
        schemaLdifPartition.setPartitionPath(schemaPartitionDirectory.toURI());

        // The schema partition
        SchemaPartition schemaPartition = new SchemaPartition(schemaManager);
        schemaPartition.setWrappedPartition(schemaLdifPartition);
        directoryService.setSchemaPartition(schemaPartition);
    }

    /**
     * Initialize the server. It creates the partition, adds the index, and
     * injects the context entries for the created partitions.
     *
     * @param workDir the directory to be used for storing the data
     * @throws Exception if there were some problems while initializing the system
     */
    private void initialiazeDirectoryService(File workDir)
            throws Exception
    {
        // Initialize the LDAP service
        directoryService = new DefaultDirectoryService();
        directoryService.setInstanceLayout(new InstanceLayout(workDir));

        CacheService cacheService = new CacheService();
        cacheService.initialize(directoryService.getInstanceLayout());

        directoryService.setCacheService(cacheService);

        // first load the schema
        initSchemaPartition();

        // then the system partition
        // this is a MANDATORY partition
        // DO NOT add this via addPartition() method, trunk code complains about duplicate partition
        // while initializing
        JdbmPartition systemPartition = new JdbmPartition(directoryService.getSchemaManager());
        systemPartition.setId("system");
        systemPartition.setPartitionPath(new File(directoryService.getInstanceLayout().getPartitionsDirectory(),
                                                  systemPartition.getId()).toURI());
        systemPartition.setSuffixDn(new Dn(ServerDNConstants.SYSTEM_DN));
        systemPartition.setSchemaManager(directoryService.getSchemaManager());

        // mandatory to call this method to set the system partition
        // Note: this system partition might be removed from trunk
        directoryService.setSystemPartition(systemPartition);

        // Disable the ChangeLog system
        directoryService.getChangeLog().setEnabled(false);
        directoryService.setDenormalizeOpAttrsEnabled(true);

        // Now we can create as many partitions as we need
        // Create some new partitions named 'foo', 'bar' and 'apache'.
        Partition partition = addPartition(partitionName, partitionDN);

        // Index some attributes on the apache partition
        addIndex(partition, "objectClass", "ou", "uid");

        // And start the service
        directoryService.startup();

        // Inject the context entry for o=carlspring partition if it does not already exist
        try
        {
            directoryService.getAdminSession().lookup(partition.getSuffixDn());
        }
        catch (LdapException lnnfe)
        {
            Dn dnFoo = new Dn("o=carlspring");
            Entry entryFoo = directoryService.newEntry(dnFoo);
            entryFoo.add("o", "carlspring");
            entryFoo.add("objectClass", "top");
            entryFoo.add("objectClass", "organization");
            directoryService.getAdminSession().add(entryFoo);
        }
    }

    /**
     * Starts the LdapServer
     *
     * @throws Exception
     */
    public void startServer()
            throws Exception
    {
        getLog().info("Starting LDAP server...");
        ldapServer = new LdapServer();
        ldapServer.setTransports(new TcpTransport(port));
        ldapServer.setDirectoryService(directoryService);
        ldapServer.start();
    }

    /**
     * Stops the LdapServer
     *
     * @throws Exception
     */
    public void stopServer()
            throws Exception
    {
        getLog().info("Stopping LDAP server...");
        if (!ldapServer.isStarted())
        {
            throw new Exception("Service is not running");
        }
        directoryService.shutdown();
        ldapServer.stop();
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

    public String getPartitionName()
    {
        return partitionName;
    }

    public void setPartitionName(String partitionName)
    {
        this.partitionName = partitionName;
    }

    public String getPartitionDN()
    {
        return partitionDN;
    }

    public void setPartitionDN(String partitionDN)
    {
        this.partitionDN = partitionDN;
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

    public DirectoryService getDirectoryService()
    {
        return directoryService;
    }

    public void setDirectoryService(DirectoryService directoryService)
    {
        this.directoryService = directoryService;
    }

    public LdapServer getLdapServer()
    {
        return ldapServer;
    }

    public void setLdapServer(LdapServer ldapServer)
    {
        this.ldapServer = ldapServer;
    }

}
