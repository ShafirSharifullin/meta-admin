package pro.siberian.metaadmin.employees

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

// пакет employees создан для примера
@Table("employees")
data class Employee(

    @Id
    val id: Long = 0,

    val name: String = "",

    val status: String = "",

    val salary: Int = 0,

    @Column("year_birth")
    val yearBirth: Int = 0,

    @JsonIgnore
    @Column("company_id")
    val companyId: Long = 0,
)