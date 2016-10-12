package br.com.tiagohs.popmovies.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import br.com.tiagohs.popmovies.BuildConfig;
import br.com.tiagohs.popmovies.R;
import br.com.tiagohs.popmovies.model.atwork.Artwork;
import br.com.tiagohs.popmovies.model.dto.ImageDTO;
import br.com.tiagohs.popmovies.model.dto.ItemListDTO;
import br.com.tiagohs.popmovies.model.dto.ListActivityDTO;
import br.com.tiagohs.popmovies.model.media.Video;
import br.com.tiagohs.popmovies.model.movie.MovieDetails;
import br.com.tiagohs.popmovies.model.response.VideosResponse;
import br.com.tiagohs.popmovies.presenter.MovieDetailsPresenter;
import br.com.tiagohs.popmovies.util.AnimationsUtils;
import br.com.tiagohs.popmovies.util.ImageUtils;
import br.com.tiagohs.popmovies.util.ViewUtils;
import br.com.tiagohs.popmovies.util.enumerations.ImageSize;
import br.com.tiagohs.popmovies.util.enumerations.ItemType;
import br.com.tiagohs.popmovies.util.enumerations.ListType;
import br.com.tiagohs.popmovies.util.enumerations.Sort;
import br.com.tiagohs.popmovies.view.AppBarMovieListener;
import br.com.tiagohs.popmovies.view.MovieDetailsView;
import br.com.tiagohs.popmovies.view.adapters.ListWordsAdapter;
import br.com.tiagohs.popmovies.view.callbacks.ImagesCallbacks;
import br.com.tiagohs.popmovies.view.callbacks.ListMoviesCallbacks;
import br.com.tiagohs.popmovies.view.callbacks.ListWordsCallbacks;
import br.com.tiagohs.popmovies.view.callbacks.MovieVideosCallbacks;
import br.com.tiagohs.popmovies.view.callbacks.PersonCallbacks;
import br.com.tiagohs.popmovies.view.callbacks.ReviewCallbacks;
import br.com.tiagohs.popmovies.view.fragment.MovieDetailsFragment;
import butterknife.BindView;
import butterknife.OnClick;

public class MovieDetailActivity extends BaseActivity implements MovieDetailsView,
        MovieVideosCallbacks, ImagesCallbacks,
        PersonCallbacks, ReviewCallbacks,
        ListMoviesCallbacks, ListWordsCallbacks {
    private static final String TAG = MovieDetailActivity.class.getSimpleName();

    private static final int REQ_START_STANDALONE_PLAYER = 1;
    private static final int REQ_RESOLVE_SERVICE_MISSING = 2;
    private static final int RECYCLER_VIEW_ORIENTATION = LinearLayoutManager.HORIZONTAL;

    private static final String MOVIE = "movie";
    private static final String START = "start";
    private static final String EXTRA_MOVIE_ID = "br.com.tiagohs.popmovies.movie";

    @BindView(R.id.movie_detail_app_bar)          AppBarLayout mAppBarLayout;
    @BindView(R.id.poster_movie)                  ImageView mPosterMovie;
    @BindView(R.id.background_movie)              ImageView mBackgroundMovie;
    @BindView(R.id.title_movie)                   TextView mTitleMovie;
    @BindView(R.id.ano_lancamento_movie)          TextView mAnoLancamento;
    @BindView(R.id.duracao_movie)                 TextView mDuracao;
    @BindView(R.id.diretores_recycler_view)       RecyclerView mDiretoresRecyclerView;
    @BindView(R.id.play_image_movie_principal)    ImageView playButtonImageView;
    @BindView(R.id.movies_btn_ja_assisti)         FloatingActionButton mJaAssistiButton;
    @BindView(R.id.progress_movies_details)       ProgressBar mProgressMovieDetails;
    @BindView(R.id.movie_details_fragment)        LinearLayout mContainerTabs;

    @Inject MovieDetailsPresenter mPresenter;

    private int mMovieID;
    private boolean mIsWatchPressed;
    private MovieDetails mMovie;
    private ListWordsAdapter mDiretoresAdapter;
    private boolean isStarted;

    public static Intent newIntent(Context context, int movieID) {
        Intent intent = new Intent(context, MovieDetailActivity.class);
        intent.putExtra(EXTRA_MOVIE_ID, movieID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getApplicationComponent().inject(this);
        mPresenter.setView(this);
        mJaAssistiButton.hide();

    }

    @Override
    protected int getActivityBaseViewID() {
        return R.layout.activity_movie_detail;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMovieID = (int) getIntent().getSerializableExtra(EXTRA_MOVIE_ID);
        mPresenter.getMovieDetails(mMovieID);

    }

    @OnClick(R.id.movies_btn_ja_assisti)
    public void onClickJaAssisti() {
        mIsWatchPressed = !mIsWatchPressed;

        if (mIsWatchPressed)
            updateState(R.drawable.ic_assistido, android.R.color.holo_green_dark, "Marcado como Assistido.");
        else
            updateState(R.drawable.ic_assitir_eye, R.color.yellow, "Desmarcado como Assistido.");
    }

    private void updateState(int iconID, int iconColor, String msg) {
        mJaAssistiButton.setImageDrawable(ViewUtils.getDrawableFromResource(this, iconID));
        mJaAssistiButton.setBackgroundTintList(ColorStateList.valueOf(ViewUtils.getColorFromResource(this, iconColor)));

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void setupDirectorsRecyclerView(List<ItemListDTO> directors) {
        mDiretoresRecyclerView.setLayoutManager(new LinearLayoutManager(this, RECYCLER_VIEW_ORIENTATION, false));
        mDiretoresAdapter = new ListWordsAdapter(this, directors, this, ItemType.DIRECTORS, R.layout.item_list_words_default);
        mDiretoresRecyclerView.setAdapter(mDiretoresAdapter);
    }

    public void updateUI(MovieDetails movie) {
        isStarted = true;
        this.mMovie = movie;
        mJaAssistiButton.show();

        if (!isDestroyed()) {
            mBackgroundMovie.post(new Runnable() {
                @Override
                public void run() {
                    ImageUtils.loadWithRevealAnimation(MovieDetailActivity.this, mMovie.getBackdropPath(), mBackgroundMovie, R.drawable.ic_image_default_back, ImageSize.BACKDROP_780);
                }
            });

            ImageUtils.load(this, mMovie.getPosterPath(), mPosterMovie, mMovie.getTitle(), ImageSize.POSTER_185);

            mTitleMovie.setText(mMovie.getTitle());
            mDuracao.setText(mMovie.getRuntime() != 0 ? getResources().getString(R.string.movie_duracao, mMovie.getRuntime()) : "--");
            mAnoLancamento.setText(String.valueOf(mMovie.getYearRelease()));
        }
    }

    public void setupTabs() {

        if (!isDestroyed()) {
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.movie_details_fragment);

            if (fragment == null) {
                fm.beginTransaction()
                        .add(R.id.movie_details_fragment, MovieDetailsFragment.newInstance(mMovie))
                        .commit();
            }
        }

        mAppBarLayout.addOnOffsetChangedListener(onOffsetChangedListener());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(MOVIE, mMovie);
        outState.putBoolean(START, isStarted);
    }

    private AppBarMovieListener onOffsetChangedListener() {
        return new AppBarMovieListener() {

            @Override
            public void onExpanded(AppBarLayout appBarLayout) {
                mToolbar.setBackground(ViewUtils.getDrawableFromResource(getApplicationContext(), R.drawable.background_action_bar_transparent));
                mToolbar.setTitle("");
                mJaAssistiButton.show();
            }

            @Override
            public void onCollapsed(AppBarLayout appBarLayout) {
                mToolbar.setBackgroundColor(ViewUtils.getColorFromResource(getApplicationContext(), R.color.colorPrimary));
                mToolbar.setTitle(mMovie.getTitle());
                mJaAssistiButton.hide();
            }

            @Override
            public void onIdle(AppBarLayout appBarLayout) {
                mToolbar.setBackground(ViewUtils.getDrawableFromResource(getApplicationContext(), R.drawable.background_action_bar_transparent));
            }
        };
    }

    @OnClick({R.id.play_image_movie_principal, R.id.background_movie})
    public void onClickBackgroundMovie() {
        try {
            if (isInternetConnected() && !mMovie.getVideos().isEmpty())
                inflateVideoPlayer(mMovie.getVideos().get(0).getKey());
        } catch (Exception e) {

        }

    }

    public void updateVideos(VideosResponse videos) {
        mMovie.setVideos(videos);
    }


    @Override
    public void onClickVideo(Video video) {
        inflateVideoPlayer(video.getKey());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case REQ_START_STANDALONE_PLAYER:
                YouTubeInitializationResult errorReason =
                        YouTubeStandalonePlayer.getReturnedInitializationResult(data);
                if (errorReason.isUserRecoverableError())
                    errorReason.getErrorDialog(this, 0).show();
                break;
        }

    }

    private void inflateVideoPlayer(String videoKey) {

        int startTimeMillis = 0;
        boolean autoplay = true;
        boolean lightboxMode = false;

        Intent intent = YouTubeStandalonePlayer.createVideoIntent(
                this, BuildConfig.GOOGLE_KEY, videoKey, startTimeMillis, autoplay, lightboxMode);

        if (intent != null) {
            if (isAuthResolveIntent(intent)) {
                startActivityForResult(intent, REQ_START_STANDALONE_PLAYER);
            } else {
                YouTubeInitializationResult.SERVICE_MISSING
                        .getErrorDialog(this, REQ_RESOLVE_SERVICE_MISSING).show();
            }
        }
    }

    private boolean isAuthResolveIntent(Intent intent) {
        List<ResolveInfo> resolveInfo = this.getPackageManager().queryIntentActivities(intent, 0);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }

    @Override
    public void onMovieSelected(int movieID, ImageView posterMovie) {
        startActivity(MovieDetailActivity.newIntent(this, movieID));
    }

    @Override
    public void onClickPerson(int castID) {
        startActivity(PersonDetailActivity.newIntent(this, castID));
    }

    @Override
    public void onItemSelected(ItemListDTO item, ItemType itemType) {

        switch (itemType) {
            case GENRE:
                startActivity(ListsDefaultActivity.newIntent(this, new ListActivityDTO(item.getItemID(), item.getNameItem(), Sort.GENEROS, R.layout.item_list_movies, ListType.MOVIES), new HashMap<String, String>()));
                break;
            case KEYWORD:
                startActivity(ListsDefaultActivity.newIntent(this, new ListActivityDTO(item.getItemID(), getString(R.string.keyword_name), item.getNameItem(), Sort.KEYWORDS, R.layout.item_list_movies, ListType.MOVIES), new HashMap<String, String>()));
                break;
            case DIRECTORS:
                onClickPerson(item.getItemID());
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_default, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_share:

                return true;
            default:
                return false;
        }
    }

    @OnClick(R.id.poster_movie)
    public void onClickPosterMovie() {
        if (!mMovie.getImages().isEmpty())
            onClickImage(getImageDTO(mMovie.getImages().size()), new ImageDTO(mMovie.getId(), null, mMovie.getPosterPath()));
    }

    private List<ImageDTO> getImageDTO(int numImages) {
        List<ImageDTO> imageDTOs = new ArrayList<>();

        for (int cont = 0; cont < numImages; cont++) {
            Artwork image = mMovie.getImages().get(cont);
            imageDTOs.add(new ImageDTO(mMovie.getId(), image.getId(), image.getFilePath()));
        }

        return imageDTOs;

    }


    @Override
    public void onClickImage(List<ImageDTO> imagens,  ImageDTO imageDTO) {
        startActivity(WallpapersDetailActivity.newIntent(this, imagens, imageDTO, getString(R.string.wallpapers_title, mMovie.getTitle())));
    }

    @Override
    public void setProgressVisibility(int visibityState) {
        mProgressMovieDetails.setVisibility(visibityState);
    }

    @Override
    public boolean isAdded() {
        return this != null;
    }

    @Override
    public void setPlayButtonVisibility(int visibilityState) {
        playButtonImageView.setVisibility(visibilityState);
    }

    @Override
    public void setTabsVisibility(int visibilityState) {

        if (visibilityState == View.VISIBLE) {
            mContainerTabs.setAnimation(AnimationsUtils.createFadeInAnimation(1000));
            mContainerTabs.setVisibility(visibilityState);
        }

    }

    @Override
    protected View.OnClickListener onSnackbarClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.getMovieDetails(mMovieID);
                mSnackbar.dismiss();
            }
        };
    }

    @Override
    public void onClickReviewLink(String url) {
        startActivityForResult(WebViewActivity.newIntent(this, url, mMovie.getTitle()), 0);
    }

    @Override
    public void onClickReview(String reviewID) {

    }
}