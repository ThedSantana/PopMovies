package br.com.tiagohs.popmovies.view.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import br.com.tiagohs.popmovies.R;
import br.com.tiagohs.popmovies.model.dto.ListActivityDTO;
import br.com.tiagohs.popmovies.model.dto.MovieListDTO;
import br.com.tiagohs.popmovies.model.movie.MovieDetails;
import br.com.tiagohs.popmovies.model.response.RankingResponse;
import br.com.tiagohs.popmovies.presenter.MovieDetailsOverviewPresenter;
import br.com.tiagohs.popmovies.util.AnimationsUtils;
import br.com.tiagohs.popmovies.util.DTOUtils;
import br.com.tiagohs.popmovies.util.LocaleUtils;
import br.com.tiagohs.popmovies.util.MovieUtils;
import br.com.tiagohs.popmovies.util.ViewUtils;
import br.com.tiagohs.popmovies.util.enumerations.ItemType;
import br.com.tiagohs.popmovies.util.enumerations.ListType;
import br.com.tiagohs.popmovies.util.enumerations.Sort;
import br.com.tiagohs.popmovies.view.MoviesDetailsOverviewView;
import br.com.tiagohs.popmovies.view.activity.ListsDefaultActivity;
import br.com.tiagohs.popmovies.view.activity.WebViewActivity;
import br.com.tiagohs.popmovies.view.adapters.ListMoviesAdapter;
import br.com.tiagohs.popmovies.view.adapters.ListWordsAdapter;
import br.com.tiagohs.popmovies.view.callbacks.ListMoviesCallbacks;
import br.com.tiagohs.popmovies.view.callbacks.ListWordsCallbacks;
import br.com.tiagohs.popmovies.view.callbacks.PersonCallbacks;
import butterknife.BindView;
import butterknife.OnClick;

public class MovieDetailsOverviewFragment extends BaseFragment implements MoviesDetailsOverviewView {
    private static final String TAG = MovieDetailsOverviewFragment.class.getSimpleName();

    private static final String ARG_MOVIE = "movie";

    @BindView(R.id.sinopse_movie)                           TextView mSinopseMovie;
    @BindView(R.id.adult_movie)                             TextView mAdultMovie;
    @BindView(R.id.generos_recycler_view)                   RecyclerView mGenerosRecyclerView;
    @BindView(R.id.similares_recycler_view)                 RecyclerView mSimilaresRecyclerView;
    @BindView(R.id.imdb_raking_progress)                    ProgressBar mImdbRankingProgress;
    @BindView(R.id.imdb_raking)                             TextView mImdbRanking;
    @BindView(R.id.imdb_num_votos)                          TextView mImdbVotes;
    @BindView(R.id.tomatoes_ranking)                        TextView mTomatoesRanking;
    @BindView(R.id.tomatoes_ranking_progress)               ProgressBar mTomatoesRankingProgress;
    @BindView(R.id.tomatoes_num_votos)                      TextView mTomatoesVotes;
    @BindView(R.id.metascore_ranking)                       TextView mMetascoreRanking;
    @BindView(R.id.metascore_ranking_progress)              ProgressBar mMetascoreRankingProgress;
    @BindView(R.id.aspas)                                   TextView mAspas;
    @BindView(R.id.movie_tomatoes_consensus)                TextView mTomatoesConsensus;
    @BindView(R.id.movie_nomeacoes)                         TextView mMovieNomeacoes;
    @BindView(R.id.imdb_riple)                              MaterialRippleLayout mImdbRiple;
    @BindView(R.id.tomatoes_riple)                          MaterialRippleLayout mTomatoesRiple;
    @BindView(R.id.metascore_riple)                         MaterialRippleLayout mMetascoreRiple;
    @BindView(R.id.movie_details_titulo_original)           TextView mTituloOriginal;
    @BindView(R.id.movie_details_idioma_original)           TextView mIdiomaOriginal;
    @BindView(R.id.movie_details_orcamento_original)        TextView mOcamento;
    @BindView(R.id.movie_details_receita_original)          TextView mReceita;
    @BindView(R.id.keywords_recycler_view)                  RecyclerView mKeywordsRecyclerView;
    @BindView(R.id.label_movie_details_idioma_original)     TextView mLabelIdiomaOriginal;
    @BindView(R.id.label_movie_details_titulo_original)     TextView mLabelTituloOriginal;
    @BindView(R.id.label_movie_details_receita_original)    TextView mLabelOcamento;
    @BindView(R.id.label_movie_details_orcamento_original)  TextView mLabelReceita;
    @BindView(R.id.label_movie_nomeacoes)                   TextView mLabelMovieNomeacoes;
    @BindView(R.id.tomatoes_consensus_assign)               TextView mTomatoesConsensusAssign;
    @BindView(R.id.imdb_raking_container)                   FrameLayout mImdbRakingContainer;
    @BindView(R.id.tomatoes_ranking_container)              FrameLayout mTomatoesRankingContainer;
    @BindView(R.id.metascore_ranking_container)             FrameLayout mMetascoreRankingContainer;
    @BindView(R.id.tomatoes_consensus_container)            RelativeLayout mTomatoesConsensusContainer;
    @BindView(R.id.rankings_progress)                       ProgressWheel mRankingProgress;
    @BindView(R.id.rankings_container)                      LinearLayout mRankingContainer;
    @BindView(R.id.similares_container)                     CardView mSimilaresTitleContainer;

    @Inject
    MovieDetailsOverviewPresenter mPresenter;

    private MovieDetails mMovie;
    private RankingResponse mMovieRankings;

    private ListWordsAdapter mGenerosAdapter;

    private ListMoviesCallbacks mListMoviesCallbacks;
    private ListWordsCallbacks mGenresCallbacks;
    private PersonCallbacks mPersonCallbacks;
    private ListWordsCallbacks mKeyWordsCallbacks;

    public static MovieDetailsOverviewFragment newInstance(MovieDetails movie) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_MOVIE, movie);

        MovieDetailsOverviewFragment movieDetailsFragment = new MovieDetailsOverviewFragment();
        movieDetailsFragment.setArguments(bundle);

        return movieDetailsFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListMoviesCallbacks = (ListMoviesCallbacks) context;
        mGenresCallbacks = (ListWordsCallbacks) context;
        mPersonCallbacks = (PersonCallbacks) context;
        mKeyWordsCallbacks = (ListWordsCallbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListMoviesCallbacks = null;
        mGenresCallbacks = null;
        mPersonCallbacks = null;
        mKeyWordsCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationComponent().inject(this);

        mPresenter.setView(this);
        mMovie = (MovieDetails) getArguments().getSerializable(ARG_MOVIE);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mMovie.getSimilarMovies().isEmpty())
            setSimilaresVisibility(View.GONE);

        mPresenter.getMoviesRankings(mMovie.getImdbID());

        addFragment(R.id.container_elenco, ListPersonsDefaultFragment.newInstance(DTOUtils.createCastPersonListDTO(mMovie.getCast()), ListPersonsDefaultFragment.createLinearListArguments(RecyclerView.HORIZONTAL, false)));
        addFragment(R.id.container_equipe_tecnica, ListPersonsDefaultFragment.newInstance(DTOUtils.createCrewPersonListDTO(mMovie.getCrew()), ListPersonsDefaultFragment.createLinearListArguments(RecyclerView.HORIZONTAL, false)));
    }

    private void addFragment(int id, Fragment fragment) {
        FragmentManager fm = getChildFragmentManager();
        Fragment f = fm.findFragmentById(id);

        if (f == null) {
            fm.beginTransaction()
                    .add(id, fragment)
                    .commit();
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdultMovie.setVisibility(View.INVISIBLE);

        configuraRecyclersViews();
        updateUI();
    }

    public void setMovie(MovieDetails movie) {
        mMovie = movie;
    }

    public void setMovieRankings(RankingResponse movieRankings) {
        mMovieRankings = movieRankings;
    }

    private void configuraRecyclersViews() {
        List<MovieListDTO> movieListDTO = new ArrayList<>();

        for (MovieDetails movie : mMovie.getSimilarMovies())
            movieListDTO.add(new MovieListDTO(movie.getId(), movie.getTitle(), movie.getPosterPath(), movie.getVoteAverage()));

        mGenerosRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.HORIZONTAL, false));

        mSimilaresRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.HORIZONTAL, false));
        mSimilaresRecyclerView.setAdapter(new ListMoviesAdapter(getActivity(), movieListDTO, mListMoviesCallbacks, R.layout.item_similares_movie));
    }

    public void updateIMDB(String ranking, String votes) {
        ViewUtils.createRatingGadget(getActivity(), Float.parseFloat(ranking), mImdbRankingProgress, 10);
        mImdbRanking.setText(String.format("%.1f", Float.parseFloat(ranking)));
        mImdbVotes.setText(getString(R.string.imdb_votes, votes));
    }

    public void updateTomatoes(String ranking, String votes) {
        ViewUtils.createRatingGadget(getActivity(), Float.parseFloat(ranking), mTomatoesRankingProgress, 10);
        mTomatoesRanking.setText(String.format("%.1f", Float.parseFloat(ranking)));
        mTomatoesVotes.setText(getString(R.string.tomatoes_num_reviews, votes));
    }

    public void updateMetascore(String ranking) {
        ViewUtils.createRatingGadget(getActivity(), Float.parseFloat(ranking), mMetascoreRankingProgress, 100);
        mMetascoreRanking.setText(ranking);
    }

    public void updateTomatoesConsensus(String tomatoConsensus) {
        mAspas.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "TimesNewRoman.ttf"));
        mTomatoesConsensus.setText(tomatoConsensus);
        mTomatoesConsensus.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));
    }

    public void updateNomeacoes(String nomeacoes) {
        mMovieNomeacoes.setText(nomeacoes != null ? nomeacoes : getString(R.string.nao_disponivel));
        mMovieNomeacoes.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));
        mLabelMovieNomeacoes.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));
    }

    private void updateUI() {

        mSinopseMovie.setText(mMovie.getOverview() != null ? mMovie.getOverview() : getResources().getString(R.string.nao_ha_sinopse, LocaleUtils.getLocaleLanguageName()));
        mSinopseMovie.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));

        mAdultMovie.setVisibility(mMovie.isAdult() ? View.VISIBLE : View.GONE);
        mAdultMovie.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));

        mTituloOriginal.setText(mMovie.getOriginalTitle());
        mTituloOriginal.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));
        mLabelTituloOriginal.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));

        mIdiomaOriginal.setText(MovieUtils.formatIdioma(getActivity(), mMovie.getOriginalLanguage()));
        mIdiomaOriginal.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));
        mLabelIdiomaOriginal.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));

        mOcamento.setText(mMovie.getBudget() != 0 ? MovieUtils.formatCurrency(mMovie.getBudget()) : "--");
        mOcamento.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));
        mLabelOcamento.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));

        mReceita.setText(mMovie.getRevenue() != 0 ? MovieUtils.formatCurrency(mMovie.getRevenue()) : "--");
        mReceita.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));
        mLabelReceita.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "opensans.ttf"));

        mKeywordsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.HORIZONTAL, false));
        mKeywordsRecyclerView.setAdapter(new ListWordsAdapter(getActivity(), DTOUtils.createKeywordsItemsListDTO(mMovie.getKeywords()), mKeyWordsCallbacks, ItemType.KEYWORD, R.layout.item_list_words_default));

        mGenerosAdapter = new ListWordsAdapter(getActivity(), DTOUtils.createGenresItemsListDTO(mMovie.getGenres()), mGenresCallbacks, ItemType.GENRE, R.layout.item_list_words_default);
        mGenerosRecyclerView.setAdapter(mGenerosAdapter);
    }

    @OnClick(R.id.tomatoes_riple)
    public void onTomatoesClick() {
        if (mMovieRankings.getTomatoURL() != null)
            startActivityForResult(WebViewActivity.newIntent(getActivity(), mMovieRankings.getTomatoURL(), mMovie.getTitle()), 0);
    }

    @OnClick(R.id.imdb_riple)
    public void onIMDBClick() {
        if (mMovieRankings.getImdbID() != null)
            startActivityForResult(WebViewActivity.newIntent(getActivity(), getString(R.string.metacritic_link, mMovie.getImdbID()), mMovie.getTitle()), 0);
    }

    @OnClick(R.id.metascore_riple)
    public void onMetascoreClick() {
        if (mMovieRankings.getMetascoreRating() != null)
            startActivityForResult(WebViewActivity.newIntent(getActivity(), getString(R.string.metacritic_link, mMovie.getOriginalTitle()), mMovie.getTitle()), 0);
    }

    @OnClick(R.id.elenco_riple)
    public void onClickElencoTitle() {
        if (!mMovie.getCast().isEmpty())
            startActivity(ListsDefaultActivity.newIntent(getActivity(), new ListActivityDTO(getString(R.string.elenco_title_activity), mMovie.getTitle(), R.layout.item_list_movies, ListType.PERSON), DTOUtils.createCastPersonListDTO(mMovie.getCast())));
    }

    @OnClick(R.id.equipe_tecnica_riple)
    public void onClickEquipeTitle() {
        if (!mMovie.getCrew().isEmpty())
            startActivity(ListsDefaultActivity.newIntent(getActivity(), new ListActivityDTO(getString(R.string.equipe_tecnica_title), mMovie.getTitle(), R.layout.item_list_movies, ListType.PERSON), DTOUtils.createCrewPersonListDTO(mMovie.getCrew())));
    }

    @OnClick(R.id.similares_riple)
    public void onClickSimilaresTitle() {
        if (!mMovie.getSimilarMovies().isEmpty())
            startActivity(ListsDefaultActivity.newIntent(getActivity(), new ListActivityDTO(mMovie.getId(), getString(R.string.similares_title_activity), mMovie.getTitle(), Sort.SIMILARS, R.layout.item_list_movies, ListType.MOVIES)));
    }

    public void setImdbRakingContainerVisibility(int visibilityState) {
        mImdbRakingContainer.setVisibility(visibilityState);
        mImdbRiple.setVisibility(visibilityState);
    }

    public void setTomatoesRakingContainerVisibility(int visibilityState) {
        mTomatoesRankingContainer.setVisibility(visibilityState);
        mTomatoesRiple.setVisibility(visibilityState);
    }

    public void setMetascoreRakingContainerVisibility(int visibilityState) {
        mMetascoreRankingContainer.setVisibility(visibilityState);
        mMetascoreRiple.setVisibility(visibilityState);
    }

    public void setTomatoesConsensusContainerVisibility(int visibilityState) {
        mTomatoesConsensusContainer.setVisibility(visibilityState);
        mTomatoesConsensusAssign.setVisibility(visibilityState);
    }

    public void setSimilaresVisibility(int visibilityState) {
        mSimilaresTitleContainer.setVisibility(visibilityState);
    }

    @Override
    protected int getViewID() {
        return R.layout.fragment_movie_detail_overview;
    }

    @Override
    protected View.OnClickListener onSnackbarClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStart();
                mSnackbar.dismiss();
            }
        };
    }

    public void setRankingProgressVisibility(int visibility) {
        mRankingProgress.setVisibility(visibility);
    }

    public void setRankingContainerVisibility(int visibility) {

        if (visibility == View.VISIBLE) {
            mRankingContainer.setAnimation(AnimationsUtils.createFadeInAnimation(1000));
            mRankingContainer.setVisibility(visibility);
        }

    }

    @Override
    public void setProgressVisibility(int visibityState) {

    }

}