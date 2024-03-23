package com.onedayoffer.taskdistribution.services;

import com.onedayoffer.taskdistribution.DTO.EmployeeDTO;
import com.onedayoffer.taskdistribution.DTO.TaskDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskDistributorImpl implements TaskDistributor {

    // Максимальная загрузка каждого 7 часов - 420 минут
    private static int MAX_LIMIT = 420;
    // Минимальная загрузка каждого 6 часов - 360 минут
    private static int MIN_LIMIT = 360;

    @Override
    public void distribute(List<EmployeeDTO> employees, List<TaskDTO> tasks) {
        // Распределяем задачи по приоритетам
        Map<Integer, List<TaskDTO>> priorityTaskMap = tasks.stream().collect(Collectors.groupingBy(TaskDTO::getPriority));

        // Набор приоритетов, по порядку
        List<Integer> priorities = priorityTaskMap.keySet().stream().sorted().toList();

        // Распределяем по часам, в порядке приоритета
        priorities.forEach(priority -> distributeTasks(priority, priorityTaskMap, employees));

    }

    private void distributeTasks(Integer priority, Map<Integer, List<TaskDTO>> priorityTaskMap, List<EmployeeDTO> employees) {
        List<TaskDTO> tasks = priorityTaskMap.get(priority);
        // todo: каждый раз проходим по всем сотрудникам, нужно сразу отсеивать тех у кого уже полная загрузка
        employees.forEach(employee -> setTasks(employee, tasks));
    }

    // todo: при распределении задач проходим по всему списку задач и удаляем подходящие из общего списка
    //  1) оптимальней за один проход находить все подходящие задачи и высчитывать сумму времени сразу
    //  2) не опираться на общий список и не редактировать его, т.к. могут вознокнуть проблемы для дальнейших оптимизаций, распараллеливании и т.д.
    private void setTasks(EmployeeDTO employee, List<TaskDTO> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        // Ищем задачи при недостаточной загрузке
        if (employee.getTotalLeadTime() < MIN_LIMIT) {
            // Получаем максимум по загрузке
            int maxSize = MAX_LIMIT - employee.getTotalLeadTime();

            // Список добавленных задач
            List<TaskDTO> addedTasks = new ArrayList<>();

            // Находим подходящие задачи
            tasks.stream().filter(task -> task.getLeadTime() <= maxSize).forEach(
                    task -> {
                        // Если добавили задачу сотруднику, сохраняем для очистки общего списка
                        if (setTask(employee, task)) {
                            addedTasks.add(task);
                        }
                    }
            );

            // Очищаем распределенные задачи
            tasks.removeAll(addedTasks);
        }
    }

    private boolean setTask(EmployeeDTO employee, TaskDTO task) {
        // todo: неоптимально высчитывается время в модели, каждый раз нужно проходить по списку
        Integer totalLeadTime = employee.getTotalLeadTime();
        // Проверям, что задача подходит по времени
        if (totalLeadTime <= MAX_LIMIT && totalLeadTime + task.getLeadTime() <= MAX_LIMIT) {
            employee.setTask(task);
            return true;
        }

        return false;
    }
}
