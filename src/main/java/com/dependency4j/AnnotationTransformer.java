package com.dependency4j;

import com.dependency4j.util.StrUtil;
import java.lang.reflect.*;

/**
 *
 * <b>AnnotationTransformer</b> is a utility class used for converting
 * annotation references to configuration objects. Configuration objects
 * are objects that hold immutable properties to be used by some functionality.
 *
 * @author daviddev16
 *
 **/
public final class AnnotationTransformer {

    /**
     *
     * This function converts {@link Pull} annotation to {@link QueryOptions}.
     * The {@code Pull} annotation is used to hold properties of {@link Field},
     * {@link Method} and {@link Constructor}.
     *
     * @return A non-null {@link QueryOptions} instance.
     *
     **/
    public static QueryOptions transformPullAnnotationToQueryOptions(Pull pullAnnotation)  {

        final QueryOptions.QueryOptionsBuilder queryOptionsBuilder = QueryOptions.builder();

        if (!StrUtil.isNullOrBlank(pullAnnotation.value()))
            queryOptionsBuilder.filterByName(pullAnnotation.value());

        if (!pullAnnotation.retrieveAnyways())
            queryOptionsBuilder.disableRetrieveAnyways();

        return queryOptionsBuilder.build();
    }
}
