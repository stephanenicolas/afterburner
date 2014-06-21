package com.github.stephanenicolas.afterburner.inserts;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class SignatureExtractor {

    public String extractSignature(CtMethod overridenMethod) throws NotFoundException {
        return extractModifier(overridenMethod) + " "
                + extractReturnType(overridenMethod) + " "
                + overridenMethod.getName() + "("
                + extractParametersAndTypes(overridenMethod) + ")"
                + extractThrowClause(overridenMethod);
    }
    
    public String invokeSuper(CtMethod method) throws NotFoundException {
        return "super."
                + method.getName() + "("
                + extractParameters(method) + ")";
    }

    private String extractThrowClause(CtMethod overridenMethod) throws NotFoundException {
        int indexException = 0;
        StringBuilder builder = new StringBuilder();
        for (CtClass paramType : overridenMethod.getExceptionTypes()) {
            builder.append(paramType.getName());
            if (indexException < overridenMethod.getExceptionTypes().length - 1) {
                builder.append(", ");
            }   
            indexException++;
        }
        if (builder.length() != 0) {
            builder.insert(0, " throws ");
        }
        return builder.toString();
    }

    private String extractParametersAndTypes(CtMethod overridenMethod) throws NotFoundException {
        StringBuilder builder = new StringBuilder();
        int indexParam = 0;
        for (CtClass paramType : overridenMethod.getParameterTypes()) {
            builder.append(paramType.getName());
            builder.append(" ");
            builder.append("p" + indexParam);
            if (indexParam < overridenMethod.getParameterTypes().length - 1) {
                builder.append(", ");
            }
            indexParam++;
        }
        return builder.toString();
    }

    private String extractParameters(CtMethod overridenMethod) throws NotFoundException {
        StringBuilder builder = new StringBuilder();
        for (int indexParam = 0; indexParam < overridenMethod.getParameterTypes().length; indexParam++) {
            builder.append("p" + indexParam);
            if (indexParam < overridenMethod.getParameterTypes().length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private String extractReturnType(CtMethod overridenMethod) throws NotFoundException {
        return overridenMethod.getReturnType().getName();
    }

    private String extractModifier(CtMethod overridenMethod) {
        return Modifier.toString(overridenMethod.getModifiers());
    }
}
