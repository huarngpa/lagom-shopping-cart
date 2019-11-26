package app.huarngpa.shoppingcart.impl

import app.huarngpa.shoppingcart.api._
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class ShoppingcartServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new ShoppingcartApplication(ctx) with LocalServiceLocator
  }

  val client: ShoppingcartService = server.serviceClient.implement[ShoppingcartService]

  override protected def afterAll(): Unit = server.stop()

  "ShoppingCart service" should {

    "default to empty list" in {
      val expected = ReadShoppingcartState(List.empty)
      client.showCart("21").invoke().map { answer =>
        answer should ===(expected)
      }
      client.showCart("49").invoke().map { answer =>
        answer should ===(expected)
      }
    }

    "allow items to be added to the cart" in {
      for {
        _ <- client.addItemToCart("21").invoke(AddItemRequest("something"))
        _ <- client.addItemToCart("21").invoke(AddItemRequest("another something"))
        _ <- client.addItemToCart("49").invoke(AddItemRequest("shouldn't be in the list"))
        answer <- client.showCart("21").invoke()
        expected = ReadShoppingcartState(List("another something", "something"))
      } yield {
        answer should ===(expected)
      }
    }

    "allow items to be removed from the cart" in {
      for {
        _ <- client.removeItemFromCart("21").invoke(RemoveItemRequest("something"))
        _ <- client.removeItemFromCart("21").invoke(RemoveItemRequest("shouldn't be in the list"))
        _ <- client.removeItemFromCart("21").invoke(RemoveItemRequest("something"))
        answer <- client.showCart("21").invoke()
        expected = ReadShoppingcartState(List("another something"))
      } yield {
        answer should ===(expected)
      }
    }
  }
}
