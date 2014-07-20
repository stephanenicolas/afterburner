package com.github.stephanenicolas.afterburner.sample;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.InsertableMethodBuilder;

import javassist.CtClass;
import javassist.CtMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.darylteo.gradle.javassist.transformers.ClassTransformer;

/**
 * A simple annotation processor.
 * @author SNI
 */
public class ExampleProcessor extends ClassTransformer {
	private Logger logger = LoggerFactory.getLogger(ExampleProcessor.class);
	private AfterBurner afterBurner = new AfterBurner(logger);

	@Override
	public boolean shouldTransform(CtClass candidateClass) throws Exception {
		boolean hasDoStuff = false;
		CtMethod[] methods = candidateClass.getMethods();
		for( CtMethod method : methods) {
			if (method.getName().equals("doStuff") || method.getName().equals("doOtherStuff")) {
				hasDoStuff = true;
			}
		}
		return hasDoStuff ;
	}

	@Override
	public void applyTransformations(CtClass classToTransform) throws Exception {
		afterBurner.afterOverrideMethod(classToTransform, "doStuff", "System.out.println(\"Inside doStuff\");");

		InsertableMethodBuilder builder = new InsertableMethodBuilder(afterBurner);
		builder
			.insertIntoClass(classToTransform)
			.inMethodIfExists("doOtherStuff")
			.beforeACallTo("bar")
			.withBody("System.out.println(\"Inside doOtherStuff\");")
			.elseCreateMethodIfNotExists("public void doOtherStuff() { ___BODY___ }")
			.doIt();
	}

}
