# Branch Menu Inheritance System

## Overview
Branches can inherit the complete menu from their parent restaurant and then modify it according to their needs. This allows for consistent base menu across all branches while enabling local customization.

## Features

### 1. **Automatic Menu Inheritance**
- When a branch starts menu setup, it automatically inherits all categories and items from the restaurant
- **Smart Inheritance**: If branch tries to add categories/items without inheritance, system auto-inherits first
- Inherited items can be modified, updated, or deleted by the branch manager
- New categories and items can be added by the branch

### 2. **Independent Menu Management**
- Each branch has its own copy of the menu
- Changes at branch level don't affect the restaurant or other branches
- Restaurant can update its menu without affecting existing branches

## API Endpoints

### Inherit Restaurant Menu
```http
POST /api/v1/branch-menu/{branchId}/inherit
Authorization: Bearer {BRANCH_MANAGER_TOKEN}
```

### Get Branch Menu
```http
GET /api/v1/branch-menu/{branchId}
Authorization: Bearer {BRANCH_MANAGER_TOKEN}
```

### Add New Category
```http
POST /api/v1/branch-menu/{branchId}/categories
{
  "categoryName": "Local Specialties"
}
```

### Add New Menu Item
```http
POST /api/v1/branch-menu/{branchId}/categories/{categoryId}/items
{
  "menuItemName": "Special Dish",
  "description": "Description",
  "price": 12.99,
  "ingredients": "Ingredients",
  "isAvailable": true,
  "preparationTime": 20,
  "preparationScore": 5
}
```

### Update Menu Item (including inherited)
```http
PUT /api/v1/branch-menu/{branchId}/items/{menuItemId}
{
  "price": 14.99,
  "isAvailable": false,
  "description": "Updated description"
}
```

### Delete Menu Item
```http
DELETE /api/v1/branch-menu/{branchId}/items/{menuItemId}
```

### Delete Category
```http
DELETE /api/v1/branch-menu/{branchId}/categories/{categoryId}
```

## Flow Summary

1. **Branch Creation**: Branch starts with no menu
2. **Menu Setup Start**: Branch manager clicks "Start Menu Setup"
   - **Automatic inheritance**: System automatically copies restaurant menu to branch
3. **Category Creation**: If branch tries to add category without inheritance:
   - System automatically inherits restaurant menu first
   - Then allows new category creation
4. **Item Addition**: If branch tries to add item without inheritance:
   - System automatically inherits restaurant menu first
   - Then allows new item creation
5. **Customization**: Branch manager can:
   - Update prices for local market
   - Disable unavailable items
   - Add local specialties
   - Remove items not sold at this location
6. **Complete Setup**: Branch finalizes their menu

## Data Model

### MenuCategory
- Can belong to either a restaurant OR a branch (not both)
- Categories copied from restaurant become independent branch copies
- Branch can add its own categories

### MenuItem
- Can belong to either a restaurant OR a branch (not both)
- Items copied from restaurant become independent branch copies
- Branch can modify price, availability, description, etc.
- Branch can delete inherited items

## Security & Permissions

- Only `BRANCH_MANAGER` can manage branch menu
- Managers can only access their own branch menu
- Restaurant admins cannot modify branch menus directly

## Example Scenarios

### Scenario 1: Price Adjustment
```json
// Restaurant has item at $10.00
// Branch in high-cost area updates price
PUT /api/v1/branch-menu/123/items/456
{
  "price": 12.50
}
```

### Scenario 2: Local Specialties
```json
// Branch adds local category
POST /api/v1/branch-menu/123/categories
{
  "categoryName": "Regional Favorites"
}

// Add local dish to new category
POST /api/v1/branch-menu/123/categories/789/items
{
  "menuItemName": "State Fair Corn Dogs",
  "price": 8.99,
  "description": "Local favorite"
}
```

### Scenario 3: Item Unavailability
```json
// Restaurant sells item but branch doesn't
PUT /api/v1/branch-menu/123/items/456
{
  "isAvailable": false
}
```

## Best Practices

1. **Inherit First**: Always inherit the base menu before making changes
2. **Local Pricing**: Adjust prices based on local market conditions
3. **Seasonal Items**: Disable seasonal items instead of deleting
4. **Local Specialties**: Use separate categories for local items
5. **Regular Updates**: Review and update menu regularly

## Error Handling

- `400 Bad Request`: Trying to inherit menu when branch already has items
- `403 Forbidden`: User not authorized for this branch
- `404 Not Found`: Branch, category, or item not found
- `409 Conflict`: Category name already exists for branch

## Notes

- Menu inheritance is a one-time operation
- Once inherited, the branch menu is independent
- Restaurant menu changes don't affect existing branches
- Branch can re-inherit menu (will overwrite local changes)
