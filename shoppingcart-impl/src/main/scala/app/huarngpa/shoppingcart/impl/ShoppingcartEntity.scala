package app.huarngpa.shoppingcart.impl

import akka.Done
import app.huarngpa.shoppingcart.api.ReadShoppingcartState
import app.huarngpa.shoppingcart.impl.helpers.CollectionImplicits._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import org.slf4j.LoggerFactory
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

class ShoppingcartEntity extends PersistentEntity {

  private val logger = LoggerFactory.getLogger(this.getClass)

  override type Command = ShoppingcartCommand[_]
  override type Event = ShoppingcartEvent
  override type State = ShoppingcartState

  override def initialState: ShoppingcartState = ShoppingcartState(List.empty)

  override def behavior: Behavior = {
    case ShoppingcartState(_) => Actions()
      .onCommand[AddItemToCartCommand, Done] {
        case (AddItemToCartCommand(product), ctx, state) =>
          ctx.thenPersist(AddItemToCartEvent(product)) { _ =>
            logger.info(s"Entity ${this.entityId} got AddItemToCartCommand for product $product.")
            ctx.reply(Done)
          }
      }
      .onCommand[RemoveItemFromCartCommand, Done] {
        case (RemoveItemFromCartCommand(product), ctx, state) =>
          ctx.thenPersist(RemoveItemFromCartEvent(product)) { _ =>
            logger.info(s"Entity ${this.entityId} got RemoveItemFromCartCommand for product $product.")
            ctx.reply(Done)
          }
      }
      .onEvent {
        case (AddItemToCartEvent(product), state) =>
          logger.info(s"Entity ${this.entityId} got AddItemToCartEvent for product $product.")
          ShoppingcartState(product :: state.products)
        case (RemoveItemFromCartEvent(product), state) =>
          logger.info(s"Entity ${this.entityId} got RemoveItemFromCartEvent for product $product.")
          ShoppingcartState(state.products.dropFirstMatch(product))
      }
      .onReadOnlyCommand[ShowCartCommand.type, ReadShoppingcartState] {
        case (ShowCartCommand, ctx, state) =>
          logger.info(s"Entity ${this.entityId} got ShowCartCommand.")
          ctx.reply(ReadShoppingcartState(state.products))
      }
  }
}

case class ShoppingcartState(products: List[String])

object ShoppingcartState {
  implicit val format: Format[ShoppingcartState] = Json.format
}

/**
  * This interface defines all the events that the ShoppingcartEntity supports.
  */
sealed trait ShoppingcartEvent extends AggregateEvent[ShoppingcartEvent] {
  def aggregateTag: AggregateEventTag[ShoppingcartEvent] = ShoppingcartEvent.Tag
}

object ShoppingcartEvent {
  val Tag: AggregateEventTag[ShoppingcartEvent] = AggregateEventTag[ShoppingcartEvent]
}

case class AddItemToCartEvent(product: String) extends ShoppingcartEvent

object AddItemToCartEvent {
  implicit val format: Format[AddItemToCartEvent] = Json.format
}

case class RemoveItemFromCartEvent(product: String) extends ShoppingcartEvent

object RemoveItemFromCartEvent {
  implicit val format: Format[RemoveItemFromCartEvent] = Json.format
}


/**
  * This interface defines all the commands that the ShoppingcartEntity supports.
  */
sealed trait ShoppingcartCommand[R] extends ReplyType[R]

case class AddItemToCartCommand(product: String) extends ShoppingcartCommand[Done]

object AddItemToCartCommand {
  implicit val format: Format[AddItemToCartCommand] = Json.format
}

case class RemoveItemFromCartCommand(product: String) extends ShoppingcartCommand[Done]

object RemoveItemFromCartCommand {
  implicit val format: Format[RemoveItemFromCartCommand] = Json.format
}

case object ShowCartCommand extends ShoppingcartCommand[ReadShoppingcartState] {
  implicit val format: Format[ShowCartCommand.type] = Json.format
}

/**
  * Akka serialization, used by both persistence and remoting, needs to have
  * serializers registered for every type serialized or deserialized. While it's
  * possible to use any serializer you want for Akka messages, out of the box
  * Lagom provides support for JSON, via this registry abstraction.
  *
  * The serializers are registered here, and then provided to Lagom in the
  * application loader.
  */
object ShoppingcartSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[AddItemToCartCommand],
    JsonSerializer[AddItemToCartEvent],
    JsonSerializer[RemoveItemFromCartCommand],
    JsonSerializer[RemoveItemFromCartEvent],
    JsonSerializer[ShoppingcartState],
    JsonSerializer[ShowCartCommand.type],
    JsonSerializer[ReadShoppingcartState]
  )
}
