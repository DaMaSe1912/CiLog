package com.damay.cilog.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.damay.cilog.BuildConfig
import com.damay.cilog.data.model.Movie
import com.damay.cilog.data.model.toDomain

enum class MovieCategory {
    ALL,
    POPULAR,
    NOW_PLAYING,
    UPCOMING,
    TOP_RATED
}

class MoviePagingSource(
    private val apiService: TmdbApiService,
    private val category: MovieCategory? = null,
    private val searchQuery: String? = null,
    private val language: String = "id-ID"
) : PagingSource<Int, Movie>() {

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1

        // If no TMDB API key is provided, gracefully serve rich demo data
        if (BuildConfig.TMDB_API_KEY.isBlank()) {
            val mockList = when {
                !searchQuery.isNullOrBlank() -> {
                    (MockMovieData.popularMovies + MockMovieData.nowPlayingMovies + MockMovieData.upcomingMovies + MockMovieData.topRatedMovies)
                        .distinctBy { it.id }
                        .filter { it.title.contains(searchQuery, ignoreCase = true) || it.overview.contains(searchQuery, ignoreCase = true) }
                }
                category == MovieCategory.NOW_PLAYING -> MockMovieData.nowPlayingMovies
                category == MovieCategory.UPCOMING -> MockMovieData.upcomingMovies
                category == MovieCategory.TOP_RATED -> MockMovieData.topRatedMovies
                else -> MockMovieData.popularMovies
            }

            return LoadResult.Page(
                data = if (page == 1) mockList else emptyList(),
                prevKey = if (page == 1) null else page - 1,
                nextKey = null
            )
        }

        return try {
            val response = when {
                !searchQuery.isNullOrBlank() -> {
                    apiService.searchMovies(query = searchQuery, page = page, language = language)
                }
                category == MovieCategory.NOW_PLAYING -> {
                    apiService.getNowPlayingMovies(page = page, language = language)
                }
                category == MovieCategory.UPCOMING -> {
                    apiService.getUpcomingMovies(page = page, language = language)
                }
                category == MovieCategory.TOP_RATED -> {
                    apiService.getTopRatedMovies(page = page, language = language)
                }
                else -> {
                    apiService.getPopularMovies(page = page, language = language)
                }
            }

            val movies = response.results.map { it.toDomain() }
            val nextKey = if (movies.isEmpty() || page >= response.totalPages) null else page + 1

            LoadResult.Page(
                data = movies,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            // If network fails on first page, fallback to mock data for flawless demo
            if (page == 1) {
                val fallbackList = when {
                    !searchQuery.isNullOrBlank() -> MockMovieData.popularMovies.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    category == MovieCategory.NOW_PLAYING -> MockMovieData.nowPlayingMovies
                    category == MovieCategory.UPCOMING -> MockMovieData.upcomingMovies
                    category == MovieCategory.TOP_RATED -> MockMovieData.topRatedMovies
                    else -> MockMovieData.popularMovies
                }
                LoadResult.Page(
                    data = fallbackList,
                    prevKey = null,
                    nextKey = null
                )
            } else {
                LoadResult.Error(e)
            }
        }
    }
}
