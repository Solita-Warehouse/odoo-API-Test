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
    val uid = client.execute(commonConfig, "authenticate", listOf(db, username, password, emptyMap<String, Any>())) as Int
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

    println("\r\n\r\n### READ - Fetching all products with name and price ###")

    val productsList = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "product.product", "search_read",
            listOf(emptyList<Any>()),
            mapOf("fields" to listOf("name", "lst_price"))
        )
    ) as Array<*>

    printXMLRPC(productsList)

    println("\r\n\r\n### CREATE - Creating new product ###")

// val productValues are the values you give the product you are creating. You can add more fields to it if you wish
    val productValues = mapOf(
        "name" to "Test",
        "type" to "product",
        "sale_ok" to false,
        "purchase_ok" to false,
        "rental" to true,
        "lst_price" to 19.99,
        // Add more fields as needed
    )
// Code that creates the model
    val productId = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
        db, uid, password,
        "product.product", "create",
        listOf(productValues)
    )) as Int


// Creates rental order and adds the "Test" product into the order line of the rental order.

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
                "sale.order", "action_confirm",  // Replace with the correct method name
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

    val orderLineValues = mapOf(
        "order_id" to 1,
        "product_id" to 1
    )

    val orderLine = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "sale.order.line", "create",
            listOf(orderLineValues)
        )) as Int

    println("\r\n\r\n### READ - Reading rental orders and order lines ###")
/*
    val orderLineList = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "sale.order.line", "search_read",
            listOf(emptyList<Any>()),
            mapOf("fields" to listOf("product_id"))
        )
    ) as Array<*>

    printXMLRPC(orderLineList)
*/
    val rentalList = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "sale.order", "search_read",
            listOf(emptyList<Any>()),
            mapOf("fields" to listOf("name"))
        )
    ) as Array<*>

    printXMLRPC(rentalList)

    val partnerList = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "res.users", "search_read",
            listOf(emptyList<Any>()),
            mapOf("fields" to listOf("partner_id"))
        )
    ) as Array<*>

    printXMLRPC(partnerList)

    /**
     * FIELDS_GET - Fetching all the fields for a given model
     */

    println("### FIELDS_GET - Fetching all fields found in product.product ###")
    val myList: List<String> = emptyList()

    val fieldsOfPartner =  models.execute(
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

    for(attribute in fieldsOfPartner){
        print(attribute)
    }
    println("\r\n")

    /**
     * SEARCH - Returning the ID for all the entry of a table that fits search parameters
     */
    println("### SEARCH - All partner whose name start with D ###")

    val partnerNameStartWithID =  models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "res.partner", "search",
            listOf(
                listOf(
                    listOf("name", "like", "D%")
                    //listOf("active", "=", false)

                )
            )
        ))as Array<*>

    for(id in partnerNameStartWithID){
        print("${id} ")
    }
    println()

    val partnerNameStartWith = models.execute(
        modelConfig,
        "execute_kw",
        listOf(
            db, uid, password,
            "res.partner", "read",
            listOf(partnerNameStartWithID),
            mapOf("fields" to listOf( "name"))
        )
    ) as Array<*>

    for(partner in partnerNameStartWith){
        println(partner)
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

