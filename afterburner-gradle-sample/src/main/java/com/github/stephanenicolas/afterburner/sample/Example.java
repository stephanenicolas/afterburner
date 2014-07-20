package com.github.stephanenicolas.afterburner.sample;

/**
 * This example class will receive all code from {@code ExampleTempltate}.
 * @author SNI
 */
public class Example extends ExampleAncestor {

    @Override
    public void doStuff() {
        super.doStuff();
    }

    public void doOtherStuff() {
    	bar();
    }

    public void bar() {
    }
}
