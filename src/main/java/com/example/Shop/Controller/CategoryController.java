package com.example.Shop.Controller;

import com.example.Shop.Entity.Category;
import com.example.Shop.Entity.Product;
import com.example.Shop.Repository.CategoryRepository;
import com.example.Shop.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;


    @GetMapping("/list")
    public String listCategories(Model model){
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "Category/list";
    }

    @GetMapping("/new")
    public String showNewCategoryForm(Model model){
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        model.addAttribute("category", new Category());
        return "Category/newForm";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                               @RequestParam(value = "products", required = false) List<Long> productsIds) {
        Category savedCategory = categoryRepository.save(category);

        if (productsIds != null) {
            for (Long id : productsIds) {
                Product product = productRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + id));
                category.addProduct(product);
            }
        }
        categoryRepository.save(savedCategory);
        return "redirect:/product/index";
    }

    @GetMapping("/edit")
    public String showEditCategoryForm(@RequestParam("id") Long id, Model model){
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category id: " + id));
        List<Product> products = productRepository.findAll();
        model.addAttribute("category", category);
        model.addAttribute("products", products);
        return "Category/editForm";
    }
    @PostMapping("/update")
    public String updateCategory(@ModelAttribute("category") Category category,
                                 @RequestParam(value="productIds", required = false) List<Long> productIds){
        Category currentCategory = categoryRepository.findById(category.getId())
                        .orElseThrow(()-> new IllegalArgumentException("invalid category id: " + category.getId()));
        currentCategory.setName(category.getName());
        currentCategory.getProducts().clear();
        if(productIds !=null){
            for(Long id : productIds){
                Product product = productRepository.findById(id)
                        .orElseThrow(()->new IllegalArgumentException("Invalid product id: "+ id));
                currentCategory.addProduct(product);
            }
        }
        categoryRepository.save(currentCategory);
        return "redirect:/category/list";
    }
    @GetMapping("/removeProduct")
    public String removeProductFromCategory(@RequestParam("categoryId") Long CategoryId, @RequestParam("productId") Long productId){
        Category category = categoryRepository.findById(CategoryId)
                .orElseThrow(()->new IllegalArgumentException("Invalid category id" + CategoryId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new IllegalArgumentException("invalid product id: "+ productId));
        category.removeProduct(product);
        categoryRepository.save(category);
        return "redirect:/category/edit?id=" + CategoryId;
    }
    @PostMapping("/{productId}/add-to-category/{categoryId}")
    public String addProductToCategory(@PathVariable Long productId,
                                       @PathVariable Long categoryId,
                                       RedirectAttributes redirectAttributes) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category id: " + categoryId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + productId));

        category.addProduct(product);
        productRepository.save(product);
        categoryRepository.save(category);
        redirectAttributes.addAttribute("id", categoryId);

        return "redirect:/product/index?id=" + categoryId;
    }



    @GetMapping("/delete")
    public String deleteCategory(@RequestParam("id") Long id){
        categoryRepository.deleteById(id);
        return"redirect:/product/index";
    }
}
