
<p align="center">
  <img src="https://i.imgur.com/OE3AM94.png"/>
</p>

<h1 align="center">Dependency4j</h1>
<h4 align="center">A lightweight Java library designed for Inverse of Control and Dependency Injection management</h3>

<br>

<p align="center">
  <img src="https://img.shields.io/badge/DEPENDENCY4J%20%20-1.0.41_SNAPSHOT-purple"/>
  <img src="https://github.com/daviddev16/dependency4j/actions/workflows/maven-testing.yml/badge.svg"/>
  <img src="https://img.shields.io/badge/Java%20%20-17.0.10-orange"/>
</p>


Dependency4j offers a lightweight solution for efficiently managing project dependencies through
implementation of both dependency injection and dependency inversion. By leveraging Dependency4j,
developers can effortlessly orchestrate dependency management within their projects, enhancing
modularity, flexibility, and testability. It also exposes functionalities that might be used to
provide a service locator pattern design in your application.

<br>

## How it works?

The way Dependency4j works is that it maps all classes in a package that are marked with `@Managed`
and their respective interfaces and superclasses in a tree called `DependencySearchTree`. Insertion
only occurs if the `@Managed` annotation contains strategies that can be matched with the `DependencyManager`
strategies. If no strategy is specified in the dependency manager, the class will be instantiated
without restriction. When a class is instantiated it starts the dependency injection propagation through
all classes.

<p align="center">
  <img src="https://i.imgur.com/elwZ4uk.png"/>
</p>

<p align="center"><i>We can write a test that simulates the sequence diagram shown in the figure above</i></p> 

````java
@Test
@DisplayName("Demonstration unit test")
void demonstrateSequenceDiagramTest() {

   /* 1. Creating and installing the DependencyManager */
   DependencyManager dependencyManager = DependencyManager.builder()
           .strategy("Staging")
           .installPackage("com.dependency4j.example")
           .getDependencyManager();

   /* 2. Fetching the IProductRepository implementation and testing */
   IProductRepository productRepository = dependencyManager.query(IProductRepository.class);
   Assertions.assertNotNull(productRepository);
   Assertions.assertEquals(StagingProductRepository.class, productRepository.getClass());

   /* 3. Installing the user's single instance */
   MyProductService myProductService = new MyProductService();

   /* -!- MyProductService contains a dependency object of IProductRepository, we should test it later  -!- */
   dependencyManager.installInstance(myProductService);

   /* 4. Testing if the IProductRepository dependency was injected in MyProductService instance */
   IProductRepository productRepositoryFromMyService = myProductService.getProductRepository();
   Assertions.assertNotNull(productRepositoryFromMyService);
   Assertions.assertEquals(StagingProductRepository.class, productRepositoryFromMyService.getClass());

}
````

<br>

## The Core Annotations

Core annotations are used to specify a certain behaviour while injecting or instatiating a class.

### **@Managed**

The `@Managed` annotation is used at _class-level_. It indicates that the class is a implementation and it
should be handled by the `DependencyManager`. Dependency4j works with 3 criteria for a class to be inserted
into the `DependencySearchTree` and instantiated:

1. _The implementation class should be annotated with `@Managed`;_
2. If strategies were used in the `DependencyManager` initialization,
   at least one of the `@Managed` strategies should match the manager's strategies.
3. By default, all classes are _disposable_, which means that all classes that do not
   match with the `DependencyManager` strategies, will not be instantiated. If you configure
   disposable to `false` using `@Managed(disposable = false)`, it will be instantiated regardless
   the strategy used by the class.

#### Example:

````java
@Managed(disposable = false)
public class UserServiceImpl implements IUserService {...}
````

### **@Strategy**

The `@Strategy` annotation is used within `@Managed` on the `strategy` parameter. It indicates
the strategy of the class. Let's say we have `StagingXYZController` annotated with
`@Managed(strategy = @Strategy({"Staging"}))` which implements `IXYZController` interface. When
query for `IXYZController.class` on a `DependencyManager` initialized with strategy *"Staging"*,
the query result should be a instance of `StagingXYZController`.

````java
@Managed(strategy = @Strategy({"Staging", "Test"}))
public class StagingProductRepository implements IProductRepository {...}
````

### **@Pull**

The `@Pull` annotates functions, variables and the constructor of a class to indicate where the
`DependencyManager` should inject a object implementation. It is equivalent to a `QueryOption`
object, which is often used within the `DependencyManager` to specify a certain behaviour to a
variable/function/constructor injection. The `value` parameter is used to filter by a name of a
implementation, this name can be added in `@Managed(name = "myImplementationName")`. If no name
is specified in the `@Managed` annotation, the name is the name of the class.

#### The @Pull Querying Behaviour

When a `value` is specified, the query will try to find a implementation object with the same name.
Let's say we specified `@Pull("ProductServiceImplv2")`, the search tree will try to find a implementation
object with name *"ProductServiceImplv2"*. The Querying behaviour will follow the rule:

<br>

> If no implementation object is found with the specified name and `retrieveAnyways` is equals to `true`,
> The query still searches for other implementations assignable to the field type. If `retrieveAnyways`
> is equals to `false`, The query will return `null`.

<br>

````java
@Managed
public class OrderController {

   private final OrderService orderService;

   /* 1. variable injection */
   @Pull("ProductServiceImplv2")
   private ProductService productService;

   private UserService userService;

   /* 2. constructor injection */
   public @Pull OrderController(OrderService orderService) {
      this.orderService = orderService;
   }

   /* 3. method/function injection */
   public @Pull void setUserService(UserService userService) {
      this.userService = userService;
   }

}
````
<br>

## Testing with Injection & Service Locator

Dependency4j can be integrated with JUnit and perform dependency injection in the test class.
You can initialize the `DependencyManager` for each test method too. Here is a example
of dependency injection in the test case, where `prepare(this)` is injecting the dependency
`messagingService`:

```java
public class QuickDependencyManagerTest {

   private final DependencyManager dependencyManager;
   private @Pull IMessagingService messagingService;

   public QuickDependencyManagerTest() {
      dependencyManager = DependencyManager.builder()
              .strategy("Testing")
              .installPackage("com.dependency4j.example")
              .prepare(this)
              .getDependencyManager();
   }

   @Test
   public void quickTestMatchMessagingService() {
      assertNotNull(dependencyManager);
      assertEquals(MessagingServiceImpl.class, messagingService.getClass());
   }

}
```
- If you don't want to use the annotation approach, you can still use the service locator pattern
  by using the `query(type)` function of `DependencyManager`. Here is a example on how to use it.
```java
    @Test
public void testMessageServiceMatching() {

   IMessagingService messagingService
           = dependencyManager.query(IMessagingService.class);

   Assertions.assertNotNull(messagingService);
   Assertions.assertEquals(MessagingServiceImpl.class, messagingService.getClass());
}
```
<br>

## Documentation

Working on it 🧐


