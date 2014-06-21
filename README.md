afterburner
===========

A library to help other librairies getting rid of boiler plate via byte code manipulation. Works on Android too. 

AfterBurner can be used to inject byte code, in an easy way (via javassist), into a given class and target method.
The code is added via byte code manipulation after javac has compiled a class. 

Mutliple plugins can be used to trigger AfterBurner on maven and gradle : 

* [for gradle](https://github.com/darylteo/gradle-plugins)
* [for maven](https://github.com/icon-Systemhaus-GmbH/javassist-maven-plugin)

Examples
--------

#### Adding a new method

Let say we a class `A` : 

```java
public class A {
  private int foo;
  public void bar() {}
  public void foo() {
     bar();
  }
}
```

We can change the method `foo()`, for instance to change the value of the member `foo`, right after a call to `bar()` :
```java
InsertableMethod.Builder builder = new InsertableMethod.Builder( new AfterBurner() );

CtClass classToInsertInto = CtClass.getDefaultPool().get(A.class);
String targetMethod = "foo";
String insertionAfterMethod = "bar";
String fullMethod = "public void foo() { this.foo = 2; }";
String body = "this.foo = 2;";
builder
  .insertIntoClass(classToInsertInto)
  .inMethodIfExists(targetMethod)
  .afterACallTo(insertionAfterMethod)
  .withBody(body)
  .elseCreateMethodIfNotExists(fullMethod)
  .doIt();
```

This will result in modifying the class `A` as if it had been written : 
```java
public class A {
  private int foo;
  public void bar() {}
  public void foo() {
     bar();
     //ADDED by AfterBurner
    this.foo = 2;
  }
}
```

#### Modifying an existing method

The `fullMethod` attribute of the builder is used if class `A` doesn't have a method `foo()`, otherwise, the `body` attribute of the builder is compiled and injected into the method `foo()` right after a call to method `bar()`.

#### Fluent API / DSL

The `InsertableMethod.Builder` is used to provide a "fluent API/DSL" to AfterBurner. But it is also possible to use AfterBurner in a more verbose way. See below

#### Fluent API / DSL

```java
afterBurner.addOrInsertMethod(new InsertableMethod(CtClass.getDefaultPool().getA()) {
    @Override
    public String getFullMethod() throws AfterBurnerImpossibleException {
        return "public void foo() { foo = 2; }";
    }

    @Override
    public String getBody() throws AfterBurnerImpossibleException {
        return "foo = 2;";
    }

    @Override
    public String getTargetMethodName() throws AfterBurnerImpossibleException {
        return "foo";
    }
            
    @Override
    public String getInsertionAfterMethod() {
        return "bar";
    }
});
```
