# Readjust Database and Display User Info

The goal is to ensure the `User` database entity is correctly structured and that the **Profile** and **Home Dashboard** screens correctly display the user's username, email, and phone number.

## Proposed Changes

### [Database & Entity]

#### [MODIFY] [User.kt](file:///C:/Users/harit/Downloads/AppProject/app/src/main/java/com/example/appproject/User.kt)
- Ensure all fields are correctly annotated for both Room and Firestore.
- Add `@get:PropertyName` and `@set:PropertyName` to all fields to ensure consistent mapping in Firestore, matching the property names.

### [UI Components]

#### [MODIFY] [Main.kt](file:///C:/Users/harit/Downloads/AppProject/app/src/main/java/com/example/appproject/Main.kt)
- **`DashboardHomeScreen`**:
    - Add `firebaseRepo: FirebaseRepository` as a parameter.
    - Implement a `LaunchedEffect` to fetch the full `User` object using the `username`.
    - Update the UI to display a "My Account" or "User Details" card containing the **Username**, **Email**, and **Phone Number**.
- **`ProfileScreen`**:
    - Verify the existing fields are correctly displaying the data fetched from the repository.
    - Ensure the "Save Changes" logic correctly updates Firestore.
- **`MainApp` (Navigation)**:
    - Update the `composable("dashboard/{username}")` and `composable("dashboard")` routes to pass the `firebaseRepo` to `DashboardHomeScreen`.

## Verification Plan

### Manual Verification
- **Sign Up / Login**: Register a new user and verify all fields are saved.
- **Dashboard**: Log in and verify that the Dashboard now shows the correct username, email, and phone number.
- **Profile**: Navigate to the Profile screen, verify the info matches, update a field (e.g., phone number), save it, and verify the Dashboard updates accordingly.
- **Admin**: Verify that the Admin screen still lists users correctly with all fields.
