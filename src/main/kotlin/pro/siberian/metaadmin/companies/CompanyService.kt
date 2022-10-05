package pro.siberian.metaadmin.companies

import org.springframework.stereotype.Service

@Service
class CompanyService(private val companyRepo: CompanyRepository) {

    fun findAll() = companyRepo.findAll().toList()

    fun findById(id: Long): Company? = companyRepo.findById(id).orElse(null)

    fun save(company: Company) = companyRepo.save(company)
}