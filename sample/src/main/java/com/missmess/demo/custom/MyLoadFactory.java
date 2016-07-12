package com.missmess.demo.custom;

import android.view.View;
import android.widget.RadioButton;

import com.missmess.demo.R;
import com.missmess.swipeloadview.ILoadViewFactory;

/**
 * @author wl
 * @since 2016/07/08 18:38
 */
public class MyLoadFactory implements ILoadViewFactory {
    @Override
    public ILoadMoreView madeLoadMoreView() {
        return new MyLoadMoreView();
    }

    class MyLoadMoreView implements ILoadMoreView {

        private RadioButton normal;
        private RadioButton loading;
        private RadioButton nodata;
        private RadioButton error;

        @Override
        public void init(FootViewAdder footViewHolder, View.OnClickListener onClickLoadMoreListener) {
            View view = footViewHolder.addFootView(R.layout.view_custom_load);
            normal = (RadioButton) view.findViewById(R.id.radioButton1);
            loading = (RadioButton) view.findViewById(R.id.radioButton2);
            nodata = (RadioButton) view.findViewById(R.id.radioButton3);
            error = (RadioButton) view.findViewById(R.id.radioButton4);
        }

        @Override
        public void showNormal() {
            normal.setChecked(true);
        }

        @Override
        public void showNomore() {
            nodata.setChecked(true);
        }

        @Override
        public void showLoading() {
            loading.setChecked(true);
        }

        @Override
        public void showFail(Exception e) {
            error.setText(e.getMessage());
            error.setChecked(true);
        }
    }
}