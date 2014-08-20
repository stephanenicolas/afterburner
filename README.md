AfterBurner <img src='https://raw.githubusercontent.com/stephanenicolas/afterburner/master/assets/afterburner_logo.jpg' alt='afterburner logo' width='150px'/> 
===========

[![Coverage Status](https://img.shields.io/coveralls/stephanenicolas/afterburner.svg)](https://coveralls.io/r/stephanenicolas/afterburner?branch=master)
[![Travis Build](https://travis-ci.org/stephanenicolas/afterburner.svg?branch=master)](https://travis-ci.org/stephanenicolas/afterburner)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.stephanenicolas.afterburner/afterburner/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.stephanenicolas.afterburner/afterburner)

A library to help other librairies getting rid of boiler plate via byte code manipulation. Works on Android too. 

AfterBurner can be used to inject byte code, in an easy way (via [javassist](https://github.com/jboss-javassist/javassist)), into a given class and target method.
The code is added via byte code manipulation after javac has compiled a class. 

Mutliple plugins can be used to trigger AfterBurner on maven and gradle : 

* [for gradle](https://github.com/darylteo/gradle-plugins)
* [for maven](https://github.com/icon-Systemhaus-GmbH/javassist-maven-plugin)

An annotation based equivalent of AfterBurner is available on GitHub : [Mimic](https://github.com/stephanenicolas/mimic).

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

CtClass classToInsertInto = ClassPool.getDefaultPool().get(A.class.getName());
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

#### Fluent API / DSL way

The `InsertableMethod.Builder` is used to provide a "fluent API/DSL" to AfterBurner. But it is also possible to use AfterBurner in a more verbose way. See below

#### Classic way
```java
afterBurner.addOrInsertMethod(new InsertableMethod(ClassPool.getDefaultPool().get("A")) {
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

there is even a small trick you can use to create a full method and recycle its body : 

```java
afterBurner.addOrInsertMethod(new InsertableMethod(ClassPool.getDefaultPool().get("A")) {
    @Override
    public String getFullMethod() throws AfterBurnerImpossibleException {
        return "public void foo() { ___BODY___ }";
    }
    ...

});
```

the token `___BODY___` will be replaced by the result of `getBody()`.


#### An android example

Let's say you got an activity class `ActivityA` :  
```java
public class ActivityA extends Activity {
}
```

if you want to log "HelloWorld" in its `onCreate` method, just do (with a builder) : 

```java
builder
  .insertIntoClass(ActivityA.class)
  .afterOverrideMethod("onCreate")
  .withBody("System.out.println(\"Hello World\");")
  .doIt();
```

#### AfterBurner related tools for Android

AfterBurner is a simple byte code weaving library. To create powerful byte code weaving gradle plugins based on AfterBurner, use : 
* [morpheus](https://github.com/stephanenicolas/morpheus), byte code weaver support lib for android.

A more detailed example, using a gradle build can be found at : 
* [loglifecycle](https://github.com/stephanenicolas/loglifecycle), Logs all lifecycle methods of a given activity on Android.


If you want to combine an annotation with byte code insertion of your favorite library, to avoid boiler plate, use AfterBurner.

### Credits

The AfterBurner logo is a courtesy of [Hitoshi Mitani](https://plus.google.com/u/0/photos/100459550951624585332/albums/5677808844706283617/5677808842260954306?pid=5677808842260954306&oid=100459550951624585332).


