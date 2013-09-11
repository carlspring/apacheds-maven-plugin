package org.carlspring.maven.apacheds;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * @author jstiefel
 * @since 6/11/12
 */
public abstract class AbstractLDAPMojoTest
        extends AbstractMojoTestCase
{

    protected static final String TARGET_TEST_CLASSES = "target/test-classes";
    protected static final String POM_PLUGIN = TARGET_TEST_CLASSES + "/poms/pom-start.xml";

    protected void configureMojo(AbstractLDAPMojo mojo)
    {
        mojo.setHost("127.0.0.1");
        mojo.setPort(10389); // Currently not working.
        mojo.setInstanceName("examplePluginInstance");
        mojo.setInstancePath(System.getProperty("basedir") + "/target/apacheds");
        mojo.setBaseDN("o=examplePluginInstanceOrganization");

        try
        {
            mojo.setupApacheDS();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
