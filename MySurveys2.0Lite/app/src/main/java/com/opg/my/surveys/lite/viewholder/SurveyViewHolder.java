package com.opg.my.surveys.lite.viewholder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.opg.my.surveys.lite.Fragments.SurveyByListFragment;
import com.opg.my.surveys.lite.R;
import com.opg.my.surveys.lite.common.Util;

/**
 * Created by Dinesh-opg on 3/1/2017.
 */

public class SurveyViewHolder extends RecyclerView.ViewHolder {
    TextView tvCounter;
    TextView surveyTitle;
    ImageView surveyDetail;
    TextView surveyStatus;
    View itemView;
    ProgressBar progressBar;

    public TextView getTvCounter() {
        return tvCounter;
    }

    public void setTvCounter(TextView tvCounter) {
        this.tvCounter = tvCounter;
    }

    public View getItemView() {
        return itemView;
    }

    public void setItemView(View itemView) {
        this.itemView = itemView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public SurveyViewHolder(View itemView,Activity context) {
        super(itemView);
        this.itemView = itemView;
        surveyTitle = (TextView) itemView.findViewById(R.id.survey_title_tv);
        surveyDetail = (ImageView) itemView.findViewById(R.id.iv_sur_det);
        surveyStatus = (TextView) itemView.findViewById(R.id.survey_status_tv);
        progressBar = (ProgressBar) itemView.findViewById(R.id.survey_progress_bar);
        tvCounter = (TextView)itemView.findViewById(R.id.tv_counter);
        Util.setTypeface(context, surveyTitle, "font/roboto_regular.ttf");
        Util.setTypeface(context, surveyStatus, "font/roboto_regular.ttf");
    }


    public TextView getSurveyTitle() {
        return surveyTitle;
    }

    public void setSurveyTitle(TextView surveyTitle) {
        this.surveyTitle = surveyTitle;
    }

    public ImageView getSurveyDetail() {
        return surveyDetail;
    }

    public void setSurveyDetail(ImageView surveyDetail) {
        this.surveyDetail = surveyDetail;
    }

    public TextView getSurveyStatus() {
        return surveyStatus;
    }

    public void setSurveyStatus(TextView surveyStatus) {
        this.surveyStatus = surveyStatus;
    }
}
