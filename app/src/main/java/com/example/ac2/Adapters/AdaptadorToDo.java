package com.example.ac2.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ac2.AddNovaTask;
import com.example.ac2.MainActivity;
import com.example.ac2.Models.ModeloToDo;
import com.example.ac2.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdaptadorToDo extends RecyclerView.Adapter<AdaptadorToDo.MyViewHolder> {

    private List<ModeloToDo> todoList;
    private MainActivity activity;
    private FirebaseFirestore firestore;

    public AdaptadorToDo(MainActivity mainActivity , List<ModeloToDo> todoList){
        this.todoList = todoList;
        activity = mainActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.cada_task , parent , false);
        firestore = FirebaseFirestore.getInstance();

        return new MyViewHolder(view);
    }

    public void deleteTask(int position){
        ModeloToDo toDoModel = todoList.get(position);
        firestore.collection("task").document(toDoModel.TaskId).delete();
        todoList.remove(position);
        notifyItemRemoved(position);
    }
    public Context getContext(){
        return activity;
    }
    public void editTask(int position){
        ModeloToDo toDoModel = todoList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("tarefa" , toDoModel.getTarefa());
        bundle.putString("prazo" , toDoModel.getPrazo());
        bundle.putString("id" , toDoModel.TaskId);

        AddNovaTask addNovaTask = new AddNovaTask();
        addNovaTask.setArguments(bundle);
        addNovaTask.show(activity.getSupportFragmentManager() , addNovaTask.getTag());
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ModeloToDo toDoModel = todoList.get(position);
        holder.mCheckBox.setText(toDoModel.getTarefa());

        holder.mDueDateTv.setText("Prazo " + toDoModel.getPrazo());

        holder.mCheckBox.setChecked(toBoolean(toDoModel.getStatus()));

        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    firestore.collection("task").document(toDoModel.TaskId).update("status" , 1);
                }else{
                    firestore.collection("task").document(toDoModel.TaskId).update("status" , 0);
                }
            }
        });

    }

    private boolean toBoolean(int status){
        return status != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView mDueDateTv;
        CheckBox mCheckBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            mDueDateTv = itemView.findViewById(R.id.due_date_tv);
            mCheckBox = itemView.findViewById(R.id.mcheckbox);

        }
    }
}
