# App Problem Reporting & Admin Feedback Feature

Implement a problem reporting system for users (with photo attachment capabilities from gallery/camera) within the Settings screen, and an admin management screen for viewing all reports, attached pictures, and providing admin feedback.

## User Review Required

> [!IMPORTANT]
> - **Photo Attachment**: Images selected by the user will be converted to Base64 strings (or handled via URI/Base64 storage in Firestore) so they can be reliably stored and retrieved without requiring external cloud storage buckets.
> - **Admin Screen**: Added navigation route for Admin to view and reply to problem reports.

## Open Questions

- None. The architecture follows existing Firebase repository and Compose patterns in the project.

## Proposed Changes

### Data Model & Repository (`com.example.appproject.data.model` & `com.example.appproject.data.repository`)

#### [MODIFY] [DataModels.kt](file:///C:/Users/harit/Downloads/App%20Project/app/src/main/java/com/example/appproject/data/model/DataModels.kt)
- Add `ProblemReport` data class containing `reportId`, `username`, `userEmail`, `title`, `description`, `photoBase64`, `timestamp`, `status`, and `adminFeedback`.

#### [MODIFY] [FirebaseRepository.kt](file:///C:/Users/harit/Downloads/App%20Project/app/src/main/java/com/example/appproject/data/repository/FirebaseRepository.kt)
- Add Firestore methods:
  - `saveProblemReport(report: ProblemReport)`
  - `getAllProblemReports(): List<ProblemReport>`
  - `getProblemReportsForUser(username: String): List<ProblemReport>`
  - `updateProblemReportFeedback(reportId: String, adminFeedback: String, status: String)`

### UI Screens & Navigation (`com.example.appproject.ui.screens` & `com.example.appproject.ui.navigation`)

#### [MODIFY] [AboutSettingsScreens.kt](file:///C:/Users/harit/Downloads/App%20Project/app/src/main/java/com/example/appproject/ui/screens/AboutSettingsScreens.kt)
- Enhance `SettingsScreen` to include a "Report a Problem" section where users can:
  - Fill out problem details (Title, Description).
  - Attach a photo from the gallery using `rememberLauncherForActivityResult(ActivityResultContracts.GetContent())`.
  - Preview the selected photo and submit the report to Firebase.
  - View their submitted reports along with any admin feedback and status.

#### [MODIFY] [AdminScreens.kt](file:///C:/Users/harit/Downloads/App%20Project/app/src/main/java/com/example/appproject/ui/screens/AdminScreens.kt)
- Add `AdminReportsScreen(firebaseRepo: FirebaseRepository)` where admin can:
  - View all user problem reports in a list/grid.
  - Inspect attached photos using `AsyncImage`.
  - Enter and submit admin feedback/response and update report status.

#### [MODIFY] [Navigation.kt](file:///C:/Users/harit/Downloads/App%20Project/app/src/main/java/com/example/appproject/ui/navigation/Navigation.kt)
- Add navigation route `"admin-reports"` and link it from admin navigation drawer/buttons.
- Pass `firebaseRepo` and current user info to `SettingsScreen` and `AdminReportsScreen`.

## Verification Plan

### Automated Tests
- Build test via Gradle (`gradle_build("app:assembleDebug")`).

### Manual Verification
- Deploy app to emulator/device, log in as user, go to Settings, report a problem with a photo attached.
- Log in as admin (`admin`), navigate to Admin Reports screen, verify the report and attached photo appear, and submit admin feedback.
- Log back in as the user, verify admin feedback is displayed in Settings.
