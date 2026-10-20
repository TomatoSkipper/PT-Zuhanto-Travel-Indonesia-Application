# Implement Multi-Device Sync with Firebase

To allow users to access their data from multiple devices and sign in using different devices, we will integrate Firebase Authentication for secure sign-in and Cloud Firestore for real-time data synchronization.

## User Review Required

> [!IMPORTANT]
> **Firebase Setup**: You will need to create a project in the [Firebase Console](https://console.firebase.google.com/), add an Android app with the package name `com.example.appproject`, and download the `google-services.json` file. Place this file in your `app/` directory.

> [!WARNING]
> **Data Migration**: Existing local users and bookings stored in the Room database will not automatically appear in Firebase. A one-time migration or manual re-registration might be required for existing data.

## Proposed Changes

### Configuration & Dependencies

#### [MODIFY] [libs.versions.toml](file:///C:/Users/harit/Downloads/AppProject/gradle/libs.versions.toml)
- Add versions and library definitions for Firebase BOM, Firebase Auth, and Cloud Firestore.
- Add the `google-services` plugin definition.

#### [MODIFY] [build.gradle.kts (Project)](file:///C:/Users/harit/Downloads/AppProject/build.gradle.kts)
- Add the `google-services` plugin to the plugins block.

#### [MODIFY] [build.gradle.kts (:app)](file:///C:/Users/harit/Downloads/AppProject/app/build.gradle.kts)
- Apply the `com.google.gms.google-services` plugin.
- Add Firebase dependencies (Auth and Firestore).

---

### Authentication Logic

#### [MODIFY] [navbar.kt](file:///C:/Users/harit/Downloads/AppProject/app/src/main/java/com/example/appproject/navbar.kt)
- Update `LoginScreen` to use `FirebaseAuth` for signing in.
- Update `RegisterScreen` to use `FirebaseAuth` for creating new accounts.
- Update `ForgotPasswordScreen` to use Firebase's password reset functionality.
- Integrate Firestore to store and retrieve additional user profile information (like phone number).

---

### Data Synchronization

#### [NEW] [FirebaseRepository.kt](file:///C:/Users/harit/Downloads/AppProject/app/src/main/java/com/example/appproject/FirebaseRepository.kt)
- Create a new repository to handle all Firestore operations for `User` profiles and `Booking` data.
- This will gradually replace or complement the `UserDao` and `BookingDao` logic for multi-device sync.

#### [MODIFY] [navbar.kt](file:///C:/Users/harit/Downloads/AppProject/app/src/main/java/com/example/appproject/navbar.kt)
- Update `BookingDialog` and other booking-related logic to write to and read from Firestore instead of the local Room database.

## Verification Plan

### Automated Tests
- I will verify the build completes successfully after adding dependencies.
- (Optional) I can add unit tests for `FirebaseRepository` using a mock or local emulator if available.

### Manual Verification
- **Sign In/Sign Up**: Verify that a user registered on one device (or emulator) can sign in on another.
- **Data Sync**: Create a booking on one device and verify it appears on another device logged into the same account.
- **Offline Mode**: Verify that Firestore's built-in persistence allows the app to function without an internet connection and syncs once reconnected.
