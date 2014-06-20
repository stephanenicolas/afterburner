package com.github.stephanenicolas.afterburner;

import java.util.ArrayList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import org.slf4j.Logger;

public class AfterBurner {
    private Logger logger;

    public AfterBurner(Logger logger) {
        this.logger = logger;
    }

    public void addOrInsertMethod(InsertableMethod insertableMethod) throws CannotCompileException, AfterBurnerImpossibleException {
        // create or complete onViewCreated
        String targetMethodName = insertableMethod.getTargetMethodName();
        CtClass classToTransform = insertableMethod.getClassToInsertInto();
        CtMethod targetMethod = extractExistingMethod(classToTransform,
                targetMethodName);
        getLogger().debug("Method : " + targetMethod);
        if (targetMethod != null) {
            InsertableMethodInjectorEditor injectorEditor = new InsertableMethodInjectorEditor(
                    classToTransform, insertableMethod);
            targetMethod.instrument(injectorEditor);
        } else {
            classToTransform.addMethod(CtNewMethod.make(
                    insertableMethod.getFullMethod(), classToTransform));
        }
    }

    public void insertConstructor(InsertableConstructor insertableConstructor) throws CannotCompileException, AfterBurnerImpossibleException,
    NotFoundException {
        // create or complete onViewCreated
        List<CtConstructor> constructorList = extractExistingConstructors(insertableConstructor);
        getLogger().debug("constructor : " + constructorList.toString());
        if (!constructorList.isEmpty()) {
            for (CtConstructor constructor : constructorList) {
                constructor
                .insertBeforeBody(insertableConstructor
                        .getConstructorBody(constructor
                                .getParameterTypes()[0]));
            }
        } else {
            getLogger().warn("No suitable constructor was found in class {}. Add a constructor with a single argument : Activity, Fragment or View. Don't use non static inner classes.",
                    insertableConstructor.getClassToInsertInto().getName());
        }
    }

    public CtMethod extractExistingMethod(final CtClass classToTransform,
            String methodName) {
        try {
            return classToTransform.getDeclaredMethod(methodName);
        } catch (Exception e) {
            return null;
        }
    }

    private List<CtConstructor> extractExistingConstructors(final InsertableConstructor insertableConstructor) throws NotFoundException, AfterBurnerImpossibleException {
        List<CtConstructor> constructors = new ArrayList<CtConstructor>();
        CtConstructor[] declaredConstructors = insertableConstructor
                .getClassToInsertInto().getDeclaredConstructors();
        for (CtConstructor constructor : declaredConstructors) {
            CtClass[] paramClasses = constructor.getParameterTypes();
            if (insertableConstructor.acceptParameters(paramClasses)) {
                constructors.add(constructor);
            }
        }
        return constructors;
    }

    private Logger getLogger() {
        return logger;
    }

    private final class InsertableMethodInjectorEditor extends ExprEditor {
        private final CtClass classToTransform;
        private final String insertionMethod;
        private final boolean insertAfter;
        private final String bodyToInsert;

        private InsertableMethodInjectorEditor(CtClass classToTransform, InsertableMethod insertableMethod) throws AfterBurnerImpossibleException {
            this.classToTransform = classToTransform;
            String insertionAfterMethod = insertableMethod
                    .getInsertionAfterMethod();
            String insertionBeforeMethod = insertableMethod
                    .getInsertionBeforeMethod();
            if (insertionBeforeMethod == null && insertionAfterMethod == null) {
                throw new AfterBurnerImpossibleException(
                        "Error in class "
                                + insertableMethod.getClass()
                                + " both insertionBeforeMethod && insertionAfterMethod are null.");
            } else if (insertionBeforeMethod != null) {
                this.insertionMethod = insertionBeforeMethod;
                insertAfter = false;
            } else {
                this.insertionMethod = insertionAfterMethod;
                insertAfter = true;
            }
            bodyToInsert = insertableMethod.getBody();
        }

        @Override
        public void edit(MethodCall m) throws CannotCompileException {
            if (m.getMethodName().equals(insertionMethod)) {

                String origMethodCall = "$_ = $proceed($$);\n";
                if (insertAfter) {
                    origMethodCall = origMethodCall + bodyToInsert;
                } else {
                    origMethodCall = bodyToInsert + origMethodCall;
                }

                m.replace(origMethodCall);
                getLogger().debug("Injected : " + origMethodCall);
                getLogger().info("Class {} has been enhanced.",
                        classToTransform.getName());
            }
        }
    }
}
