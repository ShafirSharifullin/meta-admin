package pro.siberian.metaadmin

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.core.mapping.BasicJdbcPersistentProperty
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.support.Repositories
import org.springframework.stereotype.Service
import kotlin.reflect.full.memberProperties

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

    //    Метод возвращает коды репозиториев, которыми являются их имена
    fun getRepoCodes() = repos.map { it.repoCode }

    //    Метод возвращает список значений сущностей из репозитория
    fun getAllDataFromRepo(repoCode: String, pageable: Pageable): Page<Any> {
        val items = (findRepoByRepoCode(repoCode)?.repository as PagingAndSortingRepository<Any, Any>)
            .findAll(pageable)
            .toList()
        val totalSize = (findRepoByRepoCode(repoCode)?.repository as PagingAndSortingRepository<Any, Any>).count()
        return PageImpl(items, pageable, totalSize)
    }

    fun getRepoItemById(repoCode: String, id: Long): Map<String?, Any?> {
        val repo = findRepoByRepoCode(repoCode) ?: return mapOf()
        val fieldNames = getNamesDomainFields(repoCode)
        val repoItem = (repo.repository as PagingAndSortingRepository<Any, Any>)
            .findById(id).orElse(null) ?: return mapOf()
        return fieldNames.associateWith { header ->
            repoItem.javaClass.kotlin.memberProperties.first { it.name == header }.get(repoItem)
        }
    }

    fun save(repoCode: String, domain: Any): Any =
        (findRepoByRepoCode(repoCode)?.repository as PagingAndSortingRepository<Any, Any>).save(domain)

    //    Метод возвращает имена полей, которые должны будут отображены
    fun getNamesDomainFields(repoCode: String): List<String> =
        findRepoByRepoCode(repoCode)?.domainFields
            ?.filter {
                it.field.field?.annotations?.find { annotation ->
                    annotation.annotationClass.simpleName == "MappedCollection"
                } == null
            }
            ?.map { it.nameField } ?: listOf()

    private fun findRepoByRepoCode(repoCode: String): Repo? = repos.find { it.repoCode == repoCode }
}

data class Repo(
    val repoCode: String,
    val repository: CrudRepository<*, *>,
    val domain: Class<*>,
    val domainFields: List<DomainField>,
)

data class DomainField(
    val field: BasicJdbcPersistentProperty,
    val nameField: String,
)