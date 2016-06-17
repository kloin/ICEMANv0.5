package com.teamunemployment.breadcrumbs;

/**
 * Base interface for our MVVM views.
 */
public interface BaseView<T> {

    void setPresenter(T presenter);
}
