package io.github.dependency4j;

import io.github.dependency4j.exception.ReflectionStateException;
import io.github.dependency4j.util.Checks;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * @since 1.0.7
 * */
public final class AnnotationDecomposer {

    public static final Object[] EMPTIES = new Object[0];

    @SuppressWarnings("unchecked")
    public static <R, T extends Annotation> R decomposeProperty(String propertyName, T annotation) {
        Class<?> annotationClassType = annotation.annotationType();
        Method equivalentMethod = findMappedEquivalentMethod(propertyName, annotationClassType);
        if (equivalentMethod != null) {
            Mapped mappedAnnotation = equivalentMethod.getAnnotation(Mapped.class);
            if (propertyName.equals(mappedAnnotation.value()))
                return (R) safeAnnotationMethodInvocation(equivalentMethod, annotation);
        }
        final int dotIndex = propertyName.indexOf(".");

        if (dotIndex == -1) {
            /* if no more path is passed, find the correspondent method */
            for (Method annotationMethod : annotationClassType.getMethods()) {

                if (propertyName.equals(annotationMethod.getName()))
                    return (R) safeAnnotationMethodInvocation(annotationMethod, annotation);

            }
            throw new NullPointerException("No property named " + propertyName + " was found.");
        }
        String currentToken = propertyName.substring(0, dotIndex);

        if (!currentToken.startsWith("@"))
            throw new ReflectionStateException(("\"%s\" is not a valid token. " +
                    "HINT: If the token is a annotation, it should starts with \"@\".").formatted(currentToken));

        String remainedTokens = propertyName.substring(dotIndex + 1);
        String currentAnnotationName = currentToken.substring(1);

        /* decompose property from itself */
        if (currentAnnotationName.equals(annotationClassType.getSimpleName()))
            return (R) decomposeProperty(remainedTokens, annotation);

        /* try to find the property from the composed annotations */
        for (Annotation childAnnotation : annotationClassType.getDeclaredAnnotations()) {
            Class<?> childAnnotationClassType = childAnnotation.annotationType();

            if (!childAnnotationClassType.getSimpleName().equals(currentAnnotationName))
                continue;

            Object decomposedPropertyValue = decomposeProperty(remainedTokens, childAnnotation);

            if (decomposedPropertyValue != null)
                return (R) decomposedPropertyValue;
        }
        return null;
    }

    public static boolean isAnnotationComposed(AnnotatedElement annotatedElement,
                                               Class<? extends Annotation> annotationClassType) {
        return decomposeAnnotationFromMember(annotatedElement, annotationClassType) != null;
    }

    public static <T extends Annotation> T decomposeAnnotationFromMember(AnnotatedElement annotatedElement,
                                                                         Class<T> annotationClassType) {

        for (Annotation classTypeAnnotation : annotatedElement.getAnnotations()) {
            final Annotation decomposedAnnotation = decompose(classTypeAnnotation, annotationClassType);
            if (decomposedAnnotation != null)
                return (T) decomposedAnnotation;
        }
        return null;
    }

    public static Annotation decomposeSiblingAnnotation(AnnotatedElement annotatedElement,
                                                        Class<?> annotationClassType) {

        if (annotatedElement.getClass().equals(annotationClassType))
            return (Annotation) annotatedElement;

        for (Annotation classTypeAnnotation : annotatedElement.getAnnotations()) {
            final Annotation decomposedAnnotation =
                    decompose(classTypeAnnotation, annotationClassType);

            if (decomposedAnnotation != null)
                return classTypeAnnotation;
        }
        return null;
    }

    public static <T extends Annotation, R extends Annotation> T decompose(R wrapper,
                                                                           Class<? super T> annotationClassType) {
        return internalDecompose(wrapper, annotationClassType);
    }

    @SuppressWarnings({"unchecked"})
    private static <T extends Annotation> T internalDecompose(Annotation baseAnnotation,
                                                             Class<? super T> annotationClassType) {

        Checks.nonNull(baseAnnotation, "baseAnnotation must not be null.");
        Checks.nonNull(annotationClassType, "annotationClassType must not be null.");

        Class<?> baseAnnotationClassType = baseAnnotation.annotationType();

        if (baseAnnotationClassType.equals(annotationClassType))
            return (T) baseAnnotation;

        else if (checkSelfLoopingAnnotation(baseAnnotationClassType))
            return null;

        T recursiveDecomposedAnnotation = null;
        for (Annotation annotation : baseAnnotationClassType.getDeclaredAnnotations()) {
            if (annotation.annotationType().equals(annotationClassType))
                return (T) annotation;

            recursiveDecomposedAnnotation = internalDecompose(annotation, annotationClassType);
            if (recursiveDecomposedAnnotation != null)
                break;
        }
        return recursiveDecomposedAnnotation;
    }

    public static boolean checkSelfLoopingAnnotation(Class<?> annotationClassType) {
        for (Annotation parentAnnotation : annotationClassType.getDeclaredAnnotations()) {
            Class<?> parentAnnotationClassType = parentAnnotation.annotationType();
            if (parentAnnotationClassType.equals(annotationClassType))
                return true;
        }
        return false;
    }

    private static Method findMappedEquivalentMethod(String propertyName, Class<?> annotationClassType) {
        for (Method annotationMethod : annotationClassType.getDeclaredMethods()) {
            if (annotationMethod.isAnnotationPresent(Mapped.class))
                return annotationMethod;
        }
        return null;
    }

    private static Object safeAnnotationMethodInvocation(Method method, Annotation annotation) {
        try {
            return method.invoke(annotation, EMPTIES);
        } catch (Exception exception) {
            throw new ReflectionStateException(("Failed to invoke \"%s\" " +
                    "annotation method.").formatted(method.getName()), exception);
        }
    }

}
