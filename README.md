# ShoppingCart

This is simple shopping-cart api to get familiar with the `lagom` framework. Basic functionality has been programmed for the following endpoints:
* POST `/api/add-to-cart/:id` adds an item (String) to the cart
* POST `/api/remove-from-cart/:id` removes an item (String) from the cart
* GET `/api/cart/:id` shows the current state of the cart

Just some repetitive practice in wiring up the framework and tests.

# Examples

Some usage examples:

```sh
curl -XPOST -d '{"product": "Apples"}' -H "Content-Type: application/json" http://localhost:9000/api/add-to-cart/3
curl -XPOST -d '{"product": "Apples"}' -H "Content-Type: application/json" http://localhost:9001/api/remove-from-cart/3
curl -XGET http://localhost:9000/api/cart/3
```
