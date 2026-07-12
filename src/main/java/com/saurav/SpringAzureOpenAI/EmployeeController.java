package com.saurav.SpringAzureOpenAI;

import com.saurav.SpringAzureOpenAI.dao.Employee;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PostMapping
    public Employee create(@RequestBody Employee employee) {
        return service.create(employee);
    }

    @PostMapping("/list")
    public List<Employee> createList(@RequestBody List<Employee> employees) {
        return service.createList(employees);
    }

    @GetMapping("/{id}")
    public Employee get(@PathVariable String id) {
        return service.read(id);
    }

    @GetMapping("/all")
    public List<Employee> getAll() {
        return service.readAll();
    }

    @PutMapping
    public List<Employee> update(@RequestBody List<Employee> employees) {
        return service.update(employees);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
