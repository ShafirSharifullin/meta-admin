package pro.siberian.metaadmin.employees

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface EmployeeRepository : PagingAndSortingRepository<Employee, Long>