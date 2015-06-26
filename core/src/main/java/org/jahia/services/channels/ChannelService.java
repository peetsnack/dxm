/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.channels;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * The channel service is the main service to access, retrieve, query, list and maybe even update channels
 */
public class ChannelService {

    List<ChannelProvider> channelProviders = new ArrayList<ChannelProvider>();

    private Map<String,Channel> channelMap = new HashMap<String,Channel>();

    private static volatile ChannelService instance = new ChannelService();

    public static ChannelService getInstance() {
        return instance;
    }

    public Channel getChannel(String identifier) {
        Channel result = channelMap.get(identifier);
        if (result != null) {
            return result;
        }
        result = new Channel();
        result.setIdentifier(identifier);
        for (ChannelProvider provider : channelProviders) {
            Map<String, String> channelCapabilities = provider.getChannelCapabilities(identifier);
            if (channelCapabilities != null) {
                result.getCapabilities().putAll(channelCapabilities);
            }
            if (provider.getFallBack(identifier) != null) {
                result.setFallBack(provider.getFallBack(identifier));
            }
            if (provider.getAllChannels().contains(identifier)) {
                result.setVisible(provider.isVisible(identifier));
            }
        }
        channelMap.put(identifier, result);
        return result;
    }

    public Channel resolveChannel(HttpServletRequest request) {
        String result = null;
        for (ChannelProvider provider : channelProviders) {
            result = provider.resolveChannel(request);
            if (result != null) {
                return getChannel(result);
            }
        }
        return null;
    }

    public List<String> getAllChannels() {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        for (ChannelProvider provider : channelProviders) {
            result.addAll(provider.getAllChannels());
        }
        return new ArrayList<String>(result);
    }

    public void addProvider(ChannelProvider provider) {
        if (channelProviders.contains(provider)) {
            channelProviders.remove(provider);
        }
        channelProviders.add(provider);
        channelMap.clear();
        Collections.sort(channelProviders, new Comparator<ChannelProvider>() {
            public int compare(ChannelProvider o1, ChannelProvider o2) {
                int i = o1.getPriority() - o2.getPriority();
                return i != 0 ? i : 1;
            }
        });
    }

    public boolean matchChannel(String channelId, Channel channel) {
        if (channel != null) {
            if (channelId.equals(channel.getIdentifier())) {
                return true;
            } else {
                if (channel.getFallBack() != null && !channel.getFallBack().equals("root")) {
                    return matchChannel(channelId, getChannel(channel.getFallBack()));
                }
            }
        }
        return false;
    }

    /**
     * Unregisters the specified provider.
     * 
     * @param provider
     *            a provider to be removed
     */
    public void removeProvider(ChannelProvider provider) {
        channelProviders.remove(provider);
    }
}
