package com.bocse.perfume.data;

import org.jsondoc.core.annotation.ApiObject;

/**
 * Created by bocse on 12.12.2015.
 */
@ApiObject(name = "Recommendation", description = "Defines the recommendation algorithm used.", show = false)
public enum RecommendationAlgorithm {
    textSearch,
    favoriteSimilarity,
    favoriteDistance,
    profileSimilarity,
    profileDistance,
    expertOpinion
}
