package com.zenuml.confluence.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.storage.lib.PluginLicenseStoragePluginUnresolvedException;
import com.atlassian.upm.license.storage.lib.ThirdPartyPluginLicenseStorageManager;

/**
 * A simple "Hello World" servlet that demonstrates how to use
 * {@link ThirdPartyPluginLicenseStorageManager} to ensure plugin license
 * existence/validity at a plugin entry point.
 *
 * This servlet can be reached at http://localhost:2990/jira/plugins/servlet/com.zenuml.confluence.sequence/licensehelloworld
 *
 * Note that Atlassian does not recommend writing servlets in this manner. You should really look
 * into using a templating library such as Velocity. This servlet is written this way solely for simplicity purposes.
 */
public class LicenseHelloWorldServlet extends HttpServlet
{
    private final ThirdPartyPluginLicenseStorageManager licenseManager;

    public LicenseHelloWorldServlet(ThirdPartyPluginLicenseStorageManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.setContentType("text/html");
        resp.getWriter().write("<html><body>");
        
        try
        {
            //Check and see if a license is currently stored.
            //This accessor method can be used whether or not a licensing-aware UPM is present.
            if (licenseManager.getLicense().isDefined())
            {
                PluginLicense pluginLicense = licenseManager.getLicense().get();
                //Check and see if the stored license has an error. If not, it is currently valid.
                if (pluginLicense.getError().isDefined())
                {
                    //A license is currently stored, however, it is invalid (e.g. expired or user count mismatch)
                    resp.getWriter().write("I'd love to say hello, but cannot do so because your license has an error: " 
                            + pluginLicense.getError().get().name());
                }
                else
                {
                    //A license is currently stored and it is valid.
                    resp.getWriter().write("Hello, world! You are licensed!");
                }
            }
            else
            {
                //No license (valid or invalid) is stored.
                resp.getWriter().write("I'd love to say hello, but cannot do so because you don't have a license.");
            }
        }
        catch (PluginLicenseStoragePluginUnresolvedException e)
        {
            //The current license status cannot be retrieved because the Plugin License Storage plugin is unavailable.
            resp.getWriter().write("I'd love to say hello, but cannot find my required resources. Please speak to a system administrator.");
        }
        
        resp.getWriter().write("</body></html>");
        resp.getWriter().close();
    }
}
