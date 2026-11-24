package com.damay.cilog.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.damay.cilog.R
import com.damay.cilog.data.remote.MovieCategory
import com.damay.cilog.ui.components.CategoryFilterChips
import com.damay.cilog.ui.components.CinematicEmptyState
import com.damay.cilog.ui.components.CinematicErrorState
import com.damay.cilog.ui.components.CinematicLoadingIndicator
import com.damay.cilog.ui.components.HeroFeaturedCard
import com.damay.cilog.ui.components.HeroFeaturedCarousel
import com.damay.cilog.ui.components.MoviePosterCard
import com.damay.cilog.ui.components.MovieRowSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMovieClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val homeOverview by viewModel.homeOverview.collectAsState()
    val moviePagingItems = viewModel.movies.collectAsLazyPagingItems()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Movie,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = stringResource(R.string.app_tagline),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Filter Chips (Semua | Populer | Sedang Tayang | Akan Tayang | Rating Tertinggi)
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.onCategorySelected(it) }
            )

            if (selectedCategory == MovieCategory.ALL) {
                // Disney+ / Hotstar / LK21 Style: Hero Featured Banner + Horizontal Rows
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Auto-Rotating Hero Featured Spotlight Carousel
                    if (homeOverview.featuredList.isNotEmpty()) {
                        item {
                            HeroFeaturedCarousel(
                                featuredList = homeOverview.featuredList,
                                onMovieClick = onMovieClick,
                                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                            )
                        }
                    }

                    // 2. Popular Section
                    item {
                        MovieRowSection(
                            title = stringResource(R.string.category_popular),
                            movies = homeOverview.popular,
                            onMovieClick = onMovieClick,
                            onSeeAllClick = { viewModel.onCategorySelected(MovieCategory.POPULAR) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 3. Now Playing Section
                    item {
                        MovieRowSection(
                            title = stringResource(R.string.category_now_playing),
                            movies = homeOverview.nowPlaying,
                            onMovieClick = onMovieClick,
                            onSeeAllClick = { viewModel.onCategorySelected(MovieCategory.NOW_PLAYING) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 4. Upcoming Section
                    item {
                        MovieRowSection(
                            title = stringResource(R.string.category_upcoming),
                            movies = homeOverview.upcoming,
                            onMovieClick = onMovieClick,
                            onSeeAllClick = { viewModel.onCategorySelected(MovieCategory.UPCOMING) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 5. Top Rated Section
                    item {
                        MovieRowSection(
                            title = stringResource(R.string.category_top_rated),
                            movies = homeOverview.topRated,
                            onMovieClick = onMovieClick,
                            onSeeAllClick = { viewModel.onCategorySelected(MovieCategory.TOP_RATED) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                // Opsi A: Deep Vertical Infinite Scroll Grid with Paging 3
                val refreshState = moviePagingItems.loadState.refresh

                when {
                    refreshState is LoadState.Loading && moviePagingItems.itemCount == 0 -> {
                        CinematicLoadingIndicator()
                    }
                    refreshState is LoadState.Error && moviePagingItems.itemCount == 0 -> {
                        CinematicErrorState(
                            onRetry = { moviePagingItems.retry() }
                        )
                    }
                    refreshState !is LoadState.Loading && moviePagingItems.itemCount == 0 -> {
                        CinematicEmptyState(
                            title = stringResource(R.string.search_empty_title),
                            description = stringResource(R.string.search_empty_desc)
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(count = moviePagingItems.itemCount) { index ->
                                val movie = moviePagingItems[index]
                                if (movie != null) {
                                    MoviePosterCard(
                                        movie = movie,
                                        onMovieClick = onMovieClick
                                    )
                                }
                            }

                            // Bottom loading state for pagination append
                            if (moviePagingItems.loadState.append is LoadState.Loading) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp),
                                            strokeWidth = 2.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
