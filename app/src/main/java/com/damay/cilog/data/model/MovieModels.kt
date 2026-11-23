package com.damay.cilog.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TMDB Image Base URLs
const val TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w500"
const val TMDB_BACKDROP_BASE_URL = "https://image.tmdb.org/t/p/w780"
const val TMDB_PROFILE_BASE_URL = "https://image.tmdb.org/t/p/w185"
const val TMDB_LOGO_BASE_URL = "https://image.tmdb.org/t/p/w154"

@Serializable
data class MovieResponseDto(
    val page: Int = 1,
    val results: List<MovieDto> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1,
    @SerialName("total_results") val totalResults: Int = 0
)

@Serializable
data class MovieDto(
    val id: Int,
    val title: String = "",
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    @SerialName("genre_ids") val genreIds: List<Int> = emptyList()
)

@Serializable
data class GenreDto(
    val id: Int,
    val name: String
)

@Serializable
data class VideoResponseDto(
    val results: List<VideoDto> = emptyList()
)

@Serializable
data class VideoDto(
    val id: String = "",
    val key: String = "",
    val name: String = "",
    val site: String = "",
    val type: String = "",
    val official: Boolean = false
)

@Serializable
data class CreditsDto(
    val cast: List<CastDto> = emptyList()
)

@Serializable
data class CastDto(
    val id: Int,
    val name: String,
    val character: String? = null,
    @SerialName("profile_path") val profilePath: String? = null
)

@Serializable
data class WatchProviderCountryDto(
    val flatrate: List<ProviderItemDto>? = null,
    val rent: List<ProviderItemDto>? = null,
    val buy: List<ProviderItemDto>? = null
)

@Serializable
data class WatchProvidersResponseDto(
    val results: Map<String, WatchProviderCountryDto> = emptyMap()
)

@Serializable
data class ProviderItemDto(
    @SerialName("provider_id") val providerId: Int,
    @SerialName("provider_name") val providerName: String,
    @SerialName("logo_path") val logoPath: String? = null
)

@Serializable
data class MovieDetailDto(
    val id: Int,
    val title: String = "",
    val overview: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("vote_count") val voteCount: Int = 0,
    val runtime: Int? = null,
    val genres: List<GenreDto> = emptyList(),
    val videos: VideoResponseDto? = null,
    val credits: CreditsDto? = null,
    val recommendations: MovieResponseDto? = null,
    @SerialName("watch/providers") val watchProviders: WatchProvidersResponseDto? = null
)

// Clean Domain Models
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?
)

data class WatchProvider(
    val id: Int,
    val name: String,
    val logoUrl: String?
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val isOverviewFallback: Boolean,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val runtime: Int,
    val genres: List<String>,
    val trailerYoutubeKey: String?,
    val cast: List<CastMember>,
    val recommendations: List<Movie>,
    val watchProviders: List<WatchProvider>
)

data class FavoriteMovie(
    val movieId: Int = 0,
    val title: String = "",
    val posterPath: String? = null,
    val releaseDate: String = "",
    val voteAverage: Double = 0.0,
    val addedAt: Long = 0L
) {
    val posterUrl: String?
        get() = posterPath?.let { if (it.startsWith("http")) it else "$TMDB_POSTER_BASE_URL$it" }
}

fun MovieDto.toDomain(): Movie {
    return Movie(
        id = id,
        title = title.ifEmpty { "Untitled" },
        overview = overview,
        posterUrl = posterPath?.let { "$TMDB_POSTER_BASE_URL$it" },
        backdropUrl = backdropPath?.let { "$TMDB_BACKDROP_BASE_URL$it" },
        releaseDate = releaseDate ?: "",
        voteAverage = voteAverage,
        voteCount = voteCount
    )
}
