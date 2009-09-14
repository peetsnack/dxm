package org.jahia.bin;

import org.jahia.params.ProcessingContext;
import org.jahia.services.render.RenderContext;

import javax.jcr.RepositoryException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 4:15:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edit extends Render {

    private static String editServletPath;

    @Override
    public void init() throws ServletException {
        super.init();
        if (getServletConfig().getInitParameter("edit-servlet-path") != null) {
            editServletPath = getServletConfig().getInitParameter("edit-servlet-path");
        }
    }

    public static String getEditServletPath() {
        return editServletPath;
    }


    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp) {
        RenderContext context = super.createRenderContext(req, resp);
        context.setEditMode(true);
        return context;
    }
}
