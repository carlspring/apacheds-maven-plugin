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
        mojo.setLdapHome(System.getProperty("basedir") + "/target/apacheds");
        mojo.setPort(10389);
        mojo.setUsername("admin");
        mojo.setPassword("secret");
    }

}
