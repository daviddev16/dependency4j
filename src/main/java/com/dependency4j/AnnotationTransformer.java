package com.dependency4j;

import com.dependency4j.util.StrUtil;

public final class AnnotationTransformer {

    public static QueryOptions transformPullAnnotationToQueryOptions(Pull pullAnnotation)  {

        final QueryOptions.QueryOptionsBuilder queryOptionsBuilder = QueryOptions.builder();

        if (!StrUtil.isNullOrBlank(pullAnnotation.value()))
            queryOptionsBuilder.filterByName(pullAnnotation.value());

        if (!pullAnnotation.retrieveAnyways())
            queryOptionsBuilder.disableRetrieveAnyways();

        return queryOptionsBuilder.build();
    }

}
