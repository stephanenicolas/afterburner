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

import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.afterburner.inserts.InsertableConstructor;
import com.github.stephanenicolas.afterburner.inserts.InsertableMethod;
import com.github.stephanenicolas.afterburner.inserts.CtMethodJavaWriter;

/**
 * Allows to modify byte code of java classes via javassist.
 * This class allows a rich API to injeect byte code into methods or constructors of a given class.
 * @author SNI
 */
public class AfterBurner {
    private Logger logger;
    private CtMethodJavaWriter signatureExtractor;

    public AfterBurner(Logger logger) {
        this.logger = logger;
        signatureExtractor = new CtMethodJavaWriter();
    }

    /**
     * Add/Inserts java instructions into a given method of a given class.
     * @param insertableMethod contains all information to perform byte code injection.
     * @throws CannotCompileException if the source contained in insertableMethod can't be compiled. 
     * @throws AfterBurnerImpossibleException if something else goes wrong, wraps other exceptions.
     */
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
    
    /**
     * Add/Inserts java instructions into a given override method of a given class. Before the overriden method call.
     * @param targetClass the class to inject code into.
     * @param targetMethodName the method to inject code into. Body will be injected right before the call to super.&lt;targetName&gt;.
     * @param body the instructions of java to be injected.
     * @throws CannotCompileException if the source contained in insertableMethod can't be compiled.
     * @throws AfterBurnerImpossibleException if something else goes wrong, wraps other exceptions.
     */
    public void beforeOverrideMethod(CtClass targetClass, String targetMethodName, String body) throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        InsertableMethod insertableMethod = new InsertableMethodBuilder(this, signatureExtractor).insertIntoClass(targetClass).beforeOverrideMethod(targetMethodName).withBody(body).createInsertableMethod();
        addOrInsertMethod(insertableMethod);
    }

    /**
     * Add/Inserts java instructions into a given override method of a given class. After the overriden method call.
     * @param targetClass the class to inject code into.
     * @param targetMethodName the method to inject code into. Body will be injected right after the call to super.&lt;targetName&gt;.
     * @param body the instructions of java to be injected.
     * @throws CannotCompileException if the source contained in insertableMethod can't be compiled.
     * @throws AfterBurnerImpossibleException if something else goes wrong, wraps other exceptions.
     */
    public void afterOverrideMethod(CtClass targetClass, String targetMethodName, String body) throws CannotCompileException, AfterBurnerImpossibleException, NotFoundException {
        InsertableMethod insertableMethod = new InsertableMethodBuilder(this, signatureExtractor).insertIntoClass(targetClass).afterOverrideMethod(targetMethodName).withBody(body).createInsertableMethod();
        addOrInsertMethod(insertableMethod);
    }


    /**
     * Inserts java instructions into all constructors a given class.
     * @param insertableConstructor contains all information about insertion.
     * @throws CannotCompileException if the source contained in insertableMethod can't be compiled.
     * @throws AfterBurnerImpossibleException if something else goes wrong, wraps other exceptions.
     */
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
                                .getParameterTypes()));
            }
        } else {
            getLogger().warn("No suitable constructor was found in class {}. Add a constructor with a single argument : Activity, Fragment or View. Don't use non static inner classes.",
                    insertableConstructor.getClassToInsertInto().getName());
        }
    }

    /**
     * Returns the method name methodName in classToTransform. Null if not found.
     * Due to limitations of javassist, in case of multiple overloads, one of them only is returned.
     * (https://github.com/jboss-javassist/javassist/issues/9)
     * @param classToTransform the class that should contain a method methodName.
     * @param methodName the name of the method to retrieve.
     * @return the method name methodName in classToTransform. Null if not found.
     */
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
