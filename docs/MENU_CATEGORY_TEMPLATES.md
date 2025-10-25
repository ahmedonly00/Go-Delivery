# Menu Category Templates - Implementation Guide

## Overview

Restaurant admins can now select from **45 predefined menu category templates** when creating menu categories for their restaurant, instead of creating them from scratch.

---

## How It Works

### 1. **Templates are Pre-loaded**
On application startup, 45 category templates are automatically created in the database, including:
- Appetizers, Starters, Salads
- Main Courses, Grilled, Fried, Steamed, Baked
- Pizza, Pasta, Burgers, Seafood
- Chicken, Beef, Pork, Vegetarian, Vegan
- Rice Dishes, Noodles, Fried Rice
- Soups, Stews
- Breakfast, Brunch
- Side Dishes, Fries
- Desserts, Ice Cream, Cakes
- Beverages, Hot Drinks, Cold Drinks, Smoothies, Fresh Juices
- Chef's Special, Daily Special, Combo Meals, Kids Menu
- African Dishes, Rwandan Cuisine, Mozambican Cuisine
- Fast Food, Street Food
- Healthy Options, Gluten-Free

### 2. **Restaurant Admin Workflow**
1. Admin logs into their restaurant dashboard
2. Views available category templates
3. Selects templates they want to use
4. Optionally customizes the image
5. Categories are created for their specific restaurant

---

## API Endpoints

### Get All Available Templates
```http
GET /api/menu-category/templates
```

**Response:**
```json
[
  {
    "templateId": 1,
    "categoryName": "Appetizers",
    "description": "Small dishes served before the main course",
    "defaultImageUrl": null,
    "sortOrder": 1,
    "isActive": true
  },
  {
    "templateId": 2,
    "categoryName": "Starters",
    "description": "Light dishes to begin your meal",
    "defaultImageUrl": null,
    "sortOrder": 2,
    "isActive": true
  }
]
```

### Create Category from Template
```http
POST /api/menu-category/create-from-template
Content-Type: application/json
```

**Request Body:**
```json
{
  "templateId": 1,
  "restaurantId": 5,
  "customImage": "https://example.com/my-appetizers.jpg"  // Optional
}
```

**Response:**
```json
{
  "categoryId": 123,
  "categoryName": "Appetizers",
  "description": "Small dishes served before the main course",
  "image": "https://example.com/my-appetizers.jpg",
  "sortOrder": 1,
  "isActive": true,
  "createdAt": "2025-10-25",
  "restaurantId": 5
}
```

---

## Database Schema

### New Table: `menu_category_template`
```sql
CREATE TABLE menu_category_template (
    template_id BIGINT PRIMARY KEY,
    category_name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255) NOT NULL,
    default_image_url VARCHAR(500),
    sort_order INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at DATE NOT NULL
);
```

### Existing Table: `menu_category`
- Remains unchanged
- Each category is still tied to a specific restaurant
- Categories created from templates are independent copies

---

## Flutter Integration Example

```dart
class MenuCategoryTemplateService {
  static const String baseUrl = 'http://localhost:8085/api/menu-category';

  // Get all available templates
  Future<List<MenuCategoryTemplate>> getAllTemplates(String token) async {
    final response = await http.get(
      Uri.parse('$baseUrl/templates'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
    );

    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((json) => MenuCategoryTemplate.fromJson(json)).toList();
    }
    throw Exception('Failed to load templates');
  }

  // Create category from template
  Future<MenuCategory> createFromTemplate({
    required int templateId,
    required int restaurantId,
    required String token,
    String? customImage,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/create-from-template'),
      headers: {
        'Authorization': 'Bearer $token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'templateId': templateId,
        'restaurantId': restaurantId,
        'customImage': customImage,
      }),
    );

    if (response.statusCode == 200) {
      return MenuCategory.fromJson(jsonDecode(response.body));
    }
    throw Exception('Failed to create category');
  }
}
```

---

## UI Flow Example

### Step 1: Display Templates
```dart
class SelectCategoryTemplateScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<MenuCategoryTemplate>>(
      future: templateService.getAllTemplates(token),
      builder: (context, snapshot) {
        if (!snapshot.hasData) return CircularProgressIndicator();
        
        return ListView.builder(
          itemCount: snapshot.data!.length,
          itemBuilder: (context, index) {
            final template = snapshot.data![index];
            return ListTile(
              title: Text(template.categoryName),
              subtitle: Text(template.description),
              trailing: Icon(Icons.add_circle),
              onTap: () => _selectTemplate(template),
            );
          },
        );
      },
    );
  }
}
```

### Step 2: Confirm and Create
```dart
void _selectTemplate(MenuCategoryTemplate template) async {
  // Optionally show dialog to customize image
  final customImage = await showImagePickerDialog();
  
  // Create category from template
  final category = await templateService.createFromTemplate(
    templateId: template.templateId,
    restaurantId: currentRestaurantId,
    token: authToken,
    customImage: customImage,
  );
  
  // Show success message
  ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(content: Text('${category.categoryName} added!')),
  );
}
```

---

## Benefits

✅ **Consistency** - Standardized category names across restaurants  
✅ **Speed** - Quick setup for new restaurants  
✅ **Flexibility** - Admins can still customize images  
✅ **Scalability** - Easy to add more templates  
✅ **Multi-language Ready** - Templates can be translated  

---

## Complete Template List

1. Appetizers
2. Starters
3. Salads
4. Main Courses
5. Grilled
6. Fried
7. Steamed
8. Baked
9. Pizza
10. Pasta
11. Burgers
12. Seafood
13. Chicken
14. Beef
15. Pork
16. Vegetarian
17. Vegan
18. Rice Dishes
19. Noodles
20. Fried Rice
21. Soups
22. Stews
23. Breakfast
24. Brunch
25. Side Dishes
26. Fries
27. Desserts
28. Ice Cream
29. Cakes
30. Beverages
31. Hot Drinks
32. Cold Drinks
33. Smoothies
34. Fresh Juices
35. Chef's Special
36. Daily Special
37. Combo Meals
38. Kids Menu
39. African Dishes
40. Rwandan Cuisine
41. Mozambican Cuisine
42. Fast Food
43. Street Food
44. Healthy Options
45. Gluten-Free

---

## Notes

- Templates are **global** and shared across all restaurants
- Categories created from templates are **restaurant-specific**
- Admins can still create custom categories using the existing endpoint
- Templates can be deactivated by setting `isActive = false`
- Sort order determines display order in the template list

---

## Future Enhancements

- Add default images for each template
- Support for multi-language template names
- Allow restaurants to suggest new templates
- Template categories (e.g., "Cuisine Type", "Meal Time", "Dietary")
- Template popularity tracking
