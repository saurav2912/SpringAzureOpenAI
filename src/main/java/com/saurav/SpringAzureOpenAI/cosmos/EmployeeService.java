package com.saurav.SpringAzureOpenAI.cosmos;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.JsonNode;
import com.saurav.SpringAzureOpenAI.dao.Employee;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    //private final CosmosTemplate cosmosTemplate;
    private final CosmosContainer container;
    private final CosmosAsyncContainer asyncContainer;
    private final CosmosAsyncContainer leaseContainer;

    public EmployeeService(
            //CosmosTemplate cosmosTemplate,
            CosmosAsyncClient cosmosAsyncClient,
            CosmosClient cosmosClient) {

        //this.cosmosTemplate = cosmosTemplate;

        CosmosDatabase database =
                cosmosClient.getDatabase("AI200CosmosDB");

        this.container =
                database.getContainer("Employee");
        this.asyncContainer =
                cosmosAsyncClient.getDatabase("AI200CosmosDB")
                        .getContainer("Employee");

        this.leaseContainer =
                cosmosAsyncClient.getDatabase("AI200CosmosDB")
                        .getContainer("leases");
    }

    /*@PostConstruct
    public void init() {
        // Create the lease container if it doesn't exist
        ChangeFeedProcessor changeFeedProcessor = new ChangeFeedProcessorBuilder()
                .hostName("my-host")
                .feedContainer(asyncContainer)
                .leaseContainer(leaseContainer)
                .handleChanges(changes -> {
                    for (JsonNode employee : changes) {
                        System.out.println("Change detected: " + employee);
                    }
                })
                .buildChangeFeedProcessor();
        changeFeedProcessor.start().block();
    }*/

    private void processChanges(List<JsonNode> docs) {

        docs.forEach(System.out::println);

    }

    public Employee create(Employee employee) {

        CosmosItemResponse<Employee> response =
                container.createItem(employee);

        System.out.println("CREATE RU = "
                + response.getRequestCharge());

        return employee;
    }

    public Employee read(String id) {

        CosmosItemResponse<Employee> response =
                container.readItem(
                        id,
                        new PartitionKey(id),
                        Employee.class);

        System.out.println("READ RU = "
                + response.getRequestCharge());

        return response.getItem();
    }

    public List<Employee> update(List<Employee> employees) {

        double totalRU = 0;

        for (Employee employee : employees) {

            CosmosItemResponse<Employee> response =
                    container.replaceItem(
                            employee,
                            employee.getId(),
                            new PartitionKey(employee.getId()),
                            new CosmosItemRequestOptions());

            System.out.println(employee.getId()
                    + " -> RU = "
                    + response.getRequestCharge());

            totalRU += response.getRequestCharge();
        }

        System.out.println("Total RU = " + totalRU);

        return employees;
    }

    public void delete(String id) {

        CosmosItemResponse<Object> response =
                container.deleteItem(
                        id,
                        new PartitionKey(id),
                        new CosmosItemRequestOptions());

        System.out.println("DELETE RU = "
                + response.getRequestCharge());
    }

    public List<Employee> createList(List<Employee> employees) {


        double totalRU = 0;

        for (Employee employee : employees) {

            CosmosItemResponse<Employee> response = container.createItem(employee);
            totalRU += response.getRequestCharge();
        }

        System.out.println("Total RU = " + totalRU);
        return employees;
    }

    public List<Employee> readAll() {

        List<Employee> employees = new ArrayList<>();
        double totalRU = 0;

        CosmosPagedIterable<Employee> iterable =
                container.queryItems(
                        "SELECT * FROM c order by c.department",
                        new CosmosQueryRequestOptions(),
                        Employee.class);

        for (FeedResponse<Employee> page : iterable.iterableByPage()) {


            totalRU += page.getRequestCharge();

            for (Employee emp : page.getResults()) {
                employees.add(emp);
            }
        }
        System.out.println("RU = " + totalRU);
        return employees;
    }
}
