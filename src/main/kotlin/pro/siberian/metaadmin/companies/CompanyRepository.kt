package pro.siberian.metaadmin.companies

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository : PagingAndSortingRepository<Company, Long>