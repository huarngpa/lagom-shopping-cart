package app.huarngpa.shoppingcart.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object ShoppingcartService

trait ShoppingcartService extends Service {

  def addItemToCart(id: String): ServiceCall[AddItemRequest, Done]

  def removeItemFromCart(id: String): ServiceCall[RemoveItemRequest, Done]

  def showCart(id: String): ServiceCall[NotUsed, ReadShoppingcartState]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("shoppingcart")
      .withCalls(
        restCall(Method.POST, "/api/add-to-cart/:id", addItemToCart _),
        restCall(Method.POST, "/api/remove-from-cart/:id", removeItemFromCart _),
        restCall(Method.GET, "/api/cart/:id", showCart _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

case class AddItemRequest(product: String)

object AddItemRequest {
  implicit val format: Format[AddItemRequest] = Json.format[AddItemRequest]
}

case class RemoveItemRequest(product: String)

object RemoveItemRequest {
  implicit val format: Format[RemoveItemRequest] = Json.format[RemoveItemRequest]
}

case class ReadShoppingcartState(products: List[String])

object ReadShoppingcartState {
  implicit val format: Format[ReadShoppingcartState] = Json.format[ReadShoppingcartState]
}
