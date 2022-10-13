package pro.siberian.metaadmin

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.core.mapping.BasicJdbcPersistentProperty
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.support.Repositories
import org.springframework.stereotype.Service

@Service
class MetaAdminService(listableBeanFactory: ListableBeanFactory) {

    private final var reposInfo: Repositories = Repositories(listableBeanFactory)
    private final val domains = reposInfo.toSet()
    private val repos: List<Repository> =
        domains.map { domain ->
            Repository(
                reposInfo.getRepositoryInformationFor(domain).get().repositoryInterface.simpleName,
//                Получение репозитория
                reposInfo.getRepositoryFor(domain).get() as PagingAndSortingRepository<Any, Any>,
//                Сущность, которая связана с репозиторием
                domain,
//                Список полей сущности и их имен
                reposInfo.getPersistentEntity(domain).map { DomainField(it as BasicJdbcPersistentProperty, it.name) }
            )
        }

    fun getItemById(repoCode: String, id: Int, page: Int, size: Int): Any? =
        findRepoByRepoCode(repoCode)?.getItemById(id, page, size)

    fun getRepoCodes() = repos.map { it.repoCode }

    fun getAllDataFromRepo(repoCode: String, pageable: Pageable): Page<Any>? =
        findRepoByRepoCode(repoCode)?.getAllItems(pageable)

    fun delete(repoCode: String, item: Any) = findRepoByRepoCode(repoCode)?.delete(item)

    fun save(repoCode: String, item: Any) = findRepoByRepoCode(repoCode)?.save(item)

    fun getNamesDomainFields(repoCode: String): List<String>? =
        findRepoByRepoCode(repoCode)?.getNamesDomainFields()

    private fun findRepoByRepoCode(repoCode: String): Repository? = repos.find { it.repoCode == repoCode }
}