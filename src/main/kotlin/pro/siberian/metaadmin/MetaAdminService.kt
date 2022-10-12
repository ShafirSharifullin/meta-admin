package pro.siberian.metaadmin

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.core.mapping.BasicJdbcPersistentProperty
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.support.Repositories
import org.springframework.stereotype.Service

@Service
class MetaAdminService(listableBeanFactory: ListableBeanFactory) {

    private final var reposInfo: Repositories = Repositories(listableBeanFactory)
    private final val domains = reposInfo.toSet()
    private val repos: List<Repo> =
        domains.map { domain ->
            Repo(
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

    private fun findRepoByRepoCode(repoCode: String): Repo? = repos.find { it.repoCode == repoCode }
}

data class DomainField(
    val field: BasicJdbcPersistentProperty,
    val nameField: String,
)

class Repo(
    val repoCode: String,
    private val repository: PagingAndSortingRepository<Any, Any>,
    private val domain: Class<*>,
    private val domainFields: List<DomainField>,
) {

    //    Метод возвращает список значений сущностей из репозитория
    fun getAllItems(pageable: Pageable): Page<Any> {
        val items = repository.findAll(pageable).toList()
        val totalSize = repository.count()
        return PageImpl(items, pageable, totalSize)
    }

    // Метод возвращает элемент репозитория. Для этого в него передается страница(page, size), в которой он находился и
    // идентификатор. В самом методе мы находим все элементы страницы и ищем нужный элемент.
    fun getItemById(id: Int, page: Int, size: Int): Any? =
        getAllItems(PageRequest.of(page - 1, size))
            .content
            .find { it.hashCode() == id }

    //    Метод возвращает имена полей, которые должны будут отображены
    fun getNamesDomainFields(): List<String> =
        domainFields
            .filter {
                it.field.field?.annotations?.find { annotation ->
                    annotation.annotationClass.simpleName == "MappedCollection"
                } == null
            }
            .map { it.nameField }

    fun save(item: Any) =
        repository.save(item)

    fun delete(item: Any) {
        repository.delete(item)
    }
}