package com.github.stephanenicolas.afterburner.sample;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.InsertableMethodBuilder;

import javassist.CtClass;
import javassist.CtMethod;
import de.icongmbh.oss.maven.plugin.javassist.ClassTransformer;

/**
 * A simple annotation processor.
 * @author SNI
 */
public class ExampleProcessor extends ClassTransformer {

	private AfterBurner afterBurner = new AfterBurner(getLogger());

	@Override
	protected boolean shouldTransform(CtClass candidateClass) throws Exception {
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
	protected void applyTransformations(CtClass classToTransform) throws Exception {
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
