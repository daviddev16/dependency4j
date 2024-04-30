package io.github.dependency4j;

import io.github.dependency4j.util.Checks;

import java.lang.annotation.Annotation;

public @InternalDynamicallyManaged class AnnotationDecomposer {

    public boolean isAnnotationComposed(Class<?> classType, Class<? extends Annotation> annotationClassType) {
        return decomposeAnnotationFromClassType(classType, annotationClassType) != null;
    }

    public <T extends Annotation> T decomposeAnnotationFromClassType(Class<?> classType, Class<T> annotationClassType) {

        for (Annotation classTypeAnnotation : classType.getAnnotations()) {

            final Annotation decomposedAnnotation =
                    decompose(classTypeAnnotation, annotationClassType);

            if (decomposedAnnotation != null)
                return (T) decomposedAnnotation;
        }
        return null;
    }

    public <T extends Annotation, R extends Annotation> T decompose(R wrapper, Class<T> annotationClassType) {
        return internalDecompose(wrapper, annotationClassType);
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Annotation> T internalDecompose(Annotation baseAnnotation, Class<T> annotationClassType) {

        Checks.nonNull(baseAnnotation, "baseAnnotation must not be null.");
        Checks.nonNull(annotationClassType, "annotationClassType must not be null.");

        Class<?> baseAnnotationClassType = baseAnnotation.annotationType();

        if (baseAnnotationClassType.equals(annotationClassType))
            return (T) baseAnnotation;

        else if (checkSelfLoopingAnnotation(baseAnnotationClassType))
            return null;

        T recursiveDecomposedAnnotation = null;

        for (Annotation annotation : baseAnnotationClassType.getDeclaredAnnotations())
        {
            if (annotation.annotationType().equals(annotationClassType))
                return (T) annotation;

            recursiveDecomposedAnnotation = internalDecompose(annotation, annotationClassType);

            if (recursiveDecomposedAnnotation != null)
                break;
        }

        return recursiveDecomposedAnnotation;
    }

    public boolean checkSelfLoopingAnnotation(Class<?> annotationClassType) {

        for (Annotation parentAnnotation : annotationClassType.getDeclaredAnnotations())
        {
            Class<?> parentAnnotationClassType = parentAnnotation.annotationType();

            if (parentAnnotationClassType.equals(annotationClassType))
                return true;
        }

        return false;
    }

}
