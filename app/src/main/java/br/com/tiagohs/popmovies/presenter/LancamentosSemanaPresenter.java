package br.com.tiagohs.popmovies.presenter;

import java.util.Locale;

import br.com.tiagohs.popmovies.view.LancamentosSemanaView;

public interface LancamentosSemanaPresenter extends BasePresenter<LancamentosSemanaView> {

    void initUpdateMovies(Locale locale);
    void onClickNext(Locale locale);
    void onClickAnterior(Locale locale);
}