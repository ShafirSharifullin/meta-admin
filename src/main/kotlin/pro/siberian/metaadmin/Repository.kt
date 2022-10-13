package pro.siberian.metaadmin

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.core.mapping.BasicJdbcPersistentProperty
import org.springframework.data.repository.PagingAndSortingRepository


class Repository(
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

data class DomainField(
    val field: BasicJdbcPersistentProperty,
    val nameField: String,
)