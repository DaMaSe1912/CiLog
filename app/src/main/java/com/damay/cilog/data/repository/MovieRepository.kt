package com.damay.cilog.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.damay.cilog.BuildConfig
import com.damay.cilog.data.model.CastMember
import com.damay.cilog.data.model.Movie
import com.damay.cilog.data.model.MovieDetail
import com.damay.cilog.data.model.TMDB_BACKDROP_BASE_URL
import com.damay.cilog.data.model.TMDB_LOGO_BASE_URL
import com.damay.cilog.data.model.TMDB_POSTER_BASE_URL
import com.damay.cilog.data.model.TMDB_PROFILE_BASE_URL
import com.damay.cilog.data.model.WatchProvider
import com.damay.cilog.data.model.toDomain
import com.damay.cilog.data.remote.MockMovieData
import com.damay.cilog.data.remote.MovieCategory
import com.damay.cilog.data.remote.MoviePagingSource
import com.damay.cilog.data.remote.TmdbApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

data class HomeOverviewData(
    val featuredList: List<Movie> = emptyList(),
    val popular: List<Movie> = emptyList(),
    val nowPlaying: List<Movie> = emptyList(),
    val upcoming: List<Movie> = emptyList(),
    val topRated: List<Movie> = emptyList()
) {
    val featured: Movie?
        get() = featuredList.firstOrNull()
}

@Singleton
class MovieRepository @Inject constructor(
    private val apiService: TmdbApiService
) {

    fun getMoviesStream(category: MovieCategory, language: String): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { MoviePagingSource(apiService, category = category, language = language) }
        ).flow
    }

    fun searchMoviesStream(query: String, language: String): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { MoviePagingSource(apiService, searchQuery = query, language = language) }
        ).flow
    }

    fun getHomeOverview(language: String): Flow<HomeOverviewData> = flow {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            val allMock = (MockMovieData.topRatedMovies + MockMovieData.popularMovies + MockMovieData.nowPlayingMovies + MockMovieData.upcomingMovies).distinctBy { it.id }
            val topFeatured = allMock.sortedByDescending { it.voteAverage }.take(5)
            emit(
                HomeOverviewData(
                    featuredList = topFeatured,
                    popular = MockMovieData.popularMovies,
                    nowPlaying = MockMovieData.nowPlayingMovies,
                    upcoming = MockMovieData.upcomingMovies,
                    topRated = MockMovieData.topRatedMovies
                )
            )
            return@flow
        }

        try {
            val popularDto = apiService.getPopularMovies(1, language)
            val nowPlayingDto = apiService.getNowPlayingMovies(1, language)
            val upcomingDto = apiService.getUpcomingMovies(1, language)
            val topRatedDto = apiService.getTopRatedMovies(1, language)

            val popularList = popularDto.results.map { it.toDomain() }
            val nowPlayingList = nowPlayingDto.results.map { it.toDomain() }
            val upcomingList = upcomingDto.results.map { it.toDomain() }
            val topRatedList = topRatedDto.results.map { it.toDomain() }

            val allCombined = (topRatedList + popularList + nowPlayingList + upcomingList).distinctBy { it.id }
            val topFeatured = allCombined.sortedByDescending { it.voteAverage }.take(5)

            emit(
                HomeOverviewData(
                    featuredList = topFeatured,
                    popular = popularList,
                    nowPlaying = nowPlayingList,
                    upcoming = upcomingList,
                    topRated = topRatedList
                )
            )
        } catch (e: Exception) {
            val allMock = (MockMovieData.topRatedMovies + MockMovieData.popularMovies).distinctBy { it.id }
            val topFeatured = allMock.sortedByDescending { it.voteAverage }.take(5)
            emit(
                HomeOverviewData(
                    featuredList = topFeatured,
                    popular = MockMovieData.popularMovies,
                    nowPlaying = MockMovieData.nowPlayingMovies,
                    upcoming = MockMovieData.upcomingMovies,
                    topRated = MockMovieData.topRatedMovies
                )
            )
        }
    }

    fun getMovieDetail(movieId: Int, language: String): Flow<Result<MovieDetail>> = flow {
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            emit(Result.success(MockMovieData.getDetailFor(movieId, language)))
            return@flow
        }

        try {
            val dto = apiService.getMovieDetail(movieId = movieId, language = language)

            var overviewText = dto.overview
            var isFallback = false

            // F-11: Fallback sinopsis jika bahasa Indonesia kosong
            if (overviewText.isBlank() && (language.startsWith("in") || language.startsWith("id"))) {
                try {
                    val enDto = apiService.getMovieDetail(movieId = movieId, language = "en-US")
                    if (enDto.overview.isNotBlank()) {
                        overviewText = enDto.overview
                        isFallback = true
                    }
                } catch (_: Exception) {
                    // Fallback attempt failed, keep blank
                }
            }

            // Extract official YouTube trailer
            val trailerKey = dto.videos?.results?.firstOrNull { video ->
                video.site.equals("YouTube", ignoreCase = true) &&
                        (video.type.equals("Trailer", ignoreCase = true) || video.type.equals("Teaser", ignoreCase = true))
            }?.key ?: dto.videos?.results?.firstOrNull { it.site.equals("YouTube", ignoreCase = true) }?.key

            // Cast list
            val castList = dto.credits?.cast?.take(10)?.map { castDto ->
                CastMember(
                    id = castDto.id,
                    name = castDto.name,
                    character = castDto.character ?: "",
                    profileUrl = castDto.profilePath?.let { "$TMDB_PROFILE_BASE_URL$it" }
                )
            } ?: emptyList()

            // Recommendations
            val recommendations = dto.recommendations?.results?.take(8)?.map { it.toDomain() } ?: emptyList()

            // Watch providers for Indonesia (ID) - streaming flatrate priority
            val idProviders = dto.watchProviders?.results?.get("ID")?.flatrate?.map { providerItem ->
                WatchProvider(
                    id = providerItem.providerId,
                    name = providerItem.providerName,
                    logoUrl = providerItem.logoPath?.let { "$TMDB_LOGO_BASE_URL$it" }
                )
            } ?: emptyList()

            val detail = MovieDetail(
                id = dto.id,
                title = dto.title.ifEmpty { "Untitled" },
                overview = overviewText,
                isOverviewFallback = isFallback,
                posterUrl = dto.posterPath?.let { "$TMDB_POSTER_BASE_URL$it" },
                backdropUrl = dto.backdropPath?.let { "$TMDB_BACKDROP_BASE_URL$it" },
                releaseDate = dto.releaseDate ?: "",
                voteAverage = dto.voteAverage,
                runtime = dto.runtime ?: 0,
                genres = dto.genres.map { it.name },
                trailerYoutubeKey = trailerKey,
                cast = castList,
                recommendations = recommendations,
                watchProviders = idProviders
            )

            emit(Result.success(detail))
        } catch (e: Exception) {
            // Fallback to mock on error to maintain active demo experience
            val fallbackDetail = MockMovieData.getDetailFor(movieId, language)
            emit(Result.success(fallbackDetail))
        }
    }
}
