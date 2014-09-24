guice-multiscopes is a unofficial Guice scope implementation with:

* Multiple scope instances for each scope
* Unbounded mulitiscopes - as many instances as you request
* Bounded multiscopes - similar to multiset
  * Automatic prescoping through bindings - lazy or on scope creation
* Prescoping and facade for binding prescoped object
* Multithreaded scope access
* Customizable scope storage
* Generated descopers

The goal of this projects is to completely hold and facilitate your object storage graph with guice and guice scopes.
