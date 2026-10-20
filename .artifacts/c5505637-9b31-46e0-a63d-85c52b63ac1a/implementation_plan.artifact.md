# Fix Guest Card Cropping on Phone Displays

The guest testimonial cards in the `GuestStoriesCarousel` are being cropped on phone displays because of a combination of fixed pager height (360dp) and excessive vertical padding (96dp top and bottom) inside the card. This leaves only ~168dp for the icon, stars, quote, name, and country, which is insufficient for longer reviews.

## Proposed Changes

### UI Components

#### [MODIFY] [navbar.kt](file:///C:/Users/harit/Downloads/AppProject/app/src/main/java/com/example/appproject/navbar.kt)
- Increase the `HorizontalPager` height for phone displays from 360dp to 400dp (matching the tablet height or slightly less if needed, but 400dp provides more safety).
- Adjust the vertical padding in the `GuestStoriesCarousel` card's content `Column`.
    - Reduce bottom padding significantly (from 96dp to ~32dp) to give more room for the text.
    - Adjust top padding to ~100dp for phone to ensure it still clears the floating quote icon (88dp).
- Change `Arrangement.SpaceBetween` to `Arrangement.Top` with a `Spacer` or `Arrangement.spacedBy` to ensure better layout stability when content varies in length.

## Verification Plan

### Manual Verification
- Render the `DashboardHomeScreenPreview` to verify the "Guest Stories" section on a phone-sized display.
- Ensure the quote, guest name, and country (e.g., "UK") are all visible without cropping.
