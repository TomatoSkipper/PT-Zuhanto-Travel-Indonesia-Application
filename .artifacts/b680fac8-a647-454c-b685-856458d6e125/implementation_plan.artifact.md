# Fix crash in AdminUsersScreen when banning/deleting new users

The crash is caused by using `user.id` for list updates when multiple users have `id = 0`. This causes duplicate keys in `LazyVerticalGrid` (which uses `username` as key) because the mapping logic incorrectly updates multiple users or fails to uniquely identify them.

## Proposed Changes

### [Component Name] UI Layer

#### [MODIFY] [navbar.kt](file:///C:/Users/harit/Downloads/AppProject/app/src/main/java/com/example/appproject/navbar.kt)

- Update `AdminUsersScreen` confirm button logic to use `username` for list updates instead of `id`.
- Update `AdminUserAccountCard` to display `username` as the primary identifier instead of `id`.

## Verification Plan

### Manual Verification
- Deploy the app.
- Log in as Admin.
- Navigate to "User Account".
- Create multiple new users (if possible, or ensure they have ID 0).
- Try to "Ban" one of them.
- Verify the app doesn't crash and the correct user is updated in the list.
- Try to "Delete" a user and verify it works correctly.
