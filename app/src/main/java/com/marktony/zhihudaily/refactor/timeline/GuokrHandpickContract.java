package com.marktony.zhihudaily.refactor.timeline;

import android.support.annotation.NonNull;

import com.marktony.zhihudaily.BasePresenter;
import com.marktony.zhihudaily.BaseView;
import com.marktony.zhihudaily.refactor.data.GuokrHandpickNews;

import java.util.List;

/**
 * Created by lizhaotailang on 2017/5/24.
 */

public interface GuokrHandpickContract {

    interface View extends BaseView<Presenter> {

        boolean isActive();

        void setLoadingIndicator(boolean active);

        void showResult(@NonNull List<GuokrHandpickNews.Result> list);

    }

    interface Presenter extends BasePresenter {

        void load(boolean forceUpdate, int offset, int limit);

        void outdate(int id);

    }

}
