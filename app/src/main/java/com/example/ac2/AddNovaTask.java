package com.example.ac2;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AddNovaTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNovaTask";

    private TextView prazoData;
    private EditText editarTask;
    private Button salvarButton;
    private FirebaseFirestore firestore;
    private Context context;
    private String prazo = "";
    private String id = "";
    private String prazoDataAtualizado = "";
    private ImageButton sppechBtn;
    private static final int RECOGNIZER_CODE = 1;

    public static AddNovaTask newInstance(){
        return new AddNovaTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_nova_tarefa , container , false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prazoData = view.findViewById(R.id.set_due_tv);
        editarTask = view.findViewById(R.id.task_edittext);
        salvarButton = view.findViewById(R.id.save_btn);

        firestore = FirebaseFirestore.getInstance();

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if (bundle != null){
            isUpdate = true;
            String task = bundle.getString("tarefa");
            id = bundle.getString("id");
            prazoDataAtualizado = bundle.getString("prazo");

            editarTask.setText(task);
            prazoData.setText(prazoDataAtualizado);

            if (task.length() > 0){
                salvarButton.setEnabled(false);
                salvarButton.setBackgroundColor(Color.GRAY);
            }
        }

        editarTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")){
                    salvarButton.setEnabled(false);
                    salvarButton.setBackgroundColor(Color.GRAY);
                }else{
                    salvarButton.setEnabled(true);
                    salvarButton.setBackgroundColor(getResources().getColor(R.color.green_blue));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        prazoData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();

                int MONTH = calendar.get(Calendar.MONTH);
                int YEAR = calendar.get(Calendar.YEAR);
                int DAY = calendar.get(Calendar.DATE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int ano, int mes, int diaMes) {
                        mes = mes + 1;
                        prazoData.setText(diaMes + "/" + mes + "/" + ano);
                        prazo = diaMes + "/" + mes +"/"+ano;

                    }
                } , YEAR , MONTH , DAY);

                datePickerDialog.show();
            }
        });

        boolean finalIsUpdate = isUpdate;
        salvarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String task = editarTask.getText().toString();

                if (finalIsUpdate){
                    firestore.collection("task").document(id).update("tarefa" , task , "prazo" , prazo);
                    Toast.makeText(context, "Tarefa Atualizada", Toast.LENGTH_SHORT).show();
                } else{
                        if (task.isEmpty()) {
                            Toast.makeText(context, "Tarefa vazia não autorizada!!!", Toast.LENGTH_SHORT).show();
                        } else {

                            Map<String, Object> taskMap = new HashMap<>();

                            taskMap.put("tarefa", task);
                            taskMap.put("prazo", prazo);
                            taskMap.put("status", 0);
                            taskMap.put("tempo", FieldValue.serverTimestamp());

                            firestore.collection("task").add(taskMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Tarefa Salva", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                }
                dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECOGNIZER_CODE && resultCode == RESULT_OK) {
            ArrayList<String> taskText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            editarTask.setText(taskText.get(0).toString());
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof  OnDialogCloseListner){
            ((OnDialogCloseListner)activity).onDialogClose(dialog);
        }
    }
}
