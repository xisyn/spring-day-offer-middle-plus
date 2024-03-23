package com.onedayoffer.taskdistribution.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class EmployeeDTO {
    private String fio;
    private String jobTitle;
    private List<TaskDTO> tasks = new ArrayList<>();

    public EmployeeDTO(String fio, String jobTitle) {
        this.fio = fio;
        this.jobTitle = jobTitle;
        this.tasks = new ArrayList<>();
    }

    public Integer getTotalLeadTime() {
        if (tasks.size() == 0) return 0;
        else return tasks.stream().mapToInt(TaskDTO::getLeadTime).sum();
    }

    public void setTask(TaskDTO task) {
        tasks.add(task);
    }
}
