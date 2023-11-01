import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import java.net.URL

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
/*
    try {

        val orderValues = mapOf(
            "partner_id" to 3,
            "type_id" to 2
        )
        // Create the sales order
        val createResult = models.execute(
            modelConfig,
            "execute_kw",
            listOf(
                db, uid, password,
                "sale.order", "create",
                listOf(listOf(orderValues))
            )) as Array<Any>

        val orderId = createResult[0] as Int

        // Confirm the sales order (replace with the correct method name)
        val confirmResult = models.execute(
            modelConfig,
            "execute_kw",
            listOf(
                db, uid, password,
                "sale.order", "action_cancel",  // Replace with the correct method name
                listOf(listOf(orderId))
            )) as Boolean  // Assuming it returns a boolean indicating success

        if (confirmResult) {
            println("Sales order successfully confirmed with ID: $orderId")
        } else {
            println("Failed to confirm the sales order.")
        }
    } catch (e: Exception) {
        println("Error creating or confirming the sales order: ${e.message}")
    }
*/

    val confirmCancel = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "sale.order", "action_cancel",
            listOf(listOf(3)) // id of the order_id to cancel
        )) as Boolean  // Assuming it returns a boolean indicating success

    if (confirmCancel) {
        println("Rental order successfully canceled")
    } else {
        println("Failed to cancel the rental order.")
    }

}