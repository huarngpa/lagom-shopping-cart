package app.huarngpa.shoppingcart.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import app.huarngpa.shoppingcart.api.ReadShoppingcartState
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class ShoppingcartEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("ShoppingcartEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(ShoppingcartSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[ShoppingcartCommand[_], ShoppingcartEvent, ShoppingcartState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new ShoppingcartEntity, "shoppingcart-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "ShoppingCart entity" should {

    "show empty list by default" in withTestDriver { driver =>
      val outcome = driver.run(ShowCartCommand)
      val expected = ReadShoppingcartState(List.empty)
      outcome.replies should contain only expected
    }

    def addToCart(driver: PersistentEntityTestDriver[ShoppingcartCommand[_], ShoppingcartEvent, ShoppingcartState]) = {
      val addOutcome = driver.run(AddItemToCartCommand("something"))
      addOutcome.events should contain only AddItemToCartEvent("something")
      val addAnotherOutcome = driver.run(AddItemToCartCommand("another something"))
      addAnotherOutcome.events should contain only AddItemToCartEvent("another something")
      val showOutcome = driver.run(ShowCartCommand)
      showOutcome.replies should contain only ReadShoppingcartState(List("another something", "something"))
    }

    "allow adding items to the cart" in withTestDriver { driver =>
      addToCart(driver)
    }

    "allow removing items from the cart" in withTestDriver { driver =>
      addToCart(driver)
      val removeOutcome = driver.run(RemoveItemFromCartCommand("something"))
      removeOutcome.events should contain only RemoveItemFromCartEvent("something")
      val anotherRemoveOutcome = driver.run(RemoveItemFromCartCommand("something"))
      anotherRemoveOutcome.events should contain only RemoveItemFromCartEvent("something")
      val showOutcome = driver.run(ShowCartCommand)
      showOutcome.replies should contain only ReadShoppingcartState(List("another something"))
    }
  }
}
