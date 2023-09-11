package org.example;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
//https://ws.apache.org/xmlrpc/apidocs/org/apache/xmlrpc/client/package-frame.html

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
/**
 *
 * https://www.odoo.com/documentation/16.0/developer/reference/external_api.html
 */

public class Main {
    public static void main(String[] args) throws XmlRpcException, MalformedURLException {
        // Connection information
        final String url = "http://localhost:8069";
        final String db = "db";
        final String username = "admin";
        final String password = "admin";

        /**
         * AUTHENTICATION
         * https://ws.apache.org/xmlrpc/apidocs/org/apache/xmlrpc/client/XmlRpcClientConfigImpl.html
         * https://ws.apache.org/xmlrpc/apidocs/org/apache/xmlrpc/client/XmlRpcClient.html
         */
        System.out.println("###AUTHENTICATION###");
        final XmlRpcClient client = new XmlRpcClient();
        final XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
        /**
         *
         * Set the connection data for this config.
         * Here we use http://localhost:8069 + /xmlrpc/2/common
         *      "The xmlrpc/2/common endpoint provides meta-calls which don’t require authentication,
         *          such as the authentication itself or fetching version information." -odoo.com
         */
        common_config.setServerURL(new URL(String.format("%s/xmlrpc/2/common", url)));

        // Fetching the server version is a good way to test if the connection information is correct
        HashMap serverVersion = (HashMap) client.execute(common_config, "version", emptyList());
        System.out.println("Server informaitons : " + serverVersion);

        // Authentication
        int uid = (int)client.execute(common_config, "authenticate", asList(db, username, password, emptyMap()));
        System.out.println("Authenticated user id : " + uid);

        System.out.println();


        /**
         * Fetching Model's Data
         *
         * Set up client and config.
         * connection string for this config : http://localhost:8069 + xmlrpc/2/object
         *      "The second endpoint is xmlrpc/2/object. It is used to
         *          call methods of odoo models via the execute_kw RPC function." -odoo.com
         */
        System.out.println("###Fetching user's name###");
        final XmlRpcClient models = new XmlRpcClient() {{
            setConfig(new XmlRpcClientConfigImpl() {{
                setServerURL(new URL(String.format("%s/xmlrpc/2/object", url)));
            }});
        }};

        Object[] authenticatedUser =(Object[]) models.execute("execute_kw", asList(
                db, uid, password,
                "res.users", "read",
                asList(uid),
                new HashMap() {{
                    put("fields", asList("partner_id", "login"));
                }}
        ));
        //System.out.println(asList(authenticatedUser));
        if (authenticatedUser.length > 0) {
            // Access the first record (index 0)
            HashMap<String, Object> firstRecord = (HashMap<String, Object>) authenticatedUser[0];
            if (firstRecord.containsKey("partner_id")) {
                Object[] partner_id = (Object[]) firstRecord.get("partner_id");
                System.out.println("partner_id: " + partner_id[0]);
                System.out.println("partner info?: " + partner_id[1]);
            }
            if (firstRecord.containsKey("id")) {
                String id = firstRecord.get("id").toString();
                System.out.println("Id: " + id);
            }

        } else {
            System.out.println("No records found.");
        }

        /*
        System.out.println(asList((Object[])models.execute("execute_kw", asList(
                db, uid, password,
                "res.partner", "read",
                asList(3),
                new HashMap() {{
                    put("fields", asList("name", "country_id", "comment"));
                }}
        ))));

        // Get all the ID of res.partner
        List ids = asList((Object[])models.execute("execute_kw", asList(
                db, uid, password,
                "res.partner", "search",
                asList(asList(
                        asList("is_company", "=", true)))
        )));

        // Get the fields "name", "country_id", "comment" for res.partner with id in ids
        System.out.println(asList((Object[])models.execute("execute_kw", asList(
                db, uid, password,
                "res.partner", "read",
                asList(ids),
                new HashMap() {{
                    put("fields", asList("name", "country_id", "comment"));
                }}
        ))));
        */

    }
}