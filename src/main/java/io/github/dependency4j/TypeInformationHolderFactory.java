package io.github.dependency4j;

import io.github.dependency4j.util.ReflectionUtil;
import io.github.dependency4j.util.StrUtil;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.dependency4j.AnnotationDecomposer.decomposeProperty;
import static io.github.dependency4j.AnnotationDecomposer.decomposeSiblingAnnotation;

/**
 * <b>TypeInformationHolderFactory</b> is responsible for wrapping a class type to a
 * new {@link TypeInformationHolder}. This class decompose the {@link Managed} annotation
 * properties to the {@link TypeInformationHolder} related methods.
 *
 * @author daviddev16
 * @version 1.0.8
 **/
public final class TypeInformationHolderFactory {

    public static final String MANAGED_PROPERTY_NAME       = "@Managed.name";
    public static final String MANAGED_PROPERTY_STRATEGY   = "@Managed.strategy";
    public static final String MANAGED_PROPERTY_DISPOSABLE = "@Managed.disposable";
    public static final String MANAGED_PROPERTY_DYNAMIC    = "@Managed.dynamic";

    private TypeInformationHolderFactory() {}

    public static TypeInformationHolder createTypeInformation(Class<?> classType) {
        Annotation managedSiblingAnnotation =
                decomposeSiblingAnnotation(classType, Managed.class);

        if (managedSiblingAnnotation == null)
            return createDefaultTypeInformation(classType);

        return new TypeInformationHolder() {
            private String name             = null;
            private List<String> strategies = null;
            private Boolean disposableFlag  = null;
            private Boolean dynamicFlag     = null;

            @Override
            public String getName() {
                return (name == null) ? (name = StrUtil.coalesceBlank(
                        decomposeProperty(MANAGED_PROPERTY_NAME, managedSiblingAnnotation),
                        classType.getSimpleName()))
                        : name;
            }
            @Override
            public List<String> getStrategies() {
                return (strategies == null) ? (strategies =
                        createStrategyListFromAnnotation(managedSiblingAnnotation))
                        : strategies;
            }
            @Override
            public boolean isDisposable() {
                return (disposableFlag == null) ? (disposableFlag =
                        createBooleanPropertyValue(MANAGED_PROPERTY_DISPOSABLE, managedSiblingAnnotation))
                        : disposableFlag;
            }
            @Override
            public boolean isDynamic() {
                return (dynamicFlag == null) ? (dynamicFlag =
                        createBooleanPropertyValue(MANAGED_PROPERTY_DYNAMIC, managedSiblingAnnotation))
                        : dynamicFlag;
            }
            @Override
            public Class<?> getWrappedClassType() {
                return classType;
            }
        };
    }

    private static List<String> createStrategyListFromAnnotation(Annotation managedSiblingAnnotation) {
        Strategy decomposedStrategyInformation =
                decomposeProperty(MANAGED_PROPERTY_STRATEGY, managedSiblingAnnotation);

        if (decomposedStrategyInformation == null)
            return Collections.emptyList();

        return Stream
                .of(decomposedStrategyInformation.value())
                .collect(Collectors.toList());
    }

    private static Boolean createBooleanPropertyValue(String flagPropertyName,
                                                      Annotation managedSiblingAnnotation) {
        return ReflectionUtil.defaultValueWhenNull(
                Boolean.class,
                decomposeProperty(flagPropertyName, managedSiblingAnnotation));
    }

    private static TypeInformationHolder createDefaultTypeInformation(Class<?> classType) {
        return new TypeInformationHolder() {
            @Override
            public String getName() {
                return classType.getSimpleName();
            }
            @Override
            public List<String> getStrategies() {
                return Collections.emptyList();
            }
            @Override
            public boolean isDisposable() {
                return true;
            }
            @Override
            public boolean isDynamic() {
                return false;
            }
            @Override
            public Class<?> getWrappedClassType() {
                return classType;
            }
        };
    }



}
