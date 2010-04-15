package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.PublicationInfo;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:15:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationHelper {
    private static Logger logger = Logger.getLogger(PublicationHelper.class);

    private JCRPublicationService publicationService ;

    public void setPublicationService(JCRPublicationService publicationService) {
        this.publicationService = publicationService;
    }

    /**
     * Get the publication status information for a particular path.
     *
     * @param uuid to get publication info from
     * @param currentUserSession
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public GWTJahiaPublicationInfo getPublicationInfo(String uuid, Set<String> languages, boolean full, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            PublicationInfo pubInfo = publicationService.getPublicationInfo(uuid, languages, full, true);
            return convert(pubInfo, full, currentUserSession);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

    }

    private GWTJahiaPublicationInfo convert(PublicationInfo pubInfo, boolean full, JCRSessionWrapper currentUserSession) {
        GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(pubInfo.getPath(), pubInfo.getStatus(), pubInfo.isCanPublish());
        if (full) {
            try {
                JCRNodeWrapper n = currentUserSession.getNode(pubInfo.getPath());
                if (n.hasProperty("jcr:title")) {
                    gwtInfo.setTitle(n.getProperty("jcr:title").getString());
                } else {
                    gwtInfo.setTitle(n.getName());
                }
            } catch (RepositoryException e) {
                gwtInfo.setTitle(pubInfo.getPath());
            }
        }

        Map<String, GWTJahiaPublicationInfo> gwtInfos = new HashMap<String, GWTJahiaPublicationInfo>();
        gwtInfos.put(pubInfo.getPath(), gwtInfo);
        for (Map.Entry<String, PublicationInfo> entry : pubInfo.getSubnodes().entrySet()) {
            PublicationInfo pi = entry.getValue();
            if (entry.getKey().contains("/j:translation")) {
                String key = StringUtils.substringBeforeLast(entry.getKey(), "/j:translation");
                GWTJahiaPublicationInfo lastPub = gwtInfos.get(key);
                if (lastPub != null) {
                    if (pi.getStatus() != GWTJahiaPublicationInfo.UNPUBLISHABLE && pi.getStatus() > gwtInfo.getStatus()) {
                        lastPub.setStatus(pi.getStatus());
                    }
                    if (gwtInfo.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED && pi.getStatus() != GWTJahiaPublicationInfo.UNPUBLISHED) {
                        lastPub.setStatus(pi.getStatus());
                    }
                }
            } else if (full && entry.getKey().indexOf("/j:translation") == -1) {
                GWTJahiaPublicationInfo lastPub = convert(pi, full, currentUserSession);
                gwtInfo.add(lastPub);
                gwtInfos.put(lastPub.getPath(), lastPub);
            }
            gwtInfo.addSubnodesStatus(pi.getStatus());
        }

        for (String p : pubInfo.getReferences().keySet()) {
            PublicationInfo pi = pubInfo.getReferences().get(p);
            gwtInfo.add(convert(pi, full, currentUserSession));
        }

        return gwtInfo;
    }

    /**
     * Publish a node into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param path          Path of the node to publish
     * @param languages     Set of languages to publish if null publish all languages
     * @param reverse
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void publish(String path, Set<String> languages, boolean allSubTree, boolean reverse, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            if (reverse) {
                publicationService.publish(path, Constants.LIVE_WORKSPACE, session.getWorkspace().getName(), languages, false, allSubTree);
            } else {
                publicationService.publish(path, session.getWorkspace().getName(), Constants.LIVE_WORKSPACE, languages, false, allSubTree);
            }
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Publish a list of nodes into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param paths         list of paths of the nodes to publish
     * @param languages     Set of languages to publish if null publish all languages
     * @param user          the user for obtaining the jcr session
     * @param publishParent Recursively publish the parents
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void publish(List<String> paths, Set<String> languages, JahiaUser user, boolean publishParent, JCRSessionWrapper session) throws GWTJahiaServiceException {
        // TODO implement me!
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param path      path of the node to unpublish
     * @param languages Set of languages to unpublish if null unpublish all languages
     * @param user      the user for obtaining the jcr session
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          in case of any RepositoryException
     */
    public void unpublish(String path, Set<String> languages, JahiaUser user) throws GWTJahiaServiceException {
        try {
            publicationService.unpublish(path, languages);
        } catch (RepositoryException e) {
            logger.error("repository exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }
}
