package pro.siberian.metaadmin

import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*


@Controller
class AdminController(private val metaAdminService: MetaAdminService) {

    @GetMapping("/admin")
    fun showListRepos(model: Model): String {
        model.addAttribute("repos", metaAdminService.getRepoCodes())
        return "list-repos"
    }

    @GetMapping("/admin/{repoCode}")
    fun showDataFromRepo(
        @PathVariable repoCode: String,
        @RequestParam("page") page: Optional<Int>,
        @RequestParam("size") size: Optional<Int>,
        model: Model,
    ): String {
        val currentPage = page.orElse(1)
        val pageSize = size.orElse(15)

        if (currentPage < 0 || pageSize < 0) {
            error(HttpStatus.BAD_REQUEST, "Параметры page и size должны быть положительными", model = model)
            return "error"
        }

        val repoItems = metaAdminService.getAllDataFromRepo(repoCode, PageRequest.of(currentPage - 1, pageSize))
        if (repoItems == null) {
            error(HttpStatus.NOT_FOUND, "Репозиторий не найден", model = model)
            return "error"
        }

        model.addAttribute("page", currentPage)
        model.addAttribute("size", pageSize)
        model.addAttribute("totalPages", repoItems.totalPages)
        model.addAttribute("fieldNames", metaAdminService.getNamesDomainFields(repoCode))
        model.addAttribute("repoItems", repoItems)
        model.addAttribute("repoCode", repoCode)

        return "list-repo-items"
    }

    @GetMapping("/admin/{repoCode}/{id}")
    fun showEditForm(
        @PathVariable id: Int,
        @PathVariable repoCode: String,
        @RequestParam("page") page: Int,
        @RequestParam("size") size: Int,
        model: Model,
    ): String {
        if (page < 0 || size < 0) {
            error(HttpStatus.BAD_REQUEST, "Параметры page и size должны быть положительными", model = model)
            return "error"
        }

        val repoItem = metaAdminService.getItemById(repoCode, id, page, size)

        if (repoItem == null) {
            error(HttpStatus.NOT_FOUND, "Элемент репозитория не найден", model = model)
            return "error"
        }

        model.addAttribute("fieldNames", metaAdminService.getNamesDomainFields(repoCode))
        model.addAttribute("repoItem", repoItem)

        return "edit-form"
    }

    @PostMapping("/admin/{repoCode}/{id}")
    fun updateItem(@PathVariable id: Int, @PathVariable repoCode: String, vararg strings: String): String {
        return "redirect:/admin/$repoCode"
    }

    @DeleteMapping("/admin/{repoCode}/{id}")
    fun deleteItem(
        @PathVariable id: Int,
        @PathVariable repoCode: String,
        @RequestParam("page") page: Int,
        @RequestParam("size") size: Int,
        model: Model,
    ): String {
        if (page < 0 || size < 0) {
            error(HttpStatus.BAD_REQUEST, "Параметры page и size должны быть положительными", model = model)
            return "error"
        }

        val repoItem = metaAdminService.getItemById(repoCode, id, page, size)

        if (repoItem == null) {
            error(HttpStatus.NOT_FOUND, "Элемент репозитория не найден", model = model)
            return "error"
        }

        metaAdminService.delete(repoCode, repoItem)

        return "redirect:/admin/$repoCode"
    }


    private fun error(status: HttpStatus, message: String = status.name, model: Model) {
        model.addAttribute("date", Instant.now())
        model.addAttribute("status", status.value())
        model.addAttribute("message", message)
    }
}