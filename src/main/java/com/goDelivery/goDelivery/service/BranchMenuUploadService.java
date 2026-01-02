package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.file.FileUploadResponse;
import com.goDelivery.goDelivery.dtos.menu.MenuItemRequest;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.model.BranchUsers;
import com.goDelivery.goDelivery.repository.BranchesRepository;
import com.goDelivery.goDelivery.repository.MenuCategoryRepository;
import com.goDelivery.goDelivery.repository.MenuItemRepository;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
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
public class BranchMenuUploadService {
    private final FileStorageService fileStorageService;
    private final BranchesRepository branchesRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final Tesseract tesseract;
    private final BranchMenuService branchMenuService;
    
    @Value("${tess4j.data-path:./tessdata}")
    private String tessDataPath;

    public FileUploadResponse processBranchMenuUpload(MultipartFile file, Long branchId) {
        try {
            // Validate branch exists
            Branches branch = branchesRepository.findByBranchId(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id: " + branchId));

            // Verify branch is properly set up
            if (branch.getRestaurant() == null) {
                throw new IllegalStateException("Branch must be associated with a restaurant to upload menu");
            }

            // Get or create default category for the branch
            MenuCategory defaultCategory = menuCategoryRepository.findByBranch_BranchId(branchId)
                .stream()
                .findFirst()
                .orElseGet(() -> createDefaultCategory(branch));

            String fileExtension = getFileExtension(file.getOriginalFilename());
            List<MenuItemRequest> menuItems = new ArrayList<>();

            switch (fileExtension.toLowerCase()) {
                case "pdf":
                    menuItems = processPdfFile(file.getInputStream(), defaultCategory.getCategoryId());
                    break;
                case "xlsx":
                case "xls":
                    menuItems = processExcelFile(file.getInputStream(), defaultCategory.getCategoryId());
                    break;
                case "jpg":
                case "jpeg":
                case "png":
                    menuItems = processImageFile(file, defaultCategory.getCategoryId());
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported file format: " + fileExtension);
            }

            // Save menu items using the BranchMenuService
            List<MenuItem> savedItems = new ArrayList<>();
            
            for (MenuItemRequest itemRequest : menuItems) {
                // Set the category ID and branch context
                itemRequest.setCategoryId(defaultCategory.getCategoryId());
                
                // Create menu item using the service (handles image upload if needed)
                try {
                    // Create a temporary MenuItemRequest without image for batch processing
                    MenuItemRequest tempRequest = MenuItemRequest.builder()
                            .menuItemName(itemRequest.getMenuItemName())
                            .description(itemRequest.getDescription() != null ? itemRequest.getDescription() : "")
                            .price(itemRequest.getPrice() != null ? itemRequest.getPrice() : 0.0f)
                            .ingredients(itemRequest.getIngredients() != null ? itemRequest.getIngredients() : "")
                            .preparationTime(itemRequest.getPreparationTime() != null ? itemRequest.getPreparationTime() : 15)
                            .isAvailable(itemRequest.isAvailable())
                            .categoryId(defaultCategory.getCategoryId())
                            .build();
                    
                    // Use the service to create the menu item
                    MenuItem savedItem = createMenuItemFromRequest(tempRequest, defaultCategory, branch);
                    savedItems.add(savedItem);
                } catch (Exception e) {
                    log.error("Error saving menu item {}: {}", itemRequest.getMenuItemName(), e.getMessage());
                }
            }

            // Save the file
            String fileUrl = fileStorageService.storeFile(file, "branches/" + branchId + "/menu-uploads");
            
            // Mark branch setup as complete after successful menu upload
            markBranchSetupComplete(branchId);
            
            log.info("Successfully processed menu upload for branch {}: {} items saved", branchId, savedItems.size());

            return FileUploadResponse.builder()
                    .success(true)
                    .message("File processed successfully. " + savedItems.size() + " items saved to branch menu. Branch setup is now complete!")
                    .menuItems(menuItems)
                    .fileUrl(fileUrl)
                    .build();

        } catch (Exception e) {
            log.error("Error processing menu upload for branch {}: {}", branchId, e.getMessage(), e);
            return FileUploadResponse.builder()
                    .success(false)
                    .message("Error processing file: " + e.getMessage())
                    .build();
        }
    }

    private MenuItem createMenuItemFromRequest(MenuItemRequest request, MenuCategory category, Branches branch) {
        MenuItem menuItem = MenuItem.builder()
                .menuItemName(request.getMenuItemName())
                .description(request.getDescription())
                .price(request.getPrice())
                .ingredients(request.getIngredients())
                .preparationTime(request.getPreparationTime())
                .preparationScore(5)  // Default score
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .category(category)
                .branch(branch)
                .restaurant(branch.getRestaurant())
                .isAvailable(request.isAvailable())
                .build();
                
        return menuItemRepository.save(menuItem);
    }

    private MenuCategory createDefaultCategory(Branches branch) {
        MenuCategory category = MenuCategory.builder()
                .categoryName("Branch Menu")
                .branch(branch)
                .restaurant(branch.getRestaurant())
                .createdAt(LocalDate.now())
                .build();
        return menuCategoryRepository.save(category);
    }

    private List<MenuItemRequest> processPdfFile(InputStream inputStream, Long categoryId) throws IOException {
        List<MenuItemRequest> items = new ArrayList<>();
        
        byte[] pdfBytes = inputStream.readAllBytes();
        
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                
                String[] lines = text.split("\\r?\\n");
                
                Pattern pricePattern = Pattern.compile("([$€£]?\\s*\\d+[.,]?\\d*\\.?\\d*\\s*[$€£]?|\\d+\\.?\\d*\\s*(?:USD|EGP|EUR|MZN|MT|£|€))");
                
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    Matcher matcher = pricePattern.matcher(line);
                    if (matcher.find()) {
                        String priceMatch = matcher.group(1);
                        String priceStr = priceMatch.replaceAll("[^\\d.,]", "")
                                                 .replace(',', '.');
                        
                        String name = line.substring(0, matcher.start()).trim();
                        
                        try {
                            float price = Float.parseFloat(priceStr);
                            if (name.length() > 0 && price > 0) {
                                items.add(MenuItemRequest.builder()
                                        .menuItemName(name)
                                        .price(price)
                                        .isAvailable(true)
                                        .preparationTime(15)
                                        .categoryId(categoryId)
                                        .build());
                                log.info("Added menu item: {} - {}", name, price);
                            }
                        } catch (NumberFormatException e) {
                            log.warn("Could not parse price from '{}' in line: {}", priceMatch, line);
                        }
                    }
                }
                
                if (items.isEmpty()) {
                    log.warn("No menu items were extracted from the PDF");
                }
            }
        }
        
        return items;
    }

    private List<MenuItemRequest> processExcelFile(InputStream inputStream, Long categoryId) throws IOException {
        List<MenuItemRequest> items = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
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
                                .categoryId(categoryId)
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

    private List<MenuItemRequest> processImageFile(MultipartFile file, Long categoryId) throws IOException, TesseractException {
        List<MenuItemRequest> items = new ArrayList<>();
        
        Path tempFile = Files.createTempFile("img-", "." + getFileExtension(file.getOriginalFilename()));
        try {
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            
            tesseract.setDatapath(tessDataPath);
            tesseract.setPageSegMode(4);
            tesseract.setVariable("preserve_interword_spaces", "1");
            
            log.info("Starting OCR processing for branch menu...");
            String result = tesseract.doOCR(tempFile.toFile());
            log.debug("OCR Result:\n" + result);
            
            String[] lines = result.split("\\r?\\n");
            
            Pattern pricePattern = Pattern.compile(
                "([$€£]?\\s*\\d+[.,]?\\d*\\.?\\d*\\s*[$€£]?|\\d+[.,]?\\d*\\s*(?:USD|EGP|EUR|MZN|MT|£|€))"
            );
            
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.length() < 3) continue;
                
                Matcher matcher = pricePattern.matcher(line);
                if (matcher.find()) {
                    String priceMatch = matcher.group(1);
                    String priceStr = priceMatch.replaceAll("[^\\d.,]", "")
                                             .replace(',', '.');
                    
                    String name = line.substring(0, matcher.start()).trim();
                    name = name.replaceAll("[^\\p{L}\\p{N}\\s]$", "").trim();
                    
                    if (name.length() < 2) continue;
                    
                    try {
                        float price = Float.parseFloat(priceStr);
                        if (price > 0) {
                            items.add(MenuItemRequest.builder()
                                    .menuItemName(name)
                                    .price(price)
                                    .isAvailable(true)
                                    .preparationTime(15)
                                    .categoryId(categoryId)
                                    .build());
                            log.info("Added menu item: {} - {}", name, price);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Could not parse price from '{}' in line: {}", priceMatch, line);
                    }
                }
            }
            
        } finally {
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
            log.warn("Could not parse int from: {}, using default: {}", value, defaultValue);
            return Integer.parseInt(defaultValue);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    private void markBranchSetupComplete(Long branchId) {
        try {
            // Get all branch users and mark setup as complete
            List<BranchUsers> branchUsers = branchUsersRepository.findByBranch_BranchId(branchId);
            
            for (BranchUsers user : branchUsers) {
                if (!user.isSetupComplete()) {
                    user.setSetupComplete(true);
                    user.setUpdatedAt(LocalDate.now());
                    branchUsersRepository.save(user);
                    log.info("Marked setup complete for branch user: {}", user.getEmail());
                }
            }
            
            log.info("Branch setup marked as complete for branch ID: {}", branchId);
        } catch (Exception e) {
            log.error("Error marking branch setup complete for branch {}: {}", branchId, e.getMessage());
            // Don't fail the operation if setup completion fails
        }
    }
}
