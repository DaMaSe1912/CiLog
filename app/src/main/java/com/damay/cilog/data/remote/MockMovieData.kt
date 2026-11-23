package com.damay.cilog.data.remote

import com.damay.cilog.data.model.CastMember
import com.damay.cilog.data.model.Movie
import com.damay.cilog.data.model.MovieDetail
import com.damay.cilog.data.model.WatchProvider

object MockMovieData {

    val popularMovies = listOf(
        Movie(
            id = 1022789,
            title = "Inside Out 2",
            overview = "Remaja Riley menghadapi berbagai emosi baru di markas pusat pikirannya, termasuk Anxiety, saat ia memasuki masa puber.",
            posterUrl = "https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/xg27NrXi7VXCGUr7MG75UqLl6Vg.jpg",
            releaseDate = "2024-06-11",
            voteAverage = 7.6,
            voteCount = 4800
        ),
        Movie(
            id = 533535,
            title = "Deadpool & Wolverine",
            overview = "Wade Wilson yang lesu kembali beraksi saat Time Variance Authority menariknya ke dalam misi berbahaya bersama Wolverine.",
            posterUrl = "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/yDHYTfA3R0jFYba16jBB1ef8oIt.jpg",
            releaseDate = "2024-07-24",
            voteAverage = 7.7,
            voteCount = 5900
        ),
        Movie(
            id = 693134,
            title = "Dune: Part Two",
            overview = "Paul Atreides bersatu dengan Chani dan suku Fremen untuk membalas dendam terhadap konspirator yang menghancurkan keluarganya.",
            posterUrl = "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/xOMo8BRK7PfcJv9JCnx7s5200bm.jpg",
            releaseDate = "2024-02-27",
            voteAverage = 8.2,
            voteCount = 5300
        ),
        Movie(
            id = 823464,
            title = "Godzilla x Kong: The New Empire",
            overview = "Dua titan kuno Godzilla dan Kong harus bersatu melawan ancaman kolosal yang tersembunyi di dalam perut bumi.",
            posterUrl = "https://image.tmdb.org/t/p/w500/z1p34vh7dEOnLDmyCrlUVLuoDzd.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/qrGtVFuvYmDY3r1QoJ4mKz2n0qg.jpg",
            releaseDate = "2024-03-27",
            voteAverage = 7.1,
            voteCount = 3700
        ),
        Movie(
            id = 519182,
            title = "Despicable Me 4",
            overview = "Gru dan keluarganya terpaksa melarikan diri ketika musuh baru Maxime Le Mal membalas dendam dengan pasukan serangga.",
            posterUrl = "https://image.tmdb.org/t/p/w500/wWba3TaojhK7NdycRhoQpsG0FaH.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/lgkGysTkdwGjBzb6gD6XnEa92uV.jpg",
            releaseDate = "2024-06-20",
            voteAverage = 7.1,
            voteCount = 2300
        ),
        Movie(
            id = 912649,
            title = "Venom: The Last Dance",
            overview = "Eddie Brock dan Venom menjadi buronan dari kedua dunia mereka dalam aksi penutup trilogi yang spektakuler.",
            posterUrl = "https://image.tmdb.org/t/p/w500/aosm8Vh99xcBGvtY9hdmgapwKO2.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/3V4kLQg0kSqPLctI5ziYWabAZYF.jpg",
            releaseDate = "2024-10-22",
            voteAverage = 6.8,
            voteCount = 1800
        )
    )

    val nowPlayingMovies = listOf(
        Movie(
            id = 912649,
            title = "Venom: The Last Dance",
            overview = "Eddie Brock dan Venom diburu oleh militer manusia dan pasukan makhluk simbiotik dari planet asal Venom.",
            posterUrl = "https://image.tmdb.org/t/p/w500/aosm8Vh99xcBGvtY9hdmgapwKO2.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/3V4kLQg0kSqPLctI5ziYWabAZYF.jpg",
            releaseDate = "2024-10-22",
            voteAverage = 6.8,
            voteCount = 1800
        ),
        Movie(
            id = 1034541,
            title = "Terrifier 3",
            overview = "Art the Clown menebar teror mengerikan pada malam Natal di Miles County yang tenang.",
            posterUrl = "https://image.tmdb.org/t/p/w500/63UkDbhubUq1bIOV72iRaoU7gT3.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/18TSJF1WLA4CkymvVUHQO7qDGL9.jpg",
            releaseDate = "2024-10-09",
            voteAverage = 6.9,
            voteCount = 1200
        ),
        Movie(
            id = 1184918,
            title = "The Wild Robot",
            overview = "Sebuah robot bernama Roz terdampar di pulau tak berpenghuni dan harus belajar beradaptasi dengan hewan-hewan liar di sekitarnya.",
            posterUrl = "https://image.tmdb.org/t/p/w500/wTnV3PCVW5O92JMrZ1b0eQywyh5.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/417tYZ4XUyJr6ycJCGDQA7nwupB.jpg",
            releaseDate = "2024-09-12",
            voteAverage = 8.4,
            voteCount = 3100
        ),
        Movie(
            id = 933260,
            title = "The Substance",
            overview = "Mantan bintang TV yang mulai menua mencoba obat eksperimental dari pasar gelap untuk menciptakan versi dirinya yang lebih muda.",
            posterUrl = "https://image.tmdb.org/t/p/w500/lqoMzCcZYEFK729Fc6r0pfZebBQ.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/7h6TqPB3ESmPFVosDbEGvUAv1IV.jpg",
            releaseDate = "2024-09-07",
            voteAverage = 7.3,
            voteCount = 2200
        )
    )

    val upcomingMovies = listOf(
        Movie(
            id = 402431,
            title = "Wicked",
            overview = "Kisah asal usul Elphaba yang kelak dikenal sebagai Penyihir Jahat dari Barat dan persahabatannya dengan Glinda.",
            posterUrl = "https://image.tmdb.org/t/p/w500/xDGbZ0JJ3mYaGKy4Nzd9Kph6M9L.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/uKb22E5ww9bXhZ9L48BGhFSmq5P.jpg",
            releaseDate = "2024-11-20",
            voteAverage = 7.5,
            voteCount = 680
        ),
        Movie(
            id = 1241982,
            title = "Moana 2",
            overview = "Moana menerima panggilan tak terduga dari leluhur pemandu jalan dan memulai pelayaran melintasi lautan luas Oceania.",
            posterUrl = "https://image.tmdb.org/t/p/w500/yh64qw9mgXBvlaWDi7Q9tpUBAvH.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/tElnmtQ6yszmi5Gj8aR04Wb2Q2q.jpg",
            releaseDate = "2024-11-27",
            voteAverage = 7.0,
            voteCount = 520
        ),
        Movie(
            id = 558449,
            title = "Gladiator II",
            overview = "Bertahun-tahun setelah kematian Maximus, Lucius dipaksa memasuki Colosseum setelah rumahnya ditaklukkan oleh kaisar tiran Roma.",
            posterUrl = "https://image.tmdb.org/t/p/w500/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/euYIwmwkmz95mnEx7vG5b29W48t.jpg",
            releaseDate = "2024-11-13",
            voteAverage = 6.8,
            voteCount = 1400
        ),
        Movie(
            id = 993710,
            title = "Mufasa: The Lion King",
            overview = "Menceritakan kisah kebangkitan Mufasa dari anak singa yatim piatu yang tersesat hingga menjadi raja legendaris Pride Lands.",
            posterUrl = "https://image.tmdb.org/t/p/w500/lurEK87kukWNaHd0z3Ws9661SSG.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/b85bJmwvUV7B620gY2uFmD0zM9I.jpg",
            releaseDate = "2024-12-18",
            voteAverage = 7.1,
            voteCount = 310
        )
    )

    val topRatedMovies = listOf(
        Movie(
            id = 94605,
            title = "Arcane",
            overview = "Di tengah pertikaian sengit antara kota kembar Piltover dan Zaun, dua saudari bertarung di pihak yang berseberangan dalam perang teknologi sihir dan keyakinan.",
            posterUrl = "https://image.tmdb.org/t/p/w500/fqldf2t8ztc9aiwn397FvFeQUjM.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/27W462sZGErmm4z8vuhk6tQ3W6v.jpg",
            releaseDate = "2024-11-09",
            voteAverage = 8.9,
            voteCount = 5400
        ),
        Movie(
            id = 278,
            title = "The Shawshank Redemption",
            overview = "Kisah harapan dan persahabatan seorang bankir yang dijatuhi hukuman penjara seumur hidup atas kejahatan yang tidak dilakukannya.",
            posterUrl = "https://image.tmdb.org/t/p/w500/9cqNxx0GxF0bflZmeSMuL5tnGzr.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/kXfqcdQKsToO0OUXHcrrNCHDBzO.jpg",
            releaseDate = "1994-09-23",
            voteAverage = 8.7,
            voteCount = 26500
        ),
        Movie(
            id = 238,
            title = "The Godfather",
            overview = "Kisah keluarga mafia Corleone di bawah kepemimpinan patriark Don Vito Corleone dan transformasi putranya Michael.",
            posterUrl = "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/tmU7GeKVybMWFButWEGl2M4GeiP.jpg",
            releaseDate = "1972-03-14",
            voteAverage = 8.7,
            voteCount = 20200
        ),
        Movie(
            id = 155,
            title = "The Dark Knight",
            overview = "Batman, Letnan James Gordon, dan Jaksa Harvey Dent berhadapan dengan ancaman anarki yang ditebar oleh Joker di Gotham City.",
            posterUrl = "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/dqK9Hag1054tghRQSqLSfrkvQnA.jpg",
            releaseDate = "2008-07-16",
            voteAverage = 8.5,
            voteCount = 32000
        ),
        Movie(
            id = 129,
            title = "Spirited Away",
            overview = "Chihiro yang berusia 10 tahun terperangkap di dunia roh dan harus bekerja di pemandian milik penyihir Yubaba untuk menyelamatkan orang tuanya.",
            posterUrl = "https://image.tmdb.org/t/p/w500/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg",
            backdropUrl = "https://image.tmdb.org/t/p/w780/mSDsSDwaP3E7dEfUPWy4J0djt4O.jpg",
            releaseDate = "2001-07-20",
            voteAverage = 8.5,
            voteCount = 16200
        )
    )

    fun getDetailFor(movieId: Int, language: String): MovieDetail {
        val base = (popularMovies + nowPlayingMovies + upcomingMovies + topRatedMovies).find { it.id == movieId }
            ?: popularMovies.first()

        val isIndonesian = language.startsWith("in") || language.startsWith("id")
        val overviewText = if (isIndonesian) {
            base.overview
        } else {
            "Teenager Riley faces new emotions at headquarters including Anxiety as she navigates puberty in this acclaimed cinematic adventure."
        }

        return MovieDetail(
            id = base.id,
            title = base.title,
            overview = overviewText,
            isOverviewFallback = false,
            posterUrl = base.posterUrl,
            backdropUrl = base.backdropUrl,
            releaseDate = base.releaseDate,
            voteAverage = base.voteAverage,
            runtime = 96,
            genres = listOf("Animation", "Family", "Adventure", "Comedy"),
            trailerYoutubeKey = "LEjhY15eCx0", // Official Inside Out 2 trailer key
            cast = listOf(
                CastMember(1, "Amy Poehler", "Joy (suara)", "https://image.tmdb.org/t/p/w185/rIH2cK10344qWnC1eR27VqN5V2I.jpg"),
                CastMember(2, "Maya Hawke", "Anxiety (suara)", "https://image.tmdb.org/t/p/w185/8Z7ZqGjV76D7j1r734K3c3n3L1o.jpg"),
                CastMember(3, "Kensington Tallman", "Riley Andersen (suara)", null),
                CastMember(4, "Liza Lapira", "Disgust (suara)", null)
            ),
            recommendations = popularMovies.filter { it.id != base.id }.take(4),
            watchProviders = listOf(
                WatchProvider(337, "Disney+ Hotstar", "https://image.tmdb.org/t/p/w154/7rwgEs15tFwyR9NPQ5vpzxTj19Q.jpg"),
                WatchProvider(8, "Netflix", "https://image.tmdb.org/t/p/w154/pbpMk2JmcoNnQ7rNue0msv90KG8.jpg"),
                WatchProvider(119, "Amazon Prime Video", "https://image.tmdb.org/t/p/w154/emthp39XA2aquDH0peaK3nm25RI.jpg")
            )
        )
    }
}
