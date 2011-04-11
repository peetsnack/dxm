package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.jackrabbit.core.security.authentication.AuthContext;

import javax.jcr.AccessDeniedException;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;
import java.util.HashMap;
import java.util.Map;

/**
 * Jackrabbit XASession extension for jahia
 */
public class JahiaSessionImpl extends XASessionImpl {
    private NodeTypeInstanceHandler myNtInstanceHandler;
    private Map<String, Object> jahiaAttributes;

    public JahiaSessionImpl(RepositoryContext repositoryContext, AuthContext loginContext, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        super(repositoryContext, loginContext, wspConfig);
        init();
    }

    public JahiaSessionImpl(RepositoryContext repositoryContext, Subject subject, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        super(repositoryContext, subject, wspConfig);
        init();
    }

    private void init() {
        jahiaAttributes = new HashMap<String, Object>();
    }

    // @Override

    public JahiaNodeTypeInstanceHandler getNodeTypeInstanceHandler() throws RepositoryException {
        if (myNtInstanceHandler == null) {
            myNtInstanceHandler = super.getNodeTypeInstanceHandler();
        }
        return (JahiaNodeTypeInstanceHandler) myNtInstanceHandler;
    }

    public String getPrefix(String uri) throws NamespaceException {
        try {
            return getNamespacePrefix(uri);
        } catch (NamespaceException e) {
            return repositoryContext.getNamespaceRegistry().getPrefix(uri);
        } catch (RepositoryException e) {
            throw new NamespaceException("Namespace not found: " + uri, e);
        }
    }

    public String getURI(String prefix) throws NamespaceException {
        try {
            return getNamespaceURI(prefix);
        } catch (NamespaceException e) {
            return repositoryContext.getNamespaceRegistry().getURI(prefix);
        } catch (RepositoryException e) {
            throw new NamespaceException("Namespace not found: " + prefix, e);
        }
    }

    public void setJahiaAttributes(String attributeName, Object attributeValue) {
        jahiaAttributes.put(attributeName, attributeValue);
    }

    public void toggleThisSessionAsAliased() {
        setJahiaAttributes("isAliasedUser", Boolean.TRUE);
        JahiaAccessManager accessManager = (JahiaAccessManager)context.getAccessManager();
        if (jahiaAttributes.containsKey("isAliasedUser") && (Boolean) jahiaAttributes.get("isAliasedUser")) {
            accessManager.setAliased(true);
        }
    }
}
