package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.file.FileUploadResponse;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;
import com.goDelivery.goDelivery.repository.MenuItemRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import com.goDelivery.goDelivery.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuUploadService {
    private final FileStorageService fileStorageService;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final Tesseract tesseract;
    private final EmailService emailService;
    
    @Value("${tess4j.data-path:./tessdata}")
    private String tessDataPath;

    public FileUploadResponse processMenuUpload(MultipartFile file, Long restaurantId) {
        try {
            // Validate restaurant exists
            Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

            // Get or create default category
            MenuCategory defaultCategory = menuCategoryRepository.findByRestaurantId(restaurantId)
                .stream()
                .findFirst()
                .orElseGet(() -> createDefaultCategory(restaurant));

            String fileExtension = getFileExtension(file.getOriginalFilename());
            List<MenuItemRequest> menuItems = new ArrayList<>();

            switch (fileExtension.toLowerCase()) {
                case "pdf":
                    menuItems = processPdfFile(file.getInputStream(), defaultCategory.getCategoryId(), restaurantId);
                    break;
                case "xlsx":
                case "xls":
            menuItems = processExcelFile(file.getInputStream(), defaultCategory.getCategoryId(), restaurantId);
                    break;
                case "jpg":
                case "jpeg":
                case "png":
                    menuItems = processImageFile(file, defaultCategory.getCategoryId(), restaurantId);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported file format: " + fileExtension);
            }

            // Save menu items to database
            List<MenuItem> savedItems = new ArrayList<>();
            // Get the default category for this restaurant
            MenuCategory category = menuCategoryRepository.findByRestaurantId(restaurantId)
                .stream()
                .findFirst()
                .orElseGet(() -> createDefaultCategory(restaurant));
            
            LocalDate now = LocalDate.now();
            
            for (MenuItemRequest itemRequest : menuItems) {
                // Set default values for required fields
                String description = itemRequest.getDescription() != null ? itemRequest.getDescription() : "";
                String ingredients = itemRequest.getIngredients() != null ? itemRequest.getIngredients() : "";
                String image = itemRequest.getImage() != null ? itemRequest.getImage() : "";
                Float price = itemRequest.getPrice() != null ? itemRequest.getPrice() : 0.0f;
                Integer prepTime = itemRequest.getPreparationTime() != null ? itemRequest.getPreparationTime() : 15;
                boolean isAvailable = itemRequest.isAvailable();
                
                // Create menu item with all required fields
                MenuItem menuItem = MenuItem.builder()
                        .menuItemName(itemRequest.getMenuItemName())
                        .description(description)
                        .price(price)
                        .image(image)
                        .ingredients(ingredients)
                        .preparationTime(prepTime)
                        .preparationScore(5)  // Default score
                        .createdAt(now)
                        .updatedAt(now)
                        .category(category)
                        .restaurant(restaurant)
                        .isAvailable(isAvailable)
                        .build();
                        
                savedItems.add(menuItemRepository.save(menuItem));
            }

            // Save the file
            String fileUrl = fileStorageService.storeFile(file, "restaurants/" + restaurantId + "/menu-uploads");
            
            // Send "Under Review" email after successful menu upload (NOT OTP)
            try {
                // Get restaurant admin user
                RestaurantUsers admin = restaurant.getRestaurantUsers().stream()
                    .filter(user -> user.getRole().name().equals("RESTAURANT_ADMIN"))
                    .findFirst()
                    .orElse(null);
                
                if (admin != null) {
                    // Mark email as verified and setup as complete
                    admin.setEmailVerified(true);
                    admin.setSetupComplete(true);
                    
                    // Send "under review" email instead of OTP
                    emailService.sendRestaurantUnderReviewEmail(
                        admin.getEmail(),
                        admin.getFullName(),
                        restaurant.getRestaurantName()
                    );
                    log.info("✅ 'Under Review' email sent to restaurant admin: {}", admin.getEmail());
                } else {
                    log.warn("No restaurant admin found for restaurant: {}", restaurantId);
                }
            } catch (Exception e) {
                log.error("❌ Failed to send 'Under Review' email: {}", e.getMessage());
                // Don't fail the entire operation if email sending fails
            }

            return FileUploadResponse.builder()
                    .success(true)
                    .message("File processed successfully. " + savedItems.size() + " items saved. Your restaurant is now under review. You will receive an email notification once approved.")
                    .menuItems(menuItems)
                    .fileUrl(fileUrl)
                    .build();

        } catch (Exception e) {
            return FileUploadResponse.builder()
                    .success(false)
                    .message("Error processing file: " + e.getMessage())
                    .build();
        }
    }

    private MenuCategory createDefaultCategory(Restaurant restaurant) {
        MenuCategory category = MenuCategory.builder()
                .categoryName("Main Menu")
                .description("Default menu category")
                .image("")
                .sortOrder(1)
                .isActive(true)
                .restaurant(restaurant)
                .createdAt(LocalDate.now())
                .build();
        return menuCategoryRepository.save(category);
    }

    private List<MenuItemRequest> processPdfFile(InputStream inputStream, Long categoryId, Long restaurantId) throws IOException {
        List<MenuItemRequest> items = new ArrayList<>();
        
        // Convert InputStream to byte array
        byte[] pdfBytes = inputStream.readAllBytes();
        
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                
                // Split text into lines and process each line
                String[] lines = text.split("\\r?\\n");
                
                // Pattern to match various price formats:
                // - $10.99
                // - 10.99$
                // - USD 10.99
                // - 10.99 EGP
                // - 10,99 (European format)
                Pattern pricePattern = Pattern.compile("([$€£]?\\s*\\d+[.,]?\\d*\\.?\\d*\s*[$€£]?|\\d+\\.?\\d*\s*(?:USD|EGP|EUR|£|€))");
                
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    // Look for price in the line
                    Matcher matcher = pricePattern.matcher(line);
                    if (matcher.find()) {
                        String priceMatch = matcher.group(1);
                        // Extract the price value by removing all non-digit characters except decimal point
                        String priceStr = priceMatch.replaceAll("[^\\d.,]", "")
                                                 .replace(',', '.'); // Handle European decimal format
                        
                        // Extract the item name (everything before the price)
                        String name = line.substring(0, matcher.start()).trim();
                        
                        try {
                            float price = Float.parseFloat(priceStr);
                            if (name.length() > 0 && price > 0) {
                                items.add(MenuItemRequest.builder()
                                        .menuItemName(name)
                                        .price(price)
                                        .isAvailable(true)
                                        .preparationTime(15) // Default value
                                        .categoryId(categoryId)
                                        .restaurantId(restaurantId)
                                        .build());
                                log.info("Added menu item: {} - {}", name, price);
                            }
                        } catch (NumberFormatException e) {
                            log.warn("Could not parse price from '{}' in line: {}", priceMatch, line);
                        }
                    } else {
                        log.debug("No price found in line: {}", line);
                    }
                }
                
                if (items.isEmpty()) {
                    log.warn("No menu items were extracted from the PDF. Here's the extracted text for debugging:" + text);
                }
            }
        }
        
        return items;
    }

    private List<MenuItemRequest> processExcelFile(InputStream inputStream, Long categoryId, Long restaurantId) throws IOException {
        List<MenuItemRequest> items = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (assuming first row is header)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    try {
                        MenuItemRequest item = MenuItemRequest.builder()
                                .menuItemName(getCellValue(row.getCell(0)))
                                .description(getCellValue(row.getCell(1)))
                                .price(parseFloatSafely(getCellValue(row.getCell(2)), "0"))
                                .ingredients(getCellValue(row.getCell(3)))
                                .preparationTime(parseIntSafely(getCellValue(row.getCell(4)), "15"))
                                .isAvailable(true)
                                .build();
                        items.add(item);
                    } catch (Exception e) {
                        log.error("Error processing row {}: {}", i, e.getMessage());
                    }
                }
            }
        }
        return items;
    }

    private List<MenuItemRequest> processImageFile(MultipartFile file, Long categoryId, Long restaurantId) throws IOException, TesseractException {
        List<MenuItemRequest> items = new ArrayList<>();
        
        // Convert MultipartFile to File
        Path tempFile = Files.createTempFile("img-", "." + getFileExtension(file.getOriginalFilename()));
        try {
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Set the path to the tessdata directory
            tesseract.setDatapath(tessDataPath);
            
            // Perform OCR on the image
            String result = tesseract.doOCR(tempFile.toFile());
            
            // Parse the OCR result (this is a simple example, you might need more sophisticated parsing)
            String[] lines = result.split("\\r?\\n");
            
            for (String line : lines) {
                line = line.trim();
                if (line.matches(".*\\d+\\.?\\d*\\s*\\$?\\s*\\d+.*") || line.matches(".*\\$\\s*\\d+\\.?\\d*.*")) {
                    // This is a simple pattern match for menu items with prices
                    // You might need to adjust this based on your menu format
                    String[] parts = line.split("\\s+\\$");
                    if (parts.length >= 2) {
                        String name = parts[0].trim();
                        String priceStr = parts[1].split("\\s+")[0].replaceAll("[^\\d.]", "");
                        
                        try {
                            float price = Float.parseFloat(priceStr);
                            items.add(MenuItemRequest.builder()
                                    .menuItemName(name)
                                    .price(price)
                                    .isAvailable(true)
                                    .preparationTime(15) // Default value
                                    .build());
                        } catch (NumberFormatException e) {
                            log.warn("Could not parse price from: " + line);
                        }
                    }
                }
            }
            
        } finally {
            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        }
        
        return items;
    }

    private String getCellValue(Cell cell) {
        return getCellValue(cell, "");
    }

    private String getCellValue(Cell cell, String defaultValue) {
        if (cell == null) return defaultValue;
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        // Remove .0 from whole numbers
                        double num = cell.getNumericCellValue();
                        if (num == (int) num) {
                            return String.valueOf((int) num);
                        }
                        return String.valueOf(num);
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return defaultValue;
            }
        } catch (Exception e) {
            log.warn("Error getting cell value: {}", e.getMessage());
            return defaultValue;
        }
    }
    
    private float parseFloatSafely(String value, String defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            log.warn("Could not parse float from: {}, using default: {}", value, defaultValue);
            return Float.parseFloat(defaultValue);
        }
    }
    
    private int parseIntSafely(String value, String defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Could not parse int from: " + value + ", using default: " + defaultValue);
            return Integer.parseInt(defaultValue);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
