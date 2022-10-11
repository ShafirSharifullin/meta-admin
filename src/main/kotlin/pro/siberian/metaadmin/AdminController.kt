package pro.siberian.metaadmin

import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.Instant
import java.util.*


@Controller
class AdminController(private val metaAdminService: MetaAdminService) {

    @GetMapping("/admin")
    fun showListRepo(model: Model): String {
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
        val pageSize = size.orElse(5)

        val rows = metaAdminService.getAllDataFrom(repoCode, PageRequest.of(currentPage - 1, pageSize))

        model.addAttribute("page", currentPage)
        model.addAttribute("headers", metaAdminService.getNamesDomainFields(repoCode))
        model.addAttribute("rows", rows)
        model.addAttribute("repoCode", repoCode)

        return "list-domains"
    }

    @GetMapping("/admin/{repoCode}/{id}")
    fun showEditingForm(@PathVariable id: Long, @PathVariable repoCode: String, model: Model): String {
        val repoItem = metaAdminService.getRepoItemById(repoCode, id).ifEmpty {
            error(HttpStatus.NOT_FOUND, model = model)
            return "error"
        }

        model.addAttribute("headers", metaAdminService.getNamesDomainFields(repoCode))
        model.addAttribute("repoItem", repoItem)

        return "edit-form"
    }

    @PostMapping("/admin/{repoCode}/{id}")
    fun updateRepo(@PathVariable id: Long, @PathVariable repoCode: String, vararg strings: String): String {
        return "redirect:/admin/$repoCode"
    }

    private fun error(status: HttpStatus, message: String = status.name, model: Model) {
        model.addAttribute("date", Instant.now())
        model.addAttribute("status", status.value())
        model.addAttribute("message", message)
    }
}