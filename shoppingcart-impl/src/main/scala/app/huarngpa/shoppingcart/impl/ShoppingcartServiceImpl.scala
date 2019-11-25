package app.huarngpa.shoppingcart.impl

import akka.{Done, NotUsed}
import app.huarngpa.shoppingcart.api.{AddItemRequest, ReadShoppingcartState, RemoveItemRequest, ShoppingcartService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.slf4j.LoggerFactory

/**
  * Implementation of the ShoppingcartService.
  */
class ShoppingcartServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends ShoppingcartService {

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def addItemToCart(id: String): ServiceCall[AddItemRequest, Done] = {
    ServiceCall { request =>
      val ref = persistentEntityRegistry.refFor[ShoppingcartEntity](id)
      logger.info(s"Sending AddItemToCartCommand for user $id and product ${request.product}.")
      ref.ask(AddItemToCartCommand(request.product))
    }
  }

  override def removeItemFromCart(id: String): ServiceCall[RemoveItemRequest, Done] = {
    ServiceCall { request =>
      val ref = persistentEntityRegistry.refFor[ShoppingcartEntity](id)
      logger.info(s"Sending RemoveItemFromCartCommand for user $id and product ${request.product}.")
      ref.ask(RemoveItemFromCartCommand(request.product))
    }
  }

  override def showCart(id: String): ServiceCall[NotUsed, ReadShoppingcartState] = {
    ServiceCall { _ =>
      val ref = persistentEntityRegistry.refFor[ShoppingcartEntity](id)
      logger.info(s"Sending ShowCartCommand for user $id")
      ref.ask(ShowCartCommand)
    }
  }
}
