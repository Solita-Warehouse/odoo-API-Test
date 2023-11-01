import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import java.net.URL
import java.util.Arrays.asList


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
    val uid =
        client.execute(commonConfig, "authenticate", listOf(db, username, password, emptyMap<String, Any>())) as Int
    println("Authenticated user id: $uid")

    /**
     * READ - Fetching Model's Data
     *
     * Set up client and config.
     * connection string for this config : http://localhost:8069 + xmlrpc/2/object
     * "The second endpoint is xmlrpc/2/object. It is used to
     * call methods of odoo models via the execute_kw RPC function." -odoo.com
     */
    println("\r\n\r\n### READ - Fetching user's name ###")
    val models = XmlRpcClient()
    val modelConfig = XmlRpcClientConfigImpl()
    modelConfig.serverURL = URL("$url/xmlrpc/2/object")
    /**
     * The result from the query is stored in an array (authenticatedUser)
     */



    println("\r\n\r\n### SEARCH_READ - Search Rental Orders ###")

    val rentalOrderSearch = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "sale.order", "search_read",
            listOf(emptyList<Any>()),
            mapOf("fields" to listOf("name", "partner_id", "state"))
        )
    ) as Array<*>

    printXMLRPC(rentalOrderSearch)

    println("\r\n\r\n### SEARCH_READ - Search Order Lines ###")

    val orderLineSearch = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "sale.order.line", "search_read",
            listOf(emptyList<Any>()),
            mapOf("fields" to listOf("name","product_id", "order_id"))
        )
    ) as Array<*>

    printXMLRPC(orderLineSearch)

    println("### FIELDS_GET - Fetching all fields found in product.product ###")
    val myList: List<String> = emptyList()

    val fieldsOfProduct =  models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "product.product", "fields_get",
            myList,
            mapOf(
                "attributes" to listOf("string", "type")
            )
        ))as Map<*, *>

    for(attribute in fieldsOfProduct){
        print(attribute)
        print("\r\n")
    }
    println("\r\n")

    println("### FIELDS_GET - Fetching all fields found in sale.order ###")
    val myList2: List<String> = emptyList()

    val fieldsOfSaleOrder =  models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "sale.order", "fields_get",
            myList2,
            mapOf(
                "attributes" to listOf("string", "type")
            )
        ))as Map<*, *>

    for(attribute in fieldsOfSaleOrder){
        print(attribute)
        print("\r\n")
    }
    println("\r\n")

}