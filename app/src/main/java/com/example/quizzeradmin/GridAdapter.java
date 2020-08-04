package com.example.quizzeradmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class GridAdapter extends BaseAdapter {

    public List<String> sets;
    private String category;
    private Gridlistener gridlistener;
    public GridAdapter(List<String> sets , String category ,Gridlistener gridlistener)
    {
        this.sets= sets;
        this.category = category;
        this.gridlistener = gridlistener;
    }
    @Override
    public int getCount() {
        return sets.size() + 1;
    }
    @Override
    public Object getItem(int position) {
        return null;
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;
        if(convertView == null)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item,parent,false);
        }
        else{
            view = convertView;
        }
        if(position == 0 )
        {
            ((TextView)view.findViewById(R.id.digits)).setText("+");
        }
        else {
            ((TextView)view.findViewById(R.id.digits)).setText(String.valueOf(position));
        }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(position==0)
                    {
                        gridlistener.addset();
                    }
                    else {
                    Intent questionintent = new Intent(parent.getContext(),QuestionActivity.class);
                    questionintent.putExtra("category",category);
                    questionintent.putExtra("setid", sets.get(position -1));
                    parent.getContext().startActivity(questionintent);
                    }
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(position!=0) {
                        gridlistener.onlongclick(sets.get(position-1),position);
                    }
                    return false;
                }
            });
        return view;
    }

    public interface Gridlistener{
        public void addset();

        void onlongclick(String setid,int position);
    }
}
