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

    @Parameter(property = "basedir")
    private String basedir;

    /**
     * The port to start Derby on.
     */
    @Parameter(property = "ldap.port")
    private int port;

    /**
     * The username to use when authenticating.
     */
    @Parameter(property = "ldap.username")
    private String username;

    /**
     * The password to use when authenticating.
     */
    @Parameter(property = "ldap.password")
    private String password;

    /**
     * The directory to place the apacheds files in.
     */
    @Parameter(property = "ldap.home", defaultValue = "${project.build.directory}/apacheds")
    private String ldapHome;


    @Override
    public void execute()
            throws MojoExecutionException, MojoFailureException
    {
        setupApacheDS();
    }

    protected void setupApacheDS()
            throws MojoExecutionException
    {
        // TODO
    }

    public MavenProject getProject()
    {
        return project;
    }

    public void setProject(MavenProject project)
    {
        this.project = project;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

    public String getLdapHome()
    {
        return ldapHome;
    }

    public void setLdapHome(String ldapHome)
    {
        this.ldapHome = ldapHome;
    }

}
