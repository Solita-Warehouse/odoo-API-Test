import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import java.net.URL
/**
 *
 * https://www.odoo.com/documentation/16.0/developer/reference/external_api.html
 */
fun main(args: Array<String>) {
    // Connection information
    val url = "http://localhost:8069"
    val db = "db"
    val username = "admin"
    val password = "admin"

    /**
     * AUTHENTICATION
     */
    println("### AUTHENTICATION ###")
    val client = XmlRpcClient()
    val commonConfig = XmlRpcClientConfigImpl()

    /**
     * Set the connection data for this config.
     * Here we use http://localhost:8069 + /xmlrpc/2/common
     * "The xmlrpc/2/common endpoint provides meta-calls which don’t require authentication,
     * such as the authentication itself or fetching version information." -odoo.com
     */
    commonConfig.serverURL = URL("$url/xmlrpc/2/common")

    // Fetching the server version is a good way to test if the connection information is correct
    val serverVersion = client.execute(commonConfig, "version", emptyList<Any>())
    println("Server information: $serverVersion")

    // Authentication
    val uid = client.execute(commonConfig, "authenticate", listOf(db, username, password, emptyMap<String, Any>())) as Int
    println("Authenticated user id: $uid\n")

    /**
     * Fetching Model's Data
     *
     * Set up client and config.
     * connection string for this config : http://localhost:8069 + xmlrpc/2/object
     * "The second endpoint is xmlrpc/2/object. It is used to
     * call methods of odoo models via the execute_kw RPC function." -odoo.com
     */
    println("### Fetching user's name ###")
    val models = XmlRpcClient()
    val modelConfig = XmlRpcClientConfigImpl()
    modelConfig.serverURL = URL("$url/xmlrpc/2/object")

    /**
     * The result from the query is stored in an array (authenticatedUser)
     */
    val authenticatedUser = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "res.users", "read",
            listOf(arrayOf(2,3)),
            mapOf("fields" to listOf("partner_id", "login"))
        )
    ) as Array<*>

    printXMLRPC(authenticatedUser)

    println("### Fetching all fields found in res.partner ###")


    val myList: List<String> = emptyList()

    val fieldsOfPartner =  models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "res.partner", "fields_get",
            myList,
            mapOf(
                "attributes" to listOf("string", "type")
            )
        ))as Map<*, *>

    //println(fieldsOfPartner)
    for(attribute in fieldsOfPartner){
        println(attribute)
    }
}

/**
 *  Prints the content of an XmlRpcClient.execute response
 *  NOTE I am not sur this will work for all models
 *  In the case of authenticatedUser the response has the following structure
 *  ArrayOf(MapOf())
 *  Sometimes the element in the map has an array as Value
 *  [
 *      {{partner_id=[(3)(YourCompany, Mitchell Admin)]}, {id=2}, {login=admin}},
 *      {{partner_id=[(5)(Default User Template)]}, {id=3}, {login=default}},
 *  ]
 */
fun printXMLRPC(response: Array<*>){
    for(data in response){
        for(item in data as Map<*,*>){
            if(item.value is Array<*>){
                print("${item.key}=[")
                for(subItem in item.value as Array<*>){
                    print("($subItem)")
                }
                println("]")
            }
            else{
                println(item)
            }
        }
        println()
    }
}