package com.dujiajun.faceattendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ResultFragment extends Fragment {

    private List<Student> students = new ArrayList<>();
    private ResultAdapter adapter = new ResultAdapter();
    private AttendManager manager;
    private String courseId;
    private String courseTime;
    private boolean attendMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recylerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        manager = AttendManager.getInstance(getActivity());
        return view;
    }

    public void setAttendMode(boolean attend) {
        this.attendMode = attend;
    }

    public void setStudents(List<Student> students) {
        if (students != null)
            this.students = students;
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    public void setCourseIdAndTime(String courseId, String courseTime) {
        this.courseId = courseId;
        this.courseTime = courseTime;
    }

    class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.StudentViewHolder> {

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
            StudentViewHolder viewHolder = new StudentViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            holder.tvId.setText(students.get(position).studentId);
            holder.tvName.setText(students.get(position).studentName);
        }

        @Override
        public int getItemCount() {
            return students.size();
        }

        class StudentViewHolder extends RecyclerView.ViewHolder {
            TextView tvId;
            TextView tvName;
            Button btnAlter;

            public StudentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvId = itemView.findViewById(R.id.tv_stu_id);
                tvName = itemView.findViewById(R.id.tv_stu_name);
                btnAlter = itemView.findViewById(R.id.btn_alter);
                if (attendMode) {
                    btnAlter.setVisibility(View.GONE);
                } else {
                    btnAlter.setOnClickListener(v -> manager.editAttend(courseId, courseTime, students.get(getAdapterPosition()).studentId,
                            new AttendManager.CallBack() {
                                @Override
                                public void onSuccess() {
                                    manager.getAttendStudents().add(students.get(getAdapterPosition()));
                                    ((ResultActivity) getActivity()).syncStudentList();
                                }

                                @Override
                                public void onFailed() {
                                    Toast.makeText(getActivity(), "修改出勤失败", Toast.LENGTH_SHORT).show();
                                }
                            }));
                }
            }
        }
    }
}
