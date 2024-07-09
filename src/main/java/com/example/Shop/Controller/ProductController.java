package com.example.Shop.Controller;

import com.example.Shop.Entity.*;
import com.example.Shop.Repository.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.groovy.tools.shell.IO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@SessionAttributes({"cart","totalPrice"})
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);


    @ModelAttribute("totalPrice")
    public Double totalPrice(){
        return 0.0;
    }

    @PostMapping("/addtocart")
    public String addToCart(@RequestParam("id") Long id, @ModelAttribute("cart") List<Product> cart,@ModelAttribute("totalPrice") Double totalPrice, RedirectAttributes redirectAttributes, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + id));


        cart.add(product);
        totalPrice += product.getPrice();
        model.addAttribute("totalPrice", totalPrice);
        redirectAttributes.addFlashAttribute("message", "Product added to cart successfully.");

        return "redirect:/product/index";
    }


    @GetMapping("/cart")
    public String viewCart(@ModelAttribute("cart") List<Product> cart, Model model, @ModelAttribute("totalPrice") Double totalPrice){
        model.addAttribute("cart"  ,cart);
        model.addAttribute("totalPrice", totalPrice);
        return"Product/cart";
    }

    @PostMapping("/removefromcart")
    public String removeFromCart(@RequestParam("id") Long id, @ModelAttribute("cart") List<Product> cart, RedirectAttributes redirectAttributes, @ModelAttribute("totalPrice") Double totalPrice, Model model){
        Product producttoremove = cart.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElse(null);

        if(producttoremove != null){
            cart.remove(producttoremove);
            totalPrice -= producttoremove.getPrice();
            model.addAttribute("totalPrice", totalPrice);
            redirectAttributes.addFlashAttribute("message", "Product removed from cart successfully.");
        }else{
            redirectAttributes.addFlashAttribute("error", "product not found in cart");
        }
        return "redirect:/product/cart";
    }


    @PostMapping("/finalize-order")
    public String finalizeOrder(@ModelAttribute("cart") List<Product> cart, Authentication authentication, RedirectAttributes redirectAttributes, SessionStatus sessionStatus) {
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "User not authenticated.");
            return "redirect:/login";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }

        Order order = new Order();
        order.setUser(user);

        for (Product product : cart) {
            OrderDetails orderDetails = new OrderDetails();
            orderDetails.setProduct(product);
            orderDetails.setOrder(order);
            order.getOrderDetailsList().add(orderDetails);

        }

        orderRepository.save(order);
        sessionStatus.setComplete();
        redirectAttributes.addFlashAttribute("message", "your order has been finalized.");
        return "redirect:/product/index";
    } @ModelAttribute("cart")
    public List<Product> cart(){
        return new ArrayList<>();
    }



    @PostMapping("/submit")
    @Secured("ADMIN")
    public String submitOrder(@ModelAttribute("orderId") Long orderId, Authentication authentication, SessionStatus sessionStatus, RedirectAttributes redirectAttributes){

        Optional<Order> optionalOrder= orderRepository.findById(orderId);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            for (OrderDetails detail : order.getOrderDetailsList()) {
                Product managedProduct = productRepository.findById(detail.getProduct().getId()).orElse(null);
                if (managedProduct != null) {
                    managedProduct.decreaseCount(1);
                    productRepository.save(managedProduct);
                }
            }
        }else {
            redirectAttributes.addFlashAttribute("error" , "Order not found.");
        }
        return "redirect:/product/adminPastOrders";
    }


    @GetMapping("/pastOrders")
    public String showPastOrders(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        if (user == null) {
            logger.error("User is not authenticated");
            return "redirect:/login";
        }

        User dbUser = userRepository.findByUsername(user.getUsername());
        if(dbUser ==null){
            logger.error("User is not authenticated");
            return "redirect:/login";
        }
        Long userId = dbUser.getId();
        logger.info("Fetching past orders for user ID: {}", userId);
        List<Order> pastOrders = orderRepository.findByUserId(userId);
        model.addAttribute("pastOrders", pastOrders );
        return "Product/pastOrders";
    }

    @GetMapping("/adminPastOrders")
    public String showAdminPastOrders(Model model, @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        if (user == null){
            logger.error("User is not authenticated");
            return "redirect:/login";
        }

        User dbUser = userRepository.findByUsername(user.getUsername());
        if(dbUser ==null){
            logger.error("User is not authenticated");
            return "redirect:/login";
        }


        List<Order> pastOrders = orderRepository.findAll();
        model.addAttribute("pastOrders", pastOrders );
        return "Product/adminPastOrders";
    }


    @GetMapping("/index")
    public String index(@RequestParam(value = "id", required=false) Long id, @RequestParam(value = "authors", required = false) Set<Long> authorIds, Model model) {
        List<Product> products;
        List<Category> categories = categoryRepository.findAll();
        List<Author> authors = authorRepository.findAll();
        List<Author> selectedAuthors = authorIds != null ? authorRepository.findAllById(authorIds) : null;

        if(id != null ){
            Optional<Category> selectedCategory = categoryRepository.findById(id);
                    if(selectedCategory !=null){
                        products = productRepository.findByCategories(selectedCategory);
                        model.addAttribute("selectedCategoryId" , id);
                    }else{
                        products=productRepository.findAll();
                    }
        }else {
            products = productRepository.findAll();
        }
        if(authorIds != null){
            products = products.stream()
                    .filter(product -> authorIds.contains(product.getAuthor().getId()))
                    .collect(Collectors.toList());
            model.addAttribute("selectedAuthors" , selectedAuthors);
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("authors", authors);

        return "Product/index";
    }


    @GetMapping("/new-form")
    public String showAddProduct(Model model){
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return"Product/newForm";
    }

    @PostMapping("/create")
    public String addProduct(@ModelAttribute Product product) {
        productRepository.save(product);
        return "redirect:/product/index";
    }
    private byte[] downloadImage(String imageUrl) throws IOException {
        UrlResource urlResource = new UrlResource(imageUrl);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = urlResource.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return outputStream.toByteArray();
    }

    @GetMapping("/update")
    public String updateForm(@RequestParam("id") long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "Product/updateForm";
    }

    @PostMapping("/update")
    public String update(@RequestParam("id") Long id,
                         @RequestParam("updatedProduct.name") String name,
                         @RequestParam("updatedProduct.price") double price,
                         @RequestParam("updatedProduct.details") String details,
                         @RequestParam("updatedProduct.imageUrl") String imageUrl,
                         @RequestParam("updatedProduct.productCount") int productCount,
                         @ModelAttribute Product product,
                         @RequestParam("categoryIds") List<Long> categoryIds,
                         @RequestParam(value = "fictionType", required = false)String fictionType) throws IOException {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product id: " + id));

        existingProduct.setName(name);
        existingProduct.setPrice(price);
        existingProduct.setDetails(details);
        existingProduct.setImageUrl(imageUrl);
        existingProduct.setProductCount(productCount);

        for(Long categoryId :categoryIds){
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(()->new IllegalArgumentException("Invalid category id: " + id));
            existingProduct.addCategory(category);
        }
        if (fictionType != null && existingProduct.getCategories().stream().anyMatch(category -> category.getId() == 3)) {
            Attributes attributes = new Attributes(fictionType);
            existingProduct.addAttributes(attributes);
        } else {
            existingProduct.setAttributes(null);
        }



        productRepository.save(existingProduct);
        return "redirect:/product/index";
    }

    @GetMapping("/details")
    public String productDetails(@RequestParam("id")Long id, Model model){
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Invalid product id: "+ id));

        Set<Category> categories = productRepository.findCategoriesByProductId(id);

        List<Product> similarProducts = productRepository.findByCategoriesIn(categories);

        model.addAttribute("product", product);
        similarProducts.remove(product);
        model.addAttribute("similarProducts", similarProducts);

        return "Product/details";

    }
    @GetMapping("/search")
    public String search(Model model, @RequestParam(value="query", required=false)String query){
        List<Product> products;
        if(query !=null && !query.isEmpty()){
            products=productRepository.findByNameContainingIgnoreCase(query);
        }else{
            products=productRepository.findAll();
        }model.addAttribute("products", products);
        model.addAttribute("search", query);
        return"Product/searchResults";
    }
    @PostMapping("/delete")
    public String deleteProduct(@RequestParam("id") Long id){
        productRepository.deleteById(id);
        return "redirect:/product/index";
    }


    @GetMapping("/filterByAuthor")
    public String filterByAuthor(@RequestParam("authors") Set<Long> authorIds, Model model){
        List<Author> authors = authorRepository.findAllById(authorIds);
        List<Product> products = productRepository.findAll().stream()
                .filter((product -> authors.contains(product.getAuthor())))
                .collect(Collectors.toList());
        model.addAttribute("products", products);
        model.addAttribute("selectedAuthors",authors);
        return "Product/AuthorFilter";
    }
    @ModelAttribute("authors")
    public List<Author> populateAuthors() {
        return authorRepository.findAll();
    }

    @ModelAttribute("selectedAuthors")
    public Set<Long> populateSelectedAuthors() {
        return new HashSet<>();
    }

    @GetMapping("/advanced")
    public String advancedSearch(@RequestParam(value = "id", required=false) Long id,  Model model){
        List<Product> products;
        List<Category> categories = categoryRepository.findAll();

        if(id != null ){
            Optional<Category> selectedCategory = categoryRepository.findById(id);
            if(selectedCategory !=null){
                products = productRepository.findByCategories(selectedCategory);
                model.addAttribute("selectedCategoryId" , id);
            }else{
                products=productRepository.findAll();
            }
        }else {
            products = productRepository.findAll();
    }
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "Product/advancedSearch";
    }


    @GetMapping("/filterByPrice")
    public String filterByPrice(@RequestParam double minPrice, @RequestParam double maxPrice, Model model) {


        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        model.addAttribute("products", products);

        return "Product/index";
    }




    @PostMapping("/parse")
    public String parseFile(@RequestParam MultipartFile file) throws IOException, CsvException {
        String fileName = file.getOriginalFilename();
        if (fileName.endsWith(".csv")) {
            parseCSV(file);
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            parseExcel(file);
        } else {
            throw new IllegalArgumentException("unsupported file type.");
        }
        return "redirect:/product/index";
    }
    private void parseExcel(MultipartFile file) throws IOException{
        List<Product> products = new ArrayList<>();
        Workbook workbook= new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();
        if (rows.hasNext()){
            rows.next();
        }
        while (rows.hasNext()){
            Row row=rows.next();
            Product product = new Product();
            product.setName(row.getCell(0).getStringCellValue());
            product.setPrice(row.getCell(1).getNumericCellValue());
            product.setDetails(row.getCell(2).getStringCellValue());
            product.setProductCount((int) row.getCell(3).getNumericCellValue());
            product.setImageUrl((row.getCell(4)).getStringCellValue());
            products.add(product);

             if(products.size()>=10){
                 productRepository.saveAll(products);
                 products.clear();
            }
        }
        productRepository.saveAll(products);
        workbook.close();
    }
    private void parseCSV(MultipartFile file) throws IOException,CsvException{
        Reader reader = new InputStreamReader(file.getInputStream());
        CSVReader csvReader = new CSVReader(reader);
        csvReader.readNext();

        List<String[]> rows = csvReader.readAll();
        List<Product> products = new ArrayList<>();

        for (String[] row : rows){

            Product product = new Product();
            product.setName(row[0]);
            product.setPrice(Double.parseDouble(row[1]));
            product.setDetails(row[2]);
            product.setProductCount(Integer.parseInt(row[3]));
            product.setImageUrl(row[4]);

            products.add(product);

            if(products.size() >= 10){
                productRepository.saveAll(products);
                products.clear();
            }
        }
        productRepository.saveAll(products);
    }
    @GetMapping("/export")
    @ResponseBody
    public void exportData(@RequestParam(name="format") String format, HttpServletResponse response)throws IOException {
        if ("csv".equalsIgnoreCase(format)) {
            exportCSV(response);
        } else if ("excel".equalsIgnoreCase(format)) {
            exportExcel(response);
        }
    }

    private void exportExcel(HttpServletResponse response) throws IOException{
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=\"products.xlsx\"");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Products");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Name");
        headerRow.createCell(1).setCellValue("Price");
        headerRow.createCell(2).setCellValue("Details");
        headerRow.createCell(3).setCellValue("ProductCount");
        headerRow.createCell(4).setCellValue("ImageUrl");

        List<Product> products = productRepository.findAll();
        int rowIdx=1;
        for(Product product : products){
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(product.getName());
            row.createCell(1).setCellValue(product.getPrice());
            row.createCell(2).setCellValue(product.getDetails());
            row.createCell(3).setCellValue(product.getProductCount());
            row.createCell(4).setCellValue(product.getImageUrl());
        }
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private void exportCSV(HttpServletResponse response)throws IOException{
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"products.csv\"");

        PrintWriter writer = response.getWriter();
        CSVWriter csvWriter = new CSVWriter(writer,
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        String[] headerRecord ={"Name", "Price","Details","ProductCount", "ImageUrl"};
        csvWriter.writeNext(headerRecord);

        List<Product> products= productRepository.findAll();

        for(Product product: products){
            String details = product.getDetails().replace("\"", "\"\"");
            details = details.replaceAll("\\R", " ");
            details="\"" + details + "\"";
            String[] data={
                    product.getName(),
                    String.valueOf(product.getPrice()),
                    details,
                    String.valueOf(product.getProductCount()),
                    product.getImageUrl()
            };
            csvWriter.writeNext(data);
        }
        csvWriter.close();
     }



}
