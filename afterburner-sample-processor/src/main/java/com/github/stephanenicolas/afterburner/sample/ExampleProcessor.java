package com.github.stephanenicolas.afterburner.sample;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.InsertableMethodBuilder;
import com.github.stephanenicolas.afterburner.inserts.InsertableMethod;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple annotation processor.
 * @author SNI
 */
@Slf4j
public class ExampleProcessor implements IClassTransformer {

	private AfterBurner afterBurner = new AfterBurner();

	@Override public boolean shouldTransform(CtClass candidateClass) {
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
	public void applyTransformations(CtClass classToTransform)  throws JavassistBuildException {
		log.info("Transforming " + classToTransform.getName());
    try {
      afterBurner.afterOverrideMethod(classToTransform, "doStuff", "System.out.println(\"Inside doStuff\");");

      InsertableMethodBuilder builder = new InsertableMethodBuilder(afterBurner);
      builder
        .insertIntoClass(classToTransform)
        .inMethodIfExists("doOtherStuff")
        .beforeACallTo("bar")
        .withBody("System.out.println(\"Inside doOtherStuff\");")
        .elseCreateMethodIfNotExists("public void doOtherStuff() { " + InsertableMethod.BODY_TAG + " }")
        .doIt();
    } catch (Exception e) {
      throw new JavassistBuildException(e);
    }
  }

}
