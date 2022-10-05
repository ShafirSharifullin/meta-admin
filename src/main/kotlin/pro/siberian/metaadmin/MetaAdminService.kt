package pro.siberian.metaadmin

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.data.jdbc.core.mapping.BasicJdbcPersistentProperty
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.support.Repositories
import org.springframework.stereotype.Service
import kotlin.reflect.full.memberProperties

@Service
class MetaAdminService(listableBeanFactory: ListableBeanFactory) {

    private final var reposInfo: Repositories = Repositories(listableBeanFactory)
    private final val domains = reposInfo.toSet()
    val repos: List<Repo> =
        domains.map { domain ->
            Repo(
                reposInfo.getRepositoryInformationFor(domain).get().repositoryInterface.simpleName,
                reposInfo.getRepositoryFor(domain).get() as CrudRepository<*, *>,
                domain,
                reposInfo.getPersistentEntity(domain).map { DomainField(it as BasicJdbcPersistentProperty, it.name) }
            )
        }

    fun getRepoCodes() = repos.map { it.repoCode }

    fun getAllDataFrom(repoCode: String): List<Map<String?, Any?>>? {
        val repo = findRepoByRepoCode(repoCode) ?: return null
        val fieldNames = getNamesDomainFields(repoCode)
        return repo.repository.findAll().map { el ->
            fieldNames.associateWith { header ->
                el.javaClass.kotlin.memberProperties.first { it.name == header }.get(el)
            }
        }
    }

    fun getRepoItemById(repoCode: String, id: Long): Map<String?, Any?> {
        val repo = findRepoByRepoCode(repoCode) ?: return mapOf()
        val fieldNames = getNamesDomainFields(repoCode)
        val repoItem = (repo.repository as CrudRepository<*, Any>).findById(id).orElse(null) ?: return mapOf()
        return fieldNames.associateWith { header ->
            repoItem.javaClass.kotlin.memberProperties.first { it.name == header }.get(repoItem)
        }
    }

    fun getDomain(repoCode: String): Class<*>? = findRepoByRepoCode(repoCode)?.domain

    fun getDomainFields(repoCode: String): List<DomainField> = findRepoByRepoCode(repoCode)?.domainFields ?: listOf()

    fun save(repoCode: String, domain: Class<*>): Class<*>? =
        (findRepoByRepoCode(repoCode)?.repository as CrudRepository<Class<*>, Any>).save(domain)

    fun getNamesDomainFields(repoCode: String): List<String?> =
        findRepoByRepoCode(repoCode)?.domainFields
//            Фильтр убирает поля из сущности, которые являются связью один-ко-многим
            ?.filter { it.field.field?.type?.kotlin != List::class }
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