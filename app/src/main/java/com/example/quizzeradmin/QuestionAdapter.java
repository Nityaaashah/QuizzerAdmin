package com.example.quizzeradmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.CollationElementIterator;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder>{

     private List<QuestionModel> list;
     private String category;
     private Deletelistener deletelistener;

    public QuestionAdapter(List<QuestionModel> list , String category,Deletelistener listener ) {
        this.list =list;
        this.category = category;
        this.deletelistener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String question = list.get(position).getQuestion();
        String answer = list.get(position).getAnswer();

        holder.setdata(question,answer,position);
    }

    @Override
    public int getItemCount() {
       return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView question, answer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            answer = itemView.findViewById(R.id.answer);
        }

        private void setdata(String question, String answer, final int position) {
            this.question.setText(position + 1 + ". " + question);
            this.answer.setText("Ans. "+ answer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editIntent = new Intent(itemView.getContext(),AddQuestionActivity.class);
                    editIntent.putExtra("categoryname",category);
                    editIntent.putExtra("setid",list.get(position).getSet());
                    editIntent.putExtra("position",position);
                    itemView.getContext().startActivity(editIntent);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                   deletelistener.onlongclick(position,list.get(position).getId());
                    return false;
                }
            });
        }
    }

    public interface Deletelistener
    {
        void onlongclick(int position , String id);
    }

}