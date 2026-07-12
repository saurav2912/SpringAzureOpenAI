package com.saurav.SpringAzureOpenAI.dao;

import com.azure.spring.data.cosmos.repository.CosmosRepository;

public interface EmployeeRepository extends CosmosRepository<Employee, String> {
}
